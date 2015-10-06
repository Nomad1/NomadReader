package net.runserver.library.metaData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public final class MetaDataDB
{
	private final static String TABLE_NAME = "metadata";
	private final static String DB_NAME = "metadata.db";
	private final static String DB_FIELDS = "id integer primary key, title, author, description, series, mime, part integer, flags integer, fileName, fileSize integer, fileMod integer, lastPath";
	//private final static String SELECT_STRING = "select * from " + TABLE_NAME + " where id = ?";
	
	private final HashMap<Integer, MetaData> m_metaDataCache = new HashMap<Integer, MetaData>(1000, 0.5f);

	private final SQLiteDatabase m_db;
	private final SQLiteOpenHelper m_helper;
	private final SQLiteStatement m_checkStatement;
	private final Context m_context;

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		private final int m_version;

		DatabaseHelper(Context context, String databaseName, int version)
		{
			super(context, databaseName, null, version);
			m_version = version;
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			onUpgrade(db, 0, m_version);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("drop table if exists " + TABLE_NAME);
			db.execSQL("create table " + TABLE_NAME + " ( " + DB_FIELDS + ")");
		}
	}

	public MetaDataDB(Context context)
	{
		m_helper = new DatabaseHelper(context, DB_NAME, 1);
		m_db = m_helper.getWritableDatabase();
		m_checkStatement = m_db.compileStatement("select count(*) from " + TABLE_NAME + " where id = ?");
		
		cacheAll();
		m_context = context;
	}

	public void close()
	{
		flush();
		m_db.close();
		m_helper.close();
	}

	public MetaData getMetaData(String fileName, long fileLength, long fileMod)
	{
		int hash = MetaData.getHashCode(fileName, fileLength, fileMod);

		return m_metaDataCache.get(hash);
	}
	
	public void putMetaDataDelayed(MetaData data)
	{
		((Activity)m_context).runOnUiThread(new MetaDataPutRunnable(data, this));
	}

	public void putMetaData(MetaData data)
	{
		int id = data.hashCode();
		m_metaDataCache.put(id, data);
		
		boolean insert = !exist(id);
		
		//Log.d("FileBrowser", "Checking data ");
		
		ContentValues values = new ContentValues();
		if (insert)
			values.put("id", Integer.toString(id));
		
		values.put("fileName", data.getFileName());
		values.put("fileSize", data.getFileSize());
		values.put("fileMod", data.getFileMod());
		values.put("title", data.getTitle());
		values.put("author", data.getAuthor() + "|" + data.getAuthorSortName());
		values.put("description", data.getDescription());
		values.put("series", data.getSeries());
		values.put("mime", data.getMimeType());
		values.put("part", Integer.toString(data.getPart()));
		values.put("flags", Integer.toString(data.getFlags()));
		values.put("lastPath", data.getLastPath());

		//Log.d("FileBrowser", "Going to insert meta data: id " + id);
		
		if (!m_db.inTransaction())
		{
			m_db.beginTransaction();				
			//Log.d("FileBrowser", "Begin transaction for id " + id + ", insert " + insert);
		}
		// long result;
		if (insert)
			m_db.insert(TABLE_NAME, null, values);
		else
			m_db.update(TABLE_NAME, values, "id = ?", new String[]{Integer.toString(id)});			
	}

	/*private List<String[]> query(int id)
	{
		Cursor cursor = m_db.rawQuery(SELECT_STRING, new String[]{Integer.toString(id)});
		try
		{
			//Log.d("FileBrowser", "Selecting data for id " + id + " resulted in cursor " + cursor + ", count " + cursor.getCount());
			
			if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0)
			{
				List<String[]> result = new ArrayList<String[]>(cursor.getCount());

				cursor.moveToFirst();

				do
				{
					String[] data = new String[cursor.getColumnCount()];

					for (int i = 0; i < cursor.getColumnCount(); i++)
						data[i] = cursor.getString(i);

					result.add(data);
				} while (cursor.moveToNext());

				return result;
			}
		}
		finally
		{
			if (cursor != null)
				cursor.close();
		}
		return null;
	}*/
	
	private void cacheAll()
	{
		Cursor cursor = m_db.rawQuery("select * from " + TABLE_NAME, null);
		try
		{
			//Log.d("FileBrowser", "Selecting data for id " + id + " resulted in cursor " + cursor + ", count " + cursor.getCount());
			
			if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				Map<String, String> table = new HashMap<String, String>(cursor.getColumnCount());

				do
				{
					for (int i = 0; i < cursor.getColumnCount(); i++)
					{
						String ndata = cursor.getString(i);
						if (ndata == null)
							ndata = "";
						//Log.d("FileBrowser", "Adding column id " + cursor.getColumnName(i) + ", value " + ndata);
						table.put(cursor.getColumnName(i), ndata);
					}

					int id = Integer.parseInt(table.get("id"));
					
					MetaData result = new MetaData(
							table.get("title"),
							table.get("author"),
							table.get("description"),
							table.get("series"),
							table.get("mime"),
							Integer.parseInt(table.get("part")),
							Integer.parseInt(table.get("flags")),
							table.get("fileName"),
							Long.parseLong(table.get("fileSize")),
							Long.parseLong(table.get("fileMod")));
					
					if (result.hashCode() != id)
					{
						delete(id);
						putMetaData(result);
					} else
						m_metaDataCache.put(id, result);				
						
					result.setLastPath(table.get("lastPath"));
					
				} while (cursor.moveToNext());
			}
		}
		finally
		{
			if (cursor != null)
				cursor.close();
		}		
	}

	private boolean exist(int id)
	{
		m_checkStatement.bindLong(1, id);
		return m_checkStatement.simpleQueryForLong() > 0;
	}

	public int delete(int id)
	{
		return m_db.delete(TABLE_NAME, "id = " + id, null);
	}
	
	public void flush()
	{
		if (m_db.inTransaction())
		{
			Log.d("FileBrowser", "Flushing database");

			try
			{
				m_db.setTransactionSuccessful();
				m_db.endTransaction();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public List<MetaData> getAllBooks()
	{
		List<MetaData> result = new ArrayList<MetaData>(m_metaDataCache.size());		
		
		for (MetaData entry : m_metaDataCache.values())
		{
			if (entry.isBook() && entry.getLastPath() != null)
			{
				File file = new File(entry.getLastPath());
				if (file.exists())
					result.add(entry);
			}
		}
		
		return result;
	}
	
	public List<MetaData> getAllAuthorBooks(String author, int count)
	{
		List<MetaData> result = new ArrayList<MetaData>(count);		
	
		boolean isOther = author.equals("Other");
		if (isOther)
			author = "";
		
		for (MetaData entry : m_metaDataCache.values())
		{
			if (entry.isBook() && entry.getLastPath() != null && author.equals(entry.getAuthorSortName()))
			{
				File file = new File(entry.getLastPath());
				if (file.exists())
					result.add(entry);
			}
		}
		
		return result;
	}
	
	public static final class MetaDataPutRunnable implements Runnable
	{
		private final MetaData m_metaData;
		private final MetaDataDB m_db;
		
		public MetaDataPutRunnable(MetaData metaData, MetaDataDB db)
		{
			m_metaData = metaData;
			m_db = db;
		}
		
		public void run()
		{
			m_db.putMetaData(m_metaData);
		}
	}	
}

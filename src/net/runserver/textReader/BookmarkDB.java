package net.runserver.textReader;
/*
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
import android.util.Log;*/

public final class BookmarkDB
{
	/*
	private final static String TABLE_NAME = "bookmarks";
	private final static String DB_NAME = "bookmarks.db";
	private final static String DB_FIELDS = "id integer primary key, bookName, bookSize integer, type integer, page integer, position long, font, fontSize integer, averagePage integer";
	
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

	public BookmarkDB(Context context)
	{
		m_helper = new DatabaseHelper(context, DB_NAME, 1);
		m_db = m_helper.getWritableDatabase();
		m_checkStatement = m_db.compileStatement("select count(*) from " + TABLE_NAME + " where id = ?");
		
		m_context = context;
	}

	public void close()
	{
		flush();
		m_db.close();
		m_helper.close();
	}

	public void putBookmark(Bookmark data)
	{
		int id = hash.hashCode();	
		
		boolean insert = !exist(id);
		
		//Log.d("FileBrowser", "Checking data ");
		
		ContentValues values = new ContentValues();
		if (insert)
		{
			values.put("id", Integer.toString(id));
			values.put("fileName", hash.getFileName());
			values.put("fileSize", hash.getFileSize());
			values.put("fileMod", hash.getFileMod());
		}
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
	
	public Bookmark [] getBookmarks(String fileName, int fileSize, int type)
	{
		Cursor cursor = m_db.rawQuery("select * from " + TABLE_NAME + " where fileName=" + fileName + " and fileSize=" + fileSize + " and type=" + type, null);
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
						table.put(cursor.getColumnName(i), ndata);
					}

					String fname = table.get("fileName");
					long fsize = Long.parseLong(table.get("fileSize"));
					long fmod = Long.parseLong(table.get("fileMod"));
					
					
					FileHash hash = new FileHash(fname, fsize, fmod);
					
					MetaData result = new MetaData(table.get("title"), table.get("author"), table.get("description"), table.get("series"),
							table.get("mime"), Integer.parseInt(table.get("part")), Integer.parseInt(table.get("flags")), hash);
					
					result.setLastPath(table.get("lastPath"));

					m_metaDataCache.put(hash, result);				
					
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
	
	
	public class Bookmark
	{
		
	}*/	
}

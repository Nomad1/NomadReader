package net.runserver.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBSettings
{
	private final static String TABLE_NAME = "settings";
	private final static String DB_NAME = "settings.db";

	private final SQLiteDatabase m_db;
	private final SQLiteOpenHelper m_helper;
	private final boolean m_writable;

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
			db.execSQL("create table " + TABLE_NAME + " ( _id integer primary key, name, data)");
		}
	}

	public DBSettings(Context context, boolean writable)
	{
		m_helper = new DatabaseHelper(context, DB_NAME, 1);
		m_writable = writable;
		if (writable)
		{
			m_db = m_helper.getWritableDatabase();
		} else
			m_db = m_helper.getReadableDatabase();
	}	

	public DBSettings(Context context)
	{
		m_helper = new DatabaseHelper(context, DB_NAME, 1);
		m_db = m_helper.getReadableDatabase();
		m_writable = false;
	}
	
	public void close()
	{		
		m_db.close();
		m_helper.close();
	}
	
	public void putString(String name, String value)
	{
		insertOrUpdate(name, value);
	}
	
	public void putInt(String name, int value)
	{
		insertOrUpdate(name, Integer.toString(value));
	}

	public void putFloat(String name, float value)
	{
		insertOrUpdate(name, Float.toString(value));
	}
	
	public void putBoolean(String name, boolean value)
	{
		insertOrUpdate(name, value ? "1" : "0");
	}
	
	public void putLong(String name, long value)
	{
		insertOrUpdate(name, Long.toString(value));
	}
	
	public String getString(String name, String def)
	{
		String result = query(name);
		return result == null ? def : result;
	}
	
	public int getInt(String name, int def)
	{
		String result = query(name);
		if (result == null)
			return def;
		
		try
		{
			return Integer.parseInt(result);			
		}
		catch(NumberFormatException ex)
		{
			return def;
		}
	}
	
	public float getFloat(String name, float def)
	{
		String result = query(name);
		if (result == null)
			return def;
		
		try
		{
			return Float.parseFloat(result);			
		}
		catch(NumberFormatException ex)
		{
			return def;
		}
	}
	
	public boolean getBoolean(String name, boolean def)
	{
		String result = query(name);
		if (result == null)
			return def;
		
		try
		{
			return result.equals("1");			
		}
		catch(NumberFormatException ex)
		{
			return def;
		}
	}
	
	public long getLong(String name, long def)
	{
		String result = query(name);
		if (result == null)
			return def;
		
		try
		{
			return Long.parseLong(result);			
		}
		catch(NumberFormatException ex)
		{
			return def;
		}
	}

	private void insertOrUpdate(String name, String data)
	{
		if (!m_writable)
			throw new SQLiteException("Database opened as read only!");
		
		boolean insert = !exist(name);
		ContentValues values = new ContentValues();
		if (insert)
		{
			values.put("_id", Integer.toString(name.hashCode()));
			values.put("name", name);
		}
		values.put("data", data);
		
		//long result;
		if (insert)
		{
			/*result = */m_db.insert(TABLE_NAME, null, values);
		} else
			/*result = */m_db.update(TABLE_NAME, values, "_id = " + Integer.toString(name.hashCode()), null);
		
		//Log.d("DBSettings", (insert ? "inserting" : "updating") + " data " + data + " to record "  + name + ", resulted " + result);
	}
	
	private String query(String name)
	{
		Cursor cursor = m_db.rawQuery("select data from " + TABLE_NAME + " where _id = " + Integer.toString(name.hashCode()), null);
		try
		{
			//Log.d("DBSettings", "Selecting data for record " + name + " resulted in cursor " + cursor + ", count " + cursor.getCount());
			if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				return cursor.getString(0);
			}			
		}
		finally
		{
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	
	private boolean exist(String name)
	{
		Cursor cursor = m_db.rawQuery("select data from " + TABLE_NAME + " where _id = " + Integer.toString(name.hashCode()), null);
		try
		{
			return cursor != null && cursor.getCount() > 0;
		}
		finally
		{
			if (cursor != null)
				cursor.close();
		}
	}

	public int delete(String name)
	{
		if (!m_writable)
			throw new SQLiteException("Database opened as read only!");
		
		return m_db.delete(TABLE_NAME, "_id = " + name.hashCode(), null);
	}	
}

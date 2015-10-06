package net.runserver.bookParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageData implements Serializable
{
	private static final long serialVersionUID = 2L;

	private final String m_name;
	private byte[] m_data;
	private final int m_offset;
	private final int m_length;
	private InputStream m_stream;
	
	transient private int m_width;
	transient private int m_height;	
	transient private SoftReference<Bitmap> m_bitmap;
	transient private boolean m_inited;
	
	private String m_cacheFile; 

	public String getName()
	{
		return m_name;
	}

	public byte[] getData()
	{
		return m_data;
	}
	
	public int getWidth()
	{
		if (!m_inited)
			init(null);
		return m_width;
	}

	public int getHeight()
	{
		if (!m_inited)
			init(null);
		return m_height;
	}
	
	public ImageData(String name, InputStream stream, int length) throws IOException
	{
		/*m_data = new byte[length];
		int pos = 0;
		
		while (stream.available() > 0 && pos < length)
			pos += stream.read(m_data, pos, length - pos);
*/
		m_stream = stream;
		m_offset = 0;
		m_length = length;

		m_name = name;
	}

	public ImageData(String name, byte [] data, int offset, int length) throws IOException
	{
		m_data = data;
		m_offset = offset;
		m_length = length;
		m_name = name;
	}
	
	public void init(String cachePath)
	{
		if (m_inited)
			return;
		
		if (cachePath != null)
		{
			cacheFile(cachePath);
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		if (m_cacheFile != null)
			BitmapFactory.decodeFile(m_cacheFile, options);
		else
			BitmapFactory.decodeByteArray(m_data, m_offset, m_length, options);
		
		m_width = options.outWidth;
		m_height = options.outHeight;
		m_inited = true;		
	}

	private void cacheFile(String cachePath)
	{
		try
		{
			int ppos = m_name.lastIndexOf("/");
			if (ppos == -1)
				m_cacheFile = cachePath + "/" + m_name;
			else
				m_cacheFile = cachePath + "/" + m_name.substring(ppos + 1);
			
			m_cacheFile += m_length;
			
			File file = new File(m_cacheFile);
			if (file.exists() && file.length() == m_length)
			{
				Log.d("TextReader", "Using existing image file: " + m_cacheFile);
				m_data = null; // clean up	
				m_stream = null;
				return;
			}
						
			if (m_data == null)
			{
				if (m_stream == null)
					return;
				
				m_data = new byte[m_length];
				int pos = 0;
				
				while (m_stream.available() > 0 && pos < m_length)
					pos += m_stream.read(m_data, pos, m_length - pos);
				
				m_stream = null;
			}
			
			file.createNewFile();
			
			FileOutputStream stream = new FileOutputStream(m_cacheFile);
			stream.write(m_data, m_offset, m_length);
			stream.close();
			
			Log.d("TextReader", "Cached image to file " + m_cacheFile);
			
			m_data = null; // clean up
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			m_cacheFile = null;
		}
	}
	
	public Bitmap extractImage(int maxWidth, int maxHeight)
	{
		Bitmap result = m_bitmap != null ? m_bitmap.get() : null;
		
		if (result != null && !result.isRecycled())
			return result;

		try
		{
			init(null);
			BitmapFactory.Options options = new BitmapFactory.Options();
			
			int width = m_width;
			int height = m_height;
			options.inSampleSize = 1;
			options.inPreferredConfig = /*BaseActivity.isNook || BaseActivity.isNookTouch || BaseActivity.isEmulator ? Bitmap.Config.ALPHA_8 : */Bitmap.Config.RGB_565;
			
			while (width/2 >= maxWidth && height/2 >= maxHeight)
			{
				options.inSampleSize *= 2;
				width /= 2;
				height /= 2;
			}
			if (options.inSampleSize != 1)
				Log.d("TextReader", "Image size is " + m_width + ", " + m_height + ", while needed size is " + maxWidth + ", " + maxHeight + ", using sample size " + options.inSampleSize);

			//options.
			
			if (m_cacheFile != null && m_data == null)
			{
				try
				{
					result = BitmapFactory.decodeFile(m_cacheFile, options);
				}
				catch (OutOfMemoryError ex)
				{
					System.gc();
					result = BitmapFactory.decodeFile(m_cacheFile, options);
				}
			}
			else
			{
				if (m_data == null && m_stream != null)
				{
					m_data = new byte[m_length];
					int pos = 0;
					
					while (m_stream.available() > 0 && pos < m_length)
						pos += m_stream.read(m_data, pos, m_length - pos);
					
					m_stream = null;
				}
				
				if (m_data != null)
					result = BitmapFactory.decodeByteArray(m_data, m_offset, m_length, options);
			}
			if (result != null && !result.isRecycled())
			{
				m_bitmap = new SoftReference<Bitmap>(result);
			}
			else
				m_bitmap = null;
			return result;
		}
		catch (OutOfMemoryError ex)
		{
			ex.printStackTrace();
			m_bitmap = null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			m_bitmap = null;
		}
		return null;
	}
	
	public void clean()
	{
		Bitmap result = m_bitmap != null ? m_bitmap.get() : null;
		
		if (result != null && !result.isRecycled())
			result.recycle();
		
		m_data = null;		
	}

}

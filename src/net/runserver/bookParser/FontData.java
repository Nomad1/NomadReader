package net.runserver.bookParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class FontData implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final String m_name;
	//private byte[] m_data;
	private final int m_length;
	private InputStream m_stream;
	
	transient private String m_file;
	
	public String getName()
	{
		return m_name;
	}

	public FontData(String name, InputStream stream, int length) throws IOException
	{
		/*m_data = new byte[length];
		int pos = 0;

		while (stream.available() > 0 && pos < length)
			pos += stream.read(m_data, pos, length - pos);
*/
		m_stream = stream;
		m_length = length;
		
		if (name.indexOf("/") != -1)
			m_name = name.substring(name.lastIndexOf("/"));
		else
			m_name = name;
	}	
	
	public String extractFont(String path)
	{
		if (m_file != null)
			return m_file;
		
		try
		{
			m_file = path + "/" + m_name;
			File file = new File(m_file);
			if (file.exists() && file.length() == m_length)
			{
				m_stream = null; // clean up				
				return m_file;
			}
			
			if (m_stream == null)
				return null;
			
			file.createNewFile();			
			
			FileOutputStream stream = new FileOutputStream(m_file);
			
			byte [] buffer = new byte[0x1000];
			int pos = 0;
			while (m_stream.available() > 0 && pos < m_length)
			{
				int read = m_stream.read(buffer, 0, buffer.length);			
				stream.write(buffer, 0, read);
				pos += read;
			}
			stream.close();
			
			m_stream = null; // clean up
			return m_file;			
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			m_file = null;
		}
		return null;
	}
}

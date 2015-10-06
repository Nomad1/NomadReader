package net.runserver.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class InputByteStream
{	
	private int m_position;
	private int m_size;
	private byte [] m_buffer;
	private CustomCharset m_charset;
	
	public int getSize()
	{
		return m_size;
	}
	
	public int available()
	{		
		return m_size - m_position;
	}
	
	public int getPosition()
	{
		int result = m_position;
		if (m_position >= m_size)
			result = m_size;

		return result;
	}
	
	public byte [] getBuffer()
	{
		return m_buffer;
	}
	
	public CustomCharset getCharset()
	{
		return m_charset;
	}
	
	public byte getByte(int position)
	{
		return m_buffer[position];
	}

	public InputByteStream(InputStream stream, int size)
	{
		m_charset = SystemCharset.defaultCharset();
		int pos = 0;	
				
		try
		{		
			int toCopy = size;
			
			m_buffer = new byte[size];
			
			while (stream.available() > 0 && pos < toCopy)
				pos += stream.read(m_buffer, pos, size - pos);
			
			m_size = pos;
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			//m_nativeBuffer = null;
			m_buffer = null;
			m_size = 0;
		}
	}
	
	public InputByteStream(byte [] data, int size)
	{
		m_charset = SystemCharset.defaultCharset();
		m_buffer = data;
		m_size = size;
	}
	
	public void clean()
	{
		//m_nativeBuffer = null;
		m_buffer = null;	
	}
	/*
	public byte readByte()
	{
		if (m_position >= m_size)
			return 0;

		return m_buffer[m_position++];
	}
	
	public int readBe()
	{
		if (m_position + 4 >= m_size)
			return 0;

		int result = ((m_buffer[m_position]&0xFF) << 24) + ((m_buffer[m_position + 1]&0xFF)<<16) + ((m_buffer[m_position + 2]&0xFF) << 8) + ((m_buffer[m_position + 3]&0xFF));		
		m_position += 4;		
		return result;
	}
	
	public int read()
	{
		if (m_position + 4 >= m_size)
			return 0;

		int result = (m_buffer[m_position]&0xFF) + ((m_buffer[m_position + 1]&0xFF)<<8) + ((m_buffer[m_position + 2]&0xFF) << 16) + ((m_buffer[m_position + 3]&0xFF) << 24);		
		m_position += 4;		
		return result;
	}
	
	public int readShortBe()
	{
		if (m_position + 2 >= m_size)
			return 0;

		int result = ((m_buffer[m_position]&0xFF) << 8) | (m_buffer[m_position + 1] & 0xFF);		
		m_position += 2;		
		return result;
	}
	
	public int readShort()
	{
		if (m_position + 2 >= m_size)
			return 0;

		int result = (m_buffer[m_position] & 0xFF) | ((m_buffer[m_position + 1] & 0xFF) << 8);		
		m_position += 2;		
		return result;
	}
	*/
	public byte peekByte()
	{
		if (m_position >= m_size)
			return 0;

		return m_buffer[m_position];
	}
	
	/*public boolean skip(int value)
	{	
		if (m_position + value>= m_size)
			return false;

		m_position += value;
		return true;
	}
*/
	public boolean skipTo(byte stop/*, boolean advance*/)
	{	
		for(int i=m_position; i<m_size; i++)
		{
			if (m_buffer[i] == stop)
			{
				m_position = i + 1;
				return true;
			}
		}
		m_position = m_size;
		return false;
	}
	
	public static class SearchPattern
	{
	    private final byte [] m_skip;
	    private final int [] m_occ;
		private final byte [] m_bytes;
		
		public byte [] getBytes()
		{
			return m_bytes;
		}
		
		public byte [] getSkip()
		{
			return m_skip;
		}
		
		public int[] getOcc()
		{
			return m_occ;
		}
		
	    public SearchPattern(String value)
	    {
	    		this(value.getBytes());
	    }
	    
	    public SearchPattern(byte [] value)
	    {
		    	m_bytes = value;
		    	int nlen = m_bytes.length;
		    	m_skip = new byte[nlen];
		    	m_occ = new int[256];
	    	
		    for(int i=0; i < m_occ.length; i++)
		        m_occ[i] = -1;
		 
		    for(byte i = 0; i < nlen - 1; i++) 
		        m_occ[m_bytes[i]&0xFF] = i;
		 
		    for(byte a = 0; a < nlen; ++a)
		    {
		        int offs = nlen;
		        while(offs != 0 && !suffix_match(m_bytes, nlen, offs, a))
		            --offs;
		        m_skip[nlen - a - 1] = (byte)(nlen - offs);
		    }	    
	    }
	    
		private static boolean memcmp(byte [] src, int srcOffset, byte [] dst, int dstOffset, int length)
		{
			for(int i=0;i<length;i++)
				if (src[i + srcOffset] != dst[i + dstOffset])
					return false;
			return true;
		}
		
		private static boolean suffix_match(byte [] needle, int nlen, int offset, int suffixlen)
		{
		    if (offset > suffixlen)
		        return needle[offset - suffixlen - 1] != needle[nlen - suffixlen - 1] && memcmp(needle, nlen - suffixlen, needle, offset - suffixlen, suffixlen);
		    else
		        return memcmp(needle, nlen - offset, needle, 0, offset);
		}
		
	}
	 
	public int indexOf(SearchPattern pattern)
	{
		byte [] needle = pattern.getBytes(); 
		byte [] skip = pattern.getSkip();
		int [] occ = pattern.getOcc();	   
		int nlen = needle.length;
	 
	    for(int hpos = m_position; hpos <= m_size - nlen; )
	    {
	    	int npos = nlen - 1;
	        while(needle[npos] == m_buffer[npos + hpos])
	        {
	            if(npos == 0) 
	                return hpos;
	 
	            --npos;
	        }
	        int n = skip[npos];
	        int m = npos - occ[m_buffer[npos + hpos]&0xFF];
	        if (n > m)
	        	hpos += n;
	        else
	        	hpos += m;
	    }
	    return -1;
	}
	
	public boolean skipTo(byte[] pattern, boolean advance)
	{		
		int patternLength = pattern.length;
		//if (pattern[patternLength - 1] == 0)
			//patternLength--;
		
		if (m_size - m_position < patternLength)
		{
			m_position = m_size;
			return false;
		}
		
		int j = 0;
		for(int i=m_position; i<m_size; i++)
		{
			if (m_buffer[i] == pattern[j])
			{
				j++;
				if (j == patternLength)
				{
					if (advance)
						m_position = i + 1;
					else
						m_position = i - patternLength;
					return true;
				}
			} else
				j = 0;
		}
		m_position = m_size;
		return false;
		/*
		
		int i = indexOf(new SearchPattern(pattern));
		if (i == -1)
		{
			m_position = m_size;
			return false;
		}
		if (advance)
			m_position = i + 1;
		else
			m_position = i - pattern.length;
		return true;*/
	}

	public void reset(int position)
	{
		m_position = position;		
	}

	public ByteCharSequence getString(int start, int length)
	{
		if (start + length > m_size)
			length = m_size - start;
		
		return new ByteCharSequence(m_buffer, start, length, m_charset);
	}
	
	public ByteCharSequence getAsciiString(int start, int length)
	{
		if (start + length > m_size)
			length = m_size - start;
		
		return new ByteCharSequence(m_buffer, start, length, null);
	}
	
	public ByteCharSequence getAsciiLowerString(int start, int length)
	{
		//byte [] bytes = new byte [length];
		for(int i=0;i<length;i++)
		{
			byte b = m_buffer[start+i];
			if (b >= 65 && b <= 90)
				m_buffer[start+i] = (byte)(b+32);
		}
		return new ByteCharSequence(m_buffer, start, length, null);
		//return new ByteCharSequence(chars, 0, length); 
	}
	
	public void setCharset(String name)
	{	
		try
		{
			if (name.equalsIgnoreCase("windows-1251") || name.equalsIgnoreCase("CP1251"))
				m_charset = new Charset1251();
			else
				m_charset = new SystemCharset(Charset.forName(name));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			m_charset = SystemCharset.defaultCharset();
		}
	}

	public void replaceChars(int start, int length, byte[] byteArray)
	{
		for(int i=0;i<length;i++)
		{
			m_buffer[start+i] = i >= byteArray.length ? 0 : byteArray[i];
		}
		
	}
}
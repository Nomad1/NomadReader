package net.runserver.common;

import java.nio.CharBuffer;

public final class ByteCharSequence implements CharSequence
{
	private final int m_offset;
	private final int m_length;
	private final byte[] m_buffer;
	private final CustomCharset m_charset;
	private int m_hashCode;

	@Override
	public int length()
	{
		return m_length;
	}

	@Override
	public char charAt(int pos)
	{
		return (char) m_buffer[m_offset + pos];
	}

	public byte byteAt(int pos)
	{
		return m_buffer[m_offset + pos];
	}

	public byte[] getBytes()
	{
		return m_buffer;
	}

	public int getOffset()
	{
		return m_offset;
	}

	@Override
	public CharSequence subSequence(int from, int to)
	{
		int length = to - from;
		if (from + length > m_length)
			length = m_length - from;
		
		return new ByteCharSequence(m_buffer, m_offset + from, length, m_charset);
	}

	public ByteCharSequence(String string)
	{
		m_buffer = string.getBytes();
		m_offset = 0;
		m_length = m_buffer.length;
		m_charset = SystemCharset.defaultCharset();
	}

	public ByteCharSequence(byte[] buffer, int offset, int length)
	{
		m_buffer = buffer;
		m_offset = offset;
		m_length = length;
		m_charset = SystemCharset.defaultCharset();
	}

	public ByteCharSequence(byte[] buffer, int offset, int length, CustomCharset charset)
	{
		m_buffer = buffer;
		m_offset = offset;
		m_length = length;
		m_charset = charset == null ? NullCharset.instance() : charset;
	}

	public CharBuffer toCharBuffer(int start, int length)
	{
		if (start + length > m_length)
			length = m_length - start;
		
		return m_charset.decode(m_buffer, m_offset + start, length);
	}
	/*

	public String toString(int start, int length)
	{
		if (start + length > m_length)
			length = m_length - start;
		if (length <= 0)
			return "";

		if (m_charset == null)
		{
			char[] chars = new char[length];
			int offset = m_offset + start;
			int i = 0;
			while(i < length)
			{
				chars[i] = (char) m_buffer[offset];
				i++;
				offset++;				
			}
			
			return new String(chars);

			// return EncodingUtils.getAsciiString(m_buffer, m_offset + start,
			// length);
		}

		return m_charset.decode(m_buffer, m_offset + start, length);
		// new String(m_buffer, m_offset + start, length);
	}*/

	public String toString()
	{
		return toCharBuffer(0, m_length).toString();
	}

	public int indexOf(byte[] pattern, int offset)
	{
		int patternLength = pattern.length;
		if (patternLength > m_length)
			return -1;
		//if (pattern[patternLength - 1] == 0)
			//patternLength--;

		int j = 0;
		int start = m_offset + offset;
		int end = m_offset + m_length;
		for (int i = start; i < end; i++)
		{
			if (m_buffer[i] == pattern[j])
			{
				j++;
				if (j == patternLength)
					return i - patternLength - m_offset + 1;
			} else
			{
				j = 0;
				if (end - i <= patternLength)
					break;
			}
		}
		return -1;
	}

	public int hashCode()
	{
		if (m_hashCode == 0)
		{
			int multiplier = 1;
			int offset = m_offset;
			int count = m_length;
			byte[] value = m_buffer;
			for (int i = offset + count - 1; i >= offset; i--)
			{
				m_hashCode += value[i] * multiplier;
				int shifted = multiplier << 5;
				multiplier = shifted - multiplier;
			}
		}
		return m_hashCode;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ByteCharSequence)
			return equals((ByteCharSequence)o);
		if (o instanceof CharSequence)
			return o.equals(toString());
		return super.equals(o);
	}
	
	public boolean equals(ByteCharSequence value)
	{
		int len = value.m_length; 
		if (len != m_length)
			return false;
		
		while(--len >= 0)
		{
			if (value.m_buffer[len + value.m_offset] != m_buffer[len + m_offset])
				return false;
		}
		
		return true;
	}
	
	public CharSequence optimize()
	{
		if (m_buffer.length <= 0x1000)
			return this;
		
		byte [] buffer = new byte[m_length];
		System.arraycopy(m_buffer, m_offset, buffer, 0, m_length);
		return new SimpleByteSequence(buffer, m_charset);
	}
}
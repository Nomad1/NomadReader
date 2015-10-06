package net.runserver.common;

import java.nio.CharBuffer;

public final class SimpleByteSequence implements CharSequence
{
	private final CustomCharset m_charset;
	private final byte[] m_buffer;

	@Override
	public int length()
	{
		return m_buffer.length;
	}

	@Override
	public char charAt(int pos)
	{
		return (char) m_buffer[pos];
	}
	
	@Override
	public CharSequence subSequence(int from, int to)
	{
		int length = to - from;
		if (from + length > m_buffer.length)
			length = m_buffer.length - from;
		return new ByteCharSequence(m_buffer, from, length, null);
	}

	public SimpleByteSequence(byte[] buffer, CustomCharset charset)
	{
		m_buffer = buffer;
		m_charset = charset == null ? NullCharset.instance() : charset;
	}
	
	public CharBuffer toCharBuffer(int start, int length)
	{		
		if (start + length > m_buffer.length)
			length = m_buffer.length - start;

		return m_charset.decode(m_buffer, start, length);
	}
	
	@Override
	public String toString()
	{		
		return toCharBuffer(0, m_buffer.length).toString();
	}
}

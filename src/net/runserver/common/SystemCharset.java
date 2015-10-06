package net.runserver.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class SystemCharset extends CustomCharset
{
	private static CustomCharset s_defaultCharset = null;
	
	public static CustomCharset defaultCharset()
	{
		if (s_defaultCharset == null)
			s_defaultCharset= new SystemCharset(Charset.defaultCharset());
		return s_defaultCharset;
	}
	
	
	private final Charset m_charset;
	
	public SystemCharset(Charset charset)
	{
		m_charset = charset;
	}

	@Override
	public CharBuffer decode(byte[] buffer, int start, int length)
	{
		return m_charset.decode(ByteBuffer.wrap(buffer, start, length));
	}
}

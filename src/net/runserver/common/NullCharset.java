package net.runserver.common;

import java.nio.CharBuffer;

public class NullCharset extends CustomCharset
{
	private static CustomCharset s_instance = null;
	
	public static CustomCharset instance()
	{
		if (s_instance == null)
			s_instance= new NullCharset();
		return s_instance;
	}
	
	@Override
	public CharBuffer decode(byte[] buffer, int start, int length)
	{
		char [] result = new char[length];
		
		int bytepos = start;
		int pos = 0;
		while(pos<length)
		{
			result[pos++] = (char)(buffer[bytepos++] & 0xff);
		}
		
		return CharBuffer.wrap(result);
	}
}

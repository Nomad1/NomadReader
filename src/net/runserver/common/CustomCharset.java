package net.runserver.common;

import java.nio.CharBuffer;

import org.apache.http.util.EncodingUtils;

import android.util.Log;

public abstract class CustomCharset
{
	public abstract CharBuffer decode(byte[] buffer, int start, int length);
	
	public String processString(String str)
	{
		if (str == null || str.length() == 0)
			return str;
		
		Log.d("TextReader", "Processing string " + str);
		
		byte [] bytes = EncodingUtils.getBytes(str, "UTF-7");
		return decode(bytes, 0, bytes.length).toString();
	}
}

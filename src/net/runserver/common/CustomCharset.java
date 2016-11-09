package net.runserver.common;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

import android.util.Log;

public abstract class CustomCharset
{
	public abstract CharBuffer decode(byte[] buffer, int start, int length);
	
	public String processString(String str)
	{
		if (str == null || str.length() == 0)
			return str;
		
		Log.d("TextReader", "Processing string " + str);
		
        byte[] bytes;
        try {
            bytes = str.getBytes("UTF-7");
        } catch (UnsupportedEncodingException e) {
            bytes = str.getBytes();
        }
		return decode(bytes, 0, bytes.length).toString();
	}
}

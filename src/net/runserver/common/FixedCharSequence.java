package net.runserver.common;

import java.lang.reflect.Field;
import java.nio.CharBuffer;

import android.util.Log;

public class FixedCharSequence implements CharSequence
{	
	private static final Field s_stringValue;
	
	static
	{
		Field field = null;
		try
		{
			field = String.class.getDeclaredField("value");
			field.setAccessible(true);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		s_stringValue = field;
	}
	
	private final int m_offset;
	private final int m_length;
	private final char [] m_buffer;
	private int m_hashCode;
	
	@Override
	public int length()
	{
		return m_length;
	}
	
	@Override
	public char charAt(int pos)
	{
		return m_buffer[m_offset + pos];
	}
	
	public char [] getChars()
	{
		return m_buffer;
	}
	
	public int getOffset()
	{
		return m_offset;
	}
	
	public void setChar(int pos, char value)
	{
		m_buffer[pos] = value;
	}
	
	@Override
	public CharSequence subSequence(int from, int to)
	{
		int length = to - from;
		if (from + length > m_length)
			length = m_length - from;
		return new FixedCharSequence(m_buffer, m_offset + from, length);
	}
	
	public FixedCharSequence(char [] buffer, int offset, int length)
	{
		m_buffer = buffer;
		m_offset = offset;
		m_length = length;
		if (length > m_buffer.length - offset)
		{
			Log.e("FixedCharSequence", "Invalid length/offset: " + length + "/" + offset + ", real length " + m_buffer.length);
		}
	}
	
	public FixedCharSequence(CharSequence chars)
	{
		String str = chars.toString();
		m_offset = 0;
		m_length = chars.length();
		m_buffer = new char[m_length];
		
		str.getChars(0, m_length, m_buffer, 0);
	}
	
	public String toString(int start, int length)
	{
		if (start + length > m_length)
			length = m_length - start;
		if (length <= 0)
			return "";
		return new String(m_buffer, m_offset + start, length); 
	}

	public String toString()
	{
		return toString(0, m_length); 
	}
	
	public static FixedCharSequence toFixedCharSequence(String seq)
	{
		try
		{
			return new FixedCharSequence((char[])s_stringValue.get(seq), 0, seq.length());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	
		return new FixedCharSequence(seq);
	}
	
	public static FixedCharSequence toFixedCharSequence(ByteCharSequence seq)
	{
		CharBuffer buffer = seq.toCharBuffer(0, seq.length());
		return new FixedCharSequence(buffer.array(), 0, buffer.length());
	}
	
	public static FixedCharSequence toFixedCharSequence(SimpleByteSequence seq)
	{
		CharBuffer buffer = seq.toCharBuffer(0, seq.length());
		return new FixedCharSequence(buffer.array(), 0, buffer.length());
	}
	
	public static FixedCharSequence toFixedCharSequence(CharSequence seq)
	{
		if (seq instanceof FixedCharSequence)
			return (FixedCharSequence)seq;
		
		if (seq instanceof SimpleByteSequence)
			return toFixedCharSequence((SimpleByteSequence)seq);
		
		if (seq instanceof ByteCharSequence)
			return toFixedCharSequence((ByteCharSequence)seq);
		
		if (seq instanceof String)
			return toFixedCharSequence((String)seq);
		
		return new FixedCharSequence(seq);
	}
	
	public boolean equals(char value)
	{
		return m_length == 1 && m_buffer[m_offset] == value;
	}
	
	public boolean equals(CharSequence value)
	{
		int len = value.length(); 
		if (len != m_length)
			return false;		
		
		while(--len >= 0)
		{
			if (value.charAt(len) != m_buffer[len + m_offset])
				return false;
		}
		
		return true;
	}
	
	public boolean equals(FixedCharSequence value)
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
	
	public boolean equals(Object obj)
	{
		if (obj.getClass().isAssignableFrom(FixedCharSequence.class))
			return equals((FixedCharSequence)obj);
		if (obj.getClass().isAssignableFrom(CharSequence.class))
			return equals((CharSequence)obj);
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		//return (m_length) ^ (m_length > 1 ? m_buffer[m_offset + 1] : m_length > 0 ? m_buffer[m_offset] : 0);
		int hash = m_hashCode;
		if (hash == 0)
		{
			int multiplier = 1;
			int offset = m_offset;
			int count = m_length;
			char[] value = m_buffer;
			for (int i = offset + count - 1; i >= offset; i--)
			{
				hash += value[i] * multiplier;
				int shifted = multiplier << 5;
				multiplier = shifted - multiplier;
			}
			m_hashCode = hash;
		}
		return hash;
	}
}
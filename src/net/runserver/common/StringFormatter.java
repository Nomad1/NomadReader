package net.runserver.common;

import java.util.ArrayList;
import java.util.List;

public final class StringFormatter
{
	private final CharSequence [] m_parts;
	private final Integer [] m_indexes;
	private int m_averageLength;
	
	public StringFormatter(CharSequence pattern)
	{
		List<CharSequence> parts = new ArrayList<CharSequence>(10);
		List<Integer> indexes = new ArrayList<Integer>(9);
		
		/*try
		{*/		
			int wordStart = 0;
			int numStart = 0;
			for(int i=0;i<pattern.length();i++)
			{
				char ch = pattern.charAt(i);
				if (ch == '{')
				{
					if (i > 0 && pattern.charAt(i-1) == '\\')
						continue;
					
					if (numStart != 0)
						throw new StringIndexOutOfBoundsException(); // invalid {}
					
					if (wordStart >= i)
						parts.add("");
					else
						parts.add(pattern.subSequence(wordStart, i));
					
					numStart = i + 1;				
				} else
				if (ch == '}')
				{
					if (i > 0 && pattern.charAt(i-1) == '\\')
						continue;
					
					if (numStart == 0)
						throw new StringIndexOutOfBoundsException(); // invalid {}
					
					indexes.add(Integer.parseInt(pattern.subSequence(numStart, i).toString()));
					numStart = 0;
					wordStart = i + 1;
				}			
			}
			if (wordStart >= pattern.length())
				parts.add("");
			else
				parts.add(pattern.subSequence(wordStart, pattern.length()));
		/*}
		catch(Exception ex)
		{
			Log.e("Stringformatter", "Exception formatting string " + pattern);
			ex.printStackTrace();
		}*/
		
		m_parts = parts.toArray(new CharSequence[parts.size()]);
		m_indexes = indexes.toArray(new Integer[indexes.size()]);
		m_averageLength = pattern.length();
		
		/*Log.d("Stringformatter", "Pattern: " + pattern);
		Log.d("Stringformatter", "Parts: " + m_parts);
		Log.d("Stringformatter", "Indexes: " + m_indexes);*/
	}
	
	public String format(Object ... params)
	{
		StringBuilder result = new StringBuilder(m_averageLength);
		
		for(int i=0;i<m_parts.length - 1;i++)
		{
			result.append(m_parts[i]);
			result.append(params[m_indexes[i]]);
		}
		
		if (m_parts.length != params.length)
			result.append(m_parts[m_parts.length - 1]);
		
		if (result.length() > m_averageLength)
			m_averageLength = result.length(); 
		return result.toString();
	}
	
	public static String format(String pattern, Object ... params)
	{
		StringFormatter formatter = new StringFormatter(pattern);
		return formatter.format(params);
	}
}

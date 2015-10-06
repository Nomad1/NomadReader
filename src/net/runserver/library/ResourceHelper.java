package net.runserver.library;

import java.util.HashMap;

import net.runserver.common.StringFormatter;
import android.content.Context;

public class ResourceHelper
{
	private final HashMap<Integer, StringFormatter> m_resFormatStrings;
	private final Context m_context;

	public String resString(int id)
	{		
		return m_context.getResources().getString(id);
	}	
	
	public StringFormatter resStringFormatter(int id)
	{		
		if (m_resFormatStrings.containsKey(id))
			return m_resFormatStrings.get(id);
		
		StringFormatter result = new StringFormatter(resString(id));
		
		m_resFormatStrings.put(id, result);
		return result;
	}

	public String resStringFormat(int id, Object ... params)
	{		
		StringFormatter formatter = resStringFormatter(id);
		return formatter.format(params);
	}

	public Context getContext()
	{
		return m_context;
	}
	
	public ResourceHelper(Context context)
	{
		m_context = context;
		m_resFormatStrings = new HashMap<Integer, StringFormatter>();		
	}
}

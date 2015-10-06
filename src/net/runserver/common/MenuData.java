package net.runserver.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;

public class MenuData
{
	private final int m_id;
	private final int m_layoutId;
	private final List<MenuItem> m_items;
	
	public int getId()
	{
		return m_id;
	}

	public int getLayoutId()
	{
		return m_layoutId;
	}

	public List<MenuItem> getItems()
	{
		return m_items;
	}
	
	public MenuData(XmlResourceParser parser, Context context) throws XmlPullParserException, IOException
	{
		m_items = new ArrayList<MenuItem>();
		m_id = parser.getIdAttributeResourceValue(0);
		m_layoutId = parser.getAttributeResourceValue(null, "layout", 0);
		// Log.d("FileBrowser", "Got menu with id " + m_id);
		int eventType = parser.getEventType();
		while (eventType != XmlResourceParser.END_DOCUMENT)
		{
			switch (eventType)
			{
				case XmlResourceParser.START_TAG:
					if (parser.getName().equals("item"))
						m_items.add(new MenuItem(parser, context));
					break;
				case XmlResourceParser.END_TAG:
					if (parser.getName().equals("menu"))
						return;
					break;
			}
			eventType = parser.next();
		}
	}

	public MenuData(int id, int layoutId, List<MenuItem> items)
	{
		m_id = id;
		m_layoutId = layoutId;
		m_items = items;
	}
}

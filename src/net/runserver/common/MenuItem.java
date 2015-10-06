package net.runserver.common;

import android.content.Context;
import android.content.res.XmlResourceParser;

public class MenuItem
{
	private final int m_id;
	private final int m_targetId;
	private final int m_longTargetId;
	private CharSequence m_title;
	private CharSequence m_value;
	private Object m_tag;
	
	
	private boolean m_visible;
	private boolean m_enabled;
	private boolean m_checked;
	
	public int getId()
	{
		return m_id;
	}

	public int getTargetId()
	{
		return m_targetId;
	}

	public int getLongTargetId()
	{
		return m_longTargetId;
	}
	
	public CharSequence getTitle()
	{
		return m_title;
	}
	
	public void setTitle(CharSequence value)
	{
		m_title = value;
	}
	
	public CharSequence getValue()
	{
		return m_value;
	}
	
	public void setValue(CharSequence value)
	{
		m_value = value;
	}
	
	public void setTag(Object tag)
	{
		m_tag = tag;
	}
	
	public Object getTag()
	{
		return m_tag;
	}

	public boolean isVisible()
	{
		return m_visible;
	}
	
	public void setVisible(boolean value)
	{
		m_visible = value;
	}

	public boolean isEnabled()
	{
		return m_enabled;
	}
	
	public void setEnabled(boolean value)
	{
		m_enabled = value;
	}
	
	public boolean isChecked()
	{
		return m_checked;
	}
	
	public void setChecked(boolean value)
	{
		m_checked = value;
	}
	
	public MenuItem(XmlResourceParser parser, Context context)
	{
		m_visible = true;
		m_enabled = true;
		m_id = parser.getIdAttributeResourceValue(0);
		m_targetId = parser.getAttributeResourceValue(null, "target", 0);
		m_longTargetId = parser.getAttributeResourceValue(null, "long_target", 0);
		String title = parser.getAttributeValue(null, "title");
		if (title.startsWith("@"))
			m_title = context.getResources().getString(parser.getAttributeResourceValue(null, "title", 0));
		else
			m_title = title;
		m_value = "";
		// Log.d("FileBrowser", "Got menu item id " + m_id + ", target " +
		// m_targetId + ", title " + m_title);
	}

	public MenuItem(int id, int targetId, int longTargetId, String title)
	{
		m_visible = true;
		m_enabled = true;
		m_id = id;
		m_targetId = targetId;
		m_longTargetId = longTargetId; 
		m_value = "";
		m_title = title;
	}
}
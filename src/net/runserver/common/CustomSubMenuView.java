/* 
 * Copyright 2010-2011 RunServer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.runserver.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public abstract class CustomSubMenuView extends BaseCustomMenu
{
	private final CustomMenuView m_parent;
	private final int m_backButtonId;
	private final ViewGroup m_listContainer;
	private final MenuData m_data;

	public CustomSubMenuView(Context context, ViewGroup container, int layoutResourceId, int listId, int backButtonId, 
			CustomMenuView parent, MenuData data)
	{
		super(context, container);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(layoutResourceId, this);
		
		m_backButtonId = backButtonId;
 
		if (m_backButtonId != 0)
		{
			ImageButton back = (ImageButton) findViewById(backButtonId);
			if (back != null)
				back.setOnClickListener(this);
		}

		m_data = data;
		
		m_listContainer = (ViewGroup) findViewById(listId);
		
		//drawMenu(data, m_listContainer);

		m_parent = parent;
	}
	
	// implements OnClickListener
	public void onClick(View view)
	{
		if (m_backButtonId != 0 && view.getId() == m_backButtonId)
		{
			m_parent.onMenuBackClick(this, m_data.getId());
			return;
		}

		m_parent.onMenuClick((MenuItem) view.getTag(), this, false);
	}
	
	public boolean onLongClick(View view)
	{		
		MenuItem item = (MenuItem) view.getTag();

		// Log.d(LogName, "Menu Item " + item.getId());

		m_parent.onMenuClick(item, null, true);
		return true;
	}



	public void validate()
	{
		if (m_listContainer.getChildCount() == 0)
		{
			//Log.d("TextReader", "Got onDraw for subMenu " + m_data.getId());
			drawMenu(m_data, m_listContainer);
		}
	}	

}

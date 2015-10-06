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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public abstract class CustomMenuView extends BaseCustomMenu
{
	private static String LogName = "FileBrowser";

	private final Hashtable<Integer, View> m_subMenus;
	private final Stack<View> m_menuStack;

	protected abstract int getAnimSlideInLeft();
	protected abstract int getAnimSlideOutLeft();
	protected abstract int getAnimSlideInRight();
	protected abstract int getAnimSlideOutRight();
	
	private OnMenuListener m_menuListener;
	
	
	public CustomMenuView(Context context, ViewGroup container, int xmlMenuResourceId, int mainMenuLayout, int mainMenuList)
	{
		super(context, container);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(mainMenuLayout, this);

		m_subMenus = new Hashtable<Integer, View>();
		m_menuStack = new Stack<View>();
		m_menuStack.push(this);

		ViewGroup menuView = (ViewGroup) findViewById(mainMenuList);

		try
		{
			XmlResourceParser parser = getResources().getXml(xmlMenuResourceId);

			boolean firstMenu = true;

			int eventType = parser.getEventType();
			while (eventType != XmlResourceParser.END_DOCUMENT)
			{
				switch (eventType)
				{
					case XmlResourceParser.START_TAG:
						if (parser.getName().equals("menu"))
						{
							if (parser.getAttributeValue(null, "id") != null)
							{
								MenuData menu = new MenuData(parser, context);

								if (firstMenu)
								{
									addMenu(menu);
									
									drawMenu(menu, menuView);
									container.addView(this);
									m_subMenus.put(menu.getId(), this);
									
									firstMenu = false;
								} else
								if (menu.getLayoutId() == 0)
								{
									addSubMenu(menu.getId(), menu);
								} else
								{
									View subMenu = inflater.inflate(menu.getLayoutId(), null);
									container.addView(subMenu);
									subMenu.setVisibility(GONE);
									
									m_subMenus.put(menu.getId(), subMenu);
								}
							}
						}
						break;
				}
				eventType = parser.next();
			}
		}
		catch (XmlPullParserException ex)
		{
			Log.e(LogName, "XmlPullParserException: " + ex);
		}
		catch (IOException ex)
		{
			Log.e(LogName, "IOException: " + ex);
		}
	}
	
	protected abstract View createSubMenu(MenuData menu, ViewGroup container);
	
	public void addSubMenu(int id, MenuData menu)
	{
		addMenu(menu);
		View subMenu = createSubMenu(menu, m_container);
		m_subMenus.put(id, subMenu);
		
		m_container.addView(subMenu);
		subMenu.setVisibility(GONE);
	}

	public void removeSubMenu(int id)
	{
		View menu = m_subMenus.get(id);
		if (menu != null)
		{
			m_subMenus.remove(id);
			m_container.removeView(menu);
		}
	}
	
	public View getSubMenu(int id)
	{
		return m_subMenus.get(id);
	}
	
	/*************** Public methods ****************/	
	
	public void setMenuListener(OnMenuListener listener)
	{
		m_menuListener = listener;
	}
	
	public void setBackButtonListener(int id, OnClickListener listener)
	{
		View view = findViewById(id);
		if (view != null)
			view.setOnClickListener(listener);
	}

	public void setNavigatorVisibility(int visibility)
	{
		//findViewById(R.id.navigator).setVisibility(visibility);
	}
	
	public void setUpButtonListener(OnClickListener listener)
	{
		//findViewById(R.id.navigator_up).setOnClickListener(listener);
	}

	public void setDownButtonListener(OnClickListener listener)
	{
		//findViewById(R.id.navigator_down).setOnClickListener(listener);
	}

	public void setUpButtonLongListener(OnLongClickListener listener)
	{
		//findViewById(R.id.navigator_up).setOnLongClickListener(listener);
	}

	public void setDownButtonLongListener(OnLongClickListener listener)
	{
		//findViewById(R.id.navigator_down).setOnLongClickListener(listener);
	}

	public void setSelectButtonListener(OnClickListener listener)
	{
		//findViewById(R.id.selectButton).setOnClickListener(listener);
	}

	/*************** Events ****************/

	// implements OnClickListener
	public void onClick(View view)
	{		
		MenuItem item = (MenuItem) view.getTag();

		// Log.d(LogName, "Menu Item " + item.getId());

		onMenuClick(item, null, false);
	}	
	
	// implements OnLongClickListener
	public boolean onLongClick(View view)
	{		
		MenuItem item = (MenuItem) view.getTag();

		// Log.d(LogName, "Menu Item " + item.getId());

		onMenuClick(item, null, true);
		return true;
	}	
	
	public void onMenuClick(MenuItem item, BaseCustomMenu view, boolean longClick)
	{		
		if (m_menuListener != null && m_menuListener.onMenu(item))
		{
			if (view != null)
				hideTouchscreenMenu(/*view*/);
				//view.hide();
			return;
		}
		
		if (m_subMenus != null)
		{			
			Integer nextId = 0;
			
			if (longClick)
				nextId = item.getLongTargetId();
			
			if (nextId == 0)
				nextId = item.getTargetId();
			
			if (nextId != 0 && m_subMenus.containsKey(nextId))
			{
				View subMenu = m_subMenus.get(nextId);
				showTouchscreenMenu(subMenu);
				//subMenu.show();
			}
		}
	}
	
	
	public void onMenuBackClick(BaseCustomMenu view, int menuId)
	{
		if (m_menuListener != null)
		{
			if(m_menuListener.onMenuBack(menuId))
				hideTouchscreenMenu(/*view*/);
		} else
			hideTouchscreenMenu(/*view*/);
	}
	
	public void showTouchscreenMenu(View menu)
	{		
		View oldMenu = m_menuStack.peek();
		if (oldMenu == menu)
			return;
		
		m_menuStack.push(menu);
		
		if (menu instanceof CustomSubMenuView)
			((CustomSubMenuView)menu).validate();
		
		if (!BaseActivity.isNookTouch)
		{
			Animation showAnimation  = AnimationUtils.loadAnimation(getContext(), getAnimSlideInRight());
			menu.startAnimation(showAnimation);
		}
		menu.setVisibility(VISIBLE);
		
		if (!BaseActivity.isNookTouch)
		{
			Animation hideAnimation  = AnimationUtils.loadAnimation(getContext(), getAnimSlideOutLeft());
			oldMenu.startAnimation(hideAnimation);
		}
		oldMenu.setVisibility(GONE);		
	}
	
	public void hideTouchscreenMenu(/*BaseTouchscreenMenu menu*/)
	{
		if (m_menuStack.size() < 2)
			return;
		
		View menu = m_menuStack.pop();
		
		View newMenu = m_menuStack.peek();
		
		if (!BaseActivity.isNookTouch)
		{
			Animation showAnimation  = AnimationUtils.loadAnimation(getContext(), getAnimSlideInLeft());
			newMenu.startAnimation(showAnimation);
		}
		newMenu.setVisibility(VISIBLE);
		
		if (!BaseActivity.isNookTouch)
		{
			Animation hideAnimation  = AnimationUtils.loadAnimation(getContext(), getAnimSlideOutRight());
			menu.startAnimation(hideAnimation);
		}
		menu.setVisibility(GONE);		
	}
	
	public void goToRoot()
	{
		while(m_menuStack.size() > 1)
			hideTouchscreenMenu();
	}
	
	public boolean isRoot()
	{
		return m_menuStack.size() <= 1;
	}
	
	public void goBack()
	{
		if(m_menuStack.size() > 1)
			hideTouchscreenMenu();
	}
	
	public void goToMenu(int id)
	{
		if (m_subMenus.containsKey(id))
			showTouchscreenMenu(m_subMenus.get(id));
	}
	
	public void setRootMenu(int id)
	{
		View menu = m_subMenus.get(id);
		
		if (menu == null)
			return;
		
		while(m_menuStack.size() > 0)
			m_menuStack.pop().setVisibility(GONE);

		m_menuStack.push(menu);
		
		if (menu instanceof CustomSubMenuView)
			((CustomSubMenuView)menu).validate();
		
		menu.setVisibility(VISIBLE);
	}
}

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
package net.runserver.fileBrowser;

import net.runserver.common.BaseActivity;
import net.runserver.common.CustomMenuView;
import net.runserver.common.CustomSubMenuView;
import net.runserver.common.MenuData;
import net.runserver.textReader.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FileBrowserMenu extends CustomMenuView
{	
	protected int getAnimSlideInLeft()
	{
		return R.anim.slide_in_left;
	}
	protected int getAnimSlideOutLeft()
	{
		return R.anim.slide_out_left;
	}
	protected int getAnimSlideInRight()
	{
		return R.anim.slide_in_right;
	}
	protected int getAnimSlideOutRight()
	{
		return R.anim.slide_out_right;
	}
	
	public FileBrowserMenu(Context context, ViewGroup container, int xmlMenuResourceId)
	{
		super(context, container, xmlMenuResourceId,
				R.layout.custom_menu,
				R.id.subMenu
		);
	}
	
	@Override
	protected View createSubMenu(MenuData menu, ViewGroup container)
	{
		return new FileBrowserSubMenu(getContext(), container, this, menu);
	}
	
	@Override
	protected View createMenuView()
	{
		return createMenuItem(getContext());
	}
	
	public class FileBrowserSubMenu extends CustomSubMenuView
	{
		public FileBrowserSubMenu(Context context, ViewGroup container, CustomMenuView parent, MenuData data)
		{
			super(context, container, R.layout.custom_menu, R.id.subMenu, R.id.backButton, parent, data);
		}
		
		@Override
		protected View createMenuView()
		{
			return createMenuItem(getContext());
		}
	}
	
	private static View createMenuItem(Context context)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		View result = inflater.inflate(R.layout.menu_list_item, null);
		
		if (BaseActivity.isNookTouch)
		{
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, 60);
			params.bottomMargin = 1;
			result.setLayoutParams(params);
			result.setBackgroundColor(0xffffffff);
		}
		return result;
	}
}

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
package net.runserver.textReader;

import net.runserver.common.BaseActivity;
import net.runserver.common.CustomMenuView;
import net.runserver.common.CustomSubMenuView;
import net.runserver.common.MenuData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextReaderMenu extends CustomMenuView
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
	
	public TextReaderMenu(Context context, ViewGroup container, int xmlMenuResourceId)
	{
		super(context, container, xmlMenuResourceId, R.layout.custom_menu, R.id.subMenu);
	}	
	
	@Override
	protected View createSubMenu(MenuData menu, ViewGroup container)
	{
		return new TextReaderSubMenu(getContext(), container, this, menu);
	}
	
	@Override
	protected View createMenuView()
	{
		return createMenuItem(getContext());
	}
	
	public class TextReaderSubMenu extends CustomSubMenuView
	{
		public TextReaderSubMenu(Context context, ViewGroup container, CustomMenuView parent, MenuData data)
		{
			super(context, container, R.layout.custom_menu, R.id.subMenu, R.id.backButton, parent, data);
		}
		
		@Override
		protected View createMenuView()
		{
			return createMenuItem(getContext());
		}
	}
	
	private static LayoutParams [] s_layoutParams = null;
	private static ColorStateList s_textColors = null;
	private static ColorStateList s_valueColors = null;

	private static View createMenuItem(Context context)
	{
		if (!BaseActivity.isNook)
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
		
		if (s_layoutParams == null)
		{
			s_textColors = context.getResources().getColorStateList(R.drawable.menu_item_text);
			s_valueColors = context.getResources().getColorStateList(R.drawable.menu_value_text);
			
			s_layoutParams = new LayoutParams[3];
			
			LayoutParams parentParams = new LayoutParams(LayoutParams.FILL_PARENT, 36);
			parentParams.gravity = Gravity.CENTER_VERTICAL;
			if (BaseActivity.isNookTouch)
				parentParams.bottomMargin = 1;
			s_layoutParams[0] = parentParams;
			
			LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, 35);
			textParams.setMargins(8, 0, 8, 0);
			s_layoutParams[1] = textParams;
			
			LayoutParams valueParams = new LayoutParams(LayoutParams.WRAP_CONTENT, 35);
			valueParams.setMargins(35, 0, 0, 0);
			valueParams.weight = 1.0f;
			s_layoutParams[2] = valueParams;
		}
		
		LinearLayout parent = new LinearLayout(context);
		parent.setLayoutParams(s_layoutParams[0]);
		parent.setGravity(Gravity.CENTER_VERTICAL);
		parent.setId(R.id.list_item);
		
		if (BaseActivity.isNookTouch)
		{
			parent.setPadding(6,6,6,6);
			parent.setBackgroundColor(0xffffffff);
		}
		else
			parent.setBackgroundResource(R.drawable.menu_button);
		
		
		TextView text = new TextView(context);
		text.setLayoutParams(s_layoutParams[1]);
		text.setGravity(Gravity.CENTER_VERTICAL);
		text.setTextColor(s_textColors);
		text.setId(R.id.item_text);
		text.setSingleLine();
		text.setEllipsize(TruncateAt.END);
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		//text.setTextAppearance(context, android.R.style.TextAppearance_Medium);
		parent.addView(text);
				
		TextView value = new TextView(context);
		value.setLayoutParams(s_layoutParams[2]);
		value.setGravity(Gravity.CENTER_VERTICAL);
		value.setTextColor(s_valueColors);
		value.setId(R.id.item_value);
		value.setSingleLine();
		value.setEllipsize(TruncateAt.END);
		
		value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		//text.setTextAppearance(context, android.R.style.TextAppearance_Small);
		
		parent.addView(value);
		
		ImageView check = new ImageView(context);
		check.setId(R.id.item_check_image);
		check.setVisibility(View.INVISIBLE);
		check.setLayoutParams(s_layoutParams[1]);
		check.setImageResource(R.drawable.check_button);
		parent.addView(check);
		
		ImageView more = new ImageView(context);
		more.setId(R.id.item_more_image);
		more.setVisibility(View.GONE);
		more.setImageResource(R.drawable.more_button);
		more.setLayoutParams(s_layoutParams[1]);
		parent.addView(more);
		
		return parent;
	}
}

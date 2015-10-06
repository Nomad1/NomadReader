package net.runserver.common;

import java.util.HashMap;

import net.runserver.textReader.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class BaseCustomMenu extends LinearLayout implements OnClickListener, OnLongClickListener
{
	protected final ViewGroup m_container;
	private HashMap<Integer, MenuItem> m_menuTree;
	
	protected BaseCustomMenu(Context context, ViewGroup container)
	{
		super(context);
		m_container = container;
		m_menuTree = new HashMap<Integer, MenuItem>();
	}
	
	protected abstract View createMenuView();
	
	protected void addMenu(MenuData data)
	{
		for(MenuItem item: data.getItems())
			m_menuTree.put(item.getId(), item);
	}
	
	public void drawMenu(MenuData data, ViewGroup view)
	{
		view.removeAllViews();
		for(MenuItem item: data.getItems())
		{
			View frame = createMenuView();

			TextView text = (TextView) frame.findViewById(R.id.item_text);
			text.setText(item.getTitle());
			
			if (item.getValue().length() > 0)
			{
				TextView itemValue = (TextView) frame.findViewById(R.id.item_value);
				itemValue.setText(item.getValue());
				itemValue.setVisibility(VISIBLE);
			}

			frame.setOnClickListener(this);
			
			if (item.getLongTargetId() != 0)
				frame.setOnLongClickListener(this);
			
			frame.setTag(item);
			frame.setId(item.getId());

			ImageView moreImage = null;
			if (item.getTargetId() != 0)
			{
				moreImage = (ImageView) frame.findViewById(R.id.item_more_image);
				moreImage.setVisibility(VISIBLE);
			}

			view.addView(frame);
			
			if (!item.isVisible())
				frame.setVisibility(GONE);
			
			if (!item.isEnabled())
			{
				frame.setEnabled(false);
				text.setEnabled(false);
				if (moreImage != null)
					moreImage.setVisibility(GONE);
			}
			
			if (item.isChecked())
			{
				ImageView rightImage = (ImageView) frame.findViewById(R.id.item_check_image);
				rightImage.setVisibility(VISIBLE);
			}
			
		}
		invalidate();
	}

	public void setMenuVisibility(int id, int visible)
	{
		View menu = m_container.findViewById(id);

		if (menu != null)
			menu.setVisibility(visible);
		else
		{
			MenuItem item = m_menuTree.get(id);
			if (item != null)
				item.setVisible(visible == VISIBLE);
		}
	}
	
	public void setMenuEnabled(int id, boolean enabled)
	{
		View menu = m_container.findViewById(id);

		if (menu != null)
		{
			menu.setEnabled(enabled);

			if (((MenuItem) menu.getTag()).getTargetId() != 0)
			{
				ImageView rightImage = (ImageView) menu.findViewById(R.id.item_more_image);
				if (enabled)
					rightImage.setVisibility(VISIBLE);
				else
					rightImage.setVisibility(GONE);
			}

			TextView itemName = (TextView) menu.findViewById(R.id.item_text);
			itemName.setEnabled(enabled);
		} else
		{
			MenuItem item = m_menuTree.get(id);
			if (item != null)
				item.setEnabled(enabled);
		}
	}
	
	public void setMenuChecked(int id, boolean checked)
	{
		View menu = m_container.findViewById(id);
		if (menu != null)
		{
			ImageView rightImage = (ImageView) menu.findViewById(R.id.item_check_image);
			if (checked)
				rightImage.setVisibility(VISIBLE);
			else
				rightImage.setVisibility(INVISIBLE);
		} else
		{
			MenuItem item = m_menuTree.get(id);
			if (item != null)
				item.setChecked(checked);
		}
	}

	public void setMenuValue(int id, CharSequence value)
	{
		View menu = m_container.findViewById(id);
		if (menu != null)
		{
			TextView itemValue = (TextView) menu.findViewById(R.id.item_value);
			itemValue.setText(value);
			itemValue.setVisibility(VISIBLE);
		} else
		{
			MenuItem item = m_menuTree.get(id);
			if (item != null)
				item.setValue(value);
		}
	}

	public void setMenuText(int id, String value)
	{
		View menu = m_container.findViewById(id);
		if (menu != null)
		{
			TextView itemText = (TextView) menu.findViewById(R.id.item_text);
			itemText.setText(value);
		} else
		{
			MenuItem item = m_menuTree.get(id);
			if (item != null)
				item.setTitle(value);
		}
	}

	public void setMenuValue(int id, int valueId)
	{
		setMenuValue(id, getContext().getResources().getString(valueId));
	}
	
	public void setMenuValueEx(int id, int valueId)
	{
		MenuItem targetMenu = m_menuTree.get(valueId);
		if (targetMenu != null)
			setMenuValue(id, targetMenu.getTitle());
	}

	/*
	 * public void hide() { Animation animation =
	 * AnimationUtils.loadAnimation(getContext(), R.anim.slide_right_out);
	 * startAnimation(animation); setVisibility(GONE); }
	 * 
	 * public void show() { Animation animation =
	 * AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_in);
	 * startAnimation(animation); setVisibility(VISIBLE); }
	 */
	/*************** inner classes ***************/

	public interface OnMenuListener
	{
		public boolean onMenu(MenuItem item);

		public boolean onMenuBack(int menuId);
	}
}
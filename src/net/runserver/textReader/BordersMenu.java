package net.runserver.textReader;

import net.runserver.common.BaseActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

public class BordersMenu implements OnClickListener
{
	private final LinearLayout m_borderLeftGroup;
	private final LinearLayout m_borderRightGroup;
	
	private final EditText m_borderLeft;
	private final EditText m_borderTop;
	private final EditText m_borderRight;
	private final EditText m_borderBottom;
	
	private OnApplyBordersListener m_listener;

	public int getLeft()
	{
		return Integer.parseInt(	m_borderLeft.getText().toString());
	}

	public int getRight()
	{
		return Integer.parseInt(	m_borderRight.getText().toString());
	}

	public int getTop()
	{
		return Integer.parseInt(	m_borderTop.getText().toString());
	}

	public int getBottom()
	{
		return Integer.parseInt(	m_borderBottom.getText().toString());
	}
	
	public void setListener(OnApplyBordersListener value)
	{
		m_listener = value;
	}
	
	public void setBorders(int top, int left, int bottom, int right)
	{
		if (top < 0)
			top = 0;
		if (left < 0)
			left = 0;
		if (bottom < 0)
			bottom = 0;
		if (right < 0)
			right = 0;
		m_borderLeft.setText(Integer.toString(left));
		m_borderTop.setText(Integer.toString(top));
		m_borderBottom.setText(Integer.toString(bottom));
		m_borderRight.setText(Integer.toString(right));
	}

	public BordersMenu(View bordersMenu)
	{	
		m_borderLeft = (EditText) bordersMenu.findViewById(R.id.border_left);
		m_borderTop = (EditText) bordersMenu.findViewById(R.id.border_top);
		m_borderRight = (EditText) bordersMenu.findViewById(R.id.border_right);
		m_borderBottom = (EditText) bordersMenu.findViewById(R.id.border_bottom);
		m_borderLeftGroup = (LinearLayout) bordersMenu.findViewById(R.id.border_left_group);
		m_borderRightGroup = (LinearLayout) bordersMenu.findViewById(R.id.border_right_group);
		
		if (BaseActivity.isNook)
			bordersMenu.findViewById(R.id.backButton).setOnClickListener(this);
		
		bordersMenu.findViewById(R.id.left_increment).setOnClickListener(this);
		bordersMenu.findViewById(R.id.left_decrement).setOnClickListener(this);
		bordersMenu.findViewById(R.id.top_increment).setOnClickListener(this);
		bordersMenu.findViewById(R.id.top_decrement).setOnClickListener(this);
		bordersMenu.findViewById(R.id.right_increment).setOnClickListener(this);
		bordersMenu.findViewById(R.id.right_decrement).setOnClickListener(this);
		bordersMenu.findViewById(R.id.bottom_decrement).setOnClickListener(this);
		bordersMenu.findViewById(R.id.bottom_increment).setOnClickListener(this);
	}
	
	public void setOrientation(boolean portrait)
	{
		if (BaseActivity.DisplayMetrics.widthPixels <= 320)
			portrait = true;
		else
			if (BaseActivity.isNookTouch || BaseActivity.isNook)
				portrait = false;
		//Log.d("TextReader", "Setting to portrait mode: " + portrait + ", width " + BaseActivity.DisplayMetrics.widthPixels + ", height " + BaseActivity.DisplayMetrics.heightPixels);
		
		m_borderLeftGroup.setOrientation(portrait ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
		m_borderRightGroup.setOrientation(portrait ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
	}

	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.backButton:
				if (m_listener != null)
						m_listener.onApplyBorders();
				break;
			case R.id.left_decrement:
				setBorders(getTop(), getLeft() - 1, getBottom(), getRight());
				break;
			case R.id.left_increment:
				setBorders(getTop(), getLeft() + 1, getBottom(), getRight());
				break;
			case R.id.top_decrement:
				setBorders(getTop() - 1, getLeft(), getBottom(), getRight());
				break;
			case R.id.top_increment:
				setBorders(getTop() + 1, getLeft(), getBottom(), getRight());
				break;
			case R.id.right_decrement:
				setBorders(getTop(), getLeft(), getBottom(), getRight() - 1);
				break;
			case R.id.right_increment:
				setBorders(getTop(), getLeft(), getBottom(), getRight() + 1);
				break;
			case R.id.bottom_decrement:
				setBorders(getTop(), getLeft(), getBottom() - 1, getRight());
				break;
			case R.id.bottom_increment:
				setBorders(getTop(), getLeft(), getBottom() + 1, getRight());
				break;
		}
	}

	interface OnApplyBordersListener
	{
		public void onApplyBorders();
	}
}

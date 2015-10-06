package net.runserver.textReader;

import java.util.List;

import net.runserver.common.BaseActivity;
import net.runserver.common.CustomMenuView;
import net.runserver.common.Pair;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class GotoMenu implements OnSeekBarChangeListener, OnClickListener
{
	private final ViewGroup m_gotoDialog;
	private final View m_pageTooltip;
	private final SeekBar m_pageSeeker;
	private OnPageChangeListener m_listener;
	private List<Pair<String, Float>> m_chapters;
	private LayerDrawable m_pageDrawable;
	private boolean m_inited = false;
	private final CustomMenuView m_menu;

	public boolean isVisible()
	{
		return  m_gotoDialog.getVisibility() == View.VISIBLE;
	}
	
	public ViewGroup getView()
	{
		return m_gotoDialog;
	}
	
	public void setPageChangeListener(OnPageChangeListener listener)
	{
		m_listener = listener;		
	}
	
	public GotoMenu(ViewGroup gotoDialog, CustomMenuView menu)
	{
		m_gotoDialog = gotoDialog;
		m_menu = menu;
		
		m_pageTooltip = gotoDialog.findViewById(R.id.page_tip_container);
				
		m_pageSeeker = ((SeekBar)gotoDialog.findViewById(R.id.slider));
		m_pageSeeker.setOnSeekBarChangeListener(this);
		if (!BaseActivity.isNook)
			m_pageSeeker.setThumb(gotoDialog.getContext().getResources().getDrawable(R.drawable.scrubber_control_selector_holo));
		
		m_pageDrawable = ((LayerDrawable)m_pageSeeker.getProgressDrawable());
		
		View back = gotoDialog.findViewById(R.id.backButton);
		if (back != null)
			back.setOnClickListener(this);
	}
	
	void initChapters(List<Pair<String, Float>> chapters)
	{
		m_chapters = chapters;
		m_inited = false;
		
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{

	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{

	}
	
	@Override
	public void onProgressChanged(SeekBar v, int progress, boolean fromUser)
	{
		TextView pageTooltipText = (TextView)m_gotoDialog.findViewById(R.id.page_tip);
		TextView chapterTooltipText = (TextView)m_gotoDialog.findViewById(R.id.chapter_tip);
		pageTooltipText.setText((progress+1) + "/" + (m_pageSeeker.getMax()+1));
		
		float percent = (float)progress/(float)m_pageSeeker.getMax();
		
		if (m_chapters == null || m_chapters.size() == 0)
		{
			chapterTooltipText.setVisibility(View.GONE); 	
		} else
		{
			String chapterName = "";
			for(int i=0;i<m_chapters.size();i++)
			{
				if (percent * 100.0f < m_chapters.get(i).second)
				{
					chapterName = m_chapters.get(i == 0 ? 0 : i - 1).first.trim();
					break;
				}
			}

			chapterTooltipText.setText(chapterName);
		}
		
		
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams)m_pageTooltip.getLayoutParams();		
		ViewGroup parent = (ViewGroup)m_pageTooltip.getParent();
		params.x = (int)((parent.getWidth() - m_pageTooltip.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight()) * percent);
		m_pageTooltip.setLayoutParams(params);
		
		//m_inited = false;
		//updatePages();
		
		if (m_listener != null)
			m_listener.onPageChanged(progress, percent);
	}
	
	public void show(int current, int maxPage, boolean show)
	{
		if (show)
			m_gotoDialog.setVisibility(View.VISIBLE);
		
		m_pageSeeker.setMax(maxPage - 1);
		m_pageSeeker.setProgress(current);
		
		updatePages();
	}
	
	private void updatePages()
	{
		if (!m_inited)
		{
			m_inited = true;
			
			DisplayMetrics dm = BaseActivity.DisplayMetrics;

			int width = dm.widthPixels;
			int height = 14;
			
			BitmapDrawable drawable;
			
			try
			{
				drawable = (BitmapDrawable)m_pageDrawable.getDrawable(android.R.id.background);
			}
			catch(Exception ex)
			{
				drawable = null;
			}
			
			Bitmap bitmap = drawable == null ? Bitmap.createBitmap(width, height, Config.ARGB_8888) : drawable.getBitmap();
			
			if (drawable == null)
				drawable = new BitmapDrawable(bitmap);
			
			Canvas canvas = new Canvas(bitmap);
			
			//float percent = 100.0f*(float)m_pageSeeker.getProgress()/(float)m_pageSeeker.getMax();
		
			int barWidth = width - (int)(dm.density * 20);
			int barLeft = (int)(dm.density * 10);
			int shift = -3;
			
			Paint lightGrayPaint = new Paint();
			lightGrayPaint.setColor(0xffffff);
			lightGrayPaint.setAlpha(0xff);
			
			Paint darkGrayPaint = new Paint();
			darkGrayPaint.setColor(0x222222);
			darkGrayPaint.setAlpha(0xff);
			
			Paint blackPaint = new Paint();
			blackPaint.setColor(0x000000);
			blackPaint.setAlpha(0xff);

			Paint barPaint = new Paint();
			//barPaint.setColor(0x808080);
			barPaint.setAlpha(0xff);
			Shader shader = new LinearGradient(0, 0, width, 0, 0xff666666, 0xffaaaaaa, TileMode.REPEAT); 
			barPaint.setShader(shader); 
		
			canvas.drawRect(new Rect(barLeft - 2, shift + 3, barLeft + barWidth + 2, shift + 17), darkGrayPaint);
			canvas.drawRect(new Rect(barLeft, shift + 4, barLeft + barWidth + 2, shift + 17), lightGrayPaint);
			//canvas.drawRect(new Rect(barLeft + barWidth, shift + 4, barLeft + barWidth + 1, shift + 16), lightGrayPaint);
			canvas.drawRect(new Rect(barLeft, shift + 4, barLeft + barWidth, shift + 16), barPaint);
			//canvas.drawRect(new Rect(barLeft - 1, shift + 5, barLeft, shift + 15), lightGrayPaint);
			//int progressX = barLeft + (int) (barWidth * percent / 100f);
			//canvas.drawRect(new Rect(barLeft, shift + 4, progressX, shift + 16), darkGrayPaint);		

			
			int lastx = 0;
			for(int i=0;i<m_chapters.size();i++)
			{
				float chapterPercent = m_chapters.get(i).second;
				int x = barLeft + (int) (barWidth * chapterPercent / 100f);
				
				if (x == lastx)
					continue;
				
				lastx = x;
				{
					//if (chapterPercent >= percent)
						canvas.drawLine(x, shift + 5, x, shift + 15, blackPaint);
					//else
					//	canvas.drawLine(x, shift + 5, x, shift + 15, grayPaint);
				}
			}
			
			m_pageDrawable.setDrawableByLayerId(android.R.id.background, drawable);
			m_pageSeeker.invalidate();
		}
	}

	public void hide()
	{		
		m_gotoDialog.setVisibility(View.GONE);
	}
	

	interface OnPageChangeListener
	{
		public void onPageChanged(int page, float percent);
	}


	@Override
	public void onClick(View arg0)
	{
		if (m_menu != null)
			m_menu.hideTouchscreenMenu();
		else
			hide();
	}
}

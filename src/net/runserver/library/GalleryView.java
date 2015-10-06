package net.runserver.library;

import java.util.ArrayList;
import java.util.List;

import net.runserver.common.BaseActivity;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class GalleryView extends View
{	
	private final List<GalleryImage> m_images;
	private final Paint m_bitmapPaint;
	private final Paint m_shadowPaint;
	private final float m_padding;
	private int m_width;
	private float m_lastX;
	
	private boolean m_suspended;
	
	private int m_animations = 0;
	
	public float getLastX()
	{
		return m_lastX;
	}
	
	public boolean isAnimating()	
	{
		return m_animations > 0;
	}
	
	public int getContentWidth()
	{
		return m_width;
	}

	public GalleryView(Context context, float padding)
	{
		super(context);
		m_images = new ArrayList<GalleryImage>();
		m_bitmapPaint = new Paint();

		if (!BaseActivity.isEpad)
		{	
			m_shadowPaint = new Paint();
			m_shadowPaint.setColor(0xff666666);
			m_shadowPaint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL));
		} else
			m_shadowPaint = null;
		
		m_suspended = false;
		m_padding = padding;
		
		setFocusable(true);
		m_width = 0;
	}	
	
	public void addView(GalleryImage view)
	{
		m_images.add(view);
		view.setX(m_width);
		//view.setY(0);
		m_width += view.getWidth();
		requestLayout();
		invalidate();
	}

	public void removeAllViews()
	{
		for(int i=0;i<m_images.size();i++)
			m_images.get(i).clean();
		
		m_images.clear();
		m_width = 0;
		requestLayout();
		invalidate();
	}

	public int getChildCount()
	{
		return m_images.size();
	}

	public GalleryImage getChildAt(int i)
	{
		return m_images.get(i);
	}

	protected void doDraw(Canvas canvas)
	{
		if (m_suspended)
			return;
		
		canvas.save();
		canvas.translate(/*getScrollX() + */getPaddingLeft(), /*getScrollY() + */getPaddingTop());
		
		Rect clip = canvas.getClipBounds();
		
		boolean found = false;
		
		for (int i = 0; i < m_images.size(); i++)
		{
			GalleryImage image = m_images.get(i);
			
			if (clip.right > image.getX() && clip.left <= image.getX() + image.getWidth() && clip.bottom > image.getY() && clip.top <= image.getY() + image.getHeight())
			{
				found = true;
				image.draw(canvas, m_bitmapPaint, m_shadowPaint);
			} else
			{
				if (found)
					break;
			}
		}
		canvas.restore();
	}
	
	public void suspend()
	{
		m_suspended = true;
	}
	
	public void resume()
	{
		m_suspended = false;		
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (m_suspended)
			return;
		//Log.d("TextReader", "Redraw, clip rect is " + canvas.getClipBounds());
		doDraw(canvas);
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int w = MeasureSpec.getSize(widthMeasureSpec); 
		int h = MeasureSpec.getSize(heightMeasureSpec);
		
		setMeasuredDimension(Math.max(w, m_width + (int)m_padding), h);
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_UP)
		{			
			float x = event.getX() - /*getScrollX() - */getLeft() - getPaddingLeft();
			float y = event.getY() - /*getScrollY() - */getTop() - getPaddingTop();
			m_lastX = event.getRawX();
			//Log.d("FileBrowser", "Click at " + x + ", " + y);
			
			for (int i = 0; i < m_images.size(); i++)
			{
				GalleryImage image = m_images.get(i);
				RectF rect = image.getRectF();

				if (x > rect.left && x <= rect.right && y > rect.top && y <= rect.bottom)					 
				{
					//Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
					//canvas.drawBitmap(bitmap, null, rect, m_bitmapPaint);
					//Log.d("FileBrowser", "Click at image " + i);
					image.performClick();
					return true;
				}
			}
		}
		
		return true;
	}

	public void invalidate(GalleryImage image)
	{
		if (m_suspended)
			return;

		Rect rect = image.getRect();
		rect.offset(getPaddingLeft(), getPaddingTop());		
		
		this.invalidate(rect);		
	}

	
	public static class MassFadeAnimation implements Runnable
	{
		private final GalleryView m_view;
		private final int m_startAlpha;
		private final int m_deltaAlpha;
		private final int m_time;
		private long m_startTime;

		public MassFadeAnimation(GalleryView view, int fromAlpha, int targetAlpha, int time)
		{
			m_view = view;
			m_startAlpha = fromAlpha;
			m_deltaAlpha = targetAlpha - m_startAlpha;
			m_startTime = 0;
			m_time = time;
			m_view.m_animations++;
		}

		public void run()
		{
			if (m_startTime == 0)
				m_startTime = System.currentTimeMillis();
			
			long ctime = System.currentTimeMillis();
			float stage = (float) (ctime - m_startTime) / (float) m_time;

			if (stage > 1)
				stage = 1;

			int alpha = (int)(m_startAlpha + Math.round(m_deltaAlpha * stage));

			for (int i = 0; i < m_view.getChildCount(); i++)
			{
				GalleryImage nview = m_view.getChildAt(i);
				if (nview.getAlpha() != alpha)
				{
					nview.setAlpha(alpha);
					m_view.invalidate(nview);
				}
			}
			
			if (m_startTime + m_time > ctime)
				m_view.postDelayed(this, 5);
			else
				m_view.m_animations--;
		}
	}

	public static class FadeAnimation implements Runnable
	{
		private final GalleryView m_view;
		private final GalleryImage m_image;
		private final int m_startAlpha;
		private final int m_deltaAlpha;
		private final int m_time;
		private long m_startTime;

		public FadeAnimation(GalleryView view, GalleryImage image, int targetAlpha, int time)
		{
			m_view = view;
			m_image = image;
			m_startAlpha = image.getAlpha();
			m_deltaAlpha = targetAlpha - m_startAlpha;
			m_startTime = 0;
			m_time = time;
			m_view.m_animations++;
		}

		public void run()
		{
			if (m_startTime == 0)
				m_startTime = System.currentTimeMillis();
			
			long ctime = System.currentTimeMillis();
			float stage = (float) (ctime - m_startTime) / (float) m_time;

			if (stage > 1)
				stage = 1;

			int alpha = (int)(m_startAlpha + Math.round(m_deltaAlpha * stage));

			if (alpha != m_image.getAlpha())
			{
				m_image.setAlpha(alpha);
				
				m_view.invalidate(m_image);
			}
			
			if (m_startTime + m_time > ctime)
				m_view.postDelayed(this, 5);
			else
				m_view.m_animations--;
		}
	}
	
	public static class ScaleAnimation implements Runnable
	{
		private final GalleryView m_view;
		private final GalleryImage m_image;
		//private final int m_startWidth;
		//private final int m_startHeight;
		//private final float m_deltaWidth;
		//private final float m_deltaHeight;
		private float m_startScale;
		private float m_deltaScale;
		private final int m_time;
		private final float m_targetScale;
		private long m_startTime;

		public ScaleAnimation(GalleryView view, GalleryImage image, float targetScale, int time)
		{
			m_view = view;
			m_image = image;
			//m_startWidth = image.getWidth();
			//m_startHeight = image.getHeight();
			//m_deltaWidth = Math.round(m_startWidth * (scale - 1));
			//m_deltaHeight = Math.round(m_startHeight * (scale - 1));
			m_targetScale = targetScale;
			m_startTime = 0;
			m_time = time;
			m_view.m_animations++;
		}

		public void run()
		{
			if (m_startTime == 0)
			{
				m_startTime = System.currentTimeMillis();
				m_startScale = m_image.getScale();
				m_deltaScale = m_targetScale - m_startScale;
			}
				
			long ctime = System.currentTimeMillis();
			float stage = m_time == 0 ? 1 : (float) (ctime - m_startTime) / (float) m_time;

			if (stage > 1)
				stage = 1;
			
			float scale = m_startScale + m_deltaScale * stage;

			float wchange = m_image.getWidth() * (scale - m_image.getScale()) * 0.5f;
			
			if (Math.abs(wchange) >= 1 || stage == 1)
			{			
				m_image.setScale(scale);
				
				//if (scale >= 1)
				{
					boolean found = false;
					for (int i = 0; i < m_view.getChildCount(); i++)
					{
						GalleryImage nview = m_view.getChildAt(i);
						if (!found)
							nview.setX(nview.getX() - wchange); 
						else
							nview.setX(nview.getX() + wchange);
						
						if (nview == m_image)
							found = true;
					}
				}
				m_view.invalidate();
			}
			
			if (m_startTime + m_time > ctime)
				m_view.postDelayed(this, 1);
			else
				m_view.m_animations--;
		}
	}	
}

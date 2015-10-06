package net.runserver.library;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class GalleryImage
{
	public interface OnClickListener
	{
		void onClick(GalleryImage image);
	}
	
	private final int m_width;
	private final int m_height;
	private final int m_baseLine;
	
	private float m_x;
	private float m_y;
	private Bitmap m_smallBitmap;
	private Bitmap m_mirrorBitmap;
	private Bitmap m_bigBitmap;
	private int m_smallWidth;
	private int m_smallHeight;
	
	private Object m_tag;
	private int m_id;
	private int m_alpha = 255;
	private float m_scale = 1;
	private OnClickListener m_onClickListener;
	
	private Rect m_mainRect = null;
	private Rect m_mirrorRect = null;
	
	
	public float getX()
	{
		return m_x;
	}
	
	public float getY()
	{
		return m_y;
	}
	
	public int getWidth()
	{
		return m_width;
	}
			
	public int getHeight()
	{
		return m_height;
	}
	
	public int getAlpha()
	{
		return m_alpha;
	}
	
	public RectF getRectF()
	{
		return new RectF(m_x, m_y, m_x + m_width * m_scale, m_y + m_height * m_scale);
	}
	
	public Rect getRect()
	{
		return new Rect((int)m_x, (int)m_y, (int)(m_x + m_width * m_scale), (int)(m_y + m_height * m_scale));
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public Object getTag()
	{
		return m_tag;
	}
	
	public void setOnClickListener(OnClickListener listener)
	{
		m_onClickListener = listener;		
	}
	
	public void performClick()
	{
		m_onClickListener.onClick(this);						
	}

	public void setId(int id)
	{
		m_id = id;		
	}

	public void setAlpha(int alpha)
	{
		m_alpha = alpha;		
	}
	
	public void setTag(Object tag)
	{
		m_tag = tag;		
	}
	
	public void setX(float x)
	{
		m_x = x;
		
		if (m_mainRect != null && (int)x != m_mainRect.left)
		{
			m_mainRect = null;
			m_mirrorRect = null;
		}
	}
	
	public void setY(float y)
	{
		m_y = y;
	}
	
	public void setScale(float value)
	{
		m_scale = value;
		
		if (m_mainRect != null)
		{
			int nwidth = Math.round(m_smallWidth * m_scale);
			
			if (nwidth != m_mainRect.width())
			{		
				m_mainRect = null;
				m_mirrorRect = null;
			}
		}
	}
	
	public float getScale()
	{
		return m_scale;
	}
	
	public float getMaximumScale()
	{
		/*if (m_bigBitmap.getHeight() > m_bigBitmap.getWidth())
			return m_bigBitmap.getHeight()/(float)m_smallHeight;
		else
			return m_height/(float)m_smallHeight;*/
		return m_height/(float)m_baseLine;
	}
	
	public GalleryImage(int width, int height, int baseLine)
	{
		m_width = width;
		m_height = height;
		m_baseLine = baseLine;
	}
	
	public void draw(Canvas canvas, Paint paint, Paint shadowPaint)
	{		
		paint.setAlpha(m_alpha);		
		
		if ((m_bigBitmap == null) || (m_smallBitmap != null && Math.abs(m_scale - 1) <= 0.01))
		{
			paint.setFilterBitmap(false);
			
			if (m_mainRect == null || m_mirrorRect == null)
			{
				m_mainRect = new Rect(
									(int)Math.round(m_x + (m_width - m_smallWidth) / 2),
									(int)Math.round(m_y + m_baseLine - m_smallHeight),
									(int)Math.round(m_x + (m_width + m_smallWidth) / 2),
									(int)Math.round(m_y + m_baseLine));
				
				m_mirrorRect = new Rect(
									(int)Math.round(m_x),
									(int)Math.round(m_y + m_baseLine),
									(int)Math.round(m_x + m_mirrorBitmap.getWidth()),
									(int)Math.round(m_y + m_baseLine + m_mirrorBitmap.getHeight()));
						
			
			}
			
			if (shadowPaint != null && !m_smallBitmap.hasAlpha())
			{
				canvas.drawRect(m_mainRect.left, m_mainRect.top - 1, m_mainRect.right + 2, m_mainRect.bottom + 2, shadowPaint);
			}
			
			canvas.drawBitmap(m_smallBitmap, null, m_mainRect, paint);
			canvas.drawBitmap(m_mirrorBitmap, null, m_mirrorRect, paint);
		}
		else
		{			
			paint.setFilterBitmap(true/*m_smallBitmap == null*/);
			
			if (m_mainRect == null || m_mirrorRect == null)
			{
				int baseLine = (int)Math.round(m_baseLine * m_scale);
				
				int nwidth = Math.round(m_smallWidth * m_scale);
				int nheight = Math.round(m_smallHeight * m_scale);
				int mheight = Math.round(m_mirrorBitmap.getHeight() * m_scale);
				int mwidth = Math.round(m_mirrorBitmap.getWidth() * m_scale);
				//int shift = Math.round(m_difference * m_scale);
				int totalWidth = Math.round(m_width * m_scale);
				
				/*if (nheight >= m_bigBitmap.getHeight())
				{
					int x = (int)Math.round(m_x + (totalWidth - nwidth) / 2.0f);
					int y = (int)Math.round(m_y) + nheight - m_bigBitmap.getHeight();
					
					m_mainRect = new Rect(x, y, x, y + nheight);
					m_mirrorRect = new Rect(
							(int)Math.round(m_x),
							(int)Math.round(m_y + nheight),
							(int)Math.round(m_x + mwidth),
							(int)Math.round(m_y + mheight + nheight));
				} else
				{				
					m_mainRect = new Rect(
									(int)Math.round(m_x + (totalWidth - nwidth) / 2.0f),
									(int)Math.round(m_y + shift),
									(int)Math.round(m_x + (totalWidth + nwidth) / 2.0f),
									(int)Math.round(m_y + shift + nheight));

					m_mirrorRect = new Rect(
									(int)Math.round(m_x),
									(int)Math.round(m_y + shift + nheight),
									(int)Math.round(m_x + mwidth),
									(int)Math.round(m_y + shift + mheight + nheight));
				}*/

				m_mainRect = new Rect(
									(int)Math.round(m_x + (totalWidth - nwidth) / 2.0f),
									(int)Math.round(m_y + baseLine - nheight),
									(int)Math.round(m_x + (totalWidth + nwidth) / 2.0f),
									(int)Math.round(m_y + baseLine));

				m_mirrorRect = new Rect(
									(int)Math.round(m_x),
									(int)Math.round(m_y + baseLine),
									(int)Math.round(m_x + mwidth),
									(int)Math.round(m_y + baseLine + mheight));
				
			}
			
			
			if (/*m_mainRect.height() >= m_bigBitmap.getHeight()*/m_scale>=getMaximumScale())
			{
				if (shadowPaint != null && !m_bigBitmap.hasAlpha())
				{
					canvas.drawRect(m_mainRect.left, m_mainRect.top - 1, m_mainRect.right + 2, m_mainRect.bottom + 2, shadowPaint);
				}
				
				canvas.drawBitmap(m_bigBitmap, null, m_mainRect, paint);
				//if (m_smallBitmap == null) // non-scalable image
					//canvas.drawBitmap(m_mirrorBitmap, null, m_mirrorRect, paint/*filteredPaint*/);
			} else
			{
				canvas.drawBitmap(m_bigBitmap, null, m_mainRect, paint/*filteredPaint*/);
				canvas.drawBitmap(m_mirrorBitmap, null, m_mirrorRect, paint/*filteredPaint*/);
			}
		}
	}

	public void setBitmaps(Bitmap smallBitmap, Bitmap bigBitmap, Bitmap mirrorBitmap)
	{
		m_smallBitmap = smallBitmap;
		m_smallWidth = smallBitmap == null ? bigBitmap.getWidth() : smallBitmap.getWidth();
		m_smallHeight = smallBitmap == null ? bigBitmap.getHeight() : smallBitmap.getHeight();
		m_bigBitmap = bigBitmap;
		m_mirrorBitmap = mirrorBitmap;
		m_mainRect = null;
		m_mirrorRect = null;
	}

	public void clean()
	{
		/*
		if (m_bigBitmap != m_smallBitmap)
			m_bigBitmap.recycle();
		
		m_smallBitmap.recycle();
		
		m_mirrorBitmap.recycle();*/
	}
}	

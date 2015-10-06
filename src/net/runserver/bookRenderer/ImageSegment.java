package net.runserver.bookRenderer;

import net.runserver.bookParser.ImageData;
import net.runserver.bookRenderer.TextPage.IPageSegment;
import net.runserver.bookRenderer.TextPage.PageCaret;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class ImageSegment implements IPageSegment
{
	private static final Paint s_bitmapPaint;
	private static final Paint s_backPaint;
	
	static
	{
		s_bitmapPaint = new Paint();
		s_bitmapPaint.setFilterBitmap(true);
		s_bitmapPaint.setColor(0xFFFFFFF);
		s_bitmapPaint.setDither(true);
		s_bitmapPaint.setAlpha(255);
		
		s_backPaint = new Paint();
		s_backPaint.setColor(0xFFFFFFFF);
	}
	
	private int m_width;
	private int m_height;
	private final ImageData m_image;
	private final long m_position;
	
	private int m_widthOffset;
	private int m_heightOffset;
	private boolean m_finished;
	
	public long getPosition()
	{
		return m_position;
	}
	
	public ImageSegment(ImageData image, int width, int height, long position)
	{
		m_image = image;
		m_width = width;
		m_height = height;
		m_finished = false;
		m_position = position;
	}	
	
	public void draw(Canvas canvas, float shiftX,PageCaret caret, int pageWidth, int pageHeight, boolean onlyOne)
	{
		if (!m_finished)
			finish(pageWidth, pageHeight, onlyOne);
		
		float posx = shiftX + m_widthOffset;
		float posy = caret.getPosY() + m_heightOffset + caret.getLastHeightMod();
		
		int width = (m_width >> 4);
		int height = (m_height >> 4);
		
		Bitmap bitmap = m_image.extractImage(width, height);
		
		//Log.d("TextReader", "Painting image at " + posx + ", " + posy + ", width offset " + m_widthOffset);
		
		if (bitmap != null && !bitmap.isRecycled())
		{	
			canvas.drawRect(new RectF(posx, posy, posx + width, posy + height), s_backPaint);
			canvas.drawBitmap(bitmap, null, new RectF(posx, posy, posx + width, posy + height), s_bitmapPaint);
		} else
		{
			canvas.drawRect(new RectF(posx, posy, posx + width, posy + height), s_bitmapPaint);
		}
		caret.setLastHeightMod(0);
			
		posx += width;
		posy += height;
		
		caret.setPosX(posx);
		caret.setPosY(posy);
	}

	private void finish(int pageWidth, int pageHeight, boolean onlyOne)
	{
		if (onlyOne)
		{
			m_height = m_height * pageWidth / m_width;
			m_width = pageWidth;
			
			if (m_height > pageHeight)
			{
				m_width = m_width * pageHeight / m_height;
				m_height = pageHeight;			
			}
		}
		
		m_widthOffset = ((pageWidth - m_width) / 32);
		m_heightOffset = onlyOne ? ((pageHeight - m_height) / 32) : 1;
		m_finished = true;
	}
	
	public boolean calculate(PageCaret caret, int maxHeight)
	{	
		float posy = caret.getPosY() + m_heightOffset + caret.getLastHeightMod() + (m_height >> 4);
		
		if (posy > maxHeight)
			return false;
		
		caret.setLastHeightMod(0);		
		caret.setPosY(posy);
		return true;
	}
	
	public void clean()
	{
		m_image.clean();
	}
}
package net.runserver.bookRenderer;

import net.runserver.common.FixedCharSequence;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.text.TextPaint;

public class FontStyle
{
	private static final char FIRST_RIGHT_TO_LEFT = '\u0590';
	private static final char LAST_RIGHT_TO_LEFT = '\u076d';
	private static final char FIRST_RIGHT_TO_LEFT_EX = '\ufb1d';
	private static final char LAST_RIGHT_TO_LEFT_EX = '\ufeff';
	
	private static float [] s_width = new float[32];
	
	private final TextPaint m_paint;
	
	private final float m_heightMod;
	private final float m_lineSpace;
	private final float m_height;
	private final float m_spaceWidth;
	private final float m_wordSpaceWidth;
	private final float m_dashWidth;	
	private float m_firstLine;
	
	private final int m_dashWidthInt;
	private final int m_spaceWidthInt;
	private final int m_wordSpaceWidthInt;
	private final int m_heightInt;
	private final int m_heightModInt;
	private int m_firstLineInt;
	private final boolean m_hasTab;
	
	private final char m_spaceChar;
	
	public TextPaint getPaint()
	{
		return m_paint;
	}
	
	public float getHeightMod()
	{
		return m_heightMod;
	}

	public float getHeight()
	{
		return m_height;
	}	

	public int getHeightModInt()
	{
		return m_heightModInt;
	}
	
	public int getHeightInt()
	{
		return m_heightInt;
	}
	
	public float getSpaceWidth()
	{
		return m_spaceWidth;
	}
	
	public float getDashWidth()
	{
		return m_dashWidth;
	}
	
	public int getDashWidthInt()
	{
		return m_dashWidthInt;
	}
	
	public int getSpaceWidthInt()
	{
		return m_spaceWidthInt;
	}
	
	public float getFirstLine()
	{
		return m_firstLine;
	}
	
	public int getFirstLineInt()
	{
		return m_firstLineInt;
	}
	
	public int getWordSpaceWidthInt()
	{
		return m_wordSpaceWidthInt;
	}
	
	public char getSpaceChar()
	{
		return m_spaceChar;
	}
	
	public boolean hasTab()
	{
		return m_hasTab;
	}
	
	public void setFirstLine(float value)
	{
		m_firstLine = value;
		m_firstLineInt = (int)(value * 16);
	}
	
	public FontStyle(TextPaint paint, float extraSpace, float lineSpace, float firstLine)
	{
		//m_heightMod = heightMod;
		m_paint = paint;
		m_lineSpace = lineSpace;
		m_firstLine = firstLine;
		
		m_height = paint.getTextSize();// * (2 * m_lineSpace - 1);
		
		m_heightMod = /*extraSpace / 2 +*/ (m_lineSpace - 0.75f) * paint.getTextSize() * 0.5f;
		//getFontMetrics(null) * m_lineSpace + heightMod * 2;
		m_wordSpaceWidth = measureChar(' ');
		
		Rect spaceBounds = new Rect();
		m_paint.getTextBounds(new char[]{' '}, 0, 1, spaceBounds); // thin space
		
		float spaceWidth = spaceBounds.width();
		if (spaceBounds.height() > paint.getTextSize() / 4 || spaceWidth == 0)
		{
			m_spaceWidth = m_wordSpaceWidth;
			m_spaceChar = ' ';
			//Log.d("TextReader", "Using regular space symbol cause thin width is " + spaceWidth + ", height is " + spaceBounds.height());
		} else
		{		
			m_spaceWidth = spaceWidth;
			m_spaceChar = ' ';
		}
		
		m_dashWidth = measureChar('-');
		
		Rect tabBounds = new Rect();
		m_paint.getTextBounds(new char[]{'\t'}, 0, 1, tabBounds);
		
		m_hasTab = tabBounds.height() < paint.getTextSize() / 4;
		
		m_heightModInt = (int)(m_heightMod * 16);  
		m_spaceWidthInt = (int)(m_spaceWidth * 16);
		m_wordSpaceWidthInt = (int)(m_wordSpaceWidth * 16);
		m_dashWidthInt = (int)(m_dashWidth * 16);
		m_heightInt = (int)(m_height * 16);
		m_firstLineInt = (int)(firstLine * 16);		
	}

	public void changeContrast(float extraStroke)
	{
		if (extraStroke > 0)
		{
			m_paint.setStrokeWidth(extraStroke);
			m_paint.setStyle(Style.FILL_AND_STROKE);
		} else
			m_paint.setStyle(Style.FILL);
	}

	public void drawText(Canvas canvas, String text, float posx, float posy)
	{
		canvas.drawText(text, posx, posy, m_paint);
	}
	
	public void drawText(Canvas canvas, FixedCharSequence text, float posx, float posy)
	{
		char [] chars = text.getChars();
		int offset = text.getOffset();
		int length = text.length();
		
/*		if (chars[offset] >= FIRST_RIGHT_TO_LEFT) // right to left text
		{
			char [] newchars = new char[length];
			
			int end = offset + length;
			for(int i=offset;i<end;i++)
				newchars[length-i] = chars[i];
			
			chars = newchars;
			offset = 0;
		}*/
		
		canvas.drawText(chars, offset, length, posx, posy, m_paint);
		//drawText(text, posx, posy, m_paint);
	}
	
	public float measureChar(char ch)
	{
		return m_paint.measureText(new char[]{ch},0,1);
	}
	
	public float[] getTextWidths(CharSequence text)
	{
		if (text.length() > s_width.length)
			s_width = new float[text.length()];
		m_paint.getTextWidths(text, 0, text.length(), s_width);
		return s_width;
	}

	public float measureText(CharSequence text)
	{
		return m_paint.measureText(text, 0, text.length());		
	}
	
	public float measureText(FixedCharSequence text)
	{
		return m_paint.measureText(text.getChars(), text.getOffset(), text.length());		
	}

	public int measureTextInt(CharSequence text)
	{
		return (int)(m_paint.measureText(text, 0, text.length()) * 16);
	}

	public int measureTextInt(FixedCharSequence text)
	{
		return (int)(m_paint.measureText(text.getChars(), text.getOffset(), text.length()) * 16);
	}
	
	public static boolean isRTL(CharSequence text)
	{
		char ch = text.charAt(0);
		return (ch >= FIRST_RIGHT_TO_LEFT && ch <=LAST_RIGHT_TO_LEFT) || (ch >= FIRST_RIGHT_TO_LEFT_EX && ch <= LAST_RIGHT_TO_LEFT_EX);
		//return BoringLayout.isBoring(text, m_paint) != null;
		//return text.charAt(0)>=FIRST_RIGHT_TO_LEFT;
	}
	
	public static String reverseString(String value)
	{
		if (ArabicReshaper.isArabicCharacter(value.charAt(0)))
			value = ArabicReshaper.process(value);			
		
		// simple cases
		int length = value.length();
		int offset = 0;
		char [] newchars = new char[length];
		char [] chars = value.toCharArray();
		
		int end = offset + length;
		for(int i=offset;i<end;i++)
			newchars[end - i - 1] = chars[i];
		
		return new String(newchars, 0, length);
	}
	
	public static FixedCharSequence reverseString(FixedCharSequence value)
	{
		if (ArabicReshaper.isArabicCharacter(value.charAt(0)))
			value = FixedCharSequence.toFixedCharSequence(ArabicReshaper.process(value.toString()));
		
		int length = value.length();
		char [] newchars = new char[length];
		char [] chars = value.getChars();
		int offset = value.getOffset();
		
		int end = offset + length;
		for(int i=offset;i<end;i++)
			newchars[end - i - 1] = chars[i];
		
		return new FixedCharSequence(newchars, 0, length);
	}
	
	//private static int convertUnicodeTo1251(char c)
	//{c
		//return c < 128 ? (int) (c&0xFF) : (int) (c - 848)&0xFF;
	//}

	//private static char convert1251ToUnicode(int c)
	//{
		//if (c == 80)
			//c = 32;
		//return c > 191 ? (char) (c + 848) : (char) c;
	//}
}

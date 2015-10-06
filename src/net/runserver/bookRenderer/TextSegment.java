package net.runserver.bookRenderer;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Canvas;

import net.runserver.bookParser.BaseBookReader;
import net.runserver.bookRenderer.TextPage.IPageSegment;
import net.runserver.bookRenderer.TextPage.PageCaret;
import net.runserver.common.BaseActivity;
import net.runserver.common.FixedCharSequence;
import net.runserver.common.FixedStringBuilder;

public class TextSegment implements IPageSegment
{
	private int m_flags;
	private int m_letters;
	private int m_width;
	private int m_lineWidth;
	private List<FixedCharSequence> m_words;	
	private FixedCharSequence m_text;
	
	private final FontStyle m_paint;
	private final long m_position;

	public int getFlags()
	{
		return m_flags;
	}
	
	public long getPosition()
	{
		return m_position;
	}

	public int getWidth()
	{
		return m_width;
	}
	
	public void setLineWidth(int value)
	{
		m_lineWidth = value;
	}
	
	public void setFlags(int value)
	{
		m_flags = value;
	}
	
	public int getChars()
	{
		return m_text == null ? m_letters + m_words.size() - 1 : m_text.length();
	}

	public FixedCharSequence getText()
	{
		return m_text;
	}

	public void appendText(FixedCharSequence value, int width)
	{
		if (!BaseActivity.isBiDirStringSupported && FontStyle.isRTL(value))
		{
			value = FontStyle.reverseString(value);
			//width = m_paint.measureTextInt(value);
		}
		
		int length = value.length();	
		
		m_words.add(value);
		m_letters += length;
		m_width += width;
	}

	public void appendNonBreakText(CharSequence value, int width)
	{
		CharSequence old = m_words.get(m_words.size() - 1);
		FixedStringBuilder builder = new FixedStringBuilder(old.length() + value.length());
		builder.append(old);
		builder.append(value);
		
		m_words.set(m_words.size() - 1, builder.toCharSequence());
		m_letters += value.length();
		m_width += width;
	}
	
	public void appendSpace(int width)
	{
		if (width != 0)
		{
			m_letters ++;
			m_width += width;
		}
	}
	
	public FontStyle getPaint()
	{
		return m_paint;
	}

	public TextSegment(FixedCharSequence text, int flags, FontStyle paint, int width, long position)
	{
		m_words = new LinkedList<FixedCharSequence>();
		//m_words = new ArrayList<CharSequence>(16);
		m_flags = flags;
		m_paint = paint;
		m_position = position;
		appendText(text, width);
	}
	
	public void justify(int targetWidth)
	{
		//Log.d("TextReader", "Justify for string width " + m_lineWidth + ", target width " + targetWidth + ", width " + m_width);			
		
		if (m_lineWidth == -1 || m_words.size() == 0)
		{
			finish();
			return;
		}
		
		char spaceChar = m_paint.getSpaceChar();
		
		if (m_lineWidth == 0)
			m_lineWidth = m_width;
		
		if (m_words.size() == 1)
		{
			if ((m_flags & BaseBookReader.NEW_LINE) == 0)
			{
				int spacesNeedeed = (targetWidth - m_lineWidth) / m_paint.getSpaceWidthInt();
				FixedStringBuilder result = new FixedStringBuilder(m_letters + spacesNeedeed);
				for(int j = 0; j < spacesNeedeed; j++)
					result.append(spaceChar);
				
				result.append(m_words.get(0));					
				m_text = result.toCharSequence();
				//m_words.clear();
				m_words = null;
			} else				
				finish();
			
			return;
		}
		
		int nspaces = (m_words.size() - 1);		
		int spaceNeeded = (targetWidth - m_lineWidth)  / m_paint.getSpaceWidthInt();
		
		if (spaceNeeded <= 0)
		{
			spaceNeeded = 0;
			//finish();
			//return;
		} else
		{
			//if ((targetWidth - m_lineWidth) % m_paint.getSpaceWidthInt() > m_paint.getSpaceWidthInt() / 2)
//				spaceNeeded ++;
		}
		
		//spaceNeeded += nspaces;
		
		int toAdd = (spaceNeeded / nspaces);
		if (spaceNeeded % nspaces != 0)
			toAdd++;
		
		if (toAdd < 0)
			toAdd = 0;
		
		
		FixedStringBuilder result = new FixedStringBuilder(m_letters + nspaces + spaceNeeded + 1);
		
		if ((m_flags & BaseBookReader.RTL) != 0 && !BaseActivity.isBiDirStringSupported)
		{
			int size = m_words.size();
			for (int i = size - 1; i >= 0; i--)
			{
				result.append(' ');
				if (i != size - 1)
				for (int j = 0; j < toAdd; j++)
				{
					result.append(spaceChar);
					spaceNeeded--;
					if (spaceNeeded <= 0)
					{
						toAdd = 0;
					}
				}
				result.append(m_words.get(i));
			}
		} else
		{
			boolean first = true;
			for (FixedCharSequence word : m_words)
			{
				result.append(' ');
				if (!first)
				{
					for (int j = 0; j < toAdd; j++)
					{
						result.append(spaceChar);
						spaceNeeded--;
						if (spaceNeeded <= 0)
						{
							toAdd = 0;
						}
					}
				} else
					first = false;
				result.append(word);
			}
		}

		m_text = result.toCharSequence(1);
		//m_words.clear();
		m_words = null;		
		
		m_width >>= 4;
		
		//Log.d("TextReader", "Justified string " + m_text + ", width " + m_paint.measureText(m_text) + ", line width " + m_lineWidth);
		//m_width = m_paint.measureText(m_text);
	}
	
	public void finish()
	{
		//Log.d("TextReader", "Allocating string buffer " + (m_letters + 2));
		
		if (m_words.size() == 1)
			m_text = m_words.get(0);
		else
		{
			FixedStringBuilder result = new FixedStringBuilder(m_letters + 1);
			
			if ((m_flags & BaseBookReader.RTL) != 0 && !BaseActivity.isBiDirStringSupported)
			{
				int size = m_words.size();
				for (int i = size - 1; i >= 0; i--)
				{
					result.append(' ');
					result.append(m_words.get(i));
				}
			} else
			{
				for (FixedCharSequence word : m_words)
				{
					result.append(' ');
					result.append(word);
				}
			}	
			m_text = result.toCharSequence(1);
		}		
		//m_words.clear();
		m_words = null;
		
		//Log.d("TextReader", "Result string " + m_text + ", length " + m_text.length());
		m_width >>= 4;
	}
	
	public void draw(Canvas canvas, float shiftX, PageCaret caret, int pageWidth, int pageHeight, boolean onlyOne)
	{
		float posx = caret.getPosX();
		float posy = caret.getPosY();
		
		if ((m_flags & BaseBookReader.NEW_LINE) != 0)
		{
			posx = shiftX;
			posy += m_paint.getHeight();
			
			//if (!caret.isFirstLine())
			{
				if (caret.getLastHeightMod() != 0)
					posy += caret.getLastHeightMod();
			
				posy += m_paint.getHeightMod();
			}
	
			caret.setLastHeightMod(m_paint.getHeightMod());
			
			if ((m_flags & BaseBookReader.FIRST_LINE) != 0)
				posx += m_paint.getFirstLine();
			
			caret.setPosY(posy);
		}			
		
		if (m_words != null)
			finish(pageWidth, pageHeight, onlyOne);		
		
		if ((m_flags & BaseBookReader.RTL) != 0)
		{	
			m_paint.drawText(canvas, m_text, (pageWidth >> 4) - posx - m_width + shiftX * 2, posy);
			//m_paint.drawText(canvas, m_text, posx, posy);
		}
		else
		{
			m_paint.drawText(canvas, m_text, posx, posy);			
		}
		
		posx += m_width;// >> 4;
		
		caret.setPosX(posx);
	}

	public boolean calculate(PageCaret caret, int maxHeight)
	{
		if (caret.getPosY() > maxHeight)
			return false;
		
		if ((m_flags & BaseBookReader.NEW_LINE) != 0)
		{
			float posy = caret.getPosY();
			posy += m_paint.getHeight();
			
			//if (!caret.isFirstLine())
			{
				if (caret.getLastHeightMod() != 0)
					posy += caret.getLastHeightMod();
			
				posy += m_paint.getHeightMod();
			}
	
			caret.setLastHeightMod(m_paint.getHeightMod());
			caret.setPosY(posy);
			return true;
		}		
		return true;
	}
	
	private void finish(int pageWidth, int pageHeight, boolean onlyOne)
	{
		if ((m_flags & BaseBookReader.RTL) != 0)
		{
			finish();
			m_width = (int)m_paint.measureText(m_text);
			return;
		}
		
		if ((m_flags & (BaseBookReader.JUSTIFY | BaseBookReader.LINE_BREAK)) == BaseBookReader.JUSTIFY)
		{
			justify(pageWidth);
		} else
			finish();			
	}
	
	public void clean()
	{
		m_text = null;
		m_words = null;
	}
	/*
	@Override
	public String toString()
	{
		if (m_text == null)
			finish();
		return m_text.toString();
	}*/
}
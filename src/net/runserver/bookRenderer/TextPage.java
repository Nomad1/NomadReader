/**
 * 
 */
package net.runserver.bookRenderer;

import java.util.LinkedList;
import java.util.List;

import net.runserver.bookParser.BaseBookReader;
import net.runserver.bookParser.ImageData;
import net.runserver.common.BaseActivity;
import net.runserver.common.FixedCharSequence;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class TextPage
{
	public static final int PAGE_BREAK = 8;
	public static final int PAGE_FULL = 1;
	public static final int NEW_PAGE = 2;
	public static final int FOOTER_PAGE_BREAK = 4;
	
	private int m_lastWidth;
	private int m_lastFooterWidth;
	private int m_heightLeft;
	
	private long m_startPosition;
	private long m_endPosition;
	private int m_endOffset;
	
	private List<IPageSegment> m_segments;
	private List<IPageSegment> m_footerSegments;
	private TextSegment m_lastTextSegment;
	private TextSegment m_lastFooterTextSegment;
	
	private final int m_width;
	private final int m_footerSpace;
	private final int m_number;

	private int m_height;
	private Bitmap m_bitmap;
	private boolean m_haveImages;
	private int m_pageFinishType;

	public boolean isEmpty()
	{
		return m_segments.size() == 0;
	}	
	
	public boolean haveImages()
	{
		return m_haveImages;
	}
	
	public long getStartPosition()
	{
		return m_startPosition;
	}
	
	public long getEndPosition()
	{
		return m_endPosition;
	}
	
	public int getEndOffset()
	{
		return m_endOffset;
	}
	
	public int getPageNumber()
	{
		return m_number;
	}
	
	public Bitmap getBitmap()
	{
		return m_bitmap;
	}

	public void setBitmap(Bitmap value)
	{
		m_bitmap = value;
	}
	
	public void setEndPosition(long endPosition)
	{
		m_endPosition = endPosition;
	}
	
	public void setEndOffset(int endOffset)
	{
		m_endOffset = endOffset;
	}
	
	public int getPageFinishType()
	{
		return m_pageFinishType;
	}
	
	/*public int getHeightLeft()
	{
		return m_heightLeft;
	}*/
	
	public TextPage(int width, int height, int footerSpace, int number)
	{
		m_width = width << 4;
		m_height = height << 4;
		m_heightLeft = height << 4;
		m_footerSpace = footerSpace << 4;
		m_segments = new LinkedList<IPageSegment>();
		m_footerSegments = new LinkedList<IPageSegment>();
			//new ArrayList<TextSegment>(50);
		//m_lastTextSegment = null;
		//m_lastWidth = 0;
		m_number = number;

		//Log.d("TextReader", "Text page data width " + width + ", height " + height);
	}

	private TextSegment addTextSegment(FixedCharSequence text, int flags, FontStyle paint, int width, int height, long position)	
	{
		TextSegment segment = null;
		
		if ((flags & BaseBookReader.FOOTER) != 0)
		{
			if (m_footerSegments.size() == 0)
				m_heightLeft -= m_footerSpace;
			
			m_lastFooterTextSegment = segment = new TextSegment(text, flags, paint, width, position);
			m_footerSegments.add(segment);
		} else
		{		
			m_lastTextSegment = segment = new TextSegment(text, flags, paint, width, position);
			m_segments.add(segment);
		}

		if ((flags & BaseBookReader.NEW_LINE) != 0)
		{
			if ((flags & BaseBookReader.FOOTER) != 0)
				m_lastFooterWidth = width;
			else
				m_lastWidth = width;
			
			m_heightLeft -= height;
		}
		return segment;
	}

	/*public void addLineBreak(FontStyle paint)
	{
		if (m_lastTextSegment != null)			
			m_lastTextSegment.setFlags(m_lastTextSegment.getFlags() | BaseBookReader.LINE_BREAK);

		m_lastTextSegment = null;
		m_lastWidth = paint.getFirstLineInt();
		//addTextSegment(" ", flags, paint, width, height, position);
	}*/
	
	public void addNonBreakText(CharSequence word)
	{
		if (m_lastTextSegment != null)			
		{
			int width = m_lastTextSegment.getPaint().measureTextInt(word);
			m_lastWidth += width;			
			m_lastTextSegment.appendNonBreakText(word, width);
		}
	}
	
	public void addNonBreakFooterText(CharSequence word)
	{
		if (m_lastFooterTextSegment != null)			
		{
			int width = m_lastFooterTextSegment.getPaint().measureTextInt(word);
			m_lastFooterWidth += width;			
			m_lastFooterTextSegment.appendNonBreakText(word, width);
		}
	}
	
	public int addWord(FixedCharSequence word, int flags, FontStyle paint, long position)
	{
		/*if (word.length() == 0 || word.equals(' ') || word.equals('�'))
		{
			addLineBreak(paint);
			return word.length();
		}*/
		
		boolean isFooter = (flags & BaseBookReader.FOOTER) != 0;
		TextSegment lastSegment = isFooter ? m_lastFooterTextSegment : m_lastTextSegment;
		//boolean first = isFooter? false/*m_footerSegments.size() == 0*/ : m_segments.size() == 0;
		int lastWidth = isFooter ? m_lastFooterWidth : m_lastWidth;
		
		//Log.d("TextReader", "Adding word " + word);
		
		int height = paint.getHeightInt() + 	paint.getHeightModInt() * 2;
		
		if (isFooter && m_footerSegments.size() == 0)
			height+= m_footerSpace;
		
		int width;
		
		if (!BaseActivity.isBiDirStringSupported && FontStyle.isRTL(word))
			width = paint.measureTextInt(FontStyle.reverseString(word));
		else
			width = paint.measureTextInt(word);
		
		/*if (m_lastSegment == null)
			height -= paint.getHeightModInt(); // first line*/			
		
		if ((flags & BaseBookReader.NEW_LINE) != 0)
		{			
			if (lastSegment != null)			
				lastSegment.setFlags(lastSegment.getFlags() | BaseBookReader.LINE_BREAK);

			
			//if (first)	
				//height -= paint.getHeightModInt(); // first line
			//else
			if (m_heightLeft < height)
			{
				height -= paint.getHeightModInt(); // last line
				
				if (m_heightLeft < height)
					return 0;
			}

			flags |= BaseBookReader.FIRST_LINE;
			
			//Log.d("TextReader", "New line start width " + width + ", " + word + ", flags " + flags);		
			
			addTextSegment(word, flags, paint, width, height, position);
			if (isFooter)
				m_lastFooterWidth += paint.getFirstLineInt();
			else
				m_lastWidth += paint.getFirstLineInt();
			//m_lastSegment.setFlags(m_lastSegment.getFlags() & ~BaseBookReader.WORD_BREAK);
			
			return word.length();
		}

		boolean wordBreak = lastSegment != null && (lastSegment.getFlags() & BaseBookReader.WORD_BREAK) != 0;
		int spaceWidth = lastSegment == null || wordBreak ? 0 : lastSegment.getPaint().getWordSpaceWidthInt();
		
		int usedLength = 0;
		FixedCharSequence restWord = null;
		int restWidth = 0;
		if (lastSegment != null)
		{
			int leftWidth = m_width - spaceWidth - lastWidth; 
			boolean validWidth = width <= leftWidth;			
			boolean shouldHypen = !validWidth && !wordBreak;
			
			if (shouldHypen && m_heightLeft < height) // last line?
				shouldHypen = leftWidth > m_width / 8;  // too much space left					
			
			if (shouldHypen)
			{
				Object [] hypened = new Object[4];
				if (HypenateManager.canHypenate(word, (m_width - spaceWidth - lastWidth)/16.0f, paint.getTextWidths(word), paint.getDashWidth(), hypened))
				{
					restWord = (FixedCharSequence)hypened[1];
					word = (FixedCharSequence)hypened[0];
					int nwidth = paint.measureTextInt(word);
					restWidth = width - nwidth + paint.getDashWidthInt();
					width = nwidth;
					
					usedLength = word.length() - 1;					
					validWidth = true;
					position += usedLength;
				}
			}
				
			if (validWidth)
			{				
				lastSegment.appendSpace(spaceWidth);
				if (isFooter)
					lastWidth = m_lastFooterWidth = m_lastFooterWidth + width + spaceWidth;
				else
					lastWidth = m_lastWidth = m_lastWidth + width + spaceWidth;
				
				if ((flags & BaseBookReader.WORD_BREAK) != 0)
					lastSegment.setFlags(lastSegment.getFlags() | BaseBookReader.WORD_BREAK);
				else
					lastSegment.setFlags(lastSegment.getFlags() & ~BaseBookReader.WORD_BREAK);
				
				if (lastSegment.getPaint() == paint)
				{
					if (wordBreak)
						lastSegment.appendNonBreakText(word, width);
					else
						lastSegment.appendText(word, width);
				} else
				{
					//Log.d("TextReader", "New line segment width " + m_lastWidth + ", " + word);
					
					lastSegment.setLineWidth(-1); // do not justify
					
					lastSegment = addTextSegment(word, flags &~ BaseBookReader.NEW_LINE, paint, width, height, position);
					
					lastWidth = isFooter ? m_lastFooterWidth : m_lastWidth;
				}
				
				if (usedLength == 0)
					return word.length();
				else					
				{
					width = restWidth;
					word = restWord;
				}
			}
		}		
		
		// Log.d("TextReader", "Text width " + (width + spaceWidth + m_lastWidth));
		if (lastSegment != null)
			lastSegment.setLineWidth(lastWidth);
		
		
		//if (first)	
		//	height -= paint.getHeightModInt();
		//else
		if (m_heightLeft < height)
		{
			height -= paint.getHeightModInt(); // last line
			if (m_heightLeft < height)
				return usedLength;
			//else
				//Log.d("TextReader", "Last line height " + height + ", left " + m_heightLeft);
		};
		//Log.d("TextReader", "New full line width " + m_lastWidth + ", " + word);			

		addTextSegment(word, flags | BaseBookReader.NEW_LINE, paint, width, height, position);
				
		return usedLength + word.length();
	}
	
	public void addLink(String title, CharSequence text, FontStyle paint, long position)
	{
		//spaint.getPaint().
		addWord(FixedCharSequence.toFixedCharSequence(text), 0, paint, position);
	}

	public boolean addImage(ImageData image, CharSequence text, FontStyle paint, long position)
	{
		int width = image.getWidth() << 4;		
		int height = image.getHeight() << 4;
		
		if (width > m_width)
		{
			height = height * m_width / width;
			width = m_width;
		}
		
		if (height > m_height)
		{
			width = width * m_height / height;
			height = m_height;			
		}

		/*if ((m_heightLeft < height /2 && m_heightLeft < m_height/2) || (m_heightLeft >= m_height/2 && m_heightLeft < height * 2 / 3))
		{
			//Log.d("TextReader", "Not enought free space on page to place image");
			m_heightLeft = 0;
			return false;
			
		} else*/
			
		if (m_heightLeft >= height * 4/5 || (m_heightLeft >= m_height / 2))
		{
			if (height > m_heightLeft)
			{
				width = (int)(width * m_heightLeft / height); 
				height = m_heightLeft;
			}
				
			m_heightLeft -= height;
			
			m_segments.add(new ImageSegment(image, width, height, position));
			m_haveImages = true;
			
			if (m_heightLeft < paint.getHeightInt() * 2)
				m_heightLeft = 0;
			
			return true;
		}
		
		m_heightLeft = 0;
		return false;		
	}
	
	public void clean()
	{
		if (m_bitmap != null)
			m_bitmap.recycle();

		for(IPageSegment segment: m_segments)
			segment.clean();
			
		m_segments.clear();
		
		for(IPageSegment segment: m_footerSegments)
			segment.clean();
		
		m_footerSegments.clear();
	}

	public void trimLast()
	{
		//Log.d("TextReader", "Trimming last line");
		if (m_lastTextSegment != null)
		{
			//m_lastTextSegment.finish();
			//Log.d("TextReader", "Trimming last line: " + m_lastTextSegment.getText());
			
			m_heightLeft += m_lastTextSegment.getPaint().getHeightInt() + m_lastTextSegment.getPaint().getHeightModInt();
			m_segments.remove(m_lastTextSegment);
		}
		
		m_lastTextSegment = null;
		m_lastWidth = 0;
		for(int i=m_segments.size() - 1;i>=0;i--)
		{
			IPageSegment segment = m_segments.get(i);
			if (segment.getClass() == TextSegment.class)
			{
				m_lastTextSegment = (TextSegment)segment;
				break;
			} 
		}
	}

	public void finish(int pageFinishType, int height, int shiftY)
	{
		m_startPosition = -1;
		
		if ((pageFinishType & PAGE_BREAK) == 0 && m_lastTextSegment != null)
			m_lastTextSegment.setFlags(m_lastTextSegment.getFlags() | BaseBookReader.LINE_BREAK);
		
		if (m_lastTextSegment != null && m_lastWidth > 0)
			m_lastTextSegment.setLineWidth(m_lastWidth);
		
		if ((pageFinishType & FOOTER_PAGE_BREAK) == 0 && m_lastFooterTextSegment != null)
			m_lastFooterTextSegment.setFlags(m_lastFooterTextSegment.getFlags() | BaseBookReader.LINE_BREAK);
		
		if (m_lastFooterTextSegment != null && m_lastFooterWidth > 0)
			m_lastFooterTextSegment.setLineWidth(m_lastFooterWidth);
		
		m_pageFinishType = pageFinishType;
		
		if (m_height / 16 != height)
		{
			int origHeight = height;
			if (pageFinishType == NEW_PAGE)				
				height = height * 3/4;
			
			int bigHeight = height << 4; 
			
			
			if (m_height - m_heightLeft > bigHeight)
			{			
				//int offset = (bigHeight - m_height + m_heightLeft) / 16; // >>4 
	
				PageCaret caret = new PageCaret(0, shiftY);
				
				for(IPageSegment segment: m_footerSegments)
				{
					segment.calculate(caret, height);
				}
				
				int first = m_segments.size() - 1;
				//float lastY = 0;
				//boolean found = false;
				//caret.setFirstLine(false);
				
				for (int i = m_segments.size() - 1;i>=0;i--)
				{
					/*if (!found && caret.getPosY() >= 0)
					{
						first = i;
						caret.reset();
						found = true;
					}*/
					if (m_segments.get(i).calculate(caret, height))
					{
						first = i;
					} else
						break;
				}
				
				m_heightLeft = (int)((height - caret.getPosY() + shiftY) * 16); 
					//m_height - m_heightLeft - (((int)(caret.getPosY()/* - caret.getLastHeightMod()*/)) << 4);
				m_height = origHeight << 4;
				
				if (first < 0)
				{
					m_segments.clear();
					return;
				} else
					m_segments = m_segments.subList(first, m_segments.size());
			}
			
		}
		if (m_segments.size() > 0)
		{
			m_startPosition = m_segments.get(0).getPosition();
			//Log.d("TextReader", "Page start position is " + m_startPosition + ", segment 0: " + m_segments.get(0));
		}
	}

	public void draw(Canvas canvas, int shiftX, int shiftY, Paint footerLine)
	{
		boolean one = /*m_lastTextSegment == null &&*/ m_segments.size() == 1 && m_footerSegments.size() == 0;
		float posY = shiftY;
		
		if (!one && m_heightLeft < m_height / 16 /* && m_footerSegments.size() == 0*/)
			posY += m_heightLeft / 48;
		
		PageCaret caret = new PageCaret(shiftX, posY);

		for (IPageSegment segment : m_segments)
			segment.draw(canvas, shiftX, caret, m_width, m_height, one);
		
		if (m_footerSegments.size() > 0)
		{
		//	caret.setPosY(caret.getPosY() + caret.getLastHeightMod());
			
			caret.reset();
			for (IPageSegment segment : m_footerSegments)
				segment.calculate(caret, 0x10000);
			
			//m_height >> 4
			posY = (m_height >> 4) - caret.getPosY() + shiftY - caret.getLastHeightMod();
			
			canvas.drawLine(shiftX, posY, shiftX + (m_width >> 4), posY, footerLine);
			
			caret.setPosY(posY);
			caret.setLastHeightMod(0);
			caret.setFirstLine(true);
			
			for (IPageSegment segment : m_footerSegments)
				segment.draw(canvas, shiftX, caret, m_width, m_height, false);
		}
	}
	
	public class PageCaret
	{
		private float m_posX;
		private float m_posY;
		private float m_lastHeightMod;
		private boolean m_firstLine;
		
		public float getPosX()
		{
			return m_posX;
		}
		
		public float getPosY()
		{
			return m_posY;
		}
		
		public boolean isFirstLine()
		{
			return m_firstLine;
		}
		
		public float getLastHeightMod()
		{
			return m_lastHeightMod;
		}
		
		public void setPosX(float value)
		{
			m_posX = value;
		}
		
		public void setFirstLine(boolean value)
		{
			m_firstLine = value;
		}
		
		public void setPosY(float value)
		{
			m_posY = value;
		}
		
		public void setLastHeightMod(float value)
		{
			m_firstLine = false;
			m_lastHeightMod = value;
		}
		
		public PageCaret(float posX, float posY)
		{
			m_firstLine = true;
			m_posX = posX;
			m_posY = posY;
		}
		
		public void reset()
		{
			m_posY = 0;
			m_posX = 0;
			m_lastHeightMod = 0;
			m_firstLine = true;
		}
	}
	
	public interface IPageSegment
	{
		public boolean calculate(PageCaret caret, int maxHeight);
		
		public void draw(Canvas canvas, float shiftX, PageCaret caret, int pageWidth, int pageHeight, boolean onlyOne);

		public long getPosition();
		
		public void clean();
	}

	public float getAverageLineChars(int minLines)
	{
		int lines = 0;
		int chars = 0;
		for (IPageSegment segment : m_segments)
		{
			if (segment instanceof TextSegment)
			{
				TextSegment textSegment = (TextSegment)segment; 
				if (textSegment.getChars() > 0 && (textSegment.getFlags() & (BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY | BaseBookReader.LINE_BREAK)) == (BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY))
				{
					lines++;
					chars += textSegment.getChars();
				}
			}
		}
		if (lines >= minLines && chars > 0)
			return (float)chars / (float)lines;
		return 0;
	}
	
	public int getAverageLines(int minLines)
	{
		int lines = 0;
		for (IPageSegment segment : m_segments)
		{
			if (segment instanceof TextSegment)
			{
				TextSegment textSegment = (TextSegment)segment; 
				if ((textSegment.getFlags() & (BaseBookReader.NEW_LINE)) != 0)
				{
					lines++;
				}
			}
		}
		if (lines >= minLines)
			return lines;
		return 0;
	}
}
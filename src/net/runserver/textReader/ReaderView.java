package net.runserver.textReader;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.runserver.bookParser.BaseBookReader;
import net.runserver.bookParser.BookLine;
import net.runserver.bookParser.ImageData;
import net.runserver.bookRenderer.FontStyle;
import net.runserver.bookRenderer.TextPage;
import net.runserver.common.BaseActivity;
import net.runserver.common.FixedCharSequence;
import net.runserver.common.NookEInkMode;
import net.runserver.common.NookTouchEPD;
import net.runserver.common.Pair;
import net.runserver.common.SortedList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.SystemClock;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class ReaderView extends View 
{
	private static final String [] s_superscriptNumbers = { " ¹"," ²"," ³", " \u2074"  };
	
	public static final int ORIENTATION_NORMAL = 0;
	public static final int ORIENTATION_CW = 1;
	public static final int ORIENTATION_CCW = 2;
	public static final int ORIENTATION_180 = 3;
	
	public static final int FOOTER_OFF = 0;
	public static final int FOOTER_NORMAL = 1;
	public static final int FOOTER_TICKS = 2;
	
	public static final int REFRESH_NORMAL = 0;
	public static final int REFRESH_G16 = 1;
	
	public static final int REFRESH_A2 = 2;
	
	private static final int FOOTER_HEIGHT = 18;
	
	//private final boolean m_useDirectDraw;
	//private DrawThread m_drawThread;

	private BaseBookReader m_reader;
	private List<TextPage> m_pages;
	private int m_currentPage;
	private long m_bookStart;
	
	private int m_averagePageSize;
	private int m_averagePages;
	private int m_averageTotal;
	private SortedList<Integer> m_averagePageSizes;
	
	private List<Pair<String,Float>> m_chapters;
	private List<ImageData> m_images;
	private Map<String, List<BookLine>> m_notes; 	
	private Map<String, Integer> m_styles;
	

	private FontStyle m_textPaint;
	private FontStyle m_superPaint;
	private FontStyle m_boldPaint;
	private FontStyle m_boldItalicPaint;
	private FontStyle m_italicPaint;
	private FontStyle m_header1Paint;
	private FontStyle m_header2Paint;
	private FontStyle m_header3Paint;
	private FontStyle m_header4Paint;
	private FontStyle m_subtitlePaint;

	private TextPaint m_systemPaint;
	private TextPaint m_pagePaint;
	private TextPaint m_headPaint;
	
	private Paint m_whitePaint;
	private Paint m_lightGrayPaint;
	private Paint m_darkGrayPaint;
	private Paint m_batteryPaint;
	private Paint m_grayPaint;

	private int m_batteryLevel;
	private int m_pageNumber;
	//private Rect m_boundRect;
	private int m_actualWidth;
	private int m_actualHeight;

	// design settings
	private int m_paddingLeft;
	private int m_paddingRight;
	private int m_paddingTop;
	private int m_paddingBottom;
	private int m_header;
	private int m_footer;
	private int m_maxCachedPages;
	private int m_footerSpace;
	private int m_refreshMode;
	
	private boolean m_fullScreen;	
	private int m_orientation = ORIENTATION_NORMAL;
	private boolean m_inverse = false;
	private int m_backColor;
	
	private final float m_dpiCompensation;
	
	private boolean m_pause = false;
	private View m_overlayView = null;
	
	private final Bitmap m_batteryIcon;
	private final Bitmap m_batteryIconInverted;
	private final DateFormat m_dateFormat;
	private int m_clockWidth;
	
	private CharSequence m_footerTextLeft;
	private int m_footerTextOffset;
		
	private final BroadcastReceiver m_batteryLevelReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			int level = -1;
			if (rawlevel >= 0 && scale > 0)
			{
				level = (int) Math.round((rawlevel * 100) / (float) scale);
			}
			if (level > 100)
				level = 100;
			if (level < 0)
				level = 0;
			
			if (level != m_batteryLevel)
			{
				m_batteryLevel = level;				
				invalidateHeader();
			}
		}
	};
	
	private final BroadcastReceiver m_timeReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			invalidateHeader();			
		}
	};
	
	public void setOverlayView(View value)
	{
		if (value == null && m_overlayView != null)
		{
			int location[] = new int[4];
			m_overlayView.getLocationOnScreen(location);
			location[0] += m_overlayView.getPaddingLeft(); 
			location[1] += m_overlayView.getPaddingTop(); 
			location[2] = location[0] + m_overlayView.getWidth() - m_overlayView.getPaddingRight(); 
			location[3] = location[1] + m_overlayView.getHeight() - m_overlayView.getPaddingBottom(); 
			invalidate(new Rect(location[0], location[1], location[2], location[3]));
		}
		
		m_overlayView = value;
	}
	
	public List<Pair<String,Float>> getChapters()
	{
		return m_chapters;
	}
	
	public ReaderView(Context context, ViewGroup container, /*boolean useDirectDraw, */float dpiCompensation)
	{
		super(context);
		setFocusable(false);
		//m_useDirectDraw = useDirectDraw;
		m_dpiCompensation = dpiCompensation;
		
		//if (useDirectDraw)
			//m_fullScreen = true;
		//else
			m_fullScreen = true; // TODO: check fullscreen mode

		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));		

		container.addView(this);
		
		m_actualWidth = getWidth();
		m_actualHeight = getHeight();
		
		m_maxCachedPages = BaseActivity.isNook || BaseActivity.isEpad ? 10 : 15;
		m_paddingLeft = 5;
		m_paddingRight = 5;
		m_paddingTop = 5;
		m_paddingBottom = 5;
		
		m_header = 33;
		m_clockWidth = 100;
		m_footerSpace = 3;
		
		m_dateFormat = android.text.format.DateFormat.getTimeFormat(getContext());
		
		m_footer = FOOTER_TICKS;

		m_pageNumber = 0;
		
		m_batteryIcon = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.battery));
		m_batteryIconInverted = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.battery_inv));
		
		m_averagePageSizes = new SortedList<Integer>();
		/*
		if (!m_useDirectDraw)
			m_drawThread = new DrawThread(holder, this);*/
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		//m_boundRect = new Rect(m_paddingWidth, m_paddingTop, w - m_paddingWidth, h - m_paddingBottom);
		
		if (m_dpiCompensation != 1f)
		{
			if (w > h)
				w = (int)(w / m_dpiCompensation);  // landscape
			else
				h = (int)(h / m_dpiCompensation);  // portrait			
		}

		if ((m_actualHeight != h || m_actualWidth != w) && m_currentPage != -1)
		{
			m_actualWidth = w;
			m_actualHeight = h;
			reset();
		}
		else
		{		
			m_actualWidth = w;
			m_actualHeight = h;		
			if (m_currentPage == -1)
				nextPage(true, true);
		}
	};

	public void init(BaseBookReader reader, long position, int page, List<Pair<String,Float>> chapters, Map<String,Integer> styles, List<ImageData> images, Map<String, List<BookLine>> notes)
	{
		if (reader == null)
		{
			clear();
			return;
		}
		m_chapters = chapters;
		m_images = images;
		m_styles = styles;
		m_reader = reader;
		m_notes = notes;
		m_bookStart = m_reader.getPosition();
		m_reader.reset(position);
		m_footerTextLeft = null;

		int offset = m_reader.getOffset();
		if (offset < 10)
			m_reader.setOffset(0);
			
		resetPages(false);
		m_pageNumber = page;
		nextPage(true, false);
	}
	
	public void clear()
	{
		m_chapters = null;
		m_images = null;
		m_styles = null;
		m_reader = null;
		m_notes = null;
		m_bookStart = 0;
		resetPages(false);
		m_pageNumber = 0;	
		//invalidate();
	}

	private void resetPages(boolean clearAverage)
	{
		if (m_pages != null)
			for (int i = 0; i < m_pages.size(); i++)
			{
				TextPage page = m_pages.get(i);
				if (page != null)
					page.clean();
			}
		
		m_pages = new LinkedList<TextPage>();
		m_currentPage = -1;
		
		if (clearAverage)
		{
			m_averagePages = 0;
			m_averagePageSize = 0;
			m_averageTotal = 0;
			m_averagePageSizes.clear();
		}
	}
		
	
	public void changeSettings(float extraStroke, int orientation, int header, int footer, boolean inverse, int textColor, int backColor, int refreshMode, boolean reset)
	{		
		if (reset)
			reset();
		
		m_inverse = inverse;
		
		if (refreshMode != m_refreshMode)
		{		
			m_refreshMode = refreshMode;
			updateRefresh();
		}
		
		if (m_textPaint != null)
		{	
			m_textPaint.changeContrast(extraStroke);
			m_superPaint.changeContrast(extraStroke);
			m_boldPaint.changeContrast(extraStroke);
			m_boldItalicPaint.changeContrast(extraStroke);
			m_italicPaint.changeContrast(extraStroke);
			m_header1Paint.changeContrast(extraStroke);
			m_header2Paint.changeContrast(extraStroke);
			m_header3Paint.changeContrast(extraStroke);
			m_header4Paint.changeContrast(extraStroke);
			m_subtitlePaint.changeContrast(extraStroke);
			
			int ntextColor = m_inverse ? backColor : textColor;
			
			m_textPaint.getPaint().setColor(ntextColor);
			m_superPaint.getPaint().setColor(ntextColor);
			m_boldPaint.getPaint().setColor(ntextColor);
			m_boldItalicPaint.getPaint().setColor(ntextColor);
			m_italicPaint.getPaint().setColor(ntextColor);
			m_header1Paint.getPaint().setColor(ntextColor);
			m_header2Paint.getPaint().setColor(ntextColor);
			m_header3Paint.getPaint().setColor(ntextColor);
			m_header4Paint.getPaint().setColor(ntextColor);
			m_subtitlePaint.getPaint().setColor(ntextColor);
			m_pagePaint.setColor(ntextColor);
		}
		m_backColor = m_inverse ? textColor : backColor;
		
		m_footer = footer;
		m_header = header;
		m_orientation = orientation; 
		
		if (m_headPaint != null && m_header > 0)
		{
			m_headPaint.setTextSize((int)(m_header / 1.8f));
			m_systemPaint.setTextSize((int)(m_header / 1.8f));
		}
		
		if (m_dateFormat != null && m_systemPaint != null)
			m_clockWidth = (int)m_systemPaint.measureText(m_dateFormat.format(new Date(2000, 1, 1, 12, 38)));
	}	
	
	public void reset()
	{
		if (m_reader != null)
		{
			if (m_pages != null && m_pages.size() > 0 && m_currentPage >= 0)
			{
				TextPage currentPage = m_pages.get(m_currentPage);
				m_reader.reset(currentPage.getStartPosition());
				m_footerTextLeft = null;
				m_pageNumber = currentPage.getPageNumber() - 1;
			}
		}
		resetPages(true);
	}
	
	private void updateRefresh()
	{
		switch(m_refreshMode)
		{	
			case REFRESH_A2:
				NookTouchEPD.setMode("APP_3", "A2", "ACTIVE", this);
				break;
			case REFRESH_G16:
				NookTouchEPD.setMode("APP_3", "GL16", "ACTIVE", this);
				break;
			default:
				NookTouchEPD.setMode("APP_3", "AUTO", "ACTIVE", this);
				break;
		}
	}

	public void initFonts(int size, Typeface normalFont, Typeface boldFont, Typeface italicFont, Typeface boldItalicFont, int orientation, float extraStroke, boolean inverse, int textColor, int backColor, float lineSpace, float firstLine, int header, int footer, int refreshMode, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom)
	{
		//Log.d("TextReader", "Called initFonts, reset " + reset + ", pages " + ((m_pages != null && m_pages.size() > 0 ? m_pages.size() : 0)));
		reset();
		
		m_paddingLeft = paddingLeft;
		m_paddingRight = paddingRight;
		m_paddingTop = paddingTop;
		m_paddingBottom = paddingBottom;

		if (refreshMode != m_refreshMode)
		{		
			m_refreshMode = refreshMode;
			updateRefresh();
		}
		
		m_footer = footer;
		m_header = header;
		//m_paddingBottom = footer == FOOTER_OFF ? 8 : 22;
		
		m_backColor = inverse ? textColor : backColor;

		TextPaint textPaint = new TextPaint();
		textPaint.setDither(false);
		textPaint.setLinearText(true);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(size);
		textPaint.setTypeface(normalFont);
		textPaint.setColor(inverse ? backColor : textColor);
		
		if (extraStroke > 0)
		{
			textPaint.setStrokeWidth(extraStroke);
			textPaint.setStyle(Style.FILL_AND_STROKE);
		} else
			textPaint.setStyle(Style.FILL);
		
		/*if (m_useDirectDraw)
		{
			textPaint.setAlpha(0);
			textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		}*/
		TextPaint superPaint = new TextPaint(textPaint);
		superPaint.setTextSize(size  * 3/4);		

		TextPaint italicPaint = new TextPaint(textPaint);
		italicPaint.setTypeface(italicFont);

		TextPaint boldPaint = new TextPaint(textPaint);
		boldPaint.setTypeface(boldFont);
		if (boldFont == normalFont || !boldFont.isBold())
			boldPaint.setFakeBoldText(true);

		TextPaint boldItalicPaint = new TextPaint(textPaint);
		boldItalicPaint.setTypeface(boldItalicFont);
		if (boldItalicFont == italicFont || !boldItalicFont.isBold())
			boldItalicPaint.setFakeBoldText(true);

		TextPaint header1Paint = new TextPaint(boldPaint);
		header1Paint.setTextSize(size + 8);

		TextPaint header2Paint = new TextPaint(boldPaint);
		header2Paint.setTextSize(size + 6);

		TextPaint header3Paint = new TextPaint(boldPaint);
		header3Paint.setTextSize(size + 4);

		TextPaint header4Paint = new TextPaint(textPaint);
		header4Paint.setTextSize(size + 2);

		TextPaint subtitlePaint = new TextPaint(italicPaint);
		subtitlePaint.setTextSize(size + 1);

		if (m_systemPaint == null || m_inverse != inverse)
		{
			m_systemPaint = new TextPaint();
			m_systemPaint.setTextSize(16);
			m_systemPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
			m_systemPaint.setLinearText(true);
			m_systemPaint.setAntiAlias(true);
			m_systemPaint.setTextAlign(Align.RIGHT);
			m_systemPaint.setColor(inverse ? Color.WHITE : Color.BLACK);
			
			m_pagePaint = new TextPaint(m_systemPaint);
			m_pagePaint.setTextSize(14);
			m_pagePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
			m_pagePaint.setColor(textColor);

			/*if (m_useDirectDraw)
			{
				m_systemPaint.setAlpha(0);
				m_systemPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			}*/

			m_darkGrayPaint = new Paint(m_systemPaint);
			m_darkGrayPaint.setColor(0x555555);
			/*if (m_useDirectDraw)
				m_darkGrayPaint.setAlpha(0x55);
			else*/
				m_darkGrayPaint.setAlpha(0xFF);
			
			m_whitePaint = new Paint(m_systemPaint);
			m_whitePaint.setColor(inverse ? Color.BLACK : Color.WHITE);

			m_lightGrayPaint = new Paint(m_systemPaint);
			m_lightGrayPaint.setColor(0xAAAAAA);
			/*if (m_useDirectDraw)
				m_lightGrayPaint.setAlpha(0xAA);
			else*/
				m_lightGrayPaint.setAlpha(0xFF);
			
			m_grayPaint = new Paint(m_systemPaint);
			m_grayPaint.setColor(0x808080);
			/*if (m_useDirectDraw)
				m_grayPaint.setAlpha(0x80);
			else*/
				m_grayPaint.setAlpha(0xFF);

			m_headPaint = new TextPaint(m_systemPaint);
			m_headPaint.setTextAlign(Align.LEFT);
			m_headPaint.setTextSize(18);
			m_headPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));			
			m_headPaint.setAlpha(255);
			
			m_batteryPaint = new TextPaint(m_systemPaint);
			m_batteryPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));	
			m_batteryPaint.setTextSize(14);
		}

		m_textPaint = new FontStyle(textPaint, 0.0f, lineSpace, firstLine);
		m_superPaint = new FontStyle(superPaint, 0.0f, lineSpace, firstLine);
		m_boldPaint = new FontStyle(boldPaint, 0.0f, lineSpace, firstLine);
		m_boldItalicPaint = new FontStyle(boldItalicPaint, 0.0f, lineSpace, firstLine);
		m_italicPaint = new FontStyle(italicPaint, 0.0f, lineSpace + 0.05f, firstLine);
		m_header1Paint = new FontStyle(header1Paint, 12.0f, lineSpace + 0.05f, 65.0f);
		m_header2Paint = new FontStyle(header2Paint, 8.0f, lineSpace + 0.05f, 60.0f);
		m_header3Paint = new FontStyle(header3Paint, 4.0f, lineSpace + 0.05f, 55.0f);
		m_header4Paint = new FontStyle(header4Paint, 2.0f, lineSpace + 0.05f, 50.0f);
		m_subtitlePaint = new FontStyle(subtitlePaint, 0.0f, lineSpace + 0.05f, firstLine);
		
		m_inverse = inverse;
		m_orientation = orientation; 
		
		if (m_header > 0)
		{
			m_headPaint.setTextSize((int)(m_header / 1.8f));
			m_systemPaint.setTextSize((int)(m_header / 1.8f));
		}
		
		m_clockWidth = (int)m_systemPaint.measureText(m_dateFormat.format(new Date(2000, 1, 1, 12, 38)));
		
//		if (BaseActivity.DisplayMetrics.widthPixels <= 320)
//			m_clockWidth -= 15; // no need for extra space
	}
	
	private void applyOrientation(Canvas canvas)
	{	
		if (m_dpiCompensation != 1.0f)
		{
			if (m_actualWidth > m_actualHeight)
				canvas.scale(m_dpiCompensation, 1.0f);
			else
				canvas.scale(1.0f, m_dpiCompensation);
		}
		
		switch(m_orientation)
		{
			case ORIENTATION_180:
				canvas.rotate(180);
				canvas.translate(-m_actualWidth, -m_actualHeight);
				break;
			case ORIENTATION_CW:
				canvas.rotate(90);
				canvas.translate(0, -m_actualWidth);
				break;
			case ORIENTATION_CCW:
				canvas.rotate(-90);
				canvas.translate(-m_actualHeight, 0);
				break;
		}
	}
	
	/*private void drawPage(TextPage page, Bitmap bitmap)
	{
		Canvas canvas = new Canvas(bitmap);		
		canvas.drawColor(m_inverse ? Color.BLACK : Color.WHITE, PorterDuff.Mode.SRC);
		
		canvas.save();
		applyOrientation(canvas);
		
		page.draw(canvas, m_paddingWidth, m_fullScreen ? m_paddingTop + m_fullScreenHeader : m_paddingTop);
		drawInfo(canvas, page);
		
		canvas.restore();
		
		page.setBitmap(bitmap);				
	}*/

	public void doInvalidate()
	{
		if (m_textPaint == null)
			return;

		if (m_pages == null || m_pages.size() == 0 || m_currentPage == -1)
			nextPage(false, true);

		if (m_pages != null && m_pages.size() > 0)
		{
			//TextPage page = m_pages.get(m_currentPage);

			/*if (m_useDirectDraw)
			{
				//long start = SystemClock.elapsedRealtime();

				Bitmap bitmap = page.getBitmap();
				if (bitmap == null || bitmap.isRecycled())
				{
					try
					{
						bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ALPHA_8);
					}
					catch (OutOfMemoryError ex)
					{
						for (int i = 0; i < m_pages.size(); i++)
						{
							TextPage npage = m_pages.get(i);
							if (npage != null && npage.getBitmap() != null)
								npage.getBitmap().recycle();
						}
						bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ALPHA_8);
					}
					drawPage(page, bitmap);
				} else
				{
					Canvas canvas = new Canvas(bitmap);
					canvas.save();
					
					applyOrientation(canvas);
					
					updateInfo(canvas, page);
					canvas.restore();
				}

				bitmap.copyPixelsToBuffer(DirectDraw.getBuffer());

				DirectDraw.updateBuffer();
				
				//Log.d("TextReader", "Page render took " + (SystemClock.elapsedRealtime() - start));
			} else*/
				//m_drawThread.setDirtyFlag(true);
				invalidate();

			//m_pageNumber = page.getPageNumber();
		}
	}
	
	private void updateHeader(Canvas canvas)
	{		
		if (m_header > 0)
		{
			int width = m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180 ? m_actualWidth : m_actualHeight;
			canvas.drawRect(new Rect(width - 67 - m_header, 0, width, m_header - 2), m_whitePaint);
			
			Bitmap icon = m_inverse ? m_batteryIconInverted : m_batteryIcon;

			//int baseline = (int)(m_header + m_batteryPaint.getTextSize())/2;//(int)(m_header - (m_header + m_systemPaint.getTextSize())/5);
			int batteryPos = width - m_clockWidth - m_header - icon.getWidth() + 5;
			
			Rect pos = new Rect(batteryPos - 3, (m_header - icon.getHeight()) / 2, batteryPos - 3 + icon.getWidth(), (m_header + icon.getHeight()) / 2);
			canvas.drawBitmap(icon, null, pos,  m_systemPaint);
			canvas.drawRect(new Rect(batteryPos + 2 + (100-m_batteryLevel)*23/100, pos.top + 2, batteryPos + 26, pos.bottom - 2), m_inverse ? m_darkGrayPaint : m_lightGrayPaint);
			if (m_batteryLevel == 100)
				canvas.drawText(Integer.toString(m_batteryLevel), batteryPos + 26, pos.bottom - 3, m_batteryPaint);
			else
				canvas.drawText(Integer.toString(m_batteryLevel) + "%", batteryPos + 26, pos.bottom - 3, m_batteryPaint);
			
			canvas.drawText(m_dateFormat.format(java.util.Calendar.getInstance().getTime()), width - 13, (m_header + m_systemPaint.getTextSize())/2.25f, m_systemPaint);
		}
	}


	private void drawInfo(Canvas canvas, TextPage page)
	{
		float percent = m_reader.getPercent(page.getEndPosition());		
		
		int height;
		int width;
		
		if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180)
		{
			width = m_actualWidth;
			height = m_actualHeight;			
		} else
		{
			width = m_actualHeight;
			height = m_actualWidth;
		}

		if (m_fullScreen && m_header > 0)
		{
			canvas.drawRect(new Rect(0, 0, width, m_header), m_whitePaint);			
			updateHeader(canvas);
			
			String ntitle = m_reader.getTitle().trim();
			if (ntitle.length() > 0 && ntitle.charAt(ntitle.length() - 1) == ',')
				ntitle = ntitle.substring(0, ntitle.length() - 1);
			
			StringBuilder title = new StringBuilder(100);
			title.append(ntitle);
			String chapter = null;
			
			float spercent = m_reader.getPercent(page.getStartPosition());
			
			for(int i=0;i<m_chapters.size();i++)
			{
				if (spercent < m_chapters.get(i).second)
				{
					chapter = m_chapters.get(i == 0 ? 0 : i - 1).first.trim();
					if (chapter.length() > 0 && chapter.charAt(chapter.length() - 1) == ',')
						chapter = chapter.substring(0, chapter.length() - 1);
					break;
				}	
			}
			
			if (chapter != null && !chapter.equals(ntitle) && !chapter.equals("Title Page"))
			{
				if (title.length() > 0)
				{
					char last = title.charAt(title.length() - 1);
					if (Character.isLetter(last) || Character.isDigit(last))
						title.append('.');
					title.append(' ');
				}
				title.append(chapter);
			}
			
			StaticLayout layout = new StaticLayout(title.toString(), m_headPaint, width - m_clockWidth - m_header - (m_inverse ? m_batteryIconInverted : m_batteryIcon).getWidth() - 20, android.text.Layout.Alignment.ALIGN_NORMAL, 100.0f,  0.0f, true);			

			String rtitle = layout.getLineCount() == 1 ? title.toString() : title.subSequence(0, layout.getLineEnd(0)).toString().trim() + "…";
			
			if (!BaseActivity.isBiDirStringSupported && FontStyle.isRTL(rtitle))
				rtitle = FontStyle.reverseString(rtitle);
			
			canvas.drawText(rtitle, 15, m_header - (m_header + m_headPaint.getTextSize())/5, m_headPaint);
			
			canvas.drawRect(0, m_header - 1, width, m_header, m_systemPaint);
		}
		
		if (m_footer != FOOTER_OFF)
		{
			int shift = height - FOOTER_HEIGHT;
			int barWidth = width - 105;
			int barLeft = 20;
	
			canvas.drawRect(new Rect(barLeft + barWidth, shift + 5, barLeft + barWidth + 1, shift + 15), m_lightGrayPaint);
			canvas.drawRect(new Rect(barLeft, shift + 4, barLeft + barWidth, shift + 16), m_lightGrayPaint);
			canvas.drawRect(new Rect(barLeft - 1, shift + 5, barLeft, shift + 15), m_darkGrayPaint);
			int progressX = barLeft + (int) (barWidth * percent / 100f);
			canvas.drawRect(new Rect(barLeft, shift + 4, progressX, shift + 16), m_darkGrayPaint);		
			
			if (m_footer == FOOTER_TICKS)
			{			
				int lastx = 0;
				for(int i=0;i<m_chapters.size();i++)
				{
					float chapterPercent = m_chapters.get(i).second;
					int x = barLeft + (int) (barWidth * chapterPercent / 100f);
					
					if (x == lastx)
						continue;
					
					lastx = x;
					if (TextReader.isNook)
					{
						if (chapterPercent >= percent)
						{
							canvas.drawLine(x, shift + 4, x, shift + 16, m_systemPaint);
						}
					} else
					{
						if (chapterPercent >= percent)
							canvas.drawLine(x, shift + 4, x, shift + 16, m_systemPaint);
						else
							canvas.drawLine(x, shift + 5, x, shift + 15, m_grayPaint);
					}
				}
			}
	
			/*int nchapter = (int) (page.getStreamPosition() >> 32);
			if (nchapter < 1)
				nchapter = 1;*/
			
			int pageNumber = page.getPageNumber();
			//if (m_averageTotal == 0)				
				//canvas.drawText("page " + pageNumber, width - 20, shift + 13, m_pagePaint);
			//else
			{
				//if (m_averageTotal == 0)
					//m_averageTotal = (int)Math.round(100 * pageNumber / percent);
				
				int total = m_averageTotal;
				
				if (total <= 0 && m_reader != null)
				{
					int pageLines = (int)(height /  (m_textPaint.getHeight() + m_textPaint.getHeightMod()) + 1);
					int lineChars = (int)(width / m_textPaint.getDashWidth());
					total = (int)m_reader.getMaxSize() / (pageLines * lineChars) + m_chapters.size();				
				}

				if (pageNumber > total || percent >= 100)
					total = pageNumber;
				else
				if (pageNumber == total && percent < 100)
					total = pageNumber + 1;
				
				canvas.drawText("" + pageNumber + "|" + total, width - 12, shift + 14, m_pagePaint);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		/*if (m_useDirectDraw)
		{
			//super.onDraw(canvas);
			DirectDraw.invalidate();
			return;
		}*/
		
		if (m_reader == null)
		{			
			TextPaint tempPaint = new TextPaint();
			tempPaint.setAntiAlias(true);
			tempPaint.setTextSize(getWidth() < 300 ? 26 : 32);
			tempPaint.setTypeface(Typeface.SERIF);
			tempPaint.setTextAlign(Align.CENTER);
			//canvas.save();
			//applyOrientation(canvas);
			canvas.drawText(getResources().getString(R.string.loading) , getWidth()/2, getHeight()/2, tempPaint);
			
			tempPaint.setTextAlign(Align.RIGHT);
			tempPaint.setTextSize(getWidth() < 300 ? 22: 26);
			
			try
			{
			    String version = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
				canvas.drawText("Nomad Reader v" + version, getWidth() - 5, getHeight() - 5, tempPaint);
			}
			catch (NameNotFoundException ex)
			{
				Log.d("TextReader", "Failed to get own version number");
			}
			
			//updateInfo
			
			//canvas.restore();
			return;
		}
		
		doDraw(canvas);
	}
	
	protected void doDraw(Canvas canvas)
	{
		/*
		 * if (m_cache != null) { super.onDraw(canvas); return; }
		 */


		// if (clip.intersect(new Rect(m_paddingWidth, m_paddingHeight,
		// getWidth() - m_paddingWidth, getHeight() - m_paddingHeight)))
		// canvas.drawColor(Color.WHITE);
		if (m_currentPage == -1)
			nextPage(true, true);

		if (m_pages != null && m_pages.size() > 0 && m_currentPage != -1)
		{			

			/*if (clip.right > m_boundRect.left && clip.left <= m_boundRect.right && clip.bottom > m_boundRect.top
					&& clip.top <= m_boundRect.bottom)*/
			{
				
				if (m_overlayView != null)
				{
					Rect clip = canvas.getClipBounds();
					
					int location[] = new int[4];
					m_overlayView.getLocationOnScreen(location);
					location[0] += m_overlayView.getPaddingLeft(); 
					location[1] += m_overlayView.getPaddingTop(); 
					location[2] = location[0] + m_overlayView.getWidth() - m_overlayView.getPaddingRight(); 
					location[3] = location[1] + m_overlayView.getHeight() - m_overlayView.getPaddingBottom(); 
					
					if (clip.left >= location[0] && clip.top >= location[1] && clip.right <= location[2] && clip.left <= location[3] )
					{
						//Log.d("TextReader", "Skipping overlay update in " + clip);
						return;
					}
				}
				
				//Log.d("TextReader", "Redraw, clip rect is " + canvas.getClipBounds());
				
				canvas.drawColor(m_backColor, PorterDuff.Mode.SRC);
				
				TextPage page = m_pages.get(m_currentPage);
				
				if (BaseActivity.isNook)
				{
					if (page.haveImages())
						NookEInkMode.setFourBitMode();
					else
						NookEInkMode.setTwoBitMode();
				}

				long start = SystemClock.elapsedRealtime();

				canvas.save();
				
				applyOrientation(canvas);
				
				Rect clip = canvas.getClipBounds();
				
				//Log.d("TextReader", "Clip is " + clip);
				
				int top = m_paddingTop + (m_fullScreen ? m_header : 0);
				int bottom = (m_orientation == ORIENTATION_180 || m_orientation == ORIENTATION_NORMAL ? m_actualHeight : m_actualWidth) - m_paddingBottom - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0);
				
				if (clip.bottom > top && clip.top < bottom)				
					page.draw(canvas, m_paddingLeft, top, m_pagePaint);
				/*else
					Log.d("TextReader", "Skipping page draw cause of partial update, clip " + clip + ", top " + top + ", bottom " + bottom);*/
				
				drawInfo(canvas, page);
				
				canvas.restore();
				
				//if (BaseActivity.isNookTouch)
					//NookTouchEPD.refresh();
				
				long elapsed = (SystemClock.elapsedRealtime() - start);
				if (elapsed > 400)
					Log.d("TextReader", "Page draw took " + elapsed);
			} //else
			//{
				//Log.d("TextReader", "Clip rect miss  " + canvas.getClipBounds());
			//}
		}		
	}

	private TextPage preparePage(int width, int height, int virtualHeight, long stopAt, int pageNumber)
	{
		//m_reader.init();
		 /*Log.d("TextReader", "Preparing page " + m_pageNumber +
		 ", reader position " + (int)position + ", reader " + (int)(position
		 >> 32));*/

		TextPage page = new TextPage(width, virtualHeight == 0 ? height : virtualHeight, m_footerSpace, pageNumber);

		int lastFlags = 0;
		int lastStyleFlags = 0;
		int notesCount = 0;
		boolean footerBreak = false;

		while (!m_reader.isFinished())
		{
			int flags;
			int offset;
			long position;
			CharSequence nchars;
			
			if (!footerBreak && m_footerTextLeft != null)
			{
				flags = BaseBookReader.FOOTER | BaseBookReader.LINE_BREAK | BaseBookReader.JUSTIFY;
				nchars = m_footerTextLeft;
				offset = m_footerTextOffset;
				position = 0;
			} else
			{			
				flags = m_reader.getFlags();
	
				if (!page.isEmpty() && (flags & BaseBookReader.NEW_PAGE) != 0)
				{
					//Log.d("TextReader", "Breaking page");
					break;
				}
				
				offset = m_reader.getOffset();
				position = m_reader.getPosition();			
				
				if (stopAt > 0 && position + offset >= stopAt)
				{
					page.finish(TextPage.PAGE_FULL, height, m_paddingTop + (m_fullScreen ? m_header : 0));
					return page;
				}
							
				nchars = m_reader.getText();	
				flags = m_reader.getFlags();
			}
			
			//ByteCharSequence chars = (ByteCharSequence)m_reader.getText();
			
			//nchars = nchars.toString();
			
			if ((flags & BaseBookReader.LINK) != 0)
			{
				//String linkTitle = m_reader.getLinkTitle();				
				String href = m_reader.getLinkHRef();

				//Log.d("TextReader", "Got link href " + href);
				if (href != null)
				{
					if(href.startsWith("#") && m_notes.containsKey(href.substring(1)))
					{
						Log.d("TextReader", "Time to insert note " + href);
						
						String noteSymbol;
						
						if (notesCount >= s_superscriptNumbers.length)
						{
							noteSymbol = " *";
							for(int h=0;h<notesCount - s_superscriptNumbers.length;h++)
								noteSymbol += "*";
						} else
							noteSymbol = s_superscriptNumbers[notesCount];
						
						notesCount++;
						
						StringBuilder noteText = new StringBuilder();
						noteText.append(noteSymbol);
						for(BookLine line:m_notes.get(href.substring(1)))
							if (line.getText() != null)
							{
								noteText.append(" ");
								noteText.append(line.getText().toString());
							}
						nchars = noteText.toString();
						
						page.addNonBreakText(noteSymbol);
						flags = BaseBookReader.FOOTER | BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY;
						lastFlags |= BaseBookReader.WORD_BREAK;
						offset = 0;
					} else
					{
						String linkTitle = m_reader.getLinkTitle();
						
						if (linkTitle != null && linkTitle.length() > 2 && href != null && href.contains("#") && nchars.length() < 10)
						{
							String noteSymbol;
							
							if (notesCount >= s_superscriptNumbers.length)
							{
								noteSymbol = " *";
								for(int h=0;h<notesCount - s_superscriptNumbers.length;h++)
									noteSymbol += "*";
							} else
								noteSymbol = s_superscriptNumbers[notesCount];
							
							notesCount++;
							
							StringBuilder noteText = new StringBuilder(noteSymbol.length() + linkTitle.length() + 1);
							noteText.append(noteSymbol);
							noteText.append(" ");
							noteText.append(linkTitle);
							
							nchars = noteText.toString();
							
							page.addNonBreakText(noteSymbol);
							
							flags = BaseBookReader.FOOTER | BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY;
							lastFlags |= BaseBookReader.WORD_BREAK;
							offset = 0;						
						}
					}
				} else
				{
					if ((flags & BaseBookReader.SUPER) != 0) // remove book's own footnotes
					{
						m_reader.advance();
						continue;	
					}
						
					flags = lastFlags;
				}
					//&= ~BaseBookReader.LINK;
				//	flags |= lastFlags & BaseBookReader.STYLE;*/
				
				//page.addLink(linkTitle, nchars, m_textPaint, position);
				//m_reader.advance();
				//continue;
			}

			if ((flags & BaseBookReader.IMAGE) != 0)
			{
				String imageSrc = m_reader.getImageSrc();
				Log.d("TextReader", "Processing image " + imageSrc);
				
				boolean added = false;
				
				if (m_images != null)
				{
					for(int i=0;i<m_images.size();i++)
					{
						if (m_images.get(i).getName().equals(imageSrc))
						{
							if (!page.addImage(m_images.get(i), nchars, m_textPaint, position + offset))
							{
								page.finish(TextPage.PAGE_FULL, height, m_paddingTop + (m_fullScreen ? m_header : 0));
								return page;
							}
							added = true;
							break;
						}
					}
				}
				
				if (!added)
				{
					Log.d("TextReader", "Failed to add image " + imageSrc);
				} else
				{
					Log.d("TextReader", "Added image " + imageSrc);				
					m_reader.advance();
					continue;
				}
			}			
			
			//if (nchars != null && nchars.equals("\u00a0\0")) //nbsp;
			//{
				//page.addLineBreak(m_textPaint);
				//flags |= BaseBookReader.NEW_LINE; // sanity check
				//m_reader.advance();
				//continue;
			//}
			
			if (nchars == null  || nchars.length() == 0)
				break;
			
			String [] classes = m_reader.getClassNames();
			
			//Log.d("TextReader", "Got text flag " + flags + ", value " + nchars);
			//if ((flags & BaseBookReader.RTL) != 0)
				//Log.d("TextReader", "Got RTL text " + flags + ", value " + nchars);

			if (classes != null)
				for(int l=0;l<classes.length;l++)
				{
					Integer istyle = m_styles.get(classes[l]);
					if (istyle != null)
						flags |= istyle; 
				}

			FontStyle style = m_textPaint;

			if ((flags & BaseBookReader.HEADER_4) != 0)
			{
				style = m_header4Paint;
			} else if ((flags & BaseBookReader.FOOTER) != 0)
			{
				style = m_superPaint;
			} else if ((flags & BaseBookReader.HEADER_3) != 0)
			{
				style = m_header3Paint;
			} else if ((flags & BaseBookReader.HEADER_2) != 0)
			{
				style = m_header2Paint;
			} else if ((flags & BaseBookReader.HEADER_1) != 0)
			{
				style = m_header1Paint;
			} else if ((flags & BaseBookReader.SUBTITLE) != 0)
			{
				style = m_subtitlePaint;
			} else if ((flags & BaseBookReader.BOLD) != 0)
			{
				if ((flags & BaseBookReader.ITALIC) != 0)
					style = m_boldItalicPaint;
				else
					style = m_boldPaint;
			} else if ((flags & BaseBookReader.ITALIC) != 0)
				style = m_italicPaint;
			
			//if ( lastStyle != null && lastStyle.getHeight() != style.getHeight())
			//if (((lastFlags & BaseBookReader.HEADER) != 0) != ((flags & BaseBookReader.HEADER) != 0) || ((lastFlags & BaseBookReader.SUBTITLE) != 0) != ((flags & BaseBookReader.SUBTITLE) != 0))
			
			if (lastStyleFlags != 0 && (lastStyleFlags & BaseBookReader.STYLE) != (flags & BaseBookReader.STYLE))
				flags |= BaseBookReader.NEW_LINE;

			if ((flags & BaseBookReader.NEW_LINE) != 0 && offset > 0)
				flags &= ~BaseBookReader.NEW_LINE;
				
			//long start = System.currentTimeMillis();
			
			FixedCharSequence text = FixedCharSequence.toFixedCharSequence(nchars);
			int lastChar = text.length() - 1;
			
			boolean lineStart = (flags & BaseBookReader.NEW_LINE) != 0;
			boolean hasTab = false;
			int wordLength = 0;
			int wordStart = offset;
			
			for (int i = offset; i <= lastChar; i++)
			{
				char ch = text.charAt(i);
				boolean separator = false;
				boolean dash = false;
				//boolean lineBreak = false;

				switch (ch)
				{
					case ' ':
						if (i > 0 && lineStart && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–'))
						{
							text.setChar(i, '\u00a0'); // set nbsp
							break;
						}
						
						separator = true;
						break;
					case '\t':
						if (lineStart)
						{
							//text.setChar(i, '\0');
							if (!m_textPaint.hasTab())
								text.setChar(i, ' ');
							hasTab = true;
							break;
						}
						separator = true;
						break;
					case '\n':
					case '\r':
					case '\0':
						text.setChar(i, ' ');
						if (lineStart)
						{
							break;
						}
						separator = true;
						//lineBreak = true;
						break;
					case '-':
					case '\'':
						if (m_reader.isDirty())
						{
							if ((i > 0 && text.charAt(i - 1) == ' ') || (i != lastChar && text.charAt(i+1) == ' '))
							{
								text.setChar(i, '—');
							} else
							{
								if ((i != lastChar && text.charAt(i+1) == '-'))
								{
									text.setChar(i, ' ');
								} else
								{
									dash = true;
									separator = true;
								}
							}
						} else
						{
							if ((i > 0 && text.charAt(i - 1) == ' ') || (i != lastChar && text.charAt(i+1) == ' '))
							{
								
							} else
							{
								dash = true;
								separator = true;
							}
						}
						break;
					case '\u00a0': // &nbsp;
						if (lineStart)
						{
							break;
						}
						
						if (i > 0 && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–'))
							break;

						separator = true;
						break;
					case '—':
					case '–':
						break;
					default:
						lineStart = false;
						if (i != lastChar)
							continue;
						break;						
				}
				

				if (separator || i == lastChar)
				{
					lineStart = false;
					wordLength = i - wordStart;
					if (/*!lineBreak && */(dash || (!separator && i == lastChar)))
						wordLength++;

					if (wordLength > 0/* || lineStart*/)
					{
						FixedCharSequence word;
						
						/*if (wordLength == 0)
						{
							wordLength = 1;
							word = FixedCharSequence.toFixedCharSequence(" ");
							//slineStart = false;
						} else*/
							word = (FixedCharSequence)text.subSequence(wordStart, wordStart + wordLength);//.toString();					
						
						if (hasTab && m_textPaint.getFirstLine() != 0)
						{
							String str = word.toString();
							if (str.contains(m_textPaint.hasTab() ? "\t  " : "   "))
							{
								m_textPaint.setFirstLine(0);
								m_italicPaint.setFirstLine(0);
								m_boldPaint.setFirstLine(0);
								m_boldItalicPaint.setFirstLine(0);
								m_subtitlePaint.setFirstLine(0);
								//m_superPaint.setFirstLine(0);
							}
							word = new FixedCharSequence(str.replace("\t", "      "));
						}

						if ((flags & BaseBookReader.LINE_BREAK) != 0 || (lastFlags & BaseBookReader.LINE_BREAK) != 0)
						{
							/*if ((flags & BaseBookReader.FOOTER) != 0 && m_footerTextLeft != null)
							{
								m_footerTextLeft = null;
							} else*/
							{
								if ((flags & BaseBookReader.FOOTER) != 0 && m_footerTextLeft != null)
								{
									flags &= ~(BaseBookReader.LINE_BREAK | BaseBookReader.WORD_BREAK);
								}else
								{
									flags &= ~(BaseBookReader.LINE_BREAK | BaseBookReader.WORD_BREAK);								
									flags |= BaseBookReader.NEW_LINE;
								}
							}
						} else if (dash)
							flags |= BaseBookReader.WORD_BREAK;

						/*if (lineBreak)
							flags |= BaseBookReader.LINE_BREAK;*/
						
						if (separator && !dash)
							flags &= ~BaseBookReader.WORD_BREAK;

						if ((flags & BaseBookReader.FOOTER) == 0)
							lastStyleFlags = flags;
						
						int addResult = page.addWord(word, flags, style, position + wordStart); 

						if (addResult >= wordLength)
						{
							flags &= ~(BaseBookReader.NEW_PAGE | BaseBookReader.NEW_LINE | BaseBookReader.WORD_BREAK);
							
							if ((flags & BaseBookReader.FOOTER) != 0)
								footerBreak = false;
						}
						else
						if (addResult <= 0)
						{	
							if ((flags & BaseBookReader.FOOTER) != 0)
							{
								m_footerTextLeft = text;
								m_footerTextOffset = wordStart;
								footerBreak = true;
								if (wordStart > 0)
									page.addNonBreakFooterText(" >");
								//break;
							}
							else									
								m_reader.setOffset(wordStart);
							
							page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.PAGE_BREAK : TextPage.PAGE_BREAK, height, m_paddingTop + (m_fullScreen ? m_header : 0));
							//Log.d("TextReader", "Parsing took " + (System.currentTimeMillis() - start) + ", symbols " + wordStart  + ", page break");
							return page;
						} else
						{
							if ((flags & BaseBookReader.FOOTER) != 0)
							{
								m_footerTextLeft = text;
								m_footerTextOffset = wordStart + addResult;
								footerBreak = true;
								//page.addNonBreakFooterText(" >");
								//break;
							}
							else
								m_reader.setOffset(wordStart + addResult);
							
							page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.PAGE_BREAK : TextPage.PAGE_BREAK, height, m_paddingTop + (m_fullScreen ? m_header : 0));
							return page;
						}
					}
					wordStart = i + 1;
					hasTab = false;
					
					if ((flags & BaseBookReader.FOOTER) == 0)
					{
						if (stopAt > 0 && position + wordStart >= stopAt)
						{
							if (position + wordStart != stopAt)
							{
								page.trimLast();
								wordStart = (int)(stopAt - position);
							}
							
							if (lastChar + 1 - wordStart > 0)
							{
								m_reader.setOffset(wordStart);
								page.finish(TextPage.PAGE_BREAK, height, m_paddingTop + (m_fullScreen ? m_header : 0));
							} else
								page.finish(TextPage.PAGE_FULL, height, m_paddingTop + (m_fullScreen ? m_header : 0));
							return page;
						}			
					}
					
					/*if (footerBreak)
					{
						m_reader.advance();
						continue;
					}*/
				}				
			}
			
			if (!footerBreak && (flags & BaseBookReader.FOOTER) != 0)
				m_footerTextLeft = null;
			//Log.d("TextReader", "Parsing took " + (System.currentTimeMillis() - start));

			if ((flags & BaseBookReader.FOOTER) == 0)
			{
				lastFlags = flags & ~(BaseBookReader.NEW_PAGE | BaseBookReader.NEW_LINE | BaseBookReader.WORD_BREAK);
			}
			//long start = System.currentTimeMillis();
			
			if (m_footerTextLeft == null)
				m_reader.advance();
			
			//Log.d("TextReader", "Advance took " + (System.currentTimeMillis() - start));
		}		

		page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.NEW_PAGE : TextPage.NEW_PAGE, height, m_paddingTop + (m_fullScreen ? m_header : 0));
		return page;
	}

	public void nextPage(boolean update, boolean cacheMore)
	{
		if (m_pages == null)
			return;

		if (getWidth() == 0)
			return;

		// if (m_currentPage >= 1)
		// m_pages.get(m_currentPage - 1).getBitmap().recycle(); // recycle
		// pre-prev bitmap
		
		if (m_currentPage >= m_pages.size() - 4)
			cachePage(m_pages.size() != 0 && m_reader.getPosition() == 0, cacheMore);
		
		if (m_currentPage >= m_pages.size() - 1)
			return;

		m_currentPage++;
		if (update)
			doInvalidate();
	}	
	
	private void invalidateHeader()
	{
		if (m_header > 0)
		{
			switch(m_orientation)
			{
				case ORIENTATION_180:
					invalidate(0, m_actualHeight - m_header, m_actualWidth, m_actualHeight);
					break;
				case ORIENTATION_CW:
					invalidate(m_actualWidth - m_header, 0, m_actualWidth, m_actualHeight);
					break;
				case ORIENTATION_CCW:
					invalidate(0, 0, m_header, m_actualHeight);
					break;
				default:
					invalidate(0, 0, m_actualWidth, m_header);
					break;
			}
			//Log.d("TextReader", "Header invalidated");
		}
	}	
	

	private boolean cachePage(boolean delay, boolean cacheMore)
	{
		if (m_reader == null || m_reader.isFinished())
			return false;

		//long start = android.os.SystemClock.elapsedRealtime();
		
		int height;
		int width;
		
		if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180)
		{
			width = m_actualWidth;
			height = m_actualHeight;			
		} else
		{
			width = m_actualHeight;
			height = m_actualWidth;			
		}
		
		//long start = System.currentTimeMillis();
		//android.os.Debug.startMethodTracing("textReader");
		
		
		if (m_pages.size() > 0)
		{
			TextPage prevPage = m_pages.get(m_pages.size() - 1);
			if (prevPage.getEndPosition() != m_reader.getPosition())
			{
				m_reader.reset(prevPage.getEndPosition() + prevPage.getEndOffset());
				m_footerTextLeft = null;
				//m_reader.setOffset(prevPage.getEndOffset());
			}
		}

		TextPage page = preparePage(width - m_paddingLeft - m_paddingRight, height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0), 0, -1, ++m_pageNumber);
		
		float lineChars = page.getAverageLineChars(5);
		//int pageLines = page.getAverageLines(5);
		
		//Log.d("TextReader", "Average line chars: " + lineChars + ", average lines: " + pageLines);
		
		//android.os.Debug.stopMethodTracing();
		
		//Log.d("TextReader", "Page prepare took " + (System.currentTimeMillis() - start));
		
		long endPosition = m_reader.getPosition();
		/*if (m_reader.isPushed())
			endPosition -= m_reader.getText().toString().getBytes().length;*/
				
		page.setEndPosition(endPosition);
		page.setEndOffset(m_reader.getOffset());

		long startPos = m_reader.getGlobalPosition(page.getStartPosition());
		long endPos = m_reader.getGlobalPosition(page.getEndPosition());

		if (lineChars != 0)
		{
			int pageSize = (int)(endPos - startPos);// *100/90;
			
			int lineHeight = (int)(m_textPaint.getHeight() + 2 * m_textPaint.getHeightMod());
			int pageHeight = height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0);
			
			/*Object bookData = m_reader.getData();
			
			if (bookData instanceof BookData)
			{
				int pages = ((BookData)bookData).measureBookPages(lineHeight, lineChars, pageHeight);
				
				Log.d("TextReader", "Measured " + pages + " pages");
			}*/
				
			int pageLines = (int)(pageHeight / lineHeight + 1);
			//lineChars = (int)((width - m_paddingWidth * 2) / m_textPaint.getDashWidth());
			
			//Log.d("TextReader", "Size line chars:" + lineChars + ", average lines: " + pageLines);
			
			int maxSize = (int)(pageLines * lineChars * 1.2f);
	
			if (pageSize <= 0)
				pageSize = maxSize; 
			else
			if (pageSize > maxSize)
				pageSize = maxSize;
			
			
			m_averagePageSizes.put(pageSize);
			
			int averageSize = m_averagePageSizes.getMedian();
			
			
			//Log.d("TextReader", "Average page size " + averageSize);
			
			
			{
				m_averagePageSize += pageSize;
				m_averagePages++;		
			
				//Log.d("TextReader", "Page size " + pageSize + ", pages added " + m_averagePages + ", average size " + averageSize + ", calculated average " + (m_averagePageSize/m_averagePages) + ", median " + m_averagePageSizes.getMedian());
				
				if (m_averagePages > 0 && (/*m_averagePages == 1 ||*/ (m_averagePages - 1) % (m_maxCachedPages) == 0) && averageSize != 0)
				{
					int value = (int)(m_reader.getMaxSize() / averageSize) + m_chapters.size();
					
					float percent = m_reader.getPercent(page.getStartPosition()); 
					
					int percentValue = (int)Math.round(100 * page.getPageNumber() / percent);
					if (m_averageTotal == 0)
					{
						if (percent > 10)
						{						
							m_averageTotal = percentValue;
						} else
							m_averageTotal = value;
						
						//Log.d("TextReader", "Initing average total " + m_averageTotal);					
					}
					else
					{
						if (percent > 10)
							if (value > percentValue * 1.2 || value < percentValue / 0.8)
								value = percentValue;
						
						m_averageTotal = (m_averageTotal * 3 + value)/4;					
					}
				}
			}
		}
		/*.CachePageRunnable.} else
		{
			if (m_averagePages >= m_maxCachedPages - 1)
				m_averageTotal = (int)(m_reader.getMaxPosition() * m_averagePages/m_averagePageSize);
		}*/
			
		/*if (m_useDirectDraw)
		{
			try
			{
				Bitmap bitmap = Bitmap.createBitmap(600, 800, m_useDirectDraw ? Bitmap.Config.ALPHA_8 : Bitmap.Config.RGB_565);
				drawPage(page, bitmap);
			}
			catch (OutOfMemoryError ex)
			{
				Log.d("TextReader", "Could not cache page!");
			}
		}*/		

		m_pages.add(page);

		if (m_pages.size() > m_maxCachedPages)
		{
			TextPage bpage = m_pages.remove(0);
			if (bpage != null)
				bpage.clean();
			m_currentPage--;
		}
		
		/*if (m_useDirectDraw)
		{
			for (int i = 0; i < m_currentPage; i++)
			{
				Bitmap bitmap = m_pages.get(i).getBitmap();
				if (bitmap != null)
					bitmap.recycle();
			}
			// m_pages.remove(0);
			// m_currentPage--;
		}*/
		
		//Log.d("TextReader", "Page prepare took " + (android.os.SystemClock.elapsedRealtime() - start));

		if (!m_reader.isFinished() && m_pages.size() - m_currentPage < m_maxCachedPages && !m_pause && cacheMore)
		{
			if (delay)
				postDelayed(new CachePageRunnable(/* m_currentPage + 1 */), 0);
			else
				cachePage(false, true);
		}
		return true;
	}

	public void prevPage()
	{
		if (getWidth() == 0)
			return;
		
		if (m_currentPage <= 0)
		{
			if (m_pages == null || m_pages.size() == 0)
				return;
			
			int height;
			int width;
			
			if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180)
			{
				width = m_actualWidth;
				height = m_actualHeight;			
			} else
			{
				width = m_actualHeight;
				height = m_actualWidth;			
			}
			
			
			TextPage page = m_pages.get(0);
			long pagePos = page.getStartPosition();
			if (pagePos <= m_bookStart)
				return;
			
			//int pageSize = m_averagePages == 0 ? (int)(2f * width * height / (m_textPaint.getDashWidth() * m_textPaint.getHeight())) : 2 * m_averagePageSize /m_averagePages;
			int pageLines = (int)((height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0)) /  (m_textPaint.getHeight() + m_textPaint.getHeightMod()) + 1);
			int lineChars = (int)((width - m_paddingLeft - m_paddingRight) / m_textPaint.getDashWidth());
			
			
			//if (pageSize > pageLines * lineChars)
				int pageSize = pageLines * lineChars;
				
			//if (pagePos - pageSize <= 0/* && page.getPageNumber() == 1*/)
				//return;
			
			//long start = System.currentTimeMillis();
			
			//android.os.Debug.startMethodTracing("/cache/textReader");
			
			int count = 0;
			do
			{
				//Log.d("TextReader", "Going backwards from pos " + pagePos + ", for " + pageSize);
				pageSize = m_reader.seekBackwards(pagePos, pageSize, pageLines, lineChars);
				
				int pageNumber = page.getPageNumber() - 1;
				if( pageNumber < 1)
					pageNumber = 1;
				
				long startPos = m_reader.getPosition();
				page = preparePage(width - m_paddingLeft - m_paddingRight, height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0), 0x10000, pagePos, pageNumber);
				
				//Log.d("TextReader", "New pos is " + page.getStartPosition());
				pagePos = startPos; 
				
				//if (pageSize == 0 || page.getStartPosition() == -1)
					//m_bookStart = startPos;
					//break;
			}			
			while(page.getStartPosition() == -1 && count++ < 5);
			//android.os.Debug.stopMethodTracing();
			
			//if (page.getStartPosition() == -1)
				//Log.w("TextReader", "Shit happened: 5 tries of going back resulted in empty page");
				
			long endPosition = m_reader.getPosition();
					
			page.setEndPosition(endPosition);
			page.setEndOffset(m_reader.getOffset());
			
			//Log.d("TextReader", "Backward rendering took " + (System.currentTimeMillis() - start));
			
			//m_pages.clear();
			//m_pages.add(page);
			m_pages.add(0, page);
			
			if (m_pages.size() > 20)
				m_pages.remove(m_pages.size()-1);
			
			m_currentPage = 1;			
		}
		
		m_currentPage--;
		doInvalidate();
	}
	
	public void resume()
	{
		m_pause = false;
		if (BaseActivity.isNook)
			NookEInkMode.setTwoBitMode();
		else
		if (BaseActivity.isNookTouch)
		{
			NookTouchEPD.setNoDelay();
			
			updateRefresh();
		}

		getContext().registerReceiver(m_batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		getContext().registerReceiver(m_timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
	}

	public void pause()
	{
		m_pause = true;
		
		if (BaseActivity.isNook)
			NookEInkMode.setFourBitMode();
		else
		if (BaseActivity.isNookTouch)
		{
			NookTouchEPD.setDelay();
			
			NookTouchEPD.setMode("APP_3", "AUTO", "ACTIVE", this);
		}
		
		getContext().unregisterReceiver(m_batteryLevelReceiver);
		getContext().unregisterReceiver(m_timeReceiver);
		
		if (m_pages != null)
			for (int i = 0; i < m_pages.size(); i++)
			{
				Bitmap bitmap = m_pages.get(i).getBitmap();
				if (bitmap != null)
					bitmap.recycle();
			}
	}
	
	/*public void gotoPosition(long position, int page, boolean update)
	{
		m_reader.reset(position);
		resetPages(false);
		m_pageNumber = page;
		nextPage(update);
	}*/

	/*public void gotoChapter(int id)
	{
		((ComplexBookReader) m_reader).gotoChapter(id);
		resetPages(false);
		if (m_averageTotal == 0)
			m_pageNumber = 0;
		else
		{
			m_pageNumber = (int)(m_reader.getGlobalPosition(m_reader.getPosition()) * m_averagePages/m_averagePageSize) - 1;
			if (m_pageNumber < 0)
				m_pageNumber = 0;
		}
		nextPage(true);
	}*/

	public void gotoPosition(long position)
	{
		if (m_reader == null)
			return;
		m_reader.reset(position);
		m_footerTextLeft = null;
		resetPages(false);		
		if (m_averagePages == 0)
			m_pageNumber = 0;
		else
		{
			m_pageNumber = (int)(m_reader.getGlobalPosition(m_reader.getPosition()) * m_averagePages/m_averagePageSize) - 1;
			if (m_pageNumber < 0)
				m_pageNumber = 0;
		}
		nextPage(true, true);
	}
	

	public void gotoPage(int page, float percent)
	{
		if (page == getPageNumber())
			return;
		
		if (m_reader == null)
			return;
		
		//gotoPosition(m_reader.getLocalPosition((long)(m_reader.getMaxSize() * percent)));
		m_reader.reset((long)(m_reader.getMaxSize() * percent));
		resetPages(false);
		m_pageNumber = page;
		nextPage(true, false);
		//m_reader.gotoPercent(percent);
		//nextPage(true);
	}

	/*
	public void gotoPage(int page)
	{
		if (m_reader == null)
			return;
		
		long position = m_reader.getMaxSize() * page/ m_averageTotal; 
		position = m_reader.getLocalPosition(position);
		m_reader.reset(position);
		m_footerTextLeft = null;
		resetPages(false);
		
		m_pageNumber = page - 1;
		nextPage(true);
	}
	*/
	public long getPosition()
	{
		if (m_pages == null || m_pages.size() == 0)
			return -1;

		if (m_currentPage < 0 || m_currentPage >= m_pages.size())
			return -1;

		return m_pages.get(m_currentPage).getStartPosition();
	}
	
	public int getPageNumber()
	{
		if (m_pages == null || m_pages.size() == 0)
			return 0;

		if (m_currentPage < 0 || m_currentPage >= m_pages.size())
			return 0;

		int result = m_pages.get(m_currentPage).getPageNumber() - 1;
		if (result < 1)
			return 1;

		return result;
	}
	
	public int getTotalPages()
	{
		if (m_pages == null || m_pages.size() == 0)
			return 0;

		if (m_currentPage < 0 || m_currentPage >= m_pages.size())
			return 0;

		TextPage page = m_pages.get(m_currentPage);
		
		float percent = m_reader.getPercent(page.getEndPosition());		
		
		int height;
		int width;
		
		if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180)
		{
			width = m_actualWidth;
			height = m_actualHeight;			
		} else
		{
			width = m_actualHeight;
			height = m_actualWidth;			
		}

		int pageNumber = page.getPageNumber();
			
		int total = m_averageTotal;
		
		if (total <= 0 && m_reader != null)
		{
			int pageLines = (int)(height /  (m_textPaint.getHeight() + m_textPaint.getHeightMod()) + 1);
			int lineChars = (int)(width / m_textPaint.getDashWidth());
			total = (int)m_reader.getMaxSize() / (pageLines * lineChars) + m_chapters.size();				
		}

		if (pageNumber > total || percent >= 100)
			total = pageNumber;
		else
		if (pageNumber == total && percent < 100)
			total = pageNumber + 1;
		
		return total;
	}
	
	public boolean isMenuTouch(MotionEvent event)
	{
		switch(m_orientation)
		{
			case ORIENTATION_NORMAL:
				return event.getRawY() < m_header;
			case ORIENTATION_180:
				return event.getRawY() > getHeight() - m_header;
			case ORIENTATION_CCW:
				return event.getRawX() < m_header;
			case ORIENTATION_CW:
				return event.getRawX() > getWidth() - m_header;
		}
		return false;
	}
	
	public boolean isOverlayTouch(MotionEvent event)
	{
		if (m_overlayView == null)
			return false;
		
		int location[] = new int[4];
		m_overlayView.getLocationOnScreen(location);
		location[0] += m_overlayView.getPaddingLeft(); 
		location[1] += m_overlayView.getPaddingTop(); 
		location[2] = location[0] + m_overlayView.getWidth() - m_overlayView.getPaddingRight(); 
		location[3] = location[1] + m_overlayView.getHeight() - m_overlayView.getPaddingBottom(); 
		
		return event.getRawX() >= location[0] && event.getRawY() >= location[1] && event.getRawX() <= location[2] && event.getRawY() <= location[3];
	}

	private class CachePageRunnable implements Runnable
	{
		@Override
		public void run()
		{
			if (!m_reader.isFinished() && m_pages.size() - m_currentPage < m_maxCachedPages)
				cachePage(true, true);
		}
	}
}

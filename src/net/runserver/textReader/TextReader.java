package net.runserver.textReader;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.runserver.bookParser.BaseBook;
import net.runserver.bookParser.EpubBook;
import net.runserver.bookParser.Fb2Book;
import net.runserver.bookParser.FontData;
import net.runserver.bookParser.HtmlBook;
import net.runserver.common.BaseActivity;
import net.runserver.common.CustomMenuView;
import net.runserver.common.DBSettings;
import net.runserver.common.LimitedFrameView;
import net.runserver.common.MenuData;
import net.runserver.common.MenuItem;
import net.runserver.common.Pair;
import net.runserver.common.BaseCustomMenu.OnMenuListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

public class TextReader extends BaseActivity implements OnClickListener, OnLongClickListener, OnMenuListener, UncaughtExceptionHandler,
		OnTouchListener, BrightnessMenu.OnBrightnessChangeListener, GotoMenu.OnPageChangeListener, BordersMenu.OnApplyBordersListener
{
	// constants
	public static final int NOOK_KEY_PREV_LEFT = 96;
	public static final int NOOK_KEY_PREV_RIGHT = 98;
	public static final int NOOK_KEY_NEXT_LEFT = 95;
	public static final int NOOK_KEY_NEXT_RIGHT = 97;

	public static final int NOOK_KEY_SHIFT_UP = 101;
	public static final int NOOK_KEY_SHIFT_DOWN = 100;
	
	public static final int KEYCODE_PAGE_BOTTOMLEFT = 0x5d;
	public static final int KEYCODE_PAGE_BOTTOMRIGHT = 0x5f;
	public static final int KEYCODE_PAGE_TOPLEFT = 0x5c;
	public static final int KEYCODE_PAGE_TOPRIGHT = 0x5e;
	
	public static final int REVERSE_NONE = 0;
	public static final int REVERSE_ALWAYS = 1;
	public static final int REVERSE_LANDSCAPE = 2;

	public static final int TOUCH_VERTICAL = 0;
	public static final int TOUCH_HORIZONTAL = 1;
	
	// fields
	private static final HashMap<Integer, Integer> s_fontSizes;
	private static final HashMap<Integer, Integer> s_advFontSizes;
	//private static final HashMap<Integer, Integer> s_advBrightnessValues;
	private static final HashMap<Integer, Float> s_contrastValues;
	private static final HashMap<Integer, Float> s_lineSpacings;
	private static final HashMap<Integer, Integer> s_headerSizes;
	private static final HashMap<Integer, Integer> s_footerTypes;
	private static final HashMap<Integer, Integer> s_refreshModes;
	
	private static final HashMap<String, String> s_fontPaths = new HashMap<String, String> ();

	static
	{
		s_fontSizes = new HashMap<Integer, Integer>();
		s_fontSizes.put(R.id.font_size_xsmall, 20);
		s_fontSizes.put(R.id.font_size_small, 22);
		s_fontSizes.put(R.id.font_size_medium, 25);
		s_fontSizes.put(R.id.font_size_large, 28);
		s_fontSizes.put(R.id.font_size_xlarge, 30);

		s_advFontSizes = new HashMap<Integer, Integer>();
		s_advFontSizes.put(R.id.adv_font_size_14, 14);
		s_advFontSizes.put(R.id.adv_font_size_15, 15);
		s_advFontSizes.put(R.id.adv_font_size_16, 16);
		s_advFontSizes.put(R.id.adv_font_size_17, 17);
		s_advFontSizes.put(R.id.adv_font_size_18, 18);
		s_advFontSizes.put(R.id.adv_font_size_19, 19);
		s_advFontSizes.put(R.id.adv_font_size_20, 20);
		s_advFontSizes.put(R.id.adv_font_size_21, 21);
		s_advFontSizes.put(R.id.adv_font_size_22, 22);
		s_advFontSizes.put(R.id.adv_font_size_23, 23);
		s_advFontSizes.put(R.id.adv_font_size_24, 24);
		s_advFontSizes.put(R.id.adv_font_size_25, 25);
		s_advFontSizes.put(R.id.adv_font_size_26, 26);
		s_advFontSizes.put(R.id.adv_font_size_27, 27);
		s_advFontSizes.put(R.id.adv_font_size_28, 28);
		s_advFontSizes.put(R.id.adv_font_size_29, 29);
		s_advFontSizes.put(R.id.adv_font_size_30, 30);
		s_advFontSizes.put(R.id.adv_font_size_31, 31);
		s_advFontSizes.put(R.id.adv_font_size_32, 32);
		s_advFontSizes.put(R.id.adv_font_size_33, 33);
		s_advFontSizes.put(R.id.adv_font_size_34, 34);
		s_advFontSizes.put(R.id.adv_font_size_35, 35);
		s_advFontSizes.put(R.id.adv_font_size_36, 36);
		s_advFontSizes.put(R.id.adv_font_size_37, 37);
		s_advFontSizes.put(R.id.adv_font_size_38, 38);
		s_advFontSizes.put(R.id.adv_font_size_39, 39);
		s_advFontSizes.put(R.id.adv_font_size_40, 40);

		/*s_advBrightnessValues = new HashMap<Integer, Integer>();
		s_advBrightnessValues.put(R.id.brightness_system, -1);
		s_advBrightnessValues.put(R.id.brightness_10, 25);
		s_advBrightnessValues.put(R.id.brightness_20, 50);
		s_advBrightnessValues.put(R.id.brightness_30, 75);
		s_advBrightnessValues.put(R.id.brightness_40, 100);
		s_advBrightnessValues.put(R.id.brightness_50, 125);
		s_advBrightnessValues.put(R.id.brightness_60, 150);
		s_advBrightnessValues.put(R.id.brightness_70, 175);
		s_advBrightnessValues.put(R.id.brightness_80, 200);
		s_advBrightnessValues.put(R.id.brightness_90, 225);
		s_advBrightnessValues.put(R.id.brightness_100, 250);*/

		s_contrastValues = new HashMap<Integer, Float>();
		s_contrastValues.put(R.id.contrast_normal, 0.0f);
		s_contrastValues.put(R.id.contrast_average, 0.15f);
		s_contrastValues.put(R.id.contrast_high, 0.25f);
		s_contrastValues.put(R.id.contrast_extra, 0.5f);
		s_contrastValues.put(R.id.contrast_super, 0.8f);
		s_contrastValues.put(R.id.contrast_idiot, 1.0f);

		s_lineSpacings = new HashMap<Integer, Float>();		
		// s_lineSpacings.put(R.id.adv_line_space_80, 0.8f);
		// s_lineSpacings.put(R.id.adv_line_space_85, 0.85f);
		s_lineSpacings.put(R.id.adv_line_space_90, 0.90f);
		s_lineSpacings.put(R.id.adv_line_space_95, 0.95f);
		s_lineSpacings.put(R.id.adv_line_space_100, 1f);
		s_lineSpacings.put(R.id.adv_line_space_105, 1.05f);
		s_lineSpacings.put(R.id.adv_line_space_110, 1.1f);
		s_lineSpacings.put(R.id.adv_line_space_115, 1.15f);
		s_lineSpacings.put(R.id.adv_line_space_120, 1.2f);
		s_lineSpacings.put(R.id.adv_line_space_125, 1.25f);
		s_lineSpacings.put(R.id.adv_line_space_130, 1.3f);
		s_lineSpacings.put(R.id.adv_line_space_135, 1.35f);
		s_lineSpacings.put(R.id.adv_line_space_140, 1.4f);
		s_lineSpacings.put(R.id.adv_line_space_145, 1.45f);
		s_lineSpacings.put(R.id.adv_line_space_150, 1.5f);
		
		s_headerSizes = new HashMap<Integer, Integer>();
		s_headerSizes.put(R.id.header_off, 0);
		s_headerSizes.put(R.id.header_normal, 33);
		s_headerSizes.put(R.id.header_large, 45);
		s_headerSizes.put(R.id.header_small, 25);
		s_headerSizes.put(R.id.header_system, -1);
		
		s_footerTypes = new HashMap<Integer, Integer>();
		s_footerTypes.put(R.id.footer_off, ReaderView.FOOTER_OFF);
		s_footerTypes.put(R.id.footer_flat, ReaderView.FOOTER_NORMAL);
		s_footerTypes.put(R.id.footer_ticks, ReaderView.FOOTER_TICKS);
		
		s_refreshModes = new HashMap<Integer, Integer>();
		s_refreshModes.put(R.id.refresh_normal, ReaderView.REFRESH_NORMAL);
		//s_refreshModes.put(R.id.refresh_partial_2bit, ReaderView.REFRESH_A2);
		s_refreshModes.put(R.id.refresh_partial_4bit, ReaderView.REFRESH_G16);
	}

	private CustomMenuView m_menuView;
	private ReaderView m_readerView;
	private LimitedFrameView m_menuDialog;
	private ViewGroup m_menuScreen;
	private ColorMenu m_colorMenu;
	private BordersMenu m_bordersMenu;
	private BrightnessMenu m_brightnessMenu; 
	private GotoMenu m_gotoMenu;
	
	private String m_fileName;
	private String m_filePath;
	//private boolean m_useDirectDraw;
	private UncaughtExceptionHandler m_exceptionHandler;

	private List<FontData> m_internalFonts;
	private HashMap<String, FontInfo[]> m_externalFonts;
	private HashMap<Integer, String[]> m_fontFaces;

	private BaseBook m_book;

	// settings
	private int m_fontSize = 22;
	private String m_fontFace = "#internal";
	private int m_rotation = ReaderView.ORIENTATION_NORMAL;
	private boolean m_inverse = false;
	private float m_extraStroke = 0;
	private float m_lineSpace = 1.0f;
	private int m_reverseMode = REVERSE_NONE;
	private int m_brightness = 0;
	private int m_footer = ReaderView.FOOTER_TICKS;
	private int m_header = 33;
	private int m_touchMode = TOUCH_VERTICAL;
	private int m_textColor = 0xFF000000;
	private int m_backColor = 0xFFFFFFFF;
	private int m_refreshMode = 0;
	private int m_paddingLeft = 10;
	private int m_paddingTop = 5;
	private int m_paddingBottom = 5;
	private int m_paddingRight = 10;
	
	private Context m_context = this; // this pointer for threads

	
	public TextReader()
	{
		super("Text Reader");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d("TextReader", "Starting intent " + getIntent());
		m_fontFaces = new HashMap<Integer, String[]>();

/*		runOnUiThread(new Runnable()
		{			
			@Override
			public void run()
			{*/
				Thread thread = new Thread()
				{
					@Override
					public void run()
					{
						init();
					}
				};
				thread.start();
	/*		}
	 	});*/

				
		if (!isNook && !isNookTouch && BaseActivity.SDKVersion >= 8)
		{
			Window window = getWindow();
			LayoutParams layoutParams = window.getAttributes();
			try
			{
		        Field buttonBrightness = layoutParams.getClass().getField("buttonBrightness");
		        buttonBrightness.set(layoutParams, 0);
		    } catch (Exception e)
		    {
		        e.printStackTrace();
		    }
		    window.setAttributes(layoutParams);
		}

		setContentView(R.layout.main);

		FrameLayout eink = (FrameLayout) findViewById(R.id.eink);
		m_readerView = new ReaderView(this, eink, /*m_useDirectDraw, */isEpad ? 0.92f : 1.0f);
		m_readerView.setOnTouchListener(this);

		//Log.d("TextReader", "Board is " + Build.MODEL);

		//m_useDirectDraw = isNook && DirectDraw.isDirectDrawPossible();	
		
		//Log.d("TextReader", "Creating menu");
		
		
		
		createMenu();
	}
	
	private void scanFonts()
	{		
		m_externalFonts = new HashMap<String, FontInfo[]>();

		try
		{
			if (isNook)
			{
				FontInfo.scanPath("/system/media/sdcard/my fonts/", m_externalFonts);
				FontInfo.scanPath("/sdcard/my fonts/", m_externalFonts);
			} else
			{
				FontInfo.scanPath("/sdcard/fonts/", m_externalFonts);
				FontInfo.scanPath("/media/My Files/fonts/", m_externalFonts);
				FontInfo.scanPath("/media/fonts/", m_externalFonts);
			}
		}
		catch(Exception ex)
		{
			Log.e("TextReader", "Font seek failed: " + ex);
			ex.printStackTrace();
		}
		
		if (m_fontFaces == null)
			m_fontFaces = new HashMap<Integer, String[]>();
		
		m_fontFaces.put(R.id.font_name_internal, new String[]{"#internal", getString(R.string.internal_font)});
		m_fontFaces.put(R.id.font_name_zdroid,  new String[]{"#serif", getString(R.string.droid)});
		m_fontFaces.put(R.id.font_name_zdroid_sans, new String[]{"#sans-serif", getString(R.string.droid_sans)});

		
		int id = 0;
		for(Map.Entry<String, FontInfo[]> entry: m_externalFonts.entrySet())
		{	
			FontInfo[] infos = entry.getValue();
			
			if (infos[FontInfo.NORMAL] == null)
			{
				Log.d("TextReader", "Skipped font: " + entry.getKey());
				continue;
			}
			
			//Log.d("TextReader", "Got font: " + entry.getKey());
			
			
			m_fontFaces.put(0x2000 | id, new String[]{entry.getKey(), infos[0].Name } );
			
			id++;
		}
	}
	
	private void updateMenuSize()
	{
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int widthPadding = dm.widthPixels / 50;
		if (widthPadding < dm.density * 2)
			widthPadding = (int)(dm.density * 2);
		
		float dpi = dm.density * 160;
			
		float iwidth = (dm.widthPixels - widthPadding * 2)/dpi;
		
		if (!isNookColor && iwidth > 4)
			widthPadding = (int)((dm.widthPixels - 4 * dpi)/2);
		
		int heightPadding = dm.heightPixels / 25;
		if (heightPadding < dm.density * 4)
			heightPadding = (int)(dm.density * 4);

		float iheight = (dm.heightPixels - heightPadding * 2)/dpi;
		
		if (!isNookColor && iheight > 3)
			heightPadding = (int)((dm.heightPixels - 3 * dpi)/2);

		if (widthPadding < 0)
			widthPadding = 0;
		if (widthPadding > dm.widthPixels / 3)
			widthPadding = dm.widthPixels / 3;
		
		if (heightPadding < 0)
			heightPadding = 0;
		if (heightPadding > dm.heightPixels / 3)
			heightPadding = dm.heightPixels / 3;
		
		m_menuDialog.setPadding(widthPadding, heightPadding, widthPadding, heightPadding);
		
		m_menuDialog.setSize(-1, -1);
	}

	private void createMenu()
	{
		m_menuScreen = (ViewGroup) findViewById(R.id.touchscreen);
		m_menuScreen.removeAllViews();

		m_menuDialog = new LimitedFrameView(this, 480, 144);
		if (!isNook)
		{
			updateMenuSize();
			m_menuDialog.setVisibility(View.GONE);			

			DisplayMetrics dm = BaseActivity.DisplayMetrics;
			
			if (dm.widthPixels * dm.heightPixels <= 153600)
			{
				s_fontSizes.put(R.id.font_size_xsmall, 12);
				s_fontSizes.put(R.id.font_size_small, 14);
				s_fontSizes.put(R.id.font_size_medium, 15);
				s_fontSizes.put(R.id.font_size_large, 16);
				s_fontSizes.put(R.id.font_size_xlarge, 18);
				m_fontSize = 14;
			}
		}

		m_menuDialog.setOnTouchListener(this);
		
		View menuFrame = LayoutInflater.from(this).inflate(R.layout.menu_dialog, m_menuDialog);
		View back = menuFrame.findViewById(R.id.backButton);
		if (back != null)
			back.setOnClickListener(this);
		
		m_menuScreen.addView(m_menuDialog);
		
		//Log.d("TextReader", "Initing xml menu");
		//android.os.Debug.startMethodTracing("/system/media/sdcard/textReader");
		
		m_menuView = new TextReaderMenu(this, (FrameLayout) m_menuDialog.findViewById(R.id.dialog_content), R.xml.menu);
		m_menuView.setBackButtonListener(R.id.backButton, this);
		// m_menuView.setUpButtonListener(this);
		// m_menuView.setDownButtonListener(this);
		// m_menuView.setUpButtonLongListener(this);
		// m_menuView.setDownButtonLongListener(this);
		// m_menuView.setSelectButtonListener(this);
		m_menuView.setMenuListener(this);

		m_menuView.setNavigatorVisibility(View.GONE);

		/*if (m_useDirectDraw)
		{
			m_exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
		}*/
		
		if (!isNook && !isNookTouch)
			m_colorMenu = new ColorMenu(this, m_menuView.getSubMenu(R.id.color_menu), m_textColor, m_backColor);
		
		m_bordersMenu = new BordersMenu(m_menuView.getSubMenu(R.id.bounds_menu));
		m_bordersMenu.setOrientation(DisplayMetrics.widthPixels < DisplayMetrics.heightPixels);

		if (isNook)
		{
			m_bordersMenu.setListener(this);
			
			m_menuView.setMenuVisibility(R.id.rotation_180, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_inverse, View.GONE);
			// m_menuView.setMenuVisibility(R.id.settings_inverse, View.GONE);
			//m_menuView.setMenuVisibility(R.id.main_exit, View.GONE);
			m_menuView.setMenuVisibility(R.id.main_library, View.GONE);
			m_menuView.setMenuVisibility(R.id.main_brightness, View.GONE);
			//m_menuView.setMenuVisibility(R.id.adv_settings_brightness, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_contrast, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_touch, View.GONE);
			m_menuView.setMenuVisibility(R.id.settings_color, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_refresh, View.GONE);
		} else
		if (isNookTouch)
		{
			m_menuView.setMenuVisibility(R.id.settings_color, View.GONE);
			m_menuView.setMenuVisibility(R.id.main_brightness, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_contrast, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_inverse, View.GONE);
		} else
		{
			m_menuView.setMenuVisibility(R.id.settings_contrast, View.GONE);
			m_menuView.setMenuVisibility(R.id.adv_settings_contrast, View.VISIBLE);
			m_menuView.setMenuVisibility(R.id.adv_settings_refresh, View.GONE);			
		}

		if (!isNook)
		{
			ViewGroup brightnessDialog = new FrameLayout(this);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1,-2);
			params.gravity = Gravity.CENTER;
			brightnessDialog.setLayoutParams(params);			
			LayoutInflater.from(this).inflate(R.layout.brightness_view, brightnessDialog);			
			m_menuScreen.addView(brightnessDialog);
			
			m_brightnessMenu = new BrightnessMenu(brightnessDialog);
			m_brightnessMenu.setBrightnessChangeListener(this);
			m_brightnessMenu.hide();
			
		}
		{
			ViewGroup gotoDialog = new FrameLayout(this);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1,-2);
			params.gravity = Gravity.CENTER;
			gotoDialog.setLayoutParams(params);			
			LayoutInflater.from(this).inflate(R.layout.goto_page_view, gotoDialog);
			if (isNook)
				((FrameLayout) m_menuDialog.findViewById(R.id.dialog_content)).addView(gotoDialog);
			else
				m_menuScreen.addView(gotoDialog);
			
			m_gotoMenu = new GotoMenu(gotoDialog, BaseActivity.isNook ? m_menuView : null );
			m_gotoMenu.setPageChangeListener(this);
			m_gotoMenu.hide();
		}
		
		
		//m_menuView.setMenuEnabled(R.id.goto_page, false);

		m_menuView.setMenuEnabled(R.id.main_goto, false);
		m_menuView.setMenuEnabled(R.id.main_settings, false);

		//android.os.Debug.stopMethodTracing();
		// DirectDraw.setEnableValue("3");
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		Log.d("TextReader", "Got new intent " + intent);
		super.onNewIntent(intent);

		setIntent(intent);

		if (intent != null)
		{
			Uri uri = intent.getData();
			if (uri != null)
			{
				String fileName = uri.getLastPathSegment();
				String filePath = uri.getLastPathSegment();
				if (!filePath.equals(m_filePath))
				{
					m_fileName = fileName;
					m_filePath = uri.getPath();
				} else
					return;
			} else
				return;
		}
		
		m_readerView.clear();
		
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				init();
			}
		});		
	}

	private void init()
	{	
		Intent intent = getIntent();

		if (intent != null)
		{
			Uri uri = intent.getData();
			if (uri != null)
			{
				String fileName = uri.getLastPathSegment();
				String filePath = uri.getLastPathSegment();
				if (!filePath.equals(m_filePath))
				{
					m_fileName = fileName;
					m_filePath = uri.getPath();
				} else
					return;
			} else
				return;
		}

		File file = new File(m_filePath);
		
		if (!file.exists())
			return;
		
		Log.d("TextReader", "Processing file " + m_filePath);
		
		String lowerName = m_fileName.toLowerCase(); 
		
		if (lowerName.endsWith(".epub"))
		{
			m_book = new EpubBook(m_filePath);
		}
		else if (lowerName.endsWith(".fb2") || lowerName.endsWith(".fb2.zip"))
		{
			m_book = new Fb2Book(m_filePath);
		}
		else if (lowerName.endsWith(".xhtml") || lowerName.endsWith(".html") || lowerName.endsWith(".htm"))
		{
			m_book = new HtmlBook(m_filePath);
		}
		else
		{
			// TODO: unknown book!
			return;
		}
		
		/*String cacheFile = getCacheDir() + "/cache.nbr";
		boolean inited = false;
		
		if (BaseBook.checkFile(cacheFile, file.getName(), file.length()))
		{		
			if (m_book.deserialize(cacheFile))
			{
				Log.d("TextReader", "Got cached book for file " + file.getName());			
				inited = true;
			}			
		}
		
		if (!inited)*/
		{
			//android.os.Debug.startMethodTracing("textReader");

			if (!m_book.init(getCacheDir().getAbsolutePath()))
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						m_menuScreen.setVisibility(View.GONE);
						new AlertDialog.Builder(m_context)
				        .setIcon(android.R.drawable.ic_dialog_alert)
				        .setTitle(R.string.invalid_book_title)
				        .setMessage(R.string.invalid_book)
				        .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener()
				        {
				            @Override
				            public void onClick(DialogInterface dialog, int which)
				            {
				            		m_menuScreen.setVisibility(View.VISIBLE);
				                goBack();
				            }
				        })
				        .show();
					}
				});
				return;
			}
			//m_book.serialize(cacheFile, file.getName(), file.length());
			//Log.d("TextReader", "Saved cached book for file " + file.getName());
			
			//android.os.Debug.stopMethodTracing();
		}
		
		//try
		//{
			//File bitmapsDir = new File(getCacheDir() + "/bitmaps/");
			//bitmapsDir.createNewFile();
		
			//for(ImageData data: m_book.getImages())
				//data.init(getCacheDir().getAbsolutePath());
		//}
		//catch(Exception ex)
		//{
			//Log.d("TextReader", "Failed to init bitmap cache");
		//}


		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				postInit();
			}
		});
	}

	private void postInit()
	{
		long start = System.currentTimeMillis();
		
		long[] pos = new long[] { 0, -1 };

		loadSettings(pos);
		
		if (m_header != -1)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else
			getWindow().setFlags(0,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if (!isNook && !isNookTouch)
		{
			updateBrightness();
		
			if (m_colorMenu != null)
			{
				m_colorMenu.setColors(m_textColor, m_backColor);
				m_colorMenu.setInverse(m_inverse);
			}
		}
		
		m_bordersMenu.setBorders(m_paddingTop, m_paddingLeft, m_paddingBottom, m_paddingRight);

		if (m_fontFaces != null)
			m_fontFaces.clear();
		if (m_fontFace.length() > 0 && m_fontFace.charAt(0) != '#')
			scanFonts();
		
		m_internalFonts = m_book.getFonts();

		if (m_fontFace.equals("#internal") && (m_internalFonts == null || m_internalFonts.size() == 0))
			m_fontFace = "#serif";
		
		updateFonts();
		
		List<Pair<String, Float>> chapterPositions = new ArrayList<Pair<String, Float>>(m_book.getChapters().size());
		try
		{
			for (Pair<Long, String> chapter: m_book.getChapters())
			{
				long cpos = chapter.first;
				float percent = m_book.getReader().getPercent(cpos);
				chapterPositions.add(new Pair<String, Float>(chapter.second, percent));
			}
		}
		catch(Exception ex)
		{
			Log.e("TextReader", "Error parsing chapters " + ex);
		}
		
		m_readerView.init(m_book.getReader(), pos[0], (int) pos[1], chapterPositions, m_book.getStyles(), m_book.getImages(), m_book.getNotes());

		// visual sugar - chapters, goto menu, settings menu
		
		/*m_readerView.postDelayed(new Runnable()
		{
			public void run()
			{
				long start = System.currentTimeMillis();
				
				//createMenu();
				*/
				Thread thread = new Thread()
				{
					public void run()
					{	
						if (m_fontFaces == null || m_fontFaces.size() == 0)
							scanFonts();
						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								long start = System.currentTimeMillis();
								
								updateMenu(m_book.getChapters(), m_internalFonts != null && m_internalFonts.size() > 0);
								
								m_gotoMenu.initChapters(m_readerView.getChapters());
								m_menuView.setMenuEnabled(R.id.main_goto, true);
								updateSettingsMenu();
								m_menuView.setMenuEnabled(R.id.main_settings, true);	
								
								m_menuView.setMenuEnabled(R.id.goto_page, true);
								
								Log.d("TextReader", "Goto & settings init took " + (System.currentTimeMillis() - start));
							};
						});
						
					};
				};
				thread.start();
				
			/*	Log.d("TextReader", "Initial menu creation took " + (System.currentTimeMillis() - start));
			}
		}, 1000);*/
					
		Log.d("TextReader", "Post init " + (System.currentTimeMillis() - start));
		
		if (isNookTouch)
		{
			try
			{
				Bundle bundle = new Bundle();
				bundle.putString("product_details_ean", "'" + m_book.getTitle() + "'");
				bundle.putInt("read_share_status_page", m_readerView.getPageNumber());
				bundle.putInt("read_share_status_page", (int)m_book.getReader().getPercent(m_readerView.getPosition()));			
				
				Intent intent = new Intent();
				intent.setAction("com.encore.intent.action.share.reading.status");
				intent.setFlags(0x400);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		//Log.d("TextReader", "Post init " + (System.currentTimeMillis() - start));
		// m_readerView.doInvalidate();
	}

	private void loadSettings(long[] pos)
	{
		long position = 0;
		int page = 1;

		DBSettings settings = new DBSettings(this);
		
		String bookKey = "book:" + m_filePath;
		String positionString = settings.getString(bookKey, "");
		
		try
		{
			if (positionString.length() > 0)
			{
				Log.d("TextReader", "Loadeded position string " + positionString + " for key " + bookKey);
				String[] positions = positionString.split(":");
	
				if (positions.length >= 2)
				{
					position = Long.parseLong(positions[0].trim());
					page = Integer.parseInt(positions[1].trim());
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		m_fontFace = settings.getString("font", m_fontFace);
		m_fontSize = settings.getInt("fontSize", m_fontSize);
		m_rotation = settings.getInt("rotation", m_rotation);
		m_extraStroke = settings.getFloat("extraStroke", m_extraStroke);
		m_inverse = settings.getBoolean("inverse", m_inverse);
		m_lineSpace = settings.getFloat("lineSpace", m_lineSpace);
		m_reverseMode = settings.getInt("reverseMode", m_reverseMode);
		m_brightness = settings.getInt("brightness", m_brightness);
		m_header = settings.getInt("header", m_header);
		m_footer = settings.getInt("footer", m_footer);
		m_touchMode = settings.getInt("touchMode", m_touchMode);
		m_textColor = settings.getInt("textColor", m_textColor);
		m_backColor = settings.getInt("backColor", m_backColor);
		m_refreshMode = settings.getInt("refreshMode", m_refreshMode);
		m_paddingLeft = settings.getInt("paddingLeft", m_paddingLeft);
		m_paddingTop = settings.getInt("paddingTop", m_paddingTop);
		m_paddingRight = settings.getInt("paddingRight", m_paddingRight);
		m_paddingBottom = settings.getInt("paddingBottom", m_paddingBottom);

		// Log.d("TextReader", "Loading settings: " + settings.getAll());
		pos[0] = position;
		pos[1] = page;

		settings.close();
	}
	
	private void saveSettings()
	{
		// SharedPreferences settings = getSharedPreferences("TextReader", 0);
		Thread thread = new Thread()
		{
			public void run()
			{
				doSaveSettings();
			}
		};
		thread.start();
	}

	private void doSaveSettings()
	{
		try
		{
			DBSettings settings = new DBSettings(m_context, true);
			
			settings.delete("positions");
			
			if (m_readerView != null && m_readerView.getPosition() != -1) // not yet inited
			{
				String bookKey = "book:" + m_filePath;
				String positionString = Long.toString(m_readerView.getPosition()) + ":" +  Integer.toString(m_readerView.getPageNumber()) + ":0";
				settings.putString(bookKey, positionString);
				Log.d("TextReader", "Saving position string: " + positionString + " for key " + bookKey);
			}
			
			settings.putString("font", m_fontFace);
			settings.putInt("fontSize", m_fontSize);
			settings.putInt("rotation", m_rotation);
			settings.putFloat("extraStroke", m_extraStroke);
			settings.putBoolean("inverse", m_inverse);
			settings.putFloat("lineSpace", m_lineSpace);
			settings.putInt("reverseMode", m_reverseMode);
			settings.putString("readingNow", m_filePath);
			settings.putInt("brightness", m_brightness);
			settings.putInt("header", m_header);
			settings.putInt("footer", m_footer);
			settings.putInt("touchMode", m_touchMode);
			settings.putInt("textColor", m_textColor);
			settings.putInt("backColor", m_backColor);
			settings.putInt("refreshMode", m_refreshMode);
			settings.putInt("paddingLeft", m_paddingLeft);
			settings.putInt("paddingTop", m_paddingTop);
			settings.putInt("paddingRight", m_paddingRight);
			settings.putInt("paddingBottom", m_paddingBottom);
			settings.putInt("lastApplication", 1);
	
			// Log.d("TextReader", "Saving Positions: " + positionString);
	
			// editor.commit();
	
			// Log.d("TextReader", "Saving settings: " + settings.getAll());
			settings.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private String lookupFont(List<FontData> internalFonts, String... names)
	{
		FontData fontData = null;

		for (int i = 0; i < internalFonts.size(); i++)
		{
			FontData data = internalFonts.get(i);
			String name = data.getName().toLowerCase();

			if (names.length == 0 && name.indexOf("-") == -1 && name.indexOf("_") == -1)			
			{	
				fontData = data;
				break;
			}
			
			for (int j = 0; j < names.length; j++)
				if (name.contains(names[j]))
				{
					fontData = data;
					break;
				}

			if (fontData != null)
				break;
		}

		if (fontData == null)
			return null;
		// fontData = internalFonts.get(0);

		return fontData.extractFont(getCacheDir().getAbsolutePath());
	}

	private static Object addAssetPath(AssetManager manager, String path)
	{
		if (s_fontPaths.containsKey(path))
			return null;
		
		try
		{
			Method method = AssetManager.class.getMethod("addAssetPath", String.class);
			Object result = method.invoke(manager, path);
			s_fontPaths.put(path, "");
			return result;
		}
		catch (Exception ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		return null;
	}
	
	private static Typeface createFromAsset(AssetManager assets, String name)
	{
		try
		{
			return Typeface.createFromAsset(assets, name);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	private void updateFonts()
	{
		Typeface normal = null;
		Typeface bold = null;
		Typeface italic = null;
		Typeface boldItalic = null;

		AssetManager assets = getAssets();
		
	//	Log.d("TextReader", "Setting font " + m_fontFace);

		if (!m_fontFace.startsWith("#") && m_externalFonts != null && m_externalFonts.containsKey(m_fontFace) && m_externalFonts.get(m_fontFace)[FontInfo.NORMAL] != null)
		{
			FontInfo [] infos = m_externalFonts.get(m_fontFace);
			
			if (infos[FontInfo.NORMAL] != null)
			{
				addAssetPath(assets, infos[FontInfo.NORMAL].Path);
				normal = createFromAsset(assets, infos[FontInfo.NORMAL].File);
			}

			if (infos[FontInfo.BOLD] != null)
			{
				addAssetPath(assets, infos[FontInfo.BOLD].Path);
				bold = createFromAsset(assets, infos[FontInfo.BOLD].File);
			}
			
			if (infos[FontInfo.BOLD_ITALIC] != null)
			{
				addAssetPath(assets, infos[FontInfo.BOLD_ITALIC].Path);
				boldItalic = createFromAsset(assets, infos[FontInfo.BOLD_ITALIC].File);
			}
			
			if (infos[FontInfo.ITALIC] != null)
			{
				addAssetPath(assets, infos[FontInfo.ITALIC].Path);
				italic = createFromAsset(assets, infos[FontInfo.ITALIC].File);
			}
			
			
			if (normal == null)
				normal = Typeface.SERIF;
			if (bold == null)
			{
				bold = Typeface.create(normal, Typeface.BOLD);
				if (bold == null)
					bold = normal;
			}
			if (italic == null)
			{
				italic = Typeface.create(normal, Typeface.ITALIC);
				if (italic == null)
					italic = normal;
			}
			if (boldItalic == null)
			{
				boldItalic = Typeface.create(normal, Typeface.BOLD_ITALIC);
				if (boldItalic == null)
					boldItalic = italic;
			}
		} else			
		if (m_fontFace.equals("#internal") && m_internalFonts != null && m_internalFonts.size() > 0)
		{
			addAssetPath(assets, getCacheDir().getAbsolutePath() + "/");

			String normalFile = lookupFont(m_internalFonts, "-regular", "-normal", "regular", "normal");

			if (normalFile == null)
				normalFile = lookupFont(m_internalFonts); // no suffix
			
			if (normalFile != null)
			{
				normal = createFromAsset(assets, normalFile);
			}

			if (normal == null)
				normal = Typeface.SERIF;

			String italicFile = lookupFont(m_internalFonts, "-italic", "-oblique", "-it");

			if (italicFile != null)
			{
				italic = createFromAsset(assets, italicFile);
			}

			if (italic == null)
			{
				italic = Typeface.create(normal, Typeface.ITALIC);
				if (italic == null)
					italic = normal;
			}

			String boldFile = lookupFont(m_internalFonts, "-bold", "-semibold");
			if (boldFile != null)
			{
				bold = createFromAsset(assets, boldFile);
			}

			if (bold == null)
			{
				bold = Typeface.create(normal, Typeface.BOLD);
				if (bold == null)
					bold = normal;
			}

			String boldItalicFile = lookupFont(m_internalFonts, "-bolditalic", "-semibolditalic", "-boldoblique", "-semiboldit");

			if (boldItalicFile != null)
			{
				boldItalic = createFromAsset(assets, boldItalicFile);
			}

			if (boldItalic == null)
			{
				boldItalic = Typeface.create(normal, Typeface.BOLD_ITALIC);
				if (boldItalic == null)
					boldItalic = italic;
			}

		} else
		{ // built-in fonts
			normal = Typeface.create(m_fontFace.startsWith("#") ? m_fontFace.substring(1) : m_fontFace, Typeface.NORMAL);
			
			if (normal == null)
			{
				normal = Typeface.SERIF;
				m_fontFace = "#serif";
			}

			bold = Typeface.create(normal, Typeface.BOLD);
			if (bold == null)
				bold = normal;

			italic = Typeface.create(normal, Typeface.ITALIC);
			if (italic == null)
				italic = normal;

			boldItalic = Typeface.create(normal, Typeface.BOLD_ITALIC);
			if (boldItalic == null)
				boldItalic = italic;
		}

		m_readerView.initFonts(m_fontSize, normal, bold, italic, boldItalic, m_rotation, m_extraStroke, m_inverse, m_textColor, m_backColor, m_lineSpace, m_book
				.getFirstLine() * m_fontSize / 30, m_header, m_footer, m_refreshMode, m_paddingLeft, m_paddingTop, m_paddingRight, m_paddingBottom);
	}
	
	private void updateSettings(boolean reset)
	{
		m_readerView.changeSettings(m_extraStroke, m_rotation, m_header, m_footer, m_inverse, m_textColor, m_backColor, m_refreshMode, reset);
	}

	private void updateSettingsMenu()
	{
		for (Map.Entry<Integer, Integer> entry : s_fontSizes.entrySet())
		{
			boolean checked = entry.getValue() == m_fontSize;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.settings_font_size, entry.getKey());
		}

		for (Map.Entry<Integer, Integer> entry : s_headerSizes.entrySet())
		{
			boolean checked = entry.getValue() == m_header;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.adv_settings_header, entry.getKey());
		}
		
		for (Map.Entry<Integer, Integer> entry : s_footerTypes.entrySet())
		{
			boolean checked = entry.getValue() == m_footer;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.adv_settings_footer, entry.getKey());
		}
		
		for (Map.Entry<Integer, Integer> entry : s_refreshModes.entrySet())
		{
			boolean checked = entry.getValue() == m_refreshMode;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.adv_settings_refresh, entry.getKey());
		}
		
		for (Map.Entry<Integer, Integer> entry : s_advFontSizes.entrySet())
		{
			boolean checked = entry.getValue() == m_fontSize;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.adv_settings_font_size, entry.getKey());
		}

		/*for (Map.Entry<Integer, Integer> entry : s_advBrightnessValues.entrySet())
		{
			boolean checked = entry.getValue() == m_brightness;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.adv_settings_brightness, entry.getKey());
		}*/

		for (Map.Entry<Integer, String[]> entry : m_fontFaces.entrySet())
		{
			boolean checked = m_fontFace.equals(entry.getValue()[0]);
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
			{
				m_menuView.setMenuValueEx(R.id.settings_font_name, entry.getKey());
				m_menuView.setMenuValueEx(R.id.adv_settings_font_name, entry.getKey());
			}
		}

		for (Map.Entry<Integer, Float> entry : s_lineSpacings.entrySet())
		{
			boolean checked = Math.abs(entry.getValue() - m_lineSpace) < 0.01f;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
				m_menuView.setMenuValueEx(R.id.adv_settings_line_space, entry.getKey());
		}

		for (Map.Entry<Integer, Float> entry : s_contrastValues.entrySet())
		{
			boolean checked = Math.abs(entry.getValue() - m_extraStroke) < 0.01f;
			m_menuView.setMenuChecked(entry.getKey(), checked);

			if (checked)
			{
				m_menuView.setMenuValueEx(R.id.settings_contrast, entry.getKey());
				m_menuView.setMenuValueEx(R.id.adv_settings_contrast, entry.getKey());
				//break;
			}
		}

		int rotationMenu = 0;
		int rotationId = 0;

		switch (m_rotation)
		{
			case ReaderView.ORIENTATION_NORMAL:
				rotationId = R.string.rotation_normal;
				rotationMenu = R.id.rotation_normal;
				break;
			case ReaderView.ORIENTATION_CW:
				rotationId = R.string.rotation_cw;
				rotationMenu = R.id.rotation_cw;
				break;
			case ReaderView.ORIENTATION_CCW:
				rotationId = R.string.rotation_ccw;
				rotationMenu = R.id.rotation_ccw;
				break;
			case ReaderView.ORIENTATION_180:
				rotationId = R.string.rotation_180;
				rotationMenu = R.id.rotation_180;
				break;
		}

		if (rotationId != 0)
		{
			m_menuView.setMenuValue(R.id.settings_rotation, rotationId);
			//m_menuView.setMenuValue(R.id.adv_settings_rotation, rotationId);
		}

		m_menuView.setMenuChecked(R.id.rotation_normal, false);
		m_menuView.setMenuChecked(R.id.rotation_cw, false);
		m_menuView.setMenuChecked(R.id.rotation_ccw, false);
		m_menuView.setMenuChecked(R.id.rotation_180, false);
		if (rotationMenu != 0)
			m_menuView.setMenuChecked(rotationMenu, true);

		// m_menuView.setMenuValue(R.id.settings_inverse, m_inverse ?
		// R.string.inverse_on : R.string.inverse_off);
		m_menuView.setMenuValue(R.id.adv_settings_inverse, m_inverse ? R.string.inverse_on : R.string.inverse_off);
		// m_menuView.setMenuChecked(R.id.inverse_off, !m_inverse);
		// m_menuView.setMenuChecked(R.id.inverse_on, m_inverse);

		m_menuView.setMenuValue(R.id.adv_settings_touch, m_touchMode == TOUCH_VERTICAL ? R.string.touch_vert : R.string.touch_horiz);
		
		switch (m_reverseMode)
		{
			case REVERSE_NONE:
				m_menuView.setMenuValue(R.id.adv_settings_reverse, R.string.reverse_off);
				break;
			case REVERSE_ALWAYS:
				m_menuView.setMenuValue(R.id.adv_settings_reverse, R.string.reverse_on);
				break;
			case REVERSE_LANDSCAPE:
				m_menuView.setMenuValue(R.id.adv_settings_reverse, R.string.reverse_landscape);
				break;
		}		
		
		saveSettings();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Log.d("TextReader", "Got on resume, intent " + getIntent());

		// init();

		/*
		 * Intent intent = getIntent();
		 * 
		 * if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()))
		 * { Uri uri = intent.getData(); if (uri != null) { String fileName =
		 * uri.getLastPathSegment(); if (!fileName.equals(m_fileName)) {
		 * this.res } } }
		 */

		if (m_header != -1)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		m_readerView.resume();

		// DirectDraw.doInvalidate(m_readerView);
		/*if (m_useDirectDraw)
		{
			DirectDraw.invalidate();
			m_readerView.doInvalidate();
		}*/
	}

	@Override
	protected void onPause()
	{
		doSaveSettings();
		super.onPause();

		if (m_header != -1)
			getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// DirectDraw.cancelInvalidate();
		/*if (m_useDirectDraw)
			DirectDraw.cancelInvalidate();*/
		m_readerView.pause(); // free pages memory
	}

	/*private void checkExternalFont(int id)
	{
		if (s_fontFaces.containsKey(id))
		{
			FontInfo info = (FontInfo)s_fontFaces.get(id); 
			
			Typeface font = Typeface.create(info.Path, Typeface.NORMAL);
			if (font != null)
			{
				// String fontName = font.toString();
				m_menuView.setMenuValue(id, info.Name);
				m_menuView.setMenuVisibility(id, View.VISIBLE);
				return;
			}
		}

		m_menuView.setMenuVisibility(id, View.GONE);
	}*/

	private void updateMenu(List<Pair<Long, String>> chapters, boolean internalFonts)
	{
		{ // chapters
			m_menuView.removeSubMenu(R.id.goto_chapter_menu);
	
			ArrayList<MenuItem> items = new ArrayList<MenuItem>(chapters.size());
	
			for (int i = 0; i < chapters.size(); i++)
			{
				Pair<Long, String> entry = chapters.get(i);
				MenuItem item = new MenuItem(0x1000 | i, 0, 0, entry.second);
				item.setTag(entry.first);
				items.add(item);
			}
			MenuData data = new MenuData(R.id.goto_chapter_menu, 0, items);
	
			m_menuView.addSubMenu(R.id.goto_chapter_menu, data);
		}
		
		{ // fonts
			m_menuView.removeSubMenu(R.id.font_name_menu);
			
			ArrayList<MenuItem> items = new ArrayList<MenuItem>(m_fontFaces.size());
			
			for(Map.Entry<Integer, String[]> entry: m_fontFaces.entrySet())
			{				
				items.add(new MenuItem(entry.getKey(), 0, 0, entry.getValue()[1]));
			}
			
			Collections.sort(items, new Comparator<MenuItem>() {
					@Override
					public int compare(MenuItem one, MenuItem another)
					{
						return new Integer(one.getId()).compareTo(another.getId());
					}
				});
	
			MenuData data = new MenuData(R.id.font_name_menu, 0, items);
	
			m_menuView.addSubMenu(R.id.font_name_menu, data);
			
			m_menuView.setMenuVisibility(R.id.font_name_internal, internalFonts ? View.VISIBLE : View.GONE);
		}
	}

	private void hideMenu()
	{
		m_readerView.setOverlayView(null);
		m_menuDialog.setVisibility(View.GONE);
		m_brightnessMenu.hide();
		m_gotoMenu.hide();
		m_menuScreen.setBackgroundColor(0);
		
		if (m_colorMenu != null)
		{
			int backColor = m_inverse ? m_colorMenu.getTextColor() : m_colorMenu.getBackColor();
			int textColor = m_inverse ? m_colorMenu.getBackColor() : m_colorMenu.getTextColor();
		
			if (backColor != m_backColor || textColor != m_textColor)
			{
				m_backColor = backColor;
				m_textColor = textColor;
				
				updateSettings(false);
			}
		}
		
		int paddingLeft = m_bordersMenu.getLeft();
		int paddingRight = m_bordersMenu.getRight();
		int paddingTop = m_bordersMenu.getTop();
		int paddingBottom = m_bordersMenu.getBottom();
		
		if (paddingLeft != m_paddingLeft || paddingRight != m_paddingRight || paddingTop != m_paddingTop || paddingBottom != m_paddingBottom)
		{
			m_paddingTop = paddingTop;
			m_paddingLeft = paddingLeft;
			m_paddingRight = paddingRight;
			m_paddingBottom = paddingBottom;
			
			updateFonts();
		}
		
		saveSettings();
		
		m_menuScreen.setOnTouchListener(null);
	}

	private void showMenu()
	{
		m_readerView.setOverlayView(m_menuDialog);
		m_menuDialog.setVisibility(View.VISIBLE);
		m_menuScreen.setBackgroundColor(0x40000000);
		m_menuScreen.setOnTouchListener(this);
	}
	
	private boolean isMenuVisible()
	{
		return (m_menuDialog.getVisibility() == View.VISIBLE || (m_brightnessMenu != null && m_brightnessMenu.isVisible())|| (m_gotoMenu != null && m_gotoMenu.isVisible()));
	}

	private void showBrightnessMenu()
	{		
		m_menuDialog.setVisibility(View.GONE);
		m_readerView.setOverlayView(m_brightnessMenu.getView());		
		m_brightnessMenu.show(m_brightness);
		m_menuScreen.setBackgroundColor(0);
	}

	private void showGotoMenu()
	{
		m_gotoMenu.show(m_readerView.getPageNumber(), m_readerView.getTotalPages(), !isNook);

		if (isNook)
			m_menuView.showTouchscreenMenu(m_gotoMenu.getView());
		else
		{
			m_menuDialog.setVisibility(View.GONE);
			m_readerView.setOverlayView(m_gotoMenu.getView());		
			m_menuScreen.setBackgroundColor(0);
		}
	}
	
	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.backButton:
				if (!isNook)
				{
					if (m_menuDialog.getVisibility() == View.VISIBLE)
					{
						if (m_menuView.isRoot())
							hideMenu();
						else
							m_menuView.goBack();
					}
				}
				else
					goBack();
				break;
			case R.id.touchscreen:
				hideMenu();
				break;
		}
	}

	@Override
	public boolean onLongClick(View view)
	{
		return false;
	}

	@Override
	public boolean onMenu(MenuItem item)
	{
		boolean hide = false;
		boolean update = false;
		int itemId = item.getId();
		switch (itemId)
		{
			case R.id.font_size_xsmall:
			case R.id.font_size_small:
			case R.id.font_size_medium:
			case R.id.font_size_large:
			case R.id.font_size_xlarge:
				m_fontSize = s_fontSizes.get(itemId);
				updateFonts();
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.adv_font_size_14:
			case R.id.adv_font_size_15:
			case R.id.adv_font_size_16:
			case R.id.adv_font_size_17:
			case R.id.adv_font_size_18:
			case R.id.adv_font_size_19:
			case R.id.adv_font_size_20:
			case R.id.adv_font_size_21:
			case R.id.adv_font_size_22:
			case R.id.adv_font_size_23:
			case R.id.adv_font_size_24:
			case R.id.adv_font_size_25:
			case R.id.adv_font_size_26:
			case R.id.adv_font_size_27:
			case R.id.adv_font_size_28:
			case R.id.adv_font_size_29:
			case R.id.adv_font_size_30:
			case R.id.adv_font_size_31:
			case R.id.adv_font_size_32:
			case R.id.adv_font_size_33:
			case R.id.adv_font_size_34:
			case R.id.adv_font_size_35:
			case R.id.adv_font_size_36:
			case R.id.adv_font_size_37:
			case R.id.adv_font_size_38:
			case R.id.adv_font_size_39:
			case R.id.adv_font_size_40:
				m_fontSize = s_advFontSizes.get(itemId);
				updateFonts();
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			/*case R.id.brightness_system:
			case R.id.brightness_10:
			case R.id.brightness_20:
			case R.id.brightness_30:
			case R.id.brightness_40:
			case R.id.brightness_50:
			case R.id.brightness_60:
			case R.id.brightness_70:
			case R.id.brightness_80:
			case R.id.brightness_90:
			case R.id.brightness_100:
				m_brightness = s_advBrightnessValues.get(itemId);
				updateBrightness();
				updateSettingsMenu();
				break;*/
			case R.id.adv_line_space_90:
			case R.id.adv_line_space_95:
			case R.id.adv_line_space_100:
			case R.id.adv_line_space_105:
			case R.id.adv_line_space_110:
			case R.id.adv_line_space_115:
			case R.id.adv_line_space_120:
			case R.id.adv_line_space_125:
			case R.id.adv_line_space_130:
			case R.id.adv_line_space_135:
			case R.id.adv_line_space_140:
			case R.id.adv_line_space_145:
			case R.id.adv_line_space_150:
				m_lineSpace = s_lineSpacings.get(itemId);
				updateFonts();
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.font_name_zdroid:
			case R.id.font_name_zdroid_sans:
			case R.id.font_name_internal:
				m_fontFace = m_fontFaces.get(itemId)[0];
				updateFonts();
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.rotation_cw:
				m_rotation = ReaderView.ORIENTATION_CW;
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.rotation_ccw:
				m_rotation = ReaderView.ORIENTATION_CCW;
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.rotation_normal:
				m_rotation = ReaderView.ORIENTATION_NORMAL;
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.rotation_180:
				m_rotation = ReaderView.ORIENTATION_180;
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			
			case R.id.header_large:
			case R.id.header_small:
			case R.id.header_normal:
			case R.id.header_off:
				
				if (m_header == -1)
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				
				m_header = s_headerSizes.get(itemId);
				
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.header_system:
				if (m_header != -1)
					getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);

				m_header = s_headerSizes.get(itemId);
				
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
				
			case R.id.footer_flat:
			case R.id.footer_off:
			case R.id.footer_ticks:
				m_footer = s_footerTypes.get(itemId);
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
			case R.id.adv_settings_refresh:
				m_refreshMode = (m_refreshMode + 1) % 2;
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				break;
			case R.id.refresh_normal:
			case R.id.refresh_partial_2bit:
			case R.id.refresh_partial_4bit:
				m_refreshMode = s_refreshModes.get(itemId);
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				hide = true;
				break;
				
			case R.id.adv_settings_header:			
				
				switch(m_header)
				{
					case 0:
						m_header = 25;
						break;
					case 25:
						m_header = 33;
						break;
					case 33:
						m_header = 45;
					case 45:
						m_header = -1;
						break;
					default:
						m_header = 0;
						break;
				}					
				
				if (m_header != -1)
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				else
					getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				break;
				
			case R.id.adv_settings_footer:
				m_footer = (m_footer + 1) % 3;
				updateSettings(true);
				updateSettingsMenu();
				update = true;
				break;
			case R.id.adv_settings_touch:
				m_touchMode = (m_touchMode + 1) % 2;
				updateSettingsMenu();
				update = true;
				break;
				
			case R.id.adv_settings_inverse:
				// case R.id.settings_inverse:
				m_inverse = !m_inverse;
				if (m_colorMenu != null)
					m_colorMenu.setInverse(m_inverse);
				//updateFonts();
				updateSettings(false);
				updateSettingsMenu();
				update = true;
				break;
			case R.id.adv_settings_reverse:
				m_reverseMode = (m_reverseMode + 1) % 3;
				updateSettingsMenu();
				break;
			
			case R.id.contrast_normal:
			case R.id.contrast_average:
			case R.id.contrast_high:
			case R.id.contrast_extra:
			case R.id.contrast_super:
			case R.id.contrast_idiot:
				m_extraStroke = s_contrastValues.get(itemId);
				updateSettings(false);
				updateSettingsMenu();
				update = true;
				break;
			 
			/*case R.id.settings_contrast:
			case R.id.adv_settings_contrast:

				Iterator<Map.Entry<Integer, Float>> it = s_contrastValues.entrySet().iterator();

				boolean found = false;
				while (it.hasNext())
				{
					Map.Entry<Integer, Float> entry = it.next();
					if (Math.abs(entry.getValue() - m_extraStroke) < 0.01f)
					{
						found = true;
						break;
					}
				}

				if (!found || !it.hasNext())
					it = s_contrastValues.entrySet().iterator();

				m_extraStroke = it.next().getValue();
				updateSettings();
				update = true;
				break;*/
			case R.id.goto_cover:
				List<Pair<Long, String>> chapters = m_book.getChapters();
				if (chapters != null && chapters.size() > 0)
					m_readerView.gotoPosition(chapters.get(0).first);
				// hide = true;
				break;
			case R.id.goto_page:
				showGotoMenu();
				hide = !isNook;
				break;
			case R.id.main_exit:
				doSaveSettings();
				clean();
				terminate();
				break;
			case R.id.main_library:
				Intent intent = new Intent();
				intent.setData(Uri.parse("content://settings/system"));
				intent.setClassName("net.runserver.textReader", "net.runserver.fileBrowser.FileBrowser");
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				terminate();
				break;			
				
			default:
				if ((itemId & 0x1000) != 0) // chapters
				{
					m_readerView.gotoPosition((Long) item.getTag());
					hide = true;
				} else
				if ((itemId & 0x2000) != 0) // fonts
				{
					m_fontFace = m_fontFaces.get(itemId)[0];
					updateFonts();
					updateSettingsMenu();
					update = true;
					hide = true;
				}
				break;
		}

		if (update)
			m_readerView.doInvalidate();

		return hide;
	}
	

	@Override
	public boolean onMenuBack(int menuId)
	{
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//Log.d("TextReader", "Key " + keyCode + " down");
		
		boolean pageTurn = false;
		boolean forward = true;

		switch (keyCode)
		{
			case NOOK_KEY_NEXT_LEFT:
			case NOOK_KEY_NEXT_RIGHT:
			// case KEYCODE_PAGE_BOTTOMRIGHT: same as NOOK_KEY_NEXT_LEFT
			case KEYCODE_PAGE_BOTTOMLEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case NOOK_KEY_SHIFT_DOWN:
				pageTurn = true;
				forward = !(m_rotation == ReaderView.ORIENTATION_CW || m_rotation == ReaderView.ORIENTATION_180);
				break;
			case NOOK_KEY_PREV_LEFT:
			case NOOK_KEY_PREV_RIGHT:
			case KEYCODE_PAGE_TOPRIGHT:
			case KEYCODE_PAGE_TOPLEFT:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_UP:
			case NOOK_KEY_SHIFT_UP:
				pageTurn = true;
				forward = m_rotation == ReaderView.ORIENTATION_CW || m_rotation == ReaderView.ORIENTATION_180;
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				pageTurn = true;
				forward = !(m_rotation == ReaderView.ORIENTATION_CW || m_rotation == ReaderView.ORIENTATION_180);

				if (isEpad)
					forward = !forward;
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:
				pageTurn = true;
				forward = m_rotation == ReaderView.ORIENTATION_CW || m_rotation == ReaderView.ORIENTATION_180;

				if (isEpad)
					forward = !forward;
				break;
			case KeyEvent.KEYCODE_BACK:
				if (m_menuDialog.getVisibility() == View.VISIBLE)
				{
					if (m_menuView.isRoot())
						hideMenu();
					else
						m_menuView.goBack();
					return false;
				}
				
				if ((m_brightnessMenu != null && m_brightnessMenu.isVisible()) || (m_gotoMenu != null && m_gotoMenu.isVisible()))
				{
					hideMenu();
					return false;
				}

				goBack();
				break;
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_SPACE:
				if (!isNook)
				{
					if (isMenuVisible())
						hideMenu();
					/*else
						showMenu();*/
					return false;
				}
		}

		if (pageTurn)
		{
			if (!isNook && isMenuVisible())
				return false;
			
			switch (m_reverseMode)
			{
				case REVERSE_LANDSCAPE:
					if (m_readerView.getWidth() > m_readerView.getHeight())
						forward = !forward;
					break;
				case REVERSE_ALWAYS:
					forward = !forward;
					break;
			}

			if (forward)
				m_readerView.nextPage(true, true);
			else
				m_readerView.prevPage();

			if (m_menuView != null)
				m_menuView.goToRoot();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		//Log.d("TextReader", "Key " + keyCode + " up");
		
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event)
	{		
		if (event.getAction() != MotionEvent.ACTION_UP)
		{
			/*if (isNookTouch && event.getDownTime() > 10)
			{
				openOptionsMenu();
				return false;				
			}*/
			return true;
		}

		if (view == m_menuDialog)
			return false;

		if (!isNook)
		{	
			if (isMenuVisible())
			{
				if (view != m_menuScreen && m_readerView.isOverlayTouch(event))
					return false;

				hideMenu();

				return true;
			}

			if (m_readerView.isMenuTouch(event))
			{
				openOptionsMenu();
				//showMenu();
				return false;
			}
		}
				
		boolean forward = true;
		
		switch(m_touchMode)
		{
			case TOUCH_VERTICAL:
				if (m_rotation == ReaderView.ORIENTATION_NORMAL || m_rotation == ReaderView.ORIENTATION_180)
				{
					forward = event.getX() > view.getWidth() / 2;
				} else
				{
					forward = (event.getY() > view.getHeight() / 2) == (m_rotation == ReaderView.ORIENTATION_CW);
				}
				break;
			case TOUCH_HORIZONTAL:
				if (m_rotation == ReaderView.ORIENTATION_NORMAL || m_rotation == ReaderView.ORIENTATION_180)
				{
					forward = event.getY() > view.getHeight() / 2;
				} else
				{
					forward = (event.getX() > view.getWidth() / 2) == (m_rotation == ReaderView.ORIENTATION_CW);
				}
				break;
		}

		switch (m_reverseMode)
		{
			case REVERSE_LANDSCAPE:
				if ((view.getWidth() > view.getHeight()) == (m_rotation == ReaderView.ORIENTATION_NORMAL || m_rotation == ReaderView.ORIENTATION_180))
					forward = !forward;
				break;
			case REVERSE_ALWAYS:
				forward = !forward;
				break;
		}

		if (forward)
			m_readerView.nextPage(true, true);
		else
			m_readerView.prevPage();

		return super.onTouchEvent(event);
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable arg1)
	{
		/*if (m_useDirectDraw)
			DirectDraw.cancelInvalidate();*/

		DBSettings settings = new DBSettings(this, true);
		settings.putInt("lastApplication", 0);
		settings.close();

		m_exceptionHandler.uncaughtException(arg0, arg1);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// doCreate();
		//initFonts(m_internalFonts, true);
		if (m_fontFaces.size() == 0)
			scanFonts();

		m_bordersMenu.setOrientation(DisplayMetrics.widthPixels < DisplayMetrics.heightPixels);
		updateSettings(true);
		updateSettingsMenu();
		updateMenuSize();
		// m_readerView.onSizeChanged(//w, h, newConfig., oldh)
		// m_readerView.init();
	}
	
	private void updateBrightness()
	{
		try
		{
			java.lang.reflect.Field fieldScreenBrightness = WindowManager.LayoutParams.class.getField("screenBrightness");

			int value = m_brightness <= 0 ? Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) : m_brightness <= 10 ? 10 : m_brightness;
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			fieldScreenBrightness.setFloat(lp, (float)value/255.0f);
			getWindow().setAttributes(lp);
		}
		catch(Exception ex)
		{
			Log.d("TextReader", "Failed setting brightness ");
			ex.printStackTrace();
		}
	}
	
	public void onBrightnessChanged(float value)
	{
		m_brightness = (int)(255*value) + 1;
		if (m_brightness > 255)
			m_brightness = 255;
			
		//m_menuView.setMenuValue(R.id.adv_settings_brightness, Integer.toString(100*m_brightness/255) + "%");
		updateBrightness();
	}
	
	public void onPageChanged(int page, float percent)
	{
		m_readerView.gotoPage(page, percent);
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.main_goto:
				m_menuView.setRootMenu(R.id.goto_menu);
				showMenu();
				break;
			case R.id.main_exit:	
				doSaveSettings();
				clean();
				terminate();
				
				break;
			case R.id.main_brightness:
				showBrightnessMenu();
				
				//item.setCheckable(true);
				//item.setChecked(!item.isChecked());
				return false;
			case R.id.main_library:
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.setData(Uri.parse("content://settings/system"));
				intent.setClassName("net.runserver.textReader", "net.runserver.fileBrowser.FileBrowser");
				startActivity(intent);
				terminate();
				break;
			case R.id.main_settings:
				m_menuView.setRootMenu(R.id.settings_menu);
				showMenu();
				break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.clear();
	    MenuInflater inflater = getMenuInflater();
    		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (isNookTouch)
			menu.findItem(R.id.main_brightness).setVisible(false);
		return true;
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		clean();	
	}
	
	@Override
	public void onApplyBorders()
	{
		int paddingLeft = m_bordersMenu.getLeft();
		int paddingRight = m_bordersMenu.getRight();
		int paddingTop = m_bordersMenu.getTop();
		int paddingBottom = m_bordersMenu.getBottom();
		
		if (paddingLeft != m_paddingLeft || paddingRight != m_paddingRight || paddingTop != m_paddingTop || paddingBottom != m_paddingBottom)
		{
			m_paddingTop = paddingTop;
			m_paddingLeft = paddingLeft;
			m_paddingRight = paddingRight;
			m_paddingBottom = paddingBottom;
			
			updateFonts();
			m_readerView.doInvalidate();
		}
		
		saveSettings();
		
		m_menuView.goBack();
		
	}
	
	private void clean()
	{
		//Log.d("TextReader", "onDestroy called");

		if (m_readerView != null)
		{
			m_readerView.clear();
			//m_readerView = null;
		}
		if (m_book != null)
		{
			m_book.clean();
			m_book = null;
		}
		m_externalFonts = null;
		m_fontFaces = null;
		m_internalFonts = null;
		/*
		m_menuView = null;
		m_menuDialog = null;
		m_menuScreen = null;
		m_colorMenu = null;
		m_gotoMenu = null;
		m_brightnessMenu = null;
*/
	//	finish();
		//System.gc();
		//goBack();
	}
}

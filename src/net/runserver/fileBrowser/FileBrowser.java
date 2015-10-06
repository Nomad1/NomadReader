/* 
 * Copyright 2010 RunServer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.runserver.fileBrowser;

import java.io.File;

import net.runserver.common.BaseActivity;
import net.runserver.common.DBSettings;
import net.runserver.common.MenuItem;
import net.runserver.common.CustomMenuView;
import net.runserver.common.BaseCustomMenu.OnMenuListener;
import net.runserver.library.CoverView;
import net.runserver.library.FileInfo;
import net.runserver.library.Library;
import net.runserver.library.CoverView.OnCoverClickListener;
import net.runserver.library.metaData.MetaDataFactory;
import net.runserver.textReader.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

public class FileBrowser extends BaseActivity implements OnClickListener, OnCoverClickListener, OnMenuListener
{
	public static final int KEYCODE_PAGE_BOTTOMLEFT = 0x5d;
	public static final int KEYCODE_PAGE_BOTTOMRIGHT = 0x5f;
	public static final int KEYCODE_PAGE_TOPLEFT = 0x5c;
	public static final int KEYCODE_PAGE_TOPRIGHT = 0x5e;


	public static final int MODE_NONE = 0;
	public static final int MODE_COVERS = 1;
	public static final int MODE_LIST = 2;
	public static final int MODE_SHELF = 3;
	
	private BookLibrary m_bookLibrary;

	private FileInfoView m_fileInfoView;
	private FileBrowserView m_fileBrowserView;
	private CoverView m_coverView;
	private ViewGroup m_coversContainer;
	private ViewGroup m_infoContainer;
	private ViewGroup m_listContainer;
	
	private CustomMenuView m_menuView;
	private ViewGroup m_menuScreen;
	private ViewGroup m_menuDialog;
	
	private String m_lastBook;
	private String m_cacheLocation;
	
	private int m_mode = MODE_NONE;
	
	public String getCacheLocation()
	{
		return m_cacheLocation;
	}
	
	public FileBrowser()
	{
		super("File Browser");
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Uri uri = getIntent().getData();
		
		if (uri == null)
		{
			int lastApplication = 0;
			try
			{
				DBSettings settings = new DBSettings(this);
				lastApplication = settings.getInt("lastApplication", 0);
				m_lastBook = settings.getString("readingNow", "");
				settings.close();
			}
			catch(Exception ex)
			{
				Log.d("FileBrowser", "Problem loading settings database: " + ex);
			}
			
			if (lastApplication != 0 && m_lastBook != null && m_lastBook.length() > 0 && new File(m_lastBook).exists())
			{
				Intent intent = new Intent();
				intent.setClassName("net.runserver.textReader", "net.runserver.textReader.TextReader");
				intent.setData(Uri.parse("file://" + m_lastBook));
				startActivity(intent);
				terminate();
				return;
			}
		}
		
		MetaDataFactory.initDB(this);

		setContentView(R.layout.browser);
		
		m_coversContainer = (ViewGroup)findViewById(R.id.covers);
		m_infoContainer = (ViewGroup)findViewById(R.id.details);
		m_listContainer = (ViewGroup)findViewById(R.id.list_view);

		m_coverView = new CoverView(this, m_coversContainer);
		m_coverView.setCloseClickListener(this);
		m_coverView.setCoverClickListener(this);

		m_bookLibrary = new BookLibrary(this);

		updateCacheLocation(true);
		
		m_fileInfoView = new FileInfoView(this, m_infoContainer);
		m_fileInfoView.setOnClickListener(this);
		
		m_fileBrowserView = new FileBrowserView(this, m_listContainer, m_bookLibrary);
		m_fileBrowserView.setOnClickListener(this);

		if (isNookTouch)
		{
			View coverBack = findViewById(R.id.back_plane);
			coverBack.setBackgroundColor(0xFFFFFFFF);
		}


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

		m_menuDialog = new FrameLayout(this); 
		m_menuDialog.setPadding(widthPadding, heightPadding, widthPadding, heightPadding);
		m_menuDialog.setFocusable(true);

		m_menuScreen = (ViewGroup) findViewById(R.id.menu);
		m_menuScreen.addView(m_menuDialog);

		View menu = LayoutInflater.from(this).inflate(R.layout.menu_dialog, m_menuDialog);
		menu.findViewById(R.id.backButton).setOnClickListener(this);

		m_menuView = new FileBrowserMenu(this, (FrameLayout) menu.findViewById(R.id.dialog_content), R.xml.browser_menu);
		//m_menuView.setBackButtonListener(this);
		m_menuView.setMenuListener(this);
		
		hideMenu();
		updateMenu();
		
		onChangeMode(MODE_COVERS);
	}
	
	private void updateCacheLocation(boolean clean)
	{
		m_cacheLocation = getCacheDir().getAbsolutePath();

		switch (m_bookLibrary.getCacheMode())
		{
			case Library.CACHE_INTERNAL:
				try
				{
					long longExpireDate = java.util.Calendar.getInstance().getTimeInMillis() - 3 * 24 * 60 * 60 * 1000; // 3 days
					long shortExpireDate = java.util.Calendar.getInstance().getTimeInMillis() - 1 * 24 * 60 * 60 * 1000; // 1 days
					for(File file : getCacheDir().listFiles())
					{
						if (file.lastModified() < longExpireDate || (file.lastModified() < shortExpireDate && file.length() >= 0x100000))
						{
							Log.d("FileBrowser", "Purged file " + file + " from cache");
							file.delete();
						}
					}
				}
				catch(Exception ex)
				{
					Log.d("FileBrowser", "Exception clearing cache: "+ ex);
				}
				
				break;
			case Library.CACHE_NEAR:
				m_cacheLocation = null;
				break;
			case Library.CACHE_EXTERNAL:
				if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
				{
					String externalCard = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

					File check = new File(externalCard + "/.nomadreader");
					if (check.exists())
						m_cacheLocation = check.getAbsolutePath();
					else
					{
						check = new File(externalCard + "/.nomedia");
						if (check.exists())
							m_cacheLocation = check.getAbsolutePath();
						else
						{
							check = new File(externalCard + "/tmp");
							if (check.exists())
								m_cacheLocation = check.getAbsolutePath();
							else
							{
								check = new File(externalCard + "/.nomadreader");
								try
								{
									if (check.createNewFile())
										m_cacheLocation = check.getAbsolutePath();
								}
								catch(Exception ex)
								{
									Log.d("FileBrowser", "Exception creating cache path");
								}
							}
						}
					}
				}
				break;
		}
	}	
	
	private void showMenu()
	{
		//findViewById(R.id.root).setEnabled(false);
		//findViewById(R.id.root).setDrawingCacheEnabled(true);
		m_coverView.suspend();
		m_menuScreen.setBackgroundColor(0x40000000);
		m_menuDialog.setVisibility(View.VISIBLE);
	}

	private void hideMenu()
	{
		m_menuDialog.setVisibility(View.GONE);
		//findViewById(R.id.root).setEnabled(true);
		//findViewById(R.id.root).setDrawingCacheEnabled(false);
		m_coverView.resume();
		m_menuScreen.setBackgroundColor(0);
//		findViewById(R.id.body).setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.clear();
	    MenuInflater inflater = getMenuInflater();
    		inflater.inflate(R.menu.browser_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
  		android.view.MenuItem item = menu.findItem(R.id.main_continue);
   		if (item != null)
   			item.setEnabled(m_lastBook != null && m_lastBook.length() > 0 && new File(m_lastBook).exists());
   		
		return true;
	}
	
	// implements OnClickListener, for back button
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.touchscreen:
				if (m_menuScreen.getVisibility() == View.VISIBLE)
					hideMenu();
				break;	
			case R.id.cover_close_button:
				goBack();
				break;
			case R.id.details_open:				
				if (!m_bookLibrary.onSelect(m_fileInfoView.getCurrentInfo()))
				{
					//finish(); // close while reading
				}				
				break;
			case R.id.backButton:
				if (m_menuDialog.getVisibility() == View.VISIBLE)
				{
					if (m_menuView.isRoot())
						hideMenu();
					else
						m_menuView.goBack();
				}
				break;
			case FileBrowserView.FILE_ITEM_ID:
				FileInfo info = (FileInfo)view.getTag();
				onCoverClick(info, info.isDirectory());
				break;
		}
	}

	public void onCoverClick(FileInfo info, boolean noDetails)
	{
		if (info == null)
		{
			info = m_bookLibrary.getParentItem();
		}

		boolean same = m_mode != MODE_LIST && m_fileInfoView.getCurrentInfo() == info;

		boolean folder = false;
		if (noDetails || same)
		{			
			if (m_bookLibrary.onSelect(info))
			{				
				if (m_bookLibrary.isInLibrary() && info.getItemSize() == 0)
					m_bookLibrary.scan(true);
				
				folder = true;
				m_fileInfoView.showInfo(info);
			} else
			{
				if (m_mode == MODE_COVERS)
					m_coverView.advanceVersion();
			}

			
			if (m_bookLibrary.isInRecent() || folder)
				refresh(false);
				//m_coverView.setCovers(m_bookLibrary.getItems(), !m_bookLibrary.isRoot() ? isNook || isEpad || isNookTouch || isEmulator || isLegacyAndroid ? View.VISIBLE : View.INVISIBLE : View.GONE, m_cacheLocation, m_bookLibrary.getReturnIndex());
		} else
		{
			m_fileInfoView.showInfo(info);
			if (m_mode == MODE_LIST)
			{
				if (!BaseActivity.isXLarge)
				{
					if (!BaseActivity.isNookTouch && BaseActivity.SDKVersion > 4)
					{
						Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
						m_infoContainer.startAnimation(animation);
					}
					m_infoContainer.setVisibility(View.VISIBLE);
					
					if (!BaseActivity.isNookTouch && BaseActivity.SDKVersion > 4)
					{
						Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
						m_listContainer.startAnimation(animation);
					}
					m_listContainer.setVisibility(View.GONE);
				} else
					m_fileBrowserView.setSelected(info);					
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		m_coverView.advanceVersion();
		MetaDataFactory.flushDB();
		m_bookLibrary.saveSettings();
		/*DBSettings settings = new DBSettings(this, true);
		settings.putInt("lastApplication", 0);
		settings.close();*/
		
		//DirectDraw.cancelInvalidate();
		//finish(); // do not work in background mode at all
	}
	
	public void refresh(boolean firstPage)
	{
		switch(m_mode)
		{
			case MODE_LIST:
				m_fileBrowserView.setInfo(m_bookLibrary.getCurrentInfo(), m_cacheLocation, firstPage ? 0 : m_bookLibrary.getReturnIndex());
				if (m_bookLibrary != null)
					m_fileInfoView.showInfo(m_bookLibrary.getCurrentInfo());
				break;
			case MODE_COVERS:
			default:
				m_coverView.setInfo(m_bookLibrary.getCurrentInfo(), !m_bookLibrary.isRoot() ? /*isNook || isEpad || isNookTouch || isEmulator || isLegacyAndroid ? */View.VISIBLE /*: View.INVISIBLE*/ : View.GONE, m_cacheLocation, firstPage ? 0 : m_bookLibrary.getReturnIndex());
				if (m_bookLibrary != null)
					m_fileInfoView.showInfo(m_bookLibrary.getCurrentInfo());
				break;
		}
	}
	
	private void continueReading()
	{
		DBSettings settings = new DBSettings(this);
		String book = settings.getString("readingNow", "");
		settings.close();
		
		if (book == null || book.length() == 0 || !(new File(book).exists()))
		{
			Toast.makeText(this, getResources().getString(R.string.no_book), Toast.LENGTH_LONG).show();
		} else
		{				
			Intent intent = new Intent();
			intent.setClassName("net.runserver.textReader", "net.runserver.textReader.TextReader");
			intent.setData(Uri.parse("file://" + book));
			//startActivityForResult(intent, REQUEST_INFORM_CLOSE);
			startActivity(intent);
			terminate();
			//finish(); // close after opening reader
		}
	}

	@Override
	protected void onResume()
	{
		if (m_bookLibrary.refresh())
		{
			m_fileBrowserView.post(new Runnable()
			{
				@Override
				public void run()
				{
					refresh(false);
				}
			});
		}

		if (m_coverView != null && m_mode == MODE_COVERS)
			m_coverView.onResume();
		
		try
		{
			DBSettings settings = new DBSettings(this, true);
			m_lastBook = settings.getString("readingNow", "");
			settings.putInt("lastApplication", 0);
			settings.close();
		}
		catch(Exception ex)
		{
			Log.d("FileBrowser", "Problem saving settings database: " + ex);
		}
		
		
		updateMenu();
		
		super.onResume();
	}

	@Override
	public boolean onMenu(MenuItem item)
	{
		int itemId = item.getId();
		boolean hide = false;
		boolean update = false;

		switch (itemId)
		{			
			// show filter
			case R.id.show_all:
				m_bookLibrary.setShowMode(Library.ALL_FILES);
				hide = true;
				update = true;
				break;
			case R.id.show_books:
				m_bookLibrary.setShowMode(Library.BOOKS);
				hide = true;
				update = true;
				break;
			case R.id.show_documents:
				m_bookLibrary.setShowMode(Library.DOCUMENTS);
				hide = true;
				update = true;
				break;

			// sort
			case R.id.sort_author:
				m_bookLibrary.setSortMode(Library.SORT_AUTHOR);
				hide = true;
				update = true;
				break;
			case R.id.sort_most_recent:
				m_bookLibrary.setSortMode(Library.SORT_DATE);
				hide = true;
				update = true;
				break;
			case R.id.sort_title:
				m_bookLibrary.setSortMode(Library.SORT_NAME);
				hide = true;
				update = true;
				break;
			case R.id.sort_series:
				m_bookLibrary.setSortMode(Library.SORT_SERIES);
				hide = true;
				update = true;
				break;

			// hidden files
			case R.id.hidden_hide:
				m_bookLibrary.setShowHidden(false);
				hide = true;
				update = true;
				break;
			case R.id.hidden_show:
				m_bookLibrary.setShowHidden(true);
				hide = true;
				update = true;
				break;

			// empty folders
			case R.id.empty_hide:
				m_bookLibrary.setShowEmpty(false);
				hide = true;
				update = true;
				break;
			case R.id.empty_show:
				m_bookLibrary.setShowEmpty(true);
				hide = true;
				update = true;
				break;
				
			// cache
			case R.id.thumbs_external:
				m_bookLibrary.setCacheMode(Library.CACHE_EXTERNAL);
				updateCacheLocation(false);
				hide = true;
				update = true;
				break;
			case R.id.thumbs_internal:
				m_bookLibrary.setCacheMode(Library.CACHE_INTERNAL);
				updateCacheLocation(false);
				hide = true;
				update = true;
				break;
			case R.id.thumbs_near:
				m_bookLibrary.setCacheMode(Library.CACHE_NEAR);
				updateCacheLocation(false);
				hide = true;
				update = true;
				break;
		}

		if (update)
		{
			updateMenu();
			m_bookLibrary.saveSettings();
			refresh(true);
		}

		return hide;	
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.main_refresh:
				refresh(true);
				break;
			case R.id.main_exit:				
				terminate();
				break;
			case R.id.main_scan:
				m_bookLibrary.scan(true);
				break;
			case R.id.main_continue:				
				continueReading();
				break;
			case R.id.main_settings:
				showMenu();
				break;
		}
		return true;
	}
	

	@Override
	public boolean onMenuBack(int menuId)
	{
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				if (m_menuDialog.getVisibility() == View.VISIBLE)
				{
					if (m_menuView.isRoot())
						hideMenu();
					else
						m_menuView.goBack();
					return false;
				}
				
				if (!BaseActivity.isXLarge && m_mode == MODE_LIST && m_listContainer.getVisibility()  == View.GONE)
				{
					if (!BaseActivity.isNookTouch && BaseActivity.SDKVersion > 4)
					{
						Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
						m_infoContainer.startAnimation(animation);
					}
					m_infoContainer.setVisibility(View.GONE);
					
					if (!BaseActivity.isNookTouch && BaseActivity.SDKVersion > 4)
					{
						Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
						m_listContainer.startAnimation(animation);
					}
					
					m_listContainer.setVisibility(View.VISIBLE);
					return false;
				}
			
				if (!m_bookLibrary.isRoot())
				{
					onCoverClick(null, true);				
					return false;
				}
				break;
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_SPACE:
				if (m_menuDialog.getVisibility() == View.VISIBLE)
				{
					hideMenu();
					return false;
				}
				return false;
			case KEYCODE_PAGE_TOPLEFT:
			case KEYCODE_PAGE_TOPRIGHT:
			case KeyEvent.KEYCODE_DPAD_LEFT:
				switch (m_mode)
				{
					case MODE_LIST:
						m_fileBrowserView.prevPage();
						break;
					case MODE_COVERS:
						m_coverView.prevPage();
						break;
				}
				break;
			case KEYCODE_PAGE_BOTTOMRIGHT:
			case KEYCODE_PAGE_BOTTOMLEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				switch (m_mode)
				{
					case MODE_LIST:
						m_fileBrowserView.nextPage();
						break;
					case MODE_COVERS:
						m_coverView.nextPage();
						break;
				}
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void updateMenu()
	{
		// empty
		boolean showEmpty = m_bookLibrary.getShowEmpty();
		m_menuView.setMenuChecked(R.id.empty_show, showEmpty);
		m_menuView.setMenuChecked(R.id.empty_hide, !showEmpty);
		m_menuView.setMenuValue(R.id.show_empty, showEmpty ? R.string.show : R.string.hide);

		// hidden
		boolean showHidden = m_bookLibrary.getShowHidden();
		m_menuView.setMenuChecked(R.id.hidden_show, showHidden);
		m_menuView.setMenuChecked(R.id.hidden_hide, !showHidden);
		m_menuView.setMenuValue(R.id.show_hidden, showHidden ? R.string.show : R.string.hide);

		// show mode
		int showMode = m_bookLibrary.getShowMode();
		m_menuView.setMenuChecked(R.id.show_all, showMode == Library.ALL_FILES);
		m_menuView.setMenuChecked(R.id.show_documents, showMode == Library.DOCUMENTS);
		m_menuView.setMenuChecked(R.id.show_books, showMode == Library.BOOKS);

		switch (showMode)
		{
			case Library.ALL_FILES:
				m_menuView.setMenuValue(R.id.show_show, R.string.all_files);
				break;
			case Library.DOCUMENTS:
				m_menuView.setMenuValue(R.id.show_show, R.string.documents_only);
				break;
			case Library.BOOKS:
				m_menuView.setMenuValue(R.id.show_show, R.string.books_only);
				break;
		}

		
		// sort mode
		int sortMode = m_bookLibrary.getSortMode();
		m_menuView.setMenuChecked(R.id.sort_author, sortMode == Library.SORT_AUTHOR);
		m_menuView.setMenuChecked(R.id.sort_title, sortMode == Library.SORT_NAME);
		m_menuView.setMenuChecked(R.id.sort_most_recent, sortMode == Library.SORT_DATE);
		m_menuView.setMenuChecked(R.id.sort_series, sortMode == Library.SORT_SERIES);

		switch (sortMode)
		{
			case Library.SORT_AUTHOR:
				m_menuView.setMenuValue(R.id.show_sort, R.string.author);
				break;
			case Library.SORT_NAME:
				m_menuView.setMenuValue(R.id.show_sort, R.string.title);
				break;
			case Library.SORT_DATE:
				m_menuView.setMenuValue(R.id.show_sort, R.string.most_recent);
				break;
			case Library.SORT_SERIES:
				m_menuView.setMenuValue(R.id.show_sort, R.string.series);
				break;
		}
		
		// cache mode
		int cacheMode = m_bookLibrary.getCacheMode();
		m_menuView.setMenuChecked(R.id.thumbs_external, cacheMode == Library.CACHE_EXTERNAL);
		m_menuView.setMenuChecked(R.id.thumbs_internal, cacheMode == Library.CACHE_INTERNAL);
		m_menuView.setMenuChecked(R.id.thumbs_near, cacheMode == Library.CACHE_NEAR);

		switch (cacheMode)
		{
			case Library.CACHE_EXTERNAL:
				m_menuView.setMenuValue(R.id.save_thumbs, R.string.thumbs_external);
				break;
			case Library.CACHE_INTERNAL:
				m_menuView.setMenuValue(R.id.save_thumbs, R.string.thumbs_internal);
				break;
			case Library.CACHE_NEAR:
				m_menuView.setMenuValue(R.id.save_thumbs, R.string.thumbs_near);
				break;
		}
		
		//m_menuView.setMenuEnabled(R.id.main_continue, m_lastBook != null && m_lastBook.length() > 0 && new File(m_lastBook).exists());
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		MetaDataFactory.closeDB();

		unbindDrawables(findViewById(R.id.root));
		System.gc();
	}

	private void unbindDrawables(View view)
	{
		if (view == null)
			return;

		if (view.getBackground() != null)
			view.getBackground().setCallback(null);
		
		if (view instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup)view;
			for (int i = 0; i < group.getChildCount(); i++)
				unbindDrawables(group.getChildAt(i));
			
			group.removeAllViews();
		}
	}
	
	public void onChangeMode(int mode)
	{
		switch(mode)
		{
			case MODE_LIST:
				if (m_mode == MODE_COVERS && !BaseActivity.isNookTouch  && BaseActivity.SDKVersion > 4)
				{
					if (BaseActivity.isXLarge)
					{
					} else 
					{
						{
							Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
							m_infoContainer.startAnimation(animation);
						}
						{
							Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
							m_listContainer.startAnimation(animation);
						}
						{
							Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
							m_coversContainer.startAnimation(animation);
						}
					}
				}
				
				m_coversContainer.setVisibility(View.GONE);
				m_listContainer.setVisibility(View.VISIBLE);
				m_fileBrowserView.setFocusable(true);
				
				//m_fileBrowserView.setDrawingCacheEnabled(true);
				//m_infoContainer.setDrawingCacheEnabled(true);
				//m_coverView.setDrawingCacheEnabled(true);
				
				m_coverView.setFocusable(false);
				
				if (BaseActivity.isXLarge)
				{
					m_infoContainer.setVisibility(View.VISIBLE);
					m_fileBrowserView.setSelected(m_fileInfoView.getCurrentInfo());
				} else
				{
					m_infoContainer.setVisibility(View.GONE);
				}

				break;
			case MODE_COVERS:
			default:
				if (m_mode == MODE_LIST && !BaseActivity.isNookTouch && !BaseActivity.isXLarge && BaseActivity.SDKVersion > 4)
				{
					if (BaseActivity.isXLarge)
					{
					} else
					{					
						{
							Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
							m_infoContainer.startAnimation(animation);
						}
						{
							Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
							m_listContainer.startAnimation(animation);
						}
						{
							Animation animation  = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
							m_coversContainer.startAnimation(animation);
						}
					}
				}
				
				//m_fileBrowserView.setDrawingCacheEnabled(false);
				//m_infoContainer.setDrawingCacheEnabled(false);
				//m_coverView.setDrawingCacheEnabled(false);

				m_coversContainer.setVisibility(View.VISIBLE);
				m_infoContainer.setVisibility(View.VISIBLE);
				m_listContainer.setVisibility(View.GONE);
				m_fileBrowserView.setFocusable(false);
				m_coverView.setFocusable(true);
				break;
		}
		if (m_mode == MODE_NONE)
			m_mode = mode;
		else
		{
			m_mode = mode;
			refresh(false);
		}
	}
}
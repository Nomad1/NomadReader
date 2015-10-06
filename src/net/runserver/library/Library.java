package net.runserver.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import net.runserver.common.BaseActivity;
import net.runserver.common.DBSettings;
import net.runserver.library.metaData.MetaData;
import net.runserver.library.metaData.MetaDataFactory;
import net.runserver.textReader.R;

public abstract class Library extends ResourceHelper
{
	// static
	public static final int ALL_FILES = 0;
	public static final int DOCUMENTS = 1;
	public static final int BOOKS = 2;

	public static final int SORT_NAME = 0;
	public static final int SORT_DATE = 1;
	public static final int SORT_AUTHOR = 2;
	public static final int SORT_SERIES = 3;

	public static final int CACHE_INTERNAL = 0;
	public static final int CACHE_EXTERNAL= 1;
	public static final int CACHE_NEAR = 2;
	
	public static final String s_recentDocumentsFile = ".net.runserver.fileBrowser.recent";
	public static final String s_libraryFile = ".net.runserver.fileBrowser.library";
	public static final String s_libraryAuthorFile = ".net.runserver.fileBrowser.library.author.";
	
	// variables	
	protected FileInfo m_currentInfo;	
	protected FileInfo m_libraryInfo;
	
	// settings
	protected boolean m_showHidden;
	protected boolean m_showEmpty;
	protected int m_showMode;
	protected int m_sortMode;
	protected int m_cacheMode;
	protected String m_currentFolder;
	protected List<FileInfo> m_recentDocuments;
	protected String m_recentDocumentsString;
	
	private int m_returnIndex;
	
	//protected abstract void showDirectory(FileInfo currentInfo, int page, boolean reload, boolean resort);
	public abstract boolean refresh();
	
	public FileInfo getCurrentInfo()
	{
		return m_currentInfo;
	}
	
	public int getReturnIndex()
	{
		return m_returnIndex;
	}
	
	public boolean isInRecent()
	{
		return m_currentInfo != null && m_currentInfo.getPath().equals(s_recentDocumentsFile);		
	}	
	
	public boolean isInLibrary()
	{
		return m_currentInfo != null && m_currentInfo.getPath().equals(s_libraryFile);		
	}
	
	public FileInfo getParentItem()
	{
		return m_currentInfo != null ? m_currentInfo.getParent() : null;
	}
	
	public boolean isRoot()
	{
		return m_currentInfo.getParent() == null;
	}
	
	public void setSortMode(int value)
	{
		m_sortMode = value;
		refresh();
	}

	public int getSortMode()
	{
		return m_sortMode;
	}

	public void setShowHidden(boolean value)
	{		
		m_showHidden = value;
		refresh();
	}

	public boolean getShowHidden()
	{
		return m_showHidden;
	}

	public void setShowEmpty(boolean value)
	{
		m_showEmpty = value;
		refresh();
	}

	public boolean getShowEmpty()
	{
		return m_showEmpty;
	}

	public void setCacheMode(int value)
	{
		m_cacheMode = value;
		refresh();
	}

	public int getCacheMode()
	{
		return m_cacheMode;
	}

	public void setShowMode(int value)
	{
		m_showMode = value;
		refresh();
	}

	public int getShowMode()
	{
		return m_showMode;
	}

	public Library(Context context)
	{
		super(context);
	}
	
	protected void init()
	{
		FileInfo root = new FileInfo("", resString(R.string.your_device), resString(R.string.your_device_desc), "", null);
		
		if (m_currentFolder != null && m_currentFolder != "")
		{
			Log.d("FileBrowser", "Current folder is " + m_currentFolder);

			String[] paths = m_currentFolder.split(":");
			FileInfo parent = root;
			m_currentInfo = null;
			if (paths.length > 0)
				for (int i = paths.length - 1; i >= 0; i--)
				{
					List<FileInfo> parentInfos = getFiles(parent);					
					
					boolean found = false;
					if (parentInfos != null)
						for(FileInfo info: parentInfos)
							if (info.getPath().equals(paths[i]))
							{
								parent = info;
								found = true;
								break;
							}
					
					if (!found)
						break;
					/*File file = new File(paths[i]);
					
					Log.d("FileBrowser", "Checking path " + paths[i]);
					if (file.exists() && file.isDirectory())
					{
						parent = new FileInfo(file, file.getName(), "", parent, null, 0);
					} else
						break;*/
				}

			showDirectory(parent, 0, true, true);
		} else
			showDirectory(root, 0, true, true);
	}
	
	protected void showDirectory(FileInfo currentInfo, int page, boolean reload, boolean resort)
	{
		if (currentInfo.getFiles() != null && reload && currentInfo.getPath().startsWith(s_libraryFile) )
			reload = false;
		else		
			if (!reload && currentInfo.getFiles() == null)
				reload = true;
		
		List<FileInfo> files = reload ? getFiles(currentInfo) : currentInfo.getFiles();

		if (currentInfo.getPath() != "" && (resort || reload))
		{
			switch (m_sortMode)
			{
				case SORT_NAME:
					Collections.sort(files, NameComparator.Instance);
					break;
				case SORT_DATE:
					Collections.sort(files, DateComparator.Instance);
					break;
				case SORT_AUTHOR:
					Collections.sort(files, AuthorComparator.Instance);
					break;
				case SORT_SERIES:
					Collections.sort(files, SeriesComparator.Instance);
					break;
			}	
		}

		boolean findOld = m_currentInfo != null && m_currentInfo != currentInfo && m_currentInfo.getParent() != null
				&& m_currentInfo.getParent().getPath().equals(currentInfo.getPath());

		m_returnIndex = 0;
		
		if (findOld)			
			for(int i=0;i<files.size();i++)
				if (files.get(i).getPath().equals(m_currentInfo.getPath()))
				{
					m_returnIndex = i;
				}
		
		currentInfo.setFiles(files);
		currentInfo.setItemSize(files.size());
		
		m_currentInfo = currentInfo;
				
		((Activity)getContext()).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				MetaDataFactory.flushDB();				
			}			
		});
	}

	
	protected void loadSettings()
	{
		DBSettings settings = new DBSettings(getContext());
		m_showHidden = settings.getBoolean("showHidden", false);
		m_showEmpty = settings.getBoolean("showEmpty", false);
		m_showMode = settings.getInt("showMode", BOOKS);
		m_sortMode = settings.getInt("sortMode", SORT_AUTHOR);
		m_cacheMode = settings.getInt("cacheMode", CACHE_INTERNAL);

		m_currentFolder = settings.getString("currentFolder", "");

		m_recentDocumentsString = settings.getString("recentDocuments", "");
		settings.close();
	}
	
	public void saveSettings()
	{
		try
		{
			DBSettings settings = new DBSettings(getContext(), true); 
			settings.putBoolean("showHidden", m_showHidden);
			settings.putBoolean("showEmpty", m_showEmpty);
			settings.putInt("showMode", m_showMode);
			settings.putInt("sortMode", m_sortMode);
			settings.putInt("cacheMode", m_cacheMode);
	
			StringBuilder path = new StringBuilder(m_currentInfo.getPath());
			FileInfo info = m_currentInfo.getParent();
			while (info != null)
			{
				path.append(':');
				path.append(info.getPath());
				info = info.getParent();
			}
	
			settings.putString("currentFolder", path.toString());
	
			if (m_recentDocuments != null)
			{
				StringBuilder recentDocuments = new StringBuilder();
				for (int i = 0; i < m_recentDocuments.size(); i++)
				{
					if (i != 0)
						recentDocuments.append(':');
		
					recentDocuments.append(m_recentDocuments.get(i).getPath());
				}
		
				settings.putString("recentDocuments", recentDocuments.toString());
			}
			//Log.d("FileBrowser", "Saving recentDocuments: " + path);
	
			settings.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	protected List<FileInfo> getFiles(FileInfo currentInfo)
	{
		if (currentInfo.getPath() == "")
		{
			List<FileInfo> result = new ArrayList<FileInfo>(6);
			if (Utils.pathExists("/system/media/sdcard/my documents"))
				result.add(new FileInfo(new File("/system/media/sdcard/my documents"), resString(R.string.my_documents),
						resString(R.string.my_documents_desc), resString(R.string.my_documents_desc_short), currentInfo, null, 0));
			else
			if (Utils.pathExists("/sdcard/my documents"))
				result.add(new FileInfo(new File("/sdcard/my documents"), resString(R.string.my_documents),
						resString(R.string.my_documents_desc),  resString(R.string.my_documents_desc_short), currentInfo, null, 0));
			
			if (BaseActivity.isNook)
				result.add(new FileInfo(new File("/system/media/sdcard"), resString(R.string.internal_memory),
					resString(R.string.internal_memory_desc), resString(R.string.internal_memory_desc_short), currentInfo, null, 0));

			if (BaseActivity.isNookTouch)
				result.add(new FileInfo(new File("/media/"), resString(R.string.internal_memory),
					resString(R.string.internal_memory_desc), resString(R.string.internal_memory_desc_short), currentInfo, null, 0));

			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) || android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY) )
				result.add(new FileInfo(/*new File("/sdcard")*/ android.os.Environment.getExternalStorageDirectory(), resString(R.string.external_card), resString(R.string.external_card_desc),
						resString(R.string.external_card_desc_short), currentInfo, null, 0));

			//if (m_recentDocuments != null || (m_recentDocumentsString != null && m_recentDocumentsString.length() > 0))
				result.add(new FileInfo(s_recentDocumentsFile, resString(R.string.recent_documents), resString(R.string.recent_documents_desc), resString(R.string.recent_documents_desc_short),
						currentInfo));

//			if (m_showHidden || result.size() <= 1)
				result.add(m_libraryInfo = new FileInfo(s_libraryFile, resString(R.string.library_books), resString(R.string.library_books_desc), resString(R.string.library_books_desc_short),
						currentInfo));
			
				if (m_showHidden || result.size() <= 2)
					result.add(new FileInfo(new File("/"), resString(R.string.device_root), resString(R.string.device_root_desc), resString(R.string.device_root_desc_short), currentInfo,
							null, 0));

			return result;
		}
		
		if (currentInfo.getPath().equals(s_recentDocumentsFile))
		{
			if (m_recentDocuments == null)
			{
				String [] paths = m_recentDocumentsString.split(":");
				m_recentDocuments = new ArrayList<FileInfo>(paths.length);
				
				for (int i = 0; i<paths.length;i++)
				{
					File file = new File(paths[i]);
	
					if (file.exists() && file.getName() != null)
					{
						try
						{
							m_recentDocuments.add(getFileInfo(file, currentInfo, null, 0));
						}
						catch(Exception ex)
						{
							Log.e("FileBrowser", "Failed to get recent file info " + ex);
						}
					}
				}
			}
			
			return m_recentDocuments;
		}
		
		if (currentInfo.getPath().equals(s_libraryFile))
		{
			List<MetaData> books = MetaDataFactory.getAllBooks();
			
			if (books == null || books.size() == 0)
				return new ArrayList<FileInfo>(0);
			/*for (int i = 0; i<books.size(); i++)
				result.add(getFileInfo(books.get(i).second, currentInfo, books.get(i).first, 0));*/
			
			//Map<String, List<MetaData>> authors = new HashMap<String, List<MetaData>>(books.size()/10);
			Map<String, FileInfo> authors = new HashMap<String, FileInfo>(books.size()/3);
			
			for (int i = 0; i<books.size(); i++)
			{
				String author = books.get(i).getAuthorSortName();
				if (author == null || author.length() == 0)
					author = "Other";
				
				FileInfo info = authors.get(author);
				if (info == null)
				{
					info = new FileInfo(s_libraryAuthorFile + author, author, "", books.get(i).getAuthor(), currentInfo);
					info.setItemSize(1);
					authors.put(author, info);
				}
				else
				{
					info.setItemSize(info.getItemSize() + 1);
					//authors.get(author).set + 1);
				}
			}
			
			List<FileInfo> result = new ArrayList<FileInfo>(authors.size());
			for(FileInfo info: authors.values())
			{
				info.setLongInfo(resStringFormat(R.string.folder_info_template, info.getItemSize()));
				result.add(info);
			}			
			
			return result;
		}
		
		if (currentInfo.getPath().startsWith(s_libraryAuthorFile))
		{
			String nauthor = currentInfo.getPath().substring(s_libraryAuthorFile.length());
			
			List<MetaData> books = MetaDataFactory.getAllAuthorBooks(nauthor, (int)currentInfo.getItemSize());							
						
			List<FileInfo> result;
			
			if (books == null || books.size() == 0)
				return new ArrayList<FileInfo>(0);
			
				result = new ArrayList<FileInfo>(books.size()); 
			
			for (int i = 0; i<books.size(); i++)
			{
				try
				{
					result.add(getFileInfo(new File(books.get(i).getLastPath()), currentInfo, books.get(i), 0));
				}
				catch(Exception ex)
				{
					Log.e("FileBrowser", "Failed to get file info " + ex);
				}
			}
			
			return result;
		}
		
		File dir = new File(currentInfo.getPath());
		if (dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			List<FileInfo> result;
			if (files == null || files.length == 0)
			{
				result = new ArrayList<FileInfo>(0);
				currentInfo.setItemSize(0);				
			}
			else
			{
				result = new ArrayList<FileInfo>(files.length);
				//currentInfo.setItemSize(files.length);
				
				int oldSize = 0;
	
				for (int i = 0; i < files.length; i++)
				{
					File file = files[i];
					
					int oldValid = isValidItem(file, false);
					
					if (oldValid == -1)
						continue;
					
					oldSize++;
	
					int valid = isValidItem(file, true);
					if (valid == -1)
						continue;
	
					try
					{
						result.add(getFileInfo(file, currentInfo, null, valid));
					}
					catch(Exception ex)
					{
						Log.e("FileBrowser", "Failed to get file info " + ex);
					}
				}
				currentInfo.setItemSize(oldSize);
			}
			return result;
		}

		return null;
	}

	private int isValidItem(File file, boolean recursive)
	{
		if (!m_showHidden && file.isHidden())
			return -1;

		if (file.isDirectory())
		{
			if (!recursive)
				return 0;	
			
			File[] files = file.listFiles();
			
			if (files == null)
				return -1;
			
			if (files.length == 0 && !m_showEmpty)
				return -1;
			
			int visibleFiles = 0;
			
			for(int i=0;i<files.length;i++)
				{
					if (isValidItem(files[i], false) != -1)
						visibleFiles++;						
				}
			
			if (visibleFiles == 0 && !m_showEmpty)
				return -1;
			
			return visibleFiles;
				
				/*
				 * for(int i=0;i<files.length;i++) if (isValidItem(files[i]))
				 * return true;
				 */
			/*} else
				return files.length;*/

		} else
		{
			if (m_showMode == ALL_FILES)
				return 0;

			String extension = Utils.getExtension(file.getName());

			switch (m_showMode)
			{
				case DOCUMENTS:
					if (!Utils.isDocument(extension))
						return -1;
					break;
				case BOOKS:
					if (!Utils.isBook(extension))
						return -1;
					break;
			}
		}
		return 0;
	}
	
	private FileInfo getFileInfo(File file, FileInfo currentInfo, MetaData metaData, int valid)
	{
		FileInfo info;
		if (file.isDirectory())
		{
			//File [] fileList = file.listFiles(); 
			int items = valid;//fileList == null ? 0 : fileList.length;
			String folderInfo = resStringFormat(R.string.folder_info_template, items);
			info = new FileInfo(file, file.getName(), folderInfo, folderInfo, currentInfo, null, items);
		} else
		{
			String extension = Utils.getExtension(file.getName());

			if (metaData == null)
				metaData = MetaDataFactory.getMetaData(Utils.getMimeType(extension), file);

			String name;
			String desc;

			if (metaData == null)
			{
				name = file.getName();
				desc = resStringFormat(R.string.file_info_template, extension, Utils.formatByteAmount(file.length()));
			} else
			{
				name = metaData.getTitle();
				
				StringBuilder ndesc = new StringBuilder();

				if (metaData.getAuthor() != null && metaData.getAuthor().length() > 0)
					ndesc.append(metaData.getAuthor());
				else
					ndesc.append(resString(R.string.unknown_author));

				if (metaData.getSeries() != null && metaData.getSeries().length() > 0)
				{
					ndesc.append(". ");
					ndesc.append(metaData.getSeries());
					ndesc.append(" - ");
					ndesc.append(metaData.getPart());
				}
				desc = ndesc.toString();
			}

			info = new FileInfo(file, name, desc, desc, currentInfo, metaData, (int)file.length());
		}
		return info;
	}

	protected void addRecentDocument(FileInfo info)
	{
		if (m_recentDocuments == null)
			m_recentDocuments = new ArrayList<FileInfo>();
			
		String fileName = new File(info.getPath()).getName(); 
		for(int i=0;i<m_recentDocuments.size();i++)
		{
			FileInfo recentDocument = m_recentDocuments.get(i);
			
			if (recentDocument == info || new File(recentDocument.getPath()).getName().equals(fileName))
			{
				m_recentDocuments.remove(i);
				break;
			}
		}
		
		m_recentDocuments.add(0, info);
		
		if (m_recentDocuments.size() > 9)
			m_recentDocuments.remove(m_recentDocuments.size() - 1);
		
		saveSettings();
		
		if (isInRecent())
			refresh();			
	}
	
	public boolean onSelect(FileInfo info)
	{
		if (info == null)
			return true;
		
		if (info.isDirectory())
		{
			showDirectory(info, 0, true, true);
			saveSettings();
			return true;
		}

		//DirectDraw.cancelInvalidate();
		
		String extension = Utils.getExtension(info.getPath());
		String mimeType = Utils.getMimeType(extension);
		
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setDataAndType(Uri.parse("file://" + info.getPath()), mimeType);

		if (BaseActivity.isNook)
		{
			intent.setAction("com.bravo.intent.action.VIEW");
			if (Utils.isBook(extension))
				updateReadingNow(intent);
			try
			{
				getContext().startActivity(intent);

				if (Utils.isDocument(extension))
					addRecentDocument(info);

				/*if (info.getName().equals("nookFileBrowser.apk") || info.getName().equals("net.runserver.fileBrowser.apk"))
			{
				((Activity)getContext()).finish();
			}*/
				return false;
			}
			catch (ActivityNotFoundException ex)
			{
				Log.e("FileBrowser", "Could not open file " + info.getPath() + ": " + ex);
			}
		}

		//intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		//intent.setDataAndType(Uri.parse("file://" + info.getPath()), mimeType);

		if (Utils.isBook(extension))
			updateReadingNow(intent);

		try
		{
			getContext().startActivity(intent);
			
			if (Utils.isDocument(extension))
				addRecentDocument(info);
			
			((BaseActivity)getContext()).terminate();
			return false;
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "Could not open file " + info.getPath() + ": " + ex);
			Toast.makeText(getContext(), resString(R.string.no_application) , Toast.LENGTH_LONG).show();
		}
		
		return true;
	}	

	protected void updateReadingNow(Intent intent)
	{
		// TODO: update it on Nook
	}
	
	protected static class NameComparator implements Comparator<FileInfo>
	{
		public static Comparator<FileInfo> Instance = new NameComparator();

		@Override
		public int compare(FileInfo one, FileInfo another)
		{
			if (one.isDirectory() != another.isDirectory())
			{
				return one.isDirectory() ? -1 : 1;
			}
			
			String oneName = one.getName();
			String anotherName = another.getName();
			
			if ((oneName != null) != (anotherName != null))
			{
				return oneName != null ? -1 : 1;
			}			
			
			if (oneName == null)
			{
				return 0;
			}
			
			return oneName.compareTo(anotherName);
		}
	}
	
	protected static class DateComparator implements Comparator<FileInfo>
	{
		public static Comparator<FileInfo> Instance = new DateComparator();

		@Override
		public int compare(FileInfo one, FileInfo another)
		{
			if (one.isDirectory() != another.isDirectory())
			{
				return one.isDirectory() ? -1 : 1;
			}

			return new Long(one.getLastModification()).compareTo(another.getLastModification());
		}
	}
	
	protected static class AuthorComparator implements Comparator<FileInfo>
	{
		public static Comparator<FileInfo> Instance = new AuthorComparator();

		@Override
		public int compare(FileInfo one, FileInfo another)
		{
			if (one.isDirectory() != another.isDirectory())
			{
				return one.isDirectory() ? -1 : 1;
			}
			
			if (one.isDirectory())
			{
				return NameComparator.Instance.compare(one, another);
			}
			
			if ((one.getMetaData() != null) != (another.getMetaData() != null))
			{
				return one.getMetaData() != null ? -1 : 1;
			}
			
			if (one.getMetaData() == null)
			{
				return NameComparator.Instance.compare(one, another);
			}

			String oneAuthor = one.getMetaData().getAuthorSortName();
			String anotherAuthor = another.getMetaData().getAuthorSortName();
			
			if ((oneAuthor != null) != (anotherAuthor != null))
			{
				return oneAuthor != null ? -1 : 1;
			}
			
			if (oneAuthor == null)
			{
				return NameComparator.Instance.compare(one, another);
			}
			
			if (oneAuthor.equalsIgnoreCase(anotherAuthor))
				return SeriesComparator.Instance.compare(one, another);

			
			return oneAuthor.compareToIgnoreCase(anotherAuthor);
		}
	}
	
	protected static class SeriesComparator implements Comparator<FileInfo>
	{
		public static Comparator<FileInfo> Instance = new SeriesComparator();

		@Override
		public int compare(FileInfo one, FileInfo another)
		{
			if (one.isDirectory() != another.isDirectory())
			{
				return one.isDirectory() ? -1 : 1;
			}
			
			if (one.isDirectory())
			{
				return NameComparator.Instance.compare(one, another);
			}
			
			if ((one.getMetaData() != null) != (another.getMetaData() != null))
			{
				return one.getMetaData() != null ? -1 : 1;
			}
			
			if (one.getMetaData() == null)
			{
				return NameComparator.Instance.compare(one, another);
			}
			
			String oneSeries = one.getMetaData().getSeries();
			String anotherSeries = another.getMetaData().getSeries();
			
			if ((oneSeries != null) != (anotherSeries != null))
			{
				return oneSeries != null ? -1 : 1;
			}
			
			if (oneSeries == null)
			{
				return NameComparator.Instance.compare(one, another);
			}
			
			if (oneSeries.equals(anotherSeries))
				return new Integer(one.getMetaData().getPart()).compareTo(another.getMetaData().getPart());
			
			return oneSeries.compareToIgnoreCase(anotherSeries);
		}
	}
}
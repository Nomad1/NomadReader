package net.runserver.fileBrowser;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import net.runserver.library.FileInfo;
import net.runserver.library.Library;
import net.runserver.library.Utils;
import net.runserver.library.metaData.MetaDataFactory;
import net.runserver.textReader.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class BookLibrary extends Library
{
	private boolean m_inited;
	
	public List<FileInfo> getItems()
	{
		return m_currentInfo.getFiles();
	}

	public BookLibrary(Context context)
	{
		super(context);		

		loadSettings();
	}
	
	public boolean refresh()
	{
		if (!m_inited)
		{
			m_inited = true;
			init();
			return true;
		}
		
		showDirectory(m_currentInfo, 0, true, true);
		return false;
	}
	
	public void scan(boolean gotoLibrary)
	{
		BookSeekTask seek = new BookSeekTask(getContext(), gotoLibrary, "/sdcard", "/media");
		seek.execute();
	}
	
	
	private void scanFinished(boolean gotoLibrary)
	{
		if (gotoLibrary)
		{
			m_libraryInfo.setFiles(null);			
			showDirectory(m_libraryInfo, 0, true, true);
			((FileBrowser)getContext()).refresh(true); // ugly ((
		}
	}
	
	private class BookSeekTask extends AsyncTask<Void, String, Void>
	{
		private final String [] m_roots;
		private ProgressDialog m_progressDialog;
		private final boolean m_gotoLibrary;
		private final String m_processing;
		private final String m_scanning;
		private final Context m_context;
		private int m_maxCount;
		private int m_count;

		public BookSeekTask(Context context, boolean gotoLibrary, String ... roots)
		{
			m_roots = roots;
			m_gotoLibrary = gotoLibrary;
			m_context = context;
			m_processing = context.getString(R.string.scan_processing);
			m_scanning = context.getString(R.string.scan_scanning);
		}
		
		@Override
		protected void onPreExecute()
		{
			m_progressDialog = new ProgressDialog( m_context );
			m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			m_progressDialog.setTitle(m_context.getString(R.string.scan_title));
			m_progressDialog.setMessage(m_scanning + "...");
			m_progressDialog.show();
		
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0)
		{			
			List<File> files = new LinkedList<File>();
			
			for(String root: m_roots)
				scanFolder(new File(root), new BookFilter(files));
			
			if (files.size() > 0)
			{
				m_maxCount = files.size();
			
				publishProgress();
			
				for(File file: files)
				{
					m_count++;
					publishProgress(m_processing, "..."/*, ": ", file.getName()*/);
					
					String extension = Utils.getExtension(file.getName());
					MetaDataFactory.getMetaData(Utils.getMimeType(extension), file);					
					
				}			
			}
			
			//Log.d("FileBrowser", "Processed " + files.size() + " files");
			return null;
		}
		
		private void scanFolder(File path, BookFilter filter)
		{
			publishProgress(m_scanning, " ", path.getAbsolutePath());
			
			path.listFiles(filter);
		}

		class BookFilter implements FileFilter		
		{
			private final List<File> m_result;
			
			public BookFilter(List<File> result)
			{
				m_result = result;
			}
			
			@Override
			public boolean accept(File file)
			{
				if (file.isHidden())
					return false;
				
				if (file.isDirectory())
					scanFolder(file, this);
				else
				{
					String name = file.getName();
					int length = name.length(); 
					if (
							(length >= 5 && name.charAt(length - 4) == '.' && name.charAt(length - 1) == '2') || // "*.??2"
							(length >= 6 && name.charAt(length - 5) == '.' ) || // "*.????"
							(length >= 9 && name.charAt(length - 8) == '.' && name.charAt(length - 5) == '2') // "*.??2??"
						)
					{
						if (Utils.isBook(Utils.getExtension(name)))
							m_result.add(file);
					}
				}
				
				return false;
			}
		}
		
		@Override
		protected void onProgressUpdate(String... values)
		{
			if (values.length == 0)
			{
				m_progressDialog.hide();
				
				m_progressDialog = new ProgressDialog( m_context );				
				m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_progressDialog.setTitle(m_context.getString(R.string.scan_title));
				m_progressDialog.setMessage(m_processing);
				m_progressDialog.setMax(m_maxCount);
				m_progressDialog.show();
			}
			else
			{
				if (values.length == 1)
					m_progressDialog.setMessage(values[0]);
				else
				{
					StringBuilder builder = new StringBuilder(100);
					for (int i = 0; i < values.length; i++)
					{
						builder.append(values[i]);
					}
					
					m_progressDialog.setMessage(builder.toString());
				}
				if (m_maxCount != 0)
					m_progressDialog.setProgress(m_count);
			}
			
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result)
		{
			m_progressDialog.hide();
			m_progressDialog.dismiss(); 
			m_progressDialog = null;
			
			scanFinished(m_gotoLibrary);
			super.onPostExecute(result);
		}
	}
}

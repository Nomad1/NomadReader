package net.runserver.fileBrowser;

import java.util.ArrayList;
import java.util.List;

import net.runserver.common.BaseActivity;
import net.runserver.library.FileInfo;
import net.runserver.library.Library;
import net.runserver.library.ResourceHelper;
import net.runserver.textReader.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileBrowserView extends FrameLayout implements OnClickListener
{
	public static final int FILE_ITEM_ID = 1111;

	private final ViewGroup m_listView;
	private final TextView m_title;
	private final LayoutInflater m_inflater;
	private final ArrayList<View> m_listItems;
	private final TextView m_itemRange;
	private final TextView m_pageCounter;
	private final LinearLayout m_pageIndicator;

	// private final FileInfoView m_fileInfoView;

	private int m_selectedIndex;
	private int m_pageNumber;
	private int m_pageCount;
	private int m_maximumPageSize;
	private int m_visiblePageSize;

	private OnClickListener m_clickListener;
	private View m_layout;

	// private final Library m_library;
	private final ResourceHelper m_resourceHelper;

	private FileInfo m_currentInfo;

	public FileInfo getSelectedInfo()
	{
		FileInfo info = (FileInfo) m_listItems.get(m_selectedIndex).getTag();
		return info;
	}

	public void setVisibility(int value)
	{
		m_layout.setVisibility(value);
	}

	public void setOnClickListener(OnClickListener value)
	{
		m_clickListener = value;
	}

	public FileBrowserView(Context context, ViewGroup container, ResourceHelper resourceHelper)
	{
		super(context);
		m_resourceHelper = resourceHelper;

		m_inflater = LayoutInflater.from(context);
		m_layout = m_inflater.inflate(R.layout.file_browser, this);

		//m_layout = view.findViewById(R.id.fileBrowser);
		container.addView(this);

		m_listView = (ViewGroup) m_layout.findViewById(R.id.fileList);
		m_title = (TextView) m_layout.findViewById(R.id.title);
		m_itemRange = (TextView) m_layout.findViewById(R.id.itemRange);

		m_pageCounter = (TextView) m_layout.findViewById(R.id.pageCounter);
		m_pageIndicator = (LinearLayout) m_layout.findViewById(R.id.pageIndicator);
		setDrawingCacheEnabled(true);

		if (BaseActivity.isNookTouch)
		{
			//m_pageCounter.setTextColor(0xFF000000);
			//m_title.setTextColor(0xFF000000);
			container.setBackgroundColor(0xFFFFFFFF);
		}
		else
		{
			if (BaseActivity.isXLarge)
			{
				m_title.setVisibility(View.INVISIBLE);
				m_itemRange.setVisibility(View.GONE);
			}
			// View headerLine = m_layout.findViewById(R.id.header_line);
			// headerLine.setVisibility(View.GONE);
		}

		View coversMode = m_layout.findViewById(R.id.cover_mode_button);
		coversMode.setOnClickListener(this);
		View listMode = m_layout.findViewById(R.id.list_mode_button);
		listMode.setOnClickListener(this);
		
		m_listItems = new ArrayList<View>();

		m_maximumPageSize = 0;
		m_visiblePageSize = 0;
		// m_fileInfoView = new FileInfoView(context);
		// m_fileInfoView.setVisibility(View.GONE);
		// container.addView(m_fileInfoView);
	}

	@Override
	protected void onSizeChanged(int w, int height, int oldw, int oldh)
	{
		super.onSizeChanged(w, height, oldw, oldh);
		setHeight(height);
	}

	private void setHeight(int height)
	{
		height = (int) (height / BaseActivity.DisplayMetrics.density);

		height -= 20; // footer
		height -= BaseActivity.isLarge ? 75 : 65; // header

		m_visiblePageSize = (int) (height / (BaseActivity.isLarge ? 65 : 45));
		Log.d("FileBrowser", "Height is " + height + ", max page size " + m_visiblePageSize);

		if (m_visiblePageSize <= 0)
		{
			setHeight((int) (BaseActivity.DisplayMetrics.heightPixels - 25 * BaseActivity.DisplayMetrics.density));
			return;
		}

		for (int i = m_listItems.size(); i < m_visiblePageSize; i++)
			addListItem(null);

		if (!BaseActivity.isNookTouch)
			m_maximumPageSize = 5000; // page controls would be always hidden due to scroll view
		else
			m_maximumPageSize = m_visiblePageSize;
	}

	public void setInfo(FileInfo directory, String cacheDir, int returnIndex)
	{
		m_currentInfo = directory;
		if (m_maximumPageSize == 0)
			setHeight(0);

		m_pageNumber = returnIndex / m_maximumPageSize;
		updateList();
	}

	private void setListItem(View item, FileInfo info)
	{
		TextView itemTitle = (TextView) item.findViewById(R.id.item_name);
		itemTitle.setText(info.isBack() ? ".." : info.getName());

		itemTitle.setTypeface(info.isDirectory() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

		TextView itemInfo = (TextView) item.findViewById(R.id.item_info);
		itemInfo.setText(info.isBack() ? m_resourceHelper.resStringFormat(R.string.back_to_template, info.getName()) : info.getShortInfo());

		if (BaseActivity.isNookTouch)
		{
			itemInfo.setTextColor(0xFF000000);
			itemTitle.setTextColor(0xFF000000);
		}

		item.setTag(info);
		item.setOnClickListener(m_clickListener);

		item.setVisibility(View.VISIBLE);
		/*
		 * item.findViewById(R.id.selectorBottomLine).setVisibility(View.INVISIBLE
		 * );
		 * item.findViewById(R.id.selectorTopLine).setVisibility(View.INVISIBLE
		 * );
		 */
		item.findViewById(R.id.selectorBottomLine).setEnabled(false);
		item.findViewById(R.id.selectorTopLine).setEnabled(false);
		item.findViewById(R.id.selectorRightLine).setVisibility(View.VISIBLE);
		itemTitle.setVisibility(View.VISIBLE);
		itemInfo.setVisibility(View.VISIBLE);
	}

	private void addListItem(FileInfo info)
	{
		m_inflater.inflate(R.layout.file_list_item, m_listView);

		View item = m_listView.findViewById(R.id.fileListItem);

		TextView itemTitle = (TextView) item.findViewById(R.id.item_name);
		TextView itemInfo = (TextView) item.findViewById(R.id.item_info);
		if (info != null)
		{
			itemTitle.setText(info.isBack() ? ".." : info.getName());
			itemTitle.setTypeface(info.isDirectory() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

			itemInfo.setText(info.isBack() ? m_resourceHelper.resStringFormat(R.string.back_to_template, info.getName()) : info
					.getShortInfo());

			if (BaseActivity.isNookTouch)
			{
				itemInfo.setTextColor(0xFF000000);
				itemTitle.setTextColor(0xFF000000);
			}

			item.setOnClickListener(m_clickListener);
		} else
		{
			itemTitle.setVisibility(View.INVISIBLE);
			itemInfo.setVisibility(View.INVISIBLE);
		}
			

		item.setId(FILE_ITEM_ID);
		item.setTag(info);
		item.findViewById(R.id.selectorBottomLine).setEnabled(false);
		item.findViewById(R.id.selectorTopLine).setEnabled(false);

		// m_listView.addView(item);
		m_listItems.add(item);
	}

	public void setClickListener(OnClickListener listener)
	{
		m_clickListener = listener;
	}

	private void updateList()
	{
		m_title.setText(m_currentInfo.getName());
		if (BaseActivity.isNookTouch)
			m_title.setTextColor(0xff000000);

		List<FileInfo> files = m_currentInfo.getFiles();

		int itemCount = files.size();
		if (m_currentInfo.getParent() != null)
			itemCount++;

		m_pageCount = itemCount / m_maximumPageSize;
		if ((itemCount % m_maximumPageSize) != 0)
			m_pageCount++;

		int page = m_pageNumber;

		if (page >= m_pageCount)
			page = m_pageCount - 1;

		int count = 0;
		if (m_currentInfo.getParent() != null && page == 0)
		{
			FileInfo info = m_currentInfo.getParent();
			info.setBack(true);
			if (m_listItems.size() > 0)
				setListItem(m_listItems.get(0), info);
			else
				addListItem(info);
			count++;
		}

		int start = page == 0 ? 0 : (page * m_maximumPageSize - 1);

		for (int i = start; i < files.size() && count < m_maximumPageSize; i++)
		{
			FileInfo info = files.get(i);
			info.setBack(false);
			if (count < m_listItems.size())
				setListItem(m_listItems.get(count), info);
			else
				addListItem(info);
			count++;
		}

		for (int i = count; i < m_listItems.size(); i++)
		{
			View item = m_listItems.get(i);
			item.findViewById(R.id.item_name).setVisibility(View.INVISIBLE);
			item.findViewById(R.id.item_info).setVisibility(View.INVISIBLE);
			item.findViewById(R.id.selectorBottomLine).setEnabled(false);
			item.findViewById(R.id.selectorTopLine).setEnabled(false);
			item.findViewById(R.id.selectorRightLine).setVisibility(View.VISIBLE);
			item.setTag(null);
			item.setOnClickListener(null);

			if (i >= m_visiblePageSize)
				item.setVisibility(View.GONE);
		}

		// Log.d("FileBrowser", "List adding/setting took " +
		// (SystemClock.elapsedRealtime() - startTime));

		/*
		 * 
		 * if (m_currentInfo.getParent() == null) m_itemRange.setText(""); else
		 * { if (files.size() < m_currentInfo.getItemSize()) { StringBuilder
		 * text = new StringBuilder();
		 * text.append(m_resourceHelper.resStringFormat
		 * (R.string.showing_template, files.size()));
		 * 
		 * text.append(m_resourceHelper.resStringFormat(R.string.hidden_template,
		 * m_currentInfo.getItemSize() - files.size()));
		 * 
		 * m_itemRange.setText(text); } else m_itemRange.setText(""); }
		 */

		String path = m_currentInfo.getPath();

		if (path.startsWith(Library.s_libraryAuthorFile))
			path = m_resourceHelper.resStringFormat(R.string.books_of, m_currentInfo.getShortInfo());
		else if (path.startsWith(Library.s_libraryFile) || path.startsWith(Library.s_recentDocumentsFile))
			path = m_currentInfo.getShortInfo();
		else if (path.length() > 0)
			path = m_resourceHelper.resStringFormat(R.string.folder_path, path);

		m_itemRange.setText(path);

		if (BaseActivity.isNookTouch)
			m_itemRange.setTextColor(0xff000000);

		m_pageNumber = page;

		updatePageState();

		// setItemState(m_selectedIndex, true);

		// invalidate();

		// Log.d("FileBrowser", "List update took " +
		// (SystemClock.elapsedRealtime() - startTime));
	}

	private void setItemState(int index, boolean selected)
	{
		View itemView = m_listItems.get(index);

		/*
		 * itemView.findViewById(R.id.selectorTopLine).setVisibility(selected ?
		 * View.VISIBLE : View.INVISIBLE);
		 * itemView.findViewById(R.id.selectorBottomLine).setVisibility(selected
		 * ? View.VISIBLE : View.INVISIBLE);
		 */
		itemView.findViewById(R.id.selectorTopLine).setEnabled(selected);
		itemView.findViewById(R.id.selectorBottomLine).setEnabled(selected);
		itemView.findViewById(R.id.selectorRightLine).setVisibility(selected ? View.INVISIBLE : View.VISIBLE);
	}

	private void updatePageState()
	{
		String text;

		if (m_pageCount == 1)
			text = "";
		else
			text = m_resourceHelper.resStringFormat(R.string.page_template, m_pageNumber + 1, m_pageCount).toString();

		m_pageCounter.setText(text);

		if (!BaseActivity.isNookTouch)
		{
			View pageLeft = m_layout.findViewById(R.id.page_left_button);
			pageLeft.setVisibility(m_pageNumber > 0 ? View.VISIBLE : View.INVISIBLE);
			pageLeft.setOnClickListener(this);

			View pageRight = m_layout.findViewById(R.id.page_right_button);
			pageRight.setVisibility(m_pageNumber < m_pageCount - 1 ? View.VISIBLE : View.INVISIBLE);
			pageRight.setOnClickListener(this);
		}

		m_pageIndicator.removeAllViews();

		if (m_pageCount > 1 && m_pageCount < 20)
		{
			for (int i = 0; i < m_pageNumber; i++)
			{
				ImageView view = new ImageView(getContext());
				view.setImageResource(R.drawable.page_indicator);
				m_pageIndicator.addView(view);

			}

			ImageView currentPage = new ImageView(getContext());
			currentPage.setImageResource(BaseActivity.isNook || BaseActivity.isNookTouch ? R.drawable.page_indicator_current
					: R.drawable.page_indicator_current_white);
			m_pageIndicator.addView(currentPage);

			for (int i = m_pageNumber + 1; i < m_pageCount; i++)
			{
				ImageView view = new ImageView(getContext());
				view.setImageResource(R.drawable.page_indicator);
				m_pageIndicator.addView(view);
			}
		}
	}

	public void setSelected(FileInfo info)
	{
		if (info != null)
			for (int i = 0; i < m_listItems.size(); i++)
			{
				setItemState(i, m_listItems.get(i).getTag() == info);
				// Log.d("FileBrowser", "Height is " +
				// m_listItems.get(i).getHeight() *
				// BaseActivity.DisplayMetrics.density);
			}

	}

	public void prevPage()
	{
		if (m_pageNumber > 0)
		{
			m_pageNumber--;
			updateList();
		}
	}

	public void nextPage()
	{
		if (m_pageNumber < m_pageCount - 1)
		{
			m_pageNumber++;
			updateList();
		}
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.page_left_button:
				prevPage();
				break;
			case R.id.page_right_button:
				nextPage();
				break;
			case R.id.cover_mode_button:
			case R.id.list_mode_button: // it is hard to press sometimes
				((FileBrowser) getContext()).onChangeMode(FileBrowser.MODE_COVERS);
				break;
		}
	}
}

package net.runserver.fileBrowser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.runserver.bookRenderer.HypenateManager;
import net.runserver.common.BaseActivity;
import net.runserver.common.FixedCharSequence;
import net.runserver.library.FileInfo;
import net.runserver.library.Utils;
import net.runserver.library.metaData.MetaData;
import net.runserver.library.metaData.MetaDataFactory;
import net.runserver.textReader.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FileInfoView extends LinearLayout implements OnClickListener
{
	private static final SimpleDateFormat s_dateFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm:ss");

	private final LayoutInflater m_inflater;
	private final LinearLayout m_infoList;
	private final Button m_openButton;

	private FileInfo m_currentInfo;
	private boolean m_inited = false;
	private int m_footerHeight = 40;
	private OnClickListener m_clickListener;
	private boolean m_footerHidden = false;
	//private boolean m_preferFooterHidden = false;
	private Bitmap m_cover;
	
	private TextView m_detailsTitle;
	private TextView m_detailsAuthor;
	private TextView m_detailsSeries;
	private TextView m_detailsAnnotationStart;
	private ImageView m_detailsCover;
	private ImageView m_detailsDivider;
	private ScrollView m_infoScroll;

	public FileInfo getCurrentInfo()
	{
		return m_currentInfo;
	}

	public void setOnClickListener(OnClickListener value)
	{
		m_clickListener = value;
	}

	public FileInfoView(Context context, ViewGroup parent)
	{
		super(context);

		m_inflater = LayoutInflater.from(context);
		m_inflater.inflate(R.layout.file_info, this);

		m_infoList = (LinearLayout) findViewById(R.id.info_items);
		//m_moreButton = (ImageButton) findViewById(R.id.info_more);
		m_openButton = (Button) findViewById(R.id.details_open);

		m_infoList.setOnClickListener(this);
		//m_moreButton.setOnClickListener(this);
		findViewById(R.id.details_open).setOnClickListener(this);
		//findViewById(R.id.details_cover).setOnClickListener(this);

		m_detailsTitle = (TextView) findViewById(R.id.details_title);
		m_detailsAuthor = (TextView) findViewById(R.id.details_author);
		m_detailsSeries = (TextView) findViewById(R.id.details_series);
		m_detailsAnnotationStart = (TextView) findViewById(R.id.details_annotation_start);
		m_detailsCover = (ImageView) findViewById(R.id.details_cover);
		m_detailsDivider = (ImageView) findViewById(R.id.details_divider);
		m_infoScroll = (ScrollView)findViewById(R.id.info_scroll);

		if (BaseActivity.isNookTouch)
		{
			//m_detailsTitle.setTextColor(0xFF000000);
			//m_detailsAuthor.setTextColor(0xFF000000);
			//m_detailsSeries.setTextColor(0xFF000000);
			//m_detailsAnnotationStart.setTextColor(0xFF000000);
			m_detailsDivider.setImageResource(R.drawable.divider_black);
			parent.setBackgroundColor(0xFFFFFFFF);
		}
		
		parent.addView(this);
		m_cover = null;
	}

	private static void setFrameLayoutMargins(View view, int top, int left, int right, int bottom)
	{
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
		if (bottom != -1)
			params.bottomMargin = bottom;
		if (top != -1)
			params.topMargin = top;
		if (left != -1)
			params.leftMargin = left;
		if (right != -1)
			params.rightMargin = right;
		view.setLayoutParams(params);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if (m_currentInfo != null)
			showInfo(m_currentInfo);
	}

	public void showInfo(FileInfo info)
	{
		if (info == null)
			return;
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		if (!m_inited)
		{
			m_inited = true;
			m_footerHeight = m_openButton.getLayoutParams().height;

			if (height < 960/* && !BaseActivity.isNookTouch*/)
			{
				m_infoList.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, m_footerHeight));
				m_footerHidden = true;
				//m_preferFooterHidden = true;
			} else
			{
				m_infoList.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				m_footerHidden = false;
				//m_preferFooterHidden = false;
			}
			//m_moreButton.setSelected(true);
		} else
			m_infoScroll.scrollTo(0, 0);

		m_currentInfo = info;
		
		//LinearLayout detailsBlock = (LinearLayout) findViewById(R.id.details_block);
		//LinearLayout detailsSubBlock = (LinearLayout) findViewById(R.id.details_subBlock);

		m_detailsTitle.setText(info.getName());
		m_detailsAuthor.setText("");
		m_detailsAuthor.setVisibility(GONE);
		m_detailsSeries.setText("");
		m_detailsSeries.setVisibility(GONE);
		m_detailsAnnotationStart.setText("");
		m_detailsAnnotationStart.setVisibility(GONE);
		m_detailsCover.setVisibility(GONE);
		//detailsAnnotationEnd.setText("");
		//detailsAnnotationEnd.setVisibility(GONE);
		
		/*Drawable drawable = detailsCover.getDrawable();
		if (drawable != null && drawable.getClass() == BitmapDrawable.class)
		{
			((BitmapDrawable)drawable).getBitmap().recycle();
		}
		detailsCover.setImageDrawable(null);
		
		
		detailsCover.setLayoutParams(new LayoutParams(0, 0));*/
		//detailsBlock.setGravity(Gravity.TOP);
		//detailsSubBlock.setVisibility(GONE);
		m_openButton.setVisibility(info.isDirectory() ? INVISIBLE : VISIBLE);
		m_openButton.setText(R.string.open);

		m_infoList.removeAllViews();

		if (info.isDirectory())
		{
			//addInfoListItem(R.string.directory_items, Long.toString(info.getItemSize()));
			if (m_footerHidden)
				onClick(m_infoList);
		}
		else
		{
			addInfoListItem(R.string.file_size, Utils.formatByteAmount(info.getItemSize()));
			if (m_footerHidden == (info.getMetaData() == null || info.getMetaData().isBook() == false))
				onClick(m_infoList);
		}

		if (info.getPath() != null && new File(info.getPath()).exists())
			if (info.isDirectory())
				addInfoListItem(R.string.file_path, info.getPath());
			else
			{
				File file = new File(info.getPath());
				addInfoListItem(R.string.file_name, file.getName());
				addInfoListItem(R.string.file_path, file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length()));
			}

		if (info.getLastModification() != 0)
			addInfoListItem(R.string.file_date, s_dateFormat.format(info.getLastModification()));

		if (m_cover != null)
		{
			m_cover.recycle();
			m_cover = null;
		}
		
		MetaData metaData = info.getMetaData();
		if (metaData != null)
		{
			/*if (height <= 480 && m_infoList.getVisibility() == VISIBLE)
				onClick(null);*/

			int imageWidth = 0;
			int imageHeight = 0;

			//if (detailsCover.getVisibility() == VISIBLE)
			{
				m_cover = MetaDataFactory.getCover(metaData, info.getPath(), ((FileBrowser)getContext()).getCacheLocation(), info.isDirectory());


				if (m_cover != null)
				{
					int cwidth = m_cover.getWidth();
					int cheight = m_cover.getHeight();

					int maxWidth = metaData.isBook() ? Math.min(width / 3, MetaDataFactory.MAX_COVER_WIDTH) : width - 20;

					if (cwidth > maxWidth)
					{
						cheight = maxWidth * cheight / cwidth;
						cwidth = maxWidth;
					}

					FrameLayout.LayoutParams params = cwidth > 0 ? new FrameLayout.LayoutParams(cwidth, cheight) : new FrameLayout.LayoutParams(maxWidth, maxWidth
							* MetaDataFactory.MAX_COVER_HEIGHT / MetaDataFactory.MAX_COVER_WIDTH);
					
					params.gravity = metaData.isBook() ? Gravity.TOP | Gravity.RIGHT : Gravity.CENTER;
					imageWidth = params.width;
					imageHeight = params.height;
					
					BitmapDrawable coverDrawable = new BitmapDrawable(m_cover);
					coverDrawable.setFilterBitmap(true);
					coverDrawable.setDither(true);
					m_detailsCover.setImageDrawable(coverDrawable);
					
					m_detailsCover.setLayoutParams(params);
					m_detailsCover.setVisibility(VISIBLE);
				}
			}

			m_detailsAuthor.setText(metaData.getAuthor());
			m_detailsAuthor.setVisibility(VISIBLE);

			String description = metaData.getDescription();
			
			if (metaData.isBook() || (description!= null && description.length() > 0))
			{
				//detailsSubBlock.setVisibility(VISIBLE);
				if (metaData.getSeries() != null && metaData.getSeries().length() > 0)
				{
					if (metaData.getPart() > 0)
						m_detailsSeries.setText(metaData.getSeries() + " - " + metaData.getPart());
					else
						m_detailsSeries.setText(metaData.getSeries());
					m_detailsSeries.setVisibility(VISIBLE);
				}
	
				if (description != null && description.length() > 0)
				{
					m_detailsAnnotationStart.setVisibility(VISIBLE);
					TextFormatHelper helper = new TextFormatHelper(m_detailsAnnotationStart.getPaint(), getWidth() - (int)(40 * dm.density) /* detailsAnnotationStart.getWidth() - 15*/, imageWidth, imageHeight);
					m_detailsAnnotationStart.setText(helper.formatText(description));
				}
			} //else
				//detailsBlock.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

			/*if (detailsAnnotationStart.getText().length() > 0)
				detailsDivider.setPadding(0, 6, 0, 5);
			else
				detailsDivider.setPadding(0, 25, 0, 5);*/

			if (metaData.isBook())
				m_openButton.setText(R.string.read);
		} else
		{

			if (info.getInfo() != null && info.getInfo().length() > 0)
			{
				m_detailsAnnotationStart.setText(info.getInfo());
				m_detailsAnnotationStart.setVisibility(VISIBLE);
				//detailsSubBlock.setVisibility(VISIBLE);
			}

			m_detailsDivider.setPadding(0, 25, 0, 5);

			if (getHeight() <= 480 && m_infoList.getVisibility() == GONE)
				onClick(null);
		}
		
		updateOpenButton();
	}

	private void addInfoListItem(int labelResourceId, String value)
	{
		LinearLayout layout = new LinearLayout(getContext());
		m_inflater.inflate(R.layout.info_list_item, layout);

		TextView infoLabel = (TextView) layout.findViewById(R.id.info_item_label);
		//TextView infoValue = (TextView) layout.findViewById(R.id.info_item_value);

		CharSequence label = getResources().getString(labelResourceId);
		SpannableString text = new SpannableString(label + " " + value);
		text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), label.length() + 1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		//if (BaseActivity.isNookTouch)
		//	infoLabel.setTextColor(0xFF000000);
		
		//text.setSpan(new BoldSpan(), start, end, flags)
		infoLabel.setText(text);
			//	labelResourceId);
		//infoValue.setText(value);

		m_infoList.addView(layout);
	}

	@Override
	public void onClick(View view)
	{
		if (view != null)
			switch (view.getId())
			{
				case R.id.info_items:
					//m_moreButton.setSelected(!m_moreButton.isSelected());
					m_footerHidden = !m_footerHidden;
					m_infoList.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, m_footerHidden ? m_footerHeight : LayoutParams.WRAP_CONTENT));
					updateOpenButton();
					break;
				default:
					if (m_clickListener != null)
						m_clickListener.onClick(view);
					break;
			}

	}
	
	private void updateOpenButton()
	{
		//if (m_moreSelected)
			setFrameLayoutMargins(m_openButton, 0, -1, -1, -1);
		/*else
			setFrameLayoutMargins(m_openButton, ((TextView) findViewById(R.id.details_title)).getLineHeight() / 4, -1, -1, -1);*/

	}
	
	private class TextFormatHelper
	{
		private class TextSegment
		{
			private final List<CharSequence> m_words;
			private final float m_tagetWidth;
			private float m_lineWidth;
			private int m_letters;
			private boolean m_noJustify = false;
			
			public void setNoJustify(boolean value)
			{
				m_noJustify = value;
			}
			
			public void addWord(CharSequence word, float width)
			{
				m_words.add(word);
				m_lineWidth += width;
				m_letters += word.length();
			}
			
			public TextSegment(float targetWidth)
			{
				m_tagetWidth = targetWidth;
				m_words = new ArrayList<CharSequence>();
			}
			
			public CharSequence justify(float spaceWidth)
			{
				if (m_words.size() == 1)
					return m_words.get(0);
				
				int nspaces = (m_words.size() - 1);		
				int spaceNeeded = m_noJustify ? 0 : (int)((m_tagetWidth - m_lineWidth) / spaceWidth);
				
				if (spaceNeeded <= 0)
				{
					spaceNeeded = 0;
				}
				
				int toAdd = (spaceNeeded / nspaces);
				if (spaceNeeded % nspaces != 0)
					toAdd++;
				
				if (toAdd < 0)
					toAdd = 0;
				
				StringBuilder result = new StringBuilder(m_letters + nspaces + spaceNeeded + 1);
				
				/*if ((m_flags & BaseBookReader.RTL) != 0 && !BaseActivity.isBiDirStringSupported)
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
				} else*/
				{
					boolean first = true;
					for (CharSequence word : m_words)
					{
						if (!first)
						{
							result.append(' ');
							for (int j = 0; j < toAdd; j++)
							{
								result.append(' ');
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
				
				return result;
			}
		}
		
		private final TextPaint m_paint;
		private final int m_imageHeight;
		private final int m_imageWidth;
		private float m_lastWidth;
		private final float m_lineHeight;
		private final int m_lineWidth;
		private final float m_spaceWidth;
		private final float m_dashWidth;
		private final List<TextSegment> m_lines;
		
		public TextFormatHelper(TextPaint paint, int lineWidth, int imageWidth, int imageHeight)
		{
			m_lines = new ArrayList<TextSegment>();
			m_imageHeight = imageHeight;
			m_imageWidth = imageWidth;
			m_lineWidth = lineWidth;
			m_dashWidth = paint.measureText("-");
			m_paint = paint;
			m_spaceWidth = paint.measureText(" ");
			
			m_lineHeight = paint.getTextSize(); 
		}
		
		private void addWord(FixedCharSequence chars, boolean newLine)
		{
			float width = m_paint.measureText(chars, 0, chars.length());

			int nlines = m_lines.size();
			//if (m_lastWidth == 0)
				//nlines++;
			int lineWidth = (nlines + 1) * m_lineHeight > m_imageHeight ? m_lineWidth : m_lineWidth - m_imageWidth;

			if (newLine && m_lines.size() > 0)
			{
				m_lines.get(m_lines.size() - 1).setNoJustify(true);
			}
			
			if (m_lines.size() == 0)
				newLine = true;
			
			if (newLine || m_lastWidth + width > lineWidth)
			{
				if (!newLine)
				{
					Object [] hypened = new Object[4];
					float [] widths = new float[chars.length()];
					m_paint.getTextWidths(chars, 0, chars.length(), widths); 
					if (HypenateManager.canHypenate(chars, lineWidth-m_lastWidth, widths, m_dashWidth, hypened))
					{
						FixedCharSequence restWord = (FixedCharSequence)hypened[1];
						FixedCharSequence word = (FixedCharSequence)hypened[0];
						TextSegment segment = m_lines.get(m_lines.size() - 1);
						float nwidth = m_paint.measureText(word, 0, word.length());
						segment.addWord(word, nwidth);
						m_lastWidth += nwidth;
						addWord(restWord, false);
						return;
					}
				}
				m_lastWidth = 0; // TODO: recalc line width
			}

			if (m_lastWidth != 0)
				width += m_spaceWidth;

			TextSegment segment;

			if (m_lastWidth == 0)
			{
				segment = new TextSegment(lineWidth);
				m_lines.add(segment);
			} else
				segment = m_lines.get(m_lines.size() - 1);
			
			segment.addWord(chars, width);

			m_lastWidth += width;
		
			// TODO: try hypenate word
		}
		
		public Spanned formatText(CharSequence chars)
		{
			FixedCharSequence text = FixedCharSequence.toFixedCharSequence(chars);
			int lastChar = text.length() - 1;
			
			int wordLength = 0;
			int wordStart = 0;
			boolean newLine = false;
			boolean firstLine = true;
			
			for (int i = 0; i <= lastChar; i++)
			{
				char ch = text.charAt(i);
				boolean separator = false;
				boolean dash = false;

				switch (ch)
				{
					case ' ':
						separator = true;
						break;
					case '\t':
						separator = true;
						break;
					case '\n':
					case '\r':
					case '\0':
						separator = true;
						newLine = true;
						break;
					case '-':
					case '\'':
						separator = true;
						dash = true;						
						break;
					case '\u00a0': // &nbsp;
						if (i > 0 && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–'))
							break;

						separator = true;
						break;
					case '—':
					case '–':
						break;
					default:
						if (i != lastChar)
							continue;
						break;						
				}
				
				if (separator || i == lastChar)
				{
					wordLength = i - wordStart;
					if ((dash || (!separator && i == lastChar)))
						wordLength++;

					if (wordLength > 0)
					{
						if (firstLine)
							addWord(FixedCharSequence.toFixedCharSequence("   "), true);
						
						addWord((FixedCharSequence)text.subSequence(wordStart, wordStart + wordLength), false);
						
						if (newLine)
							firstLine = true;
						else
							firstLine = false;
						
					}
					newLine = false;
					wordStart = i + 1;
				}
			}
			
			addWord(FixedCharSequence.toFixedCharSequence(""), true);
			
			SpannableStringBuilder builder = new SpannableStringBuilder();
			
			for(TextSegment segment: m_lines)
			{
				builder.append(segment.justify(m_spaceWidth));
				builder.append("\n");
			}
			
			return builder;
		}
	}
}

package net.runserver.library;

import java.util.ArrayList;
import java.util.List;

import net.runserver.common.BaseActivity;
import net.runserver.fileBrowser.FileBrowser;
import net.runserver.library.GalleryImage.OnClickListener;
import net.runserver.library.metaData.MetaData;
import net.runserver.library.metaData.MetaDataFactory;
import net.runserver.textReader.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class CoverView extends LinearLayout implements OnClickListener, android.view.View.OnClickListener
{
	private final static int s_maxTitleLines = 4;
	private final static int s_maxAuthorLines = 3;
	
	public interface OnCoverClickListener
	{
		public void onCoverClick(FileInfo info, boolean noDetails);
	}

	private final LayoutInflater m_inflater;
	private final GalleryView m_coverGallery;
	
	public final int m_maxCovers;	

	private final int m_coverMaxWidth;	
	private final int m_coverMaxHeight;
	
	private final int m_coverWidthBig;
	private final int m_coverHeightBig;
	
	private final int m_coverMirrorHeight;
	private final int m_coverHeight;
	
	private final int m_coverDirectoryWidth;
	private final int m_coverBookWidth;
	private final int m_minimalShelfWidth;
	private final int m_shelfPadding;
	
	private final float m_coverScale;
	
	private float m_density;
	
	
	private OnCoverClickListener m_clickListener;

	private Bitmap m_emptyCover;
	private Bitmap m_loadingCover;
	private Bitmap m_folderCover;
	private Bitmap m_fb2Cover;
	private Bitmap m_fb2ZipCover;
	private Bitmap m_epubCover;
	private Bitmap m_documentsFolderCover;
	//private Bitmap m_libraryFolderCover;
	private Bitmap m_sdcardFolderCover;
	//private Bitmap m_internalFolderCover;
	private Bitmap m_rootFolderCover;
	
	private Bitmap m_folderMirrorBitmap;
	private Bitmap m_loadingMirrorBitmap;	
	private Bitmap m_loadingSmallBitmap;	
	private Bitmap m_transparentBitmap;
	

	private GalleryImage m_selectedCover;
	private int m_clickedCoverId;

	private TextPaint m_textPaint;
	private TextPaint m_folderTextPaint;
	private TextPaint m_authorPaint;
	//private Paint m_shadowPaint;
	private Paint m_bitmapPaint;
	private Paint m_filterBitmapPaint;
	private Paint m_mirrorBitmapPaint;
	private Paint m_gradientBitmapPaint;
	private Bitmap m_buttonBitmap;
	private View m_progressBar;
	private View m_closeButton; 
	private View m_backButton;
	private View m_rightButton;
	private View m_leftButton;
	private ViewGroup m_coverItems;
	private HorizontalScrollView m_scrollView;
	
	private int m_listOffset = 0;
	private List<FileInfo> m_items;
	private int m_useBack;
	private int m_version = 0;
	private String m_cacheDir = null;
	
	// private final ArrayList<Bitmap> m_bitmaps;

	public CoverView(Context context, ViewGroup container)
	{
		super(context);


		m_inflater = LayoutInflater.from(context);
		m_inflater.inflate(R.layout.cover_view, this);
		
		//findViewById(R.id.cover_back).getBackground().setDither(true);		

		DisplayMetrics dm = BaseActivity.DisplayMetrics; 
				//new DisplayMetrics();		
		//((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);		
		
		int coversHeight = BaseActivity.isNook || BaseActivity.isEpad ? 148 : dm.density >= 1.5f ?  (int)(dm.heightPixels / (dm.heightPixels > dm.widthPixels ? 3f : 2.5f)) : (int)(dm.heightPixels > dm.widthPixels ? dm.widthPixels / 2.75f : dm.heightPixels / 2.75f);    
			//(int)(dm.heightPixels / (dm.heightPixels > dm.widthPixels ? 3.75f : 2.75f));
		m_density = BaseActivity.isNook || BaseActivity.isEpad ? 1 : BaseActivity.isNookTouch ? 1.42f : coversHeight / 148.0f;
		if (dm.density < 1.0)
		{
			m_density = dm.density;
			coversHeight = (int)(148 * m_density);
		}
		
		m_maxCovers = BaseActivity.isNookTouch ? 4 : BaseActivity.isNook || BaseActivity.isEpad ? 50 : 75;
		m_shelfPadding = BaseActivity.isNookTouch ? (int)(10 * m_density) : 5;
		
		//if (!BaseActivity.isXLarge)
			container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, coversHeight));
		
		
		m_minimalShelfWidth = (int)(dm.widthPixels - 96 * dm.density);		
		
		m_coverMaxHeight = (int)Math.round(144 * m_density);
		m_coverHeight = (int)Math.round(114 * m_density);
		
		m_coverMaxWidth = (int)Math.round(84 * m_density);
		m_coverHeightBig = (int)Math.round(183 * m_density);
		m_coverWidthBig = (int)Math.round(107 * m_density);		
		m_coverMirrorHeight = (int)Math.round(30 * m_density);
		m_coverDirectoryWidth = (int)Math.round(100 * m_density);
		m_coverBookWidth = (int)Math.round(92 * m_density);
		
		m_coverScale = m_coverMaxHeight/(float)m_coverHeight;
		
		Log.d("FileBrowser", "Cover max width " + m_coverMaxWidth + ", density " + m_density); 		
		
		//this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, m_coverMaxHeight));
		
		m_coverGallery = new GalleryView(context, m_shelfPadding*2);
		//m_coverGallery.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
		/*if(BaseActivity.isNookTouch)
			m_coverGallery.setPadding(0, 0, 0, 0);		
		else*/
			m_coverGallery.setPadding(m_shelfPadding, 0, m_shelfPadding, 0);		
			
		m_backButton = findViewById(R.id.cover_back_button);
		m_rightButton = findViewById(R.id.cover_right_button);
		m_leftButton = findViewById(R.id.cover_left_button);

		m_progressBar = findViewById(android.R.id.progress);
		m_closeButton = findViewById(R.id.cover_close_button);
		m_scrollView = (HorizontalScrollView) findViewById(R.id.cover_scroll_view);
		m_coverItems = (ViewGroup)findViewById(R.id.cover_items);	
		m_coverItems.addView(m_coverGallery);
		
		if (BaseActivity.isNookTouch)
		{
			View coverBack = findViewById(R.id.cover_back);
			coverBack.setBackgroundColor(0xFFFFFFFF);
		}
		{
			View listMode = findViewById(R.id.list_mode_button);
			listMode.setOnClickListener(this);
			View coversMode = findViewById(R.id.cover_mode_button);
			coversMode.setOnClickListener(this);
		}

		// no scaling for these
		m_transparentBitmap = getBitmap(R.drawable.trans);		
		m_emptyCover = getBitmap(R.drawable.no_cover);
		m_fb2Cover = getBitmap(R.drawable.no_cover_fb2);
		m_fb2ZipCover = getBitmap(R.drawable.no_cover_fb2_zip);
		m_epubCover = getBitmap(R.drawable.no_cover_epub);
		m_folderCover = getBitmap(BaseActivity.isNookTouch ? R.drawable.folder_cover_bw : R.drawable.folder_cover);
		
		// special scaling
		m_sdcardFolderCover = getBitmap(BaseActivity.isNookTouch ? R.drawable.folder_cover_card_bw : R.drawable.folder_cover_card);
		m_rootFolderCover = getBitmap(BaseActivity.isNookTouch ? R.drawable.folder_cover_root_bw : R.drawable.folder_cover_root);
		m_documentsFolderCover = getBitmap(BaseActivity.isNookTouch ? R.drawable.folder_cover_documents_bw : R.drawable.folder_cover_documents);		
		
		// 	manual scaling 
		m_folderMirrorBitmap = getBitmap(BaseActivity.isNookTouch ? R.drawable.folder_mirror_bw : R.drawable.folder_mirror);
		m_loadingCover = getBitmap(R.drawable.loading_cover);		
		m_loadingMirrorBitmap = getBitmap(R.drawable.loading_mirror);
		
		
		int directoryWidth = m_coverDirectoryWidth;
		int aspect = 100 * m_folderCover.getWidth() / m_folderCover.getHeight();		
		int directoryHeight = directoryWidth * 100 / aspect;
		
		if (directoryHeight > m_coverMaxHeight)
		{
			directoryHeight = m_coverMaxHeight;
			directoryWidth = directoryHeight * aspect / 100;
		}
		
		directoryWidth = (int)Math.round(directoryWidth / m_coverScale);
		directoryHeight = (int)Math.round(directoryHeight / m_coverScale);

		m_documentsFolderCover = Bitmap.createScaledBitmap(m_documentsFolderCover, directoryWidth, directoryHeight, true);
		m_rootFolderCover = Bitmap.createScaledBitmap(m_rootFolderCover, directoryWidth, directoryHeight, true);
		m_sdcardFolderCover = Bitmap.createScaledBitmap(m_sdcardFolderCover, directoryWidth, directoryHeight, true);
		
		m_folderMirrorBitmap = Bitmap.createScaledBitmap(m_folderMirrorBitmap, (int)(m_folderMirrorBitmap.getWidth() * m_density), (int)(m_folderMirrorBitmap.getHeight() * m_density), true);  
		m_loadingSmallBitmap = Bitmap.createScaledBitmap(m_loadingCover, m_loadingCover.getWidth() * directoryHeight/ m_loadingCover.getHeight(), directoryHeight, true);
		m_loadingCover = Bitmap.createScaledBitmap(m_loadingCover, (int)(m_loadingCover.getWidth() * m_density), (int)(m_loadingCover.getHeight() * m_density), true);
		m_loadingMirrorBitmap = Bitmap.createScaledBitmap(m_loadingMirrorBitmap, (int)(m_loadingMirrorBitmap.getWidth() * m_density), (int)(m_loadingMirrorBitmap.getHeight() * m_density), true);
		
		

		m_textPaint = new TextPaint();
		m_textPaint.setColor(0xFF202020);
		m_textPaint.setAntiAlias(true);
		m_textPaint.setTextSize(13.5f * m_density);
		m_textPaint.setTypeface(Typeface.SERIF);		
		m_textPaint.setStyle(Style.FILL_AND_STROKE);
		m_textPaint.setStrokeWidth(0.5f);
		
		m_folderTextPaint = new TextPaint(m_textPaint);
		m_folderTextPaint.setColor(0xFF39120f);
		m_folderTextPaint.setTextSize(13.5f * m_density);
		m_textPaint.setStrokeWidth(1f);

		m_authorPaint = new TextPaint();
		m_authorPaint.setColor(0xFF303030);
		m_authorPaint.setAntiAlias(true);
		m_authorPaint.setTextSize(11f * m_density);
		m_textPaint.setStyle(Style.FILL_AND_STROKE);
		m_authorPaint.setStrokeWidth(0.1f);
		
		/*m_shadowPaint = new Paint();
		m_shadowPaint.setColor(0xff666666);
		m_shadowPaint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));*/
		
		m_filterBitmapPaint = new Paint();
		m_filterBitmapPaint.setDither(true);
		m_filterBitmapPaint.setFilterBitmap(true);
		
		m_bitmapPaint = new Paint();
		m_bitmapPaint.setFilterBitmap(true);

		m_mirrorBitmapPaint = new Paint();
		m_mirrorBitmapPaint.setFilterBitmap(false);
		m_mirrorBitmapPaint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
		
		m_gradientBitmapPaint = new Paint();
		m_gradientBitmapPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		container.addView(this);
	}
	
	private Bitmap getBitmap(int id)
	{
		//return BitmapFactory.decodeResource(getResources(), id);
		return BitmapFactory.decodeStream(getResources().openRawResource(id));
	}

	public void setCloseClickListener(OnClickListener listener)
	{
		m_closeButton.setOnClickListener(listener);
	}

	public void setCoverClickListener(OnCoverClickListener listener)
	{
		m_clickListener = listener;
	}
	
	public void advanceVersion()
	{
		m_version++;
	}
	
	public void onResume()
	{
		m_progressBar.setVisibility(GONE);
		m_coverGallery.invalidate();
	}
	
	public void suspend()
	{		
		m_coverGallery.suspend();
		m_coverGallery.setFocusable(false);
		m_coverGallery.setEnabled(false);
	}
	
	public void resume()
	{
		m_coverGallery.resume();
		m_coverGallery.setFocusable(true);
		m_coverGallery.setEnabled(true);
		m_coverGallery.invalidate();
	}

	private void drawCover(GalleryImage button, Bitmap coverBitmap, Bitmap defaultMirrorBitmap, String text, String author, boolean defaultImage, boolean directory)
	{		
		if (m_buttonBitmap == null || m_buttonBitmap.isRecycled())		
			m_buttonBitmap = Bitmap.createBitmap(m_coverWidthBig, m_coverHeightBig, Bitmap.Config.ARGB_8888);
		
		if (text == null)
			text = "";
		
		//defaultImage = false;
		Canvas canvas = new Canvas(m_buttonBitmap);
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

		StaticLayout titleLayout = null;
		StaticLayout authorLayout = null;
		
		int textYShift = 0;
		int textXShift = 13;		
		int authorYShift = 0;
		int authorXShift = 14;
		
		if (defaultImage)
		{
			titleLayout = new StaticLayout(text, directory ? m_folderTextPaint : m_textPaint, m_coverMaxWidth, android.text.Layout.Alignment.ALIGN_CENTER, /*directory ?*/ 1.0f /*: 100.0f*/,  0.0f, true);
			switch(titleLayout.getLineCount() )
			{
				case 1:
					textYShift = 80;
					break;
				case 2:
					textYShift = 72;
					break;
				case 3:
					textYShift = 66;
					break;
				default:
					textYShift = 58;
					break;
			}
			
			if (directory)
			{
				textYShift -= 12;
				textXShift -= 7;
			}
			
			
			while(titleLayout.getLineCount() > s_maxTitleLines)
			{
				int end = titleLayout.getLineEnd(s_maxTitleLines - 1);
				text = text.substring(0, end - 1) + "…";
				titleLayout = new StaticLayout(text, directory ? m_folderTextPaint : m_textPaint, m_coverMaxWidth, android.text.Layout.Alignment.ALIGN_CENTER, /*directory ?*/ 1.0f/* : 100.0f*/,  0.0f, true);
			}
			
			if (author != null && author.length() > 0)
			{
				int comma = author.indexOf(',');
				if (comma != -1)
					author = author.substring(comma + 1).trim() + ' ' + author.substring(0, comma);
					
				authorLayout = new StaticLayout(author, m_authorPaint, m_coverMaxWidth, android.text.Layout.Alignment.ALIGN_CENTER, /*directory ?*/ 1.0f /*: 100.0f*/,  0.0f, true);
				switch(authorLayout.getLineCount() )
				{
					case 1:
						authorYShift = 20;
						break;
					case 2:
						authorYShift = 15;
						break;
					default:
						authorYShift = 10;
						break;
				}
				
				while (authorLayout.getLineCount() > s_maxAuthorLines)
				{
					int end = authorLayout.getLineEnd(s_maxAuthorLines - 1);
					author = author.substring(0, end - 1) + "…";
					authorLayout = new StaticLayout(author, m_authorPaint, m_coverMaxWidth, android.text.Layout.Alignment.ALIGN_CENTER, /*directory ?*/ 1.0f/* : 100.0f*/,  0.0f, true);
				}
			}
		}
		
		textYShift = (int)(textYShift*m_density);
		textXShift = (int)(textXShift*m_density);
		authorYShift = (int)(authorYShift*m_density);
		authorXShift = (int)(authorXShift*m_density);
		
		int aspect = 100 * coverBitmap.getWidth() / coverBitmap.getHeight();
		int bigWidth = directory ? m_coverDirectoryWidth : m_coverBookWidth;
		int bigHeight = bigWidth * 100 / aspect;
		
		if (bigHeight > m_coverMaxHeight)
		{
			bigHeight = m_coverMaxHeight;
			bigWidth = bigHeight * aspect / 100;
		}
		
		Rect bigRect = new Rect(0 + (m_coverWidthBig - bigWidth) / 2, 0 + (m_coverMaxHeight /*- 1*/ - bigHeight), (m_coverWidthBig + bigWidth) / 2, m_coverMaxHeight/* - 1*/);
		
		int smallWidth = (int)Math.round(bigWidth / m_coverScale);
		int smallHeight = (int)Math.round(bigHeight / m_coverScale);
		
		if (coverBitmap != null)
		{
			//if (!directory)
				//canvas.drawRect(bigRect.left, bigRect.top - 1, bigRect.right + 2, bigRect.bottom + 2, m_shadowPaint);
			canvas.drawBitmap(coverBitmap, null, bigRect, m_bitmapPaint);
			

			if (defaultImage)
			{
				canvas.translate(textXShift, textYShift);
				titleLayout.draw(canvas);
				canvas.translate(-textXShift, -textYShift);
				
				if (authorLayout != null)
				{
					canvas.translate(14, authorYShift);
					authorLayout.draw(canvas);
					canvas.translate(-14, -authorYShift);
				}
					
			}
			
			if (defaultMirrorBitmap == null)
			{			
				canvas.save();
				canvas.scale(1.0f, -1.0f);
				canvas.translate(0, -m_coverMaxHeight * 2 - 1);
				//canvas.drawRect(bigRect.left, bigRect.top, bigRect.right, bigRect.bottom + 1, m_shadowPaint);
				canvas.drawBitmap(coverBitmap, null, bigRect, /*defaultImage ? m_filterBitmapPaint : */m_mirrorBitmapPaint);
				
				/*if (defaultImage)
				{
					if (authorLayout != null)
					{
						canvas.translate(12, authorYShift);
						authorLayout.draw(canvas);
						canvas.translate(-12, -authorYShift);
					}
				}*/
				canvas.restore();
	
				canvas.drawBitmap(m_transparentBitmap, null, new Rect(0, m_coverMaxHeight + 1, m_coverWidthBig, m_coverHeightBig), m_gradientBitmapPaint);
			}
		}
		
		Bitmap smallBitmap = null;
		
		/*if (!defaultImage)
			smallBitmap = Bitmap.createScaledBitmap(coverBitmap, smallWidth, smallHeight, false);
		else*/
		{
			smallBitmap = Bitmap.createBitmap(smallWidth, smallHeight, defaultImage && directory ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas smallCanvas = new Canvas(smallBitmap);
			//if (defaultImage)
				smallCanvas.drawBitmap(m_buttonBitmap, bigRect, new Rect(0, 0, smallWidth, smallHeight), defaultImage && !directory ? m_bitmapPaint : m_filterBitmapPaint);
			//else
				//smallCanvas.drawBitmap(coverBitmap, null, new Rect(0, 0, smallWidth, smallHeight), m_filterBitmapPaint);
		}
		
		Bitmap bigBitmap = null;
		
		if (!directory)
		{			
			if (!defaultImage)
				bigBitmap = Bitmap.createScaledBitmap(coverBitmap, bigWidth, bigHeight, true);
			else
			{				
				bigBitmap = Bitmap.createBitmap(bigWidth, bigHeight, Bitmap.Config.RGB_565);
				Canvas bigCanvas = new Canvas(bigBitmap);
				//if (defaultImage)
					bigCanvas.drawBitmap(m_buttonBitmap, bigRect, new Rect(0, 0, bigWidth, bigHeight), m_filterBitmapPaint);
				//else
					//bigCanvas.drawBitmap(coverBitmap, null, new Rect(0, 0, bigWidth, bigHeight), m_filterBitmapPaint);
			}
		}
		
		Bitmap mirrorBitmap;
		if (defaultMirrorBitmap == null)
		{
			mirrorBitmap = Bitmap.createBitmap(m_coverMaxWidth, m_coverMirrorHeight, Bitmap.Config.ARGB_8888);
			Canvas mirrorCanvas = new Canvas(mirrorBitmap);
			mirrorCanvas.drawBitmap(m_buttonBitmap, new Rect(0, m_coverMaxHeight + 1, m_coverWidthBig, m_coverHeightBig), new Rect(0, 0, m_coverMaxWidth, m_coverMirrorHeight), m_bitmapPaint);
		} else
			mirrorBitmap = defaultMirrorBitmap;
		
		/*
		
		if (m_folderCover == coverBitmap)
			try
			{
				File saveFile = new File("/sdcard/folder_mirror.png");
				if (!saveFile.exists())
				{
					saveFile.createNewFile();
					smallBitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(saveFile));
				}
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		*/
		button.setBitmaps(smallBitmap, bigBitmap, mirrorBitmap);
	}

	private GalleryImage addCoverButton(int id, FileInfo info)
	{
		GalleryImage button = new GalleryImage(m_coverMaxWidth, m_coverMaxHeight, m_coverHeight);
		button.setId(id);
		button.setTag(info);
		button.setY((int)(2*m_density));
		//button.setY(getHeight() - m_coverMaxHeight);
		button.setOnClickListener(this);
		m_coverGallery.addView(button);
		m_coverItems.setLayoutParams(new LayoutParams(Math.max(m_coverGallery.getContentWidth() + m_shelfPadding*2, m_minimalShelfWidth), LayoutParams.FILL_PARENT));
		return button;
	}
	
	private GalleryImage addCoverButton(Bitmap cover, Bitmap mirrorBitmap, String text, String author, int id, FileInfo info, boolean defaultImage)
	{
		GalleryImage button = addCoverButton(id, info);
		drawCover(button, cover, mirrorBitmap, text, author, defaultImage, info.isDirectory());
		return button;
	}

	public void setInfo(FileInfo directory, int useBack, String cacheDir, int returnIndex)
	{
		if (directory == null)
			return;
		if (m_progressBar != null)
		{
			m_progressBar.setPadding(0, 0, 0, 0);
			m_progressBar.setVisibility(VISIBLE);
		}
		
		clean();		
		
		if (m_items != directory.getFiles())
		{
			m_listOffset = (returnIndex/m_maxCovers)*m_maxCovers;
			m_items = directory.getFiles();
		}
		
		m_useBack = useBack;
		m_cacheDir = cacheDir;
		
		m_backButton.setVisibility(useBack);
		m_backButton.setOnClickListener(this);
		
		updateCovers();
	}

	private void updateCovers()
	{
		if (m_items == null)
			return;		
		
		long startTime = SystemClock.elapsedRealtime(); 
		
		System.gc();

		List<ResolveCoverParameter> toResolve = new ArrayList<ResolveCoverParameter>(m_items.size() >= m_maxCovers ? m_maxCovers : m_items.size() + 1);
		
		int ncount = 0;
		int id = m_listOffset;
		int width;// = getWidth();
		//if (width == 0)
		{
			DisplayMetrics dm = new DisplayMetrics();			
			((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
			width = dm.widthPixels;
		}
		int widthCount = width / m_coverMaxWidth;
		
		
		if (widthCount == 0)
			widthCount = 5;

		if (m_useBack != GONE)
			id++;

		for (int i = m_listOffset; i < m_items.size(); i++)
		{			
			FileInfo info = m_items.get(i);
			ncount++;
			
			MetaData metaData = info.getMetaData();
			
			Bitmap coverBitmap = metaData == null ? null : metaData.getCover();
			boolean defaultImage = coverBitmap == null;
			boolean cleanImage = true;

			if (coverBitmap == null && (metaData == null || metaData.hasFileCover()))
			{
				if (ncount < widthCount)
				{
					String extension = info.isDirectory() ? null : Utils.getExtension(info.getPath());
	
					if (info.isDirectory() || (metaData != null && metaData.hasFileCover()))
					{
						String coverFile = metaData != null && metaData.getLastCoverFile() != null ? metaData.getLastCoverFile() : info.isDirectory() ? net.runserver.library.metaData.Utils.seekCover(info.getPath()) : net.runserver.library.metaData.Utils.seekCover(info.getPath(), extension);
						if (coverFile != null)
						{
							coverBitmap = BitmapFactory.decodeFile(coverFile);
							if (coverBitmap != null)
							{
								if (metaData != null)
									metaData.setLastCoverFile(coverFile);
								defaultImage = false;
							}
						}
					}
				}
					
				if (coverBitmap == null)
				{
					if (info.isDirectory())
					{
						String name = info.getName();
						defaultImage = false;
						cleanImage = false;
						/*if (name.equals(getResources().getString(R.string.internal_memory)))
							coverBitmap = m_internalFolderCover;
						else */if (name.equals(getResources().getString(R.string.device_root)))
							coverBitmap = m_rootFolderCover;
						else if (name.equals(getResources().getString(R.string.external_card)))
							coverBitmap = m_sdcardFolderCover;
						else if (name.equals(getResources().getString(R.string.my_documents)))
							coverBitmap = m_documentsFolderCover;
						else if (name.equalsIgnoreCase("my documents"))
							coverBitmap = m_documentsFolderCover;
						else
						{
							coverBitmap = m_folderCover;
							defaultImage = true;
						}
					}
				}
			}
			
			if (coverBitmap != null)
			{
				if (!cleanImage && !defaultImage)
				{
					GalleryImage view = addCoverButton(id++, info);
					//view.setBitmaps(null, coverBitmap, m_folderMirrorBitmap, 5f, m_coverWidth - 4, m_coverHeightSmall + 1);
					view.setBitmaps(null, coverBitmap, m_folderMirrorBitmap);
				} else
				{				
					addCoverButton(coverBitmap, coverBitmap == m_folderCover ? m_folderMirrorBitmap: null, info.getName(), metaData == null ? null : metaData.getAuthor(),  id++, info, defaultImage);
					//coverBitmap.recycle();
				}
			} else
			{
				GalleryImage view = addCoverButton(id++, info);
				view.setBitmaps(m_loadingSmallBitmap, m_loadingCover, m_loadingMirrorBitmap);
				//initCoverButton(m_loadingCover, info.getName(), metaData == null ? null : metaData.getAuthor(), view, true, info.isDirectory());
				toResolve.add(new ResolveCoverParameter(view, info, m_cacheDir));
			}			
			if (ncount >= m_maxCovers)
				break;
		}
		
		m_rightButton.setVisibility(ncount + m_listOffset < m_items.size() ? VISIBLE : GONE);
		m_rightButton.setOnClickListener(this);
		
		m_leftButton.setVisibility(m_listOffset > 0 ? VISIBLE : GONE);
		m_leftButton.setOnClickListener(this);

		//post(new CoverPreResolveRunable(toResolve, this, m_coverGallery));
		if (toResolve.size() > 0)
			new ResolveCoverRunable(m_coverGallery, this).execute(toResolve.toArray(new ResolveCoverParameter[toResolve.size()]));
		
		
		/*{
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)m_cover_container.getLayoutParams();
			params.gravity = m_coverGallery.getContentWidth() + 100 * m_density >= width ? Gravity.NO_GRAVITY : Gravity.CENTER; 
			m_cover_container.setLayoutParams(params);			
		}*/
		
		m_progressBar.setVisibility(GONE);
		
		m_coverGallery.invalidate();		
		
		Log.d("FileBrowser", "Cover view update took " + (SystemClock.elapsedRealtime() - startTime));
	}
/*
	public void show(View menu)
	{
		Animation showAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_top_in);
		this.startAnimation(showAnimation);
		this.setVisibility(VISIBLE);
		findViewById(R.id.cover_close_button).setVisibility(VISIBLE);

		Animation hideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_top_out);
		menu.startAnimation(hideAnimation);
		menu.setVisibility(GONE);

		if (menu.getClass() == CoverView.class)
			((CoverView) menu).findViewById(R.id.cover_close_button).setVisibility(GONE);
	}

	public void hide(View menu)
	{
		Animation showAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_bottom_in);
		menu.startAnimation(showAnimation);
		menu.setVisibility(VISIBLE);
		if (menu.getClass() == CoverView.class)
			((CoverView) menu).findViewById(R.id.cover_close_button).setVisibility(VISIBLE);

		Animation hideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_bottom_out);
		this.startAnimation(hideAnimation);
		this.setVisibility(GONE);
		findViewById(R.id.cover_close_button).setVisibility(GONE);
		m_version++;
	}
	*/
	
	/*
	public void new_show(View menu)
	{
		this.setVisibility(VISIBLE);
		menu.setVisibility(GONE);
		
		if (menu.getClass() == CoverView.class)
		{
			((CoverView) menu).findViewById(R.id.cover_close_button).setVisibility(GONE);		
		}
	}

	public void new_hide(View menu)
	{
		menu.setVisibility(VISIBLE);
		this.setVisibility(GONE);
		findViewById(R.id.cover_close_button).setVisibility(GONE);
		m_version++;
	}*/

	private void playCoverAnimation(GalleryImage buttonView, boolean grow)
	{
		if (BaseActivity.isNookTouch)
		{
			if (grow)
				postDelayed(new GalleryView.ScaleAnimation(m_coverGallery, buttonView, buttonView.getMaximumScale(), 0), 1);
			else
				postDelayed(new GalleryView.ScaleAnimation(m_coverGallery, buttonView, 1.0f, 0), 1);
/*
			if (grow)
				postDelayed(new GalleryView.FadeAnimation(m_coverGallery, buttonView, 180, 1), 1);
			else
				postDelayed(new GalleryView.FadeAnimation(m_coverGallery, buttonView, 255, 1), 1);
			
			// TODO: display marker*/
		} else
		{		
			if (grow)
				postDelayed(new GalleryView.ScaleAnimation(m_coverGallery, buttonView, buttonView.getMaximumScale(), 250), 1);
			else
				postDelayed(new GalleryView.ScaleAnimation(m_coverGallery, buttonView, 1.0f, 150), 1);
		}
	}

	@Override
	public void onClick(GalleryImage item)
	{		
		// LinearLayout item = (LinearLayout)view;

		FileInfo info = (FileInfo)item.getTag();
		boolean directory = info.isDirectory();

		if (m_selectedCover != null/* && m_selectedCover != item*/)
			playCoverAnimation(m_selectedCover, false);
		
		boolean delay = true;
		
		if (m_selectedCover == item || directory)
		{
			final int location[] = new int[2];
			m_coverGallery.getLocationOnScreen(location);
			    
			int x = (int)(m_coverGallery.getLastX() - 75 / 2); // progress whell = 75x75 
			
			m_progressBar.setPadding(x, 0, 0, 0);
			m_progressBar.setVisibility(VISIBLE);
			
			if (directory)
			{
				//postDelayed(new GalleryView.ScaleAnimation(m_coverGallery, item, 0.999f, 50), 200/*m_selectedCover == item ? 201 : 1*/);
				postDelayed(new GalleryView.FadeAnimation(m_coverGallery, item, 180, 150), 1);
			} else
				delay = false;
			m_selectedCover = null;			
		} else
		{
			playCoverAnimation(item, true);
			m_selectedCover = item;
		}

		if (m_clickListener != null && item != null)
		{
			m_clickedCoverId = item.getId();
			// Log.d("FileBrowser", "Cover click, id " + view.getId() + ", tag "
			// + view.getTag());
			if (directory)
				m_version++;
			postDelayed(new CoverClickRunable(m_coverGallery, item, directory), delay ? 400 : 0);
		}
	}

	public void clean()
	{
		m_scrollView.computeScroll();
		m_scrollView.scrollTo(0, 0);
		
		m_selectedCover = null;
		m_coverGallery.removeAllViews();		
		m_coverItems.setLayoutParams(new LayoutParams(m_minimalShelfWidth, LayoutParams.FILL_PARENT));
		
		if (m_buttonBitmap != null)
			m_buttonBitmap.recycle();
		m_buttonBitmap = null;
		//m_items = null;
		/*
		 * for (int i = 0; i < m_bitmaps.size(); i++)
		 * m_bitmaps.get(i).recycle();
		 */
		// m_bitmaps.clear();
		System.gc();
		m_version++;
	}
	
	public void flushDB()
	{
		this.post(new Runnable(){
			@Override
			public void run()
			{
				MetaDataFactory.flushDB();
			}
		});
	}

	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.cover_back_button:				
				m_clickListener.onCoverClick(null, true);
				break;
			case R.id.cover_left_button:
				m_listOffset -= m_maxCovers;
				if (m_listOffset < 0)
					m_listOffset = 0;
				clean();
				updateCovers();
				
				m_scrollView.computeScroll();
				m_scrollView.scrollTo(Short.MAX_VALUE, 0);
				
				break;
			case R.id.cover_right_button:
				m_listOffset += m_maxCovers;
				if (m_listOffset >= m_items.size())
					m_listOffset = m_items.size() - 1;
				clean();
				updateCovers();
				
				m_scrollView.computeScroll();
				m_scrollView.scrollTo(0, 0);

				break;
			case R.id.list_mode_button:
			case R.id.cover_mode_button: //it is hard to hit it
				((FileBrowser)getContext()).onChangeMode(FileBrowser.MODE_LIST);
				break;
		}
	}
	
	private class CoverClickRunable implements Runnable
	{
		private final GalleryImage m_item;
		private final boolean m_noDetails;
		//private final GalleryView m_view;

		public CoverClickRunable(GalleryView view, GalleryImage item, boolean noDetails)
		{
			m_item = item;
			m_noDetails = noDetails;
			//m_view = view;
		}

		public void run()
		{	
			m_item.setAlpha(255);
			
			if (m_clickedCoverId == m_item.getId() && m_clickListener != null)
			{	
				m_clickListener.onCoverClick((FileInfo)m_item.getTag(), m_noDetails);
			}
		}
	}
	
	
	private class ResolveCoverParameter
	{
		private final GalleryImage m_image;
		private final FileInfo m_info;
		private final String m_cachePath;

		public GalleryImage getImage()
		{
			return m_image;
		}

		public String getText()
		{
			return m_info.getName();
		}
		
		public String getAuthor()
		{
			return m_info.getMetaData() == null ? null : m_info.getMetaData().getAuthor();
		}
		
		public boolean isMultiLine()
		{
			return m_info.isDirectory();
		}

		public String getPath()
		{
			return m_info.getPath();
		}
		
		public ResolveCoverParameter(GalleryImage image, FileInfo info, String cachePath)
		{
			m_image = image;
			m_info = info;
			m_cachePath = cachePath;
		}

		public Bitmap resolveCover()
		{
			if (m_info.getMetaData() == null)
				return null;

			try
			{
				return MetaDataFactory.getCover(m_info.getMetaData(), m_info.getPath(), m_cachePath, m_info.isDirectory());
			}
			catch (OutOfMemoryError ex)
			{
				return null;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
	}

	private class ResolveCoverResult
	{
		private final GalleryImage m_image;
		private final Bitmap m_result;
		private final String m_text;
		private final String m_author;
		private final boolean m_directory;
		private final String m_path;

		public GalleryImage getImage()
		{
			return m_image;
		}

		public Bitmap getResult()
		{
			return m_result;
		}

		public String getText()
		{
			return m_text;
		}

		public String getAuthor()
		{
			return m_author;
		}

		public String getPath()
		{
			return m_path;
		}
		
		public boolean isDirectory()
		{
			return m_directory;
		}
		
		public ResolveCoverResult(GalleryImage image, Bitmap result, String text, String author, boolean directory, String path)
		{
			m_image = image;
			m_author = author;
			m_result = result;
			m_text = text;
			m_directory = directory;
			m_path = path;
		}		
	}

	private class ResolveCoverRunable extends AsyncTask<ResolveCoverParameter, ResolveCoverResult, Void>
	{
		private final GalleryView m_container;
		private final int m_version;
		private final CoverView m_view;

		public ResolveCoverRunable(GalleryView container,CoverView view)
		{
			m_container = container;
			m_version = view.m_version;
			m_view = view;
		}

		@Override
		protected Void doInBackground(ResolveCoverParameter... arg0)
		{
			for (int i = 0; i < arg0.length; i++)
			{
				ResolveCoverParameter param = arg0[i];

				publishProgress(new ResolveCoverResult(param.getImage(), param.resolveCover(), param.getText(), param.getAuthor(), param.isMultiLine(), param.getPath()));
				
				if (m_version != m_view.m_version)
					break;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(ResolveCoverResult... values)
		{
			for (int i = 0; i < values.length; i++)
			{
				GalleryImage image = values[i].getImage();
				Bitmap bitmap = values[i].getResult();
				if (bitmap != null)
				{
					drawCover(image, bitmap, null, values[i].getText(), values[i].getAuthor(), false, values[i].isDirectory());
					values[i].getResult().recycle();
				} else
				{
					String path = values[i].getPath().toLowerCase();
					Bitmap emptyCover;
					if (path.endsWith(".fb2"))
						emptyCover = m_fb2Cover;
					else if (path.endsWith(".fb2.zip"))
						emptyCover = m_fb2ZipCover;
					else if (path.endsWith(".epub"))
						emptyCover = m_epubCover;
					else
						emptyCover = m_emptyCover;
					
					//Canvas canvas = new Canvas(values[i].getBitmap());
					drawCover(image, emptyCover, null, values[i].getText(), values[i].getAuthor(), true, values[i].isDirectory());
					//canvas.drawBitmap(((BitmapDrawable) m_emptyCover).getBitmap(), null, new Rect(8, 0, 103, 144), m_bitmapPaint);
				}
				m_container.invalidate(image);
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result)
		{
			m_view.flushDB();
			super.onPostExecute(result);
		}
	}
	
	public void nextPage()
	{
		onClick(m_rightButton);
	}
	
	public void prevPage()
	{
		onClick(m_leftButton);
	}
}

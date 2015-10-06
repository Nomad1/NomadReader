package net.runserver.library.metaData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.runserver.common.BaseActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageMetaDataReader implements IMetaDataReader
{
	@Override
	public MetaData getMetaData(File file)
	{
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(file), null, options);
	        
			if (options.outWidth <= 0 || options.outHeight <= 0)
				return null;
			
			MetaData metaData = new MetaData();
			metaData.setFlags(0);
			metaData.setTitle(file.getName());
			metaData.setAuthor("Image " + options.outWidth + "x" + options.outHeight + "px");
			return metaData;			
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
	}

	@Override
	public Bitmap getCover(File file)
	{
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			//if (options.o > MetaDataFactory.MAX_COVER_WIDTH * 2 || height > MetaDataFactory.MAX_COVER_HEIGHT * 2)
				//options.inSampleSize = 2;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(file), null, options);
			if (options.outWidth <= 0 || options.outHeight <= 0)
				return null;
			
			options.inJustDecodeBounds = false;
			options.inSampleSize = 1;
			
			int width = options.outWidth;
			int height = options.outHeight;
			
			while (width > BaseActivity.DisplayMetrics.widthPixels || height > BaseActivity.DisplayMetrics.heightPixels)
			{
				width /= 2;
				height /= 2;
				options.inSampleSize *= 2;
			}
			
			return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
		}
		catch (OutOfMemoryError e)
		{
			return null;
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
	}

	@Override
	public boolean extractCovers()
	{
		return false;
	}
}

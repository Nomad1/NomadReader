package net.runserver.library.metaData;

import java.io.File;

import android.graphics.Bitmap;

public interface IMetaDataReader
{
	public MetaData getMetaData(File file);
	public Bitmap getCover(File file);
	
	public boolean extractCovers();
}

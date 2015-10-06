package net.runserver.library.metaData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class MetaDataFactory
{
	public static final int MAX_COVER_WIDTH = 160;
	public static final int MAX_COVER_HEIGHT = 240;

	private static final Hashtable<String, IMetaDataReader> s_metaDataReaders = new Hashtable<String, IMetaDataReader>();
	private static MetaDataDB s_metaDataDb = null;

	static
	{
		// TODO: this should be done in external registrar file
		EpubMetaDataReader epubInstance = new EpubMetaDataReader();
		registerMetaDataReader("application/epub", epubInstance);
		registerMetaDataReader("application/epub+zip", epubInstance);

		Fb2MetaDataReader fb2instance = new Fb2MetaDataReader();
		registerMetaDataReader("application/fb2", fb2instance);
		registerMetaDataReader("application/fb2+zip", fb2instance);
		registerMetaDataReader("application/fb2.zip", fb2instance);

		ImageMetaDataReader imageInstance = new ImageMetaDataReader();

		registerMetaDataReader("image/jpeg", imageInstance);
		registerMetaDataReader("image/pjpeg", imageInstance);
		registerMetaDataReader("image/jpg", imageInstance);
		registerMetaDataReader("image/pjpg", imageInstance);
		registerMetaDataReader("image/png", imageInstance);
	}

	public static void registerMetaDataReader(String mimeType, IMetaDataReader reader)
	{
		s_metaDataReaders.put(mimeType, reader);
	}

	public static boolean knownType(String mimeType)
	{
		return s_metaDataReaders.containsKey(mimeType);
	}

	public static Bitmap getCover(MetaData metaData, String path, String cachePath, boolean isDirectory)
	{
//		if (!metaData.isValid())
//			return null;

		Bitmap result = metaData.getCover();

		if (result != null && !result.isRecycled())
			return result;

		String extension;

		if (isDirectory)
			extension = "";
		else
			extension = Utils.getExtension(path);

		String coverFileName = null;

		if (metaData.getLastCoverFile() != null && new File(metaData.getLastCoverFile()).exists())
			coverFileName = metaData.getLastCoverFile();
		else
		{
			String cacheFileName;

			if (cachePath == null)
			{
				cacheFileName = path;
			} else
			{
				File tmp = new File(path);
				cacheFileName = cachePath + "/" + metaData.getFileSize() + "_" + tmp.getName();
			}

			if (isDirectory)
				coverFileName = Utils.seekCover(cacheFileName);
			else
				coverFileName = Utils.seekCover(cacheFileName, extension);
		}

		if (coverFileName != null)
		{
			if (!metaData.hasFileCover())
			{
				// Log.d("FileBrowser", "Updating cover flags: " +
				// metaData.getFlags() );
				metaData.setFlags(metaData.getFlags() | MetaData.COVER);
				metaData.setLastCoverFile(coverFileName);
				s_metaDataDb.putMetaDataDelayed(metaData);
			} else
				metaData.setLastCoverFile(coverFileName);

			try
			{
				result = BitmapFactory.decodeFile(coverFileName);
				if (result != null)
				{
					Log.d("FileBrowser", "Got cover from file " + coverFileName);
					
					new File(coverFileName).setLastModified(java.util.Calendar.getInstance().getTimeInMillis());
					return result;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}

		// Log.d("FileBrowser",
		// "Cover not found, going to use data reader to extract it " + path +
		// ", mime " + metaData.getMimeType());

		if (metaData.getMimeType() != null && s_metaDataReaders.containsKey(metaData.getMimeType()))
		{
			IMetaDataReader dataReader = s_metaDataReaders.get(metaData.getMimeType());

			if (dataReader != null)
			{
				result = dataReader.getCover(new File(path));

				if (result != null && dataReader.extractCovers())
				{
					boolean save = false;

					coverFileName = path.substring(0, path.length() - extension.length() - 1) + ".jpg";
					File coverFile = new File(coverFileName);

					if (cachePath != null)
						coverFileName = cachePath + "/" + metaData.getFileSize() + "_" + coverFile.getName();
					
					try
					{
						coverFile = new File(coverFileName);
						if (coverFile.exists())
							coverFile.delete();
						save = coverFile.createNewFile();
					}
					catch (IOException ex)
					{
						save = false;
					}

					if (save)
					{
						try
						{
							Bitmap bitmap = result;
							int width = result.getWidth();
							int height = result.getHeight();

							if (width > MAX_COVER_WIDTH)
								bitmap = Bitmap.createScaledBitmap(result, MAX_COVER_WIDTH,
										(int) (MAX_COVER_WIDTH * height / (float) width), true);

							if (bitmap.compress(CompressFormat.JPEG, 75, new FileOutputStream(coverFile)))
							{
								Log.d("FileBrowser", "Saved cover to file " + coverFileName);
								if (!metaData.hasFileCover())
								{
									metaData.setFlags(metaData.getFlags() | MetaData.COVER);
									metaData.setLastCoverFile(coverFileName);
									s_metaDataDb.putMetaDataDelayed(metaData);
								} else
									metaData.setLastCoverFile(coverFileName);

							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}
		}

		return result;
	}

	public static MetaData getMetaData(String mimeType, File file)
	{
		if (file == null || file.getPath() == null)
			return null;

		MetaData metaData;

		String name = file.getName();

		if (s_metaDataDb != null)
		{
			metaData = s_metaDataDb.getMetaData(name, file.length(), file.lastModified());
			if (metaData != null)
			{
				if (metaData.getLastPath() == null || !metaData.getLastPath().equals(file.getPath()))
				{
					metaData.setLastPath(file.getPath());
					s_metaDataDb.putMetaDataDelayed(metaData);
				}
				return metaData;
			}
		}

		if (s_metaDataReaders.containsKey(mimeType))
		{
			// Log.d("FileBrowser", "Going to check meta retriever data for " +
			// name);
			metaData = s_metaDataReaders.get(mimeType).getMetaData(file);

			if (metaData != null)
			{
				metaData.setMimeType(mimeType);
				metaData.setLastPath(file.getPath());
				metaData.setFileName(name);
				metaData.setFileSize(file.length());
				metaData.setFileMod(file.lastModified());

				if (!metaData.hasFileCover())
				{
					String coverFile = Utils.seekCover(name, Utils.getExtension(name));
					if (coverFile != null)
						metaData.setFlags(metaData.getFlags() | MetaData.COVER);
				}

				// Log.d("FileBrowser", "Retrieved meta data for " + name);

				s_metaDataDb.putMetaDataDelayed(metaData);

				// Log.d("FileBrowser", "Saved meta data for " + name);
				return metaData;
				// Log.d("FileBrowser", "MetaData not valid for: " + name);
			}
		}

		return null;
	}

	public static List<MetaData> getAllBooks()
	{
		if (s_metaDataDb != null)
			return s_metaDataDb.getAllBooks();
		return null;
	}

	public static List<MetaData> getAllAuthorBooks(String author, int count)
	{
		if (s_metaDataDb != null)
			return s_metaDataDb.getAllAuthorBooks(author, count);
		return null;
	}

	public static void initDB(Context context)
	{
		s_metaDataDb = new MetaDataDB(context);
	}

	public static void closeDB()
	{
		if (s_metaDataDb != null)
			s_metaDataDb.close();
		s_metaDataDb = null;
	}

	public static void flushDB()
	{
		if (s_metaDataDb != null)
			s_metaDataDb.flush();
	}
}

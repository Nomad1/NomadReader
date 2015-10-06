package net.runserver.library.metaData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class EpubMetaDataReader implements IMetaDataReader
{
	private final static int EPUB_MAX_CYCLES = 200;

	private static String getRootFilePath(ZipFile zipFile)
	{
		try
		{
			ZipEntry entry = zipFile.getEntry("META-INF/container.xml");

			if (entry == null)
				return null;

			InputStream stream = zipFile.getInputStream(entry);

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			parser.setInput(stream, null);

			int eventType = parser.getEventType();
			while (eventType != XmlResourceParser.END_DOCUMENT)
			{
				switch (eventType)
				{
					case XmlResourceParser.START_TAG:
						if (parser.getName().equals("rootfile"))
						{
							String full_path = parser.getAttributeValue(null, "full-path");
							if (full_path != null)
							{
								return full_path;
							}
						}
						break;
				}
				eventType = parser.next();
			}
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "EpubMetaDataReader: failed to read EPUB container.xml file: " + ex);
		}
		return null;
	}

	private MetaData getMetaData(InputStream stream)
	{
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			parser.setInput(stream, null);

			MetaData result = null;

			int eventType = parser.getEventType();
			int cycles = 0;
			while (eventType != XmlResourceParser.END_DOCUMENT && cycles < EPUB_MAX_CYCLES)
			{
				switch (eventType)
				{
					case XmlResourceParser.START_TAG:
						if (parser.getName().equals("metadata"))
						{
							result = new MetaData();
							result.setFlags(MetaData.BOOK);
						} else if (parser.getName().equals("dc:title"))
						{
							String title = parser.nextText();

							if (result.getTitle() == null)
							{
								result.setTitle(title);
							} else
							{
								result.setTitle(title);
							}
						} else if (parser.getName().equals("dc:description"))
						{
							String description = parser.nextText();
							result.setDescription(description);
						} else if (parser.getName().equals("dc:creator"))
						{
							String creator = parser.getAttributeValue(null, "p6:file-as");

							if (creator == null)
							{
								creator = parser.getAttributeValue(null, "opf:file-as");

								if (creator == null)
									creator = parser.nextText();
							}

							result.setAuthor(creator);
						} else if (parser.getName().equals("meta") || parser.getName().equals("opf:meta"))
						{
							String metaName = parser.getAttributeValue(null, "name");
							String metaContent = parser.getAttributeValue(null, "content");

							if (metaName != null && metaContent != null)
							{
								if (metaName.equals("calibre:series"))
								{
									result.setSeries(metaContent);
								} else if (metaName.equals("calibre:series_index"))
								{
									int index = 1;
									try
									{
										if (metaContent.contains("."))
											index = (int) Float.parseFloat(metaContent);
										else
											index = (int) Integer.parseInt(metaContent);
									}
									catch (Exception ex)
									{

									}
									result.setPart(index);
								}
							}
						}

						break;

					case XmlResourceParser.END_TAG:
						if (parser.getName().equals("metadata"))
						{
							return result;
						}
						break;
				}
				eventType = parser.next();
				cycles++;
			}
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "EpubMetaDataReader: failed to read EPUB root file: " + ex);
		}
		return null;
	}

	@Override
	public MetaData getMetaData(File file)
	{
		try
		{
			if (!Utils.isValidZipFile(file))
				return null;

			ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);

			try
			{
				String rootFile = getRootFilePath(zipFile);

				if (rootFile != null)
				{
					ZipEntry rootEntry = zipFile.getEntry(rootFile);

					if (rootEntry != null)
					{
						InputStream entryStream = zipFile.getInputStream(rootEntry);

						return getMetaData(entryStream);
					}
				}
			}
			finally
			{
				zipFile.close();
			}
			return null;
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "Epub meta data retrieve failed: " + ex);
			return null;
		}
	}

	@Override
	public Bitmap getCover(File file)
	{
		try
		{
			if (!Utils.isValidZipFile(file))
				return null;

			ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);

			try
			{
				String rootFile = getRootFilePath(zipFile);

				if (rootFile != null)
				{
					ZipEntry rootEntry = zipFile.getEntry(rootFile);

					if (rootEntry != null)
					{
						InputStream stream = zipFile.getInputStream(rootEntry);

						String coverPath = getCoverPath(stream);

						if (coverPath == null)
						{
							Log.d("FileBrowser", "Cover path not found in EPUB file, trying cover.jpg");
							coverPath = "images/cover.jpg";
							//return null;
						}

						Log.d("FileBrowser", "Checking cover at path " + coverPath);

						ZipEntry coverEntry = zipFile.getEntry(coverPath);

						if (coverEntry == null)
						{
							int slash = rootFile.lastIndexOf("/");
							if (slash != -1)
							{
								coverPath = rootFile.substring(0, slash + 1) + coverPath;
								coverEntry = zipFile.getEntry(coverPath);

								Log.d("FileBrowser", "Checking cover at path " + coverPath);
							}
						}

						if (coverEntry == null)
							return null;
						
						//Log.d("FileBrowser", "Processing cover entry size " + coverEntry.getSize());						
						
						byte [] data = new byte[(int)coverEntry.getSize()];
						InputStream istream = new BufferedInputStream(zipFile.getInputStream(coverEntry), 0x2000);
						int len = istream.read(data);
						
						//Log.d("FileBrowser", "Processed cover entry size " + len);
						
						return BitmapFactory.decodeByteArray(data, 0, len);		
					}
				}
			}
			finally
			{
				zipFile.close();
			}
			return null;
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "Epub cover data retrieve failed: " + ex);
			return null;
		}
	}

	private String getCoverPath(InputStream stream)
	{
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			parser.setInput(stream, null);

			int eventType = parser.getEventType();
			int cycles = 0;
			String metaCoverName = null;
			while (eventType != XmlResourceParser.END_DOCUMENT && cycles < EPUB_MAX_CYCLES)
			{
				switch (eventType)
				{
					case XmlResourceParser.START_TAG:
						if (parser.getName().equals("embeddedcover"))
						{
							metaCoverName = parser.nextText();
						} else
						if (parser.getName().equals("meta"))
						{
							String metaName = parser.getAttributeValue(null, "name");
							String metaContent = parser.getAttributeValue(null, "content");

							if (metaName != null && metaContent != null)
							{
								if (metaName.equals("cover") && metaContent.length() > 0)
									metaCoverName =  metaContent;
							}
						} else
						if (parser.getName().equals("item"))
						{
							String itemId = parser.getAttributeValue(null, "id");
							String itemHref = parser.getAttributeValue(null, "href");
							
							if (metaCoverName != null && itemId.equals(metaCoverName))
								return itemHref;
						}
						break;

					case XmlResourceParser.END_TAG:
						if (parser.getName().equals("metadata"))
						{
							if (metaCoverName == null)
								return null;
						} else
						if (parser.getName().equals("manifest"))
						{
							return metaCoverName;
						}
						break;
				}
				eventType = parser.next();
			}
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "EpubMetaDataReader: failed to read EPUB root file for cover: " + ex);
		}
		return null;
	}
	
	@Override
	public boolean extractCovers()
	{
		return true;
	}
}

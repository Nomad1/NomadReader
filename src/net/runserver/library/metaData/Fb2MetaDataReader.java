package net.runserver.library.metaData;

import java.io.BufferedInputStream;
//import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.runserver.common.Base64InputStream;
import net.runserver.common.InputByteStream;
import net.runserver.common.XmlReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

public class Fb2MetaDataReader implements IMetaDataReader
{
	private final static int FB2_MAX_CYCLES = 200;
	private final static int FB2_MAX_HEADER = 0x10000;
	//private final static int FB2_BINARY_SEEK_STEP = 0x2000;

	//private final static byte[] s_seekBuffer = new byte[FB2_BINARY_SEEK_STEP];
	//private final static byte[] s_binaryBytes = "<binary".getBytes();
	
	private MetaData getMetaData(InputStream stream, long size)
	{
		try
		{
			/*XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			try
			{
				parser.setInput(stream, null);
			}
			catch(XmlPullParserException ex)
			{
				if (ex.getMessage().contains("java.io.UnsupportedEncodingException"))
				{
					Set<String> charsetNames = Charset.availableCharsets().keySet();
					for (String name : charsetNames) {
					    Log.d("TextReader:", "Charset: " + name);
					}
					
					parser.setInput(stream, "Cp1252");
					charset = new Charset1251();
				} else
					throw ex;
			}*/
			
			XmlReader parser = new XmlReader(stream, (int)size);

			MetaData result = null;

			CharSequence middleName = null;
			CharSequence firstName = null;
			CharSequence lastName = null;
			
			int eventType = parser.getEventType();
			int cycles = 0;
			boolean annotationStarted = false;
			boolean authorStarted = false;
			
			while (eventType != XmlResourceParser.END_DOCUMENT && cycles < FB2_MAX_CYCLES)
			{
				if (annotationStarted)
				{
					if (eventType == XmlResourceParser.END_TAG && parser.getName().equals("annotation"))
					{
						annotationStarted = false;
					} else if (eventType == XmlResourceParser.TEXT)
					{
						String description = result.getDescription();
						String text = parser.nextText().toString();
						
						description += text;
						result.setDescription(description);
					}

				} else
				{
					switch (eventType)
					{
						case XmlResourceParser.START_TAG:
							if (parser.getName().equals("title-info"))
							{
								result = new MetaData();
								result.setFlags(MetaData.BOOK);
							} else if (parser.getName().equals("book-title"))
							{
								String title = parser.nextText().toString();
								result.setTitle(title);
							} else if (parser.getName().equals("annotation"))
							{
								annotationStarted = true;
								result.setDescription("");
							} else if (parser.getName().equals("author"))
							{
								authorStarted = true;
							} else if (authorStarted && parser.getName().equals("first-name"))
							{
								firstName = parser.nextText();								

								if (firstName != null && firstName.length() > 0)
								{
									firstName = firstName.toString().trim();
									
									result.setAuthor(firstName, middleName, lastName);
								}
							} else if (authorStarted && parser.getName().equals("last-name"))
							{
								lastName = parser.nextText();

								if (lastName != null && lastName.length() > 0)
								{
									lastName = lastName.toString().trim();
									
									result.setAuthor(firstName, middleName, lastName);
								}
							} else if (authorStarted && parser.getName().equals("middle-name"))
							{
								middleName = parser.nextText();

								if (middleName != null && middleName.length() > 0)
								{
									middleName = middleName.toString().trim();
									result.setAuthor(firstName, middleName, lastName);
								}
							} else if (parser.getName().equals("sequence"))
							{
								if (result.getSeries() == null)
								{
									CharSequence series = parser.getAttributeValue(null, "name");

									if (series != null)
									{
										result.setSeries(series.toString());

										int number;

										try
										{
											number = Integer.parseInt(parser.getAttributeValue(null, "number").toString());
										}
										catch (Exception ex)
										{
											number = 1;
										}

										result.setPart(number);
									}
								}
							}

							break;

						case XmlResourceParser.END_TAG:
							if (parser.getName().equals("author"))
							{
								authorStarted = false;
							}
							else
							if (parser.getName().equals("description") || parser.getName().equals("title-info"))
							{
								return result;
							}
							break;
					}
				}
				eventType = parser.next();
				cycles++;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log.d("FileBrowser", "FB2MetaDataReader: failed to parse FB2 file: " + ex);
		}
		return null;
	}

	private static String getCoverPageHref(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		int eventType;
		String result = null;
		do
		{
			eventType = parser.next();
			switch (eventType)
			{
				case XmlResourceParser.START_TAG:
					if (parser.getName().equals("image"))
					{
						String value = parser.getAttributeValue(null, "l:href");
						if (value != null)
							result = value;
						else
						{
							value = parser.getAttributeValue(null, "xlink:href");
							if (value != null)
								result = value;
						}
					}
					break;
				case XmlResourceParser.END_TAG:
					if (parser.getName().equals("coverpage"))
						return result;
					break;
			}
		} while (eventType != XmlResourceParser.END_DOCUMENT);

		return result;
	}

	private static String getCoverPageHref(InputStream stream)
	{
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			parser.setInput(stream, "UTF-8");

			boolean done = false;
			int eventType = parser.getEventType();
			while (eventType != XmlResourceParser.END_DOCUMENT && !done)
			{
				switch (eventType)
				{
					case XmlResourceParser.START_TAG:
						if (parser.getName().equals("coverpage"))
						{
							String coverPageHref = getCoverPageHref(parser);
							if (coverPageHref != null)
								return coverPageHref;
						} else
						if (parser.getName().equals("body"))
							return null;
						break;
					case XmlResourceParser.END_TAG:
						if (parser.getName().equals("description"))
							return null;
						break;
				}
				eventType = parser.next();
			}
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "FB2MetaDataReader: failed to parse FB2 file for cover href: " + ex);
		}
		return null;
	}

	/*private static boolean isBase64Fragment(byte [] data, int offset, int length) throws IOException
	{
		int end = length + offset;
		for (int i = offset; i < end; i++)
		{
			switch (data[i])
			{
				case ' ':
				case '<':
				case '>':
					return false;
			}
		}
		return true;
	}*/

	/*
	public static boolean skipToUniquePattern(InputStream stream, byte[] pattern) throws IOException
	{
		if (stream.available() <= 0)
			return false;

		stream.mark(pattern.length + 1);
		int pos = 0;
		int i = 0;
		do
		{
			byte b = (byte) stream.read();
			pos++;

			if (b == pattern[i])
			{
				i++;
				if (i == pattern.length)
				{
					stream.reset();
					return true;
				}
			} else
			{
				i = 0;
				stream.mark(pattern.length + 1);
			}

		} while (stream.available() > 0);
		return false;
	}

	public static boolean skipToUniquePatternBlock(InputStream stream, byte[] pattern) throws IOException
	{
		if (stream.available() <= 0)
			return false;

		do
		{
			stream.mark(s_seekBuffer.length);
			int read = stream.read(s_seekBuffer);

			int j = 0;
			int jpos = 0;
			for (int i = 0; i < read; i++)
			{
				if (s_seekBuffer[i] == pattern[j])
				{
					if (j == 0)
						jpos = i;
					j++;
					if (j == pattern.length)
					{
						stream.reset();
						return true;
					}
				} else
					j = 0;
			}

			if (j != 0)
			{
				 stream.reset();
				 stream.skip(jpos - 1);
				 stream.mark(s_seekBuffer.length * 2);
				 stream.skip(s_seekBuffer.length - (jpos - 1));
			}

		} while (stream.available() > 0);

		return false;
	}
*/
	
	//private final static SearchPattern s_binaryStart = new SearchPattern(new byte[]{'<','b','i','n','a','r','y',' '});
		//new SearchPattern("<binary");
	
	private Bitmap processBase64Block(InputByteStream binaryStream, String coverHref)
	{
		long start = SystemClock.elapsedRealtime();

		try
		{
/*			int bstart = binaryStream.indexOf(s_binaryStart);
			//if (!binaryStream.skipTo(new String("<binary").getBytes(), true))
			if (bstart == -1)
			{
				Log.d("FileBrowser", "FB2MetaDataReader: cover binary name not found");
				return null;
			}
			
			binaryStream.reset(bstart);*/			
			
			if (!binaryStream.skipTo(new String(coverHref).getBytes(), true))
			{
				Log.d("FileBrowser", "FB2MetaDataReader: cover binary name not found");
				return null;
			}

			if (!binaryStream.skipTo((byte)'>'))
			{
				Log.d("FileBrowser", "FB2MetaDataReader: cover binary block end not found");
				return null;
			}

			Log.d("FileBrowser", "FB2MetaDataReader: seek to base64 start took " + (SystemClock.elapsedRealtime() - start));

			
			byte [] data = binaryStream.getBuffer();
			int offset = binaryStream.getPosition();
			
			//InputStream stream = new Base64InputStream(new ByteArrayInputStream(data, offset, binaryStream.getSize() - binaryStream.getPosition()));
			
			int [] resultLength = { 0 };
			byte [] image = Base64InputStream.processBase64(data, offset, binaryStream.getSize() - offset, resultLength);

			if (image == null)
			{
				Log.e("FileBrowser", "FB2MetaDataReader: failed to decode base64 block ");
				return null;
			}
			
			//BitmapFactory.Options options = new BitmapFactory.Options();
			//options.inSampleSize = 8;
			//Bitmap result = BitmapFactory.decodeStream(stream, null, options);		
			Bitmap result = BitmapFactory.decodeByteArray(image, 0, resultLength[0]);

			Log.d("FileBrowser", "FB2MetaDataReader: everything took " + (SystemClock.elapsedRealtime() - start) + ", result " + result);
			return result;

		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "FB2MetaDataReader: exception decoding base64 block " + ex);
			return null;
		}
	}

	private Bitmap getCoverData(InputStream stream, long streamSize)
	{
		String coverHref = getCoverPageHref(stream);

		if (coverHref == null)
		{
			Log.d("FileBrowser", "FB2MetaDataReader: cover href is null");
			return null;
		}

		if (coverHref.startsWith("#"))
			coverHref = coverHref.substring(1);

		try
		{
			long start = SystemClock.elapsedRealtime();
			
			//android.os.Debug.startMethodTracing("/cache/textReader");
			
			InputByteStream byteStream = new InputByteStream(stream, (int)streamSize);
			
			/*if (!byteStream.skipTo(s_binaryBytes, true) || byteStream.getSize() - byteStream.getPosition() <= 0)
			{
				Log.d("FileBrowser", "FB2MetaDataReader: skip to binary cover failed");
				return null;
			}			

			Log.d("FileBrowser", "FB2MetaDataReader: base64 seek took " + (SystemClock.elapsedRealtime() - start));*/

			Bitmap result = processBase64Block(byteStream, coverHref);

			//android.os.Debug.stopMethodTracing();
			
			Log.d("FileBrowser", "FB2MetaDataReader: total time : " + (SystemClock.elapsedRealtime() - start) + ", result " + result);

			return result;
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "FB2MetaDataReader: binary cover seek failed: " + ex);
			ex.printStackTrace();
			return null;
		}

		// return null;
	}

	@Override
	public MetaData getMetaData(File file)
	{
		try
		{
			InputStream stream = null;
			ZipFile zipFile = null;

			try
			{
				if (file.getName().endsWith(".zip"))
				{
					if (!Utils.isValidZipFile(file))
						return null;

					zipFile = new ZipFile(file);

					ZipEntry entry;
					Enumeration<? extends ZipEntry> en = zipFile.entries();
					while (en.hasMoreElements())
					{
						entry = en.nextElement();
						MetaData result = getMetaData(zipFile.getInputStream(entry), FB2_MAX_HEADER);
						if (result != null)
							return result;
					}

				} else
				{
					
					stream = new FileInputStream(file);
					//BufferedInputStream bstream = new BufferedInputStream(stream, 0x2000);

					//if (!Utils.isValidXmlFile(bstream))
					//	return null;

					return getMetaData(stream, FB2_MAX_HEADER);
				}

			}
			finally
			{
				if (zipFile != null)
					zipFile.close();
				if (stream != null)
					stream.close();
			}
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "FB2 meta data retrieve failed: " + ex);
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public Bitmap getCover(File file)
	{
		try
		{
			InputStream stream = null;
			ZipFile zipFile = null;

			try
			{
				if (file.getName().endsWith(".zip"))
				{
					if (!Utils.isValidZipFile(file))
					{
						Log.d("FileBrowser", "Not valid zip file for cover " + file.getName());
						return null;
					}

					zipFile = new ZipFile(file);

					ZipEntry entry;
					Enumeration<? extends ZipEntry> en = zipFile.entries();
					while (en.hasMoreElements())
					{
						entry = en.nextElement();

						Bitmap result = getCoverData(new BufferedInputStream(zipFile.getInputStream(entry), 0x2000), entry.getSize());
						if (result != null)
							return result;
					}

				} else
				{
					stream = new FileInputStream(file);
					BufferedInputStream bstream = new BufferedInputStream(stream, 0x2000);

					if (!Utils.isValidXmlFile(bstream))
					{
						Log.d("FileBrowser", "Not valid xml file for cover " + file.getName());
						return null;
					}

					return getCoverData(bstream,/* new BufferedInputStream(new FileInputStream(file), 0x2000), */file.length());
				}

			}
			finally
			{
				if (zipFile != null)
					zipFile.close();
				if (stream != null)
					stream.close();
			}
		}
		catch (Exception ex)
		{
			Log.d("FileBrowser", "FB2 cover retrieve failed: " + ex);
			ex.printStackTrace();
		}

		return null;
	}
	
	@Override
	public boolean extractCovers()
	{
		return true;
	}
}

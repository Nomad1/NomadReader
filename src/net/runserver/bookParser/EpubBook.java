package net.runserver.bookParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.runserver.common.Pair;
import net.runserver.common.XmlReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class EpubBook extends BaseBook
{
	private final String m_fileName;
	
	public EpubBook(String fileName)
	{
		m_title = fileName;
		m_fileName = fileName;
	}

	@Override
	public boolean init(String cachePath)
	{
		if (m_inited)
			return true;
		super.init(cachePath);
		
		m_styles.put("title", BaseBookReader.HEADER_1);
		m_styles.put("title2", BaseBookReader.HEADER_2);
		m_styles.put("title3", BaseBookReader.HEADER_3);
		m_styles.put("subtitle", BaseBookReader.SUBTITLE);
		m_styles.put("epigraph", BaseBookReader.SUBTITLE);
		
		m_inited = true;
		
		try
		{
			long start = System.currentTimeMillis();
			BookData data = getContent(m_fileName, cachePath);
		
			Log.d("TextReader", "Initing readers took " + (System.currentTimeMillis() - start));
			
			if (data == null)
				return false;
		
			checkLanguage(data);
			
			m_reader = new EpubBookReader(data, m_title);
			m_reader.setMaxSize(data.getMaxPosition());
			
			return true;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	private BookData getContent(String fileName, String cachePath)
	{
		XmlBookParser bookParser = new XmlBookParser();
		try
		{
			//File file = new File(fileName);
			ZipFile zipFile = new ZipFile(fileName);

			try
			{
				String rootFile = getRootFilePath(zipFile);

				if (rootFile != null)
				{
					ZipEntry rootEntry = zipFile.getEntry(rootFile);

					if (rootEntry != null)
					{
						InputStream entryStream = zipFile.getInputStream(rootEntry);

						List<EpubContentEntry> contents = getContents(entryStream, zipFile, rootFile);

						//List<BaseBookReader> readers = new ArrayList<BaseBookReader>(contents.size());

						for (EpubContentEntry content:contents)
						{
							String contentPath = URLDecoder.decode(content.getHRef()).toString();
							
							//Log.d("TextReader", "Checking content entry " + contentPath + ", type " + content.getType());							
							
							ZipEntry contentEntry = zipFile.getEntry(contentPath);

							if (contentEntry == null)
							{
								int slash = rootFile.lastIndexOf("/");
								if (slash != -1)
								{
									contentPath = rootFile.substring(0, slash + 1) + contentPath;
									contentEntry = zipFile.getEntry(contentPath);
								}
							}

							if (contentEntry != null)
							{
								InputStream stream = new BufferedInputStream(zipFile.getInputStream(contentEntry), 0x2000);
								int length = (int)contentEntry.getSize();
								
								if (content.getType().equals("application/xhtml+xml"))
								{								
									//Log.d("TextReader", "Initing content entry " + contentPath + ", size " + contentEntry.getSize());
									XmlReader reader = new XmlReader(stream, length);
									int position = bookParser.parse(reader);
									reader.clean();
									
									/*
									  BookLine [] lines = data.getLines();									
									 
									String [] tags = data.getTags();
									
									long bodyMask = 0;
									long titleMask = 0;
									for (int i = 0; i < tags.length; i++)
									{
										if (tags[i].equals("body"))
											bodyMask = 1l << i;
										else
											if (tags[i].equals("title"))
												titleMask = 1l << i;
									}
									
									BaseBookReader reader = initReader(data);
									reader.init();
									
									CharSequence ctitle = null;
									
									if (bodyMask != 0)
									for(BookLine line :lines)
									{										
										if (ctitle == null && titleMask != 0 && (line.getTagMask() & titleMask) != 0)
											ctitle = line.getText();
											
										if ((line.getTagMask() & bodyMask) == 0)
										{
											line.setText(null);
											line.setAttributes(null);
										} else										
											line.optimize();
									}
									*/
									
									//if (!reader.isFinished() || reader.getText() != null/* || reader.getImageSrc() != null*/)
									if (position != -1)
									{
										//Log.d("TextReader", "Got content entry " + contentPath);
										//readers.add(reader);
										String title = content.getName();
										//if (!content.isValid() && ctitle != null && ctitle.length() > 0)
										//	title = ctitle.toString();
										
										/*if (m_firstLineIdent != 0.0f && reader.getText().toString().contains("	��"))
										{
											m_firstLineIdent = 0.0f;
										}*/
										
										m_chapters.add(new Pair<Long,String>((long)position, title));
									}
								} else
									if (content.getType().equals("application/x-font-ttf") || contentPath.endsWith(".ttf"))
									{
										try
										{	
											Log.d("TextReader", "Got font file " + contentPath);
											FontData font = new FontData(contentPath, stream, length);
											font.extractFont(cachePath);
											m_fonts.add(font);
										}
										catch(Exception ex)
										{
											Log.w("TextReader", "Failed to read font data " + ex);
										}
									} else
										if (content.getType().startsWith("image/") || contentPath.endsWith(".jpg")  || contentPath.endsWith(".jpeg"))
										{
											try
											{
												Log.d("TextReader", "Got image file " + contentPath);
												ImageData image = new ImageData(content.getHRef(), stream, length);
												image.init(cachePath);
												m_images.add(image);
											}
											catch(Exception ex)
											{
												Log.w("TextReader", "Failed to read image data " + ex);
											}
										} else
											if (content.getType().equals("text/css"))
											{
												parseCSS(stream);
											}
								
								stream.close();
							}
						}
						
						if (bookParser.isEmpty())
							return null;
						return bookParser.bake();
						//return readers.toArray(new BaseBookReader[readers.size()]);
					}
				}
			}
			finally
			{				
				zipFile.close();
			}
			Log.e("FileBrowser", "Invalid epub file!");
			return null;
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "Epub meta data retrieve failed: " + ex);
			ex.printStackTrace();
			return null;
		}
	}
	
	private void parseCSS(InputStream stream) throws IOException
	{		
		//Map<String, Map<String, String>> styles = new HashMap<String, Map<String, String>>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 0x2000);
		String className = "";
		boolean classOpen = false;
		while(reader.ready())
		{
			String line = reader.readLine();
			if (line == null)
				break;
			
			line = line.trim();
			
			if (line.length() == 0)
				continue;
																
			if (classOpen)
			{
				String [] elements = line.split(":", 2);
				if (elements.length == 2)
				{
					String param = elements[0].trim();
					String value = elements[1].trim();
					if (value.endsWith("}"))
						value = value.substring(0, value.length() - 1).trim();
					if (value.endsWith(";"))
						value = value.substring(0, value.length() - 1).trim();
					
					if (param.equals("text-align"))
					{
						if (value.equals("justify"))
						{
							int dot = className.lastIndexOf('.');
							String nclassName = dot == -1 ? className : className.substring(dot + 1);
							
							m_styles.put(nclassName, BaseBookReader.JUSTIFY);
						
							Log.d("TextReader", nclassName + ": " + param + "=" + value);
						}
					
						
						//if (!styles.containsKey(className))
							//styles.put(className, new HashMap<String, String>());
	
						//styles.get(className).put(param, value);
					}
					
					/*if ((className.equals("p") || className.startsWith("p.")) && (param.equals("text-indent")))
					{
						m_firstLineIdent = 55.0f; // TODO: parse
						return;
					}*/
					//Log.d("TextReader", className + ": " + param + "=" + value);
				}
				if (line.contains("}"))
					classOpen = false;
			} else
			{
				int index = line.lastIndexOf("{");
				if (index != -1)
				{
					String name = line.substring(0, index).trim();
					if (name.length() > 0)
						className = name;
					classOpen = true;
				} else														
					className = line;
			}
		}
	}

	private static int getNumber(String str)
	{
		StringBuilder result = new StringBuilder(5);
		for(int i=0;i<str.length();i++)
		{
			if (Character.isDigit(str.charAt(i)))
				result.append(str.charAt(i));
		}							
		try
		{
			return Integer.parseInt(result.toString());
		}
		catch(NumberFormatException ex)
		{
			return -1;
		}							
	}
	private List<EpubContentEntry> getContents(InputStream opfStream, ZipFile zipFile, String rootFile)
	{
		List<EpubContentEntry> result = new ArrayList<EpubContentEntry>();

		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			parser.setInput(opfStream, null);

			int eventType = parser.getEventType();
			
			String ncxName = null;
			int spineRef = 0;
			
			while (eventType != XmlResourceParser.END_DOCUMENT)
			{
				switch (eventType)
				{
					case XmlResourceParser.START_TAG:
						String tag = parser.getName();
						if (tag.equals("item"))
						{
							String id = parser.getAttributeValue(null, "id");
							String href = parser.getAttributeValue(null, "href");
							String type = parser.getAttributeValue(null, "media-type");

							if (type.equals("application/xhtml+xml") && id.equals("content"))
								id = "title_page";
							else
								if (id.equals("ncx") || type.equals("application/x-dtbncx+xml"))
									ncxName = href;
							
							result.add(new EpubContentEntry(href, id, type, getNumber(href)));
							
						} else if (tag.equals("dc:title") || tag.equals("title"))
							m_title = parser.nextText();
						else
							if (tag.equals("dc:language"))
								m_language = parser.nextText();
						else
							if (tag.equals("itemref"))
							{
								String idref = parser.getAttributeValue(null, "idref");
								
								for(int i=0;i<result.size();i++)
								if (result.get(i).getId().equals(idref))
								{
									spineRef++;
									result.get(i).setPlayOrder(spineRef);									
									break;
								}
							}
						break;
				}
				eventType = parser.next();
			}			
			
			if (ncxName == null)
			{
				ncxName = rootFile.substring(0, rootFile.lastIndexOf('.') + 1) + "ncx";
			} else
			{				
				ncxName = rootFile.substring(0, rootFile.lastIndexOf('/') + 1) + ncxName;
			}
			
			Log.d("TextReader", "Checking ncx file " + ncxName);
			ZipEntry ncxEntry = zipFile.getEntry(ncxName);
			
			if (ncxEntry != null)
			{				
				InputStream ncxStream = zipFile.getInputStream(ncxEntry);
				
				parser = factory.newPullParser();
				parser.setInput(ncxStream, null);

				eventType = parser.getEventType();
				
				int currentId = -1;
				String label = null;
				int playOrder = -1;
				
				while (eventType != XmlResourceParser.END_DOCUMENT)
				{
					switch (eventType)
					{
						case XmlResourceParser.START_TAG:
						{
							String tag = parser.getName();
							if (tag.equals("navPoint"))
							{
								//String id = parser.getAttributeValue(null, "id");
								try
								{
									playOrder = Integer.parseInt(parser.getAttributeValue(null, "playOrder"));
								}
								catch(NumberFormatException ex)
								{
									playOrder = -1;
								}
								
//								for(int i=0;i<result.size();i++)
//								{
//									if (result.get(i).getId().equals(id))
									//{
//										currentId = i;
										//break;
									//}
								//}
								
							} else
								if (tag.equals("text"))
								{
									label = parser.nextText();
									//Log.d("TextReader", "Got Label " + label);									
								} else
									if (tag.equals("content"))
									{
										String src = parser.getAttributeValue(null, "src");
										//Log.d("TextReader", "Got part src " + src);
										
										int ind = src.indexOf('#');
										if (ind != -1)
											src = src.substring(0, ind);
										
										for(int i=0;i<result.size();i++)
										{
											if (result.get(i).getHRef().equals(src))
											{
												//Log.d("TextReader", "Found id " + i);
												currentId = i;
												
												if (label != null)
												{
													EpubContentEntry entry = result.get(currentId);
													if (label != null && label.length() > 0)
														entry.setName(label);
													
													if (playOrder != -1 && spineRef == 0)
														entry.setPlayOrder(playOrder);
													
													entry.setValid(true);
													
													currentId = -1;
													playOrder = -1;
													label = null;
												}
												break;
											}
										}
									}
							break;
						}
						case XmlResourceParser.END_TAG:
						{
							String tag = parser.getName();
							if (tag.equals("navPoint"))
							{
								if (currentId != -1)
								{
									EpubContentEntry entry = result.get(currentId);
									if (label != null && label.length() > 0)
										entry.setName(label);
									
									if (playOrder != -1 && spineRef == 0)
										entry.setPlayOrder(playOrder);
								} //else Log.w("TextReader", "Missed label " + label);
								
								currentId = -1;
								playOrder = -1;
								label = null;
							}
							break;
						}
					}
					eventType = parser.next();
				}
				
			} 
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "EpubMetaDataReader: failed to read EPUB root file: " + ex);
		}
		
		Collections.sort(result, new  Comparator<EpubContentEntry>()
				{
					@Override
					public int compare(EpubContentEntry one, EpubContentEntry another)
					{
						int oneOrder = one.getPlayOrder();
						int anotherOrder = another.getPlayOrder();
						return oneOrder == anotherOrder ? 0 : oneOrder > anotherOrder ? 1 : -1;
					}
				});
		
		String lastName = null;
		
		for(EpubContentEntry contentEntry:result)
		{
			if (contentEntry.isValid())
				lastName = contentEntry.getName();
			else
				if (lastName != null)
					contentEntry.setName(lastName);				
		}
		
		return result;
	}

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
			Log.e("FileBrowser", "EpubMetaDataReader: failed to read EPUB container.xml file: " + ex);
		}
		return null;
	}
	
	
	private class EpubContentEntry
	{
		private final String m_href;
		private final String m_id;
		private final String m_type;
		private String m_name;
		private int m_playOrder;
		private boolean m_valid;
		
		public String getHRef()
		{
			return m_href;
		}
		
		public String getName()
		{
			return m_name;
		}
		
		public String getId()
		{
			return m_id;
		}
		
		public String getType()
		{
			return m_type;
		}
		
		public boolean isValid()
		{
			return m_valid;
		}
		
		public void setValid(boolean value)
		{
			m_valid = value;
		}
		
		public void setName(String value)
		{
			m_name = value;
		}
		
		public int getPlayOrder()
		{
			return m_playOrder;
		}
		
		public void setPlayOrder(int value)
		{
			m_playOrder = value;
		}
		
		public EpubContentEntry(String href, String id, String type, int playOrder)
		{
			m_href = href;
			m_id = id;
			m_type = type;
			m_name = href;
			m_playOrder = playOrder;
		}
	}	
}

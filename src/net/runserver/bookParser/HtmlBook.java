package net.runserver.bookParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;

import net.runserver.common.FixedCharSequence;
import net.runserver.common.FixedStringBuilder;
import net.runserver.common.Pair;
import net.runserver.common.XmlReader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.util.Log;

public class HtmlBook extends BaseBook
{
	private final String m_fileName;
	private boolean m_dirty;

	public HtmlBook(String fileName)
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

			if (data == null)
				return false;

			String [] tags = data.getTags();
			BookLine[] lines = data.getLines(); 
			
			HashMap<String, Long> hashTags = new HashMap<String, Long>(tags.length);
			
			for (int i = 0; i < tags.length; i++)
			{
				hashTags.put(tags[i], 1l << i);
			}
			
			Long bodyMask = hashTags.get("body");
			Long titleMask = hashTags.get("title");	

			String title = "";
			int bookEnd = 0;
			int bookStart = 0;
			
			for(int i=0;i<lines.length;i++)
			{
				BookLine line = lines[i];
				
				long mask = line.getTagMask();
				
				if (titleMask != null && (mask & titleMask) != 0)
				{
					title += line.getText();
					continue;
				}
				
				{
					if ((mask & bodyMask) != 0)
					{
						bookEnd = i == lines.length - 1 ? line.getPosition() : lines[i+1].getPosition();
						if (bookStart == 0)
							bookStart = line.getPosition();
					}
				}
				line.optimize();
			}
			
			if (title.length() > 0)
				m_title = title;
			
			m_chapters.add(0, new Pair<Long,String>(0l, title.length() > 0 ? title : "Title Page"));
			
			Log.d("TextReader", "Initing readers took " + (System.currentTimeMillis() - start));

			checkLanguage(data);

			m_reader = new HtmlBookReader(data, m_title, m_dirty);
			m_reader.setMaxSize(bookEnd - bookStart);
			m_reader.setBookStart(bookStart);
			m_reader.setMaxSize(data.getMaxPosition());

			return true;
		}
		catch (Exception ex)
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
			File file = new File(m_fileName);
			
			BufferedReader fileReader = new BufferedReader(new FileReader(file));
			String line = fileReader.readLine();
			
			XmlReader reader;
			
			if (line.toLowerCase().contains("xhtml"))
			{
				fileReader.close();
				fileReader = null;
				reader = new XmlReader(new FileInputStream(file), (int)file.length());
				m_dirty = false;
			} else
			{
			    	XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
			    	XmlContentHandler handler = new XmlContentHandler((int)(file.length() * 3 / 2));
			    	xmlReader.setContentHandler(handler);
			    	InputSource source = new InputSource(new FileInputStream(file));
			    	source.setEncoding("cp-1252"); // force no unicode chars!
			    	xmlReader.parse(source);
			    	
			    	FixedCharSequence rawText = handler.getResult(); // it is in 8-bit format, so we can just convert chars to bytes
			    	
			    	byte [] bytes = new byte[rawText.length()];
			    	for(int i=0;i<rawText.length();i++)
			    		bytes[i] = (byte)rawText.charAt(i);
			    	
			    	reader = new XmlReader(bytes, bytes.length);
				reader.setCharset("cp-1251"); // just in case it is 1251 but without valid header
				m_dirty = true;
			}
			
			int position = bookParser.parse(reader);
			reader.clean();
			
			if (position == -1)
			{
				Log.e("FileBrowser", "Bad HTML file!");
				return null;
			}
			
			return bookParser.bake();
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "HTML data retrieve failed: " + ex);
			ex.printStackTrace();
			return null;
		}
	}
	
	private class XmlContentHandler implements ContentHandler
	{
		private final FixedStringBuilder m_builder;
		
		public XmlContentHandler(int size)
		{
			m_builder = new FixedStringBuilder(size);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			m_builder.append(new FixedCharSequence(ch, start, length));
		}

		@Override
		public void endDocument() throws SAXException
		{
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			m_builder.append("</");
			m_builder.append(localName);
			m_builder.append(">");
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException
		{
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
		{
			m_builder.append(new FixedCharSequence(ch, start, length));
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException
		{
			
		}

		@Override
		public void setDocumentLocator(Locator locator)
		{
		}

		@Override
		public void skippedEntity(String name) throws SAXException
		{
		}

		@Override
		public void startDocument() throws SAXException
		{
			
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
		{
			m_builder.append("<");
			m_builder.append(localName);
			m_builder.append(">");
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException
		{
		}
		
		public FixedCharSequence getResult()
		{
			return m_builder.toCharSequence();
		}
	};
}

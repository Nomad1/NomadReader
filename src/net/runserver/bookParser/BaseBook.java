package net.runserver.bookParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.runserver.common.Pair;

public abstract class BaseBook
{
	public static final HashMap<String, Boolean> s_rtlLanguages = new HashMap<String, Boolean>();	
	public static final int CACHE_VERSION = 1;
	
	static
	{
		s_rtlLanguages.put("ar", true); // Arabic
		s_rtlLanguages.put("dv", true); // Divehi
		s_rtlLanguages.put("ha", true); // Hausa
		s_rtlLanguages.put("he", true); // Hebrew
		s_rtlLanguages.put("fa", true); // Persian (Farsi)
		s_rtlLanguages.put("ps", true); // Pashto
		s_rtlLanguages.put("ur", true); // Urdu
		s_rtlLanguages.put("yi", true); // Yiddish
		
	}
		
	protected Map<String, Integer> m_styles = new HashMap<String, Integer>();
	protected Map<String, List<BookLine>> m_notes = new HashMap<String, List<BookLine>>(); 
	protected List<Pair<Long,String>> m_chapters = new ArrayList<Pair<Long,String>>(32);
	protected List<FontData> m_fonts = new ArrayList<FontData>(2);
	protected List<ImageData> m_images = new ArrayList<ImageData>();
	protected float m_firstLineIdent = 55.0f;
	protected String m_title;
	protected String m_language;
	protected boolean m_inited;	
	protected BaseBookReader m_reader;
	protected boolean m_checkDir = false;
	
	public float getFirstLine()
	{
		return m_firstLineIdent;
	}

	public Map<String, Integer> getStyles()
	{
		return m_styles;
	}
	
	public List<Pair<Long,String>> getChapters()
	{
		return m_chapters;
	}
	
	public List<FontData> getFonts()
	{
		return m_fonts;
	}
	
	public List<ImageData> getImages()
	{
		return m_images;
	}
	
	public BaseBookReader getReader()
	{
		return m_reader;
	}
	
	public Map<String, List<BookLine>> getNotes()
	{
		return m_notes;
	}
	
	public String getTitle()
	{
		return m_title;
	}
	
	
	public boolean init(String cachePath)
	{
		m_styles = new HashMap<String, Integer>();
		m_chapters = new ArrayList<Pair<Long,String>>(32);
		m_fonts = new ArrayList<FontData>();
		m_images = new ArrayList<ImageData>();
		m_firstLineIdent = 55.0f;
		m_inited = true;
		return true;
	}
	
	public void clean()
	{
		if (m_images != null)
			for(ImageData image: m_images)
				image.clean();
					
		m_images = null;
		m_fonts = null;
		m_chapters = null;
		m_notes = null;
		m_styles = null;
		m_reader = null;
				
	}
	
	//protected abstract BaseBookReader initReader(BookData data);
	
	/*public void serialize(String path, String targetName, long size)
	{
		File file = new File(path);
		
		try
		{
			file.createNewFile();
		
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
		
			stream.writeInt(CACHE_VERSION);
			stream.writeUTF(targetName);
			stream.writeLong(size);
			
			stream.writeUTF(getTitle());
			stream.writeFloat(getFirstLine());
			stream.writeObject(getChapters());
			stream.writeObject(getFonts());
			stream.writeObject(getImages());
			stream.writeObject(getStyles());
			stream.writeObject(getReader().getData());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	/*
	@SuppressWarnings("unchecked")
	public boolean deserialize(String path)
	{		
		try
		{
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
		
			int version = stream.readInt();
			if (version != CACHE_VERSION)
				return false;
			
			String name = stream.readUTF();
			long nsize = stream.readLong();
			
			m_title = stream.readUTF();
			m_firstLineIdent = stream.readFloat();
			m_chapters = (List<Pair<Long, String>>)stream.readObject();
			m_fonts = (List<FontData>)stream.readObject();
			m_images = (List<ImageData>)stream.readObject();
			m_styles = (Map<String,Integer>)stream.readObject();
			
			Object data = stream.readObject();
			
			if (data.getClass().isArray())
			{
				BookData [] datas = (BookData [])data; 
				BaseBookReader [] readers = new BaseBookReader[datas.length];
				for(int i=0; i< datas.length; i++)
					readers[i] = initReader(datas[i]);
				
				m_reader = new ComplexBookReader(readers, m_title);					
			} else
				m_reader = initReader((BookData)data);
				
			m_inited = true;
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}*/
	
	public static boolean checkFile(String path, String targetName, long size)
	{		
		/*
		try
		{
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
		
			int version = stream.readInt();
			if (version != CACHE_VERSION)
			{
				Log.d("TextReader", "Cache file version invalid: " + CACHE_VERSION);
				return false;
			}
			
			String name = stream.readUTF();
			if (!targetName.equalsIgnoreCase(name))
			{
				Log.d("TextReader", "Cache file name do not match: " + name);
				return false;
			}
			
			long nsize = stream.readLong();
			if (nsize != size)
			{
				Log.d("TextReader", "Cache file size do not match: " + nsize);
				return false;
			}
			
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}*/
		return false;
	}
	
	protected void checkLanguage(BookData data)
	{
		if (m_language == null)
		for(int i=0;i<5;i++)
		{
			BookLine line = data.getLine(i);
			String lang = line.getAttribute("xml:lang");
			if (lang == null)
				lang = line.getAttribute("lang");
			
			if (lang != null)
			{
				m_language = lang;
				break;
			}
		}

		if (m_language != null && s_rtlLanguages.containsKey(m_language))
		{
			m_checkDir = true;
			m_firstLineIdent = 0;
		}
	}
}
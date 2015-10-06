package net.runserver.library.metaData;

import android.graphics.Bitmap;

public class MetaData
{
	public static final int BOOK = 1;
	public static final int COVER = 2;

	private String m_author;
	private String m_authorSortName;
	private String m_description;
	private String m_title;
	private String m_series;
	private String m_mimeType;
	private int m_part;
	private int m_flags;
	private String m_lastPath;
	
	private Bitmap m_cover;
	private String m_lastCoverFile;

	private String m_fileName;
	private long m_fileSize;
	private long m_fileMod;

	public String getAuthor()
	{
		return m_author;
	}

	public String getAuthorSortName()
	{
		return m_authorSortName;
	}
	
	public void setAuthor(String value)
	{
		m_author = value;
		m_authorSortName = value;

		if (!value.contains(","))
		{
			String[] strings = value.split(" ");
			if (strings.length == 2)
				m_authorSortName = strings[1] + ", " + strings[0];
			else if (strings.length == 3)
				m_authorSortName = strings[2] + ", " + strings[0];
		}
	}

	public void setAuthor(CharSequence firstName, CharSequence middleName, CharSequence lastName)
	{
		{
			StringBuilder result = new StringBuilder();
			
			if (firstName != null && firstName.length() > 0)		
				result.append(firstName);
			
			if (middleName != null && middleName.length() > 0)
			{
				if (result.length() > 0)
					result.append(" ");
				result.append(middleName);
			}
				
			if (lastName != null && lastName.length() > 0)
			{
				if (result.length() > 0)
					result.append(" ");
				
				result.append(lastName);
			}
			
			m_author = result.toString();
		}
		
		{
			StringBuilder result = new StringBuilder();
			
			if (lastName != null && lastName.length() > 0)
			{
				result.append(lastName);
			}	
			
			if (firstName != null && firstName.length() > 0)
			{
				if (result.length() > 0)
					result.append(", ");
				
				result.append(firstName);
			}
			
			m_authorSortName = result.toString();
		}
				
	}
	
	public String getDescription()
	{
		return m_description;
	}

	public void setDescription(String value)
	{
		m_description = value;
	}

	public String getTitle()
	{
		return m_title;
	}

	public void setTitle(String value)
	{
		m_title = value;
	}

	public String getSeries()
	{
		return m_series;
	}

	public void setSeries(String value)
	{
		m_series = value;
	}

	public int getPart()
	{
		return m_part;
	}

	public void setPart(int value)
	{
		m_part = value;
	}

	public Bitmap getCover()
	{
		if (m_cover != null && m_cover.isRecycled())
			m_cover = null;
		return m_cover;
	}

	public void setCover(Bitmap value)
	{
		if (m_cover != null && !m_cover.isRecycled())
			m_cover.recycle();
		m_cover = value;
	}
	
	public String getLastCoverFile()
	{
		return m_lastCoverFile;
	}
	
	public void setLastCoverFile(String value)
	{
		m_lastCoverFile = value;
	}

	public String getLastPath()
	{
		return m_lastPath;
	}
	
	public void setLastPath(String value)
	{
		m_lastPath = value;
	}

	public String getMimeType()
	{
		return m_mimeType;
	}

	public void setMimeType(String value)
	{
		m_mimeType = value;
	}
/*
	public boolean isValid()
	{
		return m_fileName != null && m_fileName.length() > 0;
	}
	*/
	public boolean isBook()
	{
		return (m_flags & BOOK) == BOOK;
	}
	
	public boolean hasFileCover()
	{
		return (m_flags & COVER) == COVER;
	}
	
	public void setFlags(int value)
	{
		m_flags = value;
	}

	public int getFlags()
	{
		return m_flags;
	}
	
	public String getFileName()
	{
		return m_fileName;
	}
	
	public void setFileName(String value)
	{
		m_fileName = value;
	}
	
	public void setFileSize(long value)
	{
		m_fileSize = value;
	}
	
	public void setFileMod(long value)
	{
		m_fileMod = value;
	}

	public long getFileSize()
	{
		return m_fileSize;
	}
	
	public long getFileMod()
	{
		return m_fileMod;
	}
	
	public MetaData()
	{
	}

	public MetaData(String title, String author, String description, String series, String mimeType,
			int part, int flags, String fileName, long fileLength, long fileMod)
	{
		m_fileName = fileName;
		m_fileSize = fileLength;
		m_fileMod = fileMod;
		m_title = title;
		
		int pos = author.indexOf("|");
		if (pos != -1)			
		{
			m_author = author.substring(0, pos);
			m_authorSortName = author.substring(pos + 1);
			if (m_authorSortName == null || m_authorSortName.length() == 0)
				m_authorSortName = m_author;
		} else
			m_authorSortName = m_author = author;
		
		m_description = description;
		m_series = series;
		m_part = part;
		m_flags = flags;
		m_mimeType = mimeType;

		m_cover = null;
	}
	
	@Override
	public int hashCode()
	{
	//	if (!isValid())
	//		return 0;
		
		return getHashCode(m_fileName, m_fileSize, m_fileMod);
	}
	
	public static final int getHashCode(String fileName, long fileSize, long fileMod)
	{
		return (int) (fileName.hashCode() ^ fileMod ^ fileSize);
	}
	
	@Override
	public boolean equals(Object o)
	{
	//	if (!isValid())
	//		return false;
		
		if (o.getClass() == MetaData.class)
		{
			MetaData another = (MetaData)o;
			//if (!another.isValid())
			//	return false;
			
			return another.m_title.equals(m_title) && another.m_author.equals(m_author);
		}

		return super.equals(o);
	}

}

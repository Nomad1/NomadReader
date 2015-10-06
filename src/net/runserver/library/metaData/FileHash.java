/*
package net.runserver.library.metaData;

public final class FileHash
{
	private final String m_fileName;
	private final long m_fileSize;
	private final long m_fileMod;
	
	public String getFileName()
	{
		return m_fileName;
	}
	
	public long getFileSize()
	{
		return m_fileSize;
	}
	
	public long getFileMod()
	{
		return m_fileMod;
	}
	
	public FileHash(String fileName, long fileLength, long fileMod)
	{
		m_fileName = fileName;
		m_fileSize = fileLength;
		m_fileMod = fileMod;
	}
	
	@Override
	public int hashCode()
	{
		return (int) (m_fileName.hashCode() + m_fileMod << 8 + m_fileSize);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o.getClass() == FileHash.class)
		{
			FileHash another = (FileHash)o;
			return another.m_fileName.equals(m_fileName) && another.m_fileSize == m_fileSize && another.m_fileMod == m_fileMod; 
		}

		return super.equals(o);
	}
	
	public boolean equals(String fileName, long fileSize, long fileMod)
	{
		return fileName.equals(m_fileName) && fileSize == m_fileSize && fileMod == m_fileMod;
	}
}
*/
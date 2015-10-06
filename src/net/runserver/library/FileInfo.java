package net.runserver.library;

import java.io.File;
import java.util.List;

import net.runserver.library.metaData.MetaData;

public class FileInfo
{
	private String m_path;
	private final String m_name;
	private String m_info;
	private String m_shortInfo;
	private final boolean m_isDirectory;
	private final FileInfo m_parent;
	private final MetaData m_metaData;
	private final long m_lastModification;
	private long m_itemSize;
	private boolean m_isBack;
	private List<FileInfo> m_files;

	public String getPath()
	{
		return m_path;
	}

	public String getName()
	{
		return m_name;
	}

	public String getInfo()
	{
		return m_info;
	}
	
	public String getShortInfo()
	{
		return m_shortInfo;
	}

	public boolean isDirectory()
	{
		return m_isDirectory;
	}
	
	public FileInfo getParent()
	{
		return m_parent;
	}

	public long getLastModification()
	{
		return m_lastModification;
	}
	
	public MetaData getMetaData()
	{
		return m_metaData;
	}
	
	public long getItemSize()
	{
		return m_itemSize;
	}
	
	public void setItemSize(long value)
	{
		m_itemSize = value;
	}	

	public void setPath(String value)
	{
		m_path = value;		
	}
	
	public boolean isBack()
	{
		return m_isBack;
	}
	
	public void setBack(boolean value)
	{
		m_isBack = value;
	}
	
 	public List<FileInfo> getFiles()
	{
		return m_files;
	}
	
	public void setFiles(List<FileInfo> value)
	{
		m_files = value;
	}	
	
	public void setInfo(String value)
	{
		m_info = value;
		m_shortInfo = value;
	}

	public void setLongInfo(String value)
	{
		m_info = value;
	}

	public FileInfo(File file, String name, String info, String shortInfo, FileInfo parent, MetaData metaData, int itemSize)
	{
		m_path = file == null ? "" : file.getAbsolutePath();
		m_name = name;
		m_info = info;
		m_shortInfo = shortInfo;
		m_isDirectory = file == null ? true : file.isDirectory();
		m_parent = parent;
		m_metaData = metaData;
		m_itemSize = itemSize;//file == null ? 0 : m_isDirectory ? file.list() == null ? 0 : file.list().length : file.length();
		m_lastModification = file == null ? 0 : file.lastModified();
	}

	public FileInfo(String path, String name, String info, String shortInfo, FileInfo parent)
	{
		m_path = path;
		m_name = name;
		m_info = info;
		m_shortInfo = shortInfo;
		m_isDirectory = true;
		m_parent = parent;
		m_metaData = null;
		m_itemSize = 0;
		m_lastModification = 0;
	}
}
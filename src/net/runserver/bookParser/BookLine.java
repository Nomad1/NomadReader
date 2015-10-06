/**
 * 
 */
package net.runserver.bookParser;

import net.runserver.common.ByteCharSequence;

public final class BookLine //implements Serializable
{
//	private static final long serialVersionUID = 3L;

	public static final int PART = 1;
	public static final int RTL = 2;
	public static final int PARENT_EMPTY = 4;
	public static final int EMPTY = 8;
	
	private int m_position;
	
	private long m_tagMask;
	private long m_classMask;
	private byte m_flags;
	
	private CharSequence m_attributes;
	private CharSequence m_text;
	
	public long getTagMask()
	{
		return m_tagMask;
	}
	
	public void setTagMask(long value)
	{
		m_tagMask = value;
	}
	
	public long getClassMask()
	{
		return m_classMask;
	}

	public CharSequence getText()
	{
		return m_text;
	}

	public boolean isRtl()
	{
		return (m_flags & RTL) != 0;
	}

	public void setText(CharSequence value)
	{
		m_text = value;
	}
	
	public void setAttributes(CharSequence value)
	{
		m_attributes = value;
	}
	
	public int getPosition()
	{
		return m_position;
	}
	
	public boolean isPart()
	{
		return (m_flags & PART) != 0;
	}
	
	public boolean isParentEmpty()
	{
		return (m_flags & PARENT_EMPTY) != 0;
	}
	
	public boolean isEmpty()
	{
		return (m_flags & EMPTY) != 0;
	}
	
	public String getAttribute(String name)
	{
		if (m_attributes == null)
			return null;

		if (m_attributes.getClass() != String.class)
			m_attributes = m_attributes.toString();
		
		String attributes = (String)m_attributes;

		int nattribute = attributes.indexOf(name, 0);
		if (nattribute == -1)
			return null;

		int nquote = attributes.indexOf("\"", nattribute + 1);
		if (nquote == -1)
			return null;

		int equote = attributes.indexOf("\"", nquote + 1);
		if (equote == -1)
			return null;

		return attributes.subSequence(nquote + 1, equote).toString();
	}	
	
	public BookLine(long tagMask, CharSequence attributes, long classMask, CharSequence text, boolean rtl, int position, boolean parentEmpty)
	{
		m_tagMask = tagMask;
		m_attributes = attributes != null && attributes.length() == 0 ? null : attributes;
		m_text = text != null && text.length() == 0 ? null : text;
		m_position = position;
		m_classMask = classMask;
		m_flags = 0;
		if (parentEmpty)
			m_flags |= PARENT_EMPTY;
		
		if (rtl)			
			m_flags |= RTL;
		
		if (text == null || text.length() == 0)			
			m_flags |= EMPTY;
	}
	
	public BookLine(BookLine parent, CharSequence text, int position)
	{		
		m_tagMask = parent.m_tagMask;
		m_classMask = parent.m_classMask;
		m_attributes = parent.m_attributes;
		m_text = text != null && text.length() == 0 ? null : text;
		m_position = position;
		m_flags = PART;
		
		if (parent.isRtl())			
			m_flags |= RTL;
		
		if (text == null || text.length() == 0)			
			m_flags |= EMPTY;
	}
	/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeLong(m_tagMask);
		out.writeLong(m_classMask);
		out.writeInt(m_position);
		out.writeByte(m_flags);
		out.writeUTF(m_attributes == null ? "" :m_attributes.toString());
		out.writeUTF(m_text == null ? "" :m_text.toString());
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		m_tagMask = in.readLong();
		m_classMask = in.readLong();
		m_position = in.readInt();
		m_flags = in.readByte();
		m_attributes = in.readUTF();
		m_text = in.readUTF();
	}
*/
	public void optimize()
	{
		if (m_attributes != null)
		{
			if (m_attributes instanceof ByteCharSequence)
				m_attributes = ((ByteCharSequence)m_attributes).optimize();
			//else
				//m_attributes = m_attributes.toString();
		}
		if (m_text != null)
		{
			if (m_text instanceof ByteCharSequence)
				m_text = ((ByteCharSequence)m_text).optimize();
			//else
				//m_text = m_text.toString();
		}
	}
}
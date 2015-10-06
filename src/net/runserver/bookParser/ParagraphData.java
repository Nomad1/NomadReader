package net.runserver.bookParser;

public class ParagraphData
{
	private int m_characters;
	private final int m_imageHeight;
	private float m_modifier;
	
	private boolean m_pageBreak;
	
	public boolean isImage()
	{
		return m_imageHeight != 0;
	}
	
	public float getModifier()
	{
		return m_modifier;
	}
	
	public boolean isPageBreak()
	{
		return m_pageBreak;
	}
	
	public int getCharacters()
	{
		return m_characters;
	}
	
	public ParagraphData(int characters, float modifier, boolean pageBreak)
	{
		m_imageHeight = 0;		
		m_characters = characters;
		m_modifier = modifier;
		m_pageBreak = pageBreak;
	}
	
	public ParagraphData(int imageHeight)
	{
		m_imageHeight = imageHeight;
		m_characters = 0;
		m_modifier = 0.0f;
	}

	public boolean addParagraph(ParagraphData another)
	{
		if (m_imageHeight != 0)
			return false;
		
		if (m_modifier != another.getModifier())
			return false;
		
		if (another.isPageBreak())
			m_pageBreak = true;
					
		m_characters += another.getCharacters();
		
		return true;
	}
	
	public int getHeight(float characterHeight, float charsPerLine)
	{
		if (m_imageHeight != 0)
			return m_imageHeight;
		
		return (int)((m_characters / charsPerLine + 1) * (characterHeight * m_modifier));		
	}
}

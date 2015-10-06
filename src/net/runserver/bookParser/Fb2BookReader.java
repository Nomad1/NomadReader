package net.runserver.bookParser;

import java.util.HashMap;

public class Fb2BookReader extends XmlBookReader
{
	private final static HashMap<String, Integer> s_fb2Tags = new HashMap<String, Integer>();

	static
	{
		s_fb2Tags.put("p", JUSTIFY | NEW_LINE | NO_NEW_PAGE);
		s_fb2Tags.put("v",  JUSTIFY | NEW_LINE);
		s_fb2Tags.put("title", HEADER_1 | NEW_LINE);
		s_fb2Tags.put("subtitle", HEADER_2 | NEW_LINE);
		s_fb2Tags.put("epigraph", SUBTITLE);
		s_fb2Tags.put("cite", SUBTITLE);
		s_fb2Tags.put("emphasis", ITALIC | NO_NEW_LINE | NO_NEW_PAGE);
		s_fb2Tags.put("strong", BOLD | NO_NEW_LINE | NO_NEW_PAGE);
		s_fb2Tags.put("a", LINK | NORMAL | NO_NEW_LINE | NO_NEW_PAGE);
		s_fb2Tags.put("empty-line", NEW_LINE);
		s_fb2Tags.put("text-author", BOLD | NEW_LINE);
		s_fb2Tags.put("image", IMAGE);
	}
	
	public Fb2BookReader(BookData data, String title)
	{
		super(data, title, s_fb2Tags, new String[]{"fictionbook", "body", "section"}, true);		
	}	
}

package net.runserver.bookParser;

import java.util.HashMap;

public class HtmlBookReader extends XmlBookReader
{
	private final static HashMap<String, Integer> s_xhtmlTags = new HashMap<String, Integer>();

	static
	{
		s_xhtmlTags.put("p", JUSTIFY | NEW_LINE);
		s_xhtmlTags.put("dd", JUSTIFY | NEW_LINE); // for books from samizdat
		s_xhtmlTags.put("span", NEW_LINE);
		s_xhtmlTags.put("h1", HEADER_1 | NEW_LINE);
		s_xhtmlTags.put("h2", HEADER_2 | NEW_LINE);
		s_xhtmlTags.put("h3", HEADER_3 | NEW_LINE);
		s_xhtmlTags.put("h4", HEADER_4 | NEW_LINE);
		s_xhtmlTags.put("i", ITALIC | NO_NEW_LINE | NO_NEW_PAGE);
		s_xhtmlTags.put("b", BOLD | NO_NEW_LINE | NO_NEW_PAGE);
		s_xhtmlTags.put("strong", BOLD | NO_NEW_LINE | NO_NEW_PAGE);
		s_xhtmlTags.put("div", DIV | NEW_LINE);
		s_xhtmlTags.put("br", NEW_LINE);
		s_xhtmlTags.put("a", LINK | NO_NEW_LINE | NO_NEW_PAGE);
		s_xhtmlTags.put("sup", SUPER | NO_NEW_LINE | NO_NEW_PAGE);
		s_xhtmlTags.put("img", IMAGE);
		s_xhtmlTags.put("image", IMAGE);
	}

	public HtmlBookReader(BookData data, String title, boolean dirty)
	{
		super(data, title, s_xhtmlTags, new String[]{ "html" }, dirty);		
	}
}

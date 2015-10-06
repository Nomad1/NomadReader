package net.runserver.textReader;

import java.io.File;
//import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

import android.util.Log;

public class FontInfo
{
	public static final int NORMAL = 0;
	public static final int ITALIC = 1;
	public static final int BOLD = 2;
	public static final int BOLD_ITALIC = 2;
	public static final int UNKNOWN = 3;
	public static final int MAX = 4;
//		
	
	public final String Name;
	public final String Typeface;
	public final String File;
	public final String Path;
	public final String ID;

	public FontInfo(String id, String file, String name, String typeface)
	{
		ID = id;
		Name = name;
		File = file;
		Path = file.substring(0, file.lastIndexOf('/'));
		Typeface = typeface;
	}
	
	@Override
	public String toString()
	{
		return ID;
	}
	
	public static FontInfo getFontInfo(String path)
	{
		try
		{
			File file = new File(path);
			if (!file.exists())
				return null;			
			
			RandomAccessFile reader = new RandomAccessFile(path, "r");
			reader.order(RandomAccessFile.BIG_ENDIAN);
			//reader.setCharset("UTF-16");
			
			Log.d("TextReader", "Processing font file " + path);

			int major = reader.readShort();
			int minor = reader.readShort();

			if (major != 1 && minor != 0)
			{
				Log.d("TextReader", "Invalid major/minor font data: " + major + ", " + minor);
				return null;
			}

			int tables = reader.readShort();

			reader.skipBytes(6);

			int infoTableOffset = 0;

			char [] buf = new char[4];
			for (int i = 0; i < tables; i++)
			{
				buf[0] = (char)reader.readByte();
				buf[1] = (char)reader.readByte();
				buf[2] = (char)reader.readByte();
				buf[3] = (char)reader.readByte();
				String tableName = new String(buf, 0, 4).toLowerCase();
				//getAsciiLowerString(reader.getPosition(), 4).toString();
				
				//Log.d("TextReader", "Processing table " + tableName);

				if (tableName.equals("name"))
				{
					reader.skipBytes(4);
					infoTableOffset = reader.readInt();
					//Log.d("TextReader", "Got offset " + infoTableOffset);
					break;
				} else
				{
					reader.skipBytes(12);
				}
			}

			if (infoTableOffset == 0)
				return null;

			reader.seek(infoTableOffset);
			reader.skipBytes(2);
			int records = reader.readShort();
			int stringOffset = reader.readShort();
			
			//Log.d("TextReader", "Got records " + records + ", offset " + stringOffset);

			String name = null;
			String typeface = null;
			//String id = null;
			//String psId = null;
			
			boolean valid = true;
			
			for (int i = 0; i < records; i++)
			{
				/*int uPlatformID = */reader.readShort();
				int uEncodingID = reader.readShort();
				/*int uLanguageID = */reader.readShort();
				int uNameID = reader.readShort();
				int uStringLength = reader.readShort();
				int uStringOffset = reader.readShort();
				
				if (uEncodingID > 10 )
					valid = false;

				//Log.d("TextReader", "Got id " + uNameID + ", length " + uStringLength + ", offset " + uStringOffset + ", encoding " + uEncodingID);
				Charset charset;
				
				long pos = reader.getFilePointer();
				reader.seek(infoTableOffset + uStringOffset + stringOffset);
				
				byte [] byteString = new byte[uStringLength];				
				reader.read(byteString);
				
				if (uEncodingID == 0 && byteString[0] != 0)
					charset = Charset.forName("UTF-8");
				else
					charset = Charset.forName("UTF-16");
				
				String string = charset.decode(ByteBuffer.wrap(byteString)).toString();
				reader.seek(pos);
				//getString(infoTableOffset + uStringOffset + stringOffset, uStringLength).toString();
				
				//Log.d("TextReader", "Got string for id " + uNameID + ": " + string + ", char at 0 is " + byteString[0]);

				switch (uNameID)
				{
					case 1: // font name
						if (name == null || name.length() == 0)
							name = string;
						break;
					/*case 3: // id
						if (id == null)
							id = string;
						break;
					case 6: // psid
						if (psId == null)
							psId = string;
						break;*/
					case 2: // typeface
						if (typeface == null)
							typeface = string;
						break;
				}
			}
			
			if (!valid || name == null)
				return null;
			/*
			if (id == null)
				id = psId;
			
			if (id == null)
				id = name;
			
			if (id == null)
				return null;*/
			
			Log.d("TextReader", "Font is " + name + ", typeface " + typeface);
			
			return new FontInfo(name.toLowerCase(), path, name, typeface);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}
	
	public static void scanPath(String path, Map<String, FontInfo[]> fonts)
	{
		scanPath(new File(path), fonts);
	}
	
	public static void scanPath(File file, Map<String, FontInfo[]> fonts)
	{
		if (file.isDirectory())
		{
			File [] files = file.listFiles();
			
			if (files != null && files.length > 0)
				for(File nfile:files)
				{
					if (nfile.isDirectory() || nfile.getName().toLowerCase().endsWith(".ttf"))
						scanPath(nfile, fonts);
				}
			return;
		}
		
		FontInfo info = getFontInfo(file.getPath());
		
		if (info == null)
			return;
		
		String id = info.ID.toLowerCase();
		
		FontInfo[] infos = fonts.get(id);
		if (infos == null)
		{
			infos = new FontInfo[MAX];
			fonts.put(id, infos);
		}
		
		int typeId = UNKNOWN;
		
		String lowerName = file.getName().toLowerCase();
		String lowerTypeface = info.Typeface.toLowerCase();
		
		if (lowerName.equals("normal.ttf") || lowerTypeface.equals("regular") || lowerTypeface.equals("normal") ||  lowerTypeface.equals("roman"))
			typeId = NORMAL;
		else
			if (lowerName.equals("italic.ttf") || lowerTypeface.equals("italic") || lowerTypeface.equals("oblique"))
				typeId = ITALIC;
			else
				if (lowerName.equals("bold.ttf") || lowerTypeface.equals("bold") || lowerTypeface.equals("semibold"))
					typeId = BOLD;
				else
					if (lowerName.equals("bold_italic.ttf") || lowerTypeface.equals("semibolditalic") || lowerTypeface.equals("bolditalic") || lowerTypeface.equals("boldoblique") || lowerTypeface.equals("semiboldoblique"))
						typeId = BOLD_ITALIC;
		
		if (typeId == UNKNOWN && infos[NORMAL] == null)
			typeId = NORMAL;
					
		infos[typeId] = info;
	}
}
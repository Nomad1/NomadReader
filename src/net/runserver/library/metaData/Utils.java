package net.runserver.library.metaData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import android.graphics.BitmapFactory;

public class Utils
{
	private static final Hashtable<String, Boolean> s_archives = new Hashtable<String, Boolean>();

	static
	{		
		s_archives.put("gz", true);
		s_archives.put("bz", true);
		s_archives.put("bz2", true);
		s_archives.put("zip", true);
		s_archives.put("rar", true);		
		s_archives.put("tar", true);
	}


	public static boolean isValidXmlFile(File file) throws IOException
	{
		FileInputStream stream = new FileInputStream(file);

		try
		{
			byte[] signature = new byte[8];
			if (stream.read(signature) != signature.length)
				return false;

			String header = new String(signature);
			return header.indexOf("<?xml") != -1;
		}
		finally
		{
			stream.close();
		}
	}

	public static boolean isValidXmlFile(InputStream stream) throws IOException
	{
		byte[] signature = new byte[8];
		stream.mark(signature.length + 1);

		if (stream.read(signature) != signature.length)
			return false;

		stream.reset();

		String header = new String(signature);
		return header.indexOf("<?xml") != -1;
	}

	public static boolean isValidZipFile(File file) throws IOException
	{
		FileInputStream stream = new FileInputStream(file);
		try
		{

			byte[] signature = new byte[2];
			if (stream.read(signature) != signature.length)
				return false;

			return signature[0] == 'P' && signature[1] == 'K';
		}
		finally
		{
			stream.close();
		}
	}
	
	public static boolean isValidZipFile(InputStream stream) throws IOException
	{
		byte[] signature = new byte[2];
		stream.mark(signature.length + 1);
		
		if (stream.read(signature) != signature.length)
			return false;
		
		stream.reset();

		return signature[0] == 'P' && signature[1] == 'K';
	}

	public static String getExtension(String fileName)
	{
		int dot = fileName.lastIndexOf('.');
		if (dot == -1 || fileName.length() - dot > 5)
			return "";
		String ext = fileName.substring(dot + 1).toLowerCase();

		if (s_archives.containsKey(ext))
		{
			String addExt = getExtension(fileName.substring(0, dot));
			if (addExt != "")
				ext = addExt + "." + ext;
		}

		return ext;
	}
	
	private static String checkCover(String name, String extension)
	{
		StringBuilder builder = new StringBuilder(name.length() + extension.length());
		builder.append(name);
		builder.append(extension);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try
		{
			BitmapFactory.decodeStream(new FileInputStream(builder.toString()), null, options);
		}
		catch (FileNotFoundException e)
		{
			return null;
		}

		if (options.outHeight <= 0 || options.outWidth <= 0)
			return null;

		return builder.toString();
	}

	public static String seekCover(String fileName)
	{
		String result = checkCover(fileName, "/.icon.jpg");
		if (result != null)
			return result;

		result = checkCover(fileName, ".jpg");
		if (result != null)
			return result;

		result = checkCover(fileName, ".jpeg");
		if (result != null)
			return result;

		result = checkCover(fileName, ".png");
		if (result != null)
			return result;

		return null;
	}

	public static String seekCover(String fileName, String extension)
	{
		String rawName = fileName.substring(0, fileName.length() - extension.length() - 1);

		String result = checkCover(rawName, ".jpg");
		if (result != null)
			return result;

		result = checkCover(fileName, ".jpg");
		if (result != null)
			return result;

		result = checkCover(rawName, ".jpeg");
		if (result != null)
			return result;

		result = checkCover(fileName, ".jpeg");
		if (result != null)
			return result;

		result = checkCover(fileName, ".png");
		if (result != null)
			return result;

		result = checkCover(rawName, ".png");
		if (result != null)
			return result;

		return null;
	}	
}

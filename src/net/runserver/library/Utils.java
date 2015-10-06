package net.runserver.library;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class Utils
{
	private static final Hashtable<String, Boolean> s_documents = new Hashtable<String, Boolean>();
	private static final Hashtable<String, Boolean> s_archives = new Hashtable<String, Boolean>();

	static
	{
		// books
		s_documents.put("epub", true);
		s_documents.put("pdf", true);
		s_documents.put("pdb", true);
		s_documents.put("fb2", true);
		s_documents.put("fb2.zip", true);
		s_documents.put("djvu", true);
		s_documents.put("djv", true);
		s_documents.put("doc", true);
		s_documents.put("txt", true);
		s_documents.put("rtf", true);
		s_documents.put("mobi", true);
		s_documents.put("chm", true);

		// other documents
		s_documents.put("html", true);
		s_documents.put("xhtml", true);
		s_documents.put("htm", true);
		
		// pictures
		s_documents.put("jpg", false);
		s_documents.put("jpeg", false);
		s_documents.put("png", false);
		s_documents.put("bmp", false);
		s_documents.put("gif", false);
		//audio
		s_documents.put("wav", false);
		s_documents.put("mp3", false);
		s_documents.put("ogg", false);
		//video
		s_documents.put("mp4", false);
		s_documents.put("avi", false);
		s_documents.put("mkv", false);
		s_documents.put("mpg", false);
		s_documents.put("mpeg", false);
		
		//
		s_archives.put("gz", true);
		s_archives.put("bz", true);
		s_archives.put("zip", true);
		s_archives.put("rar", true);		
	}

	public static void updateReadingNow(Context context, Intent intent)
	{
		try
		{
			ByteArrayOutputStream aout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(aout);
			dout.writeUTF(intent.getAction());
			dout.writeUTF(intent.getDataString());
			dout.writeUTF(intent.getType());
			dout.writeByte(0);

			byte[] data = aout.toByteArray();
			dout.close();

			ContentValues values = new ContentValues();
			values.put("data", data);
			context.getContentResolver().insert(Uri.parse("content://com.ereader.android/last"), values);
		}
		catch (Exception ex)
		{
			Log.e("FileBrowser", "Exception while updating reading now data: ", ex);
		}
	}

	public static String getMimeType(String extension)
	{
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		if (mimeTypeMap.hasExtension(extension))
			return mimeTypeMap.getMimeTypeFromExtension(extension);

		if (extension.equals("apk"))
			return "application/vnd.android.package-archive";

		/*if (extension.equals("fb2"))
			return "application/x-fictionbook+xml";
		
		if (extension.equals("fb2.zip"))
			return "application/x-fictionbook";*/
		
		if (extension.equals("fb2.zip"))
			return "application/fb2.zip";
		
		if (extension.equals("html") || extension.equals("htm") || extension.equals("xhtml"))
			return "text/html";

		if (extension.equals("txt") || extension.equals("conf") || extension.equals("ini"))
			return "text/plain";
		
		return "application/" + extension;
	}

	public static boolean pathExists(String path)
	{
		File file = new File(path);
		return file.exists();
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

	public static boolean isBook(String extension)
	{
		return s_documents.containsKey(extension) && s_documents.get(extension);
	}

	public static boolean isDocument(String extension)
	{
		return s_documents.containsKey(extension);
	}

	
	public static String formatByteAmount(long totalBytes)
	{
		if (totalBytes > 1000000000)
			return String.format("%.2f GB", (float)totalBytes / 1073741824);

		if (totalBytes > 1000000)
			return String.format("%.2f MB", (float)totalBytes / 1048576);

		if (totalBytes > 1000)
			return String.format("%.2f KB", (float)totalBytes / 1024);

		return String.format("%d B", totalBytes);
	}
}

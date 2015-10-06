package net.runserver.common;

import java.lang.reflect.Method;

import android.util.Log;

public final class NookEInkMode
{
	private static final Method s_useFourBitUpdate;
	private static final Method s_useTwoBitUpdate;	

	static
	{
		Method useFourBitUpdate = null;
		Method useTwoBitUpdate = null;	
		try
		{
			Class<?> updateClass = Class.forName("android.graphics.TwoBitSupport");
			if (updateClass != null)
			{		
				useFourBitUpdate = updateClass.getMethod("useFourBitUpdate");
				useTwoBitUpdate = updateClass.getMethod("useTwoBitUpdate");
			}
		}
		catch (Exception ex)
		{
			Log.d("NookEInkMode", "Failed to init TwoBitSupport");
		}
		
		if (useFourBitUpdate != null && useTwoBitUpdate != null)
		{
			s_useFourBitUpdate = useFourBitUpdate;
			s_useTwoBitUpdate = useTwoBitUpdate;
		} else
		{
			s_useFourBitUpdate = null;
			s_useTwoBitUpdate = null;
		}		
	}
	
	public static void setTwoBitMode()
	{
		if (s_useTwoBitUpdate != null)
		{
			try
			{
				s_useTwoBitUpdate.invoke(null);
			}
			catch (Exception ex)
			{
				Log.d("NookEInkMode", "Failed to set two bit update " + ex);
			}
		}		
	}

	
	public static void setFourBitMode()
	{
		if (s_useFourBitUpdate != null)
		{
			try
			{
				s_useFourBitUpdate.invoke(null);
			}
			catch (Exception ex)
			{
				Log.d("NookEInkMode", "Failed to set four bit update " + ex);
			}			
		}
	}
}

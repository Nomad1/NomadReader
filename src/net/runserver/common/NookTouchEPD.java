package net.runserver.common;

import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;
import android.view.View;

public final class NookTouchEPD
{
	private static final String EPDDelay = "/sys/class/graphics/fb0/epd_delay";

	//private static final Method s_epdRefresh;
	
	private static final Method s_waveValueOfMethod;
	private static final Method s_modeValueOfMethod;
	private static final Method s_regionValueOfMethod;
	
	private static final Method s_setRegionMethod;
	
	//private static final Constructor<?>  s_regionParamConstructor;
	
	private static final boolean s_delayAccessible;
	
	
	static
	{
		//Method refreshMethod = null;
		boolean delayAccessible = false;
		Method waveValueOfMethod = null;
		Method modeValueOfMethod = null;
		Method regionValueOfMethod = null;
		//Constructor<?> regionParamConstructor = null;
		Method setRegionMethod = null;
		try
		{
			RandomAccessFile testFile = new RandomAccessFile(EPDDelay, "rw");
			int value = testFile.read();
			testFile.seek(0);
			testFile.write(value);
			testFile.close();
			delayAccessible = true;
			
			Class<?> controllerClass = Class.forName("android.hardware.EpdController");
			//Class<?> refreshClass = Class.forName("android.hardware.EpdController$Refresh");
			
			Class<?> waveEnum = Class.forName("android.hardware.EpdController$Wave");
			waveValueOfMethod = waveEnum.getMethod("valueOf", String.class);
			
			Class<?> modeEnum = Class.forName("android.hardware.EpdController$Mode");
			modeValueOfMethod = modeEnum.getMethod("valueOf", String.class);		
/*			
			Class<?> regionParams = Class.forName("android.hardware.EpdController$RegionParams");
			
			Constructor<?> [] constructors = regionParams.getConstructors();
			
			for(Constructor<?> constructor: constructors)
			{				
				if (constructor.getParameterTypes().length == 6)
				{
					regionParamConstructor = constructor;
					break;
				}
			}
			
			if (regionParamConstructor == null)
				Log.e("TextReader", "RegionParam constructor not found!");
			*/
			Class<?> regionEnum = Class.forName("android.hardware.EpdController$Region");
			regionValueOfMethod = regionEnum.getMethod("valueOf", String.class);		
			
			//regionParamConstructor = regionParams.getConstructor(new Class[] {Integer.class, Integer.class, Integer.class, Integer.class, waveEnum, Integer.class});
			
			//(Ljava/lang/String;Landroid/hardware/EpdController$Region;Landroid/view/View;Landroid/hardware/EpdController$Wave;Landroid/hardware/EpdController$Mode;)V
			
			setRegionMethod = controllerClass.getMethod("setRegion", String.class, regionEnum, View.class, waveEnum, modeEnum); 
			
			//if (updateClass != null)
			//{
				//refreshMethod = updateClass.getMethod("epdRefresh", String.class, refreshClass);
				// writeMethod = updateClass.getMethod("sysWrite", String.class,
				// String.class);
			//}

			//if (refreshClass != null)
			//{
				//Method valueOfMethod = refreshClass.getMethod("valueOf", String.class);
				//refreshObject = valueOfMethod.invoke(null, "REFRESH");
			//}

		}
		catch (Exception ex)
		{
			Log.d("NookTouchEPD", "Failed to init refresh EPD " + ex);
			ex.printStackTrace();
		}

		//s_epdRefresh = refreshMethod;
		s_waveValueOfMethod = waveValueOfMethod;
		s_modeValueOfMethod = modeValueOfMethod;
		//s_regionParamConstructor = regionParamConstructor; 
		s_delayAccessible = delayAccessible;
		s_setRegionMethod = setRegionMethod;
		s_regionValueOfMethod = regionValueOfMethod;
	}
	
	private static Object getWaveEnum(String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if (s_waveValueOfMethod == null)
			return null;
		
		return s_waveValueOfMethod.invoke(null, value);		
	}
	
	private static Object getModeEnum(String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if (s_modeValueOfMethod == null)
			return null;
		
		return s_modeValueOfMethod.invoke(null, value);		
	}
	
	private static Object getRegionEnum(String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if (s_regionValueOfMethod == null)
			return null;
		
		return s_regionValueOfMethod.invoke(null, value);		
	}

	/*
	public static void refresh()
	{
		if (s_epdRefresh != null)
		{
			try
			{
				s_epdRefresh.invoke(null, "TextReader", s_refreshObject);
			}
			catch (Exception ex)
			{
				Log.d("NookTouchEPD", "Failed to refresh EPD " + ex);
				ex.printStackTrace();
			}
		}
	}
*/
	public static void setNoDelay()
	{
		if (s_delayAccessible)
		{
			try
			{
				FileWriter writer = new FileWriter(EPDDelay);
				writer.write("0");
				writer.close();
			}
			catch (Exception ex)
			{
				Log.d("NookTouchEPD", "Failed to set epd 0 delay " + ex);
				ex.printStackTrace();
			}
		}
	}

	public static void setDelay()
	{
		if (s_delayAccessible)
		{
			try
			{
				FileWriter writer = new FileWriter(EPDDelay);
				writer.write("100");
				writer.close();
			}
			catch (Exception ex)
			{
				Log.d("NookTouchEPD", "Failed to set epd delay " + ex);
				ex.printStackTrace();
			}
		}
	}
	
	public static void setMode(String region, String wave, String mode, View view)
	{
		if (s_setRegionMethod != null)
		{
			try
			{
				s_setRegionMethod.invoke(null, "TextReader", getRegionEnum(region), view, getWaveEnum(wave), getModeEnum(mode));
			}
			catch (Exception e)
			{
				Log.d("NookTouchEPD", "Failed to set EPD region mode" + e);
				e.printStackTrace();
			}
		}		
	}
}

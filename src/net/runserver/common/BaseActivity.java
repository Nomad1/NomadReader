package net.runserver.common;

import net.runserver.textReader.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity
{
	public static final int SCREENLAYOUT_SIZE_SMALL = 1;
	public static final int SCREENLAYOUT_SIZE_XLARGE = 4;
	public static final int SCREENLAYOUT_SIZE_NORMAL = 2;
	public static final int SCREENLAYOUT_SIZE_LARGE = 3;
	
	public static boolean isNook = false;
	public static boolean isNookTouch = false;
	public static boolean isNookColor = false;
	public static boolean isEpad = false;
	public static boolean isEmulator = false;
	public static boolean isArabicSupported = false;
	public static boolean isBiDirStringSupported = false;
	public static boolean isLegacyAndroid = false;
	public static boolean isXLarge = false;
	public static boolean isLarge = false;
	public static boolean isSmall = false;
	public static int SDKVersion = 3;
	public static int ScreenSize = 0;
	public static DisplayMetrics DisplayMetrics;
	
	private static Thread s_uiThread;
	
	private final String m_appName;
	
	private PowerManager.WakeLock m_wakeLock = null;
	private int m_wakeLockType;
	private int m_version;
	
	protected void setWakeLockType(int type)
	{
		m_wakeLockType = type;
	}
	
	public BaseActivity(String appName)
	{
		super();
		m_wakeLockType = PowerManager.SCREEN_DIM_WAKE_LOCK;
		m_appName = appName;
		s_uiThread = Thread.currentThread();
	}
	
	public void terminate()
	{
//		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static abstract class ResultRunnable implements Runnable
	{
		private Object m_result;
		
		public Object getResult()
		{
			return m_result;
		}

		@Override
		public void run()
		{
			m_result = resultRun();
			notify();
		}
		
		public abstract Object resultRun();
	}
	
	public static Object runOnUiThreadWait(Context context, ResultRunnable runnable)
	{
		if (s_uiThread == Thread.currentThread())
		{
			return runnable.resultRun();
		}
		synchronized (runnable)
		{
			((Activity)context).runOnUiThread(runnable);
			try
			{
				runnable.wait();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
		return runnable.getResult();
	}
	
	private static class WaitRunnable implements Runnable
	{
		private final Runnable m_target;
		
		public WaitRunnable(Runnable runnable)
		{
			m_target = runnable;
		}

		@Override
		public void run()
		{
			m_target.run();
			notify();
		}
	}
	
	public static void runOnUiThreadWait(Context context, Runnable runnable)
	{
		if (s_uiThread == Thread.currentThread())
		{
			runnable.run();
			return;
		}
		
		Runnable wrapper = new WaitRunnable(runnable);
		
		synchronized (wrapper)
		{
			((Activity)context).runOnUiThread(wrapper);
			try
			{
				wrapper.wait();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	protected void updateTitle(String title)
	{
		try
		{
			Intent intent = new Intent("com.bravo.intent.UPDATE_TITLE");
			String key = "apptitle";
			intent.putExtra(key, title);
			sendBroadcast(intent);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		DisplayMetrics = dm;
		
		isEpad = Build.BOARD.equals("WMT");
		isEmulator = Build.MODEL.equals("google_sdk");
		isNook = !isEmulator && ((dm.widthPixels == 600 && dm.heightPixels == 944) || (dm.heightPixels == 600 && dm.widthPixels == 944));
		isNookTouch = Build.MODEL.equals("NOOK") || Build.PRODUCT.equals("NOOK");
		isNookColor = Build.MODEL.equals("NookColor") || Build.PRODUCT.equals("NOOKColor") || Build.PRODUCT.equals("NOOKTablet");
		ScreenSize = getResources().getInteger(R.integer.screen_size);
		isXLarge = ScreenSize >= SCREENLAYOUT_SIZE_XLARGE;
		isLarge = ScreenSize >= SCREENLAYOUT_SIZE_LARGE;
		isSmall = ScreenSize == SCREENLAYOUT_SIZE_SMALL;
		
		Log.d("BaseActivity", "Screen size: " + ScreenSize +", dimensions " + dm.widthPixels + ", " + dm.heightPixels + ", density " + dm.density);
		try
		{
			SDKVersion = Integer.parseInt(Build.VERSION.SDK);
			isArabicSupported = SDKVersion >= 8;
			isBiDirStringSupported = SDKVersion >= 11;
			isLegacyAndroid = SDKVersion <= 4;
		}
		catch(Exception ex)
		{
			//ex.printStackTrace();
		}
	}
	
	private void createLocks()
	{
		if (isNook)
		{
			PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);	
	
			{
				m_wakeLock = power.newWakeLock(m_wakeLockType, m_appName + " TouchLock" + this.hashCode());
				m_wakeLock.setReferenceCounted(false);
			}
		}
		
		//Log.d(m_appName, "Locks created");
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		//Log.d(m_appName, "OnPause");
		
		releaseLocks();
		m_wakeLock = null;
	}

	@Override
	protected void onResume()
	{
		super.onResume();		
		//Log.d(m_appName, "OnResume");
		
		if (BaseActivity.isNook)
			updateTitle(m_appName);
		
		createLocks();
		aquireLocks();
	}	
	
	public void onUserInteraction()
	{
		super.onUserInteraction();
		//Log.d(m_appName, "OnUserInteraction");
		
		aquireLocks();
	}
	
	private void releaseLocks()
	{
		m_version++;
		
		if (BaseActivity.isNook)
		{
			if (m_wakeLock != null && m_wakeLock.isHeld())
				m_wakeLock.release();
		}
		else
		{
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		//Log.d(m_appName, "Locks released");
	}
	
	private void aquireLocks()
	{
		m_version++;	

		Window window = getWindow();
		
		if (BaseActivity.isNook)
		{
			if (m_wakeLock != null)
			{
				if (!m_wakeLock.isHeld())
					m_wakeLock.acquire();
			}
		} else
		{
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		window.getDecorView().postDelayed(new ReleaseRunnable(m_version), (int)readTimeOut());			
		
		//Log.d(m_appName, "Locks aquired");
	}
	
	private class ReleaseRunnable implements Runnable
	{
		private final int m_releaseVersion;
		
		public ReleaseRunnable(int version)
		{
			m_releaseVersion = version;
		}

		@Override
		public void run()
		{
			if (m_releaseVersion == m_version)
			{
				releaseLocks();
			} 
		}		
	}


	public String getSettingsString(String targetName)
	{
		return Settings.System.getString(getContentResolver(), targetName);
	}

	public long getSettingsLong(String targetName, long def)
	{
		try
		{
			return Settings.System.getLong(getContentResolver(), targetName);
		}
		catch (SettingNotFoundException e)
		{
			return def;
		}
	}

	protected long readTimeOut()
	{
		long timeOut = 2 * 60000;

		try
		{
			long delay;
			if (BaseActivity.isNook)
			{
				delay = getSettingsLong("bnScreensaverDelay", -1);
				if (delay > 60000)
					timeOut = delay;
			}
			else
			{
				delay = getSettingsLong(Settings.System.SCREEN_OFF_TIMEOUT, -1);
				if (delay > 60000)
					timeOut = delay;
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//Log.d(m_appName, "Screensaver set to " + timeOut);
		return timeOut;
	}

	protected void goBack()
	{
		/*try
		{
			if (getCallingActivity() != null)
			{
				Intent intent = new Intent();
				intent.setComponent(getCallingActivity());
				startActivity(intent);
				return;
			}
		}
		catch (Exception ex)
		{

		}

		// call home activity
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);*/
		moveTaskToBack(true);
	}
}

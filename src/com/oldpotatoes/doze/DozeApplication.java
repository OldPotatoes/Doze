package com.oldpotatoes.doze;

import android.app.Application;
import android.util.Log;

public class DozeApplication extends Application 
{
	public static final String APP_NAME = "Doze"; 
	private static final String TAG = DozeApplication.APP_NAME;
	private static final String LogPrefix = "Application   : ";
	private DozeRecord dozeRecord;
	private boolean activated;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		Log.d(TAG, LogPrefix + "onCreate");
		Log.i(TAG, LogPrefix + "Created application.");
        activated = false;
	}

	@Override
	public void onTerminate() 
	{
		Log.d(TAG, LogPrefix + "onTerminate");
		Log.i(TAG, LogPrefix + "Terminated application.");
		super.onTerminate();     
	}

	public synchronized DozeRecord getDozeRecord()
	{
		Log.d(TAG, LogPrefix + "getDozeRecord");
		if (dozeRecord == null)
	        dozeRecord = new DozeRecord("only", 0, 0, 0, 0, false);

		return dozeRecord;
	}

	public void setDozeRecord(DozeRecord dr)
	{
		Log.d(TAG, LogPrefix + "setDozeRecord");
		dozeRecord = dr;
	}
	
	public boolean getActivated()
	{
		Log.d(TAG, LogPrefix + "getActivated");
		return activated;
	}

	public void setActivated(boolean a)
	{
		Log.d(TAG, LogPrefix + "setActivated");
		activated = a;
	}
}

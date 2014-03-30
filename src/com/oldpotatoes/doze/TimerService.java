package com.oldpotatoes.doze;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class TimerService extends Service 
{
	public enum DozeState
	{
		DOZE,
	    WAKE, 
	    UNKNOWN
	}

	private static final String TAG = "Doze";
	private static final String LogPrefix = "Service       : ";
	private Runnable taskUpdateTime;
	private Handler timerHandler;

	private DozeState dozingOrWaking;
	private DozeRecord currentDetails;
    private long millisNextActionTime;
    
	@Override
	public void onCreate() 
	{
		super.onCreate();

		//Debug.startMethodTracing("DozeService2");

		logDebug("onCreate");

		try
		{
			timerHandler = new Handler();
	
			taskUpdateTime = new Runnable()
			{
				public void run() 
				{
					logDebug("taskUpdateTime.run");
					setNewDelay();
				}
			};
			
			dozingOrWaking = DozeState.UNKNOWN;
			millisNextActionTime = 0;
			currentDetails  = new DozeRecord();
		}
		catch (Exception ex)
		{
        	logError("OnCreate threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	@Override
	public synchronized void onStart(Intent intent, int startId) 
	{
		try
		{
			super.onStart(intent, startId);
			logDebug("onStart");
		}
		catch (Exception ex)
		{
        	logError("onStart threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		int retVal = 0;
		
		try
		{
			logDebug("onStartCommand");
			
			calculateTimings();

			if (currentDetails.activated == true)
				activateService();
			else
				deactivateService();
			
			retVal = super.onStartCommand(intent, flags, startId);
		}
		catch (Exception ex)
		{
        	logError("onStartCommand threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				

		return retVal;
	}

	@Override
	public synchronized void onDestroy() 
	{
		try
		{
			super.onDestroy();
			logDebug("onDestroy");
			 
			timerHandler.removeCallbacks(taskUpdateTime);
			timerHandler = null;
			taskUpdateTime = null;

			//Debug.stopMethodTracing();
		}
		catch (Exception ex)
		{
        	logError("onDestroy threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return new ITimerService.Stub() 
		{
			public void setTimeDetails(String dozeName, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti) 
			{
				try
				{
					logDebug("onBind.setTimeDetails");
//					logInfo("Doze: " + dozeHour + "." + dozeMinute + ", Wake: " + wakeHour + "." + wakeMinute + ", Activated: " + acti);

					currentDetails.name = dozeName;
					currentDetails.dozeHour = dozeHour;
					currentDetails.dozeMinute = dozeMinute;
					currentDetails.wakeHour = wakeHour;
					currentDetails.wakeMinute = wakeMinute;
					currentDetails.activated = acti;

					calculateTimings();
				}
				catch (Exception ex)
				{
		        	logError("onBind.setTimeDetails threw exception: " + ex.getMessage());
					ex.printStackTrace();
				}				
			}

			public void setActivated() 
			{
				try
				{
					logDebug("onBind.setActivated");
					
					activateService();
				}
				catch (Exception ex)
				{
		        	logError("onBind.setActivated threw exception: " + ex.getMessage());
					ex.printStackTrace();
				}				
			}

			public void setDeactivated() 
			{
				try
				{
					logDebug("onBind.setDeactivated");
					deactivateService();
				}
				catch (Exception ex)
				{
		        	logError("onBind.setDeactivated threw exception: " + ex.getMessage());
					ex.printStackTrace();
				}				
			}
		};
    }
	 
	private void activateService()
	{
		try
		{
			logDebug("activateService");
			currentDetails.activated = true;

			timerHandler.removeCallbacks(taskUpdateTime);
			timerHandler.postDelayed(taskUpdateTime, 100);
		}
		catch (Exception ex)
		{
        	logError("activateService threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}

	private void deactivateService()
	{
		try
		{
			logDebug("deactivateService");
			currentDetails.activated = false;

			timerHandler.removeCallbacks(taskUpdateTime);
		}
		catch (Exception ex)
		{
        	logError("deactivateService threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}

	private void calculateTimings()
	{
		try
		{
	        logDebug("calculateTimings");
	
			// get the current time
			final Calendar c = Calendar.getInstance();
			int nowYear = c.get(Calendar.YEAR);
			int nowMonth = c.get(Calendar.MONTH);
			int nowDay = c.get(Calendar.DAY_OF_MONTH);

	        logInfo("Doze: " + currentDetails.dozeHour + ":" + currentDetails.dozeMinute + ", Wake: " + currentDetails.wakeHour + ":" + currentDetails.wakeMinute);
			
			GregorianCalendar dozeGreg = new GregorianCalendar(nowYear, nowMonth, nowDay, (int)currentDetails.dozeHour, (int)currentDetails.dozeMinute);
			GregorianCalendar wakeGreg = new GregorianCalendar(nowYear, nowMonth, nowDay, (int)currentDetails.wakeHour, (int)currentDetails.wakeMinute);
			
			// Times before now are moved to tomorrow.
			if (dozeGreg.before(c))
				dozeGreg.add(GregorianCalendar.DATE, 1);
			
			if (wakeGreg.before(c))
				wakeGreg.add(GregorianCalendar.DATE, 1);

			long dozeTimeInMillis = dozeGreg.getTimeInMillis();
			long wakeTimeInMillis = wakeGreg.getTimeInMillis();
	
			dozingOrWaking = DozeState.DOZE;
			long nextTimeICareAbout = dozeTimeInMillis;
			if (wakeTimeInMillis < dozeTimeInMillis)
			{
				dozingOrWaking = DozeState.WAKE;
				nextTimeICareAbout = wakeTimeInMillis;
			}
	
			millisNextActionTime = nextTimeICareAbout;
		}
		catch (Exception ex)
		{
        	logError("calculateTimings threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}
	

	private void setNewDelay()
	{
		try
		{
	        logDebug("setNewDelay");
	        logInfo("millisNextActionTime: " + millisNextActionTime);
			
			String message = "";
			if (dozingOrWaking == DozeState.DOZE)
				message = getResources().getString(R.string.DozeMessage);
			else if (dozingOrWaking == DozeState.WAKE)
				message = getResources().getString(R.string.WakeMessage);
	
			timerHandler.removeCallbacks(taskUpdateTime);
	
			String timeMessage = getTextTimeFromMillis();
			String completeMessage = message + " " + getResources().getString(R.string.messageIn) +  " " + timeMessage;
	        logInfo(completeMessage);
	
			long millisUntilCallback = millisNextActionTime - System.currentTimeMillis();
	        logInfo("Delay for " + millisUntilCallback + " millis.");
	
	        if (millisUntilCallback > 1000)
	        	timerHandler.postDelayed(taskUpdateTime, millisUntilCallback);
	        else
	        {
				completeMessage = message + " " +  getResources().getString(R.string.messageNow);
				
				if (dozingOrWaking == DozeState.DOZE)
				{
					dozingOrWaking = DozeState.WAKE;
					setAirplaneMode(true);
				}
				else if (dozingOrWaking == DozeState.WAKE)
				{
					dozingOrWaking = DozeState.DOZE;
					setAirplaneMode(false);
				}
				
				// tidy logging table
				//databaseHelper.pruneLogging();
	
				calculateTimings();
				timerHandler.postDelayed(taskUpdateTime, 5000); // 5 seconds
	        }
	
			Toast.makeText(getApplicationContext(), completeMessage, Toast.LENGTH_SHORT).show();
	        logInfo(completeMessage);
		}
		catch (Exception ex)
		{
        	logError("setNewDelay threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}
	
	private String getTextTimeFromMillis()
	{
		String timeText = "";

		try
		{
	        logDebug("getTextTimeFromMillis");
	
			long hours = 0;
			long minutes = 0;
			long seconds = 0;
			long remaining = millisNextActionTime - System.currentTimeMillis();
	        logInfo("millis remaining: " + remaining);
			
			if (remaining > 3600000)
			{
				hours = remaining / 3600000;
				remaining -= (3600000 * hours);
			}
	
			if (remaining > 60000)
			{
				minutes = remaining / 60000;
				remaining -= (60000 * minutes);
			}
	
			if (remaining > 1000)
			{
				seconds = remaining / 1000;
				remaining -= (1000 * seconds);
			}
			
			if (remaining > 500)
			{
				seconds++;
				if (seconds == 60)
				{
					seconds = 0;
					minutes++;
					if (minutes == 60)
					{
						minutes = 0;
						hours++;
					}
				}
			}
			
			logInfo("hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds);
	        timeText = buildTimeText(hours, minutes, seconds);
		}
		catch (Exception ex)
		{
        	logError("getTextTimeFromMillis threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				

		return timeText;
	}

	private String buildTimeText(long hours, long minutes, long seconds)
	{
		String secondsText = "";
		String minutesText = "";
		String hoursText = "";

		try
		{
			logDebug("buildTimeText");
	
			boolean haveSeconds = false;
			boolean haveMinutes = false;
			boolean haveHours = false;
	
			if (seconds > 0)
			{
				haveSeconds = true;
				if (seconds == 1)
					secondsText = String.format("%d %s", seconds, getResources().getString(R.string.messageSecond));
				else
					secondsText = String.format("%d %s", seconds, getResources().getString(R.string.messageSeconds));
			}
	
			if (minutes > 0)
			{
				haveMinutes = true;
				if (minutes == 1)
					minutesText = String.format("%d %s", minutes, getResources().getString(R.string.messageMinute));
				else
					minutesText = String.format("%d %s", minutes, getResources().getString(R.string.messageMinutes));
			}
				
			if (hours > 0)
			{
				haveHours = true;
				if (hours == 1)
					hoursText = String.format("%d %s", hours, getResources().getString(R.string.messageHour));
				else
					hoursText = String.format("%d %s", hours, getResources().getString(R.string.messageHours));
			}
			
			if (haveHours && !haveMinutes && !haveSeconds)
				hoursText += ".";
			else if (!haveHours && haveMinutes && !haveSeconds)
				minutesText += ".";
			else if (!haveHours && !haveMinutes && haveSeconds)
				secondsText += ".";
			else if (haveHours && haveMinutes && !haveSeconds)
			{
				hoursText = hoursText + " " + getResources().getString(R.string.messageAnd) + " ";
				minutesText += ".";
			}
			else if (haveHours && !haveMinutes && haveSeconds)
			{
				hoursText = hoursText + " " + getResources().getString(R.string.messageAnd) + " ";
				secondsText += ".";
			}
			else if (!haveHours && haveMinutes && haveSeconds)
			{
				minutesText = minutesText + " " + getResources().getString(R.string.messageAnd) + " ";
				secondsText += ".";
			}
			else if (haveHours && haveMinutes && haveSeconds)
			{
				hoursText += ", ";
				minutesText = minutesText + " " + getResources().getString(R.string.messageAnd) + " ";
				secondsText += ".";
			}
		}
		catch (Exception ex)
		{
        	logError("buildTimeText threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				

		return hoursText + minutesText + secondsText;
	}
	
	private void setAirplaneMode(boolean enable)
	{
		try
		{
			if (enable)
				logInfo("Airplane mode turned on.");
			else
				logInfo("Airplane mode turned off.");
	
			ContentResolver cr = getContentResolver();
			Settings.System.putInt(cr, Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0);
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", enable);
			this.sendBroadcast(intent);
		}
		catch (Exception ex)
		{
        	logError("setAirplaneMode threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}

	private void logDebug(String message)
	{
		Log.d(TAG, LogPrefix + message);
		logDatabaseMessage("Debug", LogPrefix + message);
	}

	private void logInfo(String message)
	{
		Log.i(TAG, LogPrefix + message);
		logDatabaseMessage("Info", LogPrefix + message);
	}
	
	private void logError(String message)
	{
		Log.e(TAG, LogPrefix + message);
		logDatabaseMessage("Error", LogPrefix + message);
	}
   
	private void logDatabaseMessage(String status, String message)
	{
		String[] args = {status, message};
		LogToDatabase logTask = new LogToDatabase();
		logTask.execute(args);		
	}
	   
	private void pruneDatabaseLogging(String status, String message)
	{
		PruneLoggingTable logTask = new PruneLoggingTable();
		logTask.execute();		
	}

	private class LogToDatabase extends AsyncTask<String, Integer, Void>
	{
		protected Void doInBackground(final String... args) 
		{
			try
			{
	    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); 
	    		String dateText = dateFormat.format(new Date());

				ContentValues loggingValues = new ContentValues();
				loggingValues.put(DozeProvider.LOGGING_DATETIME, dateText);
				loggingValues.put(DozeProvider.LOGGING_STATUS, args[0]);
				loggingValues.put(DozeProvider.LOGGING_MESSAGE, args[1]);
				getContentResolver().insert(DozeProvider.LOGGING_CONTENT_URI, loggingValues);
			}
			catch (Exception ex)
			{
	        	logError("LogToDatabase.doInBackground threw exception: " + ex.getMessage());
				ex.printStackTrace();
			}				

			return null;
		}
	}
	
	private class PruneLoggingTable extends AsyncTask<Void, Integer, Void>
	{
		protected Void doInBackground(Void... args) 
		{
			try
			{
				getContentResolver().delete(DozeProvider.LOGGING_CONTENT_URI, null, null);
			}
			catch (Exception ex)
			{
	        	logError("PruneLoggingTable.doInBackground threw exception: " + ex.getMessage());
				ex.printStackTrace();
			}				

			return null;
		}
	}
}

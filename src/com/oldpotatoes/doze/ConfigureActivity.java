package com.oldpotatoes.doze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
//import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

public class ConfigureActivity extends Activity implements OnClickListener
{
	private static final String TAG = "Doze";
	private static final int DOZE_TIME_DIALOG_ID = 666;
	private static final int WAKE_TIME_DIALOG_ID = 667;
	private static final String LogPrefix = "Activity      : ";
    private DozeApplication app;
    private TextView textDozeDisplay;
    private TextView textWakeDisplay;
    private Button buttonDozeSelect;
    private Button buttonWakeSelect;
    private CheckBox checkActivate;
	private ITimerService service;
	private TimerServiceConnection connection;
	private Intent serviceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        try 
        {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.configure);

	        app = (DozeApplication)getApplication();

			textDozeDisplay = (TextView) findViewById(R.id.dozeDisplay);
			textWakeDisplay = (TextView) findViewById(R.id.wakeDisplay);
			buttonDozeSelect = (Button) findViewById(R.id.dozeSelect);
			buttonWakeSelect = (Button) findViewById(R.id.wakeSelect);
			checkActivate = (CheckBox) findViewById(R.id.onCheck);
			
			buttonDozeSelect.setOnClickListener(this);
			buttonWakeSelect.setOnClickListener(this); 
			checkActivate.setOnClickListener(this);
			
			logDebug("onCreate");
			logInfo("Created activity.");
        } 
        catch (Exception ex) 
        {
        	logError("onCreate threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
    }

	@Override
	protected void onStart() 
	{
		super.onStart();

		try 
		{
			logDebug("onStart");
			startService();

			readConfigurationFromDatabase();
		} 
		catch (Exception ex) 
		{
        	logError("onStart threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		try 
		{
			logDebug("onStop");
			releaseService();
		} 
		catch (Exception ex) 
		{
        	logError("onStop threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void onClick(View v)
	{
    	try 
    	{
			logDebug("onClick");
			if (v.getId() == R.id.dozeSelect)
				showDialog(DOZE_TIME_DIALOG_ID);
			else if (v.getId() == R.id.wakeSelect)
				showDialog(WAKE_TIME_DIALOG_ID);
			else if (v.getId() == R.id.onCheck)
				activatedCheckedUnchecked();
		} 
    	catch (Exception ex) 
    	{
        	logError("onClick threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

    @Override
	protected Dialog onCreateDialog(int id) 
	{
		try
		{
			logDebug("onCreateDialog");
	    	DozeRecord dr = app.getDozeRecord();
	
			switch (id) 
			{
				case DOZE_TIME_DIALOG_ID:
					return new TimePickerDialog(this, dozeSetListener, (int)dr.dozeHour, (int)dr.dozeMinute, false);
				case WAKE_TIME_DIALOG_ID:
					return new TimePickerDialog(this, wakeSetListener, (int)dr.wakeHour, (int)dr.wakeMinute, false);
			}
		}
		catch (Exception ex)
		{
        	logError("onCreateDialog threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				

		return null;
	}

    private TimePickerDialog.OnTimeSetListener dozeSetListener =
	new TimePickerDialog.OnTimeSetListener() 
	{
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		{
			logDebug("doze.onTimeSet");
			logInfo("Set new doze time, " + hourOfDay + ":" + minute + ".");
			saveNewTime(view, false, hourOfDay, minute);
		}
	};

    private TimePickerDialog.OnTimeSetListener wakeSetListener =
	new TimePickerDialog.OnTimeSetListener() {
    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
    	{
			logDebug("wake.onTimeSet");
			logInfo("Set new wake time, " + hourOfDay + ":" + minute + ".");
			saveNewTime(view, true, hourOfDay, minute);
    	}
    };

    public void saveNewTime(TimePicker view, boolean isWake, int hourOfDay, int minute) 
    {
    	try
    	{
			logDebug("saveNewTime");
	
	    	DozeRecord dr = app.getDozeRecord();
	    	
	    	if (isWake)
	    	{
//	        	logInfo("Set new wake time " + hourOfDay + ":" + minute);
		    	dr.wakeHour = hourOfDay;
				dr.wakeMinute = minute;
	    	}
	    	else
	    	{
//	        	logInfo("Set new doze time." + hourOfDay + ":" + minute);
		    	dr.dozeHour = hourOfDay;
				dr.dozeMinute = minute;
	    	}
	
	    	app.setDozeRecord(dr);
	    	
	    	writeConfigurationToDatabase();
			updateDisplay();
			updateService();
		}
		catch (Exception ex)
		{
        	logError("saveNewTime threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}
    
    public void activatedCheckedUnchecked()
    {
		try 
		{
			logDebug("saveActivated");

			DozeRecord dr = app.getDozeRecord();
			dr.activated = checkActivate.isChecked();
			logInfo("Set " + (dr.activated ? "activated" : "deactivated"));
			app.setDozeRecord(dr);
			
			writeConfigurationToDatabase();

			updateService();
			
			if (checkActivate.isChecked() == false)
			{
    			WriteLogToCard writeToCard = new WriteLogToCard();
    			writeToCard.execute();
			}
		}
		catch (Exception ex) 
		{
        	logError("saveActivated threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}    	
    }
    
    private void updateService()
    {
    	try 
    	{
			logDebug("updateService");
			DozeRecord dr = app.getDozeRecord();

			if (service == null)
				throw new Exception("Service is null");

			service.setTimeDetails(dr.name, dr.dozeHour, dr.dozeMinute, dr.wakeHour, dr.wakeMinute, dr.activated);

			if (dr.activated)
				service.setActivated();
			else
        		service.setDeactivated();
		} 
    	catch (Exception ex) 
		{
        	logError("updateService threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
   }
	
	private void updateDisplay()
	{
		try
		{
			logDebug("updateDisplay");	
	   		DozeRecord dr = app.getDozeRecord();
	
	        String dozeTime = makeTimeFromHoursMinutes(dr.dozeHour, dr.dozeMinute);
	        String wakeTime = makeTimeFromHoursMinutes(dr.wakeHour, dr.wakeMinute);
	
//	        Log.i(TAG, LogPrefix + "updateDisplay            :Doze time: " + dozeTime + ", wake time: " + wakeTime + ", activated to " + dr.activated);
	        
	        textDozeDisplay.setText(dozeTime);
	    	textWakeDisplay.setText(wakeTime);
	        checkActivate.setChecked(dr.activated);
		}
		catch (Exception ex)
		{
        	logError("updateDisplay threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
    }
    
    private static String makeTimeFromHoursMinutes(long hours, long minutes)
    {
        Log.d(TAG, LogPrefix + "makeTimeFromHoursMinutes");

        StringBuilder dozeTime = new StringBuilder();
		dozeTime.append(pad(hours));
		dozeTime.append(":");
		dozeTime.append(pad(minutes));
		String time = dozeTime.toString();
        
        return time;
    }
    
    private static String pad(long c) 
    {
    	if (c >= 10)
    		return String.valueOf(c);
    	else
    		return "0" + String.valueOf(c);
    }
	
	class TimerServiceConnection implements ServiceConnection
	{
		public void onServiceConnected(ComponentName name, IBinder boundService) 
		{
			logDebug("onServiceConnected");
			service = ITimerService.Stub.asInterface((IBinder) boundService);
		}

		public void onServiceDisconnected(ComponentName name) 
		{
			logDebug("onServiceDisconnected");
			service = null;
		}
	}
	  
	private void startService()
	{
		try
		{
			logDebug("startDozeService");
			
			if (service == null)
			{
				logInfo("Starting service.");

				serviceIntent = new Intent();
			    serviceIntent.setClassName("com.oldpotatoes.doze", com.oldpotatoes.doze.TimerService.class.getName());
			
			    startService(serviceIntent);
			    connection = new TimerServiceConnection();
			    boolean ret = bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
			    if (ret == false)
			    	throw new Exception("Unable to bind service.");
			}
		}
		catch (Exception ex)
		{
        	logError("startDozeService threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}							
	}

    private void releaseService() 
    {
    	try
    	{
			logDebug("releaseService");

	   		DozeRecord dr = app.getDozeRecord();
	   		if (!dr.activated)
	   		{
				logInfo("Releasing service.");
				
		        if (connection != null)
		        {
		        	unbindService(connection);
		        	connection = null;
		        }
		        
		        this.stopService(serviceIntent);
	   		}
		}
		catch (Exception ex)
		{
        	logError("releaseService threw exception: " + ex.getMessage());
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

	
	private void readConfigurationFromDatabase()
	{
		try
		{
			logDebug("readConfigurationFromDatabase");
	
			SelectDozeConfigTask selectTask = new SelectDozeConfigTask();
			selectTask.execute(app.getDozeRecord().name);
		}
		catch (Exception ex)
		{
        	logError("readConfigurationFromDatabase threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}

	
	private void writeConfigurationToDatabase()
	{
		try
		{
			logDebug("writeConfigurationToDatabase");
			logInfo("Write configuration to database.");
	
	        WriteDozeConfigTask addTask = new WriteDozeConfigTask();
			addTask.execute(app.getDozeRecord());
		}
		catch (Exception ex)
		{
        	logError("writeConfigurationToDatabase threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}				
	}
	
	private class WriteDozeConfigTask extends AsyncTask<DozeRecord, Integer, Void>
	{
		protected Void doInBackground(final DozeRecord... args) 
		{
			try
			{
				Log.d(TAG, LogPrefix + "WriteDozeConfigTask.doInBackground");
	
				DozeRecord dozeRec = args[0];
	
		        Log.i(TAG, LogPrefix + "doInBackground      :Setting into database the doze time: " + dozeRec.dozeHour + ":" + dozeRec.dozeMinute);
		        Log.i(TAG, LogPrefix + "doInBackground      :Setting into database the wake time: " + dozeRec.wakeHour + ":" + dozeRec.wakeMinute);
		        Log.i(TAG, LogPrefix + "doInBackground      :Setting into database the activation status: " + dozeRec.activated);
				
				ContentValues configValues = new ContentValues();
				configValues.put(DozeProvider.DETAILS_NAME, dozeRec.name);
				configValues.put(DozeProvider.DETAILS_DOZE_HOUR, dozeRec.dozeHour);
				configValues.put(DozeProvider.DETAILS_DOZE_MINUTE, dozeRec.dozeMinute);
				configValues.put(DozeProvider.DETAILS_WAKE_HOUR, dozeRec.wakeHour);
				configValues.put(DozeProvider.DETAILS_WAKE_MINUTE, dozeRec.wakeMinute);
				configValues.put(DozeProvider.DETAILS_ACTIVATED, dozeRec.activated);

				int rowsUpdated = getContentResolver().update(DozeProvider.DOZES_CONTENT_URI, configValues, null, null);
				if (rowsUpdated == 0)
					getContentResolver().insert(DozeProvider.DOZES_CONTENT_URI, configValues);
			}
			catch (Exception ex)
			{
	        	logError("AddDataTask.doInBackground threw exception: " + ex.getMessage());
				ex.printStackTrace();
			}				
			
			return null;
		}			 
	}
	
	
	private class SelectDozeConfigTask extends AsyncTask<String, Integer, List<DozeRecord>> 
	{
		protected List<DozeRecord> doInBackground(final String... dozeNames) 
		{
			List<DozeRecord> records = new ArrayList<DozeRecord>();
	
			try
			{
				Log.d(TAG, LogPrefix + "SelectDataTask.doInBackground");

				Cursor c = managedQuery(DozeProvider.DOZES_CONTENT_URI, null, null, null, DozeProvider.DETAILS_NAME);
				if (c.moveToFirst())
				{
					do
					{
						DozeRecord doze = new DozeRecord();
						doze.name = c.getString(c.getColumnIndex(DozeProvider.DETAILS_NAME));
						doze.dozeHour = c.getLong(c.getColumnIndex(DozeProvider.DETAILS_DOZE_HOUR));
						doze.dozeMinute = c.getLong(c.getColumnIndex(DozeProvider.DETAILS_DOZE_MINUTE));
						doze.wakeHour = c.getLong(c.getColumnIndex(DozeProvider.DETAILS_WAKE_HOUR));
						doze.wakeMinute = c.getLong(c.getColumnIndex(DozeProvider.DETAILS_WAKE_MINUTE));
						int acti = c.getInt(c.getColumnIndex(DozeProvider.DETAILS_ACTIVATED));
						doze.activated = (acti == 1 ? true : false);

						records.add(doze);

					} while (c.moveToNext());				
				}				
			}
			catch (Exception ex)
			{
	        	logError("SelectDataTask.doInBackground threw exception: " + ex.getMessage());
				ex.printStackTrace();
			}				
	
			return records;
		}
		
		protected void onPostExecute(final List<DozeRecord> dbList) 
		{
			try
			{
				Log.d(TAG, LogPrefix + "SelectDataTask.onPostExecute");
				Log.i(TAG, LogPrefix + "Read configuration from database.");
	
				DozeRecord rec = new DozeRecord();
				if (dbList != null && dbList.size() > 0)
					rec = dbList.get(0);
				else
				{
					final Calendar c = Calendar.getInstance();
					int nowHour = c.get(Calendar.HOUR_OF_DAY);
					int nowMinute = c.get(Calendar.MINUTE);
					
					rec.name = "only";
					rec.dozeHour = nowHour;
					rec.dozeMinute = nowMinute;
					rec.wakeHour = nowHour;
					rec.wakeMinute = nowMinute;
					rec.activated = false;					
				}

				app.setDozeRecord(rec);
	
//				Log.i(TAG, LogPrefix + "SelectDataTask           :Doze: " + rec.dozeHour + "." + rec.dozeMinute + ", Wake: " + rec.wakeHour + "." + rec.wakeMinute + ", Activated: " + rec.activated);
	
				updateDisplay();
				updateService();
			}
			catch (Exception ex)
			{
	        	logError("SelectDataTask.onPostExecute threw exception: " + ex.getMessage());
				ex.printStackTrace();
			}				
		}
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
	
	private class WriteLogToCard extends AsyncTask<Void, Integer, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				Log.d(TAG, LogPrefix + "WriteLogToCard.doInBackground");

				Cursor c = managedQuery(DozeProvider.LOGGING_CONTENT_URI, null, null, null, DozeProvider.LOGGING_DATETIME);
				List<String> logs = new ArrayList<String>();
				if (c.moveToFirst())
				{
					do
					{
						logs.add(c.getString(c.getColumnIndex(DozeProvider.LOGGING_ROWID)) + ", " + 
								c.getString(c.getColumnIndex(DozeProvider.LOGGING_DATETIME)) + ", " + 
								c.getString(c.getColumnIndex(DozeProvider.LOGGING_STATUS)) + ", " + 
								c.getString(c.getColumnIndex(DozeProvider.LOGGING_MESSAGE)));
					} while (c.moveToNext());				
				}				
				
	            File sdCard = Environment.getExternalStorageDirectory();
	            File dir = new File (sdCard.getAbsolutePath() + "/oldpotatoes/doze");
	            dir.mkdirs();

	            File file = new File(dir, "logging.txt");
	            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

	            for (String line : logs)
	            {
		            writer.write(line);
		            writer.newLine();
	            }

	            writer.close();
			}
			catch (Exception ex)
			{
	        	logError("WriteLogToCard.doInBackground threw exception: " + ex.getMessage());
				ex.printStackTrace();
			}				

			return null;
		}
	}
}
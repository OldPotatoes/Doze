package com.oldpotatoes.doze;

import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper
{
    private static final String TAG = "Doze";
	private static final String LogPrefix = "DatabaseHelper: ";

    private SQLiteDatabase db;
    
    private TableDetails detailsTable;
    private TableLogging loggingTable;

    public DatabaseHelper(Context ctx)
    {
        try 
        {
			OpenDetailsHelper detailsHelper = new OpenDetailsHelper(ctx);
			db = detailsHelper.getWritableDatabase();
			
			detailsTable = new TableDetails(ctx, db);
			loggingTable = new TableLogging(ctx, db);
		} 
        catch (Exception ex) 
        {
        	Log.e(TAG, LogPrefix + "DatabaseHelper constructor threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
    }
    
    public SQLiteDatabase getDatabase()
    {
    	return db;
    }
    
    // Details Table

    public List<DozeRecord> selectAllDetails(String nameFilter)
    {
    	List<DozeRecord> details = null;

    	try 
		{
	        details = detailsTable.selectAll(nameFilter);
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "selectAllDetails threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
    	
    	return details;
    }

	public void addDetails(String name, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti)
	{
		try 
		{
			detailsTable.insertOrUpdate(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "addDetails threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public long insertDetails(String name, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti)
	{
		long rowID = 0;

		try 
		{
			rowID = detailsTable.insert(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "insertDetails threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return rowID;
	}

	public int updateDetails(String name, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti)
	{
		int rows = 0;
		
		try 
		{
			rows = detailsTable.update(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "updateDetails threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return rows;
	}
	
    // Logging Table

    public List<String> selectAllLogging()
    {
    	List<String> log = null;

		try 
		{
			log = loggingTable.selectAll();
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "addLogging threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
    	
    	return log;
    }
        
	public long addLogging(String dateText, String status, String message)
	{
		long rowID = 0;
		
		try 
		{
			rowID = loggingTable.insert(dateText, status, message);
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "addLogging threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return rowID;
	}
    
	public int pruneLogging()
	{
		int rowsPruned = 0;

		try 
		{
			rowsPruned = loggingTable.pruneTable();		
		} 
		catch (Exception ex) 
		{
        	Log.e(TAG, LogPrefix + "pruneLogging threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return rowsPruned;
	}
    
    private static class OpenDetailsHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "Doze";
        private static final int DATABASE_VERSION = 1;

        OpenDetailsHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            try 
            {
				db.execSQL(TableDetails.getCreateStatement());
				db.execSQL(TableLogging.getCreateStatement());
			} 
            catch (SQLException ex) 
            {
	        	Log.e(TAG, LogPrefix + "ERROR: Failed to get create database: " + ex.getMessage());
	        	ex.printStackTrace();
			}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, LogPrefix + "onUpgrade           :Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TableDetails.getTableName());
            db.execSQL("DROP TABLE IF EXISTS " + TableLogging.getTableName());
            onCreate(db);
        }
    }
}

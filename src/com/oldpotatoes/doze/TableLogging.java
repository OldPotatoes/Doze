package com.oldpotatoes.doze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class TableLogging 
{
    private static final String TAG = "Doze";
	private static final String LogPrefix = "TableLogging  : ";

	public static final String TABLE = "DozeLogging";
	public static final String COL_ROWID = "_id";
	public static final String COL_DATETIME = "DateTime";
	public static final String COL_STATUS = "Status";
	public static final String COL_MESSAGE = "Message";

    private static final String STATEMENT_CREATE = "create table " + TABLE + " (" +
    		COL_ROWID + " integer primary key autoincrement, " +
    		COL_DATETIME + " text not null, " +
    		COL_STATUS + " text not null, " +
    		COL_MESSAGE + " text not null);";

    private static final String STATEMENT_INSERT = "insert into " + TABLE + 
			"(" + COL_DATETIME + ", " + COL_STATUS + ", " + COL_MESSAGE + ") " +
			"values (?, ?, ?)";
    
//    private static final String STATEMENT_PRUNE = "delete from " + TABLE + " WHERE " + COL_DATETIME + " < ";

//    private static final String STATEMENT_SELECT = "select " + COL_ROWID + ", " + COL_DATETIME + ", " + COL_STATUS + ", " + COL_MESSAGE + ", " + " FROM " + TABLE;

    private final SQLiteStatement insertStatement;
//    private final SQLiteStatement pruneStatement;
//    private final SQLiteStatement selectStatement;

    private final SQLiteDatabase db;
    
    public TableLogging(Context ctx, SQLiteDatabase sqldb)
    {
    	db = sqldb;
    	
    	insertStatement = db.compileStatement(STATEMENT_INSERT);
//    	pruneStatement = db.compileStatement(STATEMENT_PRUNE);
//    	selectStatement = db.compileStatement(STATEMENT_SELECT);
    }
    
    public static String getCreateStatement()
    {
    	return STATEMENT_CREATE;
    }
    
    public static String getTableName()
    {
    	return TABLE;
    }

    public List<String> selectAll()
    {
    	Log.d(TAG, LogPrefix + "selectAll");
    	List<String> list = new ArrayList<String>();
    	db.beginTransaction();

    	try
    	{
	    	Cursor cursor = db.query(TABLE, new String[] {COL_ROWID, COL_DATETIME, COL_STATUS, COL_MESSAGE}, null, null, null, null, COL_DATETIME);
	    	if (cursor.moveToFirst())
	    	{
	    		do
	    		{
	    			int rowID = cursor.getInt(0);
	    			String datetime = cursor.getString(1);
	    			String status = cursor.getString(2);
	    			String message = cursor.getString(3);
	    	    	
	    	    	String record = rowID + ": " + datetime + ", " + status + ": " + message;
	    			
	    			list.add(record);
	    		} while (cursor.moveToNext());
	    	}
	    	
	    	if (cursor != null && !cursor.isClosed())
	    		cursor.close();

	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "ERROR: Failed to select from logging table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}
    	
    	return list;
    }

    public long insert(String dateText, String status, String message)
    {
    	db.beginTransaction();

    	long rowID = 0;
    	try
    	{
	    	insertStatement.bindString(1, dateText);
	    	insertStatement.bindString(2, status);
	    	insertStatement.bindString(3, message);
	    	rowID = insertStatement.executeInsert();
	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "ERROR: Failed to insert '" + message + "' into logging table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}

    	return rowID;
    }

    public int pruneTable()
    {
    	int rowsDeleted = 0;
    	db.beginTransaction();
    	
    	try
    	{
			final Calendar c = Calendar.getInstance();
			final int nowYear = c.get(Calendar.YEAR);
			final int nowMonth = c.get(Calendar.MONTH);
			final int nowDay = c.get(Calendar.DAY_OF_MONTH);
			GregorianCalendar yesterday = new GregorianCalendar(nowYear, nowMonth, nowDay, 0, 0);
			if (yesterday.before(c))
				yesterday.add(GregorianCalendar.DATE, -1);

			Date yesterDate = yesterday.getTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); 

			String whereClause = " WHERE " + COL_DATETIME + " < ?";
			String[] whereArgs = {dateFormat.format(yesterDate)};
			rowsDeleted = db.delete(TABLE, whereClause, whereArgs);
			//String pruneStatement = STATEMENT_PRUNE + "'" + dateFormat.format(yesterDate) + "'";
    		//db.execSQL(pruneStatement);

			db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "ERROR: Failed to get prune statement: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}
    	
    	return rowsDeleted;
    }
}

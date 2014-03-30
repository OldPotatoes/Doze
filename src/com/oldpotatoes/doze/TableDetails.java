package com.oldpotatoes.doze;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class TableDetails 
{
    private static final String TAG = "Doze";
	private static final String LogPrefix = "TableDetails: ";

	public static final String TABLE = "DozeDetails";
	public static final String COL_ROWID = "_id";
	public static final String COL_NAME = "dozeName";
	public static final String COL_DOZE_HOUR = "dozeHour";
	public static final String COL_DOZE_MINUTE = "dozeMinute";
	public static final String COL_WAKE_HOUR = "wakeHour";
	public static final String COL_WAKE_MINUTE = "wakeMinute";
	public static final String COL_ACTIVATED = "activated";

    private static final String STATEMENT_CREATE = "create table " + TABLE + " (" +
    		COL_ROWID + " integer primary key autoincrement, " +
    		COL_NAME + " text not null, " +
    		COL_DOZE_HOUR + " integer not null, " +
    		COL_DOZE_MINUTE + " integer not null, " +
    		COL_WAKE_HOUR + " integer not null, " +
    		COL_WAKE_MINUTE + " integer not null, " +
    		COL_ACTIVATED + " integer not null);";

    private static final String STATEMENT_INSERT = "insert into " + TABLE + 
    										"(" + COL_NAME + ", " + COL_DOZE_HOUR + ", " + COL_DOZE_MINUTE + ", " + COL_WAKE_HOUR + ", " + COL_WAKE_MINUTE + ", " + COL_ACTIVATED + ") " +
    										"values (?, ?, ?, ?, ?, ?)";

    private static final String STATEMENT_UPDATE = "update " + TABLE + 
    										" set " + COL_DOZE_HOUR + "= ?, " + COL_DOZE_MINUTE + "= ?, " + COL_WAKE_HOUR + "= ?, " + COL_WAKE_MINUTE + "= ?, " + COL_ACTIVATED + "= ? " +
    										"where " + COL_NAME + "= ?" ;

    private static final String STATEMENT_SELECT = "select count(1) FROM " + TABLE + " WHERE " + COL_NAME + " = ? ";
    
    private final SQLiteStatement insertStatement;
    private final SQLiteStatement updateStatement;
    private final SQLiteStatement selectStatement;

    private final SQLiteDatabase db;
    
    public TableDetails(Context ctx, SQLiteDatabase sqldb)
    {
    	db = sqldb;
    	
        insertStatement = db.compileStatement(STATEMENT_INSERT);
        updateStatement = db.compileStatement(STATEMENT_UPDATE);
        selectStatement = db.compileStatement(STATEMENT_SELECT);
    }
    
    public static String getCreateStatement()
    {
    	return STATEMENT_CREATE;
    }
    
    public static String getTableName()
    {
    	return TABLE;
    }
    
    public List<DozeRecord> selectAll(String nameFilter)
    {
    	List<DozeRecord> list = new ArrayList<DozeRecord>();

    	db.beginTransaction();

    	try
    	{
	    	Cursor cursor = db.query(TABLE, new String[] {COL_NAME, COL_DOZE_HOUR, COL_DOZE_MINUTE, COL_WAKE_HOUR, COL_WAKE_MINUTE, COL_ACTIVATED}, COL_NAME + " = ?", new String[] {nameFilter}, null, null, COL_NAME);
	    	if (cursor.moveToFirst())
	    	{
	    		do
	    		{
	    			DozeRecord record = new DozeRecord();
	    			record.name = cursor.getString(0);
	    			record.dozeHour = cursor.getLong(1);
	    			record.dozeMinute = cursor.getLong(2);
	    			record.wakeHour = cursor.getLong(3);
	    			record.wakeMinute = cursor.getLong(4);
	    			record.activated = (cursor.getLong(5) == 1);
	    			
	    			list.add(record);
	    		} while (cursor.moveToNext());
	    	}
	    	
	    	if (cursor != null && !cursor.isClosed())
	    		cursor.close();

	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "ERROR: Failed to select from details table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}
    	
    	return list;
    }

    public void insertOrUpdate(String name, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti)
    {
    	db.beginTransaction();

    	try
    	{
    		boolean exists = exists(name);
    	
	    	if (exists)
	    		update(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);
	    	else
	    		insert(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);

	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "insertOrUpdate           :ERROR: Failed to add a record to details table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}
    }

    public long insert(String name, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti)
    {
    	long rowID = 0;
    	db.beginTransaction();

    	try
    	{
	    	insertStatement.bindString(1, name);
	    	insertStatement.bindLong(2, dozeHour);
	    	insertStatement.bindLong(3, dozeMinute);
	    	insertStatement.bindLong(4, wakeHour);
	    	insertStatement.bindLong(5, wakeMinute);
	    	insertStatement.bindLong(6, acti ? 1 : 0);
	    	rowID = insertStatement.executeInsert();

	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "ERROR: Failed to insert into details table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}

    	return rowID;
    }

    public int update(String name, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean acti)
    {
    	int rowsUpdated = 0;
    	db.beginTransaction();

    	try
    	{
	    	updateStatement.bindLong(1, dozeHour);
	    	updateStatement.bindLong(2, dozeMinute);
	    	updateStatement.bindLong(3, wakeHour);
	    	updateStatement.bindLong(4, wakeMinute);
	    	updateStatement.bindLong(5, acti ? 1 : 0);
	    	updateStatement.bindString(6, name);
	    	updateStatement.execute();

	    	boolean exists = exists(name);
	    	if (exists)
	    		rowsUpdated = 1;

	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "update                   :ERROR: Failed to update details table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}
    	
    	return rowsUpdated;
    }

    private boolean exists(String name)
    {
    	long numRows = 0;
//    	db.beginTransaction();

    	try
    	{
	    	selectStatement.bindString(1, name);
	    	numRows = selectStatement.simpleQueryForLong();
//	    	db.setTransactionSuccessful();
    	}
    	catch (Exception ex)
    	{
        	Log.e(TAG, LogPrefix + "ERROR: Failed to check if any records are in details table: " + ex.getMessage());
        	ex.printStackTrace();
    	}
//    	finally
//    	{
//    		db.endTransaction();
//    	}
    	
		return (numRows > 0 ? true : false);
    }
}

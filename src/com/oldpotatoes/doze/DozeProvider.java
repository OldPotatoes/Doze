package com.oldpotatoes.doze;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DozeProvider extends ContentProvider 
{
	private static final String TAG = "Doze";
	private static final String LogPrefix = "Provider      : ";

	private static final String AUTHORITY = "com.oldpotatoes.doze.DozeProvider";
	private static final String DOZES_BASE_PATH = "dozes";
	private static final String LOGGING_BASE_PATH = "logging";
	public static final Uri DOZES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DOZES_BASE_PATH);
	public static final Uri LOGGING_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOGGING_BASE_PATH);

	public static final String DOZE_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.oldpotatoes.doze.configuration";
	public static final String DOZE_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.oldpotatoes.doze.configuration";
	public static final String LOGGING_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.oldpotatoes.doze.logging";
	public static final String LOGGING_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.oldpotatoes.doze.logging";

	public static final String DETAILS_ROWID = TableDetails.COL_ROWID;
	public static final String DETAILS_NAME = TableDetails.COL_NAME;
	public static final String DETAILS_DOZE_HOUR = TableDetails.COL_DOZE_HOUR;
	public static final String DETAILS_DOZE_MINUTE = TableDetails.COL_DOZE_MINUTE;
	public static final String DETAILS_WAKE_HOUR = TableDetails.COL_WAKE_HOUR;
	public static final String DETAILS_WAKE_MINUTE = TableDetails.COL_WAKE_MINUTE;
	public static final String DETAILS_ACTIVATED = TableDetails.COL_ACTIVATED;
	
    public static final String LOGGING_ROWID = TableLogging.COL_ROWID;
    public static final String LOGGING_DATETIME = TableLogging.COL_DATETIME;
    public static final String LOGGING_STATUS = TableLogging.COL_STATUS;
    public static final String LOGGING_MESSAGE = TableLogging.COL_MESSAGE;

    public static final int DOZES = 1;
	public static final int DOZE_ID = 2;
	public static final int LOGGING = 3;
	public static final int LOGGING_ID = 4;

	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static 
	{
	    uriMatcher.addURI(AUTHORITY, DOZES_BASE_PATH, DOZES);
	    uriMatcher.addURI(AUTHORITY, DOZES_BASE_PATH + "/#", DOZE_ID);
	    uriMatcher.addURI(AUTHORITY, LOGGING_BASE_PATH, LOGGING);
	    uriMatcher.addURI(AUTHORITY, LOGGING_BASE_PATH + "/#", LOGGING_ID);
	}	
	
	private DatabaseHelper db;

	@Override
	public boolean onCreate() 
	{
		db = new DatabaseHelper(getContext());
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	 
	    int uriType = uriMatcher.match(uri);
	    switch (uriType)
	    {
	    	case DOZES:
	    		queryBuilder.setTables(TableDetails.TABLE);
	    	    if (sortOrder == null || sortOrder == "")
	    	    	sortOrder = TableDetails.COL_NAME;
	    		break;
		    case DOZE_ID:
	    		queryBuilder.setTables(TableDetails.TABLE);
		        queryBuilder.appendWhere(TableDetails.COL_ROWID + "=" + uri.getLastPathSegment());
		        break;
		    case LOGGING:
	    		queryBuilder.setTables(TableLogging.TABLE);
	    	    if (sortOrder == null || sortOrder == "")
	    	    	sortOrder = TableLogging.COL_DATETIME;
	    		break;
		    case LOGGING_ID:
			    queryBuilder.setTables(TableLogging.TABLE);
		        queryBuilder.appendWhere(TableLogging.COL_ROWID + "=" + uri.getLastPathSegment());
	    		break;

		    default:
		        throw new IllegalArgumentException("Unknown URI");
	    }
	    
	    Cursor cursor = queryBuilder.query(db.getDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    return cursor;
	}

	@Override
	public String getType(Uri uri) 
	{
		switch (uriMatcher.match(uri))
		{
			case DOZES:
				return DOZE_TYPE;
			case DOZE_ID:
				return DOZE_ITEM_TYPE;
			case LOGGING:
				return LOGGING_TYPE;
			case LOGGING_ID:
				return LOGGING_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		Uri _uri = null;
		long rowID = 0;
		
		try
		{
			switch (uriMatcher.match(uri))
			{
				case DOZES:
					String name = values.getAsString(TableDetails.COL_NAME);
					long dozeHour = values.getAsLong(TableDetails.COL_DOZE_HOUR);
					long dozeMinute = values.getAsLong(TableDetails.COL_DOZE_MINUTE);
					long wakeHour = values.getAsLong(TableDetails.COL_WAKE_HOUR);
					long wakeMinute = values.getAsLong(TableDetails.COL_WAKE_MINUTE);
					boolean acti = values.getAsBoolean(TableDetails.COL_ACTIVATED);
	
					rowID = db.insertDetails(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);
					if (rowID == 0)
						throw new SQLException("Failed to insert row into " + uri);
			
					_uri = ContentUris.withAppendedId(DOZES_CONTENT_URI, rowID);
					break;
	
				case LOGGING:
					String dateText = values.getAsString(TableLogging.COL_DATETIME);
					String status = values.getAsString(TableLogging.COL_STATUS);
					String message = values.getAsString(TableLogging.COL_MESSAGE);
	
					rowID = db.addLogging(dateText, status, message);
					if (rowID == 0)
						throw new SQLException("Failed to insert row into " + uri);
			
					_uri = ContentUris.withAppendedId(LOGGING_CONTENT_URI, rowID);
					break;
			
				default: throw new SQLException("Failed to insert row into " + uri);
			}

			if (rowID != 0)
				getContext().getContentResolver().notifyChange(_uri, null);    
		}
		catch (Exception ex)
		{
			Log.e(TAG, LogPrefix + "insert threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}

		return _uri;    
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int count = 0;
		
		try
		{
			switch (uriMatcher.match(uri))
			{
				case DOZES:
					String name = values.getAsString(TableDetails.COL_NAME);
					long dozeHour = values.getAsLong(TableDetails.COL_DOZE_HOUR);
					long dozeMinute = values.getAsLong(TableDetails.COL_DOZE_MINUTE);
					long wakeHour = values.getAsLong(TableDetails.COL_WAKE_HOUR);
					long wakeMinute = values.getAsLong(TableDetails.COL_WAKE_MINUTE);
					boolean acti = values.getAsBoolean(TableDetails.COL_ACTIVATED);

					count = db.updateDetails(name, dozeHour, dozeMinute, wakeHour, wakeMinute, acti);
					break;

				default: 
					throw new IllegalArgumentException("Unknown URI " + uri);    
			}       

			if (count > 0)
				getContext().getContentResolver().notifyChange(uri, null);    
		}
		catch (Exception ex)
		{
			Log.e(TAG, LogPrefix + "update threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}

		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int count = 0;
		
		try
		{
			switch (uriMatcher.match(uri))
			{
				case LOGGING:
					count = db.pruneLogging();
					break;

				default: 
					throw new IllegalArgumentException("Unknown URI " + uri);    
			}       

			if (count > 0)
				getContext().getContentResolver().notifyChange(uri, null);    
		}
		catch (Exception ex)
		{
			Log.e(TAG, LogPrefix + "delete threw exception: " + ex.getMessage());
			ex.printStackTrace();
		}

		return count;
	}

}

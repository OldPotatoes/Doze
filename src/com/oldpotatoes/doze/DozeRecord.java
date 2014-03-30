package com.oldpotatoes.doze;

import android.os.Parcel;
import android.os.Parcelable;

public class DozeRecord implements Parcelable
{
	public String name;
	public long dozeHour;
	public long dozeMinute;
	public long wakeHour;
	public long wakeMinute;
	public boolean activated;

	public DozeRecord()
	{
//        Log.d(TAG, LogPrefix + "DozeRecord constructor1");
		name = "";
		dozeHour = 0;
		dozeMinute = 0;
		wakeHour = 0;
		wakeMinute = 0;
		activated = false;
	}

	private DozeRecord(Parcel source) {
//        Log.d(TAG, LogPrefix + "DozeRecord constructor2");
		name = source.readString();
		dozeHour = source.readLong();
		dozeMinute = source.readLong();
		wakeHour = source.readLong();
		wakeMinute = source.readLong();
		activated = (source.readInt() == 1);
	}

	public DozeRecord(String nom, long dHour, long dMinute, long wHour, long wMinute, boolean acti)
	{
//        Log.d(TAG, LogPrefix + "DozeRecord constructor3");
		name = nom;
		dozeHour = dHour;
		dozeMinute = dMinute;
		wakeHour = wHour;
		wakeMinute = wMinute;
		activated = acti;
	}

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(name);
		dest.writeLong(dozeHour);
		dest.writeLong(dozeMinute);
		dest.writeLong(wakeHour);
		dest.writeLong(wakeMinute);
		dest.writeInt(activated ? 1 : 0);
	}

	public static final Creator<DozeRecord> CREATOR = new Creator<DozeRecord>() {
		public DozeRecord createFromParcel(Parcel source) {
			return new DozeRecord(source);
		}

		public DozeRecord[] newArray(int size) {
			return new DozeRecord[size];
		}
	};
}

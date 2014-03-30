/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\jamie.dyer\\workspace\\Doze\\Doze\\src\\com\\oldpotatoes\\doze\\ITimerService.aidl
 */
package com.oldpotatoes.doze;
// Doze timer interface.

public interface ITimerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.oldpotatoes.doze.ITimerService
{
private static final java.lang.String DESCRIPTOR = "com.oldpotatoes.doze.ITimerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.oldpotatoes.doze.ITimerService interface,
 * generating a proxy if needed.
 */
public static com.oldpotatoes.doze.ITimerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.oldpotatoes.doze.ITimerService))) {
return ((com.oldpotatoes.doze.ITimerService)iin);
}
return new com.oldpotatoes.doze.ITimerService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setTimeDetails:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
long _arg2;
_arg2 = data.readLong();
long _arg3;
_arg3 = data.readLong();
long _arg4;
_arg4 = data.readLong();
boolean _arg5;
_arg5 = (0!=data.readInt());
this.setTimeDetails(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
return true;
}
case TRANSACTION_setActivated:
{
data.enforceInterface(DESCRIPTOR);
this.setActivated();
reply.writeNoException();
return true;
}
case TRANSACTION_setDeactivated:
{
data.enforceInterface(DESCRIPTOR);
this.setDeactivated();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.oldpotatoes.doze.ITimerService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// Send doze details to the service

public void setTimeDetails(java.lang.String dozeName, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean activated) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dozeName);
_data.writeLong(dozeHour);
_data.writeLong(dozeMinute);
_data.writeLong(wakeHour);
_data.writeLong(wakeMinute);
_data.writeInt(((activated)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setTimeDetails, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Begin the timer

public void setActivated() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_setActivated, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// End the timer

public void setDeactivated() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_setDeactivated, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setTimeDetails = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setActivated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setDeactivated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
// Send doze details to the service

public void setTimeDetails(java.lang.String dozeName, long dozeHour, long dozeMinute, long wakeHour, long wakeMinute, boolean activated) throws android.os.RemoteException;
// Begin the timer

public void setActivated() throws android.os.RemoteException;
// End the timer

public void setDeactivated() throws android.os.RemoteException;
}

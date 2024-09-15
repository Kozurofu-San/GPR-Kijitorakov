package com.example.plot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class Communication {
    MainActivity a;
    Semaphore sem;
    IntentFilter intentFilter = new IntentFilter();
    BroadcastReceiver detachReceiver;

    UsbInterface usbInterface;
    UsbEndpoint usbEndpointOut, usbEndpointIn;
    UsbDeviceConnection usbDeviceConnection;
    UsbDevice usbDevice;
    UsbManager usbManager;

    Communication(MainActivity activity){
        a = activity;
        detachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    if(connect())
                        sem.release();
                }
                if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                    sem.drainPermits();
                    disconnect();
                }
                if (intent.getAction().equals("com.android.example.USB_PERMISSION")){
                    usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    usbManager = (UsbManager) a.getSystemService(Context.USB_SERVICE);
                    boolean a = usbManager.hasPermission(usbDevice);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if(usbDevice != null)
                        {
                            if(connect())
                                sem.release();
                        }
                    }
                    else
                    {
                        Log.d("Usb request", "permission denied for device " + usbDevice);
                    }
                }
            }
        };
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        intentFilter.addAction("com.android.example.USB_PERMISSION");
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        ContextCompat.registerReceiver(a.getApplicationContext(),detachReceiver, intentFilter,ContextCompat.RECEIVER_NOT_EXPORTED);
        sem = new Semaphore(0,true);
        if (connect())
            sem.release();
    }

    boolean connect(){
        usbManager = (UsbManager) a.getSystemService(Context.USB_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(a,0,new Intent("com.android.example.USB_PERMISSION"),PendingIntent.FLAG_MUTABLE);
        HashMap<String,UsbDevice> usbDeviceList = usbManager.getDeviceList();
//        usbDevice = a.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        for (UsbDevice device : usbDeviceList.values()) {
            if (device.getVendorId() == 1155
                    && device.getProductId() == 22336) {
                usbDevice = device;
                break;
            }
        }
        if (usbDevice != null){
            if (!usbManager.hasPermission(usbDevice)) {
                usbManager.requestPermission(usbDevice, pendingIntent);
                return false;
            }
            usbInterface = usbDevice.getInterface(1);
            for (int i=0; i<usbInterface.getEndpointCount(); i++){
                if (usbInterface.getEndpoint(i).getDirection()== UsbConstants.USB_DIR_IN)
                    usbEndpointIn = usbInterface.getEndpoint(i);
                if (usbInterface.getEndpoint(i).getDirection()== UsbConstants.USB_DIR_OUT)
                    usbEndpointOut = usbInterface.getEndpoint(i);
            }
            usbEndpointIn = usbInterface.getEndpoint(0);
            usbEndpointOut = usbInterface.getEndpoint(1);
            if (usbManager != null && usbDevice != null)
                usbDeviceConnection = usbManager.openDevice(usbDevice);
            if (usbDeviceConnection == null)
                return false;
            usbDeviceConnection.claimInterface(usbInterface,true);
            return true;
        }
        else
            return false;
    }

    void disconnect(){
        if (usbDeviceConnection != null){
            usbDeviceConnection.releaseInterface(usbInterface);
            usbDeviceConnection.close();
        }
    }

    void write(byte[] buffer, int length){
        usbDeviceConnection.bulkTransfer(usbEndpointIn,buffer,length,0);
        if (length%64==0 & length>0)
            usbDeviceConnection.bulkTransfer(usbEndpointIn,buffer,0,0);    // ZLP
    }

    void read(byte[] data, int length){
        usbDeviceConnection.bulkTransfer(usbEndpointOut,data,length,0);
    }

    int cnt;
    void setByteCnt(int n){
        cnt = n;
    }
    int getByteCnt(){
        return cnt;
    }

    void putByteToByteArray(byte[] buffer, byte val){
        buffer[cnt++] = val;
    }
    byte getByteFromByteArray(byte[] buffer){
        return buffer[cnt++];
    }

    void putShortToByteArray(byte[] buffer, short val){
        buffer[cnt++] = (byte)(val&0xFF);
        buffer[cnt++] = (byte)(val>>8);
    }
    void putShortToByteArray(byte[] buffer, short[] array){
        for (short value : array) {
            buffer[cnt++] = (byte) (value & 0xFF);
            buffer[cnt++] = (byte) (value >> 8);
        }
    }
    short getShortFromByteArray(byte[] buffer){
        return (short) (buffer[cnt++]<<8|buffer[cnt++]);
    }
    void getShortFromByteArray(byte[] buffer, short[] array){
        for (int i=0; i<array.length; i++)
            array[i] = (short)(buffer[cnt++]<<8|buffer[cnt++]);
    }
    void getEvenShortFromByteArray(byte[] buffer, float[] array){
        cnt = 0;
        for (int i=0; i<array.length; i++) {
            array[i] = (short)(buffer[cnt]&0xFF|(buffer[cnt+1]&0xFF)<<8);
            cnt += 4;
        }
    }
    void getOddShortFromByteArray(byte[] buffer, float[] array){
        cnt = 2;
        for (int i=0; i<array.length; i++) {
            array[i] = (short)(buffer[cnt]&0xFF|(buffer[cnt+1]&0xFF)<<8);
            cnt += 4;
        }
    }

    void putIntToByteArray(byte[] buffer, int val){
        buffer[cnt++] = (byte)(val>>24);
        buffer[cnt++] = (byte)((val>>16)&0xFF);
        buffer[cnt++] = (byte)((val>>8)&0xFF);
        buffer[cnt++] = (byte)(val&0xFF);
    }
    void putIntToByteArray(byte[] buffer, int[] array) {
        for (int val : array) {
            buffer[cnt++] = (byte) (val >> 24);
            buffer[cnt++] = (byte) ((val >> 16) & 0xFF);
            buffer[cnt++] = (byte) ((val >> 8) & 0xFF);
            buffer[cnt++] = (byte) (val & 0xFF);
        }
    }
    int getIntFromByteArray(byte[] buffer){
        return (buffer[cnt++]<<24|buffer[cnt++]<<16|buffer[cnt++]<<8|buffer[cnt++]);
    }
    void getIntFromByteArray(byte[] buffer, int[] array){
        for (int i=0; i<array.length; i++)
            array[i] = (buffer[cnt++]<<24|buffer[cnt++]<<16|buffer[cnt++]<<8|buffer[cnt++]);
    }

    void putStringToByteArray(byte[] buffer, String string){
        char[] chars = string.toCharArray();
        for (char c : chars) buffer[cnt++] = (byte) c;
    }
    String getStringFromByteArray(byte[] buffer, int n){
        char[] string = new char[n];
        for (int i=0; i<n; i++)
            string[i] = (char) buffer[cnt++];
        return new String(string);
    }

}

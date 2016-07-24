package com.semlerscientific.plugin;

import android.app.PendingIntent;
import android.app.Service;
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
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

public class SSSensorService extends Service
{
    private static final int SS_SENSOR_PID = 0x0303;
    private static final int SS_SENSOR_VID = 0x2047;

    private static final String ACTION_USB_PERMISSION = "com.google.android.HID.action.USB_PERMISSION";

    private static UsbDeviceConnection mConnection;
    private static UsbEndpoint mEndPointRead;
    private static UsbEndpoint mEndPointWrite;
    private static boolean mIsConnected;
    private static PendingIntent mPermissionIntent;
    private static UsbManager mUsbManager;
    private static UsbDevice mUsbDevice;

    public SSSensorService()
    {
    }

    //* looks to see if the device is connected and if it is then starts the connection state machine
    public static void lookForDevice()
    {
        if(!mIsConnected)
        {
            for (UsbDevice usbDevice : mUsbManager.getDeviceList().values())  //list VID/PID of all devices
            {
                if (SS_SENSOR_VID == usbDevice.getVendorId() &&
                        SS_SENSOR_PID == usbDevice.getProductId())
                {
                    //this kicks off the permission request process which asks the user to click that it's okay
                    //to use the device, which will call into mBroadcastReceiver
                    mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                }
            }
        }
    }

    public static boolean isConnected()
    {
        return mIsConnected;
    }

    /**
     *
     * This method reads end point 0 for data sent by the device and placed in a byte buffer.
     * The buffer is then parsed and data converted to characters and appended to StringBuilder
     * class for display.  This method is called every 100ms by the handler.
     *
     */
    public static byte[] receive()
    {
        int packetSize;
        byte[] retVal = new byte[0];
        int size = 0;

        try
        {
            if (mConnection != null && mEndPointRead != null)//Verify USB connection and data at end point
            {
                packetSize = mEndPointRead.getMaxPacketSize();

                final byte[] buffer = new byte[packetSize];        //Create new byte buffer every time
                final int status = mConnection.bulkTransfer(mEndPointRead, buffer, packetSize, 64);    //Read 64 bytes of data from end point 0 and store
                //it in buffer
                if (status >= 0)
                {
                    int length = Integer.valueOf((int) buffer[1]);
                    if (length < packetSize)
                    {
                        size = length;
                    }
                    else
                    {
                        size = packetSize;
                    }
                    retVal = Arrays.copyOfRange(buffer, 2, size + 2);
                }
            }
        }
        catch (Exception e)
        {
            //mLog("Exception: " + e.getLocalizedMessage());
            Log.w("setupReceiver", e);
            mIsConnected = false;
        }

        return retVal;
    }

    public static void send(byte[] data)
    {
        if (mUsbDevice != null && mEndPointWrite != null && mUsbManager.hasPermission(mUsbDevice))
        {
            while (data.length > 252)
            {                                //Length of entered text is greater than 252 characters
                byte[] temp = Arrays.copyOfRange(data, 0, 252);
                data = Arrays.copyOfRange(data, 253, data.length- 1);
                byte arr[] = new byte[255];
                arr[0] = 63;  //TI Report ID.  Vendor Specific		//Specify vendor specific HID Report ID
                arr[1] = (byte)(arr.length - 2);                   //MSP430 HID data block of 253 bytes

                for (int i = 0; i < temp.length; i++)
                {
                    arr[i + 2] = temp[i];
                }
                int status = mConnection.bulkTransfer(mEndPointWrite, arr, arr.length, 255);  //Send data to device
            }

            byte arr[] = new byte[data.length + 2];                    //Length of entered text is less than 252 characters
            arr[0] = 63;                                            //HID Report ID - vendor specific
            arr[1] = (byte) (arr.length - 2);                        //HID data block chunk of 253 bytes

            for (int i = 0; i < data.length; i++)
            {
                arr[i + 2] = data[i];
            }
            int status = mConnection.bulkTransfer(mEndPointWrite, arr, arr.length, 255);  //Send data
        }
    }

    /**
     * receives the permission request to connect usb devices
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    setDevice(intent);
                }
            }
            //device attached
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
            {
                synchronized (this)
                {
                    setDevice(intent);        //Connect to the selected device
                }
                //mLog("device connected");
            }
            //device detached
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
            {
                mUsbDevice = null;
                mIsConnected = false;
                //mLog("device disconnected");
            }
        }

        /**
         * Connects to user selected VID/PID
         */
        private void setDevice(Intent intent)
        {
            mUsbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (mUsbDevice != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
            {
                mConnection = mUsbManager.openDevice(mUsbDevice);        //Connect to device
                UsbInterface intf = mUsbDevice.getInterface(0);
                if (null == mConnection)
                {
                    //mLog("(unable to establish connection)\n");
                    return;
                }
                else//Device connected - claim ownership over the interface
                {
                    //mLog("connection established\n");
                    mConnection.claimInterface(intf, true);
                }
                try
                {
                    //Direction of end point 1 - OUT - from host to device
                    if (UsbConstants.USB_DIR_OUT == intf.getEndpoint(1).getDirection())
                    {
                        mEndPointWrite = intf.getEndpoint(1);
                    }
                }
                catch (Exception e)
                {
                    Log.e("endPointWrite", "Device have no endPointWrite", e);
                    return;
                }
                try
                {
                    //Direction of end point 0 - IN - from device to host
                    if (UsbConstants.USB_DIR_IN == intf.getEndpoint(0).getDirection())
                    {
                        mEndPointRead = intf.getEndpoint(0);
                    }
                }
                catch (Exception e)
                {
                    Log.e("endPointWrite", "Device have no endPointRead", e);
                    return;
                }

                mIsConnected = true;
            }
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();

        mIsConnected = false;
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mUsbDevice = null;
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);	//Get USB permission intent for broadcast
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);				//Register broadcast receiver
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}

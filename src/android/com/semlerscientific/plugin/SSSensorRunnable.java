package com.semlerscientific.plugin;

import android.os.AsyncTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SSSensorRunnable implements Runnable
{
    private static final String SS_SENSOR_NOT_CONNECTED = "Sensor not connected.";

    public interface IReceiveCallback
    {
        void dataReceived(String s);
    }

    public void setReceiveCallback(IReceiveCallback c)
    {
        mReceiveCallback = c;
    }

    public interface IIsInitializedChangedCallback
    {
        void isInitializedChanged(boolean isInitialized);
    }

    public void setIsInitializedChangedCallback(IIsInitializedChangedCallback c)
    {
        mIsInitializedChangedCallback = c;
        if(null != mIsInitializedChangedCallback)
        {
            //send initial isInitialized state
            mIsInitialized = isInitialized();
            mIsInitializedChangedCallback.isInitializedChanged(mIsInitialized);
        }
    }

    public interface IPulseDataReceivedCallback
    {
        void pulseDataReceived(int pulseData);
    }

    public void setPulseDataReceivedCallback(IPulseDataReceivedCallback c)
    {
        mPulseDataReceivedCallback = c;
    }

    public static class StartMeasurementResponse
    {
        public static final int STARTED = 0;
        public static final int NOT_ADJUSTING = 1;
        public static final int ENDED_SUCCESSFULLY_FINAL = 2;
        public static final int CANCELED_FINAL = 3;
        public static final int AMBIENT_LIGHT_FINAL = 4;
        public static final int ERROR_FINAL = 5;
        public static final int NOT_CONNECTED_FINAL = 6;
        public static final int WRONG_STATE_FINAL = 7;

        private final int mValue;

        StartMeasurementResponse(int value)
        {
            mValue = value;
        }

        public int value()
        {
            return mValue;
        }

        @Override
        public String toString()
        {
            switch(mValue)
            {
                case STARTED:
                    return "STARTED";
                case NOT_ADJUSTING:
                    return "NOT_ADJUSTING";
                case ENDED_SUCCESSFULLY_FINAL:
                    return "ENDED_SUCCESSFULLY_FINAL";
                case CANCELED_FINAL:
                    return "CANCELED_FINAL";
                case AMBIENT_LIGHT_FINAL:
                    return "AMBIENT_LIGHT_FINAL";
                case ERROR_FINAL:
                    return "ERROR_FINAL";
                case NOT_CONNECTED_FINAL:
                    return "NOT_CONNECTED";
                case WRONG_STATE_FINAL:
                    return "WRONG_STATE";
            }
            return "UNKNOWN";
        }
    }

    public interface IStartMeasurementResponseCallback
    {
        void startMeasurementResponse(StartMeasurementResponse response);
    }

    public void setStartMeasurementResponseCallback(IStartMeasurementResponseCallback c)
    {
        mStartMeasurementResponseCallback = c;
    }

    public interface ISyncResponseCallback
    {
        void syncResponse(int result, String resultDescription);
    }

    public String getProductId()
    {
        if(isInitialized())
        {
            return mProductId;
        }
        else
        {
            return SS_SENSOR_NOT_CONNECTED;
        }
    }

    public String getSerialNumber()
    {
        if(isInitialized())
        {
            return mSerialNumber;
        }
        else
        {
            return SS_SENSOR_NOT_CONNECTED;
        }
    }

    public String getProductVersion()
    {
        if(isInitialized())
        {
            return mProductVersion;
        }
        else
        {
            return SS_SENSOR_NOT_CONNECTED;
        }
    }

    public int getUses()
    {
        if(isInitialized())
        {
            return mInfoMemoryUses;
        }
        else
        {
            return 0;
        }
    }

    public int getPositives()
    {
        if(isInitialized())
        {
            return mInfoMemoryPositives;
        }
        else
        {
            return 0;
        }
    }

    public int getGraceUsesRemaining()
    {
        if(isInitialized())
        {
            return mInfoMemoryGraceUsesRemaining;
        }
        else
        {
            return 0;
        }
    }

    public Date getLastSyncTimeUtc()
    {
        if(isInitialized())
        {
            return mInfoMemoryLastDbConnectionDateTimeUtc;
        }
        else
        {
            return new Date();
        }
    }

    public int getSyncFreqDays()
    {
        if(isInitialized())
        {
            return mInfoMemorySyncFreqDays;
        }
        else
        {
            return 0;
        }
    }

    public String getCustomerId()
    {
        if(isInitialized())
        {
            return mInfoMemoryCustomerId;
        }
        else
        {
            return SS_SENSOR_NOT_CONNECTED;
        }
    }

    public String getPaymentStatus()
    {
        if(isInitialized())
        {
            return mInfoMemoryPaymentStatus;
        }
        else
        {
            return SS_SENSOR_NOT_CONNECTED;
        }
    }

    public void setPatientData(String patientLastName, String patientId, Date patientBirthday)
    {
        mTest.setPatientLastName(patientLastName);
        mTest.setPatientId(patientId);
        mTest.setPatientBirthday(patientBirthday);

        SSLogger.writeData("Setting Patient Last Name: " + patientLastName + ", ID: " + patientId + ", Birthday: " + patientBirthday);
    }

    public void newTest()
    {
        mTest = new SSTest();

        SSLogger.writeData("New test.");
    }

    public boolean clearUsesAndPositives()
    {
        byte[] message;

        if(ConnectState.INITIALIZED != mState)
        {
            return false;
        }

        //abcd mInfoMemoryUses = 0;
        //abcd mInfoMemoryPositives = 0;
        //abcd mInfoMemoryCurrentUseIsPositive = false;

        message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_USES, Integer.toString(mInfoMemoryUses));
        SSSensorService.send(message);
        message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_POSITIVES, Integer.toString(mInfoMemoryPositives));
        SSSensorService.send(message);
        message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_CURRENT_USE_IS_POSITIVE, mInfoMemoryCurrentUseIsPositive ? SSSensorMessage.INFO_MEMORY_CURRENT_USE_IS_POSITIVE_TRUE : SSSensorMessage.INFO_MEMORY_CURRENT_USE_IS_POSITIVE_FALSE);
        SSSensorService.send(message);

        message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_USES});
        SSSensorService.send(message);
        message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_POSITIVES});
        SSSensorService.send(message);
        message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_CURRENT_USE_IS_POSITIVE});
        SSSensorService.send(message);

        return true;
    }

    public void sendStartMeasurement(SSMeasurement.SideLimb sideLimb)
    {
        if(false == mIsInitialized)
        {
            SSLogger.writeData("Attempted to start measurement while sensor not connected.");

            if(null != mStartMeasurementResponseCallback)
            {
                mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.NOT_CONNECTED_FINAL));
            }
        }
        else
        {
            if(ConnectState.INITIALIZED == mState)
            {
                SSLogger.writeData("Starting Measurement on " + sideLimb);
                mCurrentMeasurement = new SSMeasurement(sideLimb);

                byte[] message = SSSensorMessage.generate(SSSensorMessage.COMMAND_ENABLE_CHANNEL);
                SSSensorService.send(message);

                if(null != mStartMeasurementResponseCallback)
                {
                    mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.STARTED));
                }
            }
            else
            {
                SSLogger.writeData("Attempted to start measurement while in state: " + mState);

                if(null != mStartMeasurementResponseCallback)
                {
                    mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.WRONG_STATE_FINAL));
                }
            }
        }
    }

    public void sendCancelMeasurement()
    {
        byte[] message = SSSensorMessage.generate(SSSensorMessage.COMMAND_DISABLE_CHANNEL);
        SSSensorService.send(message);
    }

    public Double getLeftResult()
    {
        return mTest.getLeftResult();
    }

    public Double getRightResult()
    {
        return mTest.getRightResult();
    }

    public void onlineSync(final ISyncResponseCallback responseCallback)
    {
        if(isInitialized())
        {
            class SyncAsync extends AsyncTask<Void, Void, Void>
            {
                private SSWebService.LicensingDbSensorSyncResponse syncResponse = null;

                @Override
                protected Void doInBackground(Void... params)
                {
                    try
                    {
                        SSWebService.LicensingDbSensorSyncRequest syncRequest = new SSWebService.LicensingDbSensorSyncRequest();
                        syncRequest.serialNumber = mSerialNumber;
                        syncRequest.uses = mInfoMemoryUses;
                        syncRequest.positives = mInfoMemoryPositives;
                        syncRequest.externalIp = "";
                        syncRequest.systemUuid = "";
                        syncRequest.teamViewerId = "";
                        syncRequest.appVersion = SSSensorPluginDetails.PluginName + " " + SSSensorPluginDetails.Version;
                        syncRequest.site = "";
                        syncResponse = SSWebService.LicensingDbSensorSync(syncRequest);
                    }
                    catch(Exception e)
                    {
                        SSLogger.writeException(e, "SSWebService.LicensingDbSensorSync");
                        syncResponse = null;
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid)
                {
                    byte[] message;

                    super.onPostExecute(aVoid);

                    if(null == syncResponse)
                    {
                        responseCallback.syncResponse(-1, "Unable to connect to web service.");
                    }
                    else
                    {
                        if(SSWebService.RESULT_SUCCESS == syncResponse.result)
                        {
                            updateDbConnectionDateTime(new Date());

                            if(!mInfoMemoryCustomerId.equals(Integer.toString(syncResponse.customerId)))
                            {
                                mInfoMemoryCustomerId = Integer.toString(syncResponse.customerId);

                                message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_CUSTOMER_ID, mInfoMemoryCustomerId);
                                SSSensorService.send(message);
                            }

                            if(mInfoMemorySyncFreqDays != syncResponse.syncFreq)
                            {
                                mInfoMemorySyncFreqDays = syncResponse.syncFreq;

                                message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_SYNC_FREQ_DAYS, Integer.toString(mInfoMemorySyncFreqDays));
                                SSSensorService.send(message);
                            }

                            if(!mInfoMemoryPaymentStatus.equals(syncResponse.sensorStatus))
                            {
                                mInfoMemoryPaymentStatus = syncResponse.sensorStatus;

                                message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_PAYMENT_STATUS, mInfoMemoryPaymentStatus);
                                SSSensorService.send(message);
                            }
                        }

                        responseCallback.syncResponse(syncResponse.result, syncResponse.resultDescription);
                    }
                }

                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                }
            }

            SyncAsync syncAsync = new SyncAsync();
            syncAsync.execute();
        }
        else
        {
            responseCallback.syncResponse(-1, "Sensor not connected.");
        }
    }

    public String getOfflineSyncSensorCode()
    {
        try
        {
            if(mInfoMemoryUses > SSLicenseCodes.USES_MAX)
            {
                SSLogger.writeData("This sensor has too many uses to be synced by phone code: " + mInfoMemoryUses);
                return "This sensor cannot be synced over the phone.  Please contact Semler Scientific at 877-774-4211 for assistance.";
            }

            if(mInfoMemoryPositives > SSLicenseCodes.POSITIVES_MAX)
            {
                SSLogger.writeData("This sensor has too many positives to be synced by phone code: " + mInfoMemoryPositives);
                return "This sensor cannot be synced over the phone.  Please contact Semler Scientific at 877-774-4211 for assistance.";
            }

            mOfflineSyncTime = new Date();

            return SSLicenseCodes.SensorCodeEncode(mInfoMemoryUses, mInfoMemoryPositives, Integer.parseInt(mSerialNumber.substring(1), 10));
        }
        catch(Exception e)
        {
            SSLogger.writeException(e, "Unable to generate Sensor Code");
            return "Unable to generate Sensor Code";
        }
    }

    public void offlineSync(String updateCode, ISyncResponseCallback responseCallback)
    {
        SSLicenseCodes.UpdateCodeComponents components;
        byte[] message;

        try
        {
            components = SSLicenseCodes.UpdateCodeDecode(updateCode);
        }
        catch(Exception e)
        {
            SSLogger.writeException(e, "Unable to decode Update Code");
            responseCallback.syncResponse(-1, "Unable to decode Update Code");
            return;
        }

        String sensorSnString = "P" + String.format("%07d", components.sensorSn);

        if(!sensorSnString.equals(mSerialNumber))
        {
            SSLogger.writeData("Invalid sensor indicated in Phone Synce Update Code");
            responseCallback.syncResponse(-1, "Invalid Update Code");
            return;
        }

        int newSyncFreq = (int)((components.nextUpdateDate.getTime() - mOfflineSyncTime.getTime()) / 1000 / 60 / 60 / 24);

        if(Math.abs(newSyncFreq - 30) < 2)
        {
            newSyncFreq = 30;
        }
        else if(Math.abs(newSyncFreq - 90) < 2)
        {
            newSyncFreq = 90;
        }
        else if(Math.abs(newSyncFreq - 180) < 2)
        {
            newSyncFreq = 180;
        }
        else if(Math.abs(newSyncFreq - 365) < 2)
        {
            newSyncFreq = 365;
        }
        else
        {
            SSLogger.writeData("The sync frequency implied by the end date in the update code is: " + newSyncFreq + ", but the only valid values are 30, 90, 180 and 365.");
            responseCallback.syncResponse(-1, "Invalid Update Code");
            return;
        }

        if(mInfoMemorySyncFreqDays != newSyncFreq)
        {
            mInfoMemorySyncFreqDays = newSyncFreq;

            message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_SYNC_FREQ_DAYS, Integer.toString(mInfoMemorySyncFreqDays));
            SSSensorService.send(message);
        }

        updateDbConnectionDateTime(mOfflineSyncTime);

        responseCallback.syncResponse(0, "Success");
    }

    private void updateDbConnectionDateTime(Date dateUtc)
    {
        DateFormat connectionDateFormat = new SimpleDateFormat(SSSensorMessage.INFO_MEMORY_CONNECTION_DATE_TIME_UTC_FORMAT);
        DateFormat startDateFormat = new SimpleDateFormat(SSSensorMessage.INFO_MEMORY_USE_START_DATE_TIME_UTC_FORMAT);
        byte[] message;

        mInfoMemoryLastDbConnectionDateTimeUtc = dateUtc;
        mInfoMemoryGraceUsesRemaining = 3;

        message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_LAST_DB_CONNECTION_DATE_TIME_UTC, connectionDateFormat.format(mInfoMemoryLastDbConnectionDateTimeUtc));
        SSSensorService.send(message);

        message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_GRACE_USES_REMAINING, Integer.toString(mInfoMemoryGraceUsesRemaining));
        SSSensorService.send(message);

        //correct the use start date/time if they've messed with their clock and it's set in the future, preventing them from starting a measurement
        if (mInfoMemoryUseStartDateTimeUtc.after(new Date()))
        {
            mInfoMemoryUseStartDateTimeUtc = new Date();

            message = SSSensorMessage.generateSetInfoMemory(SSSensorMessage.INFO_MEMORY_ADDR_USE_START_DATE_TIME_UTC, startDateFormat.format(mInfoMemoryUseStartDateTimeUtc));
            SSSensorService.send(message);
        }
    }

    private enum ConnectState
    {
        WAIT_FOR_CONNECTION,
        SEND_GET_PRODUCT_ID,
        SEND_GET_PRODUCT_VERSION,
        SEND_GET_SERIAL_NUMBER,
        SEND_GET_INFO_MEMORY_USES,
        SEND_GET_INFO_MEMORY_GRACE_USES_REMAINING,
        SEND_GET_INFO_MEMORY_USE_START_DATE_TIME_UTC,
        SEND_GET_INFO_MEMORY_PATIENT_LAST_NAME,
        SEND_GET_INFO_MEMORY_PATIENT_ID,
        SEND_GET_INFO_MEMORY_PATIENT_BIRTHDAY,
        SEND_GET_INFO_MEMORY_LAST_DB_CONNECTION_DATE_TIME_UTC,
        SEND_GET_INFO_MEMORY_CUSTOMER_ID,
        SEND_GET_INFO_MEMORY_SYNC_FREQ_DAYS,
        SEND_GET_INFO_MEMORY_PAYMENT_STATUS,
        SEND_GET_INFO_MEMORY_POSITIVES,
        SEND_GET_INFO_MEMORY_CURRENT_USE_IS_POSITIVE,
        INITIALIZED,
        MEASUREMENT_ADJUSTING,
        MEASUREMENT_RUNNING
    }

    private SSMeasurement mCurrentMeasurement;
    private boolean mInfoMemoryCurrentUseIsPositive;
    private String mInfoMemoryCustomerId;
    private int mInfoMemoryGraceUsesRemaining;
    private Date mInfoMemoryLastDbConnectionDateTimeUtc;
    private Date mInfoMemoryPatientBirthday;
    private String mInfoMemoryPatientId;
    private String mInfoMemoryPatientLastName;
    private String mInfoMemoryPaymentStatus;
    private int mInfoMemoryPositives;
    private int mInfoMemorySyncFreqDays;
    private int mInfoMemoryUses;
    private Date mInfoMemoryUseStartDateTimeUtc;
    private IIsInitializedChangedCallback mIsInitializedChangedCallback = null;
    private boolean mIsInitialized;
    private Date mOfflineSyncTime;
    private String mProductId;
    private String mProductVersion;
    private int mPulseDataCount;
    private IPulseDataReceivedCallback mPulseDataReceivedCallback = null;
    private IReceiveCallback mReceiveCallback = null;
    private String mSerialNumber;
    private IStartMeasurementResponseCallback mStartMeasurementResponseCallback = null;
    private ConnectState mState = ConnectState.WAIT_FOR_CONNECTION;
    private SSTest mTest = new SSTest();


    public boolean isInitialized()
    {
        return mIsInitialized;
    }

    @Override
    public void run()
    {
        boolean connected;
        byte[] message;
        SSSensorMessage.ParseResult parseResult = new SSSensorMessage.ParseResult();//a new ParseResult has .isValid set to false
        byte[] received = new byte[0];
        String stringInts = "";

        connected = SSSensorService.isConnected();

        if(!connected)
        {
            if(mIsInitialized)
            {
                mIsInitialized = false;
                SSLogger.writeData("Sensor disconnected");
                if(null != mIsInitializedChangedCallback)
                {
                    mIsInitializedChangedCallback.isInitializedChanged(mIsInitialized);
                }
            }

            SSSensorService.lookForDevice();

            mState = ConnectState.WAIT_FOR_CONNECTION;
        }
        else
        {
            //when first connected need to send initial command.  all other commands are sent when a response is received.
            if(mState == ConnectState.WAIT_FOR_CONNECTION)
            {
                //get product id
                message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_PRODUCT_ID);
                SSSensorService.send(message);

                mState = ConnectState.SEND_GET_PRODUCT_ID;
            }

            received = SSSensorService.receive();

            for(int i = 0; i < received.length; i++)
            {
                stringInts += String.format("%02X ", received[i]);
                parseResult = SSSensorMessage.parse(received[i]);

                switch(mState)
                {
                    case SEND_GET_PRODUCT_ID:
                        if(checkForProductIdResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_PRODUCT_VERSION);
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_PRODUCT_VERSION;
                        }
                        break;

                    case SEND_GET_PRODUCT_VERSION:
                        if(checkForProductVersionResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_SERIAL_NUMBER);
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_SERIAL_NUMBER;
                        }
                        break;

                    case SEND_GET_SERIAL_NUMBER:
                        if(checkForSerialNumberResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_USES});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_USES;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_USES:
                        if(checkForInfoMemoryUsesResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_GRACE_USES_REMAINING});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_GRACE_USES_REMAINING;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_GRACE_USES_REMAINING:
                        if(checkForInfoMemoryGraceUsesRemainingResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_USE_START_DATE_TIME_UTC});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_USE_START_DATE_TIME_UTC;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_USE_START_DATE_TIME_UTC:
                        if(checkForInfoMemoryUseStartDateTimeUtcResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_PATIENT_LAST_NAME});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_PATIENT_LAST_NAME;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_PATIENT_LAST_NAME:
                        if(checkForInfoMemoryPatientLastNameResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_PATIENT_ID});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_PATIENT_ID;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_PATIENT_ID:
                        if(checkForInfoMemoryPatientIdResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_PATIENT_BIRTHDAY});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_PATIENT_BIRTHDAY;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_PATIENT_BIRTHDAY:
                        if(checkForInfoMemoryPatientBirthdayResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_LAST_DB_CONNECTION_DATE_TIME_UTC});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_LAST_DB_CONNECTION_DATE_TIME_UTC;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_LAST_DB_CONNECTION_DATE_TIME_UTC:
                        if(checkForInfoMemoryLastDbConnectionDateTimeUtcResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_CUSTOMER_ID});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_CUSTOMER_ID;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_CUSTOMER_ID:
                        if(checkForInfoMemoryCustomerIdResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_SYNC_FREQ_DAYS});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_SYNC_FREQ_DAYS;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_SYNC_FREQ_DAYS:
                        if(checkForInfoMemorySyncFreqDaysResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_PAYMENT_STATUS});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_PAYMENT_STATUS;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_PAYMENT_STATUS:
                        if(checkForInfoMemoryPaymentStatusResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_POSITIVES});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_POSITIVES;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_POSITIVES:
                        if(checkForInfoMemoryPositivesResponse(parseResult))
                        {
                            message = SSSensorMessage.generate(SSSensorMessage.COMMAND_GET_INFO_MEMORY, new byte[]{SSSensorMessage.INFO_MEMORY_ADDR_CURRENT_USE_IS_POSITIVE});
                            SSSensorService.send(message);

                            mState = ConnectState.SEND_GET_INFO_MEMORY_CURRENT_USE_IS_POSITIVE;
                        }
                        break;

                    case SEND_GET_INFO_MEMORY_CURRENT_USE_IS_POSITIVE:
                        if(checkForInfoMemoryCurrentUseIsPositiveResponse(parseResult))
                        {
                            if(!mIsInitialized)
                            {
                                mIsInitialized = true;
                                SSLogger.writeData("Sensor connected: " + mSerialNumber);
                                if(null != mIsInitializedChangedCallback)
                                {
                                    mIsInitializedChangedCallback.isInitializedChanged(mIsInitialized);
                                }
                            }

                            mState = ConnectState.INITIALIZED;
                        }
                        break;

                    case INITIALIZED:
                        if(mReceiveCallback != null && received.length > 0 && i == received.length - 1)
                        {
                            mReceiveCallback.dataReceived(new String(received) + ": " + stringInts);
                        }

                        checkForInfoMemoryUsesResponse(parseResult);
                        checkForInfoMemoryPositivesResponse(parseResult);
                        checkForInfoMemoryCurrentUseIsPositiveResponse(parseResult);

                        if(parseResult.isValid)
                        {
                            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                                    parseResult.data.length > 0 &&
                                    parseResult.data[0] == SSSensorMessage.COMMAND_ENABLE_CHANNEL)
                            {
                                mState = ConnectState.MEASUREMENT_ADJUSTING;
                            }
                        }
                        break;

                    case MEASUREMENT_ADJUSTING:
                        if(mReceiveCallback != null && received.length > 0 && i == received.length - 1)
                        {
                            mReceiveCallback.dataReceived(new String(received) + ": " + stringInts);
                        }

                        if(parseResult.isValid)
                        {
                            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                                    parseResult.data.length > 0 &&
                                    parseResult.data[0] == SSSensorMessage.COMMAND_DISABLE_CHANNEL)
                            {
                                if(null != mStartMeasurementResponseCallback)
                                {
                                    mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.CANCELED_FINAL));
                                    mStartMeasurementResponseCallback = null;//since this is a final response
                                }

                                mState = ConnectState.INITIALIZED;
                            }
                            else if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_COMMAND &&
                                    parseResult.data.length > 1 &&
                                    parseResult.data[1] == SSSensorMessage.COMMAND_INTENSITY_NOT_ADJUSTING)
                            {
                                if(null != mStartMeasurementResponseCallback)
                                {
                                    mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.NOT_ADJUSTING));
                                }

                                mPulseDataCount = 0;
                                mState = ConnectState.MEASUREMENT_RUNNING;
                            }
                        }
                        break;

                    case MEASUREMENT_RUNNING:
                        if(mReceiveCallback != null && received.length > 0 && i == received.length - 1)
                        {
                            mReceiveCallback.dataReceived(new String(received) + ": " + stringInts);
                        }

                        if(parseResult.isValid)
                        {
                            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                                    parseResult.data.length > 0 &&
                                    parseResult.data[0] == SSSensorMessage.COMMAND_DISABLE_CHANNEL)
                            {
                                if(null != mStartMeasurementResponseCallback)
                                {
                                    mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.CANCELED_FINAL));
                                    mStartMeasurementResponseCallback = null;//since this is a final response
                                }

                                mState = ConnectState.INITIALIZED;
                            }
                            else if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_DATA)
                            {
                                //parseResult.data[0] is the channel --> ignored

                                //if there are fewer than 4 bytes of data (after the channel) then it is a single data point with that much data
                                if(parseResult.data.length <= 5)
                                {
                                    int data = 0;
                                    for(int j = 1; j < parseResult.data.length; j++)
                                    {
                                        data = (data << 8) | (parseResult.data[j] & 0xFF);// & with 0xFF converts the bit pattern from a signed byte to an unsigned int
                                    }

                                    mCurrentMeasurement.addDataPoint(data);
                                    if(mPulseDataReceivedCallback != null)
                                    {
                                        mPulseDataReceivedCallback.pulseDataReceived(data);
                                    }

                                    mPulseDataCount++;
                                }
                                //otherwise the data is 32-bit chunks and there can be any number of them
                                else
                                {
                                    for(int j = 1; j + 3 < parseResult.data.length && mPulseDataCount != SSMeasurement.SS_DURATION_SAMPLES; j += 4)
                                    {
                                        // & with 0xFF converts the bit pattern from a signed byte to an unsigned int
                                        int data = ((parseResult.data[j] & 0xFF) << 24) |
                                                ((parseResult.data[j + 1] & 0xFF) << 16) |
                                                ((parseResult.data[j + 2] & 0xFF) << 8) |
                                                ((parseResult.data[j + 3] & 0xFF));

                                        mCurrentMeasurement.addDataPoint(data);
                                        if(mPulseDataReceivedCallback != null)
                                        {
                                            mPulseDataReceivedCallback.pulseDataReceived(data);
                                        }

                                        mPulseDataCount++;
                                    }
                                }

                                if(mPulseDataCount == SSMeasurement.SS_DURATION_SAMPLES)
                                {
                                    mTest.addMeasurement(mCurrentMeasurement);

                                    if(null != mStartMeasurementResponseCallback)
                                    {
                                        mStartMeasurementResponseCallback.startMeasurementResponse(new StartMeasurementResponse(StartMeasurementResponse.ENDED_SUCCESSFULLY_FINAL));
                                        mStartMeasurementResponseCallback = null;//since this is a final response
                                    }

                                    message = SSSensorMessage.generate(SSSensorMessage.COMMAND_DISABLE_CHANNEL);
                                    SSSensorService.send(message);

                                    mState = ConnectState.INITIALIZED;
                                }
                            }
                        }
                        break;
                }
            }
        }
    }

    private boolean checkForProductIdResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_PRODUCT_ID)
            {
                mProductId = new String(parseResult.data).substring(1);

                return true;
            }
        }

        return false;
    }

    private boolean checkForProductVersionResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_PRODUCT_VERSION)
            {
                mProductVersion = new String(parseResult.data).substring(1);

                return true;
            }
        }

        return false;
    }

    private boolean checkForSerialNumberResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_SERIAL_NUMBER)
            {
                mSerialNumber = new String(parseResult.data).substring(1);

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryUsesResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_USES)
            {
                try
                {
                    mInfoMemoryUses = Integer.parseInt(SSSensorMessage.infoMemoryDataToString(parseResult.data));
                }
                catch(NumberFormatException e)
                {
                    SSLogger.writeException(e, "Unable to parse Uses from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryGraceUsesRemainingResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_GRACE_USES_REMAINING)
            {
                try
                {
                    mInfoMemoryGraceUsesRemaining = Integer.parseInt(SSSensorMessage.infoMemoryDataToString(parseResult.data));
                }
                catch(NumberFormatException e)
                {
                    SSLogger.writeException(e, "Unable to parse Grace Uses Remaining from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryUseStartDateTimeUtcResponse(SSSensorMessage.ParseResult parseResult)
    {
        DateFormat dateFormat = new SimpleDateFormat(SSSensorMessage.INFO_MEMORY_USE_START_DATE_TIME_UTC_FORMAT);

        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_USE_START_DATE_TIME_UTC)
            {
                try
                {
                    mInfoMemoryUseStartDateTimeUtc = dateFormat.parse(new String(parseResult.data).substring(2));
                }
                catch(ParseException e)
                {
                    SSLogger.writeException(e, "Unable to parse Use Start Date Time from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryPatientLastNameResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_PATIENT_LAST_NAME)
            {
                mInfoMemoryPatientLastName = new String(parseResult.data).substring(2);

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryPatientIdResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_PATIENT_ID)
            {
                mInfoMemoryPatientId = new String(parseResult.data).substring(2);

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryPatientBirthdayResponse(SSSensorMessage.ParseResult parseResult)
    {
        DateFormat dateFormat = new SimpleDateFormat(SSSensorMessage.INFO_MEMORY_PATIENT_BIRTHDAY_FORMAT);

        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_PATIENT_BIRTHDAY)
            {
                try
                {
                    mInfoMemoryPatientBirthday = dateFormat.parse(new String(parseResult.data).substring(2));
                }
                catch(ParseException e)
                {
                    SSLogger.writeException(e, "Unable to parse Patient Birthday from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryLastDbConnectionDateTimeUtcResponse(SSSensorMessage.ParseResult parseResult)
    {
        DateFormat dateFormat = new SimpleDateFormat(SSSensorMessage.INFO_MEMORY_CONNECTION_DATE_TIME_UTC_FORMAT);

        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_LAST_DB_CONNECTION_DATE_TIME_UTC)
            {
                try
                {
                    mInfoMemoryLastDbConnectionDateTimeUtc = dateFormat.parse(new String(parseResult.data).substring(2));
                }
                catch(ParseException e)
                {
                    SSLogger.writeException(e, "Unable to parse Last DB Connection Date Time from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryCustomerIdResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_CUSTOMER_ID)
            {
                mInfoMemoryCustomerId = SSSensorMessage.infoMemoryDataToString(parseResult.data);

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemorySyncFreqDaysResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_SYNC_FREQ_DAYS)
            {
                try
                {
                    mInfoMemorySyncFreqDays = Integer.parseInt(SSSensorMessage.infoMemoryDataToString(parseResult.data));
                }
                catch(NumberFormatException e)
                {
                    SSLogger.writeException(e, "Unable to parse Sync Freq Days from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryPaymentStatusResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_PAYMENT_STATUS)
            {
                mInfoMemoryPaymentStatus = SSSensorMessage.infoMemoryDataToString(parseResult.data);

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryPositivesResponse(SSSensorMessage.ParseResult parseResult)
    {
        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_POSITIVES)
            {
                try
                {
                    mInfoMemoryPositives = Integer.parseInt(SSSensorMessage.infoMemoryDataToString(parseResult.data));
                }
                catch(NumberFormatException e)
                {
                    SSLogger.writeException(e, "Unable to parse Positives from Sensor Info Memory.");
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkForInfoMemoryCurrentUseIsPositiveResponse(SSSensorMessage.ParseResult parseResult)
    {
        String currentUseIsPositive;

        if(parseResult.isValid)
        {
            if(parseResult.packetType == SSSensorMessage.PACKET_TYPE_ACK &&
                    parseResult.data.length > 0 &&
                    parseResult.data[0] == SSSensorMessage.COMMAND_GET_INFO_MEMORY &&
                    parseResult.data[1] == SSSensorMessage.INFO_MEMORY_ADDR_CURRENT_USE_IS_POSITIVE)
            {
                currentUseIsPositive = SSSensorMessage.infoMemoryDataToString(parseResult.data);
                if(SSSensorMessage.INFO_MEMORY_CURRENT_USE_IS_POSITIVE_TRUE == currentUseIsPositive)
                {
                    mInfoMemoryCurrentUseIsPositive = true;
                }
                else if(SSSensorMessage.INFO_MEMORY_CURRENT_USE_IS_POSITIVE_FALSE == currentUseIsPositive)
                {
                    mInfoMemoryCurrentUseIsPositive = false;
                }
                else
                {
                    SSLogger.writeData("Invalid value for Current Use Is Positive in Sensor Info Memory: " + currentUseIsPositive);
                }
            }

            return true;
        }

        return false;
    }
}

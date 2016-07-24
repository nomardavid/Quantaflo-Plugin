package com.semlerscientific.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class implements communication with the QuantaFlo Sensor.
 */
public class QuantaFloSensorPlugin extends CordovaPlugin
{
    private SSSensorRunnable mSensorRunnable;

    @Override
    protected void pluginInitialize()
    {
        SSLogger.writeData("Plugin started");

        mSensorRunnable = new SSSensorRunnable();
        ScheduledExecutorService receiveScheduler = Executors.newSingleThreadScheduledExecutor();
        receiveScheduler.scheduleAtFixedRate(mSensorRunnable, 0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
    {
        if(action.equals("setConnectionCallback"))
        {
            mSensorRunnable.setIsInitializedChangedCallback(new SSSensorRunnable.IIsInitializedChangedCallback()
            {
                @Override
                public void isInitializedChanged(boolean isInitialized)
                {
                    String connectionString;

                    if(isInitialized)
                    {
                        connectionString = "device connected!";
                    }
                    else
                    {
                        connectionString = "no devices connected";
                    }

                    PluginResult pr = new PluginResult(PluginResult.Status.OK, connectionString);
                    pr.setKeepCallback(true);
                    callbackContext.sendPluginResult(pr);
                }
            });

            return true;
        }
        else if(action.equals("getSensorProductId"))
        {
            callbackContext.success(mSensorRunnable.getProductId());
            return true;
        }
        else if(action.equals("getSensorSerialNumber"))
        {
            callbackContext.success(mSensorRunnable.getSerialNumber());
            return true;
        }
        else if(action.equals("getSensorProductVersion"))
        {
            callbackContext.success(mSensorRunnable.getProductVersion());
            return true;
        }
        else if(action.equals("getSensorUses"))
        {
            callbackContext.success(mSensorRunnable.getUses());
            return true;
        }
        else if(action.equals("getSensorPositives"))
        {
            callbackContext.success(mSensorRunnable.getPositives());
            return true;
        }
        else if(action.equals("getSensorGraceUsesRemaining"))
        {
            callbackContext.success(mSensorRunnable.getGraceUsesRemaining());
            return true;
        }
        else if(action.equals("getSensorLastSyncTimeUtc"))
        {
            callbackContext.success(mSensorRunnable.getLastSyncTimeUtc().toString());
            return true;
        }
        else if(action.equals("getSensorSyncFreqDays"))
        {
            callbackContext.success(mSensorRunnable.getSyncFreqDays());
            return true;
        }
        else if(action.equals("getSensorCustomerId"))
        {
            callbackContext.success(mSensorRunnable.getCustomerId());
            return true;
        }
        else if(action.equals("getSensorPaymentStatus"))
        {
            callbackContext.success(mSensorRunnable.getPaymentStatus());
            return true;
        }
        else if(action.equals("setPatientData"))
        {
            String patientLastName = args.getString(0);
            String patientId = args.getString(1);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date patientBirthday;
            try
            {
                patientBirthday = dateFormat.parse(args.getString(2));
            }
            catch(ParseException e)
            {
                //abcd handle this with an error response
                return true;
            }

            mSensorRunnable.setPatientData(patientLastName, patientId, patientBirthday);
            callbackContext.success();
            return true;
        }
        else if(action.equals("setPulseDataReceivedCallback"))
        {
            mSensorRunnable.setPulseDataReceivedCallback(new SSSensorRunnable.IPulseDataReceivedCallback()
            {
                @Override
                public void pulseDataReceived(int pulseData)
                {
                    PluginResult pr = new PluginResult(PluginResult.Status.OK, pulseData);
                    pr.setKeepCallback(true);
                    callbackContext.sendPluginResult(pr);
                }
            });

            return true;
        }
        else if(action.equals("startMeasurement"))
        {
            mSensorRunnable.setStartMeasurementResponseCallback(new SSSensorRunnable.IStartMeasurementResponseCallback()
            {
                @Override
                public void startMeasurementResponse(SSSensorRunnable.StartMeasurementResponse response)
                {
                    PluginResult pr = new PluginResult(PluginResult.Status.OK, response.toString());//abcd change to value()
                    switch(response.value())
                    {
                        case SSSensorRunnable.StartMeasurementResponse.STARTED:
                        case SSSensorRunnable.StartMeasurementResponse.NOT_ADJUSTING:
                            pr.setKeepCallback(true);
                            break;
                        case SSSensorRunnable.StartMeasurementResponse.ENDED_SUCCESSFULLY_FINAL:
                        case SSSensorRunnable.StartMeasurementResponse.CANCELED_FINAL:
                        case SSSensorRunnable.StartMeasurementResponse.AMBIENT_LIGHT_FINAL:
                        case SSSensorRunnable.StartMeasurementResponse.ERROR_FINAL:
                        case SSSensorRunnable.StartMeasurementResponse.NOT_CONNECTED_FINAL:
                        case SSSensorRunnable.StartMeasurementResponse.WRONG_STATE_FINAL:
                            break;
                        default:
                            throw new Error("StartMeasurementResponse value not known.");
                    }
                    callbackContext.sendPluginResult(pr);
                }
            });
            SSMeasurement.SideLimb sideLimb = new SSMeasurement.SideLimb(args.getInt(0));
            mSensorRunnable.sendStartMeasurement(sideLimb);

            return true;
        }
        else if(action.equals("cancelMeasurement"))
        {
            mSensorRunnable.sendCancelMeasurement();
            callbackContext.success();
            return true;
        }
        else if(action.equals("newTest"))
        {
            mSensorRunnable.newTest();
            callbackContext.success();
            return true;
        }
        else if(action.equals("getLeftResult"))
        {
            callbackContext.success(mSensorRunnable.getLeftResult().toString());
            return true;
        }
        else if(action.equals("getRightResult"))
        {
            callbackContext.success(mSensorRunnable.getRightResult().toString());
            return true;
        }
        else if(action.equals("syncNow"))
        {
            mSensorRunnable.onlineSync(new SSSensorRunnable.ISyncResponseCallback()
            {
                @Override
                public void syncResponse(int result, String resultDescription)
                {
                    callbackContext.success(resultDescription);
                }
            });

            return true;
        }
        else if(action.equals("getSyncSensorCode"))
        {
            callbackContext.success(mSensorRunnable.getOfflineSyncSensorCode());

            return true;
        }
        else if(action.equals("enterSyncUpdateCode"))
        {
            String updateCode = args.getString(0);

            mSensorRunnable.offlineSync(updateCode, new SSSensorRunnable.ISyncResponseCallback()
            {
                @Override
                public void syncResponse(int result, String resultDescription)
                {
                    callbackContext.success(resultDescription);
                }
            });

            return true;
        }
        //////below this line will be deleted eventually//////
        else if(action.equals("send"))
        {
            String message = args.getString(0);
            SSSensorService.send(message.getBytes());
            callbackContext.success();
            return true;
        }
        else if(action.equals("startReceiving"))
        {
            mSensorRunnable.setReceiveCallback(new SSSensorRunnable.IReceiveCallback()
            {
                @Override
                public void dataReceived(String s)
                {
                    PluginResult pr = new PluginResult(PluginResult.Status.OK, s);
                    pr.setKeepCallback(true);
                    callbackContext.sendPluginResult(pr);
                }
            });

            return true;
        }

        return false;
    }
}


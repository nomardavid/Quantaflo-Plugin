package com.semlerscientific.plugin;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Vector;
import java.util.regex.Pattern;

public class SSWebService
{
    public static final int RESULT_SUCCESS = 0;
    public static final String SENSOR_STATUS_CURRENT = "CURRENT";
    public static final String SENSOR_STATUS_RETURNED = "RETURNED";

    public static class LicensingDbCustomerListResponse
    {
        public static class Customer
        {
            public int customerId;
            public String customerName;
            public int defaultSyncFreq;
        }

        public int result;
        public String resultDescription;
        public Vector<Customer> customerList = new Vector<Customer>();
    }

    public static class LicensingDbSensorInstallRequest
    {
        public String serialNumber;
        public int customerId;
        public int syncFreq;
        public String installerEmail;
        public String externalIp;
        public String systemUuid;
    }

    public static class LicensingDbSensorInstallResponse
    {
        public int result;
        public String resultDescription;
    }

    public static class LicensingDbSensorSyncRequest
    {
        public String serialNumber;
        public int uses;
        public int positives;
        public String externalIp;
        public String systemUuid;
        public String teamViewerId;
        public String appVersion;
        public String site;
    }

    public static class LicensingDbSensorSyncResponse
    {
        public int result;
        public String resultDescription;
        public int customerId;
        public int syncFreq;
        public String sensorStatus;
    }

    private static String      METHOD_LICENSING_DB_CUSTOMER_LIST_GET = "LicensingDbCustomerListGet";
    private static String SOAP_ACTION_LICENSING_DB_CUSTOMER_LIST_GET = "https://hl7.quantaflo.com/QuantaFloDbSoapLicensingDbCustomerListGet";
    private static String      METHOD_LICENSING_DB_SENSOR_INSTALL = "LicensingDbSensorInstall";
    private static String SOAP_ACTION_LICENSING_DB_SENSOR_INSTALL = "https://hl7.quantaflo.com/QuantaFloDbSoapLicensingDbSensorInstall";
    private static String      METHOD_LICENSING_DB_SENSOR_SYNC = "LicensingDbSensorSync";
    private static String SOAP_ACTION_LICENSING_DB_SENSOR_SYNC = "https://hl7.quantaflo.com/QuantaFloDbSoapLicensingDbSensorSync";

    private static String NAMESPACE = "urn:QuantaFlo";
    private static String  SOAP_URL = "https://hl7.quantaflo.com/QuantaFloDbSoap";

    public static LicensingDbCustomerListResponse LicensingDbCustomerListGet()
    {
        LicensingDbCustomerListResponse retVal;

        SoapObject soapRequest = new SoapObject(NAMESPACE, METHOD_LICENSING_DB_CUSTOMER_LIST_GET);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soapRequest);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_URL);
        try
        {
            httpTransport.call(SOAP_ACTION_LICENSING_DB_CUSTOMER_LIST_GET, envelope);
            Vector<SoapPrimitive> soapResponse = (Vector<SoapPrimitive>)envelope.getResponse();
            retVal = new LicensingDbCustomerListResponse();
            retVal.result = Integer.parseInt((String)soapResponse.elementAt(0).getValue());
            retVal.resultDescription = (String)soapResponse.elementAt(1).getValue();
            if(RESULT_SUCCESS == retVal.result)
            {
                String[] customerListStringArray = ((String)soapResponse.elementAt(2).getValue()).split(Pattern.quote("|"));
                for(int i = 0; i < customerListStringArray.length; i += 3)
                {
                    LicensingDbCustomerListResponse.Customer customer = new LicensingDbCustomerListResponse.Customer();
                    customer.customerId = Integer.parseInt(customerListStringArray[i]);
                    customer.customerName = customerListStringArray[i + 1];
                    customer.defaultSyncFreq = Integer.parseInt(customerListStringArray[i + 2]);
                    retVal.customerList.add(customer);
                }
            }
        }
        catch(Exception e)
        {
            e.getMessage();
            retVal = null;
        }

        return retVal;
    }

    public static LicensingDbSensorInstallResponse LicensingDbSensorInstall(LicensingDbSensorInstallRequest request)
    {
        LicensingDbSensorInstallResponse retVal;

        SoapObject soapRequest = new SoapObject(NAMESPACE, METHOD_LICENSING_DB_SENSOR_INSTALL);
        soapRequest.addProperty("serialNumber", request.serialNumber);
        soapRequest.addProperty("customerId", request.customerId);
        soapRequest.addProperty("syncFreq", request.syncFreq);
        soapRequest.addProperty("installerEmail", request.installerEmail);
        soapRequest.addProperty("externalIp", request.externalIp);
        soapRequest.addProperty("systemUuid", request.systemUuid);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soapRequest);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_URL);
        try
        {
            httpTransport.call(SOAP_ACTION_LICENSING_DB_SENSOR_INSTALL, envelope);
            Vector<SoapPrimitive> soapResponse = (Vector<SoapPrimitive>)envelope.getResponse();
            retVal = new LicensingDbSensorInstallResponse();
            retVal.result = Integer.parseInt((String)soapResponse.elementAt(0).getValue());
            retVal.resultDescription = (String)soapResponse.elementAt(1).getValue();
        }
        catch(Exception e)
        {
            e.getMessage();
            retVal = null;
        }

        return retVal;
    }

    public static LicensingDbSensorSyncResponse LicensingDbSensorSync(LicensingDbSensorSyncRequest request)
    {
        LicensingDbSensorSyncResponse retVal;

        SoapObject soapRequest = new SoapObject(NAMESPACE, METHOD_LICENSING_DB_SENSOR_SYNC);
        soapRequest.addProperty("serialNumber", request.serialNumber);
        soapRequest.addProperty("uses", request.uses);
        soapRequest.addProperty("positives", request.positives);
        soapRequest.addProperty("externalIp", request.externalIp);
        soapRequest.addProperty("systemUuid", request.systemUuid);
        soapRequest.addProperty("teamViewerId", request.teamViewerId);
        soapRequest.addProperty("appVersion", request.appVersion);
        soapRequest.addProperty("site", request.site);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soapRequest);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_URL);
        try
        {
            httpTransport.call(SOAP_ACTION_LICENSING_DB_SENSOR_SYNC, envelope);
            Vector<SoapPrimitive> soapResponse = (Vector<SoapPrimitive>)envelope.getResponse();
            retVal = new LicensingDbSensorSyncResponse();
            retVal.result = Integer.parseInt((String)soapResponse.elementAt(0).getValue());
            retVal.resultDescription = (String)soapResponse.elementAt(1).getValue();
            if(RESULT_SUCCESS == retVal.result)
            {
                retVal.customerId = Integer.parseInt((String)soapResponse.elementAt(2).getValue());
                retVal.syncFreq = Integer.parseInt((String)soapResponse.elementAt(3).getValue());
                retVal.sensorStatus = (String)soapResponse.elementAt(4).getValue();
            }
        }
        catch(Exception e)
        {
            e.getMessage();
            retVal = null;
        }

        return retVal;
    }
}

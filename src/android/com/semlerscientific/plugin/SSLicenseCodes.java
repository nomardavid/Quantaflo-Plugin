package com.semlerscientific.plugin;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SSLicenseCodes
{
    public static final int POSITIVES_MAX = 65535;
    public static final int USES_MAX = 65535;

    private static final int SENSOR_CODE_LEN = 16;
    private static final int UPDATE_CODE_LEN = 10;

    private static final int SENSOR_CODE_DASH_INDEX_1 = 4;
    private static final int SENSOR_CODE_DASH_INDEX_2 = 9;
    private static final int SENSOR_CODE_DASH_INDEX_3 = 14;
    private static final int UPDATE_CODE_DASH_INDEX = 5;

    private static final int SENSOR_CODE_DATE_BITS = 10;
    private static final int USES_BITS = 16;
    private static final int POSITIVES_BITS = 16;
    private static final int UPDATE_CODE_DATE_BITS = 16;
    private static final int SERIAL_NUMBER_BITS = 21;
    private static final int CRC_BITS = 8;

    static public String SensorCodeEncode(int uses, int positives, int sensorSn)
    {
        BigInteger concat;
        String sensorCode;
        long tempLong;

        if ((uses >>> USES_BITS) > 0)
        {
            throw new Error("Uses (" + uses + ") exceeds maximum allowable size (" + USES_BITS + ")");
        }

        if ((positives >>> POSITIVES_BITS) > 0)
        {
            throw new Error("Positives (" + positives + ") exceeds maximum allowable size (" + POSITIVES_BITS + ")");
        }

        if ((sensorSn >>> SERIAL_NUMBER_BITS) > 0)
        {
            throw new Error("Sensor SN (" + sensorSn + ") exceeds maximum allowable size (" + SERIAL_NUMBER_BITS + ")");
        }

        tempLong = DateToDaysSince2015(new Date());
        concat = BigInteger.valueOf(tempLong).and((BigInteger.ONE.shiftLeft(SENSOR_CODE_DATE_BITS)).subtract(BigInteger.ONE));

        concat = concat.multiply(BigInteger.valueOf(2).pow(USES_BITS));
        concat = concat.add(BigInteger.valueOf(uses));

        concat = concat.multiply(BigInteger.valueOf(2).pow(POSITIVES_BITS));
        concat = concat.add(BigInteger.valueOf(positives));

        concat = concat.multiply(BigInteger.valueOf(2).pow(SERIAL_NUMBER_BITS));
        concat = concat.add(BigInteger.valueOf(sensorSn));

        int crc = Crc8.calc(concat, SENSOR_CODE_DATE_BITS + USES_BITS + POSITIVES_BITS + SERIAL_NUMBER_BITS);
        concat = concat.multiply(BigInteger.valueOf(2).pow(CRC_BITS));
        concat = concat.add(BigInteger.valueOf(crc));

        sensorCode = BigIntegerToBase24String(concat, SENSOR_CODE_LEN);
        sensorCode = sensorCode.substring(0, SENSOR_CODE_DASH_INDEX_1) + "-" +
                sensorCode.substring(SENSOR_CODE_DASH_INDEX_1, SENSOR_CODE_DASH_INDEX_2 - 1) + "-" +
                sensorCode.substring(SENSOR_CODE_DASH_INDEX_2 - 1, SENSOR_CODE_DASH_INDEX_3 - 2) + "-" +
                sensorCode.substring(SENSOR_CODE_DASH_INDEX_3 - 2);

        return sensorCode;
    }

    public static class SensorCodeComponents
    {
        int uses;
        int positives;
        int sensorSn;
    }

    static public SensorCodeComponents SensorCodeDecode(String sensorCode)
    {

        if (sensorCode.charAt(SENSOR_CODE_DASH_INDEX_1) != '-' ||
                sensorCode.charAt(SENSOR_CODE_DASH_INDEX_2) != '-' ||
                sensorCode.charAt(SENSOR_CODE_DASH_INDEX_3) != '-')
        {
            throw new Error("Sensor code missing dash.");
        }

        sensorCode = sensorCode.substring(0, SENSOR_CODE_DASH_INDEX_1) +
                sensorCode.substring(SENSOR_CODE_DASH_INDEX_1 + 1, SENSOR_CODE_DASH_INDEX_2) +
                sensorCode.substring(SENSOR_CODE_DASH_INDEX_2 + 1, SENSOR_CODE_DASH_INDEX_3) +
                sensorCode.substring(SENSOR_CODE_DASH_INDEX_3 + 1);
        BigInteger concat = Base24StringToBigInteger(sensorCode);

        int crcRead = concat.mod(BigInteger.valueOf(0x100)).intValue();
        SensorCodeComponents retVal = new SensorCodeComponents();
        retVal.sensorSn = ((concat.divide(BigInteger.valueOf(2).pow(CRC_BITS))).mod(BigInteger.ONE.shiftLeft(SERIAL_NUMBER_BITS))).intValue();
        retVal.positives = ((concat.divide(BigInteger.valueOf(2).pow(CRC_BITS + SERIAL_NUMBER_BITS))).mod(BigInteger.ONE.shiftLeft(POSITIVES_BITS))).intValue();
        retVal.uses = ((concat.divide(BigInteger.valueOf(2).pow(CRC_BITS + SERIAL_NUMBER_BITS + POSITIVES_BITS))).mod(BigInteger.ONE.shiftLeft(USES_BITS))).intValue();
        int date = ((concat.divide(BigInteger.valueOf(2).pow(CRC_BITS + SERIAL_NUMBER_BITS + POSITIVES_BITS + USES_BITS))).mod(BigInteger.ONE.shiftLeft(SENSOR_CODE_DATE_BITS))).intValue();

        if ((DateToDaysSince2015(new Date()) & ((1 << SENSOR_CODE_DATE_BITS) - 1)) != date)
        {
            throw new Error("Sensor Code Invalid Date");
        }

        int crcCalc = Crc8.calc(concat.divide(BigInteger.valueOf(2).pow(CRC_BITS)), SENSOR_CODE_DATE_BITS + USES_BITS + POSITIVES_BITS + SERIAL_NUMBER_BITS);

        if (crcRead != crcCalc)
        {
            throw new Error("Sensor Code Invalid Checksum");
        }

        return retVal;
    }

    static public String UpdateCodeEncode(Date nextUpdateDate, int sensorSn)
    {
        BigInteger concat;
        int days = DateToDaysSince2015(nextUpdateDate);
        String updateCode;

        if ((days >> UPDATE_CODE_DATE_BITS) > 0)
        {
            throw new Error("Date (" + nextUpdateDate.toString() + ") is too far in the future.");
        }

        if ((sensorSn >> SERIAL_NUMBER_BITS) > 0)
        {
            throw new Error("Sensor SN (" + sensorSn + ") exceeds maximum allowable size (" + SERIAL_NUMBER_BITS + ")");
        }

        concat = BigInteger.valueOf(days);
        concat = concat.shiftLeft(SERIAL_NUMBER_BITS);
        concat = concat.add(BigInteger.valueOf(sensorSn));

        int crc = Crc8.calc(concat, UPDATE_CODE_DATE_BITS + SERIAL_NUMBER_BITS);
        concat = concat.shiftLeft(CRC_BITS);
        concat = concat.add(BigInteger.valueOf(crc));

        updateCode = BigIntegerToBase24String(concat, UPDATE_CODE_LEN);
        updateCode = updateCode.substring(0, UPDATE_CODE_DASH_INDEX) + "-" + updateCode.substring(UPDATE_CODE_DASH_INDEX);

        return updateCode;
    }

    public static class UpdateCodeComponents
    {
        Date nextUpdateDate;
        int sensorSn;
    }

    public static UpdateCodeComponents UpdateCodeDecode(String updateCode)
    {
        if (updateCode.charAt(UPDATE_CODE_DASH_INDEX) != '-')
        {
            throw new Error("Update code missing dash.");
        }

        updateCode = updateCode.substring(0, UPDATE_CODE_DASH_INDEX) + updateCode.substring(UPDATE_CODE_DASH_INDEX + 1);

        BigInteger concat = Base24StringToBigInteger(updateCode);

        UpdateCodeComponents retVal = new UpdateCodeComponents();
        int crcRead = concat.mod(BigInteger.valueOf(0x100)).intValue();
        retVal.sensorSn = ((concat.shiftRight(CRC_BITS)).and((BigInteger.ONE.shiftLeft(SERIAL_NUMBER_BITS)).subtract(BigInteger.ONE))).intValue();
        retVal.nextUpdateDate = DaysSince2015ToDate(((concat.shiftRight(CRC_BITS + SERIAL_NUMBER_BITS)).and((BigInteger.ONE.shiftLeft(UPDATE_CODE_DATE_BITS)).subtract(BigInteger.ONE))).intValue());
        int crcCalc = Crc8.calc(concat.shiftRight(CRC_BITS), UPDATE_CODE_DATE_BITS + SERIAL_NUMBER_BITS);

        if (crcRead != crcCalc)
        {
            throw new Error("Sensor Code Invalid Checksum");
        }

        return retVal;
    }

    static private String BigIntegerToBase24String(BigInteger val, int minLength)
    {
        String retVal = "";

        do
        {
            byte digit = (byte)(val.mod(BigInteger.valueOf(24))).intValue();
            val = val.divide(BigInteger.valueOf(24));

            switch (digit)
            {
                case 0: retVal += '6'; break;
                case 1: retVal += 'E'; break;
                case 2: retVal += 'L'; break;
                case 3: retVal += 'R'; break;
                case 4: retVal += '0'; break;
                case 5: retVal += 'Y'; break;
                case 6: retVal += '2'; break;
                case 7: retVal += '3'; break;
                case 8: retVal += 'F'; break;
                case 9: retVal += 'H'; break;
                case 10: retVal += '7'; break;
                case 11: retVal += '8'; break;
                case 12: retVal += 'Q'; break;
                case 13: retVal += '9'; break;
                case 14: retVal += 'S'; break;
                case 15: retVal += 'U'; break;
                case 16: retVal += '1'; break;
                case 17: retVal += 'X'; break;
                case 18: retVal += '4'; break;
                case 19: retVal += 'W'; break;
                case 20: retVal += 'P'; break;
                case 21: retVal += '5'; break;
                case 22: retVal += 'M'; break;
                case 23: retVal += 'A'; break;
                default:
                    throw new Error("Invalid digit in UInt64ToBase24String: " + digit);
            }
        } while (val.compareTo(BigInteger.ONE ) >= 0 || retVal.length() < minLength);

        return retVal;
    }

    static private BigInteger Base24StringToBigInteger(String val)
    {
        BigInteger retVal = BigInteger.ZERO;

        for (int i = 0; i < val.length(); i++)
        {
            char digit = val.toUpperCase().charAt(val.length() - i - 1);

            retVal = retVal.multiply(BigInteger.valueOf(24));

            switch (digit)
            {
                case '6': retVal = retVal.add(BigInteger.valueOf(0)); break;
                case 'E': retVal = retVal.add(BigInteger.valueOf(1)); break;
                case 'L': retVal = retVal.add(BigInteger.valueOf(2)); break;
                case 'R': retVal = retVal.add(BigInteger.valueOf(3)); break;
                case '0': retVal = retVal.add(BigInteger.valueOf(4)); break;//0 or O act the same
                case 'O': retVal = retVal.add(BigInteger.valueOf(4)); break;//0 or O act the same
                case 'Y': retVal = retVal.add(BigInteger.valueOf(5)); break;
                case '2': retVal = retVal.add(BigInteger.valueOf(6)); break;
                case '3': retVal = retVal.add(BigInteger.valueOf(7)); break;
                case 'F': retVal = retVal.add(BigInteger.valueOf(8)); break;
                case 'H': retVal = retVal.add(BigInteger.valueOf(9)); break;
                case '7': retVal = retVal.add(BigInteger.valueOf(10)); break;
                case '8': retVal = retVal.add(BigInteger.valueOf(11)); break;
                case 'Q': retVal = retVal.add(BigInteger.valueOf(12)); break;
                case '9': retVal = retVal.add(BigInteger.valueOf(13)); break;
                case 'S': retVal = retVal.add(BigInteger.valueOf(14)); break;
                case 'U': retVal = retVal.add(BigInteger.valueOf(15)); break;
                case '1': retVal = retVal.add(BigInteger.valueOf(16)); break;//1 or I act the same
                case 'I': retVal = retVal.add(BigInteger.valueOf(16)); break;//1 or I act the same
                case 'X': retVal = retVal.add(BigInteger.valueOf(17)); break;
                case '4': retVal = retVal.add(BigInteger.valueOf(18)); break;
                case 'W': retVal = retVal.add(BigInteger.valueOf(19)); break;
                case 'P': retVal = retVal.add(BigInteger.valueOf(20)); break;
                case '5': retVal = retVal.add(BigInteger.valueOf(21)); break;
                case 'M': retVal = retVal.add(BigInteger.valueOf(22)); break;
                case 'A': retVal = retVal.add(BigInteger.valueOf(23)); break;
                default:
                    throw new Error("Invalid digit in Base24StringToUInt64: " + digit);
            }
        }

        return retVal;
    }

    static private int DateToDaysSince2015(Date date)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2015,0,1,0,0,0);
        Date startDate = cal.getTime();

        if (date.before(startDate))
        {
            throw new Error("DateToDaysSince2015: date cannot be before 2015: " + date.toString());
        }

        return (int)((date.getTime() - startDate.getTime()) / 1000 / 60 / 60 / 24);
    }

    static private Date DaysSince2015ToDate(int days)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2015,0,1,0,0,0);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    private static class Crc8
    {
        static byte[] crc8Table = new byte[256];

        static
        {
            // x8 + x7 + x6 + x4 + x2 + 1
            final int poly = 0xd5;

            for(int i = 0; i < 256; ++i)
            {
                int temp = i;
                for(int j = 0; j < 8; ++j)
                {
                    if((temp & 0x80) != 0)
                    {
                        temp = (temp << 1) ^ poly;
                    }
                    else
                    {
                        temp <<= 1;
                    }
                }
                crc8Table[i] = (byte)temp;
            }
        }

        static public int calc(byte[] bytes)
        {
            byte crc = 0;

            if(bytes != null && bytes.length > 0)
            {
                for(byte b : bytes)
                {
                    int index = (crc ^ b) & 0xFF;// & with 0xFF converts the bit pattern from a signed byte to an unsigned int
                    crc = crc8Table[index];
                }
            }

            return crc & 0xFF;// & with 0xFF converts the bit pattern from a signed byte to an unsigned int;
        }

        static public int calc(BigInteger val, int bits)
        {
            int crcBytesLen = (int)Math.ceil(bits / 8.0);
            byte[] crcBytes = new byte[crcBytesLen];
            for (int i = 0; i < crcBytesLen; i++)
            {
                crcBytes[i] = (byte)((val.divide(BigInteger.valueOf(2).pow(i * 8))).mod(BigInteger.valueOf(0x100))).intValue();
            }

            return calc(crcBytes);
        }
    }
}

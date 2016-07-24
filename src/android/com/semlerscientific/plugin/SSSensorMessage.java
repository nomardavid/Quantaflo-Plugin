package com.semlerscientific.plugin;

import java.util.ArrayList;

public class SSSensorMessage
{
    public static final byte COMMAND_GET_PRODUCT_ID = 0x00;
    public static final byte COMMAND_GET_PRODUCT_VERSION = 0X01;
    public static final byte COMMAND_PING = 0x06;
    public static final byte COMMAND_ENABLE_CHANNEL = 0x12;
    public static final byte COMMAND_DISABLE_CHANNEL = 0x13;
    public static final byte COMMAND_INTENSITY_ADJUSTING = 0x14;
    public static final byte COMMAND_INTENSITY_NOT_ADJUSTING = 0x15;
    public static final byte COMMAND_GET_SERIAL_NUMBER = 0x18;
    public static final byte COMMAND_GET_INFO_MEMORY = 0x27;
    public static final byte COMMAND_SET_INFO_MEMORY = 0x28;

    public static final byte INFO_MEMORY_ADDR_USES = 8;
    public static final byte INFO_MEMORY_ADDR_GRACE_USES_REMAINING = 9;
    public static final byte INFO_MEMORY_ADDR_USE_START_DATE_TIME_UTC = 10;
    public static final byte INFO_MEMORY_ADDR_PATIENT_LAST_NAME = 11;
    public static final byte INFO_MEMORY_ADDR_PATIENT_ID = 12;
    public static final byte INFO_MEMORY_ADDR_PATIENT_BIRTHDAY = 13;
    public static final byte INFO_MEMORY_ADDR_LAST_DB_CONNECTION_DATE_TIME_UTC = 14;
    public static final byte INFO_MEMORY_ADDR_CUSTOMER_ID = 15;
    public static final byte INFO_MEMORY_ADDR_SYNC_FREQ_DAYS = 16;
    public static final byte INFO_MEMORY_ADDR_PAYMENT_STATUS = 17;
    public static final byte INFO_MEMORY_ADDR_POSITIVES = 18;
    public static final byte INFO_MEMORY_ADDR_CURRENT_USE_IS_POSITIVE = 19;

    public static final String INFO_MEMORY_PATIENT_BIRTHDAY_FORMAT = "MM/dd/yyyy";//matches toShortDateString() from c#
    public static final String INFO_MEMORY_CONNECTION_DATE_TIME_UTC_FORMAT = "MM/dd/yyyy HH:mm";
    public static final String INFO_MEMORY_USE_START_DATE_TIME_UTC_FORMAT = "MM/dd/yyyy HH:mm";
    public static final String INFO_MEMORY_CURRENT_USE_IS_POSITIVE_TRUE = "True";//to match bool toString() from c#
    public static final String INFO_MEMORY_CURRENT_USE_IS_POSITIVE_FALSE = "False";//to match bool toString() from c#

    public static final byte PACKET_TYPE_DATA = 0;
    public static final byte PACKET_TYPE_COMMAND = 1;
    public static final byte PACKET_TYPE_ERROR = 2;
    public static final byte PACKET_TYPE_MESSAGE = 3;
    public static final byte PACKET_TYPE_ACK = 4;
    public static final byte PACKET_TYPE_NACK = 5;

    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    private static final byte ESC = 0x1b;

    private enum eReceiveState
    {
        PACKET_NOT_STARTED,
        STX_FOUND,
        PACKET_COMPLETE,
        WAIT_FOR_NEXT_BYTE
    }

    private static ArrayList<Byte> mReceivedData = new ArrayList<Byte>();
    private static eReceiveState mReceivePreviousState = eReceiveState.PACKET_NOT_STARTED;
    private static eReceiveState mReceiveState = eReceiveState.PACKET_NOT_STARTED;

    private static boolean needsEscape(byte data)
    {
        if(data == ESC ||
                data == ETX ||
                data == STX)
        {
            return true;
        }

        return false;
    }

    public static byte[] generate(byte command)
    {
        return generate(command, new byte[0]);
    }

    public static byte[] generate(byte command, byte[] data)
    {
        byte channel = 1;
        byte checksum = 0;
        ArrayList<Byte> retVal = new ArrayList<Byte>();

        //add stx
        retVal.add(STX);

        //add cmd/length
        byte coo = (byte)(PACKET_TYPE_COMMAND << 5 | (data.length + 2));
        if(needsEscape(coo))
        {
            retVal.add(ESC);
        }
        retVal.add(coo);

        //add channel
        if(needsEscape(channel))
        {
            retVal.add(ESC);
        }
        retVal.add(channel);
        checksum += channel;

        //add command
        if(needsEscape(command))
        {
            retVal.add(ESC);
        }
        retVal.add(command);
        checksum += command;

        //add data
        for(byte b : data)
        {
            if(needsEscape(b))
            {
                retVal.add(ESC);
            }
            retVal.add(b);
            checksum += b;
        }

        //add checksum
        if(needsEscape(checksum))
        {
            retVal.add(ESC);
        }
        retVal.add(checksum);

        //add etx
        retVal.add(ETX);

        return toPrimitive(retVal);
    }

    public static byte[] generateSetInfoMemory(byte infoMemoryAddr, String value)
    {
        byte[] data = new byte[17];

        data[0] = infoMemoryAddr;

        for(int i = 0; i < 16; i++)
        {
            if(i < value.length())
            {
                data[i] = (byte)value.charAt(i);
            }
            else
            {
                data[i] = (byte)0xFF;
            }
        }

        return generate(COMMAND_SET_INFO_MEMORY, data);
    }

    public static class ParseResult
    {
        boolean isValid = false;

        byte packetType;
        byte[] data;
    }

    public static ParseResult parse(byte receivedByte)
    {
        ParseResult retVal = new ParseResult();

        if(receivedByte == ESC && mReceiveState != eReceiveState.WAIT_FOR_NEXT_BYTE)
        {
            mReceivePreviousState = mReceiveState;
            mReceiveState = eReceiveState.WAIT_FOR_NEXT_BYTE;
        }
        else
        {
            if(mReceiveState == eReceiveState.WAIT_FOR_NEXT_BYTE)
            {
                if(receivedByte != ETX &&
                        receivedByte != STX &&
                        receivedByte != ESC)
                {
                    mReceivedData.add(ESC);
                    //todo: abcd log "found invalid escape value"
                }
                mReceivedData.add(receivedByte);

                mReceiveState = mReceivePreviousState;
            }
            else if(mReceiveState == eReceiveState.PACKET_NOT_STARTED)
            {
                if(receivedByte == STX)
                {
                    mReceiveState = eReceiveState.STX_FOUND;
                    mReceivedData.clear();
                }
                else
                {
                    //todo: abcd Logger.writeData(string.Format("Received extra data before the STX packet: 0x{0:X2}", data));
                }
            }
            else if(mReceiveState == eReceiveState.STX_FOUND)
            {
                if(receivedByte == ETX)
                {
                    //complete message received
                    retVal = parseCompleteMessage(toPrimitive(mReceivedData));

                    //reset state to wait for next message
                    mReceiveState = eReceiveState.PACKET_NOT_STARTED;
                }
                else
                {
                    mReceivedData.add(receivedByte);
                }
            }
            else
            {
                throw new RuntimeException("SSSensorMessage.parse: state unknown");
            }
        }

        return retVal;
    }

    private static ParseResult parseCompleteMessage(byte[] message)
    {
        byte checksumCalc;
        int length;
        ParseResult retVal = new ParseResult();
        byte temp = 0;

        temp = message[0];

        retVal.packetType = (byte)((temp & 0xE0) >> 5);
        length = temp & 0x1F;

        if(message.length != length + 2)
        {
            //todo: abcd log invalid message
            return retVal;
        }

        retVal.data = new byte[length];
        for(int i = 0; i < length; i++)
        {
            retVal.data[i] = message[i + 1];
        }

        checksumCalc = 0;
        for(byte c : retVal.data)
        {
            checksumCalc += c;
        }

        if(message[length + 1] != checksumCalc)
        {
            //todo: abcd log invalid message
            return retVal;
        }

        retVal.isValid = true;

        return retVal;
    }

    private static byte[] toPrimitive(ArrayList<Byte> data)
    {
        byte[] retVal = new byte[data.size()];

        for(int i = 0; i < data.size(); i++)
        {
            retVal[i] = data.get(i);
        }

        return retVal;
    }

    public static String infoMemoryDataToString(byte[] data)
    {
        int end;
        int start = 2;

        for(end = data.length - 1; end >= start; end--)
        {
            if(data[end] != (byte)0xFF)
            {
                break;
            }
        }

        return new String(data).substring(start, end + 1);
    }
}

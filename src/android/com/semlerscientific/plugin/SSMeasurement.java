package com.semlerscientific.plugin;

import java.util.ArrayList;
import java.util.Date;

public class SSMeasurement
{
    public static final int SS_FREQUENCY_HZ = 50;
    public static final int SS_DURATION_SEC = 15;
    public static final int SS_DURATION_SAMPLES = SS_FREQUENCY_HZ * SS_DURATION_SEC;

    public static class SideLimb
    {
        public static final int LEFT_FOOT = 0;
        public static final int LEFT_HAND = 1;
        public static final int RIGHT_FOOT = 2;
        public static final int RIGHT_HAND = 3;

        private final int mValue;

        SideLimb(int value)
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
                case LEFT_FOOT: return "LEFT_FOOT";
                case LEFT_HAND: return "LEFT_HAND";
                case RIGHT_FOOT: return "RIGHT_FOOT";
                case RIGHT_HAND: return "RIGHT_HAND";
            }
            return "UNKNOWN";
        }
    }

    private SSA3NDKTerms mA3NDKTerms = null;
    private SideLimb mSideLimb;
    private ArrayList<Integer> mData = new ArrayList<Integer>();
    private final Date mStartDate;

    SSMeasurement(SideLimb sideLimb)
    {
        mSideLimb = sideLimb;
        mStartDate = new Date();
    }

    public void addDataPoint(int dataPoint)
    {
        mA3NDKTerms = null;
        mData.add(dataPoint);
    }

    public ArrayList<Integer> getData()
    {
        return mData;
    }

    public SideLimb getLimbSide()
    {
        return mSideLimb;
    }

    public Date getStartDate()
    {
        return mStartDate;
    }

    public SSA3NDKTerms getA3NDKTerms()
    {
        if(null == mA3NDKTerms)
        {
            SSA3NDK a3 = new SSA3NDK();
            mA3NDKTerms = a3.calcTerms(getDataDoubleArray());
        }

        return mA3NDKTerms;
    }

    private double[] getDataDoubleArray()
    {
        double[] retVal = new double[mData.size()];

        for(int i = 0;i < mData.size();i++)
        {
            retVal[i] = mData.get(i);
        }

        return retVal;
    }
}

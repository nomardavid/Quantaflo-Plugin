package com.semlerscientific.plugin;

import java.util.ArrayList;
import java.util.Date;

public class SSTest
{
    private ArrayList<SSMeasurement> mMeasurements;
    private Date mPatientBirthday;
    private String mPatientId;
    private String mPatientLastName;
    private Double mLeftResult;
    private Double mRightResult;

    SSTest()
    {
        mMeasurements = new ArrayList<SSMeasurement>();
        mLeftResult = null;
        mRightResult = null;
    }

    public void setPatientBirthday(Date patientBirthday)
    {
        mPatientBirthday = patientBirthday;
    }

    public Date getPatientBirthday()
    {
        return mPatientBirthday;
    }

    public void setPatientId(String patientId)
    {
        mPatientId = patientId;
    }

    public String getPatientId()
    {
        return mPatientId;
    }

    public void setPatientLastName(String patientLastName)
    {
        mPatientLastName = patientLastName;
    }

    public String getPatientLastName()
    {
        return mPatientLastName;
    }

    public void addMeasurement(SSMeasurement measurement)
    {
        //clear out cached results
        if(measurement.getLimbSide().value() == SSMeasurement.SideLimb.LEFT_FOOT)//abcd it would be better if i didn't have to use value() (override equals function?)
        {
            mLeftResult = null;
        }
        else if(measurement.getLimbSide().value() == SSMeasurement.SideLimb.RIGHT_FOOT)//abcd it would be better if i didn't have to use value() (override equals function?)
        {
            mRightResult = null;
        }
        else
        {
            mLeftResult = null;
            mRightResult = null;
        }

        //cause the a3 terms to be computed now
        measurement.getA3NDKTerms();

        mMeasurements.add(measurement);
    }

    public SSMeasurement getBestHand(SSMeasurement leftHand, SSMeasurement rightHand)
    {
        //replace a missing hand with the other one
        if(null == leftHand)
        {
            leftHand = rightHand;
        }
        if (null == rightHand)
        {
            rightHand = leftHand;
        }

        //use best hand
        if (leftHand.getA3NDKTerms().mat_snr1 < rightHand.getA3NDKTerms().mat_snr1)
        {
            return rightHand;
        }
        else//if (rightHand.getA3NDKTerms().mat_snr1 <= leftHand.getA3NDKTerms().mat_snr1)
        {
            return leftHand;
        }
    }

    public double getLeftResult()
    {
        if (null == mLeftResult)
        {
            SSMeasurement leftHand = getLastByLimbSide(new SSMeasurement.SideLimb((SSMeasurement.SideLimb.LEFT_HAND)));
            SSMeasurement rightHand = getLastByLimbSide(new SSMeasurement.SideLimb((SSMeasurement.SideLimb.RIGHT_HAND)));
            SSMeasurement foot = getLastByLimbSide(new SSMeasurement.SideLimb((SSMeasurement.SideLimb.LEFT_FOOT)));

            //make sure there are enough data
            if(null == foot || (null == leftHand && null == rightHand))
            {
                mLeftResult = Double.NaN;
            }
            else
            {

                SSA3NDK a3 = new SSA3NDK();
                SSA3NDKEtaOutput eta = a3.calcEta(foot.getA3NDKTerms(), getBestHand(leftHand, rightHand).getA3NDKTerms());
                mLeftResult = a3.calcResultVascular(eta);
            }
        }

        return mLeftResult;
    }

    public double getRightResult()
    {
        if (null == mRightResult)
        {
            SSMeasurement leftHand = getLastByLimbSide(new SSMeasurement.SideLimb((SSMeasurement.SideLimb.LEFT_HAND)));
            SSMeasurement rightHand = getLastByLimbSide(new SSMeasurement.SideLimb((SSMeasurement.SideLimb.RIGHT_HAND)));
            SSMeasurement foot = getLastByLimbSide(new SSMeasurement.SideLimb((SSMeasurement.SideLimb.RIGHT_FOOT)));

            //make sure there are enough data
            if(null == foot || (null == leftHand && null == rightHand))
            {
                mRightResult = Double.NaN;
            }
            else
            {

                SSA3NDK a3 = new SSA3NDK();
                SSA3NDKEtaOutput eta = a3.calcEta(foot.getA3NDKTerms(), getBestHand(leftHand, rightHand).getA3NDKTerms());
                mRightResult = a3.calcResultVascular(eta);
            }
        }

        return mRightResult;
    }

    private SSMeasurement getLastByLimbSide(SSMeasurement.SideLimb sideLimb)
    {
        for(int i = mMeasurements.size() - 1;i >= 0;i--)//iterate backwards
        {
            if(mMeasurements.get(i).getLimbSide().value() == sideLimb.value() &&//abcd it would be better if i didn't have to use value()
                    mMeasurements.get(i).getA3NDKTerms().isValid())
            {
                return mMeasurements.get(i);
            }
        }

        return null;
    }
}

package com.semlerscientific.plugin;

public class SSA3NDK
{
    static
    {
        System.loadLibrary("SSA3NDK");
    }

    public native SSA3NDKTerms calcTerms(double[] data);
    public native SSA3NDKEtaOutput calcEta(SSA3NDKTerms foot, SSA3NDKTerms hand);
    public native double calcResultVascular(SSA3NDKEtaOutput etaOutput);
    public native double calcResultPrimaryCare(SSA3NDKEtaOutput etaOutput);
}


package com.semlerscientific.plugin;

public class SSA3NDKTerms
{
    double hf_power;
    double dyast_duration;
    double dyast_peak_norm;
    double syst_duration_norm;
    double mat_harmonic_slope;
    double mat_snr1;
    double mat_snr4;
    double mat_snr20_25;

    boolean isValid()
    {
        return !(Double.isNaN(hf_power) ||
                Double.isNaN(dyast_duration) ||
                Double.isNaN(dyast_peak_norm) ||
                Double.isNaN(syst_duration_norm) ||
                Double.isNaN(mat_harmonic_slope) ||
                Double.isNaN(mat_snr1) ||
                Double.isNaN(mat_snr4) ||
                Double.isNaN(mat_snr20_25));
    }
}

package com.sound_collage.models;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.BandPass;

public class SoundEqualizer implements AudioProcessor {
    private BandPass lowFilter;
    private BandPass midFilter;
    private BandPass highFilter;
    private final int sampleRate;
    private final Object lock = new Object();

    // Frequency ranges for the low, mid, and high bands
    private static final float LOW_FREQ = 200.0f;
    private static final float MID_FREQ = 1000.0f;
    private static final float HIGH_FREQ = 5000.0f;

    public SoundEqualizer(int sampleRate) {
        this.sampleRate = sampleRate;
        initializeFilters();
    }

    private void initializeFilters() {
        // Initialize filters with default values for bandwidth
        lowFilter = new BandPass(LOW_FREQ, 400, sampleRate);
        midFilter = new BandPass(MID_FREQ, 1000, sampleRate);
        highFilter = new BandPass(HIGH_FREQ, 2000, sampleRate);
    }




    /*
    Low Frequency Slider: Adjusting this slider will impact the bass levels in the audio. Moving it towards the maximum will increase the bass, and moving it towards the minimum will decrease it.
    Mid Frequency Slider: This slider controls the mid-range frequencies, which usually affect the body of the sound, like the lower harmonics of vocals, guitars, and pianos.
    High Frequency Slider: Adjusting the high frequency will affect the treble or the higher end of the sound spectrum. This includes higher harmonics and sounds like cymbals, high hats, and higher notes on guitars.
     */


    public void updateSettings(float low, float mid, float high) {
        synchronized (lock) {
            // Reinitialize filters with new bandwidth values
            lowFilter = new BandPass(LOW_FREQ, low * 500, sampleRate); // Example scaling for low frequency
            midFilter = new BandPass(MID_FREQ, mid * 1000, sampleRate); // Example scaling for mid frequency
            highFilter = new BandPass(HIGH_FREQ, high * 2000, sampleRate); // Example scaling for high frequency
        }
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        synchronized (lock) {
            // Apply the filters to the audio event
            if (lowFilter != null) lowFilter.process(audioEvent);
            if (midFilter != null) midFilter.process(audioEvent);
            if (highFilter != null) highFilter.process(audioEvent);
        }
        return true;
    }

    @Override
    public void processingFinished() {
        // Handle any cleanup if necessary after processing is finished
    }
}

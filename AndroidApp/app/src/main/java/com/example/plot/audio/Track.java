package com.example.plot.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import java.util.jar.Attributes;

public class Track extends AudioTrack{
    private AudioTrack track;
    public int sampleRate;
    public int channel;
    public int encoding;
    public int bufferSize;

    public Track(int sampleRate, int channel, int encoding, int bufferSize){
        super(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.encoding = encoding;
        this.bufferSize = bufferSize;
    }

    void start(){
        play();
    }


}

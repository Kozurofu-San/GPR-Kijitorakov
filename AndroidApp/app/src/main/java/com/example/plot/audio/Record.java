package com.example.plot.audio;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.plot.MainActivity;

public class Record extends AudioRecord {
    public int sampleRate;
    public int channel;
    public int encoding;
    private int bufferSize;

    public Record(Context context, int sampleRate, int channel, int encoding, int bufferSize){
        super(MediaRecorder.AudioSource.MIC,
                sampleRate, channel, encoding, bufferSize);
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.encoding = encoding;
        this.bufferSize = bufferSize;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
            Log.e("AudioRecord","Required RECORD_AUDIO permission");
    }

    @SuppressWarnings("unused")
    public void start(){
        startRecording();
    }


    @SuppressWarnings("unused")
    public int getBufferSize(){
        return bufferSize;
    }



}


//    int sampleRate = 48000;
//    int channel = AudioFormat.CHANNEL_IN_MONO;
//    int encoding = AudioFormat.ENCODING_PCM_16BIT;

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED)
//            requestPermission();


//    private void requestPermission(){
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)){
//            AlertDialog.Builder request = new AlertDialog.Builder(this);
//            request.setTitle("Permission needed");
//            request.setMessage("Audio record is needed");
//            request.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    ActivityCompat.requestPermissions(MainActivity.this,
//                            new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_AUDIO_RECORD);
//                }
//            });
//            request.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            });
//            request.create();
//            request.show();
//        }
//        else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_AUDIO_RECORD);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_AUDIO_RECORD)
//            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
//                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();
//            else
//                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
//    }

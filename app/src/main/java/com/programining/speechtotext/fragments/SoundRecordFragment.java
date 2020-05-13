package com.programining.speechtotext.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.programining.speechtotext.R;

import java.io.IOException;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class SoundRecordFragment extends Fragment {

    private static final String LOG_TAG = "SoundRecordFragment";
    private static final int STORAGE_PERMISSION_REQUEST = 100;

    private FloatingActionButton fabRecord;
    private FloatingActionButton fabStopRecord;
    private FloatingActionButton fabPlay;
    private FloatingActionButton fabStopPlay;

    private MediaRecorder mMediaRecorder;
    private String mFileName;
    private MediaPlayer mPlayer;
    private boolean isPlaying;
    private boolean isRecording;
    private Context mContext;
    //private MediatorInterface mMediatorCallback;

    public SoundRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        // mMediatorCallback = (MediatorInterface) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_sound_record, container, false);

        fabRecord = parentView.findViewById(R.id.fab_record);
        fabStopRecord = parentView.findViewById(R.id.fab_stop_recording);
        fabPlay = parentView.findViewById(R.id.fab_play);
        fabStopPlay = parentView.findViewById(R.id.fab_stop_playing);


        shouldEnableFloatingButton(fabStopRecord, false);
        shouldEnableFloatingButton(fabPlay, false);
        shouldEnableFloatingButton(fabStopPlay, false);

        if (mFileName != null) {
            shouldEnableFloatingButton(fabPlay, true);
        }

        fabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPermissionsGranted()) {
                    startRecording();
                } else {
                    showRunTimePermission();
                }
            }
        });
        fabStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });
        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaying();
            }
        });
        fabStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
            }
        });


        return parentView;
    }

    private void shouldEnableFloatingButton(FloatingActionButton fab, boolean shouldEnable) {
        String color;
        if (shouldEnable) {
            if (fab.getId() == fabRecord.getId() || fab.getId() == fabPlay.getId()) {
                color = "#66BB6A";
            } else {
                color = "#FF0000";
            }
        } else {
            color = "#c7c7c7";
        }
        fab.setEnabled(shouldEnable);
        fab.setColorFilter(Color.parseColor(color), android.graphics.PorterDuff.Mode.SRC_IN);//ContextCompat.getColor(mContext, R.color.COLOR_YOUR_COLOR)
    }


    private void startRecording() {

        setFileName();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(mFileName);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed " + e.toString());
        }
        mMediaRecorder.start();

        isRecording = true;
        shouldEnableFloatingButton(fabRecord, false);
        shouldEnableFloatingButton(fabStopRecord, true);
        shouldEnableFloatingButton(fabPlay, false);
        shouldEnableFloatingButton(fabStopPlay, false);
    }

    private void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;

        isRecording = false;
        shouldEnableFloatingButton(fabRecord, true);
        shouldEnableFloatingButton(fabStopRecord, false);
        shouldEnableFloatingButton(fabPlay, true);
        shouldEnableFloatingButton(fabStopPlay, false);


    }

    private void startPlaying() {

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        isPlaying = true;
        shouldEnableFloatingButton(fabRecord, false);
        shouldEnableFloatingButton(fabStopRecord, false);
        shouldEnableFloatingButton(fabPlay, false);
        shouldEnableFloatingButton(fabStopPlay, true);
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
        isPlaying = false;

        shouldEnableFloatingButton(fabRecord, true);
        shouldEnableFloatingButton(fabStopRecord, false);
        shouldEnableFloatingButton(fabPlay, true);
        shouldEnableFloatingButton(fabStopPlay, false);
    }

    private void setFileName() {
        String path = mContext.getExternalCacheDir().getAbsolutePath() + "/";
        String randomName = UUID.randomUUID().toString();
        mFileName = path + randomName + ".3gp";
    }


    public void showRunTimePermission() {

        if (isPermissionsGranted()) {
            // we already have the Permission, Now here we can do what we want ..!
            //
            // Toast.makeText(this, "Permission Already Granted!", Toast.LENGTH_SHORT).show();

        } else {
            // Permission is not Granted !
            // we should Request the Permission!

            // put all permissions you need in this Screen into string array
            String[] permissionsArray = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            //here we requet the permission
            requestPermissions(permissionsArray, STORAGE_PERMISSION_REQUEST);
        }

    }

    private boolean isPermissionsGranted() {

        boolean isWritingExternalStorageGranted = ActivityCompat.checkSelfPermission
                (mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        boolean isAudioRecodingGrated = ActivityCompat.checkSelfPermission
                (mContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        return (isWritingExternalStorageGranted && isAudioRecodingGrated);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // user grants the Permission!
            // you can call the function to write/read to storage here!
            Toast.makeText(mContext, "Thank you for granting the Permission!", Toast.LENGTH_SHORT).show();

        } else {
            // user didn't grant the Permission we need
            Toast.makeText(mContext, "Please Grant the Permission To use this Feature!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


}

package com.programining.speechtotext.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.programining.speechtotext.R;
import com.programining.speechtotext.adapters.AudioRecordsAdapter;
import com.programining.speechtotext.interfaces.MediatorInterface;
import com.programining.speechtotext.model.MyAudioRecord;
import com.programining.speechtotext.model.MyConstants;
import com.programining.speechtotext.model.MySQLHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private String mDisplayName;
    private MediatorInterface mMediatorCallback;

    private AudioRecordsAdapter mAdapter;
    private ArrayList<MyAudioRecord> mAudioRecords;
    private ProgressBar progressBar;

    public SoundRecordFragment() {
        // Required empty public constructor
        mAudioRecords = new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mMediatorCallback = (MediatorInterface) context;
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
        progressBar = parentView.findViewById(R.id.progressBar);


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

        mAdapter = new AudioRecordsAdapter();
        RecyclerView recyclerView = parentView.findViewById(R.id.recycler_view);
        setupRecyclerView(recyclerView);

        readAudioRecordsFromFirebase();


        return parentView;
    }

    private void readAudioRecordsFromFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MyConstants.FB_KEY_AUDIO_RECORDS);

        progressBar.setVisibility(View.VISIBLE);

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                mAudioRecords.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    MyAudioRecord a = d.getValue(MyAudioRecord.class);
                    mAudioRecords.add(a);
                }
                mAdapter.update(mAudioRecords);
                progressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("fb_error", "Failed to read value.", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        DividerItemDecoration decoration = new DividerItemDecoration(mContext, manager.getOrientation());
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter.setupAdapterListener(new AudioRecordsAdapter.AdapterListener() {
            @Override
            public void onItemClick(MyAudioRecord audioRecord) {
                AnalyzeAudioFragment fragment = new AnalyzeAudioFragment();
                fragment.setAudioRecord(audioRecord);
                mMediatorCallback.changeFragmentTo(fragment, AnalyzeAudioFragment.class.getSimpleName());
            }
        });

        recyclerView.setAdapter(mAdapter);
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

        uploadAudioRecordToStorage();

    }

    private void uploadAudioRecordToStorage() {
        progressBar.setVisibility(View.VISIBLE);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(mFileName));
        final StorageReference audioRecord = storageRef.child("AudioRecords/" + mDisplayName);

        audioRecord.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //  Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        audioRecord.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri firebaseUri) {
                                saveAudioRecordToFirebaseDatabase(MyConstants.FB_KEY_STORAGE_PATH + mDisplayName);
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void saveAudioRecordToFirebaseDatabase(String firebaseUri) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MyConstants.FB_KEY_AUDIO_RECORDS);
        //generate unique id
        String firebaseId = myRef.push().getKey();


                 /*
         private int fileId;
        private String firebaseId;
        private String localPath;
        private String displayName;
        private String localUri;
        private String firebaseUri;
        private boolean isUploaded;
        private double length;
        */
        final MyAudioRecord a = new MyAudioRecord();
        a.setFirebaseId(firebaseId);
        a.setLocalPath(mFileName);
        a.setDisplayName(mDisplayName);
        a.setLocalUri("");
        a.setFirebaseUri(firebaseUri);
        a.setUploaded(true);
        a.setLength(0.0);

        myRef.child(firebaseId).setValue(a).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                saveRecordedAudioFileToSQL(a);
            }
        });

    }

    private void saveRecordedAudioFileToSQL(MyAudioRecord a) {
        MySQLHelper mySQLHelper = new MySQLHelper(mContext);
        mySQLHelper.addAudioRecord(a);
        progressBar.setVisibility(View.GONE);
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

        mDisplayName = UUID.randomUUID().toString() + ".3gp";
        mFileName = path + mDisplayName;
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

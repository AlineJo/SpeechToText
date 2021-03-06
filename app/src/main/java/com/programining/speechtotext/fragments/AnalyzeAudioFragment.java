package com.programining.speechtotext.fragments;


import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.programining.speechtotext.R;
import com.programining.speechtotext.apimodel.ResultContainer;
import com.programining.speechtotext.model.MyAudioRecord;
import com.programining.speechtotext.model.MyConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnalyzeAudioFragment extends Fragment {

    /**
     * use this command to print the TOKEN : gcloud auth application-default print-access-token
     */
    private static final String API_TOKEN = "ya29.c.Ko8BygdiScO9YW_cBrox9vvctf3aUZ0ljzIQsqtW0PiTYQ00nEie7icMxCLR9TxGRmwx_jEJVwOLdwjc5xM6NuydRzTpCzLLJ6umMH3D7awYPQxsBChgKiMwCwzzn4EYiE6cpCu1HCGMg0j1UL-R_goqXYmqdcFtFSIVq5pjIXU6A1Z-D0paC3976ffrYaBSKEY";
    private static final String LOG_TAG = "Analyze";
    private MyAudioRecord mAudioRecord;
    private MediaPlayer mPlayer;
    private boolean isPlaying;
    private FloatingActionButton fabPlay;
    private FloatingActionButton fabStopPlay;
    private Context mContext;
    private ProgressBar progressBar;
    private TextView tvResponse;
    private SeekBar seekBar;
    private TextView tvTotalTime;
    private TextView tvElapsed;

    public AnalyzeAudioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_analize_audio, container, false);


        tvTotalTime = parentView.findViewById(R.id.tv_total_time);
        tvElapsed = parentView.findViewById(R.id.tv_total_elipse);
        fabPlay = parentView.findViewById(R.id.fab_play);
        fabStopPlay = parentView.findViewById(R.id.fab_stop_playing);
        tvResponse = parentView.findViewById(R.id.tv_result);
        progressBar = parentView.findViewById(R.id.progressBar);
        seekBar = parentView.findViewById(R.id.seekBar);
        FloatingActionButton fabAnalyze = parentView.findViewById(R.id.fab_analyze);
        TextView tvTitle = parentView.findViewById(R.id.tv_title);

        tvTitle.setText(mAudioRecord.getDisplayName());

        fabAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analyzeAudioRecord();
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
        shouldEnableFloatingButton(fabStopPlay, false);

        return parentView;
    }

    void setAudioRecord(MyAudioRecord audioRecord) {
        mAudioRecord = audioRecord;
    }

    private void startPlaying() {

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mAudioRecord.getLocalPath());
            mPlayer.prepare();
            mPlayer.start();

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    updateSeekBarProgress();
                }
            });

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }


        isPlaying = true;
        shouldEnableFloatingButton(fabPlay, false);
        shouldEnableFloatingButton(fabStopPlay, true);
    }

    private void updateSeekBarProgress() {
        seekBar.setProgress(0);
        seekBar.setMax(mPlayer.getDuration());

        // Updating progress bar
        Handler handler = new Handler();
        handler.postDelayed(getUpdateRunnable(handler), 15);


    }

    private Runnable getUpdateRunnable(final Handler handler) {
        return new Runnable() {
            public void run() {
                if (mPlayer != null) {
                    long totalDuration = mPlayer.getDuration();
                    long currentDuration = mPlayer.getCurrentPosition();

                    // Displaying Total Duration time
                    //tvTotalTime.setText(currentDuration+"");
                    // Displaying time completed playing
                    tvElapsed.setText("" + milliSecondsToTimer(currentDuration));

                    // Updating progress bar
                    seekBar.setProgress((int) currentDuration);

                    // Call this thread again after 15 milliseconds => ~ 1000/60fps
                    handler.postDelayed(this, 15);
                }
            }
        };
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
        isPlaying = false;

        shouldEnableFloatingButton(fabPlay, true);
        shouldEnableFloatingButton(fabStopPlay, false);
    }

    private void shouldEnableFloatingButton(FloatingActionButton fab, boolean shouldEnable) {
        String color;
        if (shouldEnable) {
            if (fab.getId() == fabPlay.getId()) {
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

    private void analyzeAudioRecord() {
        //to send audio file to speech to text api we need to use volley

        //TODO ; in order to use volley we need the following
        // #1 Webservice(API) link
        // #2 communication method POST/GET ...
        // #3 data to pass to API
        // #4 response type : String/JSON Obj / JSON Array

        /**
         * #1 link :   https://speech.googleapis.com/v1/speech:recognize/
         *
         * #2 communication Method : POST
         *
         * #3 data to pass :
         * {
         *   "config": {
         *       "encoding":"FLAC",
         *       "sampleRateHertz": 16000,
         *       "languageCode": "en-US",
         *       "enableWordTimeOffsets": false
         *   },
         *   "audio": {
         *       "uri":"gs://cloud-samples-tests/speech/brooklyn.flac"
         *   }
         * }
         *
         * #4 response type : JSON obj
         */

        RequestQueue queue = Volley.newRequestQueue(mContext);

        progressBar.setVisibility(View.VISIBLE);


        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Gson gson = new Gson();

                ResultContainer results = gson.fromJson(response.toString(), ResultContainer.class);
                tvResponse.setText("Transcript : " + getTranscript(results));
                tvResponse.append("\nConfidence : " + getConfidence(results));

                progressBar.setVisibility(View.GONE);
            }
        };


        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvResponse.setText(error.toString());
                progressBar.setVisibility(View.GONE);
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                MyConstants.KEY_SPEECH_TO_TEXT_URL,
                getAudioJsonObj(),
                successListener,
                errorListener
        ) {
            //pass token to the webservice!
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", MyConstants.API_KEY_BEARER + API_TOKEN);
                return headers;
            }


        };

        queue.add(request);

    }

    private String getTranscript(ResultContainer results) {
        return results
                .getResults()
                .get(0)
                .getAlternatives()
                .get(0)
                .getTranscript();
    }

    private String getConfidence(ResultContainer results) {
        return results
                .getResults()
                .get(0)
                .getAlternatives()
                .get(0)
                .getConfidence() + "";
    }

    private JSONObject getAudioJsonObj() {
        /**
         * #3 data to pass :
         * {
         *   "config": {
         *       "encoding":"AMR",
         *       "sampleRateHertz": 8000,
         *       "languageCode": "en-US",
         *       "enableWordTimeOffsets": false
         *   },
         *   "audio": {
         *       "uri":"gs://yousuf-dialogflow.appspot.com/AudioRecords/<fileName>"
         *   }
         * }
         */

        try {
            JSONObject container = new JSONObject();


            JSONObject config = new JSONObject();
            JSONObject audio = new JSONObject();

            config.put(MyConstants.KEY_JSON_KEY_SAMPLE_ENCODING, "AMR");
            config.put(MyConstants.KEY_JSON_KEY_SAMPLE_RATE, 8000);
            config.put(MyConstants.KEY_JSON_KEY_LANGUAGE, MyConstants.KEY_JSON_VALUE_LANGUAGE);//for "ar", for english MyConstants.KEY_JSON_VALUE_LANGUAGE
            config.put(MyConstants.KEY_JSON_KEY_OFFSET, false);


            audio.put(MyConstants.KEY_JSON_KEY_URI, mAudioRecord.getFirebaseUri());

            container.put(MyConstants.KEY_JSON_KEY_CONFIG, config);
            container.put(MyConstants.KEY_JSON_KEY_AUDIO, audio);
            return container;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

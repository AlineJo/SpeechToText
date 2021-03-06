package com.programining.speechtotext.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.programining.speechtotext.R;
import com.programining.speechtotext.apimodel.ResultContainer;
import com.programining.speechtotext.model.MyConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SpeechToTextSampleFragment extends Fragment {

    /**
     * use this command to print the TOKEN : gcloud auth application-default print-access-token
     */
    private static final String API_TOKEN = "ya29.c.Ko8Bygf64B09YNLzUOa0LtE-G0A3oGKJCeUrq7EGme2UNirLPv5XWfqWPqRi1PKQcnQIOpntSFzHDYboAMjDw9bGFy_ZAE011H78al9GsO8MCRw2IJuiSJ5yC2vouaEZ3idQdRhsJrYgSs4rjIUOlVHqVd_7h3VLRKhQXHR3X13FkbnbP3vwGfsCWXGC12zz15U";
    private TextView tvResponse;
    private ProgressBar progressBar;
    private Context mContext;

    public SpeechToTextSampleFragment() {
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
        View parentView = inflater.inflate(R.layout.fragment_speech_to_text_sample, container, false);

        tvResponse = parentView.findViewById(R.id.tv_response);
        progressBar = parentView.findViewById(R.id.progressBar);
        Button btnUpload = parentView.findViewById(R.id.btn_upload);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendSampleJsonToSpeechToTextAPI();

            }
        });

        return parentView;
    }

    private void sendSampleJsonToSpeechToTextAPI() {
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
         *       "encoding":"FLAC",
         *       "sampleRateHertz": 16000,
         *       "languageCode": "en-US",
         *       "enableWordTimeOffsets": false
         *   },
         *   "audio": {
         *       "uri":"gs://cloud-samples-tests/speech/brooklyn.flac"
         *   }
         * }
         */

        try {
            JSONObject container = new JSONObject();


            JSONObject config = new JSONObject();
            JSONObject audio = new JSONObject();

            config.put(MyConstants.KEY_JSON_KEY_SAMPLE_ENCODING, "FLAC");
            config.put(MyConstants.KEY_JSON_KEY_SAMPLE_RATE, 16000);
            config.put(MyConstants.KEY_JSON_KEY_LANGUAGE, MyConstants.KEY_JSON_VALUE_LANGUAGE);
            config.put(MyConstants.KEY_JSON_KEY_OFFSET, false);

            audio.put(MyConstants.KEY_JSON_KEY_URI, "gs://cloud-samples-tests/speech/brooklyn.flac");

            container.put(MyConstants.KEY_JSON_KEY_CONFIG, config);
            container.put(MyConstants.KEY_JSON_KEY_AUDIO, audio);
            return container;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}

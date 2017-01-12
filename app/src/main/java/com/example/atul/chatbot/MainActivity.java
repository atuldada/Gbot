package com.example.atul.chatbot;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       final TextView response = (TextView) findViewById(R.id.textresult);
//initializing wtth client acess token not dev token remember!
        final AIConfiguration config = new AIConfiguration("257cd8e17d844690ac5e271733f24c1c",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

//for handling text response...

       final EditText input=(EditText)findViewById(R.id.editText);
        Button go=(Button)findViewById(R.id.button);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AIDataService aiDataService = new AIDataService(config);

                final AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(input.getText().toString());

                new AsyncTask<AIRequest, Void, AIResponse>() {
                    @Override
                    protected AIResponse doInBackground(AIRequest... requests) {
                        final AIRequest request = requests[0];
                        try {
                            final AIResponse response = aiDataService.request(aiRequest);
                            return response;
                        } catch (AIServiceException e) {
                            Log.d("ApiAi", "onError from text");
                        }
                        return null;
                    }


                    @Override
                    protected void onPostExecute(AIResponse aiResponse) {
                        if (aiResponse != null) {
                            // process aiResponse here
                            response.setText("Query:" + aiResponse.getResult().getResolvedQuery()+"\nResponse:"+aiResponse.getResult().getFulfillment().getSpeech()
                            );
                        }
                    }
                }.execute(aiRequest);


            }
        });







//Speech button
        AIButton aiButton = (AIButton) findViewById(R.id.micButton);


//for the speech recoggnition
        aiButton.initialize(config);

        aiButton.setResultsListener(new AIButton.AIButtonListener() {
            @Override
            public void onResult(AIResponse result) {
                Log.d("ApiAi", "onResult");
                String parameterString = "";
                if (result.getResult().getParameters() != null && !result.getResult().getParameters().isEmpty()) {
                    for (final Map.Entry<String, JsonElement> entry : result.getResult().getParameters().entrySet()) {
                        parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                    }
                }

                response.setText("Query:" + result.getResult().getResolvedQuery()+"\nResponse:"+result.getResult().getFulfillment().getSpeech()
               );

            }

            @Override
            public void onError(AIError error) {
                Log.d("ApiAi", "onError");
                response.setText(error.toString());
            }

            @Override
            public void onCancelled() {
                Log.d("ApiAi", "onCancelled");
            }
        });
    }
}

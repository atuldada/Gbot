package com.example.atul.chatbot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.http.HttpClient;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIButton;

public class MainActivity extends AppCompatActivity {
String transaction;
    String amount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       final TextView response = (TextView) findViewById(R.id.textresult);
//initializing wtth client acess token not dev token remember!
        final AIConfiguration config = new AIConfiguration("257cd8e17d844690ac5e271733f24c1c",
                AIConfiguration.SupportedLanguages.English,   //for voice and both for text
                AIConfiguration.RecognitionEngine.System);

//for handling text response...

       final EditText input=(EditText)findViewById(R.id.editText);
        Button go=(Button)findViewById(R.id.button);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideSoftKeyboard(MainActivity.this);
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
                            response.setText("Query:" + aiResponse.getResult().getResolvedQuery()+"\nResponse:"+aiResponse.getResult().getFulfillment().getSpeech());
                            transaction = response.getText().toString();
                            if(transaction.split("\n")[1].equals("Response:Transaction request processing..."))
                            {   amount=transaction.replaceAll("\\D+","");;
                                Toast.makeText(getBaseContext(), amount,Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, Web.class);
                                startActivity(intent);
                                //new RetrieveFeedTask().execute("http://www.yourdomain.com/serverside-script.php");

                            }

                        }
                    }
                }.execute(aiRequest);
                input.setText("");
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

                response.setText("Query:" + result.getResult().getResolvedQuery()+"\nResponse:"+result.getResult().getFulfillment().getSpeech());
                transaction = response.getText().toString();
                if(transaction.split("\n")[1].equals("Response:Transaction request processing..."))
                {   amount=transaction.replaceAll("\\D+","");;
                    Toast.makeText(getBaseContext(), amount,Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, Web.class);
                    startActivity(intent);
                }

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


//
//    private String convertStreamToString(InputStream is) {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//
//        String line = null;
//        try {
//            while ((line = reader.readLine()) != null) {
//                sb.append(line).append('\n');
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return sb.toString();
//    }
//    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
//    {
//        StringBuilder result = new StringBuilder();
//        boolean first = true;
//
//        for (NameValuePair pair : params)
//        {
//            if (first)
//                first = false;
//            else
//                result.append("&");
//
//            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
//            result.append("=");
//            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//        }
//
//        return result.toString();
//    }
    class RetrieveFeedTask extends AsyncTask<String, Void,Void> {

        private Exception exception;

        protected Void doInBackground(String... urls) {
            try {
                org.apache.http.client.HttpClient httpclient = new org.apache.http.impl.client.DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://emitrauat.rajasthan.gov.in/payments/v1/init");


                URL url = new URL("http://emitrauat.rajasthan.gov.in/payments/v1/init");
                JSONObject postDataParams = new JSONObject();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("MERCHANTCODE", "HACKATHON2017"));
                params.add(new BasicNameValuePair("PRN", "Testpay12345"));
                params.add(new BasicNameValuePair("REQTIMESTAMP", "20160623132259958"));
                params.add(new BasicNameValuePair("PURPOSE", "PURPOSE"));
                params.add(new BasicNameValuePair("AMOUNT", amount));
                params.add(new BasicNameValuePair("SUCCESSURL", "https://www.google.co.in/?gws_rd=ssl"));
                params.add(new BasicNameValuePair("FAILUREURL", "https://www.google.co.in/?gws_rd=ssl"));
                params.add(new BasicNameValuePair("CANCELURL", "https://www.google.co.in/?gws_rd=ssl"));
                params.add(new BasicNameValuePair("USERNAME", "ATUL DADA"));
                params.add(new BasicNameValuePair("USERMOBILE", "8696974417"));
                params.add(new BasicNameValuePair("UDF1", "UDF1"));
                params.add(new BasicNameValuePair("UDF2", "UDF2"));
                params.add(new BasicNameValuePair("UDF3", "UDF3"));
                params.add(new BasicNameValuePair("OFFICECODE", "OFFICECODE"));
                params.add(new BasicNameValuePair("REVENUEHEAD", "REVENUEHEAD"));
                params.add(new BasicNameValuePair("CHECKSUM", "9ce9d20b6a536c98af7349d2a5b7599f"));

                httppost.setEntity(new UrlEncodedFormEntity(params));
                httpclient.execute(httppost);
                httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                        CookiePolicy.BROWSER_COMPATIBILITY);
            } catch (Exception e) {
                e.printStackTrace();
                this.exception = e;


            }return null;
        }

        protected void onPostExecute(Void feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}

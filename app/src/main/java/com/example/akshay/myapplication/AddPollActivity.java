package com.example.akshay.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akshay.myapplication.configuration.ConfigurationFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddPollActivity extends AppCompatActivity {

    private android.widget.EditText editTextPollName;
    private android.widget.EditText editTextPollStartDate;
    private android.widget.EditText editTextPollEndDate;
    private android.widget.Button buttonAddPoll;
    ProgressDialog progressDialog;
    Context context;
    HttpURLConnection connection;
    private final String base_url = ConfigurationFile.base_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poll);
        this.buttonAddPoll = (Button) findViewById(R.id.buttonAddPoll);
        editTextPollEndDate = (EditText) findViewById(R.id.editTextPollEndDate);
        editTextPollStartDate = (EditText) findViewById(R.id.editTextPollStartDate);
        editTextPollName = (EditText) findViewById(R.id.editTextPollName);
        context = this;
        this.buttonAddPoll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!editTextPollName.getText().toString().isEmpty() && !editTextPollName.getText().toString().isEmpty()
                        && !editTextPollName.getText().toString().isEmpty()){
                    //Setting Progress Dialog
                    progressDialog = ProgressDialog.show(context, "iVote", "Add Poll", true, false);
                    //Preparing Paramaneters to pass in Async Thread
                    String url ="/createPoll?pollName="+ editTextPollName.getText().toString()
                            + "&pollStartDate="+  editTextPollStartDate.getText().toString()
                            + "&pollEndDate="+  editTextPollEndDate.getText().toString();
                    //Async Runner
                    AddPollActivity.AsyncTaskRunner runner = new AddPollActivity.AsyncTaskRunner();
                    runner.execute(url);
                }else{
                    Toast.makeText(context, "Please fill all the details!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /* Thread for Server Interation - Pass paramenter and URL */
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            try {
                StringBuffer responseString = null;
                String inputLine;
                URL dataUrl = new URL(base_url + params[0]);
                connection = (HttpURLConnection) dataUrl.openConnection();
                connection.setConnectTimeout(ConfigurationFile.connectionTimeout); //'Connection Timeout' is only called at the beginning to test if the server is up or not.
                connection.setReadTimeout(ConfigurationFile.connectionTimeout); //'Read Timeout' is to test a bad network all along the transfer.
                // optional default is GET
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    responseString = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        responseString.append(inputLine);
                    }
                    in.close();
                }
                resp = responseString.toString();

            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            } finally {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace(); //If you want further info on failure...
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();

            if (resp.equals("Not Created")) {
                Toast.makeText(context, "Not Successfull!!", Toast.LENGTH_LONG).show();
            } else if (resp.equals("Created")) {
                Toast.makeText(AddPollActivity.this, "Poll Added", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    public void Logout(View view){

        Intent i=new Intent(this,LoginActivity.class);
        startActivity(i);
    }

}

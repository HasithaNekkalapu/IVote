package com.example.akshay.myapplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.akshay.myapplication.adapter.VoteScreenAdapter;
import com.example.akshay.myapplication.configuration.ConfigurationFile;
import com.example.akshay.myapplication.dao.CandidateEntity;
import com.example.akshay.myapplication.dao.PollEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class VoteScreenActivity extends ListActivity {

    Context context;
    ImageView imgViewDownArrow;
    VoteScreenAdapter voteScreenAdapter;
    ArrayList<CandidateEntity> candidateEntities;
    ListView list;
    Button btnCastVote;
    ProgressDialog progressDialog;
    String utaID = ConfigurationFile.student_UTA_ID;
    private final String base_url = ConfigurationFile.base_url;
    HttpURLConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_screen);
        context = getApplicationContext();
        candidateEntities = new ArrayList<>();

        String s = getIntent().getStringExtra("DATA");
        String responseCandidate[] = s.split(ConfigurationFile.lineSeperator);
        for(int i = 0; i< responseCandidate.length; i++){
            Log.d(" Candidate - >  "+i, responseCandidate[i]);
            String[] individualCandidateColumns = responseCandidate[i].split(ConfigurationFile.columentSeperator);
            //pollObjects.add(new PollEntity(Integer.parseInt(individualPollColumns[0]), individualPollColumns[1], "Start Date: "+individualPollColumns[2], "End Date: "+ individualPollColumns[3] ));
//            id + columentSeperator + firstName+ columentSeperator + lastname + columentSeperator+ candidateEmailId
//                    + columentSeperator + candidateDOB + columentSeperator + candidateGender + columentSeperator + candidateCourse+
//                    columentSeperator+ candidateQualities+ columentSeperator+ candidateInterests+ columentSeperator +
//                    candidatesStudentOrganization+ columentSeperator + candidateCommunityServiceHours + lineSeperator;

            candidateEntities.add(new CandidateEntity(Integer.parseInt(individualCandidateColumns[0]),
                    individualCandidateColumns[1],individualCandidateColumns[2], individualCandidateColumns[3],
                    individualCandidateColumns[4], individualCandidateColumns[5], individualCandidateColumns[6],
                    individualCandidateColumns[7],  individualCandidateColumns[8]
                    , individualCandidateColumns[9] , individualCandidateColumns[10]));
        }


        voteScreenAdapter = new VoteScreenAdapter(context, R.layout.list_vote_candidates, candidateEntities);
        setListAdapter(voteScreenAdapter);
        list = getListView();
        btnCastVote = (Button) findViewById(R.id.castVote);
        //btnCastVote.setEnabled(false);
        btnCastVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(VoteScreenAdapter.selectedCandidates.size()<=0){
                    Toast.makeText(context, "Select Candidates to Vote!!",Toast.LENGTH_SHORT).show();
                }else{
                    //Setting Progress Dialog
                    //  progressDialog = ProgressDialog.show(context, "iVote", "Checking Credentails", true, false);
                    //Preparing Paramaneters to pass in Async Thread
                    String url ="/castVote?utaID="+ utaID + "&candidateIds="+  VoteScreenAdapter.selectedCandidates.toString();
                    //Async Runner
                    VoteScreenActivity.AsyncTaskRunner runner = new VoteScreenActivity.AsyncTaskRunner();
                    runner.execute(url);
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
            //progressDialog.dismiss();
            if (resp.equalsIgnoreCase("Vote Casted")) {
                Intent intent = new Intent(context, VoteAcknowledgementActivity.class);
                startActivity(intent);

            } else if (resp.equalsIgnoreCase("UnSuccessfull")) {
                Toast.makeText(context, "Unable to Cast Vote", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void Logout(View view){

        Intent i=new Intent(this,LoginActivity.class);
        startActivity(i);
    }
}

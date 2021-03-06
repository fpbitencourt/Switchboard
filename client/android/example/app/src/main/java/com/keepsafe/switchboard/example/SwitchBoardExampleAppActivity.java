/*
   Copyright 2012 KeepSafe Software Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.keepsafe.switchboard.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.keepsafe.switchboard.Switch;
import com.keepsafe.switchboard.SwitchBoard;

public class SwitchBoardExampleAppActivity extends AppCompatActivity {
	
	private static final String TAG = "SwitchBoardExampleApp";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Initializes the default URLs the first time. 
        Log.d(TAG, "init Server Urls");
        SwitchBoard.initDefaultServerUrls("http://switchboard.herokuapp.com/SwitchboardURLs.php", "http://switchboard.herokuapp.com/SwitchboardDriver.php", true);

        //Set preferable timeouts (if you don`t want the UI locked when a client has a slower net connection, or the server don`t respond)
        Log.d(TAG, "setting timeouts");
        SwitchBoard.setLoadAndUpdateTimeouts(1000, 1000);

        /* Looks at the server if there are changes in the server URL that should be used in the future
         * 
         * In production you should be loaded asynchronous with AsyncConfigLoader.
         * new AsyncConfigLoader(this, AsyncConfigLoader.UPDATE_SERVER);
         */
        Log.d(TAG, "update server urls from remote");
        SwitchBoard.updateConfigServerUrl(this);
        
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                /* Loads the actual config. This can be done on app start or on app onResume().
                * depending how often you want to update the config.
                *
                * In production you should be loaded asynchronous with AsyncConfigLoader.
                * new AsyncConfigLoader(this, AsyncConfigLoader.CONFIG_SERVER);
                */
                Log.d(TAG, "update app config");
                SwitchBoard.loadConfig(SwitchBoardExampleAppActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        applyExperiment();
                    }
                });
            }
        }).start();
    }

    private void applyExperiment() {

        //Using Switch (a utility class that wrap SwitchBoard methods)
        Switch homeScreenMessage = new Switch(this, "homeScreenMessage");

    	//see if we're in experiment "homeScreenMessage" that we defined on the server
        if(homeScreenMessage.isActive()) {
        	Log.d(TAG, "isInExperiment homeScreen");
        	//check if the experiment has values. Only needed when passing custom variables
        	if(homeScreenMessage.hasValues()) {
        		Log.d(TAG, "has values");
        		TextView tv = (TextView) findViewById(R.id.messagebox);	
        		tv.setVisibility(View.VISIBLE);
        		
        		//get experiment values
                //getting the user specific values
                String message = (String) homeScreenMessage.getValue("message");
                String messageTitle = (String) homeScreenMessage.getValue("messageTitle");

                tv.setText(Html.fromHtml(message));
                Log.d(TAG, "set message text in UI");

                /* Track the view in your preferred analytics
                 * using messageTitle to track test
                 */

                //tracks when user clicks on HTML link from your A/B test
                tv.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        /* Track the click in your preferred analytics
                         * using messageTitle to track test
                         */
                    }
                });

        	}	
        }
    }
    
    public void goNext(View v){
    	Intent i;

        //Using SwitchBoard directly

    	//gives users a different activity when they are in A/B test
    	//example on how to test user flows
    	if(SwitchBoard.isInExperiment(this, "nextActivityTest"))
    		i = new Intent(this, NextActivityTest.class);
    	else
    		i = new Intent(this, NextActivityNormal.class);
    	
    	startActivity(i);
    }
}
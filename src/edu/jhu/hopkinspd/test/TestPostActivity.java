/*
 * Copyright (c) 2015 Johns Hopkins University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holder nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.jhu.hopkinspd.test;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.conf.TestConfig;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TestPostActivity extends Activity implements SensorEventListener{

	Button next = null;
	private static final int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;
	protected static final String TAG = GlobalApp.TAG + "|" + "TestPostActivity";;
	TextView ins;
	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	GlobalApp app;
	int completeTestNumber = 0;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
	    super.onCreate(savedInstanceState);
	    app = (GlobalApp) getApplication();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
	    
	    setContentView(R.layout.testpostpage);
	    
	    ins = (TextView)findViewById(R.id.text_ins);
	    ins.setTextSize(GlobalApp.ACTIVE_TESTS_FONT_SIZE);
	    
	    Bundle bundle = getIntent().getExtras();
	    completeTestNumber = 0;
	    if (bundle != null)
	    	completeTestNumber = bundle.getInt("TestNumber", 0);
		
		if(completeTestNumber >= TestConfig.getNumberOfTests())
		{
			Log.i(TAG, "testNumber reaches to NUMBER_OF_TESTS: " 
					+ completeTestNumber);
			finish();
			return;
		}
			
		
	    
	    int numRestTests = TestConfig.getNumberOfTests() 
	    		- completeTestNumber - 1;
	    //ins.setText("The test is complete. Only " + numRestTests + " tests left to go!");
	    ins.setText(String.format(getString(R.string.next_test_dir), numRestTests));
		next = (Button)findViewById(R.id.button_nexttest);
        next.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
//            	GlobalApp.writeLogTextLine("Start test " + testNumber + " button pressed", false);
            	Log.i(TAG, "click next test " + completeTestNumber + " button pressed");
            	Intent nextPage = new Intent(app, TestPrepActivity.class);
				nextPage.putExtra("TestNumber", completeTestNumber);
                               
                startActivity(nextPage);
                finish();
            }

        });
		
    }
	

	
	private long lastBackClick;
    @Override
    public void onBackPressed()
    {
		long current = System.currentTimeMillis();
		if(current - lastBackClick > 15*1000 ){
			lastBackClick = current;
			Toast.makeText(app, "click again to exit current test", Toast.LENGTH_LONG).show();
		}else{
			super.onBackPressed();
		}
    }
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.values[0] > 0)
		{
			next.setEnabled(true);
		}
		else
		{
			next.setEnabled(false);
		}
	}

}

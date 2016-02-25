package edu.jhu.hopkinspd.test;

import java.io.BufferedWriter;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class TestActivity extends Activity
{
	protected static final String TAG = "TestActivity";

	int[] testInsStringList = {R.string.dir_voice, R.string.dir_balance, R.string.dir_gait,
			R.string.dir_dexterity, R.string.dir_reaction, R.string.dir_rest_tremor, R.string.dir_postural_tremor};

	int[] testViewList = {R.layout.testpage, R.layout.testpage, R.layout.testpage,
			R.layout.testtappage, R.layout.testreactpage, R.layout.testpage, R.layout.testpage};

	int testNumber = 0;
	private AudioCapture audioObj = null;
	private AccelCapture accelObj = null;
	private GyroCapture gyroObj = null;
	private boolean gyro_on = false;
	private TapCapture tapObj = null;
	private ReactCapture reactObj = null;
	private boolean reactButtonOn = false;
	private Button button = null;
	private GlobalApp app;
	private Button nextButton = null;

	private long lastBackClick;
	
	private CountDownTimer taskTimer = null;

	private String logFileType = "activetest";

	private BufferedWriter logWriter;

	private CountDownTimer cdTimer;

	private CountDownTimer ptTimer;

	TextView ins;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		app = (GlobalApp) getApplication();
		logWriter = app.openLogTextFile(logFileType );
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			testNumber = bundle.getInt("TestNumber", 0);
			
			
		}

		setContentView(testViewList[testNumber]);
		ins = (TextView)findViewById(R.id.dir_text);
		ins.setTextSize(GlobalApp.ACTIVE_TESTS_FONT_SIZE);
		ins.setText(testInsStringList[testNumber]);

		if (testNumber == GlobalApp.TEST_DEXTERITY)
		{
			button = (Button)findViewById(R.id.tap1_button);
			button.setVisibility(View.INVISIBLE);
			button = (Button)findViewById(R.id.tap2_button);
			button.setVisibility(View.INVISIBLE);
		}
		if (testNumber == GlobalApp.TEST_REACTION)
		{
			button = (Button)findViewById(R.id.button_react);
			button.setVisibility(View.INVISIBLE);
		}
		nextButton = (Button)findViewById(R.id.button_next);
		nextButton.setVisibility(View.GONE);
		
		
		nextButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.i(TAG, "onClick next button");
				// skip test
				skipTest();
			}
			
		});
		
		// Pre-test pause
		cdTimer =
		new CountDownTimer(GlobalApp.preTestPauseDur[testNumber]*1000, 1000)
		{
			public void onTick(long millisLeft){}
			public void onFinish()
			{
				// Pre-test vibrate?
				if (GlobalApp.preTestVibrate[testNumber])
				{
					((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(GlobalApp.VIBRATE_DUR*1000);
				}
				
				runTest();
				
				if(app.isInTestDemo()){
					nextButton.setVisibility(View.VISIBLE);
				}else
					nextButton.setVisibility(View.GONE);
				
			}
		}.start();
		setTextColor(app.getBooleanPref(getString(R.string.colorHighContrastOn)));
		gyro_on = app.getBooleanPref(getString(R.string.test_gait));
	}

	private void setTextColor(boolean highContrast) {
		if(highContrast){
			this.nextButton.setTextColor(Color.WHITE);
			if(button != null)
				this.button.setTextColor(Color.WHITE);
			ins.setTextColor(Color.WHITE);
		}else{
			this.nextButton.setTextColor(Color.BLUE);
			if(button != null)
				this.button.setTextColor(Color.BLUE);
			
			ins.setTextColor(Color.BLACK);
		}
	}
	/**
	 * Used in demo mode.
	 * Skip the current test
	 */
	private void skipTest(){
		if(taskTimer != null)
		{
			taskTimer.cancel();
			finishTest();
		}
	}

	private synchronized void runTest()
	{
		if (testNumber == GlobalApp.TEST_VOICE)
		{
			audioObj = new AudioCapture(app, testNumber);
			if(!audioObj.startRecording())
			{
				String text = "Recording is not initialized. Please try again later.";		
				app.writeLogTextLine(logWriter, text, true);
			}
		}
		else if ((testNumber == GlobalApp.TEST_BALANCE) 
				|| (testNumber == GlobalApp.TEST_GAIT)
				|| (testNumber == GlobalApp.TEST_REST_TREMOR)
				|| (testNumber == GlobalApp.TEST_POSTURAL_TREMOR)
				)
		{
			accelObj = new AccelCapture(app, testNumber);
			accelObj.startRecording();
			
			if(gyro_on){
				gyroObj = new GyroCapture(app, testNumber);
				gyroObj.startRecording();
			}
		}
		else if (testNumber == GlobalApp.TEST_DEXTERITY)
		{
			button = (Button)findViewById(R.id.tap1_button);
			button.setVisibility(View.VISIBLE);
			button = (Button)findViewById(R.id.tap2_button);
			button.setVisibility(View.VISIBLE);
			tapObj = new TapCapture(app, testNumber);
			tapObj.startRecording();
		}
		else if (testNumber == GlobalApp.TEST_REACTION)
		{
			reactObj = new ReactCapture(app, testNumber);
			reactObj.startRecording();
		}

		taskTimer = new CountDownTimer(GlobalApp.testCaptureDur[testNumber]*1000, GlobalApp.CHANGE_REACT_DUR*1000)
		{
			public void onTick(long millisLeft)
			{
				if (testNumber == GlobalApp.TEST_REACTION)
				{
					// Choose new random tap button status
					int reactButtonVisible = View.INVISIBLE;
					boolean random = Math.random() > 0.5;
					if (random != reactButtonOn)
					{
						reactButtonOn = random;
						button = (Button)findViewById(R.id.button_react);
						if (reactButtonOn)
						{
							reactButtonVisible = View.VISIBLE;
						}
						button.setVisibility(reactButtonVisible);
						reactObj.handleTouchEvent(null, reactButtonOn);
					}
				}
			}

			public void onFinish()
			{
				finishTest();
			}
		}.start();
	}
	
	private synchronized void finishTest(){
		if (testNumber == GlobalApp.TEST_VOICE)
		{	
			audioObj.stopRecording();
			audioObj.destroy();
		}
		else if ((testNumber == GlobalApp.TEST_BALANCE) 
				|| (testNumber == GlobalApp.TEST_GAIT)
				|| (testNumber == GlobalApp.TEST_REST_TREMOR)
				|| (testNumber == GlobalApp.TEST_POSTURAL_TREMOR)
				)
		{
			accelObj.stopRecording();
			accelObj.destroy();
			if(gyro_on){
				gyroObj.stopRecording();
				gyroObj.destroy();
			}
		}
		else if (testNumber == GlobalApp.TEST_DEXTERITY)
		{
			button = (Button)findViewById(R.id.tap1_button);
			button.setVisibility(View.INVISIBLE);
			button = (Button)findViewById(R.id.tap2_button);
			button.setVisibility(View.INVISIBLE);
			tapObj.stopRecording();
			tapObj.destroy();
		}
		else if (testNumber == GlobalApp.TEST_REACTION)
		{
			reactObj.stopRecording();
			reactObj.destroy();
		}
		postTestPause();
	}

	private void postTestPause()
	{
		// Post-test pause
		ptTimer = 
		new CountDownTimer(GlobalApp.postTestPauseDur[testNumber]*1000, 1000)
		{
			public void onTick(long millisLeft){}
			public void onFinish()
			{
				// Post-test vibrate?
				if (GlobalApp.postTestVibrate[testNumber])
				{
					((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(GlobalApp.VIBRATE_DUR * 1000);
				}
				testComplete();
			}
		}.start();
	}
	
	private void testComplete()
	{
		Intent nextPage = null;

		// When we're done, roll onto the next test prep page
//		testNumber += 1;
		testNumber = app.getNextTestNumber(testNumber+1);
		if (!TestPrepActivity.singleTest 
				&& testNumber < GlobalApp.NUMBER_OF_TESTS)
		{
			if (app.isNextTestScreenOn()){
				nextPage = new Intent(app, TestPostActivity.class);
				nextPage.putExtra("TestNumber", testNumber);
			}else{
				nextPage = new Intent(app, TestPrepActivity.class);
				nextPage.putExtra("TestNumber", testNumber);
			}
		}
		else
		{
			nextPage = new Intent(app, TestEndActivity.class);
			if(TestPrepActivity.singleTest)
				Log.i(TAG, "single test end");
			else
				Log.i(TAG, "testNumber reaches to NUMBER_OF_TESTS: " + testNumber);
			
		}
		startActivity(nextPage);
		finish();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me)
	{
		if (tapObj != null)
		{
			tapObj.handleTouchEvent(me);
		}
		if (reactObj != null)
		{
			reactObj.handleTouchEvent(me, reactButtonOn);
		}
		if(app.isInTestDemo())
			return super.dispatchTouchEvent(me);
		else
			return false;
		
	}

	//	@Override
	//	public boolean onTouchEvent(MotionEvent me)
	//	{
	//	}

	@Override
	public void onBackPressed()
	{
		long current = System.currentTimeMillis();
		if(current - lastBackClick > 15*1000 ){
			lastBackClick = current;
			Toast.makeText(app, "click again to exit current test", Toast.LENGTH_LONG).show();
		}else{
			if(taskTimer != null)
				taskTimer.cancel();
			if(cdTimer != null)
				cdTimer.cancel();
			if(ptTimer != null)
				ptTimer.cancel();
			if (testNumber == GlobalApp.TEST_VOICE && audioObj != null)
			{	
				audioObj.stopRecording();
				audioObj.destroy();
			}
			else if ((testNumber == GlobalApp.TEST_BALANCE) 
					|| (testNumber == GlobalApp.TEST_GAIT)
					|| (testNumber == GlobalApp.TEST_REST_TREMOR)
					|| (testNumber == GlobalApp.TEST_POSTURAL_TREMOR)
					)
			{
				if(accelObj != null){
					accelObj.stopRecording();
					accelObj.destroy();	
				}
				if(gyro_on && gyroObj != null){
					gyroObj.stopRecording();
					gyroObj.destroy();
				}
			}
			else if (testNumber == GlobalApp.TEST_DEXTERITY)
			{
				button = (Button)findViewById(R.id.tap1_button);
				button.setVisibility(View.INVISIBLE);
				button = (Button)findViewById(R.id.tap2_button);
				button.setVisibility(View.INVISIBLE);
				if (tapObj != null){
					tapObj.stopRecording();
					tapObj.destroy();
				}
				
			}
			else if (testNumber == GlobalApp.TEST_REACTION && reactObj != null)
			{
				reactObj.stopRecording();
				reactObj.destroy();
			}
			super.onBackPressed();
		}
	}
}
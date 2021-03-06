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
package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;
import java.util.ArrayList;


import android.view.MotionEvent;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.TestActivity;
import edu.jhu.hopkinspd.test.TestPrepActivity;

public abstract class TestConfig {

	public static final int[] ALL_TEST_NAMES = {
			R.string.test_voice,
			R.string.test_balance,
			R.string.test_gait,
			R.string.test_dexterity,
			R.string.test_dexterity_left,
			R.string.test_dexterity_right,
			R.string.test_reaction,
			R.string.test_rest_tremor,
			R.string.test_rest_tremor_left,
			R.string.test_rest_tremor_right,
			R.string.test_postural_tremor,
			R.string.test_postural_tremor_left,
			R.string.test_postural_tremor_right,
	};
	
	public static final TestConfig[] ALL_TESTS = {
			new VoiceTestConfig(),
			new BalanceTestConfig(),
			new GaitTestConfig(),
			new TapTestConfig(),
			new TapTestConfig("left"),
			new TapTestConfig("right"),
			new ReactionTestConfig(),
			new RestTremorTestConfig(),
			new RestTremorTestConfig("left"),
			new RestTremorTestConfig("right"),
			new PosturalTremorTestConfig(),
			new PosturalTremorTestConfig("left"),
			new PosturalTremorTestConfig("right")
	};
	
	public int test_name, test_disp_name;
	public int pre_test_text, pre_icon;
	public int test_text, test_view;
	public int preTestPauseDur = 2;
	public int postTestPauseDur = 0;
	public int testCaptureDur = 20;
	public boolean preTestVibrate = false;
	public boolean postTestVibrate = false;
	public int pre_test_layout = R.layout.testpreppage;
	public String help_link;

    public int audio_ins;
	
	private static ArrayList<TestConfig> enabled_tests = null;
	public static boolean gyro_on = false; 
	
	protected int getDisplayName(int test_name){
	    GlobalApp app = GlobalApp.getApp();
	    String testName = app.getString(test_name);
	    int test_display_name = app.getResources()
	            .getIdentifier(testName + "_disp_name", "string", 
	                    app.getPackageName());
	    return test_display_name; 
	}
	
	private static ArrayList<TestConfig> getEnabledTests(){
		if(enabled_tests == null){
			GlobalApp app = GlobalApp.getApp();
			enabled_tests = new ArrayList<TestConfig>();
			for(int i = 0; i < ALL_TEST_NAMES.length; i++){
				int test = ALL_TEST_NAMES[i];
				if(app.getBooleanPref(app.getString(test))){
					enabled_tests.add(ALL_TESTS[i]);
				}
			}
			
		}
		return enabled_tests;
	}
	
	public static String[] getEnabledTestDisplayNames(){
	    GlobalApp app = GlobalApp.getApp();
	    if(enabled_tests == null)
	        getEnabledTests();
	    String[] names = new String[enabled_tests.size()];
	    for(int i =0; i < enabled_tests.size(); i++){
	        names[i] = app.getString(enabled_tests.get(i).test_disp_name);
	    }
	    return names;
	}
	
	public static void updateEnabledTests(){
	    GlobalApp app = GlobalApp.getApp();
        enabled_tests = new ArrayList<TestConfig>();
        for(int i = 0; i < ALL_TEST_NAMES.length; i++){
            int test = ALL_TEST_NAMES[i];
            if(app.getBooleanPref(app.getString(test))){
                enabled_tests.add(ALL_TESTS[i]);
            }
        }
        
	}
	
	public static int getNumberOfTests() {
		ArrayList<TestConfig> enabled_tests = getEnabledTests(); 
		return enabled_tests.size();
	}
	
	public static TestConfig getTestConfig(int testNumber){
		ArrayList<TestConfig> enabled_tests = getEnabledTests();
		return enabled_tests.get(testNumber);
	}
	
	
	public abstract void runTest(TestActivity activity, BufferedWriter logWriter);
	public abstract void completeTest();
	public abstract void cancelTest();

	public void createTest(TestActivity activity){}

	public void dispatchTouchEvent(MotionEvent me) {}

	public void onInTestTimerTick(TestActivity activity) {}

    public void createPreTest(TestPrepActivity testPrepActivity) {}
	
	
}

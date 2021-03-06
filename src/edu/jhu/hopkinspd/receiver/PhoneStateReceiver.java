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
package edu.jhu.hopkinspd.receiver;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.stream.TelephonyWriter;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;


public class PhoneStateReceiver extends BroadcastReceiver {
	private static final String TAG = PhoneStateReceiver.class.getSimpleName();
	private static String phone_number = "";
	private boolean incomingFlag = false;
	private TelephonyWriter writer;
	private GlobalApp app;
	private static String state = "";
	
	public PhoneStateReceiver(TelephonyWriter telephonyWriter) {
		super();
		this.writer = telephonyWriter;
		app = GlobalApp.getApp();
	}

	public PhoneStateReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	    if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
	        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);    // outgoing call
	        if(phoneNumber == null || phoneNumber == "")
	        	phoneNumber = "unknown";
	        phone_number = app.encrypt(phoneNumber);
	        state = "OUTGOING";
	        Log.i(TAG, "call OUT:" + phone_number);
	        writer.writeLogTextLine("RECEIVER_ACTION_NEW_OUTGOING_CALL " + phoneNumber);
	    } else {
	        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
	
	        switch (tm.getCallState()) {
	        case TelephonyManager.CALL_STATE_RINGING:  // incoming call
	            incomingFlag = true;
	            String inc = intent.getStringExtra("incoming_number");
	            if(inc == null) return;
	            phone_number = app.encrypt(inc);
	            Log.d(TAG, "CALL_STATE_RINGING :" + phone_number);
	            state = "INCOMING";
	            writer.writeLogTextLine("RECEIVER_CALL_STATE_RINGING " +state +" " + phone_number);
	            break;
	        case TelephonyManager.CALL_STATE_OFFHOOK:
	            if (incomingFlag) {
	                Log.d(TAG, "incoming ACCEPT :" + phone_number);
	            }
	            Log.d(TAG, "CALL_STATE_OFFHOOK");
	            writer.writeLogTextLine("RECEIVER_CALL_STATE_OFFHOOK " +state +" " + phone_number);
	            break;
	
	        case TelephonyManager.CALL_STATE_IDLE:
	            if (incomingFlag) {     // hang up
	                Log.d(TAG, "incoming IDLE, number:" + phone_number);
	            }
	            Log.d(TAG, "CALL_STATE_IDLE");
	            writer.writeLogTextLine("RECEIVER_CALL_STATE_IDLE " +state +" " + phone_number);
	            incomingFlag = false;
	            phone_number = "";
	            break;
	        }
	    }
	}
}
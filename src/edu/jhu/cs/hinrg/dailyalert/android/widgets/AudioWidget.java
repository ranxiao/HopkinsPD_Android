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
/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.jhu.cs.hinrg.dailyalert.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import edu.jhu.cs.hinrg.dailyalert.android.activities.FormEntryActivity;
import edu.jhu.cs.hinrg.dailyalert.android.utilities.FileUtils;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class AudioWidget extends QuestionWidget implements IBinaryWidget {
    private final static String t = "MediaWidget";

    private Button mCaptureButton;
    private Button mPlayButton;
    private Button mChooseButton;

    private String mBinaryName;
    private String mInstanceFolder;

    private boolean mWaitingForData;


    public AudioWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mWaitingForData = false;
        mInstanceFolder =
            FormEntryActivity.InstancePath.substring(0,
                FormEntryActivity.InstancePath.lastIndexOf("/") + 1);

        setOrientation(LinearLayout.VERTICAL);

        // setup capture button
        mCaptureButton = new Button(getContext());
        mCaptureButton.setText(getContext().getString(R.string.capture_audio));
        mCaptureButton
                .setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.APPLICATION_FONT_SIZE);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());
                mWaitingForData = true;
                ((Activity) getContext())
                        .startActivityForResult(i, FormEntryActivity.AUDIO_CAPTURE);

            }
        });
        // mCaptureButton.setOnLongClickListener(listener);

        // setup capture button
        mChooseButton = new Button(getContext());
        // TODO: add to strings.xml
        mChooseButton.setText("Choose Sound");
        mChooseButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.APPLICATION_FONT_SIZE);
        mChooseButton.setPadding(20, 20, 20, 20);
        mChooseButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("audio/*");
                mWaitingForData = true;
                ((Activity) getContext())
                        .startActivityForResult(i, FormEntryActivity.AUDIO_CHOOSER);

            }
        });
        // mChooseButton.setOnLongClickListener(listener);

        // setup play button
        mPlayButton = new Button(getContext());
        mPlayButton.setText(getContext().getString(R.string.play_audio));
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalApp.APPLICATION_FONT_SIZE);
        mPlayButton.setPadding(20, 20, 20, 20);

        // on play, launch the appropriate viewer
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                File f = new File(mInstanceFolder + "/" + mBinaryName);
                i.setDataAndType(Uri.fromFile(f), "audio/*");
                ((Activity) getContext()).startActivity(i);

            }
        });
        // mPlayButton.setOnLongClickListener(listener);

        // retrieve answer from data model and update ui
        mBinaryName = prompt.getAnswerText();
        if (mBinaryName != null) {
            mPlayButton.setEnabled(true);
        } else {
            mPlayButton.setEnabled(false);
        }

        // finish complex layout
        addView(mCaptureButton);
        addView(mChooseButton);
        addView(mPlayButton);
    }


    private void deleteMedia() {
        // get the file path and delete the file
        File f = new File(mInstanceFolder + "/" + mBinaryName);
        if (!f.delete()) {
            Log.i(t, "Failed to delete " + f);
        }

        // clean up variables
        mBinaryName = null;
    }


    @Override
    public void clearAnswer() {
        // remove the file
        deleteMedia();

        // reset buttons
        mPlayButton.setEnabled(false);
    }


    @Override
    public IAnswerData getAnswer() {
        if (mBinaryName != null) {
            return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }


    private Uri getUriFromPath(String path) {
        // find entry in content provider
        Cursor c =
            getContext().getContentResolver().query(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                "_data='" + path + "'", null, null);
        c.moveToFirst();

        // create uri from path
        String newPath =
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                    + c.getInt(c.getColumnIndex("_id"));
        c.close();
        return Uri.parse(newPath);
    }


    private String getPathFromUri(Uri uri) {

        String[] audioProjection = {
            Audio.Media.DATA
        };
        Cursor c = ((Activity) getContext()).managedQuery(uri, audioProjection, null, null, null);
        ((Activity) getContext()).startManagingCursor(c);
        int column_index = c.getColumnIndexOrThrow(Audio.Media.DATA);
        String audioPath = null;
        if (c.getCount() > 0) {
            c.moveToFirst();
            audioPath = c.getString(column_index);
        }
        return audioPath;
    }


    @Override
    public void setBinaryData(Object binaryuri) {
        // when replacing an answer. remove the current media.
        if (mBinaryName != null) {
            deleteMedia();
        }

        // get the file path and create a copy in the instance folder
        String binaryPath = getPathFromUri((Uri) binaryuri);
        String extension = binaryPath.substring(binaryPath.lastIndexOf("."));
        String destAudioPath = mInstanceFolder + "/" + System.currentTimeMillis() + extension;

        File source = new File(binaryPath);
        File newAudio = new File(destAudioPath);
        FileUtils.copyFile(source, newAudio);

        if (newAudio.exists()) {
            // Add the copy to the content provier
            ContentValues values = new ContentValues(6);
            values.put(Audio.Media.TITLE, newAudio.getName());
            values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
            values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

            Uri AudioURI =
                getContext().getContentResolver().insert(Audio.Media.EXTERNAL_CONTENT_URI, values);
            Log.i(t, "Inserting AUDIO returned uri = " + AudioURI.toString());
        } else {
            Log.e(t, "Inserting Audio file FAILED");
        }

        mBinaryName = newAudio.getName();
        mWaitingForData = false;
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    @Override
    public boolean isWaitingForBinaryData() {
        return mWaitingForData;
    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        super.setOnLongClickListener(l);
        mCaptureButton.setOnLongClickListener(l);
        mChooseButton.setOnLongClickListener(l);
        mPlayButton.setOnLongClickListener(l);
    }

}

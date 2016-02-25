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

package edu.jhu.cs.hinrg.dailyalert.android.tasks;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.model.xform.XFormSerializingVisitor;

import edu.jhu.cs.hinrg.dailyalert.android.activities.FormEntryActivity;
import edu.jhu.cs.hinrg.dailyalert.android.database.FileDbAdapter;
import edu.jhu.cs.hinrg.dailyalert.android.listeners.FormSavedListener;
import edu.jhu.cs.hinrg.dailyalert.android.logic.FormController;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveToDiskTask extends AsyncTask<Void, String, Integer> {
    private final static String t = "SaveToDiskTask";

    private FormSavedListener mSavedListener;
    private Context mContext;
    private Boolean mSave;
    private Boolean mMarkCompleted;

	

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;
    public static final int SAVED_AND_EXIT = 504;
    public static final int SAVED_AND_UPLOAD = 505;

	private static final String TAG = "SaveToDiskTask";


    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected Integer doInBackground(Void... nothing) {

        // validation failed, pass specific failure
        int validateStatus = validateAnswers(mMarkCompleted);
        if (validateStatus != VALIDATED) {
            return validateStatus;
        }

        FormEntryActivity.mFormController.postProcessInstance();

        if (mSave && exportData(mContext, mMarkCompleted)) {
        	if(FormEntryActivity.upload)
        		return SAVED_AND_UPLOAD;
            return SAVED_AND_EXIT;
        } else if (exportData(mContext, mMarkCompleted)) {
            return SAVED;
        }

        return SAVE_ERROR;

    }


    public boolean exportData(Context context, boolean markCompleted) {

        ByteArrayPayload payload;
        try {

            // assume no binary data inside the model.
            FormInstance datamodel =
                FormEntryActivity.mFormController.getInstance();
            

            XFormSerializingVisitor serializer = new XFormSerializingVisitor();
            payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);

            // write out xml
            exportXmlFile(payload, FormEntryActivity.InstancePath);
            Log.i(TAG, "write out xml " + FormEntryActivity.InstancePath);
        } catch (IOException e) {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }

        FileDbAdapter fda = new FileDbAdapter();
        fda.open();
        File f = new File(FormEntryActivity.InstancePath);
        Cursor c = fda.fetchFilesByPath(f.getAbsolutePath(), null);
        if (!mMarkCompleted) {
            if (c != null && c.getCount() == 0) {
                fda.createFile(FormEntryActivity.InstancePath, FileDbAdapter.TYPE_INSTANCE,
                    FileDbAdapter.STATUS_INCOMPLETE);
            } else {
                fda.updateFile(FormEntryActivity.InstancePath, FileDbAdapter.STATUS_INCOMPLETE);
            }
        } else {
            if (c != null && c.getCount() == 0) {
                fda.createFile(FormEntryActivity.InstancePath, FileDbAdapter.TYPE_INSTANCE,
                    FileDbAdapter.STATUS_COMPLETE);

            } else {
                fda.updateFile(FormEntryActivity.InstancePath, FileDbAdapter.STATUS_COMPLETE);
            }
        }
        // clean up cursor
        if (c != null) {
            c.close();
        }

        fda.close();
        return true;

    }


    private boolean exportXmlFile(ByteArrayPayload payload, String path) {

        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        byte[] data = new byte[len];
        try {
            int read = is.read(data, 0, len);
            if (read > 0) {
                // write xml file
                try {
                    // String filename = path + "/" +
                    // path.substring(path.lastIndexOf('/') + 1) + ".xml";
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path));
                    bw.write(new String(data, "UTF-8"));
                    bw.flush();
                    bw.close();
                    return true;

                } catch (IOException e) {
                    Log.e(t, "Error writing XML file");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(t, "Error reading from payload data stream");
            e.printStackTrace();
            return false;
        }

        return false;

    }


    @Override
    protected void onPostExecute(Integer result) {
        synchronized (this) {
            if (mSavedListener != null)
                mSavedListener.savingComplete(result);
        }
    }


    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            mSavedListener = fsl;
        }
    }


    public void setExportVars(Context context, Boolean saveAndExit,
            Boolean markCompleted) {
        mContext = context;
        mSave = saveAndExit;
        mMarkCompleted = markCompleted;
    }


    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     * 
     * @param markCompleted
     * @return validatedStatus
     */

    private int validateAnswers(Boolean markCompleted) {

        FormIndex i = FormEntryActivity.mFormController.getFormIndex();

        FormEntryActivity.mFormController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        int event;
        while ((event = FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP)) != FormEntryController.EVENT_END_OF_FORM) {
            if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                int saveStatus =
                    FormEntryActivity.mFormController.answerQuestion(FormEntryActivity.mFormController.getQuestionPrompt()
                            .getAnswerValue());
                if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) {
                    return saveStatus;
                }
            }
        }

        FormEntryActivity.mFormController.jumpToIndex(i);
        return VALIDATED;
    }


	

}

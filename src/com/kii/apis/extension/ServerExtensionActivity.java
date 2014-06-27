
package com.kii.apis.extension;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.R;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiServerCodeEntry;
import com.kii.cloud.storage.KiiServerCodeEntryArgument;
import com.kii.cloud.storage.KiiServerCodeExecResult;
import com.kii.cloud.storage.exception.app.AppException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ServerExtensionActivity extends FragmentActivity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_extension);
        findViewById(R.id.delete_all_notes).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_all_notes:
                runExtension();
                break;
        }
    }

    protected void runExtension() {
        new AsyncTaskWithProgress(this) {
            String msg = null;
            int resultCode = 0;

            @Override
            protected Void doInBackground(Void... params) {
                KiiServerCodeEntry entry = Kii.serverCodeEntry("deleteAllNotes");

                try {
                    JSONObject rawArg = new JSONObject();

                    KiiServerCodeEntryArgument arg = KiiServerCodeEntryArgument
                            .newArgument(rawArg);

                    // Execute the Server Code
                    KiiServerCodeExecResult res = entry.execute(arg);

                    // Parse the result.
                    JSONObject returned = res.getReturnedValue();
                    JSONObject returnedValue = returned.getJSONObject("returnedValue");
                    resultCode = returnedValue.getInt("result");
                    msg = returnedValue.optString("msg");
                } catch (AppException ae) {
                    // Handle error.
                    ae.printStackTrace();
                    has_exception = true;
                } catch (IOException ie) {
                    // Handle error.
                    ie.printStackTrace();
                    has_exception = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    has_exception = true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (!has_exception) {
                    if (resultCode == 0) {
                        Toast.makeText(mActivity, "Run server extension successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (!TextUtils.isEmpty(msg)) {
                            Toast.makeText(mActivity, msg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

        }.execute();

    }
}

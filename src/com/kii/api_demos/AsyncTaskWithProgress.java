
package com.kii.api_demos;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public abstract class AsyncTaskWithProgress extends AsyncTask<Void, Void, Void> {
    ProgressDialogFragment dialog = null;
    protected FragmentActivity mActivity = null;
    protected boolean has_exception = false;

    public AsyncTaskWithProgress(FragmentActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialogFragment.newInstance(mActivity.getString(R.string.in_progressing));
        dialog.show(mActivity.getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        dialog.dismiss();
        if (has_exception) {
            Toast.makeText(mActivity, "Operation failed, check the log",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

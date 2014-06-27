
package com.kii.apis;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public abstract class AsyncTaskWithProgress extends AsyncTask<Void, Void, Void> {
    ProgressDialogFragment dialog = null;
    protected FragmentActivity mActivity = null;
    protected boolean has_exception = false;
    String msg = null;

    public AsyncTaskWithProgress(FragmentActivity activity) {
        super();
        mActivity = activity;
    }

    public AsyncTaskWithProgress(FragmentActivity activity, String message) {
        super();
        mActivity = activity;
        msg = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialogFragment.newInstance(msg == null ? mActivity
                .getString(R.string.in_progressing) : msg);
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

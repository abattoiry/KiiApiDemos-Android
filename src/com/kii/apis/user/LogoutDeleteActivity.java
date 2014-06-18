
package com.kii.apis.user;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.R;
import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;

public class LogoutDeleteActivity extends FragmentActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.logout_delete);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        TextView tv = (TextView) findViewById(R.id.user);
        tv.setText(getString(R.string.current_user, KiiUser.getCurrentUser().getUsername()));
        View v = findViewById(R.id.logout);
        v.setOnClickListener(this);
        v = findViewById(R.id.delete);
        v.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                KiiUser.logOut();
                Toast.makeText(LogoutDeleteActivity.this,
                        "Current user is loged out, please login again",
                        Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.delete:
                new DeleteUserTask().execute();
                break;
        }
    }

    class DeleteUserTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;
        boolean succ = true;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().delete();
            } catch (Exception e) {
                e.printStackTrace();
                succ = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance("Deleting from server...");
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (succ) {
                Toast.makeText(LogoutDeleteActivity.this,
                        "Current user is deleted, please login again",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(LogoutDeleteActivity.this,
                        "Delete current user failed, please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

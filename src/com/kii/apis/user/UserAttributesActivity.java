
package com.kii.apis.user;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.api_demos.R;
import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;

public class UserAttributesActivity extends FragmentActivity implements OnClickListener {

    public static final String KEY_GENDER = "gender";
    public static final String KEY_AGE = "age";
    Spinner genderSpinner;
    TextView ageView, passwordView, oldpwdView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.user_attributes);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        genderSpinner = (Spinner) findViewById(R.id.gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        ageView = (TextView) findViewById(R.id.age);
        passwordView = (TextView) findViewById(R.id.password);
        oldpwdView = (TextView) findViewById(R.id.old_password);
        findViewById(R.id.save_button).setOnClickListener(this);
        findViewById(R.id.change_password).setOnClickListener(this);
        new RefreshTask().execute();
    }

    void updateInfo() {
        KiiUser mUser = KiiUser.getCurrentUser();
        int gender = mUser.getInt(KEY_GENDER, 0);
        genderSpinner.setSelection(gender);
        int age = mUser.getInt(KEY_AGE, 0);
        ageView.setText(Integer.toString(age));
    }

    class RefreshTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().refresh();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            updateInfo();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password:
                String password = passwordView.getText().toString();
                if (!KiiUser.isValidPassword(password)) {
                    Toast.makeText(this, R.string.password_invalid, Toast.LENGTH_LONG).show();
                    return;
                }
                new ChangePasswordTask(oldpwdView.getText().toString(), password).execute();
                break;
            case R.id.save_button:
                new SaveInfoTask().execute();
                break;
        }
    }

    class SaveInfoTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser mUser = KiiUser.getCurrentUser();
                long g_id = genderSpinner.getSelectedItemId();
                if (g_id == AdapterView.INVALID_ROW_ID) {
                    g_id = 0;
                }
                mUser.set(KEY_GENDER, (int) g_id);
                String ageStr = ageView.getText().toString();
                int age = 0;
                try {
                    age = Integer.parseInt(ageStr);
                } catch (Exception e) {
                }
                mUser.set(KEY_AGE, age);
                mUser.update();
                mUser.refresh();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            updateInfo();
        }
    }

    class ChangePasswordTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;
        String newPwd = null, oldPwd = null;
        boolean succ = true;

        public ChangePasswordTask(String oldPassword, String password) {
            newPwd = password;
            oldPwd = oldPassword;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().changePassword(newPwd, oldPwd);
            } catch (Exception e) {
                succ = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Toast.makeText(UserAttributesActivity.this,
                    succ ? R.string.change_pwd_succ : R.string.change_pwd_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

}

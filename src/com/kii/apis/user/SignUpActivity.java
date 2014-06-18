
package com.kii.apis.user;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.R;
import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.ShowCodeActivity;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.ConflictException;

public class SignUpActivity extends FragmentActivity implements OnClickListener,
        OnItemSelectedListener {
    final static String LOG_TAG = "SignUp";
    EditText usernameEdit, emailEdit, phoneEdit, passwordEdit, codeEdit;
    Spinner countrySpinner;
    int country_code_pos = 0;
    View status_panel;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.sign_up);
        usernameEdit = (EditText) findViewById(R.id.username);
        emailEdit = (EditText) findViewById(R.id.email);
        phoneEdit = (EditText) findViewById(R.id.phone);
        passwordEdit = (EditText) findViewById(R.id.password);
        countrySpinner = (Spinner) findViewById(R.id.country_spinner);
        codeEdit = (EditText) findViewById(R.id.code);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.country_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setOnItemSelectedListener(this);
        View v = findViewById(R.id.register);
        v.setOnClickListener(this);
        v = findViewById(R.id.verify_phone);
        v.setOnClickListener(this);
        v = findViewById(R.id.refresh);
        v.setOnClickListener(this);
        status_panel = findViewById(R.id.status_panel);
        if (Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.sign_up_already_login, Toast.LENGTH_LONG).show();
            updateStatus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_showcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_code:
                String title = getString(R.string.showcode_title, getTitle());
                String[] group = new String[] {
                        "Sign up with username & phone",
                        "Sign up with only email",
                        "Check user input data",
                        "Verify phone code",
                        "Get user's token"
                };
                String[] child = new String[] {
                        "kUserBuilder = KiiUser.builderWithName(username);\r\n"
                                + "kUserBuilder.withPhone(phone);\r\n"
                                + "KiiUser user = builder.build();\r\n"
                                + "user.setCountry(\"US\")\r\n"
                                + "try {\r\n"
                                + "    user.register(password);\r\n"
                                + "} catch (Exception e) {\r\n"
                                + "}",
                        "Builder builder = KiiUser.builderWithEmail(email);\r\n"
                                + "KiiUser user = builder.build();\r\n"
                                + "try {\r\n"
                                + "    user.register(password);\r\n"
                                + "} catch (Exception e) {\r\n"
                                + "}",
                        "//All return boolean\r\n"
                                + "KiiUser.isValidUserName(username);\r\n"
                                + "KiiUser.isValidEmail(email);\r\n"
                                + "KiiUser.isValidPhone(phone);\r\n"
                                + "KiiUser.isValidPassword(password);",
                        "KiiUser user = KiiUser.getCurrentUser();\r\n"
                                + "try {\r\n"
                                + "    user.verifyPhone(code);\r\n"
                                + "} catch (Exception e) {\r\n"
                                + "}",
                        "KiiUser.getCurrentUser().getAccessToken();"
                };
                Intent intent = new Intent(this, ShowCodeActivity.class);
                intent.putExtra(ShowCodeActivity.EXTRA_TITLE, title);
                intent.putExtra(ShowCodeActivity.EXTRA_GROUPS, group);
                intent.putExtra(ShowCodeActivity.EXTRA_CHILD, child);
                startActivity(intent);
                break;
        }
        return true;
    }

    protected void updateStatus() {
        KiiUser user = KiiUser.getCurrentUser();
        if (user == null) {
            return;
        }
        status_panel.setVisibility(View.VISIBLE);
        TextView tv = (TextView) status_panel.findViewById(R.id.email_status);
        tv.setText(user.isEmailVerified() ? R.string.email_verified : R.string.email_not_verified);
        tv = (TextView) status_panel.findViewById(R.id.phone_status);
        tv.setText(user.isPhoneVerified() ? R.string.phone_verified : R.string.phone_not_verified);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                checkAndRegister();
                break;
            case R.id.verify_phone:
                new VerifyPhoneTask().execute();
                break;
            case R.id.refresh:
                new RefreshTask().execute();
                break;
        }
    }

    protected void checkAndRegister() {
        KiiUser.Builder kUserBuilder = null;
        String username = usernameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String phone = phoneEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if (!TextUtils.isEmpty(username)) {
            if (!KiiUser.isValidUserName(username)) {
                Toast.makeText(this, R.string.username_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            kUserBuilder = KiiUser.builderWithName(username);
        }
        if (!TextUtils.isEmpty(email)) {
            if (!KiiUser.isValidEmail(email)) {
                Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            if (kUserBuilder == null)
                kUserBuilder = KiiUser.builderWithEmail(email);
            else
                kUserBuilder.withEmail(email);
        }
        if (!TextUtils.isEmpty(phone)) {
            if (!KiiUser.isValidPhone(phone)) {
                Toast.makeText(this, R.string.phone_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            if (kUserBuilder == null)
                kUserBuilder = KiiUser.builderWithPhone(phone);
            else
                kUserBuilder.withPhone(phone);
        }
        if (kUserBuilder == null) {
            Toast.makeText(this, R.string.reg_at_least_one, Toast.LENGTH_LONG).show();
            return;
        }
        if (!KiiUser.isValidPassword(password)) {
            Toast.makeText(this, R.string.password_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        KiiUser user = kUserBuilder.build();
        String[] country_codes = getResources().getStringArray(R.array.country_codes);
        user.setCountry(country_codes[country_code_pos]);
        new RegTask(user, password).execute();
    }

    class RegTask extends AsyncTask<Void, Void, Void> {
        KiiUser mUser;
        String mPassword;
        String err_msg = null;
        ProgressDialogFragment dialog = null;
        String token = null;

        public RegTask(KiiUser user, String password) {
            mUser = user;
            mPassword = password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mUser.register(mPassword);
                mUser.refresh();
                token = mUser.getAccessToken();
            } catch (ConflictException e) {
                err_msg = "Register failed since the username/email/phone already exsits.";
            } catch (Exception e) {
                err_msg = "Register failed due to server/network error";
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.registering));
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (err_msg != null) {
                Toast.makeText(SignUpActivity.this, err_msg, Toast.LENGTH_LONG).show();
            } else {
                Utils.saveToken(getApplicationContext(), token);
                Toast.makeText(SignUpActivity.this, "Register successfully", Toast.LENGTH_LONG)
                        .show();
                updateStatus();
            }
        }
    }

    class RefreshTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().refresh();
            } catch (Exception e) {
                Log.e(LOG_TAG, Log.getStackTraceString(e));
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
            updateStatus();
        }
    }

    class VerifyPhoneTask extends AsyncTask<Void, Void, Void> {
        String err_msg = null;
        ProgressDialogFragment dialog = null;
        String code = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().verifyPhone(code);
                KiiUser.getCurrentUser().refresh();
            } catch (Exception e) {
                err_msg = "Verify code failed";
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.verifying_code));
            dialog.show(getSupportFragmentManager(), "dialog");
            code = codeEdit.getText().toString();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (err_msg != null) {
                Toast.makeText(SignUpActivity.this, err_msg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SignUpActivity.this, "Verify code  successfully", Toast.LENGTH_LONG)
                        .show();
                updateStatus();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long lid) {
        country_code_pos = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}


package com.kii.api_demos.user_management;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.api_demos.AsyncTaskWithProgress;
import com.kii.api_demos.ProgressDialogFragment;
import com.kii.api_demos.R;
import com.kii.api_demos.Utils;
import com.kii.cloud.storage.KiiUser;

public class SignInActivity extends FragmentActivity implements OnCheckedChangeListener,
        OnItemSelectedListener, OnClickListener {

    String token = null;
    TextView tokenView = null, userView = null;
    Button loginTokenButton;
    RadioGroup radioGroup;
    Spinner countrySpinner;
    int country_code_pos = 0;
    EditText usernameEdit, passwordEdit;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.sign_in);
        if (Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.sign_in_already_login, Toast.LENGTH_LONG).show();
        }
        userView = (TextView) findViewById(R.id.user);
        tokenView = (TextView) findViewById(R.id.token);
        loginTokenButton = (Button) findViewById(R.id.login_with_token);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        countrySpinner = (Spinner) findViewById(R.id.country_spinner);
        usernameEdit = (EditText) findViewById(R.id.username);
        passwordEdit = (EditText) findViewById(R.id.password);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.country_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setOnItemSelectedListener(this);
        loginTokenButton.setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
        SharedPreferences prefs = getSharedPreferences(Utils.PREFS_NAME,
                Context.MODE_PRIVATE);
        token = prefs.getString(Utils.KEY_TOEKN, "");
        Log.d("Token", token);
        updateInfoViews();
    }

    void updateInfoViews() {
        if (Utils.isCurrentLogined()) {
            userView.setText(getString(R.string.current_user, KiiUser.getCurrentUser()
                    .getUsername()));
        } else {
            userView.setText(getString(R.string.not_login));
        }
        String text = token;
        if (TextUtils.isEmpty(token)) {
            text = getString(R.string.no_token);
            loginTokenButton.setEnabled(false);
        } else {
            loginTokenButton.setEnabled(true);
        }
        tokenView.setText(getString(R.string.token_label, text));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.login_with_phone:
                countrySpinner.setVisibility(View.VISIBLE);
                break;
            default:
                countrySpinner.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
        country_code_pos = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_with_token:
                new LoginWithTokenTask(this).execute();
                break;
            case R.id.login:
                new LoginTask(this).execute();
                break;
        }
    }

    class LoginWithTokenTask extends AsyncTaskWithProgress {
        public LoginWithTokenTask(FragmentActivity activity) {
            super(activity);
        }

        boolean succ = true;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.loginWithToken(token);
            } catch (Exception e) {
                succ = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(SignInActivity.this, succ ? R.string.login_succ : R.string.login_failed,
                    Toast.LENGTH_SHORT).show();
            updateInfoViews();
        }
    }

    class LoginTask extends AsyncTaskWithProgress {
        public LoginTask(FragmentActivity activity) {
            super(activity);
        }

        boolean succ = false;

        @Override
        protected Void doInBackground(Void... params) {
            KiiUser mUser = null;
            try {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.login_with_username:
                        mUser = KiiUser.logIn(usernameEdit.getText().toString(), passwordEdit
                                .getText()
                                .toString());
                        break;
                    case R.id.login_with_phone:
                        String[] country_codes = getResources().getStringArray(
                                R.array.country_codes);
                        mUser = KiiUser.logInWithLocalPhone(usernameEdit.getText().toString(),
                                passwordEdit
                                        .getText()
                                        .toString(), country_codes[country_code_pos]);
                        break;
                }
                if (mUser != null) {
                    mUser.refresh();
                    token = mUser.getAccessToken();
                    Utils.saveToken(getApplicationContext(), token);
                    succ = true;
                }
            } catch (Exception e) {
                succ = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            updateInfoViews();
            Toast.makeText(SignInActivity.this, succ ? R.string.login_succ : R.string.login_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

}

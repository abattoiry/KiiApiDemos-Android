package com.kii.apis.user.signup;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.ConflictException;

/**
 * Fragment for signup page
 */
public class SignupFragment extends Fragment {
    public static SignupFragment newInstance() {
        SignupFragment fragment = new SignupFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    final static String LOG_TAG = "SignUp";

    EditText usernameEdit;
    EditText emailEdit;
    EditText phoneEdit;
    EditText passwordEdit;
    EditText codeEdit;
    Spinner countrySpinner;
    int country_code_pos = 0;
    View status_panel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sign_up, container, false);

        Context context = root.getContext();

        usernameEdit = (EditText) root.findViewById(R.id.username);
        emailEdit = (EditText) root.findViewById(R.id.email);
        phoneEdit = (EditText) root.findViewById(R.id.phone);
        passwordEdit = (EditText) root.findViewById(R.id.password);
        countrySpinner = (Spinner) root.findViewById(R.id.country_spinner);
        codeEdit = (EditText) root.findViewById(R.id.code);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.country_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country_code_pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        View v = root.findViewById(R.id.register);
        v.setOnClickListener(mClickListener);
        v = root.findViewById(R.id.verify_phone);
        v.setOnClickListener(mClickListener);
        v = root.findViewById(R.id.refresh);
        v.setOnClickListener(mClickListener);
        status_panel = root.findViewById(R.id.status_panel);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity == null) { return; }

        if (Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.sign_up_already_login, Toast.LENGTH_LONG).show();
            updateStatus();
        }

        activity.setTitle(R.string.sign_up);
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

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
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
    };

    protected void checkAndRegister() {
        Activity activity = getActivity();
        if (activity == null) { return; }

        KiiUser.Builder kUserBuilder = null;
        String username = usernameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String phone = phoneEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if (!TextUtils.isEmpty(username)) {
            if (!KiiUser.isValidUserName(username)) {
                Toast.makeText(activity, R.string.username_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            kUserBuilder = KiiUser.builderWithName(username);
        }
        if (!TextUtils.isEmpty(email)) {
            if (!KiiUser.isValidEmail(email)) {
                Toast.makeText(activity, R.string.email_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            if (kUserBuilder == null)
                kUserBuilder = KiiUser.builderWithEmail(email);
            else
                kUserBuilder.withEmail(email);
        }
        if (!TextUtils.isEmpty(phone)) {
            if (!KiiUser.isValidPhone(phone)) {
                Toast.makeText(activity, R.string.phone_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            if (kUserBuilder == null)
                kUserBuilder = KiiUser.builderWithPhone(phone);
            else
                kUserBuilder.withPhone(phone);
        }
        if (kUserBuilder == null) {
            Toast.makeText(activity, R.string.reg_at_least_one, Toast.LENGTH_LONG).show();
            return;
        }
        if (!KiiUser.isValidPassword(password)) {
            Toast.makeText(activity, R.string.password_invalid, Toast.LENGTH_LONG).show();
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
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Activity activity = getActivity();
            if (activity == null) { return; }

            if (err_msg != null) {
                Toast.makeText(activity, err_msg, Toast.LENGTH_LONG).show();
            } else {
                Utils.saveToken(activity.getApplicationContext(), token);
                Toast.makeText(activity, "Register successfully", Toast.LENGTH_LONG)
                        .show();
                updateStatus();
            }
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
            dialog.show(getFragmentManager(), "dialog");
            code = codeEdit.getText().toString();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Activity activity = getActivity();
            if (activity == null) { return; }

            if (err_msg != null) {
                Toast.makeText(activity, err_msg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Verify code  successfully", Toast.LENGTH_LONG)
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
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            updateStatus();
        }
    }

}

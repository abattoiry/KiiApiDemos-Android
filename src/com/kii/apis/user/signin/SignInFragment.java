package com.kii.apis.user.signin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;

/**
 * Fragment for Sign in Page
 */
public class SignInFragment extends Fragment {
    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    String token = null;
    TextView tokenView = null;
    TextView userView = null;
    Button loginTokenButton;
    RadioGroup radioGroup;
    Spinner countrySpinner;
    int country_code_pos = 0;
    EditText usernameEdit;
    EditText passwordEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sign_in, container, false);

        Context context = root.getContext();

        userView = (TextView) root.findViewById(R.id.user);
        tokenView = (TextView) root.findViewById(R.id.token);
        loginTokenButton = (Button) root.findViewById(R.id.login_with_token);
        radioGroup = (RadioGroup) root.findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        countrySpinner = (Spinner) root.findViewById(R.id.country_spinner);
        usernameEdit = (EditText) root.findViewById(R.id.username);
        passwordEdit = (EditText) root.findViewById(R.id.password);
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
        loginTokenButton.setOnClickListener(mClickListener);
        root.findViewById(R.id.login).setOnClickListener(mClickListener);
        SharedPreferences prefs = context.getSharedPreferences(Utils.PREFS_NAME,
                Context.MODE_PRIVATE);
        token = prefs.getString(Utils.KEY_TOEKN, "");
        Log.d("Token", token);
        updateInfoViews();

        return root;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity == null) { return; }

        if (Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.sign_in_already_login, Toast.LENGTH_LONG).show();
        }

        activity.setTitle(R.string.sign_in);
    }

    private final RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
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
    };

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentActivity activity = getActivity();
            if (activity == null) { return; }

            switch (v.getId()) {
            case R.id.login_with_token:
                new LoginWithTokenTask(activity).execute();
                break;
            case R.id.login:
                new LoginTask(activity).execute();
                break;
            }
        }
    };

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
            Activity activity = getActivity();
            if (activity == null) { return; }

            Toast.makeText(activity, succ ? R.string.login_succ : R.string.login_failed,
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
            Activity activity = getActivity();
            if (activity == null) { return null; }

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
                    Utils.saveToken(activity.getApplicationContext(), token);
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
            Activity activity = getActivity();
            if (activity == null) { return; }

            updateInfoViews();
            Toast.makeText(activity, succ ? R.string.login_succ : R.string.login_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

}

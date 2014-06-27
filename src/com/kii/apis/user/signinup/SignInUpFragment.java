package com.kii.apis.user.signinup;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.R;
import com.kii.apis.ShowCodeActivity;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;

/**
 * Fragment for Simple Sign In / Up Page
 */
public class SignInUpFragment extends Fragment {
    public static SignInUpFragment newInstance() {
        SignInUpFragment fragment = new SignInUpFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    EditText usernameEdit;
    EditText passwordEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.simple_sign_up_in, container, false);

        usernameEdit = (EditText) root.findViewById(R.id.username);
        passwordEdit = (EditText) root.findViewById(R.id.password);
        View v = root.findViewById(R.id.login);
        v.setOnClickListener(mClickListener);
        v = root.findViewById(R.id.register);
        v.setOnClickListener(mClickListener);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.default_showcode, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = getActivity();
        if (activity == null) { return true; }

        switch (item.getItemId()) {
        case R.id.action_code:
            String title = getString(R.string.showcode_title, activity.getTitle());
            String[] group = new String[] {
                    "Simple sign up",
                    "Simple sign in",
            };
            String[] child = new String[] {
                    "Builder builder = KiiUser.builderWithName(username);\r\n"
                            + "KiiUser user = builder.build();\r\n"
                            + "try {\r\n"
                            + "    user.register(password);\r\n"
                            + "} catch (Exception e) {\r\n"
                            + "}",
                    "KiiUser user = KiiUser.logIn(username, password);",
            };
            Intent intent = new Intent(activity, ShowCodeActivity.class);
            intent.putExtra(ShowCodeActivity.EXTRA_TITLE, title);
            intent.putExtra(ShowCodeActivity.EXTRA_GROUPS, group);
            intent.putExtra(ShowCodeActivity.EXTRA_CHILD, child);
            startActivity(intent);
            break;
        }
        return true;
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity == null) { return; }

            String[] text = new String[3];
            text[0] = usernameEdit.getText().toString();
            text[1] = passwordEdit.getText().toString();
            if (!KiiUser.isValidUserName(text[0])) {
                Toast.makeText(activity, "Username is not valid", Toast.LENGTH_LONG).show();
                return;
            }
            if (!KiiUser.isValidPassword(text[1])) {
                Toast.makeText(activity, "Password is not valid", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            switch (v.getId()) {
            case R.id.login:
                text[2] = "login";
                break;
            case R.id.register:
                text[2] = "reg";
                break;
            }
            new LoginOrRegTask().execute(text[0], text[1], text[2]);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        activity.setTitle(R.string.simple_sign_up_in_title);
    }

    class LoginOrRegTask extends AsyncTask<String, Void, Void> {

        ProgressDialogFragment dialog = null;
        String token = null;
        KiiUser user = null;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Activity activity = getActivity();
            if (activity == null) { return; }

            if (TextUtils.isEmpty(token)) {
                Toast.makeText(activity,
                        "Reg or login failed, please try again later",
                        Toast.LENGTH_LONG).show();
            } else {
                Utils.saveToken(activity.getApplicationContext(), token);
                Toast.makeText(activity, "Login successful",
                        Toast.LENGTH_LONG).show();

                getFragmentManager().popBackStack();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance("In progress now");
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected Void doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String type = params[2];
            try {
                if (type.equals("reg")) {
                    KiiUser.Builder builder = KiiUser.builderWithName(username);
                    user = builder.build();
                    user.register(password);
                } else {
                    user = KiiUser.logIn(username, password);
                }
                token = user.getAccessToken();
            } catch (Exception e) {
                token = null;
            }
            return null;
        }
    }
}

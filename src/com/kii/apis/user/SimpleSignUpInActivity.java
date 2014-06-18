
package com.kii.apis.user;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.kii.apis.R;
import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.ShowCodeActivity;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.KiiUser.Builder;

public class SimpleSignUpInActivity extends FragmentActivity implements OnClickListener {

    EditText usernameEdit, passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_sign_up_in);
        usernameEdit = (EditText) findViewById(R.id.username);
        passwordEdit = (EditText) findViewById(R.id.password);
        View v = findViewById(R.id.login);
        v.setOnClickListener(this);
        v = findViewById(R.id.register);
        v.setOnClickListener(this);
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
                Intent intent = new Intent(this, ShowCodeActivity.class);
                intent.putExtra(ShowCodeActivity.EXTRA_TITLE, title);
                intent.putExtra(ShowCodeActivity.EXTRA_GROUPS, group);
                intent.putExtra(ShowCodeActivity.EXTRA_CHILD, child);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        String[] text = new String[3];
        text[0] = usernameEdit.getText().toString();
        text[1] = passwordEdit.getText().toString();
        if (!KiiUser.isValidUserName(text[0])) {
            Toast.makeText(this, "Username is not valid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!KiiUser.isValidPassword(text[1])) {
            Toast.makeText(this, "Password is not valid", Toast.LENGTH_LONG)
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

    class LoginOrRegTask extends AsyncTask<String, Void, Void> {

        ProgressDialogFragment dialog = null;
        String token = null;
        KiiUser user = null;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (TextUtils.isEmpty(token)) {
                Toast.makeText(SimpleSignUpInActivity.this,
                        "Reg or login failed, please try again later",
                        Toast.LENGTH_LONG).show();
            } else {
                Utils.saveToken(getApplicationContext(), token);
                Toast.makeText(SimpleSignUpInActivity.this, "Login successful",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance("In progress now");
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected Void doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String type = params[2];
            try {
                if (type.equals("reg")) {
                    Builder builder = KiiUser.builderWithName(username);
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

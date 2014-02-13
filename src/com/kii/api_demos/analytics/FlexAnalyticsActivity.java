
package com.kii.api_demos.analytics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kii.api_demos.ProgressDialogFragment;
import com.kii.api_demos.R;
import com.kii.api_demos.Utils;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

import java.io.IOException;

public class FlexAnalyticsActivity extends FragmentActivity implements OnClickListener {

    public static final String KEY_SCORE = "Score";
    public static final String KEY_VERSION = "AppVersion";
    public static final String KEY_LEVEL = "Level";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_BUCKET_NAME = "score";
    KiiObject scoreObject = null;

    EditText scoreField, versionField;
    Spinner levelSpinner;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flex_analytics);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        scoreField = (EditText) findViewById(R.id.score);
        versionField = (EditText) findViewById(R.id.ver);
        levelSpinner = (Spinner) findViewById(R.id.level);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mLevelStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);
        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
    }

    private static final String[] mLevelStrings = {
            "Easy", "Normal", "Hard"
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                saveScore();
                break;
        }
    }

    void saveScore() {
        int score = Integer.parseInt(scoreField.getText().toString());
        if (score < 0 || score > 100) {
            Toast.makeText(this, "Score need to be in [0,100]", Toast.LENGTH_SHORT).show();
            return;
        }
        int appVer = Integer.parseInt(versionField.getText().toString());
        if (appVer < 0) {
            Toast.makeText(this, "AppVersion need to be larger than 0", Toast.LENGTH_SHORT).show();
            return;
        }
        String level = (String) levelSpinner.getSelectedItem();
        scoreObject = Kii.bucket(KEY_BUCKET_NAME).object();
        scoreObject.set(KEY_SCORE, score);
        scoreObject.set(KEY_VERSION, appVer);
        scoreObject.set(KEY_LEVEL, level);
        new SaveScoreTask().execute();
    }

    class SaveScoreTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;
        boolean succ = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance("Saving high score…");
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (succ) {
                Toast.makeText(FlexAnalyticsActivity.this,
                        "Save successfully", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(FlexAnalyticsActivity.this,
                        "Save failed", Toast.LENGTH_LONG)
                        .show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                scoreObject.set("username", KiiUser.getCurrentUser().getUsername());
                scoreObject.save();
                succ = true;
            } catch (BadRequestException e) {
            } catch (ConflictException e) {
            } catch (ForbiddenException e) {
            } catch (NotFoundException e) {
            } catch (UnauthorizedException e) {
            } catch (UndefinedException e) {
            } catch (IOException e) {
            }
            return null;
        }

    }

}

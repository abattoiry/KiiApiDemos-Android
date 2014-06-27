
package com.kii.apis.analytics;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.Constants;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.analytics.KiiAnalyticsException;
import com.kii.cloud.analytics.aggregationresult.GroupedResult;
import com.kii.cloud.analytics.aggregationresult.GroupedSnapShot;
import com.kii.cloud.analytics.aggregationresult.ResultQuery;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class FlexAnalyticsActivity extends FragmentActivity implements OnClickListener {

    public static final String KEY_SCORE = "Score";
    public static final String KEY_VERSION = "AppVersion";
    public static final String KEY_LEVEL = "Level";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_BUCKET_NAME = "score";
    KiiObject scoreObject = null;

    EditText scoreField, versionField;
    Spinner levelSpinner;

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
        findViewById(R.id.save_button).setOnClickListener(this);
        findViewById(R.id.show_button).setOnClickListener(this);
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
            case R.id.show_button:
                new FetchResultsTask(this).execute();
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
        new SaveScoreTask(this).execute();
    }

    class SaveScoreTask extends AsyncTaskWithProgress {
        public SaveScoreTask(FragmentActivity activity) {
            super(activity);
        }

        boolean succ = false;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
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

    class FetchResultsTask extends AsyncTaskWithProgress {
        String resultData = null;

        public FetchResultsTask(FragmentActivity activity) {
            super(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ResultQuery query = ResultQuery.builderWithGroupingKey("UserLevel")
                    .build();

            try {
                GroupedResult result = KiiAnalytics.getResult(Constants.ANALYTICS_AVG_SCORES_ID,
                        query);
                List<GroupedSnapShot> snapshots = result.getSnapShots();
                StringBuilder sb = new StringBuilder();
                for (GroupedSnapShot gs : snapshots) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(gs.getName());
                    sb.append("        ");
                    JSONArray data = gs.getData();
                    sb.append(data.getDouble(data.length() - 1));
                }
                resultData = sb.toString();
            } catch (KiiAnalyticsException e) {
            } catch (JSONException jsone) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            AlertDialog.Builder ab = new AlertDialog.Builder(mActivity);
            ab.setTitle("Average Scores");
            ab.setMessage(resultData);
            ab.setPositiveButton(android.R.string.ok, null);
            ab.show();
        }

    }

}

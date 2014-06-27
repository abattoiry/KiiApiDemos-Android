
package com.kii.apis.abtest;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kii.apis.Constants;
import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.abtesting.ExperimentNotAppliedException;
import com.kii.cloud.abtesting.KiiExperiment;
import com.kii.cloud.abtesting.Variation;
import com.kii.cloud.analytics.KiiEvent;
import com.kii.cloud.storage.exception.app.AppException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ABTestsActivity extends FragmentActivity implements OnClickListener {

    KiiExperiment experiment = null;
    Variation va = null;
    String buttonColor = "green";
    String buttonLabel = "Get Started Now!";
    Button button = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abtests);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        new GetVariableTask().execute();
    }

    class GetVariableTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                experiment = KiiExperiment.getByID(Constants.ABTestingID);
                va = experiment.getVariationByName("A");
                va = experiment.getAppliedVariation();
            } catch (AppException e) {
            } catch (IOException e) {
            } catch (ExperimentNotAppliedException e) {
            }
            JSONObject test = va.getVariableSet();
            try {
                buttonColor = test.getString("buttonColor");
                buttonLabel = test.getString("buttonLabel");
            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.in_progressing));
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if ("red".equals(buttonColor)) {
                button.setBackgroundColor(Color.RED);
            } else if ("green".equals(buttonColor)) {
                button.setBackgroundColor(Color.GREEN);
            }
            button.setText(buttonLabel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (va != null) {
            KiiEvent viewEvent = va.eventForConversion(getApplicationContext(),
                    "eventViewed");
            try {
                viewEvent.push();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button: {
                if (va != null) {
                    KiiEvent clickedEvent = va.eventForConversion(getApplicationContext(),
                            "eventClicked");
                    try {
                        clickedEvent.push();
                    } catch (IOException e) {
                    }
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://documentation.kii.com/en/guides/ab-testing/"));
                startActivity(intent);
            }
                break;
        }
    }
}

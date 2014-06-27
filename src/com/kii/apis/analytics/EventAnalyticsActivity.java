
package com.kii.apis.analytics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.Constants;
import com.kii.apis.R;
import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.analytics.KiiAnalyticsException;
import com.kii.cloud.analytics.KiiEvent;
import com.kii.cloud.analytics.aggregationresult.DateRange;
import com.kii.cloud.analytics.aggregationresult.GroupedResult;
import com.kii.cloud.analytics.aggregationresult.GroupedSnapShot;
import com.kii.cloud.analytics.aggregationresult.ResultQuery;
import com.kii.cloud.analytics.aggregationresult.SimpleDate;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventAnalyticsActivity extends FragmentActivity {

    ArrayAdapter<String> mAdapter = null;
    ListView mList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_analytics);
        mList = (ListView) findViewById(android.R.id.list);
        // Below code shows how to send one event
        KiiEvent event = KiiAnalytics.event("LaunchEventAnalytics");
        try {
            event.push();
        } catch (IOException e) {
        }
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mList.setAdapter(mAdapter);
        fetchRecentWeekData();
    }

    void fetchRecentWeekData() {
        new AsyncTaskWithProgress(this) {
            ArrayList<String> results = new ArrayList<String>();

            @Override
            protected Void doInBackground(Void... params) {
                Calendar calendar = Calendar.getInstance();
                SimpleDate end = new SimpleDate(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                calendar.roll(Calendar.WEEK_OF_YEAR, -1);
                SimpleDate start = new SimpleDate(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                DateRange dateRange = new DateRange(start, end);

                ResultQuery query = ResultQuery.builderWithDateRange(dateRange).build();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    // Gets the snapshots
                    GroupedResult result = KiiAnalytics.getResult(Constants.ANALYTICS_EVENT_ID,
                            query);
                    // Check the snapshots
                    List<GroupedSnapShot> snapshots = result.getSnapShots();
                    if (snapshots.size() > 0) {
                        GroupedSnapShot gs = snapshots.get(0);
                        long time = gs.getPointStart();
                        JSONArray dataArray = gs.getData();
                        for (int i = 0; i < dataArray.length(); i++) {
                            int count = 0;
                            try {
                                count = dataArray.getInt(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                has_exception = true;
                            }
                            String s = format.format(new Date(time)) + "    " + count;
                            results.add(s);
                            time += gs.getPointInterval();
                        }
                    }
                } catch (KiiAnalyticsException e) {
                    // Please check KiiAnalyticsException to see what went wrong
                    has_exception = true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (results.size() == 0) {
                    Toast.makeText(mActivity, "Cannot get results", Toast.LENGTH_SHORT).show();
                } else {
                    for (String s : results) {
                        mAdapter.add(s);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

        }.execute();
    }
}


package com.kii.apis.geolocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kii.apis.Constants;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.GeoPoint;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class QueryByGeoBoxActivity extends Activity {

    List<String> geo_items;

    private EditText mLatText;
    private EditText mLongText;

    private ListView mListView;
    private Button queryButton;
    ArrayAdapter<String> mAdapter;
    ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_geobox);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        geo_items = new ArrayList<String>();
        initView();
        setListen();
    }

    void initView()
    {

        mLatText = (EditText) findViewById(R.id.LatText);

        mLongText = (EditText) findViewById(R.id.LongText);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            mLatText.setText(Double.toString(location.getLatitude()));
            mLongText.setText(Double.toString(location.getLongitude()));
        }
        queryButton = (Button) findViewById(R.id.queryButton);
        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, geo_items);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Exit");
        dialog.setMessage("Exit?");
        dialog.setCanceledOnTouchOutside(true);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Exit",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        finish();

                    }
                });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        dialog.show();
    }

    private void setListen() {
        queryButton.setOnClickListener(query_geobox_fn);
    }

    private Button.OnClickListener query_geobox_fn = new Button.OnClickListener()
    {

        public void onClick(View v)
        {
            query_geobox();

        }
    };

    protected void query_geobox() {

        double lat = 0.00;
        double lon = 0.00;
        if (mLatText.getText().length() == 0)
        {
            Toast.makeText(QueryByGeoBoxActivity.this,
                    "cur latitude is null,pelase input latitude", Toast.LENGTH_LONG).show();
            return;
        }
        if (mLongText.getText().length() == 0)
        {
            Toast.makeText(QueryByGeoBoxActivity.this,
                    "cur longitude is null,pelase input longitude", Toast.LENGTH_LONG).show();
            return;
        }
        geo_items.clear();
        mAdapter.notifyDataSetChanged();

        new QueryWithGeoBoxTask().execute();
    }

    class QueryWithGeoBoxTask extends AsyncTask<Void, Void, Void> {

        boolean saveSucc = false;
        List<KiiObject> objLists = null;

        @Override
        protected Void doInBackground(Void... params) {

            // Prepare the target Bucket to be queried.
            KiiBucket bucket = KiiUser.getCurrentUser().bucket(Constants.GEOLOCATION_BUCKET);

            try {
                // Define GeoBox with NorthEast and SouthWest points.
                double lat = Double.parseDouble(mLatText.getText().toString());
                double lon = Double.parseDouble(mLongText.getText().toString());

                GeoPoint sw = new GeoPoint(lat - 0.0004, lon - 0.0004);
                GeoPoint ne = new GeoPoint(lat + 0.0004, lon + 0.0004);
                KiiClause clause = KiiClause.geoBox(Constants.GEOLOCATION_POI, ne, sw);
                KiiQuery query = new KiiQuery(clause);

                // Execute GeoBox query.
                KiiQueryResult<KiiObject> result = bucket.query(query);
                objLists = result.getResult();

                if ((objLists != null) && (objLists.size() > 0))
                {
                    saveSucc = true;
                }

                // Parsing the result will follow...

            } catch (IOException ioe) {
                // check fail reason
            } catch (AppException e) {
                // check fail reason.
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(QueryByGeoBoxActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("query with geobox...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (saveSucc) {
                for (KiiObject obj : objLists) {
                    GeoPoint n;
                    String mark;
                    Toast.makeText(QueryByGeoBoxActivity.this, "query with geobox success!",
                            Toast.LENGTH_LONG).show();
                    n = obj.getGeoPoint(Constants.GEOLOCATION_POI);
                    mark = obj.getString(Constants.GEOLOCATION_TITLE);
                    String lat_s = Double.toString(n.getLatitude());
                    String lon_s = Double.toString(n.getLongitude());
                    String latandlon = "title=" + mark + "," + "lat=" + lat_s + "," + "lon="
                            + lon_s;

                    geo_items.add(latandlon);
                }

            } else {
                geo_items.add("no match Poi!");
                Toast.makeText(QueryByGeoBoxActivity.this,
                        "query with geobox fail!,please add GeoPoint", Toast.LENGTH_LONG).show();
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}

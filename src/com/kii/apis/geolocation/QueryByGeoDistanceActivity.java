
package com.kii.apis.geolocation;

import android.app.Activity;
import android.app.ProgressDialog;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryByGeoDistanceActivity extends Activity {

    List<String> geo_items;

    private EditText mLatText;
    private EditText mLongText;
    private EditText mDistanceText;

    private ListView mListView;
    private Button queryButton;
    ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_geodistance);
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

        mDistanceText = (EditText) findViewById(R.id.MarkText);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            mLatText.setText(Double.toString(location.getLatitude()));
            mLongText.setText(Double.toString(location.getLongitude()));
        }
        mDistanceText.setText("3000");

        queryButton = (Button) findViewById(R.id.queryButton);
        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, geo_items);
        mListView.setAdapter(mAdapter);
        // adapter.notifyDataSetChanged();

    }

    private void setListen() {
        queryButton.setOnClickListener(query_geodistance_fn);
    }

    private Button.OnClickListener query_geodistance_fn = new Button.OnClickListener()
    {

        public void onClick(View v)
        {
            query_geodistance();

        }
    };

    protected void query_geodistance() {
        //
        // KiiObject object = null;
        // GeoPoint point1 ;
        // double lat=0.00;
        // double lon=0.00;
        // object = Constants.kUser.bucket("Geopoint").object();
        // lat = Constants.latitude;
        // lon = Constants.longitude;
        //
        // String lat_s=Double.toString(lat);
        // String lon_s=Double.toString(lon);
        // String latandlon="lat="+lat_s+","+"lon="+lon_s;
        //
        // geo_items.add(latandlon);
        // mAdapter.notifyDataSetChanged();
        //
        // point1 = new GeoPoint(lat, lon);
        // // set the point to KiiObject
        // object.set("location1", point1);
        // object.set(Constants.GEOLOCATION_TITLE, title);
        // object.set("content", content);
        double lat = 0.00;
        double lon = 0.00;
        if (mLatText.getText().length() == 0)
        {
            Toast.makeText(QueryByGeoDistanceActivity.this,
                    "cur latitude is null,pelase input latitude", Toast.LENGTH_LONG).show();
            return;
        }
        if (mLongText.getText().length() == 0)
        {
            Toast.makeText(QueryByGeoDistanceActivity.this,
                    "cur longitude is null,pelase input longitude", Toast.LENGTH_LONG).show();
            return;
        }

        if (mDistanceText.getText().length() == 0)
        {
            Toast.makeText(QueryByGeoDistanceActivity.this,
                    "cur distance is null,pelase input distance", Toast.LENGTH_LONG).show();
            return;
        }

        geo_items.clear();
        mAdapter.notifyDataSetChanged();

        new QueryWithGeoBoxTask().execute();
    }

    class QueryWithGeoBoxTask extends AsyncTask<Void, Void, Void> {

        boolean saveSucc = false;
        List<KiiObject> objLists = null;
        double distance;

        @Override
        protected Void doInBackground(Void... params) {

            // Prepare the target Bucket to be queried.
            KiiBucket bucket = KiiUser.getCurrentUser().bucket(Constants.GEOLOCATION_BUCKET);

            try {

                double lat = Double.parseDouble(mLatText.getText().toString());
                double lon = Double.parseDouble(mLongText.getText().toString());
                double query_distance = Double.parseDouble(mDistanceText.getText().toString());
                // Define GeoDistance 1
                String distanceField1 = "distance_from_center1";
                GeoPoint center1 = new GeoPoint(lat - 0.0004, lon - 0.0004);
                KiiClause clause1 = KiiClause.geoDistance(Constants.GEOLOCATION_POI, center1,
                        query_distance, distanceField1);

                // Define GeoDistance 2
                String distanceField2 = "distance_from_center2";
                GeoPoint center2 = new GeoPoint(lat + 0.0004, lon + 0.0004);
                KiiClause clause2 = KiiClause.geoDistance(Constants.GEOLOCATION_POI, center2,
                        query_distance,
                        distanceField2);

                // Create a query instance and set the sorting order.
                KiiQuery query = new KiiQuery(KiiClause.and(clause1, clause2));
                String sortKey = "_calculated." + distanceField1;
                query.sortByAsc(sortKey);

                // Execute GeoDistance query.
                KiiQueryResult<KiiObject> result = bucket.query(query);

                // Parsing the result.
                // (In this example, we just fetch the first Object).
                objLists = result.getResult();

                if ((objLists != null) && (objLists.size() > 0))
                {
                    saveSucc = true;
                }
            } catch (IOException ioe) {
                // check fail reason
            } catch (AppException e) {
                // check fail reason.
            }
            return null;
        }

        ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(QueryByGeoDistanceActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("query with geodistance...");
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
                    // Toast.makeText(QueryWithGeoDistanceActivity.this,
                    // "The Distance=" + Double.toString(distance),
                    // Toast.LENGTH_LONG).show();
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
                Toast.makeText(QueryByGeoDistanceActivity.this,
                        "query with geodistance fail!,please add GeoPoint", Toast.LENGTH_LONG)
                        .show();
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}

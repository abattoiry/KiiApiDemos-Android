
package com.kii.apis.geolocation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kii.apis.Constants;
import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.GeoPoint;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.util.List;

public class AddPOIActivity extends FragmentActivity implements OnItemClickListener {

    private EditText mLatText;
    private EditText mLongText;
    private EditText mMarkText;

    private ListView mListView;
    private Button addButton;
    ArrayAdapter<POI> mAdapter;
    POI curPOI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_geopoint);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        initView();
    }

    void initView()
    {
        mLatText = (EditText) findViewById(R.id.LatText);
        mLongText = (EditText) findViewById(R.id.LongText);
        mMarkText = (EditText) findViewById(R.id.title);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            mLatText.setText(Double.toString(location.getLatitude()));
            mLongText.setText(Double.toString(location.getLongitude()));
        }
        addButton = (Button) findViewById(R.id.addbutton);
        addButton.setOnClickListener(add_geopoint_fn);
        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new ArrayAdapter<POI>(this,
                android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        new RefreshTask().execute();
    }

    private Button.OnClickListener add_geopoint_fn = new Button.OnClickListener()
    {
        public void onClick(View v)
        {
            add_geopoint();
        }
    };

    protected void add_geopoint() {

        KiiObject object = null;
        GeoPoint point1;
        double lat = 0.00;
        double lon = 0.00;

        try {
            lat = Double.parseDouble(mLatText.getText().toString());
            lon = Double.parseDouble(mLongText.getText().toString());
        } catch (Exception e) {
            Toast.makeText(this, "Please check your input", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = mMarkText.getText().toString();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Title cannot be null", Toast.LENGTH_SHORT).show();
            return;
        }
        object = KiiUser.getCurrentUser().bucket(Constants.GEOLOCATION_BUCKET).object();
        point1 = new GeoPoint(lat, lon);
        object.set(Constants.GEOLOCATION_POI, point1);
        object.set(Constants.GEOLOCATION_TITLE, title);
        new SaveTask().execute(object);
    }

    class SaveTask extends AsyncTask<KiiObject, Void, Void> {
        ProgressDialogFragment dialog = null;
        boolean saveSucc = false;

        @Override
        protected Void doInBackground(KiiObject... params) {
            try {
                params[0].save();
                saveSucc = true;
            } catch (Exception e) {
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
            if (saveSucc) {
                Toast.makeText(AddPOIActivity.this, "save success!", Toast.LENGTH_LONG).show();
                new RefreshTask().execute();
            } else {
                Toast.makeText(AddPOIActivity.this, "save fail!", Toast.LENGTH_LONG).show();
            }
        }
    }

    class RefreshTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;
        List<KiiObject> objLists = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiQuery all_query = new KiiQuery();
                KiiQueryResult<KiiObject> result = KiiUser.getCurrentUser()
                        .bucket(Constants.GEOLOCATION_BUCKET)
                        .query(all_query);
                objLists = result.getResult();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            mAdapter.clear();
            if (objLists == null) {
                Toast.makeText(AddPOIActivity.this, "Fetch data failed", Toast.LENGTH_LONG).show();
                return;
            }
            for (KiiObject obj : objLists) {
                POI p = new POI();
                p.title = obj.getString(Constants.GEOLOCATION_TITLE);
                p.location = obj.getGeoPoint(Constants.GEOLOCATION_POI);
                p.uri = obj.toUri();
                mAdapter.add(p);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    class DeleteTask extends AsyncTask<KiiObject, Void, Void> {

        boolean delSucc = false;

        @Override
        protected Void doInBackground(KiiObject... params) {
            try {
                params[0].delete();
                delSucc = true;
            } catch (Exception e) {
            }
            return null;
        }

        ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AddPOIActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("Deleting POI…");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (delSucc) {
                Toast.makeText(AddPOIActivity.this, "Deleted successfully", Toast.LENGTH_LONG)
                        .show();
                new RefreshTask().execute();
            } else {
                Toast.makeText(AddPOIActivity.this, "Deleted failed, please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        curPOI = mAdapter.getItem(position);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("Warning");
        ab.setMessage("Are you sure to delete POI item?");
        ab.setNegativeButton(android.R.string.cancel, null);
        ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                KiiObject object = KiiObject.createByUri(curPOI.uri);
                new DeleteTask().execute(object);
            }
        });
        ab.show();
    }
}

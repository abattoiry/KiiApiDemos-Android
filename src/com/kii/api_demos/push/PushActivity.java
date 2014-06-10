
package com.kii.api_demos.push;

import android.os.Bundle;
import android.widget.Toast;

import com.kii.api_demos.R;
import com.kii.api_demos.Utils;

public class PushActivity extends PushBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.push);
        registerGCM();
    }
}

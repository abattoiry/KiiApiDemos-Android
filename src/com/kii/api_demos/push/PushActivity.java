
package com.kii.api_demos.push;

import android.os.Bundle;

public class PushActivity extends PushBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerGCM();
    }
}

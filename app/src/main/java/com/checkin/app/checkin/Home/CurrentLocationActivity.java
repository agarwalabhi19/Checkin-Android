package com.checkin.app.checkin.Home;

import android.os.Bundle;

import com.checkin.app.checkin.Misc.BaseActivity;
import com.checkin.app.checkin.R;

import androidx.annotation.Nullable;
import butterknife.ButterKnife;

public class CurrentLocationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        ButterKnife.bind(this);
    }
}
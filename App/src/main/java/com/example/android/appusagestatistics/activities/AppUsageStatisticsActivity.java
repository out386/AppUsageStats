/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.fragments.AppDetailFragment;
import com.example.android.appusagestatistics.fragments.ViewPagerFragment;
import com.example.android.appusagestatistics.services.PopulateDatabaseService;

public class AppUsageStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_statistics);
        if (savedInstanceState == null) {
            changeFragment(ViewPagerFragment.newInstance());
        }

        Intent intent = new Intent(this, PopulateDatabaseService.class);
        stopService(intent);
        startService(intent);
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment instanceof AppDetailFragment)
            transaction.addToBackStack(null);

        transaction.replace(R.id.container, fragment)
                .commit();
    }

    public void showDetail(String appName, int dateOffset) {
        changeFragment(AppDetailFragment.newInstance(appName, dateOffset));
    }
}

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

package com.example.android.appusagestatistics.models;

import android.graphics.drawable.Drawable;

/**
 * Entity class represents usage stats and app icon.
 */
public class CustomUsageEvents {
    public String packageName;
    public long timestamp;
    public String eventType;

    public CustomUsageEvents(String packageName, String eventType, long timestamp) {
        this.packageName = packageName;
        this.timestamp = timestamp;
        this.eventType = eventType;
    }
}

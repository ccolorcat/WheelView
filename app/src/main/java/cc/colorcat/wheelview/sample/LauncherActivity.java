/*
 * Copyright 2018 cxx
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

package cc.colorcat.wheelview.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        batchClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class<?> clazz;
                switch (v.getId()) {
                    case R.id.btn_time:
                        clazz = TimeActivity.class;
                        break;
                    case R.id.btn_region:
                        clazz = RegionActivity.class;
                        break;
                    case R.id.btn_province:
                        clazz = WheelViewActivity.class;
                        break;
                    default:
                        throw new IllegalStateException();
                }
                startActivity(new Intent(LauncherActivity.this, clazz));
            }
        }, R.id.btn_time, R.id.btn_region, R.id.btn_province);
    }

    private void batchClick(View.OnClickListener listener, @IdRes int... ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(listener);
        }
    }
}

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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cc.colorcat.wheelview.WheelView;

public class WheelViewActivity extends AppCompatActivity {
    private List<Province> mProvince = new ArrayList<>(35);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_view);

        final TextView content = findViewById(R.id.tv_content);

        WheelView wheelView = findViewById(R.id.wheel_view);
        wheelView.addOnItemSelectedListener(new WheelView.SafeOnItemSelectedListener() {
            @Override
            public void onSafeItemSelected(int position) {
                content.setText(mProvince.get(position).getName());
            }
        });
        fillData(wheelView);
    }

    private void fillData(WheelView wheelView) {
        InputStream input = getResources().openRawResource(R.raw.region);
        Gson gson = new Gson();
        List<Province> provinces = gson.fromJson(new InputStreamReader(input), new TypeToken<List<Province>>() {
        }.getType());
        mProvince.addAll(provinces);
        wheelView.updateItemData(mProvince);
    }
}

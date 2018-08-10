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
import java.util.Locale;

import cc.colorcat.wheelview.MultiWheelView;

/**
 * Author: cxx
 * Date: 2018-08-10
 * GitHub: https://github.com/ccolorcat
 */
public class RegionActivity extends AppCompatActivity {
    private List<Province> mProvinces = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_wheel_view);
        final TextView content = findViewById(R.id.tv_content);
        MultiWheelView multiWheelView = findViewById(R.id.multi_wheel_view);
        multiWheelView.addOnSelectedChangeListener(new MultiWheelView.SafeOnSelectedChangeListener() {
            @Override
            public void onSafeSelectedChanged(int... positions) {
                Province province = mProvinces.get(positions[0]);
                City city = province.children().get(positions[1]);
                District district = city.children().get(positions[2]);
                String address = String.format(Locale.getDefault(), "%s%s%s", province.getName(), city.getName(), district.getName());
                content.setText(address);
            }
        });
        fillData(multiWheelView);
    }

    private void fillData(MultiWheelView multiWheelView) {
        InputStream input = getResources().openRawResource(R.raw.region);
        Gson gson = new Gson();
        List<Province> provinces = gson.fromJson(new InputStreamReader(input), new TypeToken<List<Province>>() {
        }.getType());
        mProvinces.addAll(provinces);
        multiWheelView.updateData(mProvinces);
    }
}

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

import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-08-10
 * GitHub: https://github.com/ccolorcat
 */
public class AreaActivity extends AppCompatActivity {
    private List<Province> mProvinces = new ArrayList<>();

    private void loadData() {
        InputStream input = getResources().openRawResource(R.raw.area);
        Gson gson = new Gson();
        List<Province> provinces = gson.fromJson(new InputStreamReader(input), new TypeToken<List<Province>>() {}.getType());
        mProvinces.addAll(provinces);
    }
}

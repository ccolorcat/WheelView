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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.colorcat.wheelview.WheelView;

public class WheelViewActivity extends AppCompatActivity {
    private List<Province> mProvince = new ArrayList<>(35);
    private WheelView mWheelView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_view);

        final TextView content = findViewById(R.id.tv_content);
        mWheelView = findViewById(R.id.wheel_view);
        mWheelView.addOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                if (position != WheelView.INVALID_POSITION) {
                    content.setText(mProvince.get(position).getName());
                } else {
                    content.setText("");
                }
            }
        });
        final EditText number = findViewById(R.id.et_position);
        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int position = Integer.parseInt(number.getText().toString().trim());
                    mWheelView.setSelectedPosition(position);
                } catch (Exception ignore) {
                }
            }
        });
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshData();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
        new RegionLoader(this).load(new RegionLoader.OnLoadedListener() {
            @Override
            public void onLoaded(List<Province> provinces) {
                mProvince.clear();
                mProvince.addAll(provinces);
                mWheelView.updateData(mProvince);
            }
        });
    }
}

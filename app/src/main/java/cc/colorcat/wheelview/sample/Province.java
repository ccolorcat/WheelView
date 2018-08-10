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

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cc.colorcat.wheelview.MultiWheelView;


/**
 * Created by cxx on 2018/4/27.
 * xx.ch@outlook.com
 */
public class Province extends Area {
    @SerializedName("children")
    private List<City> cities;

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    @NonNull
    @Override
    public List<? extends MultiWheelView.Node> children() {
        return cities;
    }

    @Override
    public String toString() {
        return "Province{" +
                "cities=" + cities +
                '}';
    }
}

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

import java.util.ArrayList;
import java.util.List;

import cc.colorcat.wheelview.MultiWheelView;

/**
 * Author: cxx
 * Date: 2018-08-07
 * GitHub: https://github.com/ccolorcat
 */
public class Year implements MultiWheelView.Node {
    final int year;
    final List<Month> months = new ArrayList<>();

    public Year(int year) {
        this.year = year;
        for (int i = 1; i <= 12; i++) {
            months.add(new Month(year, i));
        }
    }

    @Override
    public String contentToString() {
        return Integer.toString(year);
    }

    @Override
    public List<? extends MultiWheelView.Node> children() {
        return months;
    }
}

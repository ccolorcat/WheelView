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

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.colorcat.wheelview.MultiWheelView;

/**
 * Author: cxx
 * Date: 2018-08-07
 * GitHub: https://github.com/ccolorcat
 */
class Month extends TimeNode {
    private static final SparseArray<List<Day>> DAYS = new SparseArray<>(4);

    static {
        final List<Day> DAYS28 = new ArrayList<>(28);
        final List<Day> DAYS29 = new ArrayList<>(29);
        final List<Day> DAYS30 = new ArrayList<>(30);
        final List<Day> DAYS31 = new ArrayList<>(31);
        for (int i = 1; i <= 31; ++i) {
            DAYS31.add(new Day(i));
        }
        DAYS30.addAll(DAYS31.subList(0, 30));
        DAYS29.addAll(DAYS31.subList(0, 29));
        DAYS28.addAll(DAYS31.subList(0, 28));
        DAYS.put(31, DAYS31);
        DAYS.put(30, DAYS30);
        DAYS.put(29, DAYS29);
        DAYS.put(28, DAYS28);
    }

    private final Year year;
    private List<Day> days;

    Month(int value, Year year) {
        super(value);
        this.year = year;
    }


    @Override
    public List<? extends MultiWheelView.Node> children() {
        if (days == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year.value);
            calendar.set(Calendar.MONTH, this.value - 1);
            days = DAYS.get(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            if (days == null) {
                throw new AssertionError();
            }
        }
        return days;
    }
}

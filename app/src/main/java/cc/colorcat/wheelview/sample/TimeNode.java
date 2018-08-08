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

import java.util.Collections;
import java.util.List;

import cc.colorcat.wheelview.MultiWheelView;

/**
 * Author: cxx
 * Date: 2018-08-08
 * GitHub: https://github.com/ccolorcat
 */
class TimeNode implements MultiWheelView.Node {
    protected final int value;

    TimeNode(int value) {
        this.value = value;
    }

    @Override
    public String contentToString() {
        return Integer.toString(value);
    }

    @Override
    public List<? extends MultiWheelView.Node> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}

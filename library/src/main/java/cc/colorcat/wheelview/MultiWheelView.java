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

package cc.colorcat.wheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by cxx on 2018/4/28.
 * xx.ch@outlook.com
 */
public class MultiWheelView extends LinearLayout {
    private int mCount;
    private WheelView[] mViews;
    private Object[] mData;
    private MultiViewBinder[] mBinders;
    private List<OnSelectedChangeListener> mListeners;

    public MultiWheelView(Context context) {
        super(context);
        init(context, null);
    }

    public MultiWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MultiWheelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        super.setOrientation(LinearLayout.HORIZONTAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MultiWheelView);
        @LayoutRes
        int layout = ta.getResourceId(R.styleable.MultiWheelView_wheelViewLayout, R.layout.wheel_view_layout_multi_wheel_view);
        mCount = ta.getInteger(R.styleable.MultiWheelView_wheelViewCount, 1);
        ta.recycle();

        LayoutInflater inflater = LayoutInflater.from(context);
        mData = new Object[mCount];
        mViews = new WheelView[mCount];
        for (int i = 0; i < mCount; ++i) {
            WheelView view = (WheelView) inflater.inflate(layout, this, false);
            mViews[i] = view;
            mData[i] = new ArrayList<Node>();
            view.addOnItemSelectedListener(i != mCount - 1 ? new WheelViewSelectedListener(i) : new LastWheelViewSelectedListener());
            view.setViewBinder(new InnerMultiViewBinder(i));
            addChildView(view);
        }
    }

    private void addChildView(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        super.addView(child, -1, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException();
    }

    public void setItemViewHolderFactories(WheelView.ItemViewHolderFactory... factories) {
        if (factories.length != mCount) {
            throw new IllegalArgumentException("factories.length != wheelViewCount");
        }
        for (int i = 0, size = mViews.length; i < size; ++i) {
            mViews[i].setItemViewHolderFactory(factories[i]);
        }
    }

    public void setMultiViewBinders(MultiViewBinder... binders) {
        if (binders.length != mCount) {
            throw new IllegalArgumentException("binders.length != wheelViewCount");
        }
        mBinders = binders;
    }

    public void addOnSelectedChangeListener(OnSelectedChangeListener listener) {
        if (listener != null) {
            if (mListeners == null) {
                mListeners = new ArrayList<>(4);
            }
            mListeners.add(listener);
        }
    }

    public void removeOnSelectedChangeListener(OnSelectedChangeListener listener) {
        if (listener != null && mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public int[] getSelectedPositions() {
        int[] result = new int[mCount];
        for (int i = 0; i < mCount; ++i) {
            result[i] = mViews[i].getSelectedItemPosition();
        }
        return result;
    }

    public void setData(List<? extends Node> data) {
        List<Node> first = getData(0);
        first.clear();
        first.addAll(data);
        mViews[0].setItemData(first);
    }

    @SuppressWarnings("unchecked")
    private List<Node> getData(int index) {
        return (List<Node>) mData[index];
    }


    private class InnerMultiViewBinder implements WheelView.ViewBinder {
        private final int mIndex;

        private InnerMultiViewBinder(int index) {
            mIndex = index;
        }

        @Override
        public boolean onBind(WheelView.ItemViewHolder holder, int position) {
            Node node = getData(mIndex).get(position);
            if (mBinders == null || mBinders[mIndex] == null || !mBinders[mIndex].onBind(holder, node)) {
                holder.textView.setText(node.contentToString());
            }
            return true;
        }

        @Override
        public void onClear(WheelView.ItemViewHolder holder) {
            if (mBinders != null && mBinders[mIndex] != null) {
                mBinders[mIndex].onClear(holder);
            }
        }
    }


    private class WheelViewSelectedListener implements WheelView.OnItemSelectedListener {
        private final int mIndex;

        private WheelViewSelectedListener(int index) {
            mIndex = index;
        }

        @Override
        public void onItemSelected(int position) {
            List<Node> next = getData(mIndex + 1);
            next.clear();
            next.addAll(position != WheelView.INVALID_POSITION ? getData(mIndex).get(position).children() : Collections.<Node>emptyList());
            mViews[mIndex + 1].setItemData(next);
        }
    }


    private class LastWheelViewSelectedListener implements WheelView.OnItemSelectedListener {
        @Override
        public void onItemSelected(int position) {
            if (mListeners != null) {
                int[] selectedPositions = new int[mCount];
                for (int i = 0; i < mCount; ++i) {
                    selectedPositions[i] = mViews[i].getSelectedItemPosition();
                }
                for (int i = 0, size = mListeners.size(); i < size; ++i) {
                    mListeners.get(i).onSelectedChanged(selectedPositions);
                }
            }
        }
    }


    public interface MultiViewBinder {
        /**
         * 默认的数据绑定仅设置文本，且文本是调用 {@link Node#contentToString()}。
         *
         * @return 如果不希望使用默认的数据绑定返回 {@code true}，否则返回 {@code false}.
         */
        boolean onBind(WheelView.ItemViewHolder holder, Node node);

        /**
         * 清除占位符中的数据
         */
        void onClear(WheelView.ItemViewHolder holder);
    }

    public static abstract class SimpleMultiViewBinder implements MultiViewBinder {
        @Override
        public void onClear(WheelView.ItemViewHolder holder) {
        }
    }


    public interface OnSelectedChangeListener {
        void onSelectedChanged(int... positions);
    }


    public interface Node {
        String contentToString();

        List<? extends Node> children();
    }
}

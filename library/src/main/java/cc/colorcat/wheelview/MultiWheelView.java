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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
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
        int displayCount = ta.getInteger(R.styleable.MultiWheelView_displayCount, -1);
        boolean radicalNotify = ta.getBoolean(R.styleable.MultiWheelView_radicalNotify, true);
        ta.recycle();

        if (mCount < 1) {
            throw new IllegalArgumentException("wheelViewCount must be greater than 0");
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        mData = new Object[mCount];
        mViews = new WheelView[mCount];
        for (int i = 0; i < mCount; ++i) {
            WheelView view = (WheelView) inflater.inflate(layout, this, false);
            if (displayCount > 0) {
                view.setDisplayCount(displayCount);
            }
            mViews[i] = view;
            mData[i] = new ArrayList<Node>();
            if (i != mCount - 1) {
                view.mRadicalNotify = radicalNotify;
                view.registerTargetDataObserver(new WheelViewDataObserver(i));
            } else {
                view.mRadicalNotify = false;
                view.registerTargetDataObserver(new LastWheelViewDataObserver());
            }
            view.setItemAdapter(new InnerItemAdapter<>(i, new SimpleMultiItemAdapter()));
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

    public int getWheelViewCount() {
        return mCount;
    }

    public int getDisplayCount() {
        return mViews[0].getDisplayCount();
    }

    public void setDisplayCount(int displayCount) {
        for (WheelView view : mViews) {
            view.setDisplayCount(displayCount);
        }
    }

    public void setMaskBackground(int index, @ColorInt int color) {
        mViews[index].setMaskBackground(color);
    }

    /**
     * @see MultiWheelView#getWheelViewCount()
     */
    public void setMaskBackground(int index, Drawable drawable) {
        mViews[index].setMaskBackground(drawable);
    }

    /**
     * @see MultiWheelView#getWheelViewCount()
     */
    public void setMaskView(int index, @NonNull View view) {
        mViews[index].setMaskView(view);
    }

    /**
     * @see MultiWheelView#getWheelViewCount()
     */
    public <VH extends WheelView.ItemHolder> void setMultiItemAdapter(int index, @NonNull MultiItemAdapter<VH> adapter) {
        WheelView.checkNotNull(adapter, "adapter == null");
        mViews[index].setItemAdapter(new InnerItemAdapter<>(index, adapter));
    }

    public void addOnSelectedChangeListener(OnSelectedChangeListener listener) {
        if (listener != null) {
            if (mListeners == null) {
                mListeners = new ArrayList<>(4);
            }
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    public void removeOnSelectedChangeListener(OnSelectedChangeListener listener) {
        if (listener != null && mListeners != null) {
            mListeners.remove(listener);
        }
    }

    /**
     * 返回所有 {@link WheelView} 中间项所对应的 position，与 {@link WheelView} 的顺序一一对应。
     * note: 可能存在 {@link WheelView#INVALID_POSITION}
     */
    public int[] getSelectedPositions() {
        int[] result = new int[mCount];
        for (int i = 0; i < mCount; ++i) {
            result[i] = mViews[i].getSelectedItemPosition();
        }
        return result;
    }

    public void updateData(@NonNull List<? extends Node> data) {
        WheelView.checkNotNull(data, "data == null");
        List<Node> first = getData(0);
        first.clear();
        first.addAll(data);
        mViews[0].updateItemData(first);
    }

    @SuppressWarnings("unchecked")
    private List<Node> getData(int index) {
        return (List<Node>) mData[index];
    }


    private class InnerItemAdapter<VH extends WheelView.ItemHolder> extends WheelView.ItemAdapter<VH> {
        private final int mIndex;
        private final MultiItemAdapter<VH> mItemAdapter;

        private InnerItemAdapter(int index, MultiItemAdapter<VH> itemAdapter) {
            mIndex = index;
            mItemAdapter = itemAdapter;
        }

        @NonNull
        @Override
        public VH onCreateItemHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            return mItemAdapter.onCreateItemHolder(parent, itemLayout);
        }

        @Override
        public void onBindItemHolder(@NonNull VH holder, int position) {
            mItemAdapter.onBindItemHolder(holder, getData(mIndex).get(position));
        }
    }


    private class WheelViewDataObserver implements WheelView.TargetDataObserver {
        private final int mIndex;

        private WheelViewDataObserver(int index) {
            mIndex = index;
        }

        @Override
        public void onDataChanged(int position) {
            updateNextItemData(position);
        }

        @Override
        public void onDataInvalid() {
            updateNextItemData(WheelView.INVALID_POSITION);
        }

        private void updateNextItemData(int position) {
            // 更新下一个 WheelView 的数据。
            List<Node> next = getData(mIndex + 1);
            next.clear();
            next.addAll(position != WheelView.INVALID_POSITION ? getData(mIndex).get(position).children() : Collections.<Node>emptyList());
            mViews[mIndex + 1].updateItemData(next);
        }
    }

    private class LastWheelViewDataObserver implements WheelView.TargetDataObserver {
        @Override
        public void onDataChanged(int position) {
            notifySelectedChanged();
        }

        @Override
        public void onDataInvalid() {
            notifySelectedChanged();
        }

        private void notifySelectedChanged() {
            if (mListeners != null) {
                final int[] selectedPositions = getSelectedPositions();
                for (int i = 0, size = mListeners.size(); i < size; ++i) {
                    mListeners.get(i).onSelectedChanged(selectedPositions);
                }
            }
        }
    }


    public static abstract class MultiItemAdapter<VH extends WheelView.ItemHolder> {
        /**
         * @param itemLayout WheelView xml 布局中的 itemLayout, 如未指定则为 android.R.layout.simple_list_item_1
         */
        @NonNull
        public abstract VH onCreateItemHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout);

        /**
         * 数据绑定，默认仅设置文本，且文本是调用 {@link Node#contentToString()} 来转换的。
         */
        public abstract void onBindItemHolder(@NonNull VH holder, Node data);
    }

    private static class SimpleMultiItemAdapter extends MultiItemAdapter<WheelView.ItemHolder> {
        @NonNull
        @Override
        public WheelView.ItemHolder onCreateItemHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            return new WheelView.ItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull WheelView.ItemHolder holder, Node data) {
            holder.textView.setText(data.contentToString());
        }
    }


    public static abstract class SafeOnSelectedChangeListener implements OnSelectedChangeListener {
        @Override
        public void onSelectedChanged(int... positions) {
            for (int position : positions) {
                if (position == WheelView.INVALID_POSITION) {
                    return;
                }
            }
            onSafeSelectedChanged(positions);
        }

        /**
         * @param positions 其中所有的值都不可能为 {@link WheelView#INVALID_POSITION}
         * @see OnSelectedChangeListener
         * @see WheelView#INVALID_POSITION
         */
        public abstract void onSafeSelectedChanged(int... positions);
    }


    public interface OnSelectedChangeListener {
        /**
         * @param positions 长度等于 xml 布局中 wheelViewCount，且顺次对应。
         *                  Note: positions 中可能存在 {@link WheelView#INVALID_POSITION} 值，此种情况说明所对应的数据为空。
         * @see SafeOnSelectedChangeListener
         * @see WheelView#INVALID_POSITION
         */
        void onSelectedChanged(int... positions);
    }


    public interface Node {
        String contentToString();

        @NonNull
        List<? extends Node> children();
    }
}

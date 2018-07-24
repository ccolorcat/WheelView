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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cxx on 2018/4/26.
 * xx.ch@outlook.com
 */
public class WheelView extends FrameLayout {
    public static final int INVALID_POSITION = -1;

    private View mCoverView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mManager;
    private RecyclerView.Adapter mAdapter;
    private List<Object> mData = new ArrayList<>();
    @LayoutRes
    private int mItemLayout;
    private int mDisplayCount; // 同时显示的 item 数量
    private int mPlaceholderCount; // 占位的 item 数量，实际 item 数量 = 用户设置的数据的数量 + 2 * mPlaceholderCount
    private int mItemHeight = Integer.MIN_VALUE;
    private int mSelectedPosition = WheelView.INVALID_POSITION; // 正中间的 item 的 position
    private List<OnItemSelectedListener> mListeners;
    private ViewBinder mBinder;

    public WheelView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public WheelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WheelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WheelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        mDisplayCount = ta.getInteger(R.styleable.WheelView_displayCount, 3);
        if ((mDisplayCount & 1) == 0) {
            if (mDisplayCount > 5) {
                --mDisplayCount;
            } else {
                ++mDisplayCount;
            }
        }
        mPlaceholderCount = mDisplayCount >> 1;
        mItemLayout = ta.getResourceId(R.styleable.WheelView_itemLayout, android.R.layout.simple_list_item_1);
        int coverColor = ta.getColor(R.styleable.WheelView_coverColor, Color.TRANSPARENT);
        ta.recycle();

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        mAdapter = new WheelViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int first = mManager.findFirstCompletelyVisibleItemPosition();
                    if (mSelectedPosition != first) {
                        mSelectedPosition = first;
                        notifyItemSelectedChanged();
                    }
                }
            }
        });
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mRecyclerView);
        super.addView(mRecyclerView);

        if (coverColor != Color.TRANSPARENT) {
            createCoverView(context);
            setBackground(mCoverView, buildCoverBackground(coverColor));
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException();
    }


    public void setCoverBackground(Drawable drawable) {
        if (mCoverView == null) {
            createCoverView(getContext());
        }
        setBackground(mCoverView, drawable);
    }

    private void createCoverView(Context context) {
        mCoverView = new View(context);
        mCoverView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        super.addView(mCoverView);
    }

    public void setItemData(List<?> data) {
        if (data == null) throw new IllegalArgumentException("data == null");
        mData.clear();
        pushPlaceholderData();
        mData.addAll(data);
        pushPlaceholderData();
        mAdapter.notifyDataSetChanged();
        if (data.isEmpty()) {
            mSelectedPosition = WheelView.INVALID_POSITION;
        } else {
            mSelectedPosition = 0;
            mRecyclerView.smoothScrollToPosition(mSelectedPosition);
        }
        notifyItemSelectedChanged();
    }

    private void pushPlaceholderData() {
        for (int i = 0; i < mPlaceholderCount; ++i) {
            mData.add("");
        }
    }

    private void notifyItemSelectedChanged() {
        if (mListeners != null) {
            for (int i = 0, size = mListeners.size(); i < size; ++i) {
                mListeners.get(i).onItemSelected(mSelectedPosition);
            }
        }
    }

    public void setViewBinder(ViewBinder binder) {
        mBinder = binder;
    }

    public void addOnItemSelectedListener(OnItemSelectedListener listener) {
        if (listener != null) {
            if (mListeners == null) {
                mListeners = new ArrayList<>(4);
            }
            mListeners.add(listener);
        }
    }

    public void removeOnItemSelectedListener(OnItemSelectedListener listener) {
        if (listener != null && mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public int getSelectedItemPosition() {
        return mSelectedPosition;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mItemHeight = (bottom - top) / mDisplayCount;
            mAdapter.notifyDataSetChanged();
        }
    }


    private class WheelViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
            return new ItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            if (mBinder == null
                    || position < mPlaceholderCount
                    || position >= mData.size() - mPlaceholderCount
                    || !mBinder.onBind(holder, position - mPlaceholderCount)) {
                holder.textView.setText(String.valueOf(mData.get(position)));
            }
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (mItemHeight != Integer.MIN_VALUE && lp.height != mItemHeight) {
                lp.height = mItemHeight;
                holder.itemView.setLayoutParams(lp);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }


    private static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    private static Drawable buildCoverBackground(@ColorInt int coverColor) {
        int red = Color.red(coverColor);
        int green = Color.green(coverColor);
        int blue = Color.blue(coverColor);
        int alpha = Color.alpha(coverColor);

        int quarter = Color.argb((int) (alpha * 0.75), red, green, blue);
        int center = Color.argb((int) (alpha * 0.1), red, green, blue);
        int[] colors = {coverColor, quarter, center, quarter, coverColor};
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    }


    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }


    public interface ViewBinder {
        /**
         * @return 如果手动处理数据绑定则返回 true，否则返回 false
         */
        boolean onBind(ItemViewHolder holder, int position);
    }
}

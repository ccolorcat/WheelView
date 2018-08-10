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
    public static final String TAG = WheelView.class.getSimpleName();

    public static final int INVALID_POSITION = RecyclerView.NO_POSITION;

    private static final int VIEW_TYPE_PLACE_HOLDER = 0;
    private static final int VIEW_TYPE_DATA = 1;

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
    private List<TargetDataObserver> mObservers;
    boolean mScrollStateIdle = true;
    boolean mUpdateOnIdle = true;

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
        mItemLayout = ta.getResourceId(R.styleable.WheelView_itemLayout, android.R.layout.simple_list_item_1);
        int coverColor = ta.getColor(R.styleable.WheelView_coverColor, Color.TRANSPARENT);
        ta.recycle();

        if (mDisplayCount < 1) {
            throw new IllegalArgumentException("displayCount must be greater than 0");
        }
        if ((mDisplayCount & 1) == 0) {
            if (mDisplayCount > 5) {
                --mDisplayCount;
            } else {
                ++mDisplayCount;
            }
        }
        mPlaceholderCount = mDisplayCount >> 1;

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mScrollStateIdle = newState == RecyclerView.SCROLL_STATE_IDLE;
                if (!mUpdateOnIdle || mScrollStateIdle) {
                    notifyDataStateChanged(false);
                }
            }
        });
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mRecyclerView);
        addChildView(mRecyclerView);

        if (coverColor != Color.TRANSPARENT) {
            addCoverView(context);
            setBackground(mCoverView, buildCoverBackground(coverColor));
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

    public void setCoverBackground(Drawable drawable) {
        if (mCoverView == null) {
            addCoverView(getContext());
        }
        setBackground(mCoverView, drawable);
    }

    public void setCoverView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("view == null");
        }
        if (mCoverView != null) {
            super.removeView(mCoverView);
        }
        addChildView(view);
        mCoverView = view;
    }

    private void addCoverView(Context context) {
        mCoverView = new View(context);
        mCoverView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addChildView(mCoverView);
    }

    public <VH extends ItemHolder> void setItemAdapter(ItemAdapter<VH> adapter) {
        if (adapter == null) {
            throw new NullPointerException("adapter == null");
        }
        setRecyclerViewAdapter(adapter);
        mAdapter.notifyDataSetChanged();
    }

    public void updateItemData(List<?> data) {
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (mAdapter == null) {
            setRecyclerViewAdapter(new DefaultItemAdapter());
        }
        mData.clear();
        mData.addAll(data);
        mAdapter.notifyDataSetChanged();
        notifyDataStateChanged(true);
    }

    private <VH extends ItemHolder> void setRecyclerViewAdapter(ItemAdapter<VH> adapter) {
        mAdapter = new WheelViewAdapter<>(adapter);
        mRecyclerView.setAdapter(mAdapter);
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

    public void registerTargetDataObserver(TargetDataObserver observer) {
        if (observer != null) {
            if (mObservers == null) {
                mObservers = new ArrayList<>(4);
            }
            mObservers.add(observer);
        }
    }

    public void unregisterTargetDataObserver(TargetDataObserver observer) {
        if (observer != null && mObservers != null) {
            mObservers.remove(observer);
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
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void notifyDataStateChanged(boolean forceUpdate) {
        boolean changed = false;
        final int first = mManager.findFirstCompletelyVisibleItemPosition();
        if (first != mSelectedPosition) {
            mSelectedPosition = first;
            changed = true;
        }
        final int size = mData.size();
        if (mSelectedPosition < 0 || mSelectedPosition >= size) {
            if (forceUpdate && !mData.isEmpty()) {
                if (mSelectedPosition < 0) {
                    mSelectedPosition = 0;
                } else if (mSelectedPosition >= size) {
                    mSelectedPosition = size - 1;
                }
                changed = true;
            } else {
                mSelectedPosition = WheelView.INVALID_POSITION;
            }
        }
        if (changed || forceUpdate) {
            notifyTargetDataChanged();
        }
        if (changed && mScrollStateIdle) {
            notifyItemSelectedChanged();
        }
    }

    private void notifyItemSelectedChanged() {
        if (mListeners != null) {
            for (int i = 0, size = mListeners.size(); i < size; ++i) {
                mListeners.get(i).onItemSelected(mSelectedPosition);
            }
        }
    }

    private void notifyTargetDataChanged() {
        if (mObservers != null) {
            if (mSelectedPosition != WheelView.INVALID_POSITION) {
                for (int i = 0, size = mObservers.size(); i < size; ++i) {
                    mObservers.get(i).onDataChanged(mSelectedPosition);
                }
            } else {
                for (int i = 0, size = mObservers.size(); i < size; ++i) {
                    mObservers.get(i).onDataInvalid();
                }
            }
        }
    }


    private class WheelViewAdapter<VH extends ItemHolder> extends RecyclerView.Adapter<WheelViewHolder<? extends ItemHolder>> {
        private final ItemAdapter<VH> mItemAdapter;

        private WheelViewAdapter(ItemAdapter<VH> itemAdapter) {
            mItemAdapter = itemAdapter;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < mPlaceholderCount || position >= mData.size() + mPlaceholderCount) {
                return VIEW_TYPE_PLACE_HOLDER;
            }
            return VIEW_TYPE_DATA;
        }

        @Override
        public int getItemCount() {
            return mData.size() + mPlaceholderCount + mPlaceholderCount;
        }

        @NonNull
        @Override
        public WheelViewHolder<? extends ItemHolder> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_PLACE_HOLDER) {
                View placeholder = new View(parent.getContext());
                placeholder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return new WheelViewHolder<>(new ItemHolder(placeholder));
            }
            VH itemHolder = mItemAdapter.onCreateItemHolder(parent, mItemLayout);
            return new WheelViewHolder<>(itemHolder);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(@NonNull WheelViewHolder<? extends ItemHolder> holder, int position) {
            if (holder.getItemViewType() == VIEW_TYPE_DATA) {
                mItemAdapter.onBindItemHolder((VH) holder.itemHolder, position - mPlaceholderCount);
            }
            // 判断当前 item 的高度与测定的高度是否一致，如果不一致则重新设置。
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (mItemHeight != Integer.MIN_VALUE && lp.height != mItemHeight) {
                lp.height = mItemHeight;
                holder.itemView.setLayoutParams(lp);
            }
        }
    }


    private class DefaultItemAdapter extends ItemAdapter<ItemHolder> {
        @NonNull
        @Override
        public ItemHolder onCreateItemHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull ItemHolder holder, int position) {
            holder.textView.setText(String.valueOf(mData.get(position + mPlaceholderCount)));
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


    private static class WheelViewHolder<VH extends ItemHolder> extends RecyclerView.ViewHolder {
        private VH itemHolder;

        private WheelViewHolder(VH holder) {
            super(holder.itemView);
            this.itemHolder = holder;
        }
    }


    public static class ItemHolder {
        public final View itemView;
        public final TextView textView;

        public ItemHolder(View itemView) {
            this.itemView = itemView;
            this.textView = itemView.findViewById(android.R.id.text1);
        }
    }


    public static abstract class ItemAdapter<VH extends ItemHolder> {
        /**
         * @param itemLayout xml 布局中的 itemLayout, 如未指定则为 android.R.layout.simple_list_item_1
         */
        @NonNull
        public abstract VH onCreateItemHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout);

        /**
         * 默认的数据绑定仅设置文本，且文本是调用 {@link String#valueOf(Object)} 来转换的。
         */
        public abstract void onBindItemHolder(@NonNull VH holder, int position);
    }


    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }


    public interface TargetDataObserver {
        void onDataChanged(int position);

        void onDataInvalid();
    }
}

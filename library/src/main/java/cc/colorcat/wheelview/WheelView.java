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
import android.support.v7.widget.LinearSmoothScroller;
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
    public static final int INVALID_POSITION = RecyclerView.NO_POSITION;

    private static final int VIEW_TYPE_PLACE_HOLDER = 0;
    private static final int VIEW_TYPE_DATA = 1;

    // 庶罩，实现渐变突出中间项的效果。
    private View mMaskView;
    private RecyclerView mRecyclerView;
    private SnapHelper mSnapHelper;
    private LinearLayoutManager mManager;
    private RecyclerView.Adapter mAdapter;
    private final List<Object> mData = new ArrayList<>();
    // mData 更新或 mDisplayCount 变更等需强制检查中间项的数据变化，设为 true 以作标记, 处理后重置为 false.
    private boolean mForceNotify = false;
    // item 布局，用户未设置则默认为 android.R.layout.simple_list_item_1
    @LayoutRes
    private int mItemLayout;
    // 同时展示的 item 数量，含用于占位的 item.
    // 此值必须为正奇数，便于上下对称，如果用户设定为偶数需修正之。
    private int mDisplayCount = -1;
    // 用于占位的 item 数，始终为 mDisplayCount 的一半。
    private int mPlaceholderCount;
    // mDisplayCount 变更等需重新计算 mItemHeight, 设为 true 以作标记，处理后重置为 false.
    private boolean mForceLayout = false;
    // item 的高度，其值在 onLayout 中计算(height / mDisplayCount).
    private int mItemHeight = Integer.MIN_VALUE;
    // 中间的 item 所对应的 position.
    private int mSelectedPosition = WheelView.INVALID_POSITION;
    private List<OnItemSelectedListener> mListeners;
    private List<TargetDataObserver> mObservers;
    // 滚动状态，未滚动为 true，否则为 false.
    boolean mScrollStateIdle = true;
    // 如果为 true，则即使在滚动中也会检查中间项的变化，会影响 TargetDataObserver 被调用的频率。
    boolean mRadicalNotify = false;

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
        mItemLayout = ta.getResourceId(R.styleable.WheelView_itemLayout, android.R.layout.simple_list_item_1);
        int maskColor = ta.getColor(R.styleable.WheelView_maskColor, Color.TRANSPARENT);
        int displayCount = ta.getInteger(R.styleable.WheelView_displayCount, 3);
        mRadicalNotify = ta.getBoolean(R.styleable.WheelView_radicalNotify, false);
        ta.recycle();

        setDisplayCount(displayCount, false);

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mScrollStateIdle = newState == RecyclerView.SCROLL_STATE_IDLE;
                // 滚动停止检测中间项的数据变化
                if (mScrollStateIdle) {
                    notifyDataStateChanged(false);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // 如果 mRadicalNotify 为 true 则滚动中也检测中间项的数据变化
                if (mRadicalNotify) {
                    notifyDataStateChanged(false);
                }
            }
        });
        // 保证有一个 item 处于整个 RecyclerView 的中间位置。
        mSnapHelper = new LinearSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        addChildView(mRecyclerView);

        // 用户设置过遮罩颜色则添加遮罩 View
        if (maskColor != Color.TRANSPARENT) {
            addMaskView(context);
            setBackground(mMaskView, buildMaskBackground(maskColor));
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

    /**
     * @return 同时展示的 item 数量。
     */
    public int getDisplayCount() {
        return mDisplayCount;
    }

    /**
     * @param displayCount 同时展示的 item 数量。
     * @throws IllegalArgumentException 如果 displayCount 小于 1 将抛出此异常。
     */
    public void setDisplayCount(int displayCount) {
        setDisplayCount(displayCount, true);
    }

    private void setDisplayCount(int displayCount, boolean forceLayout) {
        if (displayCount < 1) {
            throw new IllegalArgumentException("displayCount must be greater than 0");
        }
        int count = displayCount;
        // 为偶数，则修正为奇数，大小向 5 靠拢。
        if ((count & 1) == 0) {
            if (count > 5) {
                --count;
            } else {
                ++count;
            }
        }
        if (mDisplayCount != count) {
            mDisplayCount = count;
            mPlaceholderCount = mDisplayCount >> 1;
            mForceLayout = forceLayout;
            if (mForceLayout) {
                requestLayout();
            }
        }
    }

    public void setMaskBackground(@ColorInt int color) {
        setMaskBackground(buildMaskBackground(color));
    }

    public void setMaskBackground(Drawable drawable) {
        if (mMaskView == null) {
            addMaskView(getContext());
        }
        setBackground(mMaskView, drawable);
    }

    public void setMaskView(@NonNull View view) {
        checkNotNull(view, "view == null");
        if (mMaskView != null) {
            super.removeView(mMaskView);
        }
        addChildView(view);
        mMaskView = view;
    }

    private void addMaskView(Context context) {
        mMaskView = new View(context);
        mMaskView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addChildView(mMaskView);
    }

    public <VH extends ItemHolder> void setItemAdapter(@NonNull ItemAdapter<VH> adapter) {
        setRealAdapter(checkNotNull(adapter, "adapter == null"));
    }

    public void updateData(@NonNull List<?> data) {
        checkNotNull(data, "data == null");
        mData.clear();
        mData.addAll(data);
        mForceNotify = true;
        if (mAdapter == null) {
            setRealAdapter(new DefaultItemAdapter());
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private <VH extends ItemHolder> void setRealAdapter(ItemAdapter<VH> adapter) {
        mAdapter = new WheelViewAdapter<>(adapter);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void addOnItemSelectedListener(OnItemSelectedListener listener) {
        if (listener != null) {
            if (mListeners == null) {
                mListeners = new ArrayList<>(4);
            }
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
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
            if (!mObservers.contains(observer)) {
                mObservers.add(observer);
            }
        }
    }

    public void unregisterTargetDataObserver(TargetDataObserver observer) {
        if (observer != null && mObservers != null) {
            mObservers.remove(observer);
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedPosition(int position) {
        if (mSelectedPosition != position && position >= 0 && position < mData.size()) {
            LinearSmoothScroller scroller = new StartLinearSmoothScroller(getContext());
            scroller.setTargetPosition(position);
            mManager.startSmoothScroll(scroller);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed || mForceLayout) {
            // 计算 item 的高度。
            mItemHeight = (bottom - top) / mDisplayCount;
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
        // 数据更新或 mDisplayCount 变更触发的 onLayout 需检测中间项的变化。
        if (mForceNotify) {
            mForceNotify = false;
            notifyDataStateChanged(true);
        }
        if (mForceLayout) {
            mForceLayout = false;
            if (mAdapter != null) {
                // 重新测定 mItemHeight 且 mAdapter 不为空会再次触发 onLayout，此时需强制检测中间项的变化。
                mForceNotify = true;
            }
        }
    }

    private void notifyDataStateChanged(boolean forceNotify) {
        boolean changed = false;
        final int selected = findTargetPosition();
        if (selected != mSelectedPosition) {
            mSelectedPosition = selected;
            changed = true;
        }
        if (changed || forceNotify) {
            notifyTargetDataChanged();
        }
        if (changed && mScrollStateIdle) {
            notifyItemSelectedChanged();
        }
    }

    private int findTargetPosition() {
        if (mData.isEmpty()) {
            return WheelView.INVALID_POSITION;
        }
        int position = mRecyclerView.getChildAdapterPosition(mSnapHelper.findSnapView(mManager)) - mPlaceholderCount;
        if (position < 0 || position >= mData.size()) {
            return WheelView.INVALID_POSITION;
        }
        return position;
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
            // 最前和最后的 mPlaceholderCount 项均只起占位作用，不展示任何实质内容。
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
                // 占位 View
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
                // 非占位 View 需绑定数据，因最前面有 mPlaceholderCount 个 View 用于占位，故：
                // 数据索引值 = position - mPlaceholderCount
                mItemAdapter.onBindItemHolder((VH) holder.itemHolder, position - mPlaceholderCount);
            }
            // 判断当前 item 的高度与测定的高度是否一致，如不一致则重新设定。
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
            holder.textView.setText(String.valueOf(mData.get(position)));
        }
    }


    private static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    private static Drawable buildMaskBackground(@ColorInt int maskColor) {
        int red = Color.red(maskColor);
        int green = Color.green(maskColor);
        int blue = Color.blue(maskColor);
        int alpha = Color.alpha(maskColor);

        int quarter = Color.argb((int) (alpha * 0.75), red, green, blue);
        int center = Color.argb(0, red, green, blue);
        int[] colors = {maskColor, quarter, center, quarter, maskColor};
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    }

    static <T> T checkNotNull(T t, String msg) {
        if (t == null) {
            throw new IllegalArgumentException(msg);
        }
        return t;
    }


    /**
     * 滚动时，始终把指定项滚动至最顶端。
     */
    private static class StartLinearSmoothScroller extends LinearSmoothScroller {
        private StartLinearSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return LinearSmoothScroller.SNAP_TO_START;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return LinearSmoothScroller.SNAP_TO_START;
        }
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
         * 数据绑定，默认仅设置文本，且文本调用 {@link String#valueOf(Object)} 转换。
         */
        public abstract void onBindItemHolder(@NonNull VH holder, int position);
    }


    public static abstract class SafeOnItemSelectedListener implements OnItemSelectedListener {
        @Override
        public final void onItemSelected(int position) {
            if (position != WheelView.INVALID_POSITION) {
                onSafeItemSelected(position);
            }
        }

        /**
         * 仅在 position 有效，即不等于 {@link WheelView#INVALID_POSITION} 时才会被调用。
         */
        public abstract void onSafeItemSelected(int position);
    }

    public interface OnItemSelectedListener {
        /**
         * 中间项对应的 position 变化且当前未滚动时调用。
         * note: 数据更新但中间项对应的 position 不一定变化，此时不会被调用，且 position 可能为 {@link WheelView#INVALID_POSITION}
         *
         * @see TargetDataObserver
         */
        void onItemSelected(int position);
    }


    public interface TargetDataObserver {
        /**
         * 中间项数据变化时被调用，只有数据已更新即便对应的 position 无变化也会被调用。
         * 如果 {@link WheelView#mRadicalNotify} 为 true 时，即使在滚动中，只要中间项数据发生变化也会被调用。
         *
         * @param position 不可能为 {@link WheelView#INVALID_POSITION}
         */
        void onDataChanged(int position);

        /**
         * 中间项数据所对应的 position 变为 {@link WheelView#INVALID_POSITION} 时被调用。
         */
        void onDataInvalid();
    }
}

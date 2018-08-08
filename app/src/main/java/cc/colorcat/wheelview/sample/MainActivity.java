package cc.colorcat.wheelview.sample;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.colorcat.wheelview.MultiWheelView;
import cc.colorcat.wheelview.WheelView;

public class MainActivity extends AppCompatActivity {
    private final List<Year> mYears = new ArrayList<>(100);

    {
        for (int i = 1950; i < 2050; ++i) {
            mYears.add(new Year(i));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView content = findViewById(R.id.tv_content);

        MultiWheelView wheelView = findViewById(R.id.mwv_calendar);
        wheelView.addOnSelectedChangeListener(new MultiWheelView.OnSelectedChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSelectedChanged(int... positions) {
                Year year = mYears.get(positions[0]);
                Month month = (Month) year.children().get(positions[1]);
                Day day = (Day) month.children().get(positions[2]);
                content.setText(String.format("%d 年 %d 月 %d 日", year.value, month.value, day.value));
            }
        });
        wheelView.setItemViewHolderFactories(new Factory1(), new Factory2(), new Factory3());
        wheelView.setMultiViewBinders(new MultiViewBinder1(), new MultiViewBinder2(), new MultiViewBinder3());
        wheelView.setData(mYears);
    }


    private static class Factory1 implements WheelView.ItemViewHolderFactory {

        @NonNull
        @Override
        public WheelView.ItemViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            itemView.setBackgroundColor(Color.RED);
            return new WheelView.ItemViewHolder(itemView);
        }
    }

    private static class Factory2 implements WheelView.ItemViewHolderFactory {
        @NonNull
        @Override
        public WheelView.ItemViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            itemView.setBackgroundColor(Color.GREEN);
            return new WheelView.ItemViewHolder(itemView);
        }
    }


    private static class Factory3 implements WheelView.ItemViewHolderFactory {
        @NonNull
        @Override
        public WheelView.ItemViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            itemView.setBackgroundColor(Color.BLUE);
            return new WheelView.ItemViewHolder(itemView);
        }
    }

    private static class MultiViewBinder1 extends MultiWheelView.SimpleMultiViewBinder {
        @SuppressLint("DefaultLocale")
        @Override
        public boolean onBind(WheelView.ItemViewHolder holder, MultiWheelView.Node node) {
            Year year = (Year) node;
            holder.textView.setText(String.format("%d 年", year.value));
            return true;
        }
    }

    private static class MultiViewBinder2 extends MultiWheelView.SimpleMultiViewBinder {
        @SuppressLint("DefaultLocale")
        @Override
        public boolean onBind(WheelView.ItemViewHolder holder, MultiWheelView.Node node) {
            Month month = (Month) node;
            holder.textView.setText(String.format("%d 月", month.value));
            return true;
        }
    }

    private static class MultiViewBinder3 extends MultiWheelView.SimpleMultiViewBinder {
        @SuppressLint("DefaultLocale")
        @Override
        public boolean onBind(WheelView.ItemViewHolder holder, MultiWheelView.Node node) {
            Day day = (Day) node;
            holder.textView.setText(String.format("%d 日", day.value));
            return true;
        }
    }
}


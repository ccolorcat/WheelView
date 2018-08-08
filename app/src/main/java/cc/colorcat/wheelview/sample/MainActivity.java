package cc.colorcat.wheelview.sample;

import android.annotation.SuppressLint;
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
        wheelView.setData(mYears);
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
    }


    private static class Factory1 implements WheelView.ItemViewHolderFactory {

        @NonNull
        @Override
        public WheelView.ItemViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new WheelView.ItemViewHolder(itemView);
        }
    }

    private static class Factory2 implements WheelView.ItemViewHolderFactory {
        @NonNull
        @Override
        public WheelView.ItemViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new WheelView.ItemViewHolder(itemView);
        }
    }


    private static class Factory3 implements WheelView.ItemViewHolderFactory {
        @NonNull
        @Override
        public WheelView.ItemViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, @LayoutRes int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new WheelView.ItemViewHolder(itemView);
        }
    }
}


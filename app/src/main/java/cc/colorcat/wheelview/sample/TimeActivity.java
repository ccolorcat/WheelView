package cc.colorcat.wheelview.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cc.colorcat.wheelview.MultiWheelView;
import cc.colorcat.wheelview.WheelView;

public class TimeActivity extends AppCompatActivity {
    private final List<Year> mYears = new ArrayList<>(100);

    {
        for (int i = 1950; i < 2050; ++i) {
            mYears.add(new Year(i));
        }
    }

    private MultiWheelView wheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_wheel_view);

        final TextView content = findViewById(R.id.tv_content);

        wheelView = findViewById(R.id.multi_wheel_view);
        wheelView.addOnSelectedChangeListener(new MultiWheelView.SafeOnSelectedChangeListener() {
            @Override
            public void onSafeSelectedChanged(int... positions) {
                Year year = mYears.get(positions[0]);
                Month month = (Month) year.children().get(positions[1]);
                Day day = (Day) month.children().get(positions[2]);
                content.setText(String.format(Locale.getDefault(), "%d 年 %d 月 %d 日", year.value, month.value, day.value));
            }
        });
        wheelView.setMultiItemAdapter(0, new YearAdapter());
        wheelView.setMultiItemAdapter(1, new MonthAdapter());
        wheelView.setMultiItemAdapter(2, new DayAdapter());
        wheelView.updateData(mYears);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                wheelView.updateData(mYears);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private static class YearAdapter extends MultiWheelView.MultiItemAdapter<WheelView.ItemHolder> {
        @NonNull
        @Override
        public WheelView.ItemHolder onCreateItemHolder(@NonNull ViewGroup parent, int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_node, parent, false);
            return new WheelView.ItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull WheelView.ItemHolder holder, MultiWheelView.Node data) {
            holder.textView.setText(String.format("%s 年", data.contentToString()));
            holder.textView.setSelected(isLeapYear(((Year) data).value));
        }
    }

    private static class MonthAdapter extends MultiWheelView.MultiItemAdapter<WheelView.ItemHolder> {

        @NonNull
        @Override
        public WheelView.ItemHolder onCreateItemHolder(@NonNull ViewGroup parent, int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_node, parent, false);
            return new WheelView.ItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull WheelView.ItemHolder holder, MultiWheelView.Node data) {
            Month month = (Month) data;
            int value = month.value;
            if (value > 7) {
                --value;
            }
            holder.textView.setText(String.format("%s 月", data.contentToString()));
            holder.textView.setSelected((value & 1) != 0);
        }
    }

    private static class DayAdapter extends MultiWheelView.MultiItemAdapter<DayItemHolder> {
        @NonNull
        @Override
        public DayItemHolder onCreateItemHolder(@NonNull ViewGroup parent, int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
            return new DayItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull DayItemHolder holder, MultiWheelView.Node data) {
            Day day = (Day) data;
            holder.textView.setText(data.contentToString());
            holder.text2.setText(String.valueOf((day.value + 6) / 7));
        }
    }

    private static class DayItemHolder extends WheelView.ItemHolder {
        public final TextView text2;

        DayItemHolder(View itemView) {
            super(itemView);
            this.text2 = itemView.findViewById(android.R.id.text2);
        }
    }

    private static boolean isLeapYear(int year) {
        return (year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);
    }
}


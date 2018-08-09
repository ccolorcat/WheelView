package cc.colorcat.wheelview.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private MultiWheelView wheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView content = findViewById(R.id.tv_content);

        wheelView = findViewById(R.id.mwv_calendar);
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
//        wheelView.setMultiItemAdapter(0, new IconAdapter());
//        wheelView.setMultiItemAdapter(1, new TwoLineAdapter());
//        wheelView.setMultiItemAdapter(2, new SingleLineAdapter());
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
            case R.id.fill_data:
                wheelView.updateData(mYears);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private static class IconAdapter extends MultiWheelView.MultiItemAdapter<IconItemHolder> {
        @NonNull
        @Override
        public IconItemHolder onCreateItemHolder(@NonNull ViewGroup parent, int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_item, parent, false);
            return new IconItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull IconItemHolder holder, MultiWheelView.Node data) {
            holder.textView.setText(String.format("%s 年", data.contentToString()));
            holder.icon.setImageResource(R.mipmap.ic_launcher_round);
        }

        @Override
        public void onClearItemHolder(@NonNull IconItemHolder holder) {
            holder.icon.setImageDrawable(null);
        }
    }

    private static class TwoLineAdapter extends MultiWheelView.MultiItemAdapter<TwoLineItemHolder> {

        @NonNull
        @Override
        public TwoLineItemHolder onCreateItemHolder(@NonNull ViewGroup parent, int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new TwoLineItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull TwoLineItemHolder holder, MultiWheelView.Node data) {
            holder.textView.setText(String.format("%s 月", data.contentToString()));
            Month month = (Month) data;
            int value = month.value;
            if (value > 7) {
                --value;
            }
            String des = (value & 1) == 0 ? "月小" : "月大";
            holder.text2.setText(des);
        }

        @Override
        public void onClearItemHolder(@NonNull TwoLineItemHolder holder) {
            holder.text2.setText("");
        }
    }

    private static class SingleLineAdapter extends MultiWheelView.MultiItemAdapter<SingleLineItemHolder> {
        @NonNull
        @Override
        public SingleLineItemHolder onCreateItemHolder(@NonNull ViewGroup parent, int itemLayout) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            return new SingleLineItemHolder(itemView);
        }

        @Override
        public void onBindItemHolder(@NonNull SingleLineItemHolder holder, MultiWheelView.Node data) {
            holder.textView.setText(data.contentToString());
        }
    }


    private static class IconItemHolder extends WheelView.ItemHolder {
        public final ImageView icon;

        IconItemHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
        }
    }

    private static class SingleLineItemHolder extends WheelView.ItemHolder {
        SingleLineItemHolder(View itemView) {
            super(itemView);
            this.textView.setGravity(Gravity.CENTER);
        }
    }

    private static class TwoLineItemHolder extends WheelView.ItemHolder {
        public final TextView text2;

        TwoLineItemHolder(View itemView) {
            super(itemView);
            this.text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}


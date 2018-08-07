package cc.colorcat.wheelview.sample;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.colorcat.wheelview.MultiWheelView;

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
                content.setText(String.format("%d 年 %d 月 %d 日", year.year, month.month, day.day));
            }
        });
    }
}

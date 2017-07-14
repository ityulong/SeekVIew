package xaircraft.seekview;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import xaircraft.seekview.model.AirLineStatus;
import xaircraft.seekview.model.ILineStatus;
import xaircraft.seekview.model.MyAirLine;
import xaircraft.seekview.view.AreaSeek;
import xaircraft.seekview.view.ThumbnailSeek;

public class MainActivity extends Activity {
    private final static int LINES_COUNT = 999;
    private final static int SHOW_COUNT = 50;
    private final static int SELECT_START = 20;
    private final static int SELECT_END = 200;


    private AreaSeek mSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ThumbnailSeek thumbnailSeek = (ThumbnailSeek) findViewById(R.id.my_th_seek);
        thumbnailSeek.setCountScale(LINES_COUNT, SHOW_COUNT);

        //初始化数据
        List<AirLineStatus> status = new ArrayList<>();
        for (int i = 0; i < LINES_COUNT; i++) {
            MyAirLine line = new MyAirLine();
            line.setIndex(i);
            if (i < 100) {
                if (i % 3 == 0) {
                    line.setFinished(true);
                }
            } else if (i < 200) {
                if (i / 2 % 3 == 0) {
                    line.setFinished(true);

                }
            } else if (i < 300) {
                if (i / 3 % 3 == 0) {
                    line.setFinished(true);
                }
            } else if (i < 400) {
                if (i / 4 % 3 == 0) {
                    line.setFinished(true);
                }
            } else if (i < 500) {
                if (i / 5 % 3 == 0) {
                    line.setFinished(true);
                }
            } else {
                if (i / 20 % 3 == 0) {
                    line.setFinished(true);
                }
            }
            status.add(line);
        }
        thumbnailSeek.setData(status,SELECT_START,SELECT_END);
        thumbnailSeek.setDragListener(
                new ThumbnailSeek.OnDragBarListener() {
                    @Override
                    public void OnChange(int start, int end, List<? extends ILineStatus> lines) {
                        mSeek.setMax(end);
                        mSeek.setMin(start);
                        mSeek.setCompletedLines(lines);
                    }
                });

        //初始化 AreaSeek
        mSeek = (AreaSeek) findViewById(R.id.my_area_seek);
        mSeek.setLeftToRight(true);
        mSeek.setMax(SHOW_COUNT - 1);
        mSeek.setMin(0);
        mSeek.setStart(SELECT_START);
        mSeek.setEnd(SELECT_END);
        mSeek.setCompletedLines(status.subList(0, SHOW_COUNT));
        mSeek.setOnSeekBarChangeListener(new AreaSeek.OnSeekBarChangeListener() {
            @Override
            public void onSelectChanged(AreaSeek seek, int start, int end) {
                thumbnailSeek.setSelectStart(start);
                thumbnailSeek.setSelectEnd(end);
            }

            @Override
            public void onStopTrackingTouch(AreaSeek seekBar) {

            }

        });
    }
}

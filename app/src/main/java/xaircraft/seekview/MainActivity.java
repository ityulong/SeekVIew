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
    private final static int LINES_COUNT = 2000;
    private final static int SHOW_COUNT = 200;

    private AreaSeek mSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSeek = (AreaSeek) findViewById(R.id.my_area_seek);
        mSeek.setMax(100);
        mSeek.setMin(0);
        mSeek.setLeftToRight(true);

        ThumbnailSeek thumbnailSeek = (ThumbnailSeek) findViewById(R.id.my_th_seek);
        thumbnailSeek.setCountScale(LINES_COUNT, SHOW_COUNT);

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
        thumbnailSeek.setLines(status);
        thumbnailSeek.setDragListener(new ThumbnailSeek.OnDragBarListener() {
                                          @Override
                                          public void OnChange(int start, int end, List<? extends ILineStatus> lines) {
                                              mSeek.setMax(end);
                                              mSeek.setMin(start);
                                              mSeek.setCompletedLines(lines);
                                          }
                                      });
    }
}

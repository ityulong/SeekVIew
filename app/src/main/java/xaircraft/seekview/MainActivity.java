package xaircraft.seekview;

import android.app.Activity;
import android.os.Bundle;

import xaircraft.seekview.view.AreaSeek;
import xaircraft.seekview.view.ThumbnailSeek;

public class MainActivity extends Activity {
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
        thumbnailSeek.setCountScale(1000,200);
        thumbnailSeek.setDragListener(new ThumbnailSeek.OnDragBarListener() {
            @Override
            public void OnChange(int start, int end) {
                mSeek.setMax(end);
                mSeek.setMin(start);
            }
        });


    }
}

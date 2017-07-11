package xaircraft.seekview;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private AreaSeek mSeek;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSeek = (AreaSeek) findViewById(R.id.my_area_seek);
        mSeek.setMax(100);
        mSeek.setMin(0);
        mSeek.setStart(30);
        mSeek.setEnd(60);
        mSeek.setLeftToRight(true);


    }
}

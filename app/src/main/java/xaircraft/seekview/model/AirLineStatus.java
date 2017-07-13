package xaircraft.seekview.model;

/**
 * Created by chenyulong on 2017/7/12.
 */

public abstract class AirLineStatus {

    private boolean isFinished = false;

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    private int mIndex;

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }
}

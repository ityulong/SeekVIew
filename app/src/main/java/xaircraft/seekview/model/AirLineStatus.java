package xaircraft.seekview.model;

/**
 * Created by chenyulong on 2017/7/12.
 */

public abstract class AirLineStatus implements ILineStatus {

    private boolean isFinished = false;

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    private int mIndex;

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public void setIndex(int index) {
        mIndex = index;
    }


}

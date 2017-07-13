package xaircraft.seekview.model;

/**
 * Created by chenyulong on 2017/7/13.
 */

public interface ILineStatus {
    boolean isFinished();

    void setFinished(boolean finished);

    int getIndex();

    void setIndex(int index);
}

package xaircraft.seekview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import xaircraft.seekview.R;
import xaircraft.seekview.help.Utils;
import xaircraft.seekview.model.ILineStatus;

/**
 * Created by chenyulong on 2017/7/11.
 */

public class ThumbnailSeek extends View {
    //draw Text Gravity
    public final static int TEXT_GRAVITY_INTERNAL = 0;
    public final static int TEXT_GRAVITY_TOP = 1;
    public final static int LINE_STATUS_FINISHED = 0;
    public final static int LINE_STATUS_SELECTED = 1;


    private Paint mFillPaint;
    private Paint mStrokePaint;
    private Drawable mStartThumbDrawable;

    private int mCount;
    //unit dp
    private static final float BAR_HEIGHT = 25;
    private static final int DRAG_HEIGHT = 35;

    private int barHeight;

    private int dragWidth = 100;
    private int dragHeight;


    private boolean dClicked = false;
    private boolean isFirstDraw = true;

    private Rect dragRect = new Rect();
    private float dragScale;
    private Rect airLineRect = new Rect();

    private int bStartCount;
    private int bEndCount;

    private int dragColor = 0X88C7AFAF;
    private int dragBorderColor = 0Xff000000;

    private int barColor = 0xFFFFFFFF;
    private int barBorderColor = 0X88C7AFAF;

    private int lineSelectedColor = 0x80FFFF00;
    private int lineFinishedColor = 0xFF3F51B5;


    private OnDragBarListener mDragListener;

    private List<? extends ILineStatus> lines;

    private float perPxLine;
    private float perLinePx;
    private int mSelectStart;
    private int mSelectEnd;
    private boolean mReverse;
    private int mShowNumber;

    private String startStr;
    private String endStr;


    //是否可以拖动航线条选择显示范围
    private boolean touchAirLineBar = false;

    public ThumbnailSeek(Context context) {
        this(context, null);
    }

    public ThumbnailSeek(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbnailSeek(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);

        barHeight = Utils.dip2px(getContext(), BAR_HEIGHT);

        dragHeight = Utils.dip2px(getContext(), DRAG_HEIGHT);

        //总航线框


        mStartThumbDrawable = getResources().getDrawable(R.drawable.widget_area_seekbar_btn_start);
        Log.d("function_lifecycle", "ThumbnailSeek()");


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureDimension(200, widthMeasureSpec);
        int height = measureDimension(200, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;   //UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    public boolean clickDragBar(int x, int y) {
        return dragRect.contains(x, y);
    }

    public boolean clickAirLineBar(int x, int y) {
        return airLineRect.contains(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (isTouchAirLineBar()) {
                if (clickAirLineBar(x, y)) {
                    dClicked = true;
                    removeDragBar(x);
                }
            } else {
                if (clickDragBar(x, y)) {
                    dClicked = true;
                }
            }

            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (dClicked) {
                int x = (int) event.getX();
//                isFirstDraw = false;
                removeDragBar(x);

            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            dClicked = false;
        }

        return super.onTouchEvent(event);
    }

    private void removeDragBar(int x) {
        dragRect.left = x - dragWidth / 2;
        dragRect.right = x + dragWidth / 2;

        if (dragRect.left < airLineRect.left) {
            dragRect.left = (int) airLineRect.left;
            dragRect.right = dragRect.left + dragWidth;
        } else if (dragRect.right > airLineRect.right) {
            dragRect.right = (int) airLineRect.right;
            dragRect.left = (int) airLineRect.right - dragWidth;
        }

        float startCountS = (float) (dragRect.left - airLineRect.left) / (airLineRect.width());
        float endCountS = (float) (dragRect.right - airLineRect.left) / (airLineRect.width());
        bStartCount = (int) (mCount * startCountS);
        bEndCount = (int) (mCount * endCountS);
        if (bEndCount == mCount) {
            bEndCount = mCount - 1;
        }
        if (mDragListener != null) {
            List<? extends ILineStatus> subLines = lines.subList(bStartCount, bEndCount + 1);
            mDragListener.OnChange(bStartCount, bEndCount, subLines);
            Log.d("sub_lines", "start:" + bStartCount + ", end:" + bEndCount);
            Log.d("sub_lines", "in sub list ,start:" + subLines.get(0).getIndex() + ", end:" + subLines.get(subLines.size() - 1).getIndex());

        }
        invalidate();
    }

    public void setCountScale(int count, int showNumber) {
        Log.d("function_lifecycle", "setCountScale()");
        if (count > 0) {
            mShowNumber = showNumber;
            mCount = count;
            if (mCount >= showNumber)
                dragScale = (float) showNumber / mCount;
            else
                throw new RuntimeException("showNumber > count");

            startStr = String.valueOf(1);
            endStr = String.valueOf(mCount);
            mFillPaint.setTextSize(30);


            bStartCount = 0;
            bEndCount = mShowNumber - 1;
            invalidate();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("function_lifecycle", "onDraw()");
        if (mCount > 0) {

            //初始化一些数据
            int height = getHeight();
            int width = getWidth();
            int rLeft = 0;
            int rTop = height / 2 - barHeight / 2;
            int rRight = width;
            int rBottom = height / 2 + barHeight / 2;
            airLineRect.set(rLeft, rTop, rRight, rBottom);
            perPxLine = (float) mCount / airLineRect.width();
            perLinePx = (float) airLineRect.width() / mCount;

            Rect parentR = new Rect(0, 0, width, height);
//            drawTextToRect(startStr, endStr, parentR, TEXT_GRAVITY_INTERNAL, canvas);

            //画总航线条
            mFillPaint.setColor(barColor);
            canvas.drawRect(airLineRect, mFillPaint);
            //画航线条边框
            mStrokePaint.setColor(barBorderColor);
            canvas.drawRect(airLineRect, mStrokePaint);

            //画已经完成的航线
            int finishCount = 0;
            int startNumber = 0;
            for (int i = 0; i < lines.size(); i++) {
                ILineStatus line = lines.get(i);
                if (line.isFinished()) {
                    //记录从第几条航向开始画已完成的航线
                    if (finishCount == 0)
                        startNumber = line.getIndex();
                    finishCount++;
                    //如果下一个未完成就画已完成的航线，并且清零
                    if ((i + 1 < lines.size() && !lines.get(i + 1).isFinished())) {
                        drawLinesStatus(startNumber, finishCount, canvas, LINE_STATUS_FINISHED);
                        Log.d("draw_finish_line", "start position:" + startNumber + ", finish count:" + finishCount);
                        finishCount = 0;
                    } else if (i + 1 == lines.size() && finishCount > 0) {
                        drawLinesStatus(startNumber, finishCount, canvas, LINE_STATUS_FINISHED);
                        Log.d("draw_finish_line", "start position:" + startNumber + ", finish count:" + finishCount);
                        finishCount = 0;
                    }
                }
            }

            //画选择的航线
            if (!isReverse())
                drawLinesStatus(mSelectStart, mSelectEnd - mSelectStart + 1, canvas, LINE_STATUS_SELECTED);
            else
                drawLinesStatus(mSelectEnd, mSelectStart - mSelectEnd + 1, canvas, LINE_STATUS_SELECTED);

            //画拖动框
            if (isFirstDraw) {
                dragWidth = (int) ((rRight - rLeft) * dragScale);
                int Left = 0;
                int Top = height / 2 - dragHeight / 2;
                int Right = Left + dragWidth;
                int Bottom = Top + dragHeight;
                dragRect.set(Left, Top, Right, Bottom);
                mFillPaint.setColor(dragColor);
                canvas.drawRect(dragRect, mFillPaint);
                mStrokePaint.setColor(dragBorderColor);
                canvas.drawRect(dragRect, mStrokePaint);
                isFirstDraw = false;
            } else {
                mFillPaint.setColor(dragColor);
                canvas.drawRect(dragRect, mFillPaint);
                mStrokePaint.setColor(dragBorderColor);
                canvas.drawRect(dragRect, mStrokePaint);
                Log.d("draw rect", dragRect.toString());
            }
            //画拖动框上面的文字
            String startCountStr = String.valueOf(bStartCount + 1);
            String endCountStr = String.valueOf(bEndCount + 1);
            drawTextToRect(startCountStr, endCountStr, dragRect, TEXT_GRAVITY_TOP, canvas);


        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mStartThumbDrawable.setState(getDrawableState());
    }

    public void drawTextToRect(String startStr, String endStr, Rect rect, int gravity, Canvas canvas) {

        mFillPaint.setColor(0xFF999999);
        float sTextWidth = mFillPaint.measureText(startStr);
        float eTextWidth = mFillPaint.measureText(endStr);
        switch (gravity) {
            case TEXT_GRAVITY_INTERNAL: {
                //开始和结束text
                mFillPaint.setTextSize(30);
                mFillPaint.setTextAlign(Paint.Align.LEFT);
                Paint.FontMetrics fontMetrics = mFillPaint.getFontMetrics();
                float mTop = fontMetrics.top;//为基线到字体上边框的距离
                float mBottom = fontMetrics.bottom;//为基线到字体下边框的距离
                if (sTextWidth >= eTextWidth) {
                    canvas.drawText(startStr, rect.left, rect.centerY() - mTop / 2 - mBottom / 2, mFillPaint);
                    canvas.drawText(endStr, rect.right - Math.max(sTextWidth, eTextWidth), rect.centerY() - mTop / 2 - mBottom / 2, mFillPaint);
                } else {
                    canvas.drawText(startStr, rect.left + eTextWidth - sTextWidth, rect.centerY() - mTop / 2 - mBottom / 2, mFillPaint);
                    canvas.drawText(endStr, rect.right - eTextWidth, rect.centerY() - mTop / 2 - mBottom / 2, mFillPaint);
                }

                break;
            }
            case TEXT_GRAVITY_TOP: {
                mFillPaint.setTextSize(24);
                mFillPaint.setTextAlign(Paint.Align.RIGHT);
                Paint.FontMetrics fontMetrics = mFillPaint.getFontMetrics();
                float mBottom = fontMetrics.bottom;//为基线到字体下边框的距离
                canvas.drawText(startStr, rect.left, rect.top - mBottom, mFillPaint);
                mFillPaint.setTextAlign(Paint.Align.LEFT);
                fontMetrics = mFillPaint.getFontMetrics();
                mBottom = fontMetrics.bottom;//为基线到字体下边框的距离
                canvas.drawText(endStr, rect.right, rect.top - mBottom, mFillPaint);
                break;
            }
        }

    }

    public void setDragListener(OnDragBarListener listener) {
        Log.d("function_lifecycle", "setDragListener()");

        this.mDragListener = listener;
    }

    public interface OnDragBarListener {
        void OnChange(int start, int end, List<? extends ILineStatus> lines);
    }

    public void setData(List<? extends ILineStatus> lines, int start, int end) {
        Log.d("function_lifecycle", "setData()");

        if (lines != null && lines.size() > 0) {
            this.lines = lines;
        }
        this.mSelectStart = start;
        this.mSelectEnd = end;
        invalidate();
    }

    public void drawLinesStatus(int start, int count, Canvas canvas, int lineStatus) {
        switch (lineStatus) {
            case LINE_STATUS_FINISHED:
                mFillPaint.setColor(lineFinishedColor);
                break;
            case LINE_STATUS_SELECTED:
                mFillPaint.setColor(lineSelectedColor);
                break;
        }
        //当只有一个像素的时候需要几条航线，如果小于这个值就不画线
        if (count > perPxLine) {
            int left = (int) (airLineRect.left + start * perLinePx + 0.5);
            int top = (int) airLineRect.top;
            int right = (int) (left + perLinePx * count);
            int bottom = (int) airLineRect.bottom;
            Rect rect = new Rect(left, top, right, bottom);
            Log.d("draw_finish_rect", rect.toString());
            canvas.drawRect(rect, mFillPaint);
        }

    }

    public void setSelectStart(int start) {
        Log.d("function_lifecycle", "setSelectStart()");

        if (mSelectStart != start) {
            mSelectStart = start;
            if (mSelectStart > mCount) mSelectStart = mCount;
            if (mSelectStart < 0) mSelectStart = 0;
            mReverse = mSelectStart > mSelectEnd;
            invalidate();
        }
    }


    public void setSelectEnd(int end) {
        Log.d("function_lifecycle", "setSelectEnd()");
        if (mSelectEnd != end) {
            mSelectEnd = end;
            if (mSelectEnd > mCount) mSelectEnd = mCount;
            if (mSelectEnd < 0) mSelectEnd = 0;
            mReverse = mSelectStart > mSelectEnd;
            invalidate();
        }
    }

    public boolean isReverse() {
        return mReverse;
    }

    /**
     * 跳转到已经选择的航线
     */
    public void jump2Selected() {
        if (!isReverse()) {
            if (mSelectStart + mShowNumber < mCount) {
                dragRect.left = airLineRect.left + (int) (perLinePx * mSelectStart);
                dragRect.right = dragRect.left + dragWidth;
                bStartCount = mSelectStart;
                bEndCount = bStartCount + mShowNumber - 1;
            } else if (mSelectEnd - mShowNumber + 1 >= 0) {
                dragRect.right = airLineRect.left + (int) (perLinePx * (mSelectEnd + 1));
                dragRect.left = dragRect.right - dragWidth;
                bEndCount = mSelectEnd;
                bStartCount = bEndCount - mShowNumber + 1;
            } else {
                return;
            }

        } else {
            if (mSelectEnd + mShowNumber < mCount) {
                dragRect.left = airLineRect.left + (int) (perLinePx * mSelectEnd);
                dragRect.right = dragRect.left + dragWidth;
                bStartCount = mSelectEnd;
                bEndCount = bStartCount + mShowNumber - 1;
            } else if (mSelectStart - mShowNumber + 1 >= 0) {
                dragRect.right = airLineRect.left + (int) (perLinePx * (mSelectStart + 1));
                dragRect.left = dragRect.right - dragWidth;
                bEndCount = mSelectStart;
                bStartCount = mSelectEnd - mShowNumber + 1;

            } else {
                return;
            }
        }

        if (mDragListener != null) {
            List<? extends ILineStatus> subLines = lines.subList(bStartCount, bEndCount + 1);
            mDragListener.OnChange(bStartCount, bEndCount, subLines);
//            Log.d("sub_lines", "start:" + bStartCount + ", end:" + bEndCount);
//            Log.d("sub_lines", "in sub list ,start:" + subLines.get(0).getIndex() + ", end:" + subLines.get(subLines.size() - 1).getIndex());
        }
        invalidate();
    }

    public boolean isTouchAirLineBar() {
        return touchAirLineBar;
    }

    public void setTouchAirLineBar(boolean touchAirLineBar) {
        this.touchAirLineBar = touchAirLineBar;
    }
}

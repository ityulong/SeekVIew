package xaircraft.seekview.view;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import xaircraft.seekview.R;
import xaircraft.seekview.help.Utils;
import xaircraft.seekview.model.AirLineStatus;

public class AreaSeek extends View {

    public static final int THUMB_START = 1;
    public static final int THUMB_END = 2;

    private static final int GRAVITY_LEFT = 1;
    private static final int GRAVITY_RIGHT = 2;

    private int mStartThumbLineColor = 0XFF333FB0;
    private int mEndThumbLineColor = 0XFFB13434;

    private int TOUCH_WIDTH = 80;

    private final int min_size;
    private int mMin = 0;
    private int mMax = 0;

    private boolean mReverse;

    private int mStart;
    private int mEnd;
    private Paint mFillPaint;
    private Paint mStokePaint;

    private final int REFLINE_WIDTH = 16;
    private final int PADDING_LEFT = 30;
    private final int PADDING_RIGHT = 30;
    private final int PADDING_TOP = 30;
    private final int PADDING_BOTTOM = 0;
    private int mCount = 0;

    private int mThumbWidth = 50;
    private int mCompletedColor;
    private float tickWidth;
    private float firstLineX;

    private Drawable mStartThumbDrawable;
    private Drawable mEndThumbDrawable;
    //    private SparseArray mCompletedLines = new SparseArray();
    //    private SparseArray mCleanupLines = new SparseArray();
    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    int pressWhat = 0;
    int pressLine = -1;

    int pressX = 0;
    int pressY = 0;

    int hoverX = 0;
    int hoverLine = -1;
    private boolean mLeftToRight;

    private boolean mLockStart;
    private boolean mLockEnd;

    private SparseArray<AirLineStatus> selectLines = new SparseArray<>();

    public AreaSeek(Context context) {
        this(context, null, 0);
    }

    public AreaSeek(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AreaSeek(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);
        mStokePaint = new Paint();
        mStokePaint.setStyle(Paint.Style.STROKE);
        mStokePaint.setAntiAlias(true);
        min_size = Utils.dip2px(getContext(), 80);

        mStartThumbDrawable = getResources().getDrawable(R.drawable.widget_area_seekbar_btn_start);
        mEndThumbDrawable = getResources().getDrawable(R.drawable.widget_area_seekbar_btn_end);
        mCompletedColor = getResources().getColor(R.color.colorPrimary);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));

        mThumbWidth = mStartThumbDrawable.getIntrinsicWidth();
        TOUCH_WIDTH = (int) (mStartThumbDrawable.getIntrinsicWidth() * 1.5);

    }


    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = min_size;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (pressWhat == 0) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                if (hitTestX(x, y, mStart - mMin, TOUCH_WIDTH) && !isLockStart()) {
                    pressWhat = THUMB_START;
                    pressLine = mStart;
                    pressX = x;
                    pressY = y;
                } else if (hitTestX(x, y, mEnd - mMin, TOUCH_WIDTH) && !isLockEnd()) {
                    pressWhat = THUMB_END;
                    pressLine = mEnd;
                    pressX = x;
                    pressY = y;
                }
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressWhat > 0) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                hoverX = x;

                boolean hitTest = false;
                int hitCount = 0;
                for (int i = 0; i < mCount; i++) {
                    if (hitTestX(x, y, i, tickWidth)) {
                        hitCount = i;
                        hitTest = true;
                        break;
                    }
                }

                if (hitTest) {
                    if (pressWhat == THUMB_START) {
                        setStart(hitCount + mMin);
                    } else if (pressWhat == THUMB_END) {
                        setEnd(hitCount + mMin);
                    }
                }

                invalidate();
                return true;
            }
        } else {
            pressWhat = 0;
            pressLine = -1;

            if (mOnSeekBarChangeListener != null)
                mOnSeekBarChangeListener.onRangeChanged(this, mStart, mEnd);
            if (mOnSeekBarChangeListener != null)
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
            invalidate();
        }
        return super.onTouchEvent(event);
    }


    /**
     * @param pressX 点击的x
     * @param pressY 点击的y
     * @param value  在第几条航线处（start ~ end）
     * @param width  触控宽度
     * @return
     */
    private boolean hitTestX(int pressX, int pressY, int value, float width) {
        if (mLeftToRight) {
            int lineLeft = (int) (firstLineX + tickWidth * value - width / 2);
            int lineRight = (int) (firstLineX + tickWidth * value + width / 2);
            Rect rect = new Rect(lineLeft, 0, lineRight, getHeight());

            return rect.contains(pressX, pressY);
        } else {
            int lineLeft = (int) (firstLineX - tickWidth * value - width / 2);
            int lineRight = (int) (firstLineX - tickWidth * value + width / 2);
            Rect rect = new Rect(lineLeft, 0, lineRight, getHeight());

            return rect.contains(pressX, pressY);
        }
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mStartThumbDrawable.setState(getDrawableState());
    }

    private int getLineLeftX(int lineIndex) {
        if (mLeftToRight) {
            return (int) (firstLineX - tickWidth / 2 + tickWidth * lineIndex);
        } else {
            return (int) (firstLineX - tickWidth / 2 - tickWidth * lineIndex);
        }
    }


    private int getLineCenterX(int lineIndex) {
        if (mLeftToRight) {
            return (int) (firstLineX + tickWidth * lineIndex);
        } else {
            return (int) (firstLineX - tickWidth * lineIndex);
        }
    }

    private int getLineRightX(int lineIndex) {
        if (mLeftToRight) {
            return (int) (firstLineX + tickWidth / 2 + tickWidth * lineIndex);
        } else {
            return (int) (firstLineX + tickWidth / 2 - tickWidth * lineIndex);
        }
    }

    public boolean isLockStart() {
        return mLockStart;
    }

    public void setLockStart(boolean lockStart) {
        mLockStart = lockStart;
    }

    public boolean isLockEnd() {
        return mLockEnd;
    }

    public void setLockEnd(boolean lockEnd) {
        mLockEnd = lockEnd;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mFillPaint.setColor(0xFFEEFFEE);
        int width = getWidth();
        int height = getHeight() - mThumbWidth / 2;
        int left = 0;
        int right = width;
        int top = 0;
        int bottom = height;


        final int count = mCount;

        if (count > 0) {
            tickWidth = (float) (width - PADDING_LEFT - PADDING_RIGHT) / count;
            firstLineX = mLeftToRight ? PADDING_LEFT + tickWidth / 2 : width - PADDING_RIGHT - tickWidth / 2;

            mFillPaint.setColor(0xFFEEEEEE);
            mFillPaint.setColor(0xFFEEEEEE);
            canvas.drawRect(getLineLeftX(0), top + PADDING_TOP, getLineRightX(count - 1), bottom - PADDING_BOTTOM, mFillPaint);

            for (int i = mMin; i < mMax; i++) {
                int lineX = getLineLeftX(i - mMin);


                // completed area
//                if (mCompletedLines.size() > 0) {
//                    int index = mCompletedLines.indexOfKey(i);
//                    if (index >= 0) {
//                        mFillPaint.setColor(mCompletedColor);
//                        canvas.drawRect(lineX, top + PADDING_TOP, lineX + tickWidth, bottom - PADDING_BOTTOM, mFillPaint);
//                    }
//                }

                if (selectLines.size() > 0) {
                    int index = selectLines.indexOfKey(i);

                    Log.d("draw_finish", "index is " + index);
                    if (selectLines.get(i) == null)
                        Log.d("draw_finish", "index:" + index + " is " + "null");

                    if (index >= 0 && selectLines.get(i).isFinished()) {
                        mFillPaint.setColor(mCompletedColor);
                        canvas.drawRect(lineX, top + PADDING_TOP, lineX + tickWidth, bottom - PADDING_BOTTOM, mFillPaint);
                    }
                }

//                if (mCleanupLines.size() > 0) {
//                    int key = mCleanupLines.indexOfKey(i);
//                    if (key >= 0) {
//                        mFillPaint.setColor(0xFF9900FF);
//                        canvas.drawRect(lineX, top + PADDING_TOP, lineX + tickWidth, bottom - PADDING_BOTTOM, mFillPaint);
//                    }
//                }

                // border
                mStokePaint.setStrokeWidth(1);
                mStokePaint.setColor(0x80dddddd);
                canvas.drawRect(lineX, top + PADDING_TOP, lineX + tickWidth, bottom - PADDING_BOTTOM, mStokePaint);
            }


            // selected area
            if (mLeftToRight) {
                int startLineX = getLineCenterX(mStart - mMin);
                int endLineX = getLineCenterX(mEnd - mMin);
                mFillPaint.setColor(0x80FFFF00);
                if (isReverse()) {
                    canvas.drawRect(endLineX - tickWidth / 2, top + PADDING_TOP, startLineX + tickWidth / 2, bottom - PADDING_BOTTOM, mFillPaint);
                } else {
                    canvas.drawRect(startLineX - tickWidth / 2, top + PADDING_TOP, endLineX + tickWidth / 2, bottom - PADDING_BOTTOM, mFillPaint);
                }
            } else {
                int startLineX = getLineCenterX(mStart - mMin);
                int endLineX = getLineCenterX(mEnd - mMin);
                mFillPaint.setColor(0x80FFFF00);
                if (isReverse()) {
                    canvas.drawRect(startLineX - tickWidth / 2, top + PADDING_TOP, endLineX + tickWidth / 2, bottom - PADDING_BOTTOM, mFillPaint);

                } else {
                    canvas.drawRect(endLineX - tickWidth / 2, top + PADDING_TOP, startLineX + tickWidth / 2, bottom - PADDING_BOTTOM, mFillPaint);
                }
            }

            // bar
//            int barY = getHeight() - PADDING_BOTTOM;
//            mStokePaint.setStrokeWidth(16);
//            mStokePaint.setColor(0xFFe8e800);
//            canvas.drawLine(getLineLeftX(isReverse() ? mEnd : mStart), barY, getLineRightX(isReverse() ? mStart : mEnd), barY, mStokePaint);

            // thumbs
            drawThumb(canvas, mStartThumbDrawable, mStartThumbLineColor, isReverse() ? GRAVITY_RIGHT : GRAVITY_LEFT, mStart - mMin);
            drawThumb(canvas, mEndThumbDrawable, mEndThumbLineColor, isReverse() ? GRAVITY_LEFT : GRAVITY_RIGHT, mEnd - mMin);
        }


    }

    private void drawThumb(Canvas canvas, Drawable drawable, int lineColor, int gravity, int lineIndex) {

        int left;
        int halfThumbWidth = mThumbWidth / 2;
        int thumbWidth = mThumbWidth;

        if (mLeftToRight) {
            switch (gravity) {
                case GRAVITY_LEFT:
                    left = (int) (firstLineX + tickWidth * lineIndex - halfThumbWidth);
                    break;
                case GRAVITY_RIGHT:
                    left = (int) (firstLineX + tickWidth * lineIndex - halfThumbWidth);
                    break;
                default:
                    left = (int) (firstLineX + tickWidth * lineIndex);
            }
        } else {
            switch (gravity) {
                case GRAVITY_LEFT:
                    left = (int) (firstLineX - tickWidth * lineIndex - halfThumbWidth);
                    break;
                case GRAVITY_RIGHT:
                    left = (int) (firstLineX - tickWidth * lineIndex - halfThumbWidth);
                    break;
                default:
                    left = (int) (firstLineX - tickWidth * lineIndex);
            }
        }

        int areaTop = PADDING_TOP;
        int areaBottom = getHeight() - PADDING_BOTTOM - halfThumbWidth;


        String str = String.valueOf((lineIndex + 1 + mMin));
        mFillPaint.setColor(0xFF999999);
        mFillPaint.setTextSize(24);
        float strWidth = mFillPaint.measureText(str);
        canvas.drawText(str, left + halfThumbWidth - strWidth / 2, areaTop - 8, mFillPaint);

        mStokePaint.setStrokeWidth(4);
        mStokePaint.setColor(lineColor);
        canvas.drawLine(left + halfThumbWidth, areaTop, left + halfThumbWidth, areaBottom, mStokePaint);

        int right = left + thumbWidth;
        int top = areaBottom - halfThumbWidth;
        int bottom = top + thumbWidth;
        Rect rect = new Rect(left, top, right, bottom);
        Log.d("drawable index", "index :" + lineIndex + "," + rect.toString());
        canvas.save();
        drawable.setState(getDrawableState());
        drawable.setBounds(rect);
        drawable.draw(canvas);
        canvas.restore();

    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }


//    public int get

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public boolean isReverse() {
        return mReverse;
    }

    public void setStart(int index) {
        if (mStart != index) {
            mStart = index;
            if (mStart > mMax) mStart = mMax;
            if (mStart < mMin) mStart = mMin;
            mReverse = mStart > mEnd;
            if (mOnSeekBarChangeListener != null)
                mOnSeekBarChangeListener.onRangeChanged(this, mStart, mEnd);
            invalidate();

//            Debugger.out("Start= " + index);
        }
    }

    public void setEnd(int index) {
        if (mEnd != index) {
            mEnd = index;
            if (mEnd > mMax) mEnd = mMax;
            if (mEnd < mMin) mEnd = mMin;
            mReverse = mStart > mEnd;
            if (mOnSeekBarChangeListener != null)
                mOnSeekBarChangeListener.onRangeChanged(this, mStart, mEnd);
            invalidate();


//            Debugger.out("End= " + index);
        }
    }

    public void setMin(int min) {

        mMin = min;
        mCount = mMax - mMin + 1;
        setStart(mMin);
        setEnd(mMax);
//        mCompletedLines.clear();
        selectLines.clear();
        invalidate();


    }

    public void setMax(int max) {
        mMax = max;
        mCount = mMax - mMin + 1;
        setStart(mMin);
        setEnd(mMax);
        selectLines.clear();
        invalidate();

//        Debugger.out("max= " + max + ", count=" + mCount);
    }

//    public void setCompletedLines(int[] completedLineIndexes) {
//        mCompletedLines.clear();
//        if (completedLineIndexes == null) return;
//        for (int i = 0; i < completedLineIndexes.length; i++) {
//            mCompletedLines.put(completedLineIndexes[i], completedLineIndexes[i]);
//        }
//    }

    public void setCompletedLines(List<AirLineStatus> lines) {
        selectLines.clear();
        if (lines == null) return;
        for (AirLineStatus item : lines) {
            selectLines.put(item.getIndex(), item);
        }
    }
//
//    public void setCleanupLines(int[] cleanupLineIndexes) {
//        mCleanupLines.clear();
//        if (cleanupLineIndexes == null) return;
//        for (int i = 0; i < cleanupLineIndexes.length; i++) {
//            mCleanupLines.put(cleanupLineIndexes[i], cleanupLineIndexes[i]);
//        }
//    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    public void setLeftToRight(boolean leftToRight) {
        mLeftToRight = leftToRight;
    }


    public interface OnSeekBarChangeListener {

        void onRangeChanged(AreaSeek seek, int start, int end);

        //        void onStartTrackingTouch(AreaSeek seekBar);
        void onStopTrackingTouch(AreaSeek seekBar);

    }
}

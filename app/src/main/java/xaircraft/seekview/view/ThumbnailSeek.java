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
    private Rect rectBar;

    private int bStartCount;
    private int bEndCount;

    private int dragColor = 0X88C7AFAF;
    private int dragBorderColor = 0Xff000000;

    private int barColor = 0xFFFFFFFF;
    private int barBorderColor = 0X88C7AFAF;

    private OnDragBarListener mDragListener;

    private List<? extends ILineStatus> lines;

    private float perPxLine;
    private float perLinePx;


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

        mStartThumbDrawable = getResources().getDrawable(R.drawable.widget_area_seekbar_btn_start);


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

    public boolean clickDrawable(int x, int y) {
        return dragRect.contains(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (clickDrawable(x, y)) {
                dClicked = true;
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (dClicked) {
                int x = (int) event.getX();
                isFirstDraw = false;
                dragRect.left = x - dragWidth / 2;
                dragRect.right = x + dragWidth / 2;

                if (dragRect.left < rectBar.left) {
                    dragRect.left = (int) rectBar.left;
                    dragRect.right = dragRect.left + dragWidth;
                } else if (dragRect.right > rectBar.right) {
                    dragRect.right = (int) rectBar.right;
                    dragRect.left = (int) rectBar.right - dragWidth;
                }

                float startCountS = (float) (dragRect.left - rectBar.left) / (rectBar.width());
                float endCountS = (float) (dragRect.right - rectBar.left) / (rectBar.width());
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
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            dClicked = false;
        }

        return super.onTouchEvent(event);
    }

    public void setCountScale(int count, int showNumber) {
        mCount = count;
        if (mCount > showNumber)
            dragScale = (float) showNumber / mCount;
        else
            throw new RuntimeException("showNumber > count");
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        if (mCount > 0) {
            String startStr = String.valueOf(1);
            String endStr = String.valueOf(mCount);
            mFillPaint.setTextSize(30);
            float sTextWidth = mFillPaint.measureText(startStr);
            float eTextWidth = mFillPaint.measureText(endStr);
            Rect parentR = new Rect(0, 0, width, height);
            drawTextToRect(startStr, endStr, parentR, TEXT_GRAVITY_INTERNAL, canvas);


            //画矩形
            mFillPaint.setColor(barColor);
            int rLeft = (int) sTextWidth + 2;
            int rTop = height / 2 - barHeight / 2;
            int rRight = width - (int) eTextWidth;
            int rBottom = height / 2 + barHeight / 2;
            rectBar = new Rect(rLeft, rTop, rRight, rBottom);
            perPxLine = (float) mCount / rectBar.width();
            perLinePx = (float) rectBar.width() / mCount;
            canvas.drawRect(rectBar, mFillPaint);
            //画矩形边框
            mStrokePaint.setColor(barBorderColor);
            canvas.drawRect(rectBar, mStrokePaint);

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
                    if ((i +1 < lines.size() && !lines.get(i + 1).isFinished())) {
                        drawaFinishLines(startNumber, finishCount, canvas);
                        Log.d("draw_finish_line", "start position:" + startNumber + ", finish count:" + finishCount);
                        finishCount = 0;
                    } else if (i + 1 == lines.size() && finishCount > 0) {
                        drawaFinishLines(startNumber, finishCount, canvas);
                        Log.d("draw_finish_line", "start position:" + startNumber + ", finish count:" + finishCount);
                        finishCount = 0;
                    }
                }
            }

            //画拖动框
            if (isFirstDraw) {
                dragWidth = (int) ((rRight - rLeft) * dragScale);
                int Left = (int) sTextWidth + 2;
                int Top = height / 2 - dragHeight / 2;
                int Right = Left + dragWidth;
                int Bottom = Top + dragHeight;
                dragRect.set(Left, Top, Right, Bottom);
                mFillPaint.setColor(dragColor);
                canvas.drawRect(dragRect, mFillPaint);
                mStrokePaint.setColor(dragBorderColor);
                canvas.drawRect(dragRect, mStrokePaint);

            } else {

                mFillPaint.setColor(dragColor);
                canvas.drawRect(dragRect, mFillPaint);
                mStrokePaint.setColor(dragBorderColor);
                canvas.drawRect(dragRect, mStrokePaint);
                Log.d("draw rect", dragRect.toString());
                //画拖动框上面的文字

                String startCountStr = String.valueOf(bStartCount + 1);
                String endCountStr = String.valueOf(bEndCount + 1);
                drawTextToRect(startCountStr, endCountStr, dragRect, TEXT_GRAVITY_TOP, canvas);

            }


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
                canvas.drawText(startStr, rect.left, rect.centerY() - mTop / 2 - mBottom / 2, mFillPaint);
                canvas.drawText(endStr, rect.right - eTextWidth, rect.centerY() - mTop / 2 - mBottom / 2, mFillPaint);
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
        this.mDragListener = listener;
    }

    public interface OnDragBarListener {
        void OnChange(int start, int end, List<? extends ILineStatus> lines);
    }

    public void setLines(List<? extends ILineStatus> lines) {
        if (lines != null && lines.size() > 0)
            this.lines = lines;
    }

    public void drawaFinishLines(int start, int count, Canvas canvas) {
        //当只有一个像素的时候需要几条航线，如果小于这个值就不画线
        if (count > perPxLine) {
            int left = (int) (rectBar.left + start * perLinePx + 0.5);
            int top = (int) rectBar.top;
            int right = (int) (left + perLinePx * count);
            int bottom = (int) rectBar.bottom;
            Rect rect = new Rect(left, top, right, bottom);
            mFillPaint.setColor(0XFF00FF00);
            Log.d("draw_finish_rect", rect.toString());
            canvas.drawRect(rect, mFillPaint);
        }

    }
}

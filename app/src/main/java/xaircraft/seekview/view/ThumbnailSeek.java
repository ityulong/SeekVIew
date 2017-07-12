package xaircraft.seekview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import xaircraft.seekview.R;
import xaircraft.seekview.help.Utils;

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
    private static final int DRAG_HEIGHT= 35;

    private int barHeight;

    private int dragWidth = 100;
    private int dragHeight;


    private boolean dClicked = false;
    private boolean isFirstDraw = true;

    private Rect dragRect = new Rect();
    private float dragScale;
    private RectF rectBar;

    private int bStartCount;
    private int bEndCount;

    private OnDragBarListener mDragListener;

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
        // TODO Auto-generated method stub
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
            if(dClicked){
                int x = (int) event.getX();
                isFirstDraw = false;
                dragRect.left = x - dragWidth / 2;
                dragRect.right = x + dragWidth / 2;

                if(dragRect.left < rectBar.left){
                    dragRect.left = (int)rectBar.left;
                    dragRect.right = dragRect.left + dragWidth;
                }else if(dragRect.right > rectBar.right){
                    dragRect.right = (int)rectBar.right;
                    dragRect.left = (int)rectBar.right - dragWidth;
                }

                float startCountS = (float)(dragRect.left - rectBar.left) / (rectBar.width());
                float endCountS = (float)(dragRect.right - rectBar.left) / (rectBar.width());
                bStartCount = (int)(1000 * startCountS);
                bEndCount = (int)(1000 * endCountS);
                if (mDragListener != null) {
                    mDragListener.OnChange(bStartCount, bEndCount);
                }
                invalidate();
            }
            return true;
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            dClicked = false;
        }

        return super.onTouchEvent(event);
    }

    public void setCountScale(int count, int showNumber) {
        mCount = count;
        if(mCount > showNumber)
            dragScale = (float)showNumber/mCount;
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
            mFillPaint.setColor(0x88008866);
            int rLeft = (int) sTextWidth + 2;
            int rTop = height / 2 - barHeight / 2;
            int rRight = width - (int) eTextWidth;
            int rBottom = height / 2 + barHeight / 2;
            rectBar = new RectF(rLeft, rTop, rRight, rBottom);
            canvas.drawRoundRect(rectBar, 25, 25, mFillPaint);

            //画拖动框
            if(isFirstDraw){
                dragWidth = (int)((rRight - rLeft) * dragScale);
                int Left = (int) sTextWidth + 2;
                int Top = height / 2 - dragHeight / 2;
                int Right = Left + dragWidth;
                int Bottom = Top + dragHeight;
                dragRect.set(Left, Top, Right, Bottom);
                mFillPaint.setColor(0x88ffffff);
                canvas.drawRect(dragRect,mFillPaint);
            }else{

                mFillPaint.setColor(0x88ffffff);
                canvas.drawRect(dragRect,mFillPaint);
                Log.d("draw rect", dragRect.toString());
                //画拖动框上面的文字

                String startCountStr = String.valueOf(bStartCount);
                String endCountStr = String.valueOf(bEndCount);
                drawTextToRect(startCountStr, endCountStr, dragRect, TEXT_GRAVITY_TOP, canvas);

            }
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mStartThumbDrawable.setState(getDrawableState());
    }

    public void drawTextToRect(String startStr, String endStr, Rect rect, int gravity,Canvas canvas) {

        mFillPaint.setColor(0xFF999999);
        float sTextWidth = mFillPaint.measureText(startStr);
        float eTextWidth = mFillPaint.measureText(endStr);
        switch (gravity){
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

    public void setDragListener(OnDragBarListener listener){
        this.mDragListener = listener;
    }

    public interface OnDragBarListener{
        void OnChange(int start,int end);
    }
}

package com.cuixuesen.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoxDrawingView extends View {
    private static final String TAG = BoxDrawingView.class.getSimpleName();

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    // 保存数据状态，用Bundle
    private Bundle savedStateBundle;

    // Used when creating the view in code
    public BoxDrawingView(Context context) {
        super(context);
    }

    // Used when inflating the view from XML
    public BoxDrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Paint the boxes a nice semitransparent red (ARGB)
        mBoxPaint= new Paint();
        mBoxPaint.setColor(0x22ff0000);

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box: mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.save();
            // 旋转画布
            canvas.rotate(box.getCurrentAngle(), box.getCenter().x, box.getCenter().y);
            // 确定矩形四个顶点的位置配上画笔即可
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
//                    mCurrentBox.setCurrent(current);
//                    invalidate();
                    if (event.getPointerCount() == 1 && mCurrentBox.getCurrentAngle() == 0) {
                        // 如果只有一个手指按下，而且还未曾旋转过的话，就进行大小的缩放
                        mCurrentBox.setCurrent(current);
                    }
                    // 如果按下了两根手指
                    if (event.getPointerCount() == 2) {
                        // 获取角度 tanA = a 等于 aTana = A 返回值是弧度，弧度转角度，* 180 / pai
                        float angle = (float)(Math.atan((event.getY(1) - event.getY(0)) / (event.getX(1) - event.getX(0))) * 180 / Math.PI);
                        Log.i(TAG, "onTouchEvent: angle:" + (angle - mCurrentBox.getOriginAngle()));
                        // 已旋转的角度 = 之前旋转的角度 + 新旋转的角度
                        // 新旋转的角度 = 本次move到的角度 - 手指按下的角度
                        mCurrentBox.setCurrentAngle(mCurrentBox.getCurrentAngle() + angle - mCurrentBox.getOriginAngle());
                        mCurrentBox.setOriginAngle(angle);
                    }
                    invalidate();
                }
                break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    action = "ACTION_POINTER_DOWN";
                    // 两根手指同时按下
                    if (event.getPointerCount() == 2) {
                        // 首先获取按下时的角度（有一个弧度转角度的过程）
                        // 每次按下的时候将角度存入现在矩形的原始角度
                        float angle = (float) (Math.atan((event.getY(1) - event.getY(0)) / (event.getX(1)-event.getX(0))) * 180 / Math.PI);
                        mCurrentBox.setOriginAngle(angle);
                    }
                    break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
            default:
                break;
        }

        Log.i(TAG, "onTouchEvent: " + action + " at x=" + current.x + ", y=" + current.y);

        return true;
    }

    /**
     * 挑战练习31.5 设备旋转问题
     *
     * 1. 重写View的以下方法，用来保存数据和恢复数据
     * protected Parcelable onSaveInstanceState(){}
     * protected void onRestoreInstanceState(Parcelable state) {}
     *
     * 2. View视图需要有ID，只有这样，上面两个方法才会被调用
     * 在XML中增加 id box_drawing_view
     *
     * 3. 因为实现Parcelable比较繁琐，用Bundle保存状态数据
     *
     * 4. 需要保存View父视图的状态，在Bundle中保存super.onSaveInstanceState方法结果，然后调用super.onRestoreInstanceState(restoreState)
     * 把结果发送给超类
     */

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        savedStateBundle = new Bundle();
        // 在bundle中保存父视图状态，恢复状态时会将此发送给超类
        savedStateBundle.putParcelable("onSaveInstanceState", super.onSaveInstanceState());
        // 保存当前的状态数据，bundle保存List<Object>，强转Serializable
        savedStateBundle.putSerializable("mBoxen", (Serializable) mBoxen);
        return savedStateBundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        // 将父视图状态取出，发送给超类
        Parcelable restoreState = bundle.getParcelable("onSaveInstanceState");
        super.onRestoreInstanceState(restoreState);
        // 恢复状态数据
        mBoxen = (List<Box>) bundle.getSerializable("mBoxen");
    }
}

package com.cuixuesen.draganddraw;

import android.graphics.PointF;

public class Box {
    private PointF mOrigin;
    private PointF mCurrent;

    // 此次按下的角度
    private float mOriginAngle;
    private float mCurrentAngle;

    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        mOriginAngle = 0;
        mCurrentAngle = 0;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public void setOrigin(PointF origin) {
        mOrigin = origin;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public float getOriginAngle() {
        return mOriginAngle;
    }

    public void setOriginAngle(float originAngle) {
        mOriginAngle = originAngle;
    }

    public float getCurrentAngle() {
        return mCurrentAngle;
    }

    public void setCurrentAngle(float currentAngle) {
        mCurrentAngle = currentAngle;
    }

    public PointF getCenter() {
        return new PointF((mCurrent.x + mOrigin.x) / 2, (mCurrent.y + mOrigin.y) / 2);
    }
}

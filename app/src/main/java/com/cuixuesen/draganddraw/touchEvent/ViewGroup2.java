package com.cuixuesen.draganddraw.touchEvent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

// https://libill.github.io/2019/09/09/android-touch-event/ 学习笔记
public class ViewGroup2 extends LinearLayout {
    private final static String TAG = ViewGroup2.class.getName();

    public ViewGroup2(Context context) {
        super(context);
    }

    public ViewGroup2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(TAG, "dispatchTouchEvent: action " + StringUtils.getMotionEventName(ev));
        boolean superReturn = super.dispatchTouchEvent(ev);
        Log.d(TAG, "dispatchTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + superReturn);
        return superReturn;
        // 返回true使事件不再分发
//        Log.d(TAG, "dispatchTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + true);
//        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i(TAG, "onInterceptTouchEvent: action " + StringUtils.getMotionEventName(ev));
//        boolean superReturn = super.onInterceptTouchEvent(ev);
//        Log.d(TAG, "onInterceptTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + superReturn);
//        return superReturn;
        // 返回true把事件拦截了
        Log.d(TAG, "onInterceptTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + true);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.i(TAG, "onTouchEvent: action " + StringUtils.getMotionEventName(ev));
//        boolean superReturn = super.onTouchEvent(ev);
//        Log.d(TAG, "onTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + superReturn);
//        return superReturn;
        // 返回true把事件处理了
        Log.d(TAG, "onTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + true);
        return true;
    }
}

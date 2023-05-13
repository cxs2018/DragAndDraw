package com.cuixuesen.draganddraw.touchEvent;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cuixuesen.draganddraw.R;

public class TouchActivity extends AppCompatActivity {
    private static final String TAG = TouchActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(TAG, "dispatchTouchEvent: action " + StringUtils.getMotionEventName(ev));
        boolean superReturn = super.dispatchTouchEvent(ev);
        Log.d(TAG, "dispatchTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + superReturn);
        return superReturn;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.i(TAG, "onTouchEvent: action " + StringUtils.getMotionEventName(ev));
        boolean superReturn = super.onTouchEvent(ev);
        Log.d(TAG, "onTouchEvent: action " + StringUtils.getMotionEventName(ev) + " " + superReturn);
        return superReturn;
    }
}

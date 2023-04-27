package com.cuixuesen.draganddraw;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

public class SunsetFragment extends Fragment {

    private View mSceneView;
    private View mSunView;
    private View mSkyView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    private ObjectAnimator downAnimator;
    private AnimatorSet downAnimatorSet;
    private ObjectAnimator upAnimator;
    private AnimatorSet upAnimatorSet;

    private ObjectAnimator sunsetSkyAnimator;
    private ObjectAnimator sunriseSkyAnimator;
    private ObjectAnimator enterNightAnimator;
    private ObjectAnimator enterDawnAnimator;

    private float sunYFirstStart;
    private float sunYFirstEnd;

    private boolean isSunDown = true; // 是否落日
    private boolean isFirstClick = true; // 是否第一次点击

    public static SunsetFragment newInstance() {
       return new SunsetFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSkyView = view.findViewById(R.id.sky);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        // 先初始化天空颜色变化动画
        initSkyAnimation();

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startAnimation();
                // 第一次点击初始化
                if (isFirstClick) {
                    sunYFirstStart = mSunView.getTop();
                    sunYFirstEnd = mSkyView.getHeight();
                    initUpDownAnimation();
                }
                isFirstClick = false;
                // 如果没有在执行动画，是可以改变的
                if (!downAnimatorSet.isRunning() && !upAnimatorSet.isRunning()) {
                    // 判断太阳是否在上方
                    if (isSunDown) {
                        downAnimatorSet.start();
                    } else {
                        upAnimatorSet.start();
                    }
                    isSunDown = !isSunDown;
                }
            }
        });

        return view;
    }

    private void startAnimation() {
        float sunYStart = mSunView.getTop();
        float sunYEnd = mSkyView.getHeight();

        ObjectAnimator heightAnimator = ObjectAnimator.ofFloat(mSunView, "y", sunYStart, sunYEnd).setDuration(1500);
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator.ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor).setDuration(1500);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());

        ObjectAnimator sunriseSkyAnimator = ObjectAnimator.ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor).setDuration(1500);
        sunriseSkyAnimator.setEvaluator(new ArgbEvaluator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(heightAnimator).with(sunsetSkyAnimator).before(sunriseSkyAnimator);
        animatorSet.start();
    }

    private void initUpDownAnimation() {
        // 太阳降落的动画
        downAnimator = ObjectAnimator.ofFloat(mSunView, "y", sunYFirstStart, sunYFirstEnd).setDuration(1500);
        downAnimator.setInterpolator(new AccelerateInterpolator());
        downAnimatorSet = new AnimatorSet();
        downAnimatorSet.play(downAnimator).with(sunsetSkyAnimator).before(enterNightAnimator);

        // 太阳升起的动画
        upAnimator = ObjectAnimator.ofFloat(mSunView, "y", sunYFirstEnd, sunYFirstStart).setDuration(1500);
        upAnimator.setInterpolator(new AccelerateInterpolator());
        upAnimatorSet = new AnimatorSet();
        upAnimatorSet.play(upAnimator).with(sunriseSkyAnimator).after(enterDawnAnimator);
    }

    private void initSkyAnimation() {
        // 落日的天空颜色变化
        sunsetSkyAnimator = ObjectAnimator.ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor).setDuration(1500);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());

        // 日出的天空颜色变化
        sunriseSkyAnimator = ObjectAnimator.ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mBlueSkyColor).setDuration(1500);
        sunriseSkyAnimator.setEvaluator(new ArgbEvaluator());

        // 进入夜幕的天空颜色变化
        enterNightAnimator= ObjectAnimator.ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor).setDuration(1500);
        enterNightAnimator.setEvaluator(new ArgbEvaluator());

        // 进入夜幕的天空颜色变化
        enterDawnAnimator= ObjectAnimator.ofInt(mSkyView, "backgroundColor", mNightSkyColor, mSunsetSkyColor).setDuration(1500);
        enterDawnAnimator.setEvaluator(new ArgbEvaluator());
    }
}
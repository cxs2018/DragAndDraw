package com.cuixuesen.draganddraw;

import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class DragAndDrawActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return DragAndDrawFragment.newInstance();
    }
}
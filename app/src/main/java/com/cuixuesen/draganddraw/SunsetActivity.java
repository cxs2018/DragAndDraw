package com.cuixuesen.draganddraw;

import androidx.fragment.app.Fragment;

public class SunsetActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return SunsetFragment.newInstance();
    }
}
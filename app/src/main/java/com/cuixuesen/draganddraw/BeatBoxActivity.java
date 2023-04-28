package com.cuixuesen.draganddraw;

import androidx.fragment.app.Fragment;

public class BeatBoxActivity extends SingleFragmentActivity{
    @Override
    public Fragment createFragment() {
        return BeatBoxFragment.newInstance();
    }
}

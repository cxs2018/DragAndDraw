package com.cuixuesen.draganddraw;

import androidx.fragment.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity{

    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}

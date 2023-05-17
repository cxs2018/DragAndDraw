package com.cuixuesen.draganddraw.photoGallery;

import androidx.fragment.app.Fragment;

import com.cuixuesen.draganddraw.SingleFragmentActivity;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}

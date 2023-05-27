package com.cuixuesen.draganddraw.photoGallery;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.cuixuesen.draganddraw.SingleFragmentActivity;

public class PhotoGalleryActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}

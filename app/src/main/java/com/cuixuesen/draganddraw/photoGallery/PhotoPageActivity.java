package com.cuixuesen.draganddraw.photoGallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.cuixuesen.draganddraw.SingleFragmentActivity;

public class PhotoPageActivity extends SingleFragmentActivity {
    private PhotoPageFragment mFragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent intent = new Intent(context, PhotoPageActivity.class);
        intent.setData(photoPageUri);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        mFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return mFragment;
    }

    /**
     * 挑战练习 Webview 使用后退键浏览历史网页
     * 重写Webview所在Fragment绑定的Activity的onBackPressed方法
     */
    @Override
    public void onBackPressed() {
        WebView webView = mFragment.getWebView();
        // canGoBack 判断是否有历史信息
        if (webView.canGoBack()) {
            // 如果有的话，就利用goBack回到前一个历史网页
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

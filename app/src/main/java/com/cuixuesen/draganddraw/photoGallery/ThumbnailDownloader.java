package com.cuixuesen.draganddraw.photoGallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    public static final int MESSAGE_DOWNLOAD = 0;
    public static final int MESSAGE_PRELOAD = 1;
    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    // 创建一个变量用于储存Handler变量的值
    private Handler mResponseHandler;
    private ThumbnailDownloaderListener<T> mThumbnailDownloaderListener;

    private LruCache<String, Bitmap> mCache;

    // 新增一个用来在请求者和结果之间通信的监听器接口
    public interface ThumbnailDownloaderListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    // 设置 ThumbnailDownloaderListener 监听器的方法
    public void setThumbnailDownloaderListener(ThumbnailDownloaderListener<T> listener) {
        mThumbnailDownloaderListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    /**
     * 该方法是在looper首次检查消息队列之前调用的，是建立Handler的最好地方
     */
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                // 先检查消息类型，再获取obj的值（T类型下载请求）
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                } else if (msg.what == MESSAGE_PRELOAD) {
                    String urlToPreload = (String) msg.obj;
                    handlePreload(urlToPreload);
                    Log.i(TAG, "handleMessage: Preload URL " + urlToPreload);
                }
            }
        };
        int maxCacheSize = 4 * 1024 * 1024; // 4MB
        mCache = new LruCache<>(maxCacheSize);
    }

    /**
     * 线程退出通知方法
     * @return
     */
    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    /**
     * 存根方法
     * @param target
     * @param url
     */
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    /**
     * 预加载
     * @param url
     */
    public void queuePreloadThumbnail(String url) {
            mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mResponseHandler.removeMessages(MESSAGE_PRELOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }

            // 传给FlickrFetchr新实例，并把返回的字节转化成位图
//            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
//            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
//            Log.i(TAG, "handleRequest: Bitmap created");
            final Bitmap bitmap;
            if (mCache.get(url) == null) {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                Log.i(TAG, "handleRequest: Bitmap created");
                mCache.put(url, bitmap);
            } else {
                bitmap = mCache.get(url);
                Log.i(TAG, "handleRequest: Bitmap from cache");
            }
            // 发布Message
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "handleRequest: Error downloading image", e);
        }
    }

    private void handlePreload(String url) {
        try {
            if (url == null) {
                return;
            }
            if (mCache.get(url) == null) {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                Log.i(TAG, "handleRequest: preload created");
                mCache.put(url, bitmap);
            }
        } catch (IOException e) {
            Log.e(TAG, "handleRequest: Error preloading image", e);
        }
    }
}

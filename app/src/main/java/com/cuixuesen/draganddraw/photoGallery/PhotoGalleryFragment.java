package com.cuixuesen.draganddraw.photoGallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cuixuesen.draganddraw.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;

    private List<GalleryItem> mItems = new ArrayList<>();

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    private int mNextPage = 1, mLastPosition;
    private final int MAX_PLACES = 3;

    private PhotoAdapter mPhotoAdapter;
    private FetchItemsTask mFetchItemsTask;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setHasOptionsMenu(true);

        updateItems();

//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);
//        PollService.setServiceAlarm(getActivity(), true);

        Handler responseHandler = new Handler();
        // 创建线程
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloaderListener(
                new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        // 用返回的Bitmap执行UI更新操作
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                }
        );
        // 启动线程
        mThumbnailDownloader.start();
        // 要在start方法之后调用getLooper方法，这是一种保证线程就绪的处理方式
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.photo_recycler_view);
//        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        // 挑战练习：添加动态调整网格
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i(TAG, "onGlobalLayout: " + mPhotoRecyclerView.getWidth() + mLastPosition);
                int columns = mPhotoRecyclerView.getWidth() / 240;
                mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
                mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
//                mPhotoRecyclerView.addOnScrollListener(onBottomListener);
                mPhotoRecyclerView.getLayoutManager().scrollToPosition(mLastPosition);
            }
        });

//        mPhotoRecyclerView.setOnScrollListener(onBottomListener);

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                if (searchView != null) {
                    // 得到输入管理对象
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        // 这将让键盘在所有的情况下都被隐藏，但是我们一般在点击搜索按钮后，输入法都会乖乖的自动隐藏
                        // 输入法如果是显示状态，那么就隐藏输入法
                        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }
                    searchView.onActionViewCollapsed(); // 不获取焦点
                }
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
//        if (PollService.isServiceAlarmOn(getActivity())) {
//            toggleItem.setTitle(R.string.stop_polling);
//        } else {
//            toggleItem.setTitle(R.string.start_polling);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        mFetchItemsTask = new FetchItemsTask(query);
        mFetchItemsTask.execute();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoAdapter = new PhotoAdapter(mItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            // 打开浏览器
//            Intent intent = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
//            startActivity(intent);
            // 打开webview
            Intent intent = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(intent);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;


        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
             // 为前十个和后十个预加载
//            for(int i = Math.max(0, position - 10); i < Math.min(mGalleryItems.size() - 1, position + 10); i++) {
//                Log.e(TAG, "onBindViewHolder: Preload position " + i);
//                mThumbnailDownloader.queuePreloadThumbnail(mGalleryItems.get(i).getUrl());
//            }
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
//            try {
//                String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
//                Log.i(TAG, "doInBackground: Fetched contents of URL: " + result);
//            } catch (IOException e) {
//                Log.e(TAG, "doInBackground: Failed to fetch URL: " + e );
//            }
//            return new FlickrFetchr().fetchItems(mNextPage);

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems.addAll(galleryItems);
            setupAdapter();
        }
    }

    // 挑战练习：分页加载
    private RecyclerView.OnScrollListener onBottomListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            mLastPosition = layoutManager.findLastVisibleItemPosition();
            if (mPhotoAdapter == null) {
                return;
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastPosition >= mPhotoAdapter.getItemCount() - 1) {
                if (mFetchItemsTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mNextPage++;
                    if (mNextPage <= MAX_PLACES) {
                        Toast.makeText(getActivity(), "waiting to load...", Toast.LENGTH_SHORT).show();
                        updateItems();
                    } else {
                        Toast.makeText(getActivity(), "This is the end.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };
}

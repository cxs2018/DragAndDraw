package com.cuixuesen.draganddraw;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;

    private List<GalleryItem> mItems = new ArrayList<>();

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
        mFetchItemsTask = new FetchItemsTask();
        mFetchItemsTask.execute();
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
                mPhotoRecyclerView.addOnScrollListener(onBottomListener);
                mPhotoRecyclerView.getLayoutManager().scrollToPosition(mLastPosition);
            }
        });

        mPhotoRecyclerView.setOnScrollListener(onBottomListener);

        setupAdapter();

        return v;
    }

    private void updateItems() {
        mFetchItemsTask = new FetchItemsTask();
        mFetchItemsTask.execute();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoAdapter = new PhotoAdapter(mItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
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
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
//            try {
//                String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
//                Log.i(TAG, "doInBackground: Fetched contents of URL: " + result);
//            } catch (IOException e) {
//                Log.e(TAG, "doInBackground: Failed to fetch URL: " + e );
//            }
            return new FlickrFetchr().fetchItems(mNextPage);
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

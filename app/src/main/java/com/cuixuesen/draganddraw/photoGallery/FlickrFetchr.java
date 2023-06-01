package com.cuixuesen.draganddraw.photoGallery;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";

    private static final String API_KEY = "8c58614a37b6e585abe6c233d17b7dc3";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    public static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest")
            .buildUpon()
//            .appendQueryParameter("method", "flickr.photos.getRecent")
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
//            .appendQueryParameter("page", "" + nextPage)
            .build();

    /**
     * 从指定URL获取原始数据并返回一个字节流数组
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 将getUrlBytes(String)方法返回的结果转换为String
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {

        List<GalleryItem> items = new ArrayList<>();

        try {
            // "https://api.flickr.com/services/rest/"
            // https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=8c58614a37b6e585abe6c233d17b7dc3&format=json&nojsoncallback=1&extras=url_s&page=1
//            String url = Uri.parse("https://api.flickr.com/services/rest/")
//                    .buildUpon()
//                    .appendQueryParameter("method", "flickr.photos.getRecent")
//                    .appendQueryParameter("api_key", API_KEY)
//                    .appendQueryParameter("format", "json")
//                    .appendQueryParameter("nojsoncallback", "1")
//                    .appendQueryParameter("extras", "url_s")
//                    .appendQueryParameter("page", "" + nextPage)
//                    .build().toString();
            Log.i(TAG, "fetchItems: start: " + url);
            String jsonString = getUrlString(url);
            Log.i(TAG, "fetchItems: Received JSON" + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
            Log.i(TAG, "fetchItems: GalleryItem list: " + items.size());
        } catch (IOException e) {
            Log.e(TAG, "fetchItems: Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "fetchItems: Failed to parse JSON", e);
        }

        return items;
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method);

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }

    private String buildUrl(Location location) {
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("lat", "" + location.getLatitude())
                .appendQueryParameter("lon", "" + location.getLongitude())
                .build().toString();
    }

    public List<GalleryItem> searchPhotos(Location location) {
        String url = buildUrl(location);
        return downloadGalleryItems(url);
    }
}

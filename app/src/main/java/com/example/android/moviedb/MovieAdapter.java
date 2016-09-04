package com.example.android.moviedb;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieAdapter extends BaseAdapter {
    private Context mContext;
    private final int IMAGE_WIDTH_PHONE = 185;
    private final int IMAGE_WIDTH_TABLET = 185;
    final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w";
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private ArrayList<Movie> movies = new ArrayList<>();

    public MovieAdapter(Context c, ArrayList<Movie> mov) {
        mContext = c;
        movies = mov;
    }

    public int getCount() {
        return movies.size();
    }

    public Movie getItem(int position) {
        return movies.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }


    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int dpHeight = (int) (displayMetrics.heightPixels / displayMetrics.density);
        int dpWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);
        Log.v(LOG_TAG, "Height of phone is " + dpHeight + " and width of phone is " + dpWidth);

        //int scale = dpHeight/dpWidth;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            // layout suited for both tablets and phones
            if (dpWidth < 480)
            imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 850));
            else imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 280));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        if (movies.size() != 0) {
            if (dpWidth < 480)
            Picasso.with(mContext).load(POSTER_BASE_URL + IMAGE_WIDTH_PHONE + movies.get(position).getPosterPath()).into(imageView);
            else Picasso.with(mContext).load(POSTER_BASE_URL + IMAGE_WIDTH_TABLET + movies.get(position).getPosterPath()).into(imageView);
        }
        return imageView;
    }
}
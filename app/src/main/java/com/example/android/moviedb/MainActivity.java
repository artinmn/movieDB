package com.example.android.moviedb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter mAdapter;
    private ArrayList<Movie> movies = new ArrayList<>();
    private int page = 1;
    private int setting_option_selected = 0;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private final int IMAGE_WIDTH_PHONE = 185;
    private final int IMAGE_WIDTH_TABLET = 185;
    final String POPULAR_BASE_URL = "http://api.themoviedb.org/3/movie/popular?page=";
    final String TOP_RATED_BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?page=";
    final String UPCOMING_BASE_URL = "http://api.themoviedb.org/3/movie/upcoming?page=";
    final String NOW_PLAYING_BASE_URL = "http://api.themoviedb.org/3/movie/now_playing?page=";
    private String[] URL_ARRAY = {TOP_RATED_BASE_URL, POPULAR_BASE_URL, UPCOMING_BASE_URL, NOW_PLAYING_BASE_URL};
    private String[] TITLE_STRING = {"Top Rated", "Popular", "Upcoming", "Now Playing"};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FetchMoviePosterTask fetchMoviePosterTask = new FetchMoviePosterTask();
        fetchMoviePosterTask.execute(URL_ARRAY[setting_option_selected]+page);
        setContentView(R.layout.activity_main);
        addListenerOnButtonNext();
        addListenerOnButtonPrevious();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            final AlertDialog.Builder settings = new AlertDialog.Builder(MainActivity.this);
            settings.setTitle(R.string.menu_title).setSingleChoiceItems (R.array.settings_order, setting_option_selected,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        movies.clear();
                        page = 1;
                        FetchMoviePosterTask fetchMoviePosterTask = new FetchMoviePosterTask();
                        fetchMoviePosterTask.execute(URL_ARRAY[which]+page);
                        setting_option_selected = which;
                        dialog.dismiss();
                    }
                });
                settings.show();
            }
        return super.onOptionsItemSelected(item);
    }


    public void addListenerOnButtonNext() {

        ImageButton imageButton;
        imageButton = (ImageButton) findViewById(R.id.next);

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //mAdapter.clear();
                movies.clear();
                page++;
                FetchMoviePosterTask fetchMoviePosterTask = new FetchMoviePosterTask();
                fetchMoviePosterTask.execute(URL_ARRAY[setting_option_selected]+page);
            }
        });
    }

    public void addListenerOnButtonPrevious() {

        ImageButton imageButton;
        imageButton = (ImageButton) findViewById(R.id.previous);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (page != 1) {
                    movies.clear();
                    page--;
                    FetchMoviePosterTask fetchMoviePosterTask = new FetchMoviePosterTask();
                    fetchMoviePosterTask.execute(URL_ARRAY[setting_option_selected] + page);
                }
            }
        });
    }

    public class FetchMoviePosterTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviePosterTask.class.getSimpleName();

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // JSON objects keys
            final String RESULTS = "results";
            final String MOVIE_ID = "id";
            final String POSTER_PATH = "poster_path";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String ORIGINAL_TITLE = "original_title";
            final String VOTE_AVERAGE = "vote_average";
            final String VOTE_COUNT = "vote_count";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            for(int i = 0; i < movieArray.length(); i++) {
                int movieId;
                String posterPath;
                String originalTitle;
                String overview;
                String releaseDate;
                double voteAverage;
                int voteCount;

                JSONObject movieDetail = movieArray.getJSONObject(i);

                // data needed for movie object
                movieId = movieDetail.getInt(MOVIE_ID);
                posterPath = movieDetail.getString(POSTER_PATH);
                originalTitle = movieDetail.getString(ORIGINAL_TITLE);
                overview = movieDetail.getString(OVERVIEW);
                releaseDate = movieDetail.getString(RELEASE_DATE);
                voteAverage = movieDetail.getDouble(VOTE_AVERAGE);
                voteCount = movieDetail.getInt(VOTE_COUNT);

                // create movie object for each iteration
                movies.add(new Movie(movieId,posterPath,originalTitle,overview,releaseDate,voteAverage, voteCount));
            }
            return movies;
        }
        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // raw JSON response as a string
            String movieJsonStr = null;

            try {
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(params[0]).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();
                Log.v(LOG_TAG,"URL is " + builtUri);

                URL url = new URL(builtUri.toString());

                // request to website and make connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // add newline for debugging purposes
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // An error happened during getting data or parsing
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Movie> result) {

            mAdapter = new MovieAdapter(MainActivity.this, result);
            GridView gridview = (GridView) findViewById(R.id.gridview);
            DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();
            float dpHeight = (displayMetrics.heightPixels / displayMetrics.density);
            float dpWidth =  (displayMetrics.widthPixels / displayMetrics.density);
            setTitle(TITLE_STRING[setting_option_selected]+" - "+"page "+page);
            int IMAGE_WIDTH;
            if (dpWidth < 480)
                IMAGE_WIDTH = IMAGE_WIDTH_PHONE;
            else
                IMAGE_WIDTH = IMAGE_WIDTH_TABLET;
            float factor = Math.round(dpWidth/IMAGE_WIDTH);
            Log.v(LOG_TAG, "factor is :" + factor + "the number is :" + (dpWidth/IMAGE_WIDTH));
            gridview.setColumnWidth((int)(dpWidth/(factor)*displayMetrics.density));
            gridview.setAdapter(mAdapter);
            ((TextView) findViewById(R.id.page_number)).setText(Integer.toString(page));

            gridview.setOnScrollListener(new AbsListView.OnScrollListener() {
                ImageButton imageButtonNext = (ImageButton) findViewById(R.id.next);
                ImageButton imageButtonPrevious = (ImageButton) findViewById(R.id.previous);
                TextView textViewPageNumver = (TextView) findViewById(R.id.page_number);
                private int bottom = 0;

                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                    switch(scrollState) {
                        case 2: // SCROLL_STATE_FLING
                            imageButtonNext.setVisibility(View.VISIBLE);
                            imageButtonPrevious.setVisibility(View.VISIBLE);
                            //textViewPageNumver.setVisibility(View.GONE);
                            break;

                        case 1: // SCROLL_STATE_TOUCH_SCROLL
                            imageButtonNext.setVisibility(View.VISIBLE);
                            imageButtonPrevious.setVisibility(View.VISIBLE);
                            //textViewPageNumver.setVisibility(View.VISIBLE);
                            break;

                        case 0: // SCROLL_STATE_IDLE
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (bottom == 0) {
                                        imageButtonNext.setVisibility(View.GONE);
                                        imageButtonPrevious.setVisibility(View.GONE);
                                        //textViewPageNumver.setVisibility(View.GONE);
                                }
                                }
                            }, 2000);
                            break;

                        default:
                            imageButtonNext.setVisibility(View.GONE);
                            imageButtonPrevious.setVisibility(View.GONE);
                            //textViewPageNumver.setVisibility(View.GONE);
                            break;
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                {
                    if(firstVisibleItem + visibleItemCount >= totalItemCount){
                        bottom = 1;
                        Log.v(LOG_TAG, "bottom has been reached!");
                        imageButtonNext.setVisibility(View.VISIBLE);
                        imageButtonPrevious.setVisibility(View.VISIBLE);
                        textViewPageNumver.setVisibility(View.VISIBLE);
                    }
                    else {
                        bottom = 0;
                        textViewPageNumver.setVisibility(View.GONE);
                    }
                }
            });
            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    //int movie_id = result.get(position).getMovieId();
                    int[] movie_info = {result.get(position).getMovieId(), page, position};
                    Intent detailsIntent = new Intent(
                            MainActivity.this, DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movie_info);
                    startActivity(detailsIntent);
                }
            });
        }
    }
}

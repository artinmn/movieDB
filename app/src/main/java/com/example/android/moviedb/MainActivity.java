package com.example.android.moviedb;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

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
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private final int IMAGE_WIDTH_PHONE = 185;
    private final int IMAGE_WIDTH_TABLET = 185;
    final String POPULAR_BASE_URL =
            "http://api.themoviedb.org/3/movie/popular?";
    final String TOP_RATED_BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FetchMovieDataTask fetchMovieDataTask = new FetchMovieDataTask();
        fetchMovieDataTask.execute(TOP_RATED_BASE_URL);
    }

    public class FetchMovieDataTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

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

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            for(int i = 0; i < movieArray.length(); i++) {
                int movieId;
                String posterPath;
                String originalTitle;
                String overview;
                String releaseDate;
                double voteAverage;

                JSONObject movieDetail = movieArray.getJSONObject(i);

                // data needed for movie object
                movieId = movieDetail.getInt(MOVIE_ID);
                posterPath = movieDetail.getString(POSTER_PATH);
                originalTitle = movieDetail.getString(ORIGINAL_TITLE);
                overview = movieDetail.getString(OVERVIEW);
                releaseDate = movieDetail.getString(RELEASE_DATE);
                voteAverage = movieDetail.getDouble(VOTE_AVERAGE);

                // create movie object for each iteration
                movies.add(new Movie(movieId,posterPath,originalTitle,overview,releaseDate,voteAverage));
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

            setContentView(R.layout.activity_main);
            mAdapter = new MovieAdapter(MainActivity.this, result);
            GridView gridview = (GridView) findViewById(R.id.gridview);
            DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();
            float dpHeight = (displayMetrics.heightPixels / displayMetrics.density);
            float dpWidth =  (displayMetrics.widthPixels / displayMetrics.density);
            int IMAGE_WIDTH;
            if (dpWidth < 480)
                IMAGE_WIDTH = IMAGE_WIDTH_PHONE;
            else
                IMAGE_WIDTH = IMAGE_WIDTH_TABLET;
            float factor = Math.round(dpWidth/IMAGE_WIDTH);
            Log.v(LOG_TAG, "factor is :" + factor + "the number is :" + (dpWidth/IMAGE_WIDTH));
            gridview.setColumnWidth((int)(dpWidth/(factor)*displayMetrics.density));
            gridview.setAdapter(mAdapter);

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    int movie_id = result.get(position).getMovieId();
                    Intent detailsIntent = new Intent(
                            MainActivity.this, DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movie_id);
                    startActivity(detailsIntent);
                }
            });
        }
    }
}

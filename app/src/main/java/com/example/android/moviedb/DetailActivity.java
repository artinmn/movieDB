package com.example.android.moviedb;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;

public class DetailActivity extends AppCompatActivity {

    final static String MOVIE_DETAIL_URL = "http://api.themoviedb.org/3/movie/";
    private static Movie movie_detail;
    private final static int IMAGE_WIDTH_PHONE = 185;
    private final static int IMAGE_WIDTH_TABLET = 185;
    final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActivity().getIntent().getExtras();
        setContentView(R.layout.activity_detail);
        DetailFragment details = new DetailFragment();
        details.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private int[] movie_info = new int[3];

        //private int movieID;

        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            FetchMovieDataTask fetchMovieDataTask = new FetchMovieDataTask();
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                movie_info = intent.getIntArrayExtra(Intent.EXTRA_TEXT);
                //movieID = intent.getIntExtra(Intent.EXTRA_TEXT, 0);
                String movieIDstr = Integer.toString(movie_info[0]);
                fetchMovieDataTask.execute(MOVIE_DETAIL_URL+movieIDstr+"?");
            }
            return rootView;
        }

        public class FetchMovieDataTask extends AsyncTask<String, Void, Movie> {

            private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

            private Movie getMovieDataFromJson(String movieJsonStr)
                    throws JSONException {

                // JSON objects keys
                final String MOVIE_ID = "id";
                final String POSTER_PATH = "poster_path";
                final String OVERVIEW = "overview";
                final String RELEASE_DATE = "release_date";
                final String ORIGINAL_TITLE = "original_title";
                final String VOTE_AVERAGE = "vote_average";
                final String VOTE_COUNT = "vote_count";

                JSONObject movieJson = new JSONObject(movieJsonStr);

                    int movieId;
                    String posterPath;
                    String originalTitle;
                    String overview;
                    String releaseDate;
                    double voteAverage;
                    int voteCount;

                    // data needed for movie object
                    movieId = movieJson.getInt(MOVIE_ID);
                    posterPath = movieJson.getString(POSTER_PATH);
                    originalTitle = movieJson.getString(ORIGINAL_TITLE);
                    overview = movieJson.getString(OVERVIEW);
                    releaseDate = movieJson.getString(RELEASE_DATE);
                    voteAverage = movieJson.getDouble(VOTE_AVERAGE);
                    voteCount = movieJson.getInt(VOTE_COUNT);

                    // create movie object for each iteration
                    movie_detail = new Movie(movieId,posterPath,originalTitle,overview,releaseDate,voteAverage,voteCount);

                return movie_detail;
            }
            @Override
            protected Movie doInBackground(String... params) {

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
            protected void onPostExecute(final Movie result) {

                Log.v(LOG_TAG, "Title is " + result.getTitle());
                int movie_position = (movie_info[1]-1) * 20 + movie_info[2] + 1;
                ((TextView) getView().findViewById(R.id.original_title)).setText("  " + Integer.toString(movie_position) + ") " + result.getTitle());
                ((TextView) getView().findViewById(R.id.plot_synopsis)).setText(result.getOverview());
                ((TextView) getView().findViewById(R.id.release_date)).setText(result.getDate().split("-")[0]);
                ((TextView) getView().findViewById(R.id.user_rating)).setText(Double.toString(result.getRating())+"/10");
                ((TextView) getView().findViewById(R.id.user_count)).setText(NumberFormat.getInstance().format(result.getCount()) + " users");
                ImageView imageView = new ImageView(getView().getContext());
                Picasso.with(imageView.getContext()).load(POSTER_BASE_URL + IMAGE_WIDTH_PHONE + result.getPosterPath()).into(((ImageView) getView().findViewById(R.id.movie_poster)));
                }
            }
        }
    }


package com.example.android.moviedb;

// movie object to hold movie detail information
public class Movie {

    // variables of movie object
    private int mMovieId;

    private String mPosterPath;

    private String mOriginalTitle;

    private String mOverview;

    private String mReleaseDate;

    private double mVoteAverage;

    private int mVoteCount;

    // public movie object constructor
    public Movie(int id, String poster, String title, String overview, String date, double rating, int count){

        mMovieId = id;
        mPosterPath = poster;
        mOriginalTitle = title;
        mOverview = overview;
        mReleaseDate = date;
        mVoteAverage = rating;
        mVoteCount = count;
    }

    // movie detail getters
    public int getMovieId() { return mMovieId; }

    public String getPosterPath() { return mPosterPath; }

    public String getTitle() { return mOriginalTitle; }

    public String getOverview() { return mOverview; }

    public String getDate() { return mReleaseDate; }

    public double getRating() { return mVoteAverage; }

    public int getCount() { return mVoteCount; }
}

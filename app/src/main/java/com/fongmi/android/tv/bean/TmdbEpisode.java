package com.fongmi.android.tv.bean;

import android.text.TextUtils;

public class TmdbEpisode {

    private final int number;
    private final String title;
    private final String date;
    private final String overview;
    private final String stillUrl;

    public TmdbEpisode(int number, String title, String date, String overview, String stillUrl) {
        this.number = number;
        this.title = title;
        this.date = date;
        this.overview = overview;
        this.stillUrl = stillUrl;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return TextUtils.isEmpty(title) ? "" : title;
    }

    public String getDate() {
        return TextUtils.isEmpty(date) ? "" : date;
    }

    public String getOverview() {
        return TextUtils.isEmpty(overview) ? "" : overview;
    }

    public String getStillUrl() {
        return TextUtils.isEmpty(stillUrl) ? "" : stillUrl;
    }
}

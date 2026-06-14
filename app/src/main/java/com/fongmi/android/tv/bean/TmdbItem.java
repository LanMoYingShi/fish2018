package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import java.io.Serializable;

public class TmdbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int tmdbId;
    private final String mediaType;
    private final String title;
    private final String subtitle;
    private final String overview;
    private final String posterUrl;
    private final String backdropUrl;
    private final String credit;
    private final double rating;

    public TmdbItem(int tmdbId, String mediaType, String title, String subtitle, String overview, String posterUrl, String backdropUrl) {
        this(tmdbId, mediaType, title, subtitle, overview, posterUrl, backdropUrl, "", 0.0);
    }

    public TmdbItem(int tmdbId, String mediaType, String title, String subtitle, String overview, String posterUrl, String backdropUrl, String credit) {
        this(tmdbId, mediaType, title, subtitle, overview, posterUrl, backdropUrl, credit, 0.0);
    }

    public TmdbItem(int tmdbId, String mediaType, String title, String subtitle, String overview, String posterUrl, String backdropUrl, String credit, double rating) {
        this.tmdbId = tmdbId;
        this.mediaType = mediaType;
        this.title = title;
        this.subtitle = subtitle;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.credit = credit;
        this.rating = rating;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public String getMediaType() {
        return TextUtils.isEmpty(mediaType) ? "" : mediaType;
    }

    public String getTitle() {
        return TextUtils.isEmpty(title) ? "" : title;
    }

    public String getSubtitle() {
        return TextUtils.isEmpty(subtitle) ? "" : subtitle;
    }

    public String getOverview() {
        return TextUtils.isEmpty(overview) ? "" : overview;
    }

    public String getPosterUrl() {
        return TextUtils.isEmpty(posterUrl) ? "" : posterUrl;
    }

    public String getBackdropUrl() {
        return TextUtils.isEmpty(backdropUrl) ? "" : backdropUrl;
    }

    public String getCredit() {
        return TextUtils.isEmpty(credit) ? "" : credit;
    }

    public double getRating() {
        return rating;
    }

    public boolean isTv() {
        return "tv".equals(mediaType);
    }

    public boolean isMovie() {
        return "movie".equals(mediaType);
    }
}

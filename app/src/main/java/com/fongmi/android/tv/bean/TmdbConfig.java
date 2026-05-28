package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.google.gson.annotations.SerializedName;

public class TmdbConfig {

    private static final String DEFAULT_API_BASE = "https://api.tmdb.org/3";
    private static final String DEFAULT_IMAGE_BASE = "https://image.tmdb.org/t/p/w342";
    private static final String DEFAULT_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780";
    private static final String DEFAULT_LANGUAGE = "zh-CN";

    @SerializedName("apiBase")
    private String apiBase;
    @SerializedName("apiKey")
    private String apiKey;
    @SerializedName(value = "apikey", alternate = {"api_key", "tmdbApiKey", "key"})
    private String apiKeyCompat;
    @SerializedName("language")
    private String language;
    @SerializedName("imageBase")
    private String imageBase;
    @SerializedName("backdropBase")
    private String backdropBase;

    public static TmdbConfig objectFrom(String json) {
        try {
            TmdbConfig config = App.gson().fromJson(json, TmdbConfig.class);
            return config == null ? new TmdbConfig().sanitize() : config.sanitize();
        } catch (Throwable e) {
            return new TmdbConfig().sanitize();
        }
    }

    public TmdbConfig sanitize() {
        apiBase = trimOr(apiBase, DEFAULT_API_BASE);
        apiKey = trimOr(apiKey, trimOr(apiKeyCompat, ""));
        apiKeyCompat = apiKey;
        language = trimOr(language, DEFAULT_LANGUAGE);
        imageBase = trimOr(imageBase, DEFAULT_IMAGE_BASE);
        backdropBase = trimOr(backdropBase, DEFAULT_BACKDROP_BASE);
        return this;
    }

    public String getApiBase() {
        return apiBase;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getLanguage() {
        return language;
    }

    public String getImageBase() {
        return imageBase;
    }

    public String getBackdropBase() {
        return backdropBase;
    }

    public boolean isReady() {
        return !TextUtils.isEmpty(getApiKey());
    }

    public String toJson() {
        return App.gson().toJson(sanitize());
    }

    private static String trimOr(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value.trim();
    }
}

package com.fongmi.android.tv.ui.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.utils.ImgUtil;

public class TmdbPlaybackActivity extends VideoActivity implements TmdbPlaybackEnhancer.Host {

    private TmdbPlaybackEnhancer tmdbEnhancer;
    private View tmdbHero;
    private ImageView tmdbBackdrop;
    private ImageView tmdbPoster;
    private TextView tmdbTitle;
    private TextView tmdbSubtitle;

    @Override
    protected void initView(android.os.Bundle savedInstanceState) {
        tmdbEnhancer = new TmdbPlaybackEnhancer(this);
        super.initView(savedInstanceState);
        initTmdbChrome();
    }

    @Override
    protected void onDetailReady(Vod item) {
        applyTmdbArtwork(item.getName(), item.getRemarks(), item.getContent(), item.getPic(), item.getPic());
        tmdbEnhancer.onDetailReady(item);
    }

    @Override
    public String getKey() {
        return super.getKey();
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void applyTmdbVod(Vod vod) {
        updateVod(vod);
    }

    @Override
    public void applyTmdbArtwork(String title, String subtitle, String overview, String poster, String backdrop) {
        if (tmdbHero == null || tmdbBackdrop == null || tmdbPoster == null) return;
        ImgUtil.load(title, backdrop, tmdbBackdrop);
        ImgUtil.load(title, poster, tmdbPoster);
        tmdbTitle.setText(title);
        tmdbSubtitle.setText(subtitle);
        tmdbHero.setVisibility(View.VISIBLE);
    }

    private void initTmdbChrome() {
        tmdbHero = findViewById(getResources().getIdentifier("tmdbHero", "id", getPackageName()));
        tmdbBackdrop = findViewById(getResources().getIdentifier("tmdbBackdrop", "id", getPackageName()));
        tmdbPoster = findViewById(getResources().getIdentifier("tmdbPoster", "id", getPackageName()));
        tmdbTitle = findViewById(getResources().getIdentifier("tmdbTitle", "id", getPackageName()));
        tmdbSubtitle = findViewById(getResources().getIdentifier("tmdbSubtitle", "id", getPackageName()));
    }
}

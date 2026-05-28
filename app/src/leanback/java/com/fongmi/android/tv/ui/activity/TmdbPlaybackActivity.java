package com.fongmi.android.tv.ui.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.utils.ImgUtil;

public class TmdbPlaybackActivity extends VideoActivity implements TmdbPlaybackEnhancer.Host {

    private TmdbPlaybackEnhancer tmdbEnhancer;
    private View tmdbHero;
    private View tmdbInfoPanel;
    private ImageView tmdbBackdrop;
    private ImageView tmdbPoster;
    private ImageView tmdbInfoBackdrop;
    private ImageView tmdbInfoPoster;
    private TextView tmdbTitle;
    private TextView tmdbSubtitle;
    private TextView tmdbInfoTitle;
    private TextView tmdbInfoSubtitle;
    private TextView tmdbInfoOverview;

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
        if (tmdbInfoPanel == null || tmdbInfoBackdrop == null || tmdbInfoPoster == null) return;
        ImgUtil.load(title, backdrop, tmdbInfoBackdrop);
        ImgUtil.load(title, poster, tmdbInfoPoster);
        tmdbInfoTitle.setText(title);
        tmdbInfoSubtitle.setText(subtitle);
        tmdbInfoOverview.setText(overview);
        tmdbInfoOverview.setVisibility(overview == null || overview.isEmpty() ? View.GONE : View.VISIBLE);
        tmdbInfoPanel.setVisibility(View.VISIBLE);
        hidePlainHeader();
    }

    private void initTmdbChrome() {
        tmdbHero = findViewById(R.id.tmdbHero);
        tmdbBackdrop = findViewById(R.id.tmdbBackdrop);
        tmdbPoster = findViewById(R.id.tmdbPoster);
        tmdbTitle = findViewById(R.id.tmdbTitle);
        tmdbSubtitle = findViewById(R.id.tmdbSubtitle);
        tmdbInfoPanel = findViewById(R.id.tmdbInfoPanel);
        tmdbInfoBackdrop = findViewById(R.id.tmdbInfoBackdrop);
        tmdbInfoPoster = findViewById(R.id.tmdbInfoPoster);
        tmdbInfoTitle = findViewById(R.id.tmdbInfoTitle);
        tmdbInfoSubtitle = findViewById(R.id.tmdbInfoSubtitle);
        tmdbInfoOverview = findViewById(R.id.tmdbInfoOverview);
    }

    private void hidePlainHeader() {
        int[] ids = {R.id.name, R.id.remark, R.id.row1, R.id.director, R.id.actor, R.id.row2};
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }
}

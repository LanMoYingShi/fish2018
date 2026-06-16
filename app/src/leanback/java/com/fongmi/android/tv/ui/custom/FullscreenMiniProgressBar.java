package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;

import com.fongmi.android.tv.R;

public class FullscreenMiniProgressBar extends ProgressBar {

    private final Runnable ticker = this::updateProgress;

    public FullscreenMiniProgressBar(Context context) {
        super(context);
    }

    public FullscreenMiniProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullscreenMiniProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(ticker);
        post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(ticker);
        super.onDetachedFromWindow();
    }

    private void updateProgress() {
        Player player = getPlayer();
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        long position = player == null ? 0 : Math.max(0, player.getCurrentPosition());

        if (!isFullscreen() || player == null || duration <= 0 || duration == C.TIME_UNSET) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            setProgress((int) Math.min(getMax(), position * getMax() / duration));
        }
        postDelayed(ticker, 1000);
    }

    private Player getPlayer() {
        PlayerView playerView = getRootView() == null ? null : getRootView().findViewById(R.id.exo);
        return playerView == null ? null : playerView.getPlayer();
    }

    private boolean isFullscreen() {
        if (!(getParent() instanceof View parent)) return false;
        ViewGroup.LayoutParams params = parent.getLayoutParams();
        return params != null && params.width == ViewGroup.LayoutParams.MATCH_PARENT && params.height == ViewGroup.LayoutParams.MATCH_PARENT;
    }
}

package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.ResUtil;

public class FullscreenMiniProgressBar extends View {

    private static final int MAX = 1000;

    private final Runnable ticker = this::updateProgress;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int progress;

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, ResUtil.dp2px(1));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.argb(51, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(Color.rgb(33, 150, 243));
        canvas.drawRect(0, 0, getWidth() * progress / (float) MAX, getHeight(), paint);
    }

    private void updateProgress() {
        Player player = getPlayer();
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        long position = player == null ? 0 : Math.max(0, player.getCurrentPosition());

        if (!isFullscreen() || isControlVisible() || player == null || duration <= 0 || duration == C.TIME_UNSET) {
            setVisibility(GONE);
        } else {
            progress = (int) Math.min(MAX, position * MAX / duration);
            setVisibility(VISIBLE);
            invalidate();
        }
        postDelayed(ticker, 1000);
    }

    private Player getPlayer() {
        PlayerView playerView = getRootView() == null ? null : getRootView().findViewById(R.id.exo);
        return playerView == null ? null : playerView.getPlayer();
    }

    private boolean isControlVisible() {
        View control = getRootView() == null ? null : getRootView().findViewById(R.id.control);
        return control != null && control.getVisibility() == VISIBLE;
    }

    private boolean isFullscreen() {
        if (!(getParent() instanceof View parent)) return false;
        ViewGroup.LayoutParams params = parent.getLayoutParams();
        return params != null && params.width == ViewGroup.LayoutParams.MATCH_PARENT && params.height == ViewGroup.LayoutParams.MATCH_PARENT;
    }
}

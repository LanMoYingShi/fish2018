package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.fongmi.android.tv.utils.Traffic;
import com.google.android.material.textview.MaterialTextView;

public class RealtimeTrafficTextView extends MaterialTextView {

    private final Runnable ticker = this::updateTraffic;

    public RealtimeTrafficTextView(Context context) {
        super(context);
    }

    public RealtimeTrafficTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RealtimeTrafficTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    private void updateTraffic() {
        Traffic.setSpeed(this);
        postDelayed(ticker, 1000);
    }
}

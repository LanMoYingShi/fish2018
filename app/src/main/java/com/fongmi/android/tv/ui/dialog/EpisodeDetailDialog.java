package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.TmdbEpisode;
import com.fongmi.android.tv.databinding.DialogEpisodeDetailBinding;
import com.fongmi.android.tv.ui.adapter.TmdbPhotoAdapter;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 集数详情全屏对话框
 */
public class EpisodeDetailDialog {

    private static Dialog currentDialog;
    private static DialogEpisodeDetailBinding currentBinding;
    private static Activity currentActivity;

    public static void show(Activity activity, TmdbEpisode episode, List<String> photos) {
        dismiss();

        currentActivity = activity;
        currentBinding = DialogEpisodeDetailBinding.inflate(LayoutInflater.from(activity));
        Dialog dialog = new Dialog(activity, R.style.DialogFullScreen);
        dialog.setContentView(currentBinding.getRoot());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.8f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        // 标题
        currentBinding.title.setText(episode.getDisplayTitle());

        // 元信息（评分、时长、日期）
        List<String> metaParts = new ArrayList<>();
        if (episode.getVoteAverage() > 0) {
            metaParts.add(String.format(Locale.US, "★ %.1f", episode.getVoteAverage()));
        }
        if (episode.getRuntime() > 0) {
            metaParts.add(episode.getRuntime() + " 分钟");
        }
        if (!TextUtils.isEmpty(episode.getDate())) {
            metaParts.add(episode.getDate());
        }
        if (!metaParts.isEmpty()) {
            currentBinding.meta.setVisibility(View.VISIBLE);
            currentBinding.meta.setText(TextUtils.join("  ·  ", metaParts));
        } else {
            currentBinding.meta.setVisibility(View.GONE);
        }

        // 简介
        if (!TextUtils.isEmpty(episode.getOverview())) {
            currentBinding.overview.setText(episode.getOverview());
        } else {
            currentBinding.overview.setText(R.string.detail_no_overview);
        }

        // 剧照
        if (!TextUtils.isEmpty(episode.getStillUrl())) {
            currentBinding.still.setVisibility(View.VISIBLE);
            Glide.with(activity)
                    .load(episode.getStillUrl())
                    .into(currentBinding.still);
        } else {
            currentBinding.still.setVisibility(View.GONE);
        }

        // 剧照列表
        setupPhotoList(activity, photos);

        // 关闭按钮
        currentBinding.close.setOnClickListener(v -> dismiss());

        currentDialog = dialog;
        dialog.show();
    }

    private static void setupPhotoList(Activity activity, List<String> photos) {
        if (photos != null && !photos.isEmpty()) {
            currentBinding.photoTitle.setVisibility(View.VISIBLE);
            currentBinding.photoList.setVisibility(View.VISIBLE);
            currentBinding.photoList.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            TmdbPhotoAdapter adapter = new TmdbPhotoAdapter();
            adapter.setOnItemClickListener((url, position) -> {
                PhotoViewerDialog.show(activity, photos, position, null);
            });
            adapter.setItems(photos);
            currentBinding.photoList.setAdapter(adapter);
        } else {
            currentBinding.photoTitle.setVisibility(View.GONE);
            currentBinding.photoList.setVisibility(View.GONE);
        }
    }

    public static void updatePhotos(List<String> photos) {
        if (currentActivity != null && currentBinding != null && photos != null && !photos.isEmpty()) {
            currentActivity.runOnUiThread(() -> setupPhotoList(currentActivity, photos));
        }
    }

    public static void dismiss() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
            currentBinding = null;
            currentActivity = null;
        }
    }
}

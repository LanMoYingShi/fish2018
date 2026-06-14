package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.TmdbImageSaver;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * 照片全屏查看器 + 左右滑动 + 保存 + 旋转。
 */
public class PhotoViewerDialog {

    public interface OnSaveListener {
        void onSave(String url);
    }

    public static void show(Activity activity, List<String> photos, int position, OnSaveListener saveListener) {
        new PhotoViewerDialog(activity, photos, position, saveListener).show();
    }

    private final Activity activity;
    private final List<String> photos;
    private final int startPosition;
    private final OnSaveListener saveListener;
    private int currentPosition;
    private int originalOrientation;

    private PhotoViewerDialog(Activity activity, List<String> photos, int position, OnSaveListener saveListener) {
        this.activity = activity;
        this.photos = photos;
        this.startPosition = position;
        this.currentPosition = position;
        this.saveListener = saveListener;
        this.originalOrientation = activity.getRequestedOrientation();
    }

    private void show() {
        Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // 全屏根容器
        FrameLayout root = new FrameLayout(activity);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackgroundColor(Color.BLACK);

        // ViewPager2 支持左右滑动
        ViewPager2 viewPager = new ViewPager2(activity);
        viewPager.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewPager.setAdapter(new PhotoPagerAdapter(photos));
        viewPager.setCurrentItem(startPosition, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }
        });
        root.addView(viewPager);

        // 右上角保存按钮 - 始终显示
        MaterialButton saveBtn = new MaterialButton(activity);
        saveBtn.setText(R.string.detail_image_save);
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setBackgroundColor(0x80000000);
        saveBtn.setPadding(32, 16, 32, 16);
        FrameLayout.LayoutParams saveBtnParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END
        );
        saveBtnParams.setMargins(16, 48, 16, 16);
        saveBtn.setLayoutParams(saveBtnParams);
        saveBtn.setOnClickListener(v -> {
            if (currentPosition < photos.size()) {
                String url = photos.get(currentPosition);
                // 优先使用自定义 saveListener，否则使用默认保存逻辑
                if (saveListener != null) {
                    saveListener.onSave(url);
                } else {
                    savePhotoDefault(url);
                }
            }
        });
        root.addView(saveBtn);

        // 左上角旋转按钮 - 始终显示
        MaterialButton rotateBtn = new MaterialButton(activity);
        rotateBtn.setText(R.string.detail_image_rotate);
        rotateBtn.setTextColor(Color.WHITE);
        rotateBtn.setBackgroundColor(0x80000000);
        rotateBtn.setPadding(32, 16, 32, 16);
        FrameLayout.LayoutParams rotateBtnParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START
        );
        rotateBtnParams.setMargins(16, 48, 16, 16);
        rotateBtn.setLayoutParams(rotateBtnParams);
        rotateBtn.setOnClickListener(v -> toggleOrientation());
        root.addView(rotateBtn);

        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.black);
        }

        // 关闭对话框时恢复原始方向
        dialog.setOnDismissListener(d -> {
            activity.setRequestedOrientation(originalOrientation);
        });

        dialog.show();
    }

    private void toggleOrientation() {
        int current = activity.getRequestedOrientation();
        // 获取当前实际方向
        int actualOrientation = activity.getResources().getConfiguration().orientation;

        // 如果当前是竖屏或未指定，切换到横屏
        if (actualOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT ||
            current == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ||
            current == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
            current == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            // 否则切换到竖屏
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 默认保存逻辑（转换为高清图并保存）。
     */
    private void savePhotoDefault(String url) {
        if (TextUtils.isEmpty(url)) return;
        if (!(activity instanceof androidx.fragment.app.FragmentActivity)) {
            Notify.show(R.string.detail_image_save_failed);
            return;
        }
        Notify.show(R.string.detail_image_saving);
        String highResUrl = convertToHighRes(url);
        TmdbImageSaver.save((androidx.fragment.app.FragmentActivity) activity, highResUrl, new TmdbImageSaver.Callback() {
            @Override
            public void success(String name) {
                Notify.show(activity.getString(R.string.detail_image_save_success, name));
            }

            @Override
            public void error(String message) {
                String prefix = activity.getString(R.string.detail_image_save_failed);
                Notify.show(TextUtils.isEmpty(message) || prefix.equals(message) ? prefix : prefix + "\n" + message);
            }
        });
    }

    /**
     * 转换为高清图片 URL（original 尺寸）。
     */
    private String convertToHighRes(String url) {
        if (TextUtils.isEmpty(url)) return url;
        // TMDB 图片 URL 格式：https://image.tmdb.org/t/p/w500/xxx.jpg
        // 替换为 original：https://image.tmdb.org/t/p/original/xxx.jpg
        if (url.contains("image.tmdb.org/t/p/")) {
            return url.replaceFirst("/w\\d+/", "/original/")
                      .replaceFirst("/h\\d+/", "/original/");
        }
        return url;
    }

    /**
     * 照片 ViewPager 适配器。
     */
    private class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {
        private final List<String> urls;

        PhotoPagerAdapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setBackgroundColor(Color.BLACK);
            return new PhotoViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext())
                    .load(urls.get(position))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            PhotoViewHolder(ImageView view) {
                super(view);
                this.imageView = view;
            }
        }
    }
}

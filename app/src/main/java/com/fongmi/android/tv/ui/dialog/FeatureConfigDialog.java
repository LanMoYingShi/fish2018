package com.fongmi.android.tv.ui.dialog;

import android.content.DialogInterface;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.TmdbConfig;
import com.fongmi.android.tv.databinding.DialogFeatureConfigBinding;
import com.fongmi.android.tv.setting.Setting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FeatureConfigDialog {

    public static final int TMDB = 1;

    private final FragmentActivity activity;
    private final DialogFeatureConfigBinding binding;
    private AlertDialog dialog;
    private Runnable onDismiss;

    public static FeatureConfigDialog create(FragmentActivity activity) {
        return new FeatureConfigDialog(activity);
    }

    public FeatureConfigDialog(FragmentActivity activity) {
        this.activity = activity;
        this.binding = DialogFeatureConfigBinding.inflate(LayoutInflater.from(activity));
    }

    public FeatureConfigDialog type(int type) {
        return this;
    }

    public FeatureConfigDialog onDismiss(Runnable onDismiss) {
        this.onDismiss = onDismiss;
        return this;
    }

    public void show() {
        initDialog();
        initView();
    }

    private void initDialog() {
        dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.setting_tmdb)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.dialog_positive, this::onPositive)
                .setNegativeButton(R.string.dialog_negative, this::onNegative)
                .create();
        dialog.setOnDismissListener(dialog -> {
            if (onDismiss != null) onDismiss.run();
        });
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setDimAmount(0);
    }

    private void initView() {
        TmdbConfig config = TmdbConfig.objectFrom(Setting.getTmdbConfig());
        binding.baseLayout.setHint(activity.getString(R.string.dialog_tmdb_key));
        binding.extra1Layout.setHint(activity.getString(R.string.dialog_tmdb_lang));
        binding.extra2Layout.setHint("");
        binding.extra3Layout.setHint("");
        binding.extra4Layout.setHint("");
        binding.base.setText(config.getApiKey());
        binding.extra1.setText(config.getLanguage());
        binding.extra2Layout.setVisibility(android.view.View.GONE);
        binding.extra3Layout.setVisibility(android.view.View.GONE);
        binding.extra4Layout.setVisibility(android.view.View.GONE);
    }

    private void onPositive(DialogInterface dialog, int which) {
        String json = "{\"apiKey\":\"" + escape(binding.base.getText()) + "\",\"language\":\"" + escape(binding.extra1.getText()) + "\"}";
        Setting.putTmdbConfig(TmdbConfig.objectFrom(json).toJson());
        dialog.dismiss();
    }

    private void onNegative(DialogInterface dialog, int which) {
        dialog.dismiss();
    }

    private String escape(CharSequence value) {
        return String.valueOf(value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

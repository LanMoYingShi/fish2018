package com.fongmi.android.tv.ui.dialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.TmdbConfig;
import com.fongmi.android.tv.setting.Setting;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TMDB 元数据增强配置弹窗。
 *
 * 仿照 {@link ShortDramaSourceDialog} 的交互：
 * - 配置 TMDB API Key（或 v4 Access Token）
 * - 全局启用 / 关闭 TMDB 增强
 * - 通过 Chip 标签维护启用站点规则与排除站点黑名单
 * 点"确定"才统一保存。
 */
public class TmdbSourceDialog {

    private final FragmentActivity activity;
    private AlertDialog dialog;
    private ChipGroup enabledChips;
    private ChipGroup disabledChips;
    private TextView disabledLabel;
    private EditText apiKeyInput;
    private EditText omdbApiKeyInput;
    private MaterialSwitch enableSwitch;
    private Runnable onDismiss;

    // 暂存数据，点"确定"才保存
    private List<String> tempEnabledRules;
    private List<String> tempDisabledSites;

    public static TmdbSourceDialog create(FragmentActivity activity) {
        return new TmdbSourceDialog(activity);
    }

    private TmdbSourceDialog(FragmentActivity activity) {
        this.activity = activity;
    }

    public TmdbSourceDialog onDismiss(Runnable callback) {
        this.onDismiss = callback;
        return this;
    }

    public void show() {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_tmdb_source, null);
        enabledChips = view.findViewById(R.id.enabledChips);
        disabledChips = view.findViewById(R.id.disabledChips);
        disabledLabel = view.findViewById(R.id.disabledLabel);
        apiKeyInput = view.findViewById(R.id.apiKeyInput);
        omdbApiKeyInput = view.findViewById(R.id.omdbApiKeyInput);
        enableSwitch = view.findViewById(R.id.enableSwitch);
        EditText ruleInput = view.findViewById(R.id.ruleInput);
        View addBtn = view.findViewById(R.id.add);
        View manageBtn = view.findViewById(R.id.manage);
        View resetBtn = view.findViewById(R.id.resetDefault);

        // 初始化暂存数据
        TmdbConfig config = TmdbConfig.objectFrom(Setting.getTmdbConfig());
        tempEnabledRules = new ArrayList<>(config.getEnabledSites());
        tempDisabledSites = new ArrayList<>(config.getDisabledSites());
        apiKeyInput.setText(TextUtils.isEmpty(config.getAccessToken()) ? config.getApiKey() : config.getAccessToken());
        omdbApiKeyInput.setText(config.getOmdbApiKey());
        enableSwitch.setChecked(Setting.isTmdbEnabled());
        updateChipsDisplay();

        addBtn.setOnClickListener(v -> addRule(ruleInput));
        ruleInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addRule(ruleInput);
                return true;
            }
            return false;
        });
        manageBtn.setOnClickListener(v -> showSiteManage());
        resetBtn.setOnClickListener(v -> resetToDefault());

        dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.setting_tmdb_source)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive, (d, w) -> onSave())
                .setNegativeButton(R.string.dialog_negative, null)
                .setOnDismissListener(d -> { if (onDismiss != null) onDismiss.run(); })
                .create();
        dialog.show();
    }

    private void onSave() {
        String apiKey = apiKeyInput.getText().toString().trim();
        String omdbApiKey = omdbApiKeyInput.getText().toString().trim();
        // v4 Access Token 含两个点，按 token 存；否则按 api_key 存
        boolean isToken = apiKey.split("\\.").length >= 3;
        StringBuilder sb = new StringBuilder("{");
        if (isToken) sb.append("\"accessToken\":\"").append(escape(apiKey)).append("\",");
        else sb.append("\"apiKey\":\"").append(escape(apiKey)).append("\",");
        if (!TextUtils.isEmpty(omdbApiKey)) {
            sb.append("\"omdbApiKey\":\"").append(escape(omdbApiKey)).append("\",");
        }
        sb.append("\"enabledSites\":").append(toJsonArray(tempEnabledRules)).append(',');
        sb.append("\"disabledSites\":").append(toJsonArray(tempDisabledSites));
        sb.append('}');
        Setting.putTmdbConfig(TmdbConfig.objectFrom(sb.toString()).toJson());
        Setting.putTmdbEnabled(enableSwitch.isChecked());
    }

    private void showSiteManage() {
        List<Site> sites = VodConfig.get().getSites().stream().filter(s -> s != null && !s.isEmpty()).toList();
        if (sites.isEmpty()) return;

        List<String> enabledRules = new ArrayList<>(tempEnabledRules);
        List<String> disabledSites = new ArrayList<>(tempDisabledSites);
        boolean enableAll = enabledRules.isEmpty();

        String[] labels = new String[sites.size()];
        boolean[] checked = new boolean[sites.size()];

        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            labels[i] = TextUtils.isEmpty(site.getName()) ? site.getKey() : site.getName() + "  " + site.getKey();
            boolean inBlacklist = disabledSites.contains(site.getKey());
            boolean matchedByRule = enableAll || matchesRule(enabledRules, site);
            checked[i] = matchedByRule && !inBlacklist;
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_tmdb_site_manage)
                .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(R.string.dialog_positive, (d, w) -> applySiteManage(sites, enabledRules, disabledSites, checked, enableAll))
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private void applySiteManage(List<Site> sites, List<String> enabledRules, List<String> disabledSites, boolean[] checked, boolean enableAll) {
        List<String> newEnabled = new ArrayList<>();
        // 保留关键词（非站点条目）
        for (String rule : enabledRules) {
            if (findSite(rule) == null) newEnabled.add(rule);
        }

        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            String key = site.getKey();
            boolean nowChecked = checked[i];
            boolean matchedByKeyword = false;

            for (String rule : enabledRules) {
                if (findSite(rule) == null && matchesRule(List.of(rule), site)) {
                    matchedByKeyword = true;
                    break;
                }
            }

            if (nowChecked) {
                disabledSites.remove(key);
                // enableAll 模式下默认全部启用，仅当显式取消才需要黑名单；勾选状态无需加入
                if (!enableAll && !matchedByKeyword && !newEnabled.contains(key)) {
                    newEnabled.add(key);
                }
            } else {
                if ((enableAll || matchedByKeyword) && !disabledSites.contains(key)) {
                    disabledSites.add(key);
                }
            }
        }

        tempEnabledRules = newEnabled;
        tempDisabledSites = disabledSites;
        updateChipsDisplay();
    }

    private boolean matchesRule(List<String> rules, Site site) {
        String key = site.getKey() == null ? "" : site.getKey().toLowerCase(Locale.ROOT);
        String name = site.getName() == null ? "" : site.getName().toLowerCase(Locale.ROOT);
        for (String rule : rules) {
            if (TextUtils.isEmpty(rule)) continue;
            String r = rule.trim().toLowerCase(Locale.ROOT);
            if (key.equals(r) || name.equals(r)) return true;
            if (key.contains(r) || name.contains(r)) return true;
        }
        return false;
    }

    private Site findSite(String value) {
        if (TextUtils.isEmpty(value)) return null;
        String target = value.trim();
        for (Site site : VodConfig.get().getSites()) {
            if (site == null || site.isEmpty()) continue;
            if (target.equalsIgnoreCase(site.getKey())) return site;
            if (!TextUtils.isEmpty(site.getName()) && target.equalsIgnoreCase(site.getName())) return site;
        }
        return null;
    }

    private String displayName(Site site) {
        return TextUtils.isEmpty(site.getName()) ? site.getKey() : site.getName();
    }

    private String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(escape(values.get(i))).append('"');
        }
        return sb.append(']').toString();
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Chip 相关
    private void updateChipsDisplay() {
        enabledChips.removeAllViews();
        disabledChips.removeAllViews();

        if (tempEnabledRules.isEmpty()) {
            // 空规则代表"全部站点启用"，用一个提示 Chip 表示
            Chip chip = new Chip(activity);
            chip.setText(R.string.dialog_tmdb_all_sites);
            chip.setCheckable(false);
            chip.setCloseIconVisible(false);
            enabledChips.addView(chip);
        } else {
            for (String rule : tempEnabledRules) {
                if (TextUtils.isEmpty(rule)) continue;
                enabledChips.addView(createChip(rule.trim(), false));
            }
        }

        if (tempDisabledSites.isEmpty()) {
            disabledLabel.setVisibility(View.GONE);
            disabledChips.setVisibility(View.GONE);
        } else {
            disabledLabel.setVisibility(View.VISIBLE);
            disabledChips.setVisibility(View.VISIBLE);
            for (String key : tempDisabledSites) {
                Site site = findSite(key);
                String name = site != null ? displayName(site) : key;
                Chip chip = createChip(name, true);
                chip.setTag(key);
                disabledChips.addView(chip);
            }
        }
    }

    private Chip createChip(String text, boolean isDisabled) {
        Chip chip = new Chip(activity);
        Site site = isDisabled ? null : findSite(text);
        chip.setText(site != null ? displayName(site) : text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);

        if (isDisabled) {
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColorResource(android.R.color.holo_red_light);
            chip.setChipStrokeWidth(2f);
        }

        chip.setOnCloseIconClickListener(v -> {
            if (isDisabled) removeFromBlacklist((String) chip.getTag());
            else removeEnabledRule(text);
        });

        return chip;
    }

    private void removeFromBlacklist(String key) {
        tempDisabledSites.remove(key);
        updateChipsDisplay();
    }

    private void removeEnabledRule(String rule) {
        Site site = findSite(rule);
        if (site != null) {
            tempEnabledRules.remove(site.getKey());
            tempEnabledRules.remove(displayName(site));
        } else {
            tempEnabledRules.remove(rule);
        }
        updateChipsDisplay();
    }

    private void resetToDefault() {
        tempEnabledRules.clear();
        tempDisabledSites.clear();
        updateChipsDisplay();
    }

    private void addRule(EditText input) {
        String rule = input.getText().toString().trim();
        if (TextUtils.isEmpty(rule)) return;
        Site site = findSite(rule);
        String toAdd = site != null ? site.getKey() : rule;
        if (tempEnabledRules.contains(toAdd)) {
            input.setText("");
            return;
        }
        tempEnabledRules.add(toAdd);
        input.setText("");
        updateChipsDisplay();
    }
}

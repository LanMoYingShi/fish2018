package com.fongmi.android.tv.ui.helper;

import android.text.TextUtils;

import com.fongmi.android.tv.bean.TmdbConfig;
import com.fongmi.android.tv.bean.TmdbItem;
import com.fongmi.android.tv.service.TmdbService;
import com.github.catvod.crawler.SpiderDebug;

import java.util.List;

/**
 * TMDB 匹配器
 * 负责根据视频名称搜索并匹配 TMDB 数据
 */
public class TmdbMatcher {

    private final TmdbService tmdbService;
    private final TmdbConfig tmdbConfig;

    public TmdbMatcher(TmdbService tmdbService, TmdbConfig tmdbConfig) {
        this.tmdbService = tmdbService;
        this.tmdbConfig = tmdbConfig;
    }

    /**
     * 搜索并匹配最佳 TMDB 项
     *
     * @param videoName 视频名称
     * @return 最佳匹配项，未找到返回 null
     */
    public TmdbItem searchAndMatch(String videoName) {
        if (TextUtils.isEmpty(videoName) || !tmdbConfig.isReady()) {
            SpiderDebug.log("TMDB 匹配跳过: name=" + videoName + " ready=" + tmdbConfig.isReady());
            return null;
        }

        try {
            // 清理视频名称
            String cleanName = cleanVideoName(videoName);
            SpiderDebug.log("TMDB 搜索: " + cleanName);

            // 搜索
            List<TmdbItem> results = tmdbService.search(cleanName, tmdbConfig);
            if (results == null || results.isEmpty()) {
                SpiderDebug.log("TMDB 搜索无结果");
                return null;
            }

            // 返回第一个结果（最相关）
            TmdbItem best = results.get(0);
            SpiderDebug.log("TMDB 匹配成功: " + best.getTitle() + " [" + best.getMediaType() + "]");
            return best;

        } catch (Exception e) {
            SpiderDebug.log("TMDB 搜索失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 清理视频名称，移除干扰字符
     */
    private String cleanVideoName(String name) {
        if (TextUtils.isEmpty(name)) return "";

        String clean = name;

        // 移除常见标签
        clean = clean.replaceAll("(?i)\\[.*?\\]", ""); // [xxx]
        clean = clean.replaceAll("(?i)\\(.*?\\)", ""); // (xxx)
        clean = clean.replaceAll("(?i)【.*?】", ""); // 【xxx】
        clean = clean.replaceAll("(?i)（.*?）", ""); // （xxx）

        // 移除年份
        clean = clean.replaceAll("\\d{4}", "");

        // 移除季集标记
        clean = clean.replaceAll("(?i)S\\d+E\\d+", "");
        clean = clean.replaceAll("(?i)第\\d+季", "");
        clean = clean.replaceAll("(?i)第\\d+集", "");

        // 移除清晰度标记
        clean = clean.replaceAll("(?i)\\b(HD|4K|1080[Pp]|720[Pp]|BluRay|WEB-DL|HDTV)\\b", "");

        // 移除多余空格
        clean = clean.trim().replaceAll("\\s+", " ");

        return clean;
    }

    /**
     * 从标题中提取年份
     */
    public Integer extractYear(String title) {
        if (TextUtils.isEmpty(title)) return null;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");
        java.util.regex.Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }
}

package com.fongmi.android.tv.player.karaoke;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KaraokeUsdbProvider implements KaraokeTrackProvider {

    private static final Pattern RSS_ITEM = Pattern.compile("(?is)<item>(.*?)</item>");
    private static final Pattern RSS_TITLE = Pattern.compile("(?is)<title>(.*?)</title>");
    private static final Pattern RSS_GUID = Pattern.compile("(?is)<guid>(\\d+)</guid>");

    @Override
    public List<KaraokeTrackRepository.SearchResult> search(String keyword) throws Exception {
        List<KaraokeTrackRepository.SearchResult> results = new ArrayList<>();
        String id = KaraokeTrackRepository.parseUsdbId(keyword);
        if (!TextUtils.isEmpty(id)) {
            results.add(resultById(id));
            return results;
        }
        if (TextUtils.isEmpty(keyword) || keyword.trim().length() < 2) return results;
        results.addAll(searchRss(keyword, "https://usdb.animux.de/rss/rss_new_top10.php"));
        results.addAll(searchRss(keyword, "https://usdb.animux.de/rss/rss_downloads_top10.php"));
        return results;
    }

    private static KaraokeTrackRepository.SearchResult resultById(String id) throws Exception {
        String detailUrl = "https://usdb.animux.de/?link=detail&id=" + id;
        String html = KaraokeTrackRepository.getRemoteText(detailUrl, null);
        String title = KaraokeTrackRepository.parseTitle(html);
        String artist = KaraokeTrackRepository.parseArtist(title);
        String song = KaraokeTrackRepository.parseSong(title);
        String bpm = KaraokeTrackRepository.parseDetailField(html, "BPM");
        String gap = KaraokeTrackRepository.parseDetailField(html, "GAP");
        String note = "BPM " + KaraokeTrackRepository.emptyDash(bpm) + " · GAP " + KaraokeTrackRepository.emptyDash(gap);
        return new KaraokeTrackRepository.SearchResult("USDB", song, artist, note, detailUrl, false);
    }

    private static List<KaraokeTrackRepository.SearchResult> searchRss(String keyword, String url) throws Exception {
        List<KaraokeTrackRepository.SearchResult> results = new ArrayList<>();
        String html = KaraokeTrackRepository.getRemoteText(url, null);
        Matcher item = RSS_ITEM.matcher(html);
        String normalized = KaraokeTrackRepository.normalizeSearch(keyword);
        while (item.find() && results.size() < 5) {
            String block = item.group(1);
            String title = KaraokeTrackRepository.find(RSS_TITLE, block, 1);
            String id = KaraokeTrackRepository.find(RSS_GUID, block, 1);
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(id)) continue;
            String clean = KaraokeTrackRepository.html(title);
            if (!KaraokeTrackRepository.normalizeSearch(clean).contains(normalized)) continue;
            results.add(new KaraokeTrackRepository.SearchResult("USDB RSS", KaraokeTrackRepository.parseSong(clean), KaraokeTrackRepository.parseArtist(clean), "USDB #" + id, "https://usdb.animux.de/?link=detail&id=" + id, false));
        }
        return results;
    }
}

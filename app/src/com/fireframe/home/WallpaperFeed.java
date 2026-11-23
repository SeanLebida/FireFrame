package com.fireframe.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Pulls one recent Hubble image and one recent NASA image into the custom wallpaper.
// Checked about once an hour. A new picture is saved at most every 12 hours.
class WallpaperFeed {
    static final long INTERVAL = 12L * 60 * 60 * 1000;
    static final String HUBBLE = "https://esahubble.org/images/feed/";
    static final String NASA = "https://www.nasa.gov/feeds/iotd-feed";
    static boolean running;

    static final class Shot {
        final String url, credit;
        Shot(String url, String credit) { this.url = url; this.credit = credit; }
    }

    static void maybeRefresh(Context ctx, File dest) {
        if (dest == null) return;
        SharedPreferences prefs = ctx.getSharedPreferences("fireframe", Context.MODE_PRIVATE);
        long last = prefs.getLong("wallpaperFetched", 0);
        if (running) return;
        if (dest.exists() && System.currentTimeMillis() - last < INTERVAL) return;
        running = true;
        new Thread(() -> {
            String credit = null;
            try {
                credit = fetch(prefs, dest);
            } catch (Exception ignored) {
            }
            String shown = credit;
            new Handler(Looper.getMainLooper()).post(() -> {
                running = false;
                if (shown != null) HomeScreen.onWallpaperReady(shown);
            });
        }).start();
    }

    static String fetch(SharedPreferences prefs, File dest) throws Exception {
        List<Shot> shots = new ArrayList<>();
        List<Shot> hubble = feed(HUBBLE, true);
        List<Shot> nasa = feed(NASA, false);
        int n = Math.max(hubble.size(), nasa.size());
        for (int i = 0; i < n && shots.size() < 8; i++) {
            if (i < hubble.size()) shots.add(hubble.get(i));
            if (i < nasa.size() && shots.size() < 8) shots.add(nasa.get(i));
        }
        if (shots.isEmpty()) return null;
        int start = prefs.getInt("wallpaperFeedIndex", 0);
        File tmp = new File(dest.getParentFile(), "wallpaper-next.jpg");
        for (int k = 0; k < shots.size(); k++) {
            Shot shot = shots.get((start + k) % shots.size());
            if (!download(shot.url, tmp)) continue;
            if (dest.exists() && !dest.delete()) return null;
            if (!tmp.renameTo(dest)) return null;
            prefs.edit()
                    .putLong("wallpaperFetched", System.currentTimeMillis())
                    .putInt("wallpaperFeedIndex", start + k + 1)
                    .putString("wallpaper", "custom")
                    .apply();
            return shot.credit;
        }
        return null;
    }

    static List<Shot> feed(String page, boolean hubble) {
        List<Shot> out = new ArrayList<>();
        String xml;
        try {
            xml = get(page, 500000);
        } catch (Exception e) {
            return out;
        }
        int from = 0;
        while (out.size() < 4) {
            int a = xml.indexOf("<item>", from);
            int b = a < 0 ? -1 : xml.indexOf("</item>", a);
            if (a < 0 || b < 0) break;
            String item = xml.substring(a, b);
            from = b + 7;
            String title = clean(textBetween(item, "<title>", "</title>"));
            String lower = (title + item).toLowerCase();
            if (lower.contains("banner") || lower.contains("newsletter") || lower.contains("logo")) continue;
            String url = firstJpeg(item);
            if (url == null) continue;
            if (hubble) url = url.replace("/images/screen/", "/images/large/");
            String who = hubble ? "ESA/Hubble" : "NASA";
            out.add(new Shot(url, title.length() == 0 ? who : title + "  ·  " + who));
        }
        return out;
    }

    static String firstJpeg(String item) {
        int i = 0;
        while (i < item.length()) {
            int h = indexOfIgnoreCase(item, "http", i);
            if (h < 0) return null;
            int end = h;
            while (end < item.length() && item.charAt(end) != '"' && item.charAt(end) != '\''
                    && item.charAt(end) != ' ' && item.charAt(end) != '<') end++;
            String url = item.substring(h, end);
            String low = url.toLowerCase();
            if (low.contains(".jpg") || low.contains(".jpeg")) return url;
            i = end + 1;
        }
        return null;
    }

    static boolean download(String url, File dest) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(30000);
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("User-Agent", "FireFrame");
            if (c.getResponseCode() != 200) return false;
            int len = c.getContentLength();
            if (len > 12 * 1000 * 1000) return false;
            try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[65536];
                int got = 0;
                for (int n; (n = in.read(buf)) > 0; ) {
                    got += n;
                    if (got > 12 * 1000 * 1000) return false;
                    out.write(buf, 0, n);
                }
            }
            return dest.length() > 10000;
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    static String get(String url, int max) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(20000);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "FireFrame");
        try (InputStream in = c.getInputStream()) {
            byte[] buf = new byte[4096];
            StringBuilder sb = new StringBuilder();
            int got = 0;
            for (int n; (n = in.read(buf)) > 0 && got < max; ) {
                int take = Math.min(n, max - got);
                sb.append(new String(buf, 0, take, "UTF-8"));
                got += take;
            }
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }

    static String textBetween(String s, String a, String b) {
        int i = s.indexOf(a);
        if (i < 0) return "";
        i += a.length();
        int j = s.indexOf(b, i);
        if (j < 0) return "";
        return s.substring(i, j);
    }

    static String clean(String s) {
        s = s.replaceAll("<[^>]+>", "");
        s = s.replace("<![CDATA[", "").replace("]]>", "");
        s = s.replace("&amp;", "&").replace("&#039;", "'").replace("&#39;", "'");
        s = s.replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">");
        s = s.trim();
        return s.length() > 90 ? s.substring(0, 90).trim() : s;
    }

    static int indexOfIgnoreCase(String s, String needle, int from) {
        return s.toLowerCase().indexOf(needle, from);
    }
}

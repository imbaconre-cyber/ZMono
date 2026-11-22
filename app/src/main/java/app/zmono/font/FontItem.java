package app.zmono.font;

import android.content.Intent;
import android.os.Bundle;

public class FontItem {
    public static final String EXTRA_NAME = "font_name";
    public static final String EXTRA_SIZE = "font_size";
    public static final String EXTRA_URL = "font_url";
    public static final String EXTRA_CATEGORY = "font_category";
    public static final String EXTRA_THUMB = "font_thumb";
    public static final String EXTRA_PREVIEW = "font_preview";

    public final String name;
    public final String size;
    public final String url;
    public final String category;
    public final String thumb;
    public final String preview;

    public FontItem(String name, String size, String url, String category, String thumb, String preview) {
        this.name = name == null ? "Font" : name;
        this.size = size == null ? "0 KB" : size;
        this.url = url == null ? "" : url;
        this.category = category == null ? "Fonts" : category;
        this.thumb = thumb == null ? "" : thumb;
        this.preview = preview == null ? "" : preview;
    }

    public String downloadUrl() {
        if (url.startsWith("http")) return url;
        return downloadHost(size) + url;
    }

    public static String downloadHost(String size) {
        long bytes = humanReadableSizeToBytes(size);
        long max = 19L * 1024 * 1024;
        if (bytes < max) {
            return "https://cdn.jsdelivr.net/gh/KhunHtetzNaing/zFont_Fonts@master";
        }
        return "https://github.com/KhunHtetzNaing/zFont_Fonts/raw/master";
    }

    public static long humanReadableSizeToBytes(String sizeStr) {
        if (sizeStr == null) return 0;
        sizeStr = sizeStr.trim().toUpperCase();
        long mult = 1;
        String num = sizeStr;
        if (sizeStr.endsWith("TB")) { mult = 1024L * 1024 * 1024 * 1024; num = sizeStr.substring(0, sizeStr.length() - 2); }
        else if (sizeStr.endsWith("GB")) { mult = 1024L * 1024 * 1024; num = sizeStr.substring(0, sizeStr.length() - 2); }
        else if (sizeStr.endsWith("MB")) { mult = 1024L * 1024; num = sizeStr.substring(0, sizeStr.length() - 2); }
        else if (sizeStr.endsWith("KB")) { mult = 1024L; num = sizeStr.substring(0, sizeStr.length() - 2); }
        else if (sizeStr.endsWith("B")) { mult = 1; num = sizeStr.substring(0, sizeStr.length() - 1); }
        try {
            return (long) (Double.parseDouble(num.trim()) * mult);
        } catch (Exception e) {
            return 0;
        }
    }

    public void putExtras(Intent intent) {
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_SIZE, size);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_THUMB, thumb);
        intent.putExtra(EXTRA_PREVIEW, preview);
    }

    public static FontItem fromIntent(Intent intent) {
        if (intent == null) return null;
        return new FontItem(
                intent.getStringExtra(EXTRA_NAME),
                intent.getStringExtra(EXTRA_SIZE),
                intent.getStringExtra(EXTRA_URL),
                intent.getStringExtra(EXTRA_CATEGORY),
                intent.getStringExtra(EXTRA_THUMB),
                intent.getStringExtra(EXTRA_PREVIEW)
        );
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString(EXTRA_NAME, name);
        b.putString(EXTRA_SIZE, size);
        b.putString(EXTRA_URL, url);
        b.putString(EXTRA_CATEGORY, category);
        b.putString(EXTRA_THUMB, thumb);
        b.putString(EXTRA_PREVIEW, preview);
        return b;
    }

    public static FontItem fromBundle(Bundle b) {
        if (b == null) return null;
        return new FontItem(
                b.getString(EXTRA_NAME),
                b.getString(EXTRA_SIZE),
                b.getString(EXTRA_URL),
                b.getString(EXTRA_CATEGORY),
                b.getString(EXTRA_THUMB),
                b.getString(EXTRA_PREVIEW)
        );
    }
}
package app.zmono.font;

public class FontItem {
    public final String name;
    public final String size;
    public final String url; // relative or absolute
    public final String category;
    public final String thumb;
    public final String preview;

    public FontItem(String name, String size, String url, String category, String thumb, String preview) {
        this.name = name;
        this.size = size;
        this.url = url;
        this.category = category;
        this.thumb = thumb;
        this.preview = preview;
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
        sizeStr = sizeStr.trim().toUpperCase();
        long mult = 1;
        String num = sizeStr;
        if (sizeStr.endsWith("TB")) { mult = 1024L*1024*1024*1024; num = sizeStr.substring(0, sizeStr.length()-2); }
        else if (sizeStr.endsWith("GB")) { mult = 1024L*1024*1024; num = sizeStr.substring(0, sizeStr.length()-2); }
        else if (sizeStr.endsWith("MB")) { mult = 1024L*1024; num = sizeStr.substring(0, sizeStr.length()-2); }
        else if (sizeStr.endsWith("KB")) { mult = 1024L; num = sizeStr.substring(0, sizeStr.length()-2); }
        else if (sizeStr.endsWith("B")) { mult = 1; num = sizeStr.substring(0, sizeStr.length()-1); }
        try {
            return (long)(Double.parseDouble(num.trim()) * mult);
        } catch (Exception e) {
            return 0;
        }
    }
}
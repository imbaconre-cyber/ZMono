package app.zmono.font;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
    public interface Progress {
        void onProgress(long downloaded, long total);
        void onDone(File file);
        void onError(Exception e);
    }

    public static File targetFile(String fontName) {
        File base = new File(Environment.getExternalStorageDirectory(), "monofonts/ttf");
        if (!base.exists()) base.mkdirs();
        // sanitize
        String safe = fontName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!safe.toLowerCase().endsWith(".ttf") && !safe.toLowerCase().endsWith(".otf")) {
            safe = safe + ".ttf";
        }
        return new File(base, safe);
    }

    public static void download(String url, File out, Progress cb) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(120000);
                conn.setRequestProperty("User-Agent", "ZMono/1.0");
                conn.connect();
                long total = conn.getContentLengthLong();
                InputStream in = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(out);
                byte[] buf = new byte[256 * 1024];
                long downloaded = 0;
                int r;
                while ((r = in.read(buf)) != -1) {
                    fos.write(buf, 0, r);
                    downloaded += r;
                    if (cb != null) cb.onProgress(downloaded, total);
                }
                fos.flush();
                fos.close();
                in.close();
                if (cb != null) cb.onDone(out);
            } catch (Exception e) {
                if (cb != null) cb.onError(e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
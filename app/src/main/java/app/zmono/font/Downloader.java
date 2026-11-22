package app.zmono.font;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

public final class Downloader {
    public interface Callback {
        void onProgress(long downloaded, long total);
        void onSuccess(File file);
        void onError(Exception e);
    }

    public static final class Handle {
        public volatile boolean cancelled = false;
        volatile Thread thread;
        public void cancel() {
            cancelled = true;
            Thread t = thread;
            if (t != null) t.interrupt();
        }
    }

    private Downloader() {}

    public static File targetFile(String fontName) {
        File base = new File(Environment.getExternalStorageDirectory(), "monofonts/ttf");
        if (!base.exists() && !base.mkdirs()) {
            // fall back to app-specific external dir handled by caller; still attempt
        }
        String safe = fontName == null ? "font" : fontName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String lower = safe.toLowerCase();
        if (!lower.endsWith(".ttf") && !lower.endsWith(".otf")) {
            safe = safe + ".ttf";
        }
        return new File(base, safe);
    }

    public static Handle download(String url, File out, Callback cb, Executor executor) {
        Handle handle = new Handle();
        Runnable task = () -> {
            handle.thread = Thread.currentThread();
            HttpURLConnection conn = null;
            try {
                if (handle.cancelled) return;
                if (out.getParentFile() != null && !out.getParentFile().exists()) {
                    out.getParentFile().mkdirs();
                }
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(120000);
                conn.setRequestProperty("User-Agent", "ZMono/1.0");
                conn.connect();

                if (handle.cancelled) return;
                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("HTTP " + code);
                }

                long total = conn.getContentLengthLong();
                InputStream in = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(out);
                byte[] buf = new byte[256 * 1024];
                long downloaded = 0;
                int r;
                while ((r = in.read(buf)) != -1) {
                    if (handle.cancelled) {
                        fos.close();
                        in.close();
                        return;
                    }
                    fos.write(buf, 0, r);
                    downloaded += r;
                    if (cb != null) cb.onProgress(downloaded, total);
                }
                fos.flush();
                fos.close();
                in.close();
                if (!handle.cancelled && cb != null) cb.onSuccess(out);
            } catch (Exception e) {
                if (!handle.cancelled && cb != null) cb.onError(e);
            } finally {
                if (conn != null) conn.disconnect();
                handle.thread = null;
            }
        };

        if (executor != null) {
            executor.execute(task);
        } else {
            new Thread(task, "Downloader").start();
        }
        return handle;
    }
}
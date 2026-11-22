package app.zmono.font;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressBar progress;
    private TextView status;
    private final List<FontItem> fonts = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        progress = findViewById(R.id.progress);
        status = findViewById(R.id.status);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= fonts.size()) return;
            FontItem f = fonts.get(position);
            downloadFont(f);
        });

        ensureStoragePermission();
        loadFonts();
    }

    private void ensureStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(i);
                } catch (Exception e) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void loadFonts() {
        progress.setVisibility(View.VISIBLE);
        status.setText(getString(R.string.loading));
        new Thread(() -> {
            try {
                List<FontItem> loaded = FontRepository.fetchFonts();
                runOnUiThread(() -> {
                    fonts.clear();
                    fonts.addAll(loaded);
                    labels.clear();
                    for (FontItem f : loaded) {
                        labels.add(f.name + "  (" + f.size + ") - " + f.category);
                    }
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    status.setText("Loaded " + loaded.size() + " fonts");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    status.setText("Failed: " + e.getMessage());
                    Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void downloadFont(FontItem f) {
        File out = Downloader.targetFile(f.name);
        Toast.makeText(this, "Downloading " + f.name, Toast.LENGTH_SHORT).show();
        progress.setVisibility(View.VISIBLE);
        Downloader.download(f.downloadUrl(), out, new Downloader.Progress() {
            @Override
            public void onProgress(long downloaded, long total) {
                runOnUiThread(() -> {
                    if (total > 0) {
                        int pct = (int) (downloaded * 100 / total);
                        status.setText("Downloading " + f.name + ": " + pct + "%");
                    } else {
                        status.setText("Downloading " + f.name + ": " + (downloaded / 1024) + " KB");
                    }
                });
            }

            @Override
            public void onDone(File file) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    status.setText("Saved: " + file.getAbsolutePath());
                    Toast.makeText(MainActivity.this, "Downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    // open apply flow
                    Intent i = new Intent(MainActivity.this, ApplyActivity.class);
                    i.putExtra("fontPath", file.getAbsolutePath());
                    i.putExtra("fontName", f.name);
                    startActivity(i);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    status.setText("Download error: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
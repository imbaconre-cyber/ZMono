package app.zmono.font;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplyActivity extends AppCompatActivity {
    private FontItem font;
    private File fontFile;
    private ExecutorService executor;
    private Handler mainHandler;
    private Downloader.Handle downloadHandle;
    private volatile boolean destroyed = false;

    private TextView fontName;
    private TextView fontSize;
    private TextView fontCategory;
    private TextView fontPath;
    private TextView preview;
    private ProgressBar overallProgress;
    private TextView overallStatus;
    private ProgressBar downloadProgress;
    private TextView downloadStatus;

    private TextView[] statuses;
    private MaterialButton[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        font = FontItem.fromIntent(getIntent());
        if (font == null) {
            Toast.makeText(this, "Missing font data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fontName = findViewById(R.id.fontName);
        fontSize = findViewById(R.id.fontSize);
        fontCategory = findViewById(R.id.fontCategory);
        fontPath = findViewById(R.id.fontPath);
        preview = findViewById(R.id.preview);
        overallProgress = findViewById(R.id.overallProgress);
        overallStatus = findViewById(R.id.overallStatus);
        downloadProgress = findViewById(R.id.downloadProgress);
        downloadStatus = findViewById(R.id.downloadStatus);

        fontName.setText(getString(R.string.font_name_label, font.name));
        fontSize.setText(getString(R.string.font_size_label, font.size));
        fontCategory.setText(getString(R.string.font_category_label, font.category));

        fontFile = Downloader.targetFile(font.name);
        fontPath.setText(getString(R.string.font_path_label, fontFile.getAbsolutePath()));

        statuses = new TextView[]{null,
                findViewById(R.id.status1), findViewById(R.id.status2), findViewById(R.id.status3),
                findViewById(R.id.status4), findViewById(R.id.status5), findViewById(R.id.status6),
                findViewById(R.id.status7)};
        buttons = new MaterialButton[]{null,
                findViewById(R.id.btn1), findViewById(R.id.btn2), findViewById(R.id.btn3),
                findViewById(R.id.btn4), findViewById(R.id.btn5), findViewById(R.id.btn6),
                findViewById(R.id.btn7)};

        for (int i = 1; i <= 7; i++) {
            statuses[i].setText(R.string.step_status_pending);
        }

        buttons[1].setOnClickListener(v -> {
            setStepStatus(1, R.string.step_status_working);
            openUrl("https://galaxystore.samsung.com/detail/com.samsung.android.app.sans");
            markStepDone(1);
        });
        buttons[2].setOnClickListener(v -> {
            setStepStatus(2, R.string.step_status_working);
            openFontSettings();
            markStepDone(2);
        });
        buttons[3].setOnClickListener(v -> {
            setStepStatus(3, R.string.step_status_working);
            openBackup();
            markStepDone(3);
        });
        buttons[4].setOnClickListener(v -> downloadFont());
        buttons[5].setOnClickListener(v -> {
            setStepStatus(5, R.string.step_status_working);
            showInstructions();
            markStepDone(5);
        });
        buttons[6].setOnClickListener(v -> {
            setStepStatus(6, R.string.step_status_working);
            openFontSettings();
            markStepDone(6);
        });
        buttons[7].setOnClickListener(v -> {
            setStepStatus(7, R.string.step_status_working);
            openRestore();
            markStepDone(7);
        });

        // Auto-check if font already downloaded
        if (fontFile.exists() && fontFile.length() > 0) {
            markStepDone(4);
            try {
                preview.setTypeface(Typeface.createFromFile(fontFile));
            } catch (Exception e) {
                // ignore
            }
        }

        overallStatus.setText(R.string.select_font);
    }

    private void downloadFont() {
        setStepStatus(4, R.string.step_status_working);
        buttons[4].setEnabled(false);
        downloadProgress.setVisibility(View.VISIBLE);
        downloadProgress.setIndeterminate(true);
        downloadStatus.setText(getString(R.string.downloading_preview));
        overallProgress.setVisibility(View.VISIBLE);
        overallProgress.setIndeterminate(true);

        downloadHandle = Downloader.download(font.downloadUrl(), fontFile, new Downloader.Callback() {
            @Override
            public void onProgress(long downloaded, long total) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    if (total > 0) {
                        int pct = (int) (downloaded * 100 / total);
                        downloadProgress.setIndeterminate(false);
                        downloadProgress.setProgress(pct);
                        overallProgress.setIndeterminate(false);
                        overallProgress.setProgress(pct);
                        downloadStatus.setText(getString(R.string.download_progress, font.name, pct));
                        overallStatus.setText(getString(R.string.download_progress, font.name, pct));
                    } else {
                        downloadStatus.setText(getString(R.string.download_done, downloaded / 1024 + " KB"));
                    }
                });
            }

            @Override
            public void onSuccess(File file) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    downloadProgress.setVisibility(View.GONE);
                    overallProgress.setVisibility(View.GONE);
                    downloadStatus.setText(getString(R.string.download_done, file.getAbsolutePath()));
                    overallStatus.setText(getString(R.string.download_done, file.getAbsolutePath()));
                    fontPath.setText(getString(R.string.font_path_label, file.getAbsolutePath()));
                    markStepDone(4);
                    buttons[4].setEnabled(true);
                    try {
                        preview.setTypeface(Typeface.createFromFile(file));
                    } catch (Exception e) {
                        // ignore
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    downloadProgress.setVisibility(View.GONE);
                    overallProgress.setVisibility(View.GONE);
                    downloadStatus.setText(getString(R.string.download_failed, e.getMessage()));
                    overallStatus.setText(getString(R.string.download_failed, e.getMessage()));
                    setStepStatus(4, R.string.step_status_failed);
                    buttons[4].setEnabled(true);
                    Snackbar.make(findViewById(R.id.root), getString(R.string.download_failed, e.getMessage()), Snackbar.LENGTH_LONG)
                            .setAction(R.string.snackbar_retry, v -> downloadFont()).show();
                });
            }
        }, executor);
    }

    private void markStepDone(int step) {
        setStepStatus(step, R.string.step_status_done);
        // enable next button if exists
        if (step + 1 <= 7 && buttons[step + 1] != null) {
            buttons[step + 1].setEnabled(true);
        }
        overallStatus.setText(getString(R.string.step_status_done) + " — " + step + "/7");
    }

    private void setStepStatus(int step, int resId) {
        if (step >= 1 && step <= 7 && statuses[step] != null) {
            statuses[step].setText(resId);
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.open_settings_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void openFontSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.open_settings_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openBackup() {
        try {
            Intent i = new Intent();
            i.setClassName("com.samsung.android.scloud", "com.samsung.android.scloud.ui.activity.BackupActivity");
            startActivity(i);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.open_settings_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openRestore() {
        try {
            Intent i = new Intent();
            i.setClassName("com.samsung.android.scloud", "com.samsung.android.scloud.ui.activity.RestoreActivity");
            startActivity(i);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.open_settings_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showInstructions() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.apply_instructions_title)
                .setMessage(getString(R.string.apply_instructions, fontFile.getAbsolutePath()))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        if (downloadHandle != null) downloadHandle.cancel();
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}
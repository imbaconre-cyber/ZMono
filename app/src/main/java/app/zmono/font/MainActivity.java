package app.zmono.font;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recycler;
    private FontAdapter adapter;
    private ProgressBar progress;
    private TextView status;
    private View emptyState;
    private MaterialButton retryButton;
    private MaterialButton applyButton;
    private TextView previewTitle;
    private TextView previewText;
    private TextInputEditText searchInput;

    private final List<FontItem> allFonts = new ArrayList<>();
    private final List<FontItem> filtered = new ArrayList<>();

    private ExecutorService executor;
    private Handler mainHandler;
    private Downloader.Handle previewDownload;
    private volatile boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        recycler = findViewById(R.id.recycler);
        progress = findViewById(R.id.progress);
        status = findViewById(R.id.status);
        emptyState = findViewById(R.id.emptyState);
        retryButton = findViewById(R.id.retryButton);
        applyButton = findViewById(R.id.applyButton);
        previewTitle = findViewById(R.id.previewTitle);
        previewText = findViewById(R.id.previewText);
        searchInput = findViewById(R.id.searchInput);

        adapter = new FontAdapter((font, pos) -> onFontSelected(font));
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        applyButton.setOnClickListener(v -> {
            FontItem sel = adapter.getSelected();
            if (sel == null) {
                Toast.makeText(this, R.string.select_font, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, ApplyActivity.class);
            sel.putExtras(i);
            startActivity(i);
        });

        retryButton.setOnClickListener(v -> loadFonts());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadFonts();
    }

    private void loadFonts() {
        showLoading(true);
        showEmpty(false);
        executor.execute(() -> {
            try {
                List<FontItem> loaded = FontRepository.fetchFonts();
                mainHandler.post(() -> {
                    if (destroyed) return;
                    allFonts.clear();
                    allFonts.addAll(loaded);
                    applyFilter(searchInput.getText() == null ? "" : searchInput.getText().toString());
                    status.setText(getString(R.string.loaded_fonts, loaded.size()));
                    showLoading(false);
                    showEmpty(filtered.isEmpty());
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    status.setText(getString(R.string.failed_load, e.getMessage()));
                    showLoading(false);
                    showEmpty(true);
                    Snackbar.make(findViewById(R.id.root), getString(R.string.failed_load, e.getMessage()), Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> loadFonts()).show();
                });
            }
        });
    }

    private void applyFilter(String query) {
        filtered.clear();
        String q = query == null ? "" : query.trim().toLowerCase();
        for (FontItem f : allFonts) {
            if (q.isEmpty() || f.name.toLowerCase().contains(q) || f.category.toLowerCase().contains(q)) {
                filtered.add(f);
            }
        }
        adapter.setItems(filtered);
        showEmpty(filtered.isEmpty() && !allFonts.isEmpty());
    }

    private void onFontSelected(FontItem font) {
        previewTitle.setText(font.name);
        previewText.setText(font.name + " — " + getString(R.string.preview_sample));
        previewText.setTypeface(Typeface.DEFAULT);
        applyButton.setEnabled(false);

        // Cancel previous preview download
        if (previewDownload != null) previewDownload.cancel();

        File out = Downloader.targetFile(font.name);
        if (out.exists() && out.length() > 0) {
            try {
                previewText.setTypeface(Typeface.createFromFile(out));
                applyButton.setEnabled(true);
                status.setText(R.string.preview_ready);
            } catch (Exception e) {
                status.setText(getString(R.string.preview_failed, e.getMessage()));
            }
            return;
        }

        status.setText(R.string.downloading_preview);
        previewDownload = Downloader.download(font.downloadUrl(), out, new Downloader.Callback() {
            @Override
            public void onProgress(long downloaded, long total) { /* no-op for preview */ }

            @Override
            public void onSuccess(File file) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    try {
                        previewText.setTypeface(Typeface.createFromFile(file));
                        applyButton.setEnabled(true);
                        status.setText(R.string.preview_ready);
                    } catch (Exception e) {
                        status.setText(getString(R.string.preview_failed, e.getMessage()));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    status.setText(getString(R.string.preview_failed, e.getMessage()));
                });
            }
        }, executor);
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        if (previewDownload != null) previewDownload.cancel();
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}
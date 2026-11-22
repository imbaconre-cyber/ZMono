package app.zmono.font;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ApplyActivity extends AppCompatActivity {
    private String fontPath;
    private String fontName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        fontPath = getIntent().getStringExtra("fontPath");
        fontName = getIntent().getStringExtra("fontName");

        TextView nameTv = findViewById(R.id.fontName);
        TextView pathTv = findViewById(R.id.fontPath);
        nameTv.setText(fontName != null ? fontName : "Font");
        pathTv.setText(fontPath != null ? fontPath : "");

        Button b1 = findViewById(R.id.btnInstallSans);
        Button b2 = findViewById(R.id.btnSelectSans);
        Button b3 = findViewById(R.id.btnBackup);
        Button b5 = findViewById(R.id.btnInstallCustom);
        Button b6 = findViewById(R.id.btnSelectDefault);
        Button b7 = findViewById(R.id.btnRestore);

        b1.setOnClickListener(v -> openUrl("https://galaxystore.samsung.com/detail/com.samsung.android.app.sans"));
        b2.setOnClickListener(v -> openFontSettings());
        b3.setOnClickListener(v -> openBackup());
        b5.setOnClickListener(v -> installCustom());
        b6.setOnClickListener(v -> openFontSettings());
        b7.setOnClickListener(v -> openRestore());

        // preview
        TextView preview = findViewById(R.id.preview);
        if (fontPath != null) {
            File f = new File(fontPath);
            if (f.exists() && f.length() < 50L * 1024 * 1024) {
                try {
                    Typeface tf = Typeface.createFromFile(f);
                    preview.setTypeface(tf);
                } catch (Exception e) {
                    // ignore
                }
            } else if (f.exists()) {
                preview.setText("Preview skipped (font > 50MB). Large heap enabled for loading.");
            }
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    private void openFontSettings() {
        try {
            Intent i = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(i);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, "Open Settings > Display > Font manually", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "Open Samsung Cloud > Backup manually", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "Open Samsung Cloud > Restore manually", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void installCustom() {
        // Monofont approach: package the TTF as Samsung Sans APK. Full APK repackaging
        // requires a template APK + signing; guide the user for the manual step.
        new AlertDialog.Builder(this)
                .setTitle("Install Custom Font")
                .setMessage("ZMono saved the font to:\n" + fontPath +
                        "\n\nMonofont trick: replace the installed Samsung Sans package with one containing this TTF, then restore Settings from Samsung Cloud.\n\n" +
                        "Automated APK repackaging is device/ROM specific. Use the saved TTF with #mono_ or flash a Magisk module if needed.")
                .setPositiveButton("OK", null)
                .show();
    }
}
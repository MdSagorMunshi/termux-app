package com.termux.app.activities;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.termux.R;
import com.termux.shared.interact.ShareUtils;
import com.termux.shared.termux.TermuxConstants;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Toolbar back navigation
        MaterialToolbar toolbar = findViewById(R.id.about_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set dynamic version info
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView versionView = findViewById(R.id.about_version);
            versionView.setText("v" + pInfo.versionName);

            TextView versionCodeView = findViewById(R.id.about_version_code);
            versionCodeView.setText(String.valueOf(pInfo.versionCode));

            TextView packageNameView = findViewById(R.id.about_package_name);
            packageNameView.setText(pInfo.packageName);

            TextView buildTypeView = findViewById(R.id.about_build_type);
            buildTypeView.setText(com.termux.BuildConfig.DEBUG ? "Debug" : "Release");
        } catch (Exception e) {
            // Ignore
        }

        // Click handlers
        View githubRow = findViewById(R.id.about_github_row);
        githubRow.setOnClickListener(v ->
            ShareUtils.openUrl(this, TermuxConstants.TERMINUX_DEVELOPER_GITHUB_URL));

        View emailRow = findViewById(R.id.about_email_row);
        emailRow.setOnClickListener(v ->
            ShareUtils.openUrl(this, "mailto:" + TermuxConstants.TERMUX_SUPPORT_EMAIL_URL));

        View termuxLink = findViewById(R.id.about_termux_link);
        termuxLink.setOnClickListener(v ->
            ShareUtils.openUrl(this, "https://termux.dev"));

        // Animate hero section
        animateEntrance();
    }

    private void animateEntrance() {
        ImageView logo = findViewById(R.id.about_logo);
        if (logo != null) {
            logo.setAlpha(0f);
            logo.setScaleX(0.5f);
            logo.setScaleY(0.5f);
            logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .setStartDelay(200)
                .start();
        }
    }
}

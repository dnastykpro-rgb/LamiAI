package com.lumi.ai.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String errorText = Log.getStackTraceString(throwable);
            new Handler(Looper.getMainLooper()).post(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Ошибка запуска LUMI AI")
                        .setMessage(errorText.length() > 600 ? errorText.substring(0, 600) : errorText)
                        .setPositiveButton("Закрыть", (d, w) -> finish())
                        .setCancelable(false)
                        .show();
            });
        });

        try {
            EdgeToEdge.enable(this);
            super.onCreate(savedInstanceState);

            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setGravity(Gravity.CENTER);
            root.setBackgroundColor(Color.parseColor("#141414"));

            LinearLayout titleRow = new LinearLayout(this);
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setGravity(Gravity.CENTER);

            TextView txtLumi = new TextView(this);
            txtLumi.setText("LUMI ");
            txtLumi.setTextColor(Color.WHITE);
            txtLumi.setTextSize(46f);
            txtLumi.setTypeface(Typeface.DEFAULT_BOLD);

            TextView txtAi = new TextView(this);
            txtAi.setText("AI");
            txtAi.setTextColor(Color.parseColor("#E51A1A"));
            txtAi.setTextSize(46f);
            txtAi.setTypeface(Typeface.DEFAULT_BOLD);

            titleRow.addView(txtLumi);
            titleRow.addView(txtAi);

            TextView txtSub = new TextView(this);
            txtSub.setText("based at Google AI");
            txtSub.setTextColor(Color.parseColor("#A0A0A0"));
            txtSub.setTextSize(14f);
            txtSub.setGravity(Gravity.CENTER);

            root.addView(titleRow);
            root.addView(txtSub);

            root.setAlpha(0f);
            root.setScaleX(0.85f);
            root.setScaleY(0.85f);
            setContentView(root);

            root.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).start();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } catch (Exception e) {
                    showError(Log.getStackTraceString(e));
                }
            }, 1100);
        } catch (Exception e) {
            showError(Log.getStackTraceString(e));
        }
    }

    private void showError(String text) {
        new AlertDialog.Builder(this)
                .setTitle("Сбой при старте")
                .setMessage(text.length() > 600 ? text.substring(0, 600) : text)
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }
}

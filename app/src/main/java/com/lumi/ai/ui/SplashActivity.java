package com.lumi.ai.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.lumi.ai.R;

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
            setContentView(R.layout.activity_splash);

            View logo = findViewById(R.id.imgSplashLogo);
            if (logo != null) {
                logo.setScaleX(0.85f);
                logo.setScaleY(0.85f);
                logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).start();
            }

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

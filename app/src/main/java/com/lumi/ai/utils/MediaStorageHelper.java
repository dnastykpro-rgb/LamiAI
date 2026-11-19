package com.lumi.ai.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

public class MediaStorageHelper {

    // Сжимаем фото из галереи/камеры и переводим в Base64 для отправки в Gemini
    public static String uriToBase64(Context context, Uri imageUri) {
        try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
            Bitmap original = BitmapFactory.decodeStream(is);
            if (original == null) return null;

            int maxDim = 1600;
            int width = original.getWidth();
            int height = original.getHeight();
            if (width > maxDim || height > maxDim) {
                float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
                original = Bitmap.createScaledBitmap(original, Math.round(width * ratio), Math.round(height * ratio), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    // Сохраняем сгенерированную картинку из Base64 во внутреннюю папку чата
    public static String saveBase64ImageToInternal(Context context, String base64Data) {
        try {
            byte[] decoded = Base64.decode(base64Data, Base64.DEFAULT);
            File dir = new File(context.getFilesDir(), "lumi_media");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "img_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decoded);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    // Сохраняем скачанный MP4-видеофайл во внутреннюю папку чата
    public static String saveVideoStreamToInternal(Context context, ResponseBody body) {
        try {
            File dir = new File(context.getFilesDir(), "lumi_media");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "vid_" + System.currentTimeMillis() + ".mp4");
            try (InputStream is = body.byteStream(); FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    // Экспорт фото или видео в общую Галерею телефона
    public static boolean exportMediaToGallery(Context context, String internalPath, boolean isVideo) {
        try {
            File sourceFile = new File(internalPath);
            if (!sourceFile.exists()) return false;

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, isVideo ? "video/mp4" : "image/png");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, isVideo ? Environment.DIRECTORY_MOVIES + "/LumiAI" : Environment.DIRECTORY_PICTURES + "/LumiAI");

            Uri collection = isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri itemUri = context.getContentResolver().insert(collection, values);
            if (itemUri == null) return false;

            try (InputStream is = new FileInputStream(sourceFile);
                 OutputStream os = context.getContentResolver().openOutputStream(itemUri)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


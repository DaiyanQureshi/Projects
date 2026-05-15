package com.daiyan.dataencrypt;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioUtils {

    static String selectedFileRealName;

    public static void setSelectedFileRealName(String name) {
        selectedFileRealName = name;
    }

    public static byte[] loadAudio(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public static String saveAudio(Context context, byte[] audioData) throws IOException {
        // Seedha folder target karo
        File folder = new File(Environment.getExternalStorageDirectory(), "StegX Encryption");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Exact original file ka naam lo
        String fileName = (selectedFileRealName != null) ? selectedFileRealName : "stegx_encrypted_audio.wav";
        File file = new File(folder, fileName);

        // BRUTAL FORCE OVERWRITE: Agar purani file wahan hai, toh usko jad se ukhad do
        if (file.exists()) {
            boolean isDeleted = file.delete();
            // Delete hone ke baad thoda time do OS ko file system update karne ke liye
            if (!isDeleted) {
                showToast(context, "Warning: Purani file delete nahi ho paayi!");
            }
        }

        // Ab naya data (naya encrypted message) physically hard disk par likho
        try (FileOutputStream out = new FileOutputStream(file, false)) { // 'false' means overwrite, not append
            out.write(audioData);
            out.flush(); // Memory buffer se force push karo disk par (Bohot zaroori step!)

            showToast(context, "Audio file forcefully replaced!");
            return file.getAbsolutePath();

        } catch (Exception e) {
            showToast(context, "Save Error: " + e.getMessage());
            throw e;
        }
    }
    // Helper function to find existing file URI by its name
    private static Uri getUriFromDisplayName(Context context, String displayName) {
        String[] projection = {MediaStore.Audio.Media._ID};
        String selection = MediaStore.Audio.Media.DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{displayName};

        Uri queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = context.getContentResolver().query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            }
        } catch (Exception e) {
            Log.e("AudioUtils", "Error querying MediaStore", e);
        }
        return null;
    }

    private static void showToast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}
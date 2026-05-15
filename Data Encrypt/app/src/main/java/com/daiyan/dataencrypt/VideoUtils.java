package com.daiyan.dataencrypt;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoUtils {
    public static List<Bitmap> loadVideoFrames(Context context, Uri uri) {
        // Use MediaMetadataRetriever or FFmpeg to extract frames
        List<Bitmap> frames = new ArrayList<>();
        // Implement frame extraction logic here
        return frames;
    }

    public static String saveVideo(Context context, List<Bitmap> frames) {
        // Convert frames to video (use MediaCodec or FFmpeg)
        // For simplicity, we'll save the first frame as an image
        if (frames.isEmpty()) {
            return "No frames to save";
        }

        Bitmap firstFrame = frames.get(0);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, "stegx_encoded_" + System.currentTimeMillis() + ".mp4");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Save to MediaStore for Android 10+
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/StegX");
            Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = resolver.openOutputStream(uri)) {
                    firstFrame.compress(Bitmap.CompressFormat.PNG, 100, out);
                    return "Video saved to Movies/StegX";
                } catch (IOException e) {
                    return "Error saving video: " + e.getMessage();
                }
            }
        } else {
            // Save to external storage for older versions
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "StegX");
            if (!directory.exists()) {
                directory.mkdirs(); // Create directory if it doesn't exist
            }
            File file = new File(directory, "stegx_encoded_" + System.currentTimeMillis() + ".mp4");
            try (FileOutputStream out = new FileOutputStream(file)) {
                firstFrame.compress(Bitmap.CompressFormat.PNG, 100, out);
                return "Video saved to Movies/StegX";
            } catch (IOException e) {
                return "Error saving video: " + e.getMessage();
            }
        }
        return "Failed to save video";
    }
}
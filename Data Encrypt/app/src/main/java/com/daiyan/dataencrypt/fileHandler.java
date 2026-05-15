package com.daiyan.dataencrypt;


import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.FileInputStream;

public class fileHandler {

    public static void saveToFile(Context context, String filename, String data) {
        try {
            // Get directory for internal storage
            File direcotory = new File(Environment.getExternalStorageDirectory(),"StegX Encryption");

            if (!direcotory.exists()) {
            direcotory.mkdirs(); // Create directory if it doesn't exist
        }
            File file = new File(direcotory,filename);

            // Write data to the file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();

            Log.d("FileHelper", "File saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(Context context, String filename) {
        try {
            File direcotory = new File(Environment.getExternalStorageDirectory(),"StegX Encryption");
            File file = new File(direcotory, filename);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {}
//        // Save to MediaStore for Android 10+ (Scoped Storage)
//        values.put(MediaStore.Images.Media.RELATIVE_PATH, "StegX Encryption"); // Change directory name
//        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        if (uri != null) {
//            try (OutputStream out = resolver.openOutputStream(uri)) {
//                showToast("Image saved to StegX Encryption");
//            }
//        }
//    } else {
//        // Save to external storage for older versions
//        File directory = new File(Environment.getExternalStorageDirectory(), "StegX Encryption"); // Changed path
//        if (!directory.exists()) {
//            directory.mkdirs(); // Create directory if it doesn't exist
//        }
//        File file = new File(directory, "stegx_encoded_" + System.currentTimeMillis() + ".png");
//        try (FileOutputStream out = new FileOutputStream(file)) {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//            showToast("Image saved to StegX Encryption");
//        }
//    }


}

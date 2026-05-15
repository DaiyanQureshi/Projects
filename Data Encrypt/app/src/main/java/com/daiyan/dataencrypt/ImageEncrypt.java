package com.daiyan.dataencrypt;

import android.graphics.Bitmap;
import java.nio.charset.StandardCharsets;

public class ImageEncrypt {

    public static Bitmap encodeMessage(Bitmap bitmap, String message) {
        if (bitmap == null || message == null || message.isEmpty()) {
            return bitmap;
        }

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Use long to prevent integer overflow on large images
        long availableBits = ((long) width * height) - 32;

        if ((long) messageBytes.length * 8 > availableBits) {
            return bitmap; // Or throw exception
        }

        byte[] data = new byte[4 + messageBytes.length];
        int length = messageBytes.length;
        data[0] = (byte) (length >>> 24);
        data[1] = (byte) (length >>> 16);
        data[2] = (byte) (length >>> 8);
        data[3] = (byte) length;
        System.arraycopy(messageBytes, 0, data, 4, messageBytes.length);

        // Sirf tab copy karo jab zarurat ho
        Bitmap mutableBitmap = bitmap.isMutable() ? bitmap : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (mutableBitmap == null) {
            return null;
        }

        // Memory optimization: Sirf ek row ka array banaya
        int[] rowPixels = new int[width];
        int dataIndex = 0;
        int bitPosition = 7;
        boolean encodingDone = false;

        for (int y = 0; y < height && !encodingDone; y++) {
            mutableBitmap.getPixels(rowPixels, 0, width, 0, y, width, 1);

            for (int x = 0; x < width; x++) {
                if (dataIndex >= data.length) {
                    encodingDone = true;
                    break;
                }

                int currentByte = data[dataIndex] & 0xFF;
                int bit = (currentByte >> bitPosition) & 1;

                int pixel = rowPixels[x];
                int newBlue = (pixel & 0xFF) & 0xFE | bit;
                rowPixels[x] = (pixel & 0xFFFFFF00) | newBlue;

                if (--bitPosition < 0) {
                    bitPosition = 7;
                    dataIndex++;
                }
            }
            // Modified row ko wapas bitmap me set kar diya
            mutableBitmap.setPixels(rowPixels, 0, width, 0, y, width, 1);
        }

        return mutableBitmap;
    }

    public static String decodeMessage(Bitmap bitmap) {
        if (bitmap == null) return "";

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] rowPixels = new int[width];

        int messageLength = 0;
        int bitsRead = 0;
        boolean lengthRead = false;

        // Step 1: Sirf pehli length read karne ke liye loop
        for (int y = 0; y < height && !lengthRead; y++) {
            bitmap.getPixels(rowPixels, 0, width, 0, y, width, 1);
            for (int x = 0; x < width; x++) {
                int bit = rowPixels[x] & 1;
                messageLength |= bit << (31 - bitsRead);
                bitsRead++;

                if (bitsRead == 32) {
                    lengthRead = true;
                    break;
                }
            }
        }

        if (messageLength <= 0 || messageLength > (width * height - 32) / 8) {
            return "";
        }

        byte[] messageBytes = new byte[messageLength];
        int byteIndex = 0;
        int currentByte = 0;
        int bitPosition = 7;
        bitsRead = 0; // Length header track karne ke liye reset

        // Step 2: Main message bytes decode karne ke liye row loop
        for (int y = 0; y < height && byteIndex < messageLength; y++) {
            bitmap.getPixels(rowPixels, 0, width, 0, y, width, 1);
            for (int x = 0; x < width; x++) {

                // Pehle 32 bits (header) skip karna zaruri hai
                if (bitsRead < 32) {
                    bitsRead++;
                    continue;
                }

                int bit = rowPixels[x] & 1;
                currentByte |= bit << bitPosition;

                if (--bitPosition < 0) {
                    messageBytes[byteIndex++] = (byte) currentByte;
                    currentByte = 0;
                    bitPosition = 7;
                    if (byteIndex >= messageLength) break;
                }
            }
        }

        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
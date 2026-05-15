package com.daiyan.dataencrypt;

import android.graphics.Bitmap;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class VideoEncrypt {

    // Encodes message across video frames
    public static List<Bitmap> encodeVideoFrames(List<Bitmap> originalFrames, String message) {
        byte[] data = prepareVideoData(message);
        int dataIndex = 0;
        int bitPosition = 7;

        for (Bitmap frame : originalFrames) {
            Bitmap mutableFrame = frame.copy(Bitmap.Config.ARGB_8888, true);
            int[] pixels = new int[mutableFrame.getWidth() * mutableFrame.getHeight()];
            mutableFrame.getPixels(pixels, 0, mutableFrame.getWidth(), 0, 0,
                    mutableFrame.getWidth(), mutableFrame.getHeight());

            for (int i = 0; i < pixels.length; i++) {
                if (dataIndex >= data.length) break;

                int currentByte = data[dataIndex] & 0xFF;
                int bit = (currentByte >> bitPosition) & 1;

                // Embed in blue channel
                pixels[i] = (pixels[i] & 0xFFFFFFFE) | bit;

                if (--bitPosition < 0) {
                    bitPosition = 7;
                    dataIndex++;
                }
            }
            mutableFrame.setPixels(pixels, 0, mutableFrame.getWidth(), 0, 0,
                    mutableFrame.getWidth(), mutableFrame.getHeight());
        }
        return originalFrames;
    }

    // Decodes message from video frames
    public static String decodeVideoFrames(List<Bitmap> encodedFrames) {
        byte[] data = new byte[encodedFrames.size() * 1000]; // Initial buffer
        int byteIndex = 0;
        int bitPosition = 7;

        for (Bitmap frame : encodedFrames) {
            int[] pixels = new int[frame.getWidth() * frame.getHeight()];
            frame.getPixels(pixels, 0, frame.getWidth(), 0, 0,
                    frame.getWidth(), frame.getHeight());

            for (int pixel : pixels) {
                int bit = pixel & 1;
                data[byteIndex] |= bit << bitPosition;

                if (--bitPosition < 0) {
                    bitPosition = 7;
                    if (++byteIndex >= data.length) break;
                }
            }
        }
        return new String(data, StandardCharsets.UTF_8).trim();
    }

    private static byte[] prepareVideoData(String message) {
        byte[] header = message.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[header.length + 4];
        System.arraycopy(header, 0, data, 4, header.length);
        return data;
    }
}
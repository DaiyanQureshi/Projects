package com.daiyan.dataencrypt;

import java.nio.charset.StandardCharsets;

public class AudioEncrypt {

    // Encodes message into audio data
    public static byte[] encodeAudio(byte[] audioData, String message) {
        if (audioData == null || message == null || message.isEmpty()) return audioData;

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;

        // Space check: Audio file me itni jagah honi chahiye ki message aur header aa sake (1 byte message = 8 bytes audio)
        if (audioData.length < (messageLength + 4) * 8) {
            throw new IllegalArgumentException("Audio file is too small to hold this much data.");
        }

        byte[] header = new byte[4];
        header[0] = (byte) (messageLength >>> 24);
        header[1] = (byte) (messageLength >>> 16);
        header[2] = (byte) (messageLength >>> 8);
        header[3] = (byte) messageLength;

        int dataIndex = 0;

        // Embed header
        for (byte b : header) {
            for (int j = 0; j < 8; j++) {
                int bit = (b >> (7 - j)) & 1;
                audioData[dataIndex] = (byte) ((audioData[dataIndex] & 0xFE) | bit);
                dataIndex++;
            }
        }

        // Embed message
        for (byte b : messageBytes) {
            for (int j = 0; j < 8; j++) {
                int bit = (b >> (7 - j)) & 1;
                audioData[dataIndex] = (byte) ((audioData[dataIndex] & 0xFE) | bit);
                dataIndex++;
            }
        }

        return audioData;
    }

    // Decodes message from audio data
    public static String decodeAudio(byte[] audioData) {
        if (audioData == null || audioData.length < 32) return "Invalid File";

        int dataIndex = 0;

        // Read header
        byte[] header = new byte[4];
        for (int i = 0; i < 4; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                int bit = audioData[dataIndex] & 1;
                b = (byte) ((b << 1) | bit);
                dataIndex++;
            }
            header[i] = b;
        }

        int messageLength = ((header[0] & 0xFF) << 24) |
                ((header[1] & 0xFF) << 16) |
                ((header[2] & 0xFF) << 8) |
                (header[3] & 0xFF);

        // Security check: Agar length negative hai ya audio file ki limit se zyada hai, toh corrupt file hai
        if (messageLength <= 0 || messageLength > (audioData.length - 32) / 8) {
            return "No hidden message found or corrupted file.";
        }

        // Read message
        byte[] messageBytes = new byte[messageLength];
        for (int i = 0; i < messageLength; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                int bit = audioData[dataIndex] & 1;
                b = (byte) ((b << 1) | bit);
                dataIndex++;
            }
            messageBytes[i] = b;
        }

        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
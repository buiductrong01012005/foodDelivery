package com.example.fooddelivery.Service;

import com.example.fooddelivery.Utils.NameConvertUtil; // Import lớp tiện ích

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImageRenService {

    /**
     * Tải ảnh từ URL, lưu với tên file đã được chuẩn hóa và phần mở rộng gốc.
     *
     * @param imageUrl URL của ảnh trên web.
     * @param baseFileName Tên file cơ sở (đã được chuẩn hóa, ví dụ: "com-suon-bi-cha").
     * @return Tên file đầy đủ đã được lưu (ví dụ: "com-suon-bi-cha.jpg"), hoặc null nếu thất bại.
     * @throws IOException Nếu có lỗi.
     */
    public static String downloadAndSaveImage(String imageUrl, String baseFileName) throws IOException {
        String resourcesPath = "D:\\CNPM\\foodDelivery\\FoodDelivery\\src\\main\\resources\\images"; // Sua theo may cua ban.

        File directory = new File(resourcesPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Không thể tạo thư mục lưu trữ: " + resourcesPath);
            }
        }

        URL url;
        try {
            url = new URL(imageUrl);
        } catch (MalformedURLException e) {
            throw new IOException("URL hình ảnh không hợp lệ: " + imageUrl, e);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            throw new IOException("Lỗi tải ảnh từ server. Mã phản hồi: " + responseCode + " cho URL: " + imageUrl);
        }

        // Lấy phần mở rộng từ URL gốc hoặc từ Content-Type
        String extension = NameConvertUtil.getFileExtension(url.getPath()); // Ưu tiên từ path
        if (extension.isEmpty()) {
            String contentType = connection.getContentType();
            if (contentType != null && contentType.startsWith("images/")) {
                extension = contentType.substring("images/".length());
                if ("jpeg".equalsIgnoreCase(extension)) extension = "jpg";
            } else {
                extension = "jpg"; // Mặc định là jpg nếu không xác định được
            }
        }

        // Tạo tên file cuối cùng
        String finalFileName = baseFileName + "." + extension;
        Path savePath = Paths.get(resourcesPath, finalFileName);

        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, savePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Đã tải và lưu ảnh thành công: " + savePath.toString());
            return finalFileName; // Trả về tên file đã lưu (bao gồm đuôi)
        } finally {
            connection.disconnect();
        }
    }
}

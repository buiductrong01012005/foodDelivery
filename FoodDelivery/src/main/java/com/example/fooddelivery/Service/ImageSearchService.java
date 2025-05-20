package com.example.fooddelivery.Service;

import com.fasterxml.jackson.databind.JsonNode; // Ví dụ dùng Jackson
import com.fasterxml.jackson.databind.ObjectMapper; // Ví dụ dùng Jackson
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ImageSearchService {

    // Thay thế bằng API Key và Custom Search Engine ID của bạn nếu dùng Google
    private static final String GOOGLE_API_KEY = "AIzaSyC4x1-CKSSQw5nL0cwBJmk2nTbSF_eu9PU";
    private static final String GOOGLE_CX_ID = "92902084094f447b2"; // Custom Search Engine ID

    /**
     * Tìm kiếm URL hình ảnh dựa trên từ khóa (tên món ăn).
     * Đây là ví dụ giả định cho Google Custom Search API.
     * Bạn cần thay thế bằng logic gọi API thực tế.
     *
     * @param query Tên món ăn để tìm kiếm.
     * @param numResults Số lượng URL ảnh muốn lấy.
     * @return Danh sách các URL hình ảnh, hoặc danh sách rỗng nếu không tìm thấy/lỗi.
     */
    public static List<String> searchImageUrls(String query, int numResults) {
        List<String> imageUrls = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return imageUrls;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            // Ví dụ URL cho Google Custom Search API (cần thay thế bằng API thực tế bạn chọn)
            // API này thường trả về JSON
            String apiUrl = String.format(
                    "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image&num=%d&imgSize=medium",
                    GOOGLE_API_KEY, GOOGLE_CX_ID, encodedQuery, numResults
            );

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                // Đọc toàn bộ response thành String (đơn giản hóa, có thể dùng StringBuilder)
                String jsonResponse = new Scanner(inputStream, StandardCharsets.UTF_8.toString()).useDelimiter("\\A").next();
                inputStream.close();

                // Phân tích JSON để lấy URL ảnh (sử dụng Jackson làm ví dụ)
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonResponse);

                if (rootNode.has("items")) {
                    for (JsonNode item : rootNode.get("items")) {
                        if (item.has("link")) {
                            imageUrls.add(item.get("link").asText());
                        }
                    }
                }
            } else {
                System.err.println("Lỗi khi gọi API tìm kiếm ảnh: " + responseCode);
                // Đọc thông báo lỗi từ server nếu có
                try (InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        String errorResponse = new Scanner(errorStream, StandardCharsets.UTF_8.toString()).useDelimiter("\\A").next();
                        System.err.println("Chi tiết lỗi từ server: " + errorResponse);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Lỗi xảy ra khi tìm kiếm ảnh: " + e.getMessage());
            e.printStackTrace();
        }
        return imageUrls;
    }

//    public static void main(String[] args) {
//
//         List<String> urls = searchImageUrls("Sữa chua mít ", 1);
//         if (!urls.isEmpty()) {
//             System.out.println("URL ảnh tìm được: " + urls.get(0));
//         } else {
//             System.out.println("Không tìm thấy ảnh.");
//         }
//    }
}
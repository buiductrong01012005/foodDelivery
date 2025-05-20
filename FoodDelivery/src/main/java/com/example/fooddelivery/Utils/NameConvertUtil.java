package com.example.fooddelivery.Utils; // Hoặc package phù hợp của bạn

import java.text.Normalizer;
import java.util.regex.Pattern;

public class NameConvertUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Chuyển đổi chuỗi thành dạng viết thường, không dấu, viết liền.
     * @param input Chuỗi đầu vào
     * @return Chuỗi đã được chuẩn hóa (slugified)
     */
    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "default-image-name"; // Hoặc một tên mặc định khác
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = DIACRITICS.matcher(normalized).replaceAll("");
        slug = NONLATIN.matcher(slug).replaceAll(""); // Loại bỏ các ký tự không phải chữ, số, gạch ngang
        return slug.toLowerCase();
    }

    /**
     * Lấy phần mở rộng (đuôi file) từ một tên file hoặc URL.
     * @param fileNameOrUrl Tên file hoặc URL
     * @return Phần mở rộng file viết thường, hoặc chuỗi rỗng nếu không tìm thấy.
     */
    public static String getFileExtension(String fileNameOrUrl) {
        if (fileNameOrUrl == null || fileNameOrUrl.isEmpty()) {
            return "";
        }
        String cleanUrl = fileNameOrUrl;
        // Loại bỏ query parameters nếu là URL
        int queryParamIndex = cleanUrl.indexOf('?');
        if (queryParamIndex > -1) {
            cleanUrl = cleanUrl.substring(0, queryParamIndex);
        }

        int dotIndex = cleanUrl.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < cleanUrl.length() - 1) {
            return cleanUrl.substring(dotIndex + 1).toLowerCase();
        }
        return ""; // Mặc định nếu không có đuôi file rõ ràng
    }

    public static void main(String[] args) {
        System.out.println(toSlug("Cơm Sườn Bì Chả")); // com-suon-bi-cha
        System.out.println(toSlug("Phở Bò Tái Nạm Gầu ٩(̾●̮̮̃̾•̃̾)۶")); // pho-bo-tai-nam-gau
        System.out.println(getFileExtension("image.JPG")); // jpg
        System.out.println(getFileExtension("http://example.com/archive.tar.gz")); // gz
        System.out.println(getFileExtension("nodotextension")); // ""
        System.out.println(getFileExtension("http://example.com/image.png?param=value&another=val")); // png
    }
}

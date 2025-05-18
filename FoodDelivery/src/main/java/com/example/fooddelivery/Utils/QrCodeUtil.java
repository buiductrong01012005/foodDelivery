package com.example.fooddelivery.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class QrCodeUtil {

    public static void showQrCode(String orderCode, double totalAmount, List<String> productDetails) {
        StringBuilder qrContent = new StringBuilder();
        qrContent.append("\u0110\u01a1n h\u00e0ng: ").append(orderCode).append("\n");
        qrContent.append("Tổng tiền: ").append(String.format("%.0f VN\u0110", totalAmount)).append("\n");
        qrContent.append("Sản phẩm:\n");

        for (String item : productDetails) {
            qrContent.append("- ").append(item).append("\n");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(new String(qrContent.toString().getBytes("UTF-8"), "UTF-8"),
                    BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image fxImage = SwingFXUtils.toFXImage(qrImage, null);

            ImageView qrView = new ImageView(fxImage);
            qrView.setFitWidth(300);
            qrView.setFitHeight(300);

            Alert qrAlert = new Alert(Alert.AlertType.INFORMATION);
            qrAlert.setTitle("Mã QR đơn hàng");
            qrAlert.setHeaderText("Quét mã để xem đơn hàng");
            qrAlert.getDialogPane().setContent(qrView);
            qrAlert.showAndWait();

        } catch (WriterException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Lỗi tạo mã QR");
            error.setContentText("Không thể tạo mã QR: " + e.getMessage());
            error.showAndWait();
        }
    }
}

package com.example.fooddelivery.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class QrCodeUtil {

    public static void showQrCode(String content) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image fxImage = SwingFXUtils.toFXImage(qrImage, null);

            ImageView qrView = new ImageView(fxImage);
            qrView.setFitWidth(200);
            qrView.setFitHeight(200);

            Alert qrAlert = new Alert(Alert.AlertType.INFORMATION);
            qrAlert.setTitle("Mã QR đơn hàng");
            qrAlert.setHeaderText("Quét mã để theo dõi đơn hàng:");
            qrAlert.getDialogPane().setContent(qrView);
            qrAlert.showAndWait();

        } catch (WriterException e) {
            System.err.println("Lỗi tạo mã QR: " + e.getMessage());
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Lỗi tạo mã QR");
            error.setHeaderText("Không thể tạo mã QR");
            error.setContentText(e.getMessage());
            error.showAndWait();
        }
    }
}

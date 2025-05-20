module org.example.fooddelivery {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    requires jbcrypt;
    requires com.mailjet.api;
    requires org.json;

    // ✅ Thư viện tạo QR code
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // ✅ JavaFX Swing + AWT dùng để hiển thị ảnh QR code
    requires javafx.swing;

    // ✅ Thư viện xử lý JSON nếu dùng Gson
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires java.sql;

    // ✅ Mở model cho Gson và JavaFX table/view binding
    opens com.example.fooddelivery.Model to com.google.gson, javafx.base;

    // ✅ Mở các gói cho JavaFX để load FXML
    opens com.example.fooddelivery to javafx.fxml;
    opens com.example.fooddelivery.Controller to javafx.fxml;
    opens com.example.fooddelivery.Utils to javafx.fxml;

    // ✅ Export để các module khác có thể sử dụng nếu cần
    exports com.example.fooddelivery;
    exports com.example.fooddelivery.Controller;
    exports com.example.fooddelivery.Model;
    exports com.example.fooddelivery.Utils;
}

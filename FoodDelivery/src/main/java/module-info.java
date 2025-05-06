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
    requires java.sql;
    requires jbcrypt;
    requires com.mailjet.api;
    requires org.json;

    // ✅ Thêm dòng này để sử dụng thư viện Gson
    requires com.google.gson;

    // ✅ Mở package chứa Model để Gson có thể truy cập bằng reflection
    opens com.example.fooddelivery.Model to com.google.gson;

    // Mở package chứa Controller cho JavaFX FXML
    opens com.example.fooddelivery to javafx.fxml;
    opens com.example.fooddelivery.Controller to javafx.fxml;

    exports com.example.fooddelivery;
    exports com.example.fooddelivery.Controller;
}

module org.badmus.smartexpensetrancker {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  requires org.controlsfx.controls;
  requires com.dlsc.formsfx;
  requires net.synedra.validatorfx;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.bootstrapfx.core;

  requires com.almasb.fxgl.all;
  requires java.sql;
  requires static lombok;

  opens org.badmus.smartexpensetrancker to javafx.fxml;
  opens org.badmus.smartexpensetrancker.controller to javafx.fxml; // Add this line
  exports org.badmus.smartexpensetrancker;
}

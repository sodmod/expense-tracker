package org.badmus.smartexpensetrancker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ExpenseTrackerApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/expensetrancker/view/MainView.fxml")));
    stage.setTitle("Smart Expense Tracker");
    stage.setScene(new Scene(root));
    stage.setMinWidth(650);
    stage.setMinHeight(600);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

package org.badmus.smartexpensetrancker.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class GeneralUtils {


  // Utility methods for input and alert dialogs
  public static void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Information");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static boolean validateIfTextFieldIsNumber(TextField textField){
    return textField.getText().matches("\\d*\\.?\\d{0,2}");
  }

  public static boolean validateTextFieldNull(TextField textField ) {
    return textField.getText().isEmpty();
  }

  public static boolean validateDateFieldNull(DatePicker datePicker){
    return datePicker.getValue() == null;
  }

}

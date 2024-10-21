package org.badmus.smartexpensetrancker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Data;
import org.badmus.smartexpensetrancker.ExpenseTrackerApplication;
import org.badmus.smartexpensetrancker.config.DatabaseConfig;
import org.badmus.smartexpensetrancker.model.Expense;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

import static java.lang.System.out;
import static org.badmus.smartexpensetrancker.config.AppConfig.APP_DIR;
import static org.badmus.smartexpensetrancker.config.AppConfig.getInstance;
import static org.badmus.smartexpensetrancker.utils.GeneralUtils.*;

@Data
public class ExpenseTrackerController {

  @FXML
  private TextField amountField;
  @FXML
  private DatePicker datePicker;
  @FXML
  private TextField searchField;
  @FXML
  private RadioButton dailyRadio;
  @FXML
  private RadioButton weeklyRadio;
  @FXML
  private TextField categoryField;
  @FXML
  private RadioButton monthlyRadio;
  @FXML
  private TextField descriptionField;
  @FXML
  private ToggleGroup timeFrameGroup;
  @FXML
  private ListView<String> resultsListView;
  @FXML
  private ListView<Expense> summaryListView;

  public void initialize() {
    DatabaseConfig.createTable();
  }

  /**
   * These controllers handles every action buttons to connect to the db
   * the handleAddExpense, handleViewSummary, handleSearch and handleGenerateReport
   * Note: This above controller method are not view controllers
   */
  @FXML
  public void handleAddExpense() {
    // Collect user input
    // Restrict input to numbers and decimal points for amountField
    out.println("*********** adding expense *************");
    if (!validateIfTextFieldIsNumber(amountField)) {
      showAlert("Please enter a valid amount.");
      return;
    }
    if (validateTextFieldNull(amountField)) {
      showAlert("Amount field cannot be null");
      return;
    }

    if (validateTextFieldNull(categoryField)) {
      showAlert("Category field cannot be null");
      return;
    }

    if (validateTextFieldNull(descriptionField)) {
      showAlert("description field cannot be null");
      return;
    }

    if (validateDateFieldNull(datePicker)) {
      showAlert("Date field cannot be empty");
      return;
    }


    LocalDate date = datePicker.getValue();
    String category = categoryField.getText();
    String description = descriptionField.getText();
    double amount = Double.parseDouble(amountField.getText());

    // Add to the database
    String sql = getInstance().getProperty("sql.query.add_expense_into_db");
    try (Connection conn = DatabaseConfig.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setDouble(1, amount);
      pstmt.setString(2, category);
      pstmt.setDate(3, java.sql.Date.valueOf(date));
      pstmt.setString(4, description);
      pstmt.executeUpdate();
      showAlert("Expense added successfully!");

      // clear input field
      amountField.clear();
      categoryField.clear();
      descriptionField.clear();
      datePicker.setValue(null);
      out.print("************ successfully add expense ****************");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void handleSummaryAction(ActionEvent actionEvent) {
    try {
      // Load the ExpenseSummaryView.fxml file
      Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
      FXMLLoader loader = new FXMLLoader(getClass().getResource(APP_DIR.concat(getInstance().getProperty("views.ExpenseSummaryView"))));
      Parent root = loader.load();

      // Get the controller of ExpenseSummaryView to access its ListView
      ExpenseTrackerController controller = loader.getController();

      populateExpenseSummary(controller, getInstance().getProperty("sql.query.populate_expense_summary"));
      // Set the new scene
      stage.setScene(new Scene(root));
      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void handleFilterAction() {
    timeFrameGroup = new ToggleGroup();
    dailyRadio.setToggleGroup(timeFrameGroup);
    weeklyRadio.setToggleGroup(timeFrameGroup);
    monthlyRadio.setToggleGroup(timeFrameGroup);
    String timeFrame = "";
    if (dailyRadio.isSelected()) {
      timeFrame = "daily";
    } else if (weeklyRadio.isSelected()) {
      timeFrame = "weekly";
    } else if (monthlyRadio.isSelected()) {
      timeFrame = "monthly";
    } else {
      showAlert("Error", "Please select a time frame.");
      return;
    }

    String sql = switch (timeFrame) {
      case "daily" -> getInstance().getProperty("sql.query.filterDaily");
      case "weekly" -> getInstance().getProperty("sql.query.filterWeekly");
      case "monthly" -> getInstance().getProperty("sql.query.filterMonthly");
      default -> "";
    };

    populateExpenseSummary(this, sql);
  }

  @FXML
  public void handleSearch() {
    // User inputs keyword or date range
    String keyword = searchField.getText().trim();

    // Get the date input from the DatePicker (optional)
    LocalDate selectedDate = datePicker.getValue();

    // Use ObservableList to ensure the ListView observes changes
    ObservableList<String> searchResults = FXCollections.observableArrayList();

    // Base SQL query (we'll add conditions to this depending on user input)
    StringBuilder sql = new StringBuilder(getInstance().getProperty("sql.query.handleSearch.baseQuery"));

    // Flags to track whether we're adding conditions for keyword and date
    boolean isKeywordProvided = !keyword.isEmpty();
    boolean isDateProvided = selectedDate != null;

    // Add SQL conditions based on user input
    if (isKeywordProvided) {
      sql.append(" ").append(getInstance().getProperty("sql.query.handleSearch.isKeywordProvided"));
    }
    if (isDateProvided) {
      sql.append(" ").append(getInstance().getProperty("sql.query.handleSearch.isDateProvided"));
    }

    try (Connection conn = DatabaseConfig.connect(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
      int paramIndex = 1;

      // Set parameters for the keyword if provided
      if (isKeywordProvided) {
        pstmt.setString(paramIndex++, "%" + keyword + "%");
        pstmt.setString(paramIndex++, "%" + keyword + "%");
      }

      // Set parameter for the date if provided
      if (isDateProvided) {
        pstmt.setDate(paramIndex, java.sql.Date.valueOf(selectedDate));
      }

      ResultSet rs = pstmt.executeQuery();

      // Populate the ListView with the search results
      while (rs.next()) {
        String result = "Amount: $" + rs.getDouble("amount") +
          ", Category: " + rs.getString("category") +
          ", Date: " + rs.getString("date") +
          ", Description: " + rs.getString("description");

        searchResults.add(result);  // Add result to the ObservableList
      }

      // Update the ListView with the ObservableList
      resultsListView.setItems(searchResults);

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void handleGenerateReport() {
    out.println("******************** generating expense report ***********************");
    // SQL to retrieve all expenses
    String sql = getInstance().getProperty("sql.query.handleGenerateReport");
    try (Connection conn = DatabaseConfig.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery();
         FileWriter writer = new FileWriter("expense_report.txt")) {

      // Write the header of the report
      writer.write("Expense Report\n");
      writer.write("===============================\n");

      // Iterate over the result set and write each expense to the report
      while (rs.next()) {
        Expense expense = Expense.builder()
          .amount(rs.getDouble("amount"))
          .category(rs.getString("category"))
          .date(rs.getDate("date").toLocalDate())
          .description(rs.getString("description"))
          .build();
        writer.write(expense.toString());
      }

      // Show success alert to the user
      showAlert("Success", "Expense report generated successfully!");
      out.println("********************* Report generated successfully *********************");
    } catch (SQLException | IOException e) {
      showAlert("Error", "Failed to generate the report.");
      out.println("********************* Report generated successfully *********************");
      e.printStackTrace();
    }
  }

  /**
   * These controllers handles every action buttons to connect to the db
   * the handleAddExpense, handleViewSummary, handleSearch and handleGenerateReport
   * Note: This above controller method are not view controllers
   */

  // Handles the "Add New Expense" button click
  @FXML
  public void handleAddExpenseView(ActionEvent actionEvent) {
    try {
      // Load the AddExpenseView.fxml file
      Parent root =
        FXMLLoader.load(Objects.requireNonNull(getClass().getResource(APP_DIR.concat(getInstance().getProperty("views.AddExpenseView")))));
      Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
      stage.setTitle("Add new Expense");
      stage.setScene(new Scene(root));
      stage.setMinWidth(650);
      stage.setMinHeight(600);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace(); // Handle any exceptions related to loading the FXML file
    }
  }

  // Handles the "Search & Filter Expenses view"
  @FXML
  public void handleSearchView(ActionEvent actionEvent) {
    try {
      // Load the SearchExpenseView.fxml file
      Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

      Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(APP_DIR.concat(getInstance().getProperty(
        "views.SearchExpenseView")))));

      stage.setTitle("Search for Expense");
      stage.setScene(new Scene(root));
      stage.show();

    } catch (IOException e) {
      e.printStackTrace(); // Handle any exceptions related to loading the FXML file
    }
  }

  @FXML
  public void handleBackToMainView(ActionEvent event) throws IOException {
    ExpenseTrackerApplication expenseTrackerApplication = new ExpenseTrackerApplication();
    try {
      // Load the MainView.fxml file
//      FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/expensetrancker/view/MainView.fxml"));
//      Parent mainView = loader.load();

      // Get the current stage (window) and set the scene to MainView
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//      stage.setScene(new Scene(mainView));
//      stage.show();
      expenseTrackerApplication.start(stage);
    } catch (IOException e) {
      e.printStackTrace(); // Handle any exceptions related to loading the FXML file
    }
  }

  // Method to populate expense summary
  private void populateExpenseSummary(ExpenseTrackerController controller, String sql) {
    try (Connection conn = DatabaseConfig.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
      ResultSet rs = pstmt.executeQuery();
      ObservableList<Expense> expenseList = FXCollections.observableArrayList();

      while (rs.next()) {
        String category = rs.getString("category");
        double total = rs.getDouble("total");

        Expense expenseSummary = new Expense(total, category, LocalDate.now(), "");
        expenseList.add(expenseSummary);
      }

      // Populate the ListView
      controller.summaryListView.setItems(expenseList);
      controller.summaryListView.setCellFactory(param -> new ListCell<>() {
        @Override
        protected void updateItem(Expense expense, boolean empty) {
          super.updateItem(expense, empty);
          if (empty || expense == null) {
            setText(null);
          } else {
            setText("Category: " + expense.getCategory() + ", Total: $" + expense.getAmount());
          }
        }
      });

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}

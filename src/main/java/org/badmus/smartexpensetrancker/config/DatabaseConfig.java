package org.badmus.smartexpensetrancker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;
import static org.badmus.smartexpensetrancker.config.AppConfig.getInstance;

public class DatabaseConfig {

  public static Connection connect() {
    try {
      String url = getInstance().getProperty("db.url");
      String username = getInstance().getProperty("db.username");
      String password = getInstance().getProperty("db.password");

      // Return a connection to the db
      return getConnection(url, username, password);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void createTable() {
    String sql = getInstance().getProperty("sql.query.create_expense_table");
    try (Connection conn = connect()) {
      assert conn != null;
      conn.createStatement().execute(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}

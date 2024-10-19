package org.badmus.smartexpensetrancker.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
  public static final String APP_DIR="/com/expensetrancker/view/";
  private static final String PROPERTIES_FILE = "src/main/resources/config.properties";

  private static AppConfig instance;
  private final Properties properties;

  // Private constructor to restrict instantiation
  private AppConfig() {
    properties = new Properties();
    loadProperties();
  }

  // Public method to provide access to the singleton instance
  public static AppConfig getInstance() {
    if (instance == null) {
      instance = new AppConfig();
    }
    return instance;
  }

  // Method to load properties from a file
  private void loadProperties() {
    try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
      properties.load(fis);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Method to retrieve a property value by key
  public String getProperty(String key) {
    return properties.getProperty(key);
  }
}

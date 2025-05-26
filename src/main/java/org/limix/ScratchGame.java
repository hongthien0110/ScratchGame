package org.limix;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption("c", "config", true, "Configuration file path");
    options.addOption("b", "betting-amount", true, "Betting amount");

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      String configPath = cmd.getOptionValue("config", "config.json");
      int bettingAmount = Integer.parseInt(cmd.getOptionValue("betting-amount", "0"));

      // Load configuration
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> config = objectMapper.readValue(
          new File(configPath),
          new TypeReference<Map<String, Object>>() {}
      );

      System.out.println("Configuration loaded: " + config);
      System.out.println("Starting application with betting amount: " + bettingAmount);

    } catch (Exception e) {
      System.err.println("Error parsing command line: " + e.getMessage());
    }
  }
}
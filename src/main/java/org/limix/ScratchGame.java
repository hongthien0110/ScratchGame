package org.limix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.limix.entities.GameConfig;
import org.limix.entities.Symbols;
import org.limix.symboltypes.StandardSymbols;

public class ScratchGame {

  private final GameConfig config;
  private final double bettingAmount;
  private final String[][] matrix;
  private final Random random = new Random();
  private final Map<String, List<String>> appliedWinCombinations = new HashMap<>();
  //  private final List<String> appliedBonusSymbols = new ArrayList<>();
  private String appliedBonusSymbol = "";

  public ScratchGame(GameConfig config, double bettingAmount) {
    this.config = config;
    this.bettingAmount = bettingAmount;
    this.matrix = new String[config.getRows()][config.getColumns()];
  }

  // create a position record
  record Position(int row, int column) {

  }

  public void run() throws JsonProcessingException {
    generateMatrix();
    applyWinningCombinations();

    // print the result output
    String output = extractOutput();
    System.out.println(output);
  }

  public void generateMatrix() {
    // generate standard symbols
    Map<Position, Map<String, Integer>> positionSymbolMap = config.getProbabilities()
        .getStandardSymbols()
        .stream()
        .collect(Collectors.toMap(
            s -> new Position(s.getRow(), s.getColumn()),
            StandardSymbols::getSymbols
        ));

    int row = matrix.length;
    int column = matrix[0].length;

    Position bonusPosition = new Position(random.nextInt(row), random.nextInt(column));
    IntStream.range(0, row).forEach(r ->
        IntStream.range(0, column).forEach(c -> {
          Position pos = new Position(r, c);
          if (pos.equals(bonusPosition) && config.getProbabilities().getBonusSymbols() != null
              && !config.getProbabilities().getBonusSymbols().getSymbols().isEmpty()) {
            String bonusSymbol = selectSymbol(
                config.getProbabilities().getBonusSymbols().getSymbols());
            appliedBonusSymbol = bonusSymbol;
            matrix[r][c] = bonusSymbol;
          } else {
            Map<String, Integer> symbolProbabilities = positionSymbolMap.getOrDefault(pos,
                positionSymbolMap.get(new Position(0, 0)));
            matrix[r][c] = selectSymbol(symbolProbabilities);
          }
        })
    );
  }

  private String selectSymbol(Map<String, Integer> symbolProbabilities) {
    if (symbolProbabilities == null || symbolProbabilities.isEmpty()) {
      return null;
    }

    int totalWeight = symbolProbabilities.values().stream()
        .mapToInt(Integer::intValue)
        .sum();

    if (totalWeight <= 0) {
      throw new IllegalArgumentException("Total weight must be positive.");
    }

    double value = Math.random() * totalWeight;
    int weight = 0;

    for (Entry<String, Integer> entry : symbolProbabilities.entrySet()) {
      weight += entry.getValue();
      if (value <= weight) {
        return entry.getKey();
      }
    }

    return null; // must be never happen
  }

  public void applyWinningCombinations() {
    // process winning combinations for same_symbols
    applyWinningStandardSymbols();

    // process winning combinations for linear_symbols
    applyWinningLinearSymbols();
  }

  public void applyWinningStandardSymbols() {
    // count for standard symbols
    Map<String, Long> symbolCounts = Arrays.stream(matrix)
        .flatMap(Arrays::stream)
        .filter(Objects::nonNull)
        .filter(symbol -> "standard".equals(config.getSymbols().get(symbol).getType()))
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.counting()
        ));

    Map<String, Integer> sameSymbols = config.getWinCombinations().entrySet().stream()
        .filter(Objects::nonNull)
        .filter(entry -> "same_symbols".equals(entry.getValue().getWhen()))
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> entry.getValue().getCount()
        ));

    symbolCounts.forEach((key, value) ->
        sameSymbols.entrySet().stream()
            .filter(entry -> value >= entry.getValue())
            .max(Entry.comparingByValue())
            .ifPresent(entry ->
                appliedWinCombinations.computeIfAbsent(
                    key,
                    k -> new ArrayList<>()
                ).add(entry.getKey())
            ));
  }

  public void applyWinningLinearSymbols() {
    Map<String, Map<List<Position>, String>> linearSymbols =
        config.getWinCombinations().entrySet().stream()
            .filter(Objects::nonNull)
            .filter(entry -> "linear_symbols".equals(entry.getValue().getWhen()))
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> entry.getValue().getCoveredAreas().stream()
                    .map(areas -> areas.stream()
                        .map(pos -> {
                          String[] parts = pos.split(":");
                          int r = Integer.parseInt(parts[0]);
                          int c = Integer.parseInt(parts[1]);
                          return new Position(r, c);
                        })
                        .collect(Collectors.toList())
                    )
                    .filter(positions -> {
                      Position firstPos = positions.getFirst();
                      String symbol = matrix[firstPos.row()][firstPos.column()];
                      return symbol != null &&
                          "standard".equals(config.getSymbols().get(symbol).getType())
                          && positions.stream()
                          .allMatch(pos -> symbol.equals(matrix[pos.row()][pos.column()]));
                    })
                    .collect(Collectors.toMap(
                        positions -> positions,
                        positions -> {
                          Position firstPos = positions.getFirst();
                          return matrix[firstPos.row()][firstPos.column()];
                        }
                    ))
            ));

    linearSymbols.forEach((combinationName, coveredAreas) ->
        coveredAreas.forEach((pos, symbol) -> appliedWinCombinations
            .computeIfAbsent(symbol, k -> new ArrayList<>())
            .add(combinationName))
    );
  }

  public double calculateReward() {
    if (appliedWinCombinations.isEmpty()) {
      return 0;
    }

    double reward = appliedWinCombinations.entrySet().stream()
        .mapToDouble(entry -> {
          String symbol = entry.getKey();
          double symbolReward =
              bettingAmount * config.getSymbols().get(symbol).getRewardMultiplier();
          return entry.getValue().stream()
              .map(winCombinations -> config.getWinCombinations().get(winCombinations)
                  .getRewardMultiplier())
              .reduce(symbolReward,
                  (current, winCombinationsReward) -> current * winCombinationsReward);
        }).sum();

    // apply bonus symbols
//    reward = Arrays.stream(matrix)
//        .flatMap(Arrays::stream)
//        .filter(Objects::nonNull)
//        .filter(symbol -> "bonus".equals(config.getSymbols().get(symbol).getType()))
//        .peek(appliedBonusSymbols::add)
//        .map(symbol -> config.getSymbols().get(symbol))
//        .reduce(
//            reward,
//            (currentReward, symbolConfig) -> "multiply_reward".equals(symbolConfig.getImpact())
//                ? currentReward * symbolConfig.getRewardMultiplier()
//                : "extra_bonus".equals(symbolConfig.getImpact())
//                    ? currentReward + symbolConfig.getExtra()
//                    : currentReward
//            ,
//            (a, b) -> a
//        );

    // apply bonus symbols
    if (appliedBonusSymbol != null && !appliedBonusSymbol.isEmpty()) {
      Symbols symbolConfig = config.getSymbols().get(appliedBonusSymbol);
      if (symbolConfig != null) {
        switch (symbolConfig.getImpact()) {
          case "multiply_reward" -> reward *= symbolConfig.getRewardMultiplier();
          case "extra_bonus" -> reward += symbolConfig.getExtra();
        }
      }
    }

    return reward;
  }

  public String extractOutput() throws JsonProcessingException {
    double reward = calculateReward();

    Map<String, Object> resultMap = new LinkedHashMap<>();
    resultMap.put("matrix", matrix);
    resultMap.put("applied_winning_combinations", appliedWinCombinations);
    resultMap.put("applied_bonus_symbol", appliedBonusSymbol);
    resultMap.put("reward", reward);

    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resultMap);
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption("c", "config", true, "Configuration file path");
    options.addOption("b", "betting-amount", true, "Betting amount");

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      String configPath = cmd.getOptionValue("config", "config.json");
      int bettingAmount = Integer.parseInt(cmd.getOptionValue("betting-amount", "100"));

      // load config
      ObjectMapper objectMapper = new ObjectMapper();
      GameConfig config = objectMapper.readValue(new File(configPath), GameConfig.class);

      // run the game
      ScratchGame scratchGame = new ScratchGame(config, bettingAmount);
      scratchGame.run();

    } catch (Exception e) {
      System.err.println("Error parsing command line: " + e.getMessage());
    }
  }
}
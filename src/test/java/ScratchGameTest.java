import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.limix.ScratchGame;
import org.limix.entities.GameConfig;
import org.limix.entities.Probabilities;
import org.limix.entities.Symbols;
import org.limix.entities.WinCombinations;
import org.limix.symboltypes.BonusSymbols;
import org.limix.symboltypes.StandardSymbols;

class ScratchGameTest {

  private ScratchGame scratchGame;
  private GameConfig config;
  private final int bettingAmount = 100;

  @BeforeEach
  void setUp() {
    config = createValidConfig();
    scratchGame = new ScratchGame(config, bettingAmount);
  }

  private GameConfig createValidConfig() {
    GameConfig config = new GameConfig();
    config.setRows(3);
    config.setColumns(3);

    // initialize the standard symbols without any repeated symbols
    List<StandardSymbols> standardSymbols = List.of(
        new StandardSymbols(0, 0, Map.of("A", 1)),
        new StandardSymbols(0, 1, Map.of("B", 1)),
        new StandardSymbols(0, 2, Map.of("C", 1)),
        new StandardSymbols(1, 0, Map.of("D", 1)),
        new StandardSymbols(1, 1, Map.of("E", 1)),
        new StandardSymbols(1, 2, Map.of("F", 1)),
        new StandardSymbols(2, 0, Map.of("A", 1)),
        new StandardSymbols(2, 1, Map.of("B", 1)),
        new StandardSymbols(2, 2, Map.of("C", 1))
    );

    BonusSymbols bonusSymbols = new BonusSymbols(Map.of("MISS", 1));

    Probabilities probabilities = new Probabilities();
    probabilities.setStandardSymbols(standardSymbols);
    probabilities.setBonusSymbols(bonusSymbols);
    config.setProbabilities(probabilities);

    config.setSymbols(Map.of(
        "A", new Symbols(5, "standard", 0, null),
        "B", new Symbols(3, "standard", 0, null),
        "C", new Symbols(2.5, "standard", 0, null),
        "D", new Symbols(2, "standard", 0, null),
        "E", new Symbols(1.2, "standard", 0, null),
        "F", new Symbols(1, "standard", 0, null),
        "10x", new Symbols(10, "bonus", 0, "multiply_reward"),
        "+1000", new Symbols(0, "bonus", 1000, "extra_bonus"),
        "MISS", new Symbols(0, "bonus", 0, "miss")
    ));

    List<List<String>> coveredAreas = new ArrayList<>();
    coveredAreas.add(List.of("0:0", "0:1", "0:2"));
    coveredAreas.add(List.of("1:0", "1:1", "1:2"));
    coveredAreas.add(List.of("2:0", "2:1", "2:2"));

    config.setWinCombinations(Map.of(
        "same_symbol_3",
          new WinCombinations(1, "same_symbols", 3, "same_symbols", null),
        "same_symbol_4",
          new WinCombinations(1.5, "same_symbols", 4, "same_symbols", null),
        "same_symbol_5",
          new WinCombinations(2, "same_symbols", 5, "same_symbols", null),
        "same_symbols_horizontally",
          new WinCombinations(2, "linear_symbols", 0, "horizontally_linear_symbols", coveredAreas)
    ));

    return config;
  }

  @Test
  @DisplayName("Should generate valid JSON output")
  void shouldGenerateValidOutput() throws JsonProcessingException {
    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    String output = scratchGame.extractOutput();

    assertAll(
        () -> assertTrue(output.startsWith("{")),
        () -> assertTrue(output.endsWith("}")),
        () -> assertTrue(output.contains("\"matrix\"")),
        () -> assertTrue(output.contains("\"reward\"")),
        () -> assertFalse(output.contains("\"null\""))
    );
  }

  @Test
  @DisplayName("Should calculate correct reward for standard symbols (same_symbol_3)")
  void shouldCalculateStandardReward_same_symbol_3() {

    // the symbol A is repeated 3 times to create same_symbol_3
    List<StandardSymbols> standardSymbols = List.of(
        new StandardSymbols(0, 0, Map.of("A", 1)),
        new StandardSymbols(0, 1, Map.of("B", 1)),
        new StandardSymbols(0, 2, Map.of("C", 1)),
        new StandardSymbols(1, 0, Map.of("D", 1)),
        new StandardSymbols(1, 1, Map.of("E", 1)),
        new StandardSymbols(1, 2, Map.of("F", 1)),
        new StandardSymbols(2, 0, Map.of("A", 1)),
        new StandardSymbols(2, 1, Map.of("A", 1)),
        new StandardSymbols(2, 2, Map.of("B", 1))
    );
    config.getProbabilities().setStandardSymbols(standardSymbols);

    // no bonus symbols
    config.getProbabilities().getBonusSymbols().setSymbols(new HashMap<>());

    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    double reward = scratchGame.calculateReward();

    // expectation:
    // reward = betting amount * reward of A * same_symbol_3 (ignore as 1)
    assertEquals(100 * 5, reward);
  }

  @Test
  @DisplayName("Should calculate correct reward for standard symbols (same_symbol_4)")
  void shouldCalculateStandardReward_same_symbol_4() {

    // the symbol B is repeated 4 times to create same_symbol_4
    List<StandardSymbols> standardSymbols = List.of(
        new StandardSymbols(0, 0, Map.of("B", 1)),
        new StandardSymbols(0, 1, Map.of("B", 1)),
        new StandardSymbols(0, 2, Map.of("C", 1)),
        new StandardSymbols(1, 0, Map.of("D", 1)),
        new StandardSymbols(1, 1, Map.of("E", 1)),
        new StandardSymbols(1, 2, Map.of("F", 1)),
        new StandardSymbols(2, 0, Map.of("B", 1)),
        new StandardSymbols(2, 1, Map.of("B", 1)),
        new StandardSymbols(2, 2, Map.of("C", 1))
    );
    config.getProbabilities().setStandardSymbols(standardSymbols);

    // no bonus symbols
    config.getProbabilities().getBonusSymbols().setSymbols(new HashMap<>());

    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    double reward = scratchGame.calculateReward();

    // expectation:
    // reward = betting amount * reward of B * same_symbol_4
    assertEquals(100 * 3 * 1.5, reward);
  }

  @Test
  @DisplayName("Should calculate correct reward for standard symbols (same_symbols_horizontally)")
  void shouldCalculateStandardReward_same_symbols_horizontally() {

    // there is C symbol get same_symbols_horizontally
    List<StandardSymbols> standardSymbols = List.of(
        new StandardSymbols(0, 0, Map.of("C", 1)),
        new StandardSymbols(0, 1, Map.of("C", 1)),
        new StandardSymbols(0, 2, Map.of("C", 1)),
        new StandardSymbols(1, 0, Map.of("D", 1)),
        new StandardSymbols(1, 1, Map.of("E", 1)),
        new StandardSymbols(1, 2, Map.of("F", 1)),
        new StandardSymbols(2, 0, Map.of("A", 1)),
        new StandardSymbols(2, 1, Map.of("B", 1)),
        new StandardSymbols(2, 2, Map.of("B", 1))
    );
    config.getProbabilities().setStandardSymbols(standardSymbols);

    // no bonus symbols
    config.getProbabilities().getBonusSymbols().setSymbols(new HashMap<>());

    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    double reward = scratchGame.calculateReward();

    // expectation:
    // reward = betting amount * reward of C * same_symbols_horizontally
    assertEquals(100 * 2.5 * 2, reward);
  }

  @Test
  @DisplayName("Should calculate correct reward for standard symbols (same_symbols_horizontally)")
  void shouldCalculateStandardReward_win_combinations() {

    // there is C symbol get same_symbols_horizontally
    List<StandardSymbols> standardSymbols = List.of(
        new StandardSymbols(0, 0, Map.of("C", 1)),
        new StandardSymbols(0, 1, Map.of("C", 1)),
        new StandardSymbols(0, 2, Map.of("C", 1)),
        new StandardSymbols(1, 0, Map.of("A", 1)),
        new StandardSymbols(1, 1, Map.of("A", 1)),
        new StandardSymbols(1, 2, Map.of("F", 1)),
        new StandardSymbols(2, 0, Map.of("A", 1)),
        new StandardSymbols(2, 1, Map.of("B", 1)),
        new StandardSymbols(2, 2, Map.of("C", 1))
    );
    config.getProbabilities().setStandardSymbols(standardSymbols);

    // no bonus symbols
    config.getProbabilities().getBonusSymbols().setSymbols(new HashMap<>());

    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    double reward = scratchGame.calculateReward();

    // expectation:
    // reward = betting amount * reward of A * same_symbol_3 (ignore as 1)
    // + betting amount * reward of C * same_symbol_4 * same_symbols_horizontally
    assertEquals(100 * 5 + 100 * 2.5 * 1.5 * 2, reward);
  }

  @Test
  @DisplayName("Should handle bonus symbols if it appears")
  void shouldHandleBonusSymbols() {
    config.getProbabilities().getBonusSymbols().setSymbols(Map.of("10x", 1));

    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    double reward = scratchGame.calculateReward();
    assertEquals(0, reward);

    // the symbol B is repeated 4 times to create same_symbol_4
    List<StandardSymbols> standardSymbols = List.of(
        new StandardSymbols(0, 0, Map.of("B", 1)),
        new StandardSymbols(0, 1, Map.of("B", 1)),
        new StandardSymbols(0, 2, Map.of("C", 1)),
        new StandardSymbols(1, 0, Map.of("D", 1)),
        new StandardSymbols(1, 1, Map.of("E", 1)),
        new StandardSymbols(1, 2, Map.of("F", 1)),
        new StandardSymbols(2, 0, Map.of("B", 1)),
        new StandardSymbols(2, 1, Map.of("B", 1)),
        new StandardSymbols(2, 2, Map.of("C", 1))
    );
    config.getProbabilities().setStandardSymbols(standardSymbols);

    scratchGame.generateMatrix();
    scratchGame.applyWinningCombinations();
    double reward2 = scratchGame.calculateReward();

    // expectation:
    // reward = betting amount * reward of B * ( same_symbol_3 || same_symbol_4) * bonus
    // note: the bonus symbol may randomly override the symbol reward (B), or not.
    assertTrue(reward2 == 100 * 3 * 10 || reward2 == 100 * 3 * 1.5 * 10);
  }
}
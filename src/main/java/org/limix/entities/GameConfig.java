package org.limix;

import java.util.Map;

public class GameConfig {
  private int columns;
  private int rows;
  private Map<String, Symbols> symbols;
  private Probabilities probabilities;
  private Map<String, WinCombinations> winCombinations;
}

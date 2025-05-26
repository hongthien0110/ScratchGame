package org.limix.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameConfig {
  private int rows;
  private int columns;
  private Map<String, Symbols> symbols;
  private Probabilities probabilities;

  @JsonProperty("win_combinations")
  private Map<String, WinCombinations> winCombinations;
}

package org.limix.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class WinCombinations {

  @JsonProperty("reward_multiplier")
  private double rewardMultiplier;

  private String when;
  private int count;
  private String group;

  @JsonProperty("covered_areas")
  private List<List<String>> coveredAreas;
}

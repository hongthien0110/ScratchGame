package org.limix.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.limix.symboltypes.BonusSymbols;
import org.limix.symboltypes.StandardSymbols;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Probabilities {

  @JsonProperty("standard_symbols")
  private List<StandardSymbols> standardSymbols;

  @JsonProperty("bonus_symbols")
  private BonusSymbols bonusSymbols;

}

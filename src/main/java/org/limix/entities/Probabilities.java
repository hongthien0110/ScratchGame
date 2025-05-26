package org.limix.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.limix.symboltypes.BonusSymbol;
import org.limix.symboltypes.StandardSymbol;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Probability {

  @JsonProperty("standard_symbols")
  private List<StandardSymbol> standardSymbols;

  @JsonProperty("bonus_symbols")
  private BonusSymbol bonusSymbol;

}

package xyz.zlatanov.frakkintoasters.setup;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;

@Data
@Accessors(fluent = true)
public class GameSetupDriver {
    private ObjectiveCard        objective;
    private int                  playerCount;
    private FirstPlayerSelection firstPlayerSelection;
}

package xyz.zlatanov.frakkintoasters.state;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.view.TurnView;

import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;

@Getter
@Accessors(fluent = true)
public class Turn implements TurnView {
    @Setter(onMethod_ = @Mutates)
    private int      number              = 0;
    @Setter(onMethod_ = @Mutates)
    private GameStep step                = SETUP;
    @Setter(onMethod_ = @Mutates)
    private int      currentPlayerNumber = 1;
    @Getter(AccessLevel.NONE)
    private Integer  actingPlayerNumber; // null means the current player is acting

    @Override
    public int actingPlayerNumber() {
        return actingPlayerNumber != null ? actingPlayerNumber : currentPlayerNumber;
    }

    /**
     * @param actingPlayerNumber the player acting out of turn, or {@code null} when the current player is acting
     */
    @Mutates
    public Turn actingPlayerNumber(Integer actingPlayerNumber) {
        this.actingPlayerNumber = actingPlayerNumber;
        return this;
    }
}

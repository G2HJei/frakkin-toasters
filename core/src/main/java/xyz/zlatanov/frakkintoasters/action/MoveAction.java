package xyz.zlatanov.frakkintoasters.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

@SuperBuilder
@Getter
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MoveAction extends PlayerAction {

    private final Location location;

    @Override
    public List<Action> apply(Game game) {
        return List.of();
    }
}

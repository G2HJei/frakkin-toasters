package xyz.zlatanov.frakkintoasters.action;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import xyz.zlatanov.frakkintoasters.Game;

import java.util.List;

@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NoopAction extends PlayerAction {

    @Override
    public List<Action> apply(Game game) {
        return List.of();
    }
}

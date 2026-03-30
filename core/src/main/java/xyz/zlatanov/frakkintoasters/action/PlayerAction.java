package xyz.zlatanov.frakkintoasters.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public abstract class PlayerAction implements Action {

    protected final int playerNumber;

}

package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.character.Character;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class AssaultRaptor implements HumanFighter {

    private final int       id;
    private       Character pilot;
}

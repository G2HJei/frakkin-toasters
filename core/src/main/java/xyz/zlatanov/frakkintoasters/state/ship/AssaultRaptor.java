package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.character.Character;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class AssaultRaptor implements HumanFighter {

    private final int       id;
    @Setter(onMethod_ = @Mutates) //todo write the setter, remove lombok annotations ?
    private       Character pilot;
}

package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER_MARK_VII;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class ViperMarkVII extends Viper {
    private final ShipType type = VIPER_MARK_VII;

}

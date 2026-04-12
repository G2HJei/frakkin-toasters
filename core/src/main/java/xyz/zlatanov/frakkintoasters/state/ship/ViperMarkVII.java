package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER_MARK_VII;

@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class ViperMarkVII extends Viper {

    public ViperMarkVII(int id) {
        super(id);
    }

    @Override
    public ShipType type() {
        return VIPER_MARK_VII;
    }
}

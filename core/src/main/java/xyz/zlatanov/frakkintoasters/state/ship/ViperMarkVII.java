package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class ViperMarkVII extends Viper {

    public ViperMarkVII(int id) {
        super(id);
    }
}

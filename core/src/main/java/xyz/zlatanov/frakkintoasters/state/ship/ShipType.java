package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ShipType {
    ASSAULT_RAPTOR(AssaultRaptor.class),
    BASESTAR(Basestar.class),
    HEAVY_RAIDER(HeavyRaider.class),
    RAIDER(Raider.class),
    RAPTOR(Raptor.class),
    VIPER(Viper.class),
    VIPER_MARK_VII(ViperMarkVII.class),
    CIVILIAN(CivilianShip.class);

    private final Class<? extends Ship> shipClass;

    public static ShipType of(Class<? extends Ship> clazz) {
        return Arrays.stream(values())
                .filter(type -> type.shipClass.equals(clazz))
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }
}

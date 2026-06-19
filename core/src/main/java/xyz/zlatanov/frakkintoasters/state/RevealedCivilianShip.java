package xyz.zlatanov.frakkintoasters.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class RevealedCivilianShip {
    private int civilianShipId;
    /**
     * Remaining events the ship is revealed for
     */
    private int duration;
}

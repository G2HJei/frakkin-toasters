package xyz.zlatanov.frakkintoasters.state.card;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The Destination deck, with the distance printed at the bottom of each card (rule 12.3). The effects are in
 * {@code DestinationCardFollowups}, the images in {@code materials/images}. The "Mining Asteroid" card is left out: it
 * searches the Crisis deck for the "Scar" card, which the Crisis deck doesn't have.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum DestinationCard {
    // Galactica
    ASTEROID_FIELD(3),
    BARREN_PLANET(2),
    CYLON_AMBUSH(3),
    CYLON_REFINERY(2),
    DEEP_SPACE(2),
    DESOLATE_MOON(3),
    ICY_MOON(1),
    RAGNAR_ANCHORAGE(1),
    REMOTE_PLANET(2),
    TYLIUM_PLANET(1),
    // Exodus
    ALGAE_PLANET(1),
    CYLON_RAIDERS(3),
    DERELICT_BASESTAR(2),
    DYING_STAR(2),
    GAS_GIANT(1),
    LIONS_HEAD_NEBULA(3),
    RADIOACTIVE_CLOUD(2),
    // Pegasus
    A_CIVILIAN_CONVOY(3),
    BINARY_STAR(2),
    GAS_CLOUD(1),
    MISJUMP(0); // has no distance: the card is discarded and a new one drawn

    private final int distance;
}

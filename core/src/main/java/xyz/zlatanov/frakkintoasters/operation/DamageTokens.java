package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class DamageTokens {

    private DamageTokens() {
    }

    // The damage token that names the location, if one does: not every location has one, e.g. "Communications".
    static Optional<Object> of(Location location) {
        return Stream.concat(Arrays.stream(GalacticaDamage.values()), Arrays.stream(PegasusDamage.values()))
                .<Object>map(token -> token)
                .filter(token -> ((Enum<?>) token).name().equals(location.name()))
                .findFirst();
    }

    static DeckId deckOf(Object token) {
        return switch (token) {
            case GalacticaDamage ignored -> DeckId.GALACTICA_DAMAGE;
            case PegasusDamage ignored -> DeckId.PEGASUS_DAMAGE;
            default -> throw new FrakCallTheAdmiralException("Not a damage token: " + token);
        };
    }
}

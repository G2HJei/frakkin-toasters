package xyz.zlatanov.frakkintoasters.event.character;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.HAND;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.RANDOM;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DECEITFUL;

/**
 * The discards of one Crisis Card's effects, for "Deceitful": the first one of Romo Lampkin is 1 card smaller. A builder
 * of a card's effects asks it for every discard. It is built once per card, so the reduction is once per card. The
 * discards are forced, so "For Love" may make them smaller too ({@link ForcedDiscards}).
 */
@RequiredArgsConstructor
public class DeceitfulDiscards {

    private final GameView game;
    private final boolean  applies;
    private       boolean  used;

    /**
     * @param applies whether the discards come from a Crisis Card, not a location or a Super Crisis Card
     */
    public static DeceitfulDiscards of(GameView game, boolean applies) {
        return new DeceitfulDiscards(game, applies);
    }

    public Event discard(int playerNumber, int count) {
        return discard(playerNumber, CHOSEN, count);
    }

    public Event discardRandom(int playerNumber, int count) {
        return discard(playerNumber, RANDOM, count);
    }

    public Event discardAll(int playerNumber) {
        return discard(playerNumber, HAND, 0);
    }

    private Event discard(int playerNumber, DiscardKind kind, int count) {
        if (reduces(playerNumber)) {
            return new DeceitfulEvent(playerNumber, kind, count);
        }
        // A Super Crisis Card's check is immune to the character abilities (rule 6.1.2.5), "For Love" included.
        val superCrisis = game.activeSkillCheck() != null && game.activeSkillCheck().type().isSuperCrisis();
        return superCrisis
                ? ForcedDiscards.plain(playerNumber, kind, count)
                : ForcedDiscards.discard(game, playerNumber, kind, count);
    }

    private boolean reduces(int playerNumber) {
        if (!applies || used || !game.player(playerNumber).hasAbility(DECEITFUL)) {
            return false;
        }
        used = true;
        return true;
    }
}

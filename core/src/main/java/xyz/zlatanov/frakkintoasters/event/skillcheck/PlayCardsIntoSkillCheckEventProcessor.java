package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities;
import xyz.zlatanov.frakkintoasters.event.character.LogicBoundEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PROBATION;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;

public class PlayCardsIntoSkillCheckEventProcessor extends EventProcessor<PlayCardsIntoSkillCheckEvent> {

    private static final int UNLIMITED = Integer.MAX_VALUE;

    /**
     * @return how many cards the player may put into the active skill check: 1 in the "Brig" or as a Cylon, 2 when
     * Infiltrating (rules 6.1.2.3 and 6.2.3.2), and what his character's abilities change, none for a Super Crisis
     */
    public static int maxCards(GameView game, PlayerView player) {
        val inBrig = game.locate(player.character()).filter(BRIG::equals).isPresent();
        var max = inBrig || !player.isHuman() ? 1
                : player.isInfiltrating() ? 2
                : UNLIMITED;
        if (!CharacterAbilities.canAffectSkillCheck(game)) {
            return max;
        }
        // "Vanity": nothing during another player's Action step.
        if (player.hasAbility(VANITY)
                && game.turn().step() == ACTION
                && game.turn().currentPlayerNumber() != player.number()) {
            return 0;
        }
        // "Calculating": 2, or 3 Infiltrating, except in the "Brig".
        if (player.hasAbility(CALCULATING) && !inBrig) {
            max = player.isInfiltrating() ? 3 : 2;
        }
        // "Misguided": never more than 3.
        if (player.hasAbility(MISGUIDED)) {
            max = Math.min(max, 3);
        }
        return max;
    }

    public static boolean canContribute(GameView game, PlayerView player, int count) {
        if (count == 0) {
            return true;
        }
        // "Impulsive": none or 2 and more, but in the "Brig" a single card is all she may play.
        val impulsive = CharacterAbilities.canAffectSkillCheck(game)
                && player.hasAbility(IMPULSIVE)
                && game.locate(player.character()).filter(BRIG::equals).isEmpty();
        return count <= maxCards(game, player) && !(impulsive && count == 1);
    }

    @Override
    protected boolean isValid() {
        return event.skillCards() != null
                && holdsSkillCards(event.skillCards())
                && canContribute(game, player, event.skillCards().size());
    }

    @Override
    public Followup process() {
        ops.skillCheck().playCards(player.number(), event.skillCards());
        if (game.activeSkillCheck().investigated() && !event.skillCards().isEmpty()) {
            // The cards go into the pile like any other, and everyone sees them (the Destiny cards are dealt face down).
            ops.skillCheck().show(player.number(), event.skillCards());
        }
        return allOf(logicBound(), probation());
    }

    // "Logic-Bound": one of the cards he plays goes in face up, his pick. Nothing to do when everyone sees them anyway.
    private Followup logicBound() {
        if (event.skillCards().isEmpty()
                || game.activeSkillCheck().investigated()
                || !CharacterAbilities.canAffectSkillCheck(game)
                || !player.hasAbility(LOGIC_BOUND)) {
            return Followup.NONE;
        }
        return one(event.skillCards().stream()
                .distinct()
                .map(card -> new LogicBoundEvent(player.number(), card))
                .toArray(Event[]::new));
    }

    // The President may then use "Probation" to look at the cards its holder put in (rule A2.3.13.2), unless he holds
    // it himself.
    private Followup probation() {
        if (event.skillCards().isEmpty()
                || !game.quorumCardHolder(PROBATION).equals(Optional.of(player.number()))
                || game.president() == null
                || game.president() == player.character()) {
            return Followup.NONE;
        }
        val president = game.playerNumber(game.president());
        return one(new UseProbationEvent(president, player.number(), event.skillCards().size()), new NoOpEvent(president));
    }
}

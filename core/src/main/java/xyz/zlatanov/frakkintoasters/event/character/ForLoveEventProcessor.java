package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FOR_LOVE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

public class ForLoveEventProcessor extends EventProcessor<ForLoveEvent> {

    /**
     * @return whether another player than the one who discards has "For Love", so the discard asks. Whether she can
     * use it is checked when it happens
     */
    public static boolean mayBeOffered(GameView game, int discarderPlayerNumber) {
        return game.players().stream()
                .anyMatch(other -> other.number() != discarderPlayerNumber && other.hasAbility(FOR_LOVE));
    }

    /**
     * @return the player who can use "For Love" on this discard now: another player, once a turn, with a Treachery card
     * to draw, on a discard that takes at least 1 card
     */
    public static Optional<PlayerView> athenaFor(GameView game, int discarder, DiscardKind discard, int count) {
        if (ForcedDiscards.cardsTaken(game, discarder, discard, count) < 1 || game.decks().skillCardsLeft(TREACHERY) < 1) {
            return Optional.empty();
        }
        return game.players().stream()
                .filter(other -> other.number() != discarder)
                .filter(other -> CharacterAbilities.canUseOncePerTurn(game, other, FOR_LOVE))
                .map(PlayerView.class::cast)
                .findFirst();
    }

    @Override
    protected boolean isValid() {
        return isPlayer(event.discarderPlayerNumber()) && event.discard() != null
                && athenaFor(game, event.discarderPlayerNumber(), event.discard(), event.count())
                .filter(athena -> athena.number() == player.number())
                .isPresent();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(FOR_LOVE);
        ops.player(player.number()).drawSkillCards(TREACHERY);
        return ForcedDiscards.oneFewer(game, event.discarderPlayerNumber(), event.discard(), event.count());
    }
}

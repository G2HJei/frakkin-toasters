package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.ReligiousVisionsEvent;
import xyz.zlatanov.frakkintoasters.event.character.ReligiousVisionsEventProcessor;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardReveals;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class CrisisStepEventProcessor extends EventProcessor<CrisisStepEvent> {


    @Override
    public Followup process() {
        val current = game.currentPlayer();
        if (game.turn().endedEarly()
                || !current.isHuman()// An Infiltrating Cylon Leader counts as human, so has Crisis step(rule 6.2.3.4).
                // Revealed Cylons and characters in the "Brig" have no Crisis step (rules 8.5, 6.1.3).
                || game.locate(current.character()).equals(Optional.of(BRIG))) {
            return NONE;
        }
        ops.changeStep(CRISIS);
        val resolve = ReligiousVisionsEventProcessor.drawsTwo(game, current) ? religiousVisions(current) : draw(current);
        // A card that returned to this step has an Activate Cylon Ships step of its own, which the turn doesn't list.
        return event.again() ? all(resolve, single(new ActivateCylonShipsStepEvent())) : resolve;
    }

    private Followup draw(PlayerView current) {
        val card = ops.player(current.number()).drawCrisisCard();
        // The card stays with the turn for its icons, until EndTurnEvent discards it (rule 8.8).
        ops.playCrisisCard(card);
        return CrisisCardReveals.drawn(game, card, current.number());
    }

    // "Religious Visions": she draws 2, picks the one to resolve and puts the other at the bottom. Only she sees them.
    private Followup religiousVisions(PlayerView roslin) {
        val playerOps = ops.player(roslin.number());
        val first = playerOps.drawCrisisCard();
        val second = playerOps.drawCrisisCard();
        playerOps.see(DeckId.CRISIS, List.of(first, second));
        if (first == second) {
            return single(new ReligiousVisionsEvent(roslin.number(), first, second)); // two copies: nothing to choose
        }
        return one(
                new ReligiousVisionsEvent(roslin.number(), first, second),
                new ReligiousVisionsEvent(roslin.number(), second, first));
    }
}

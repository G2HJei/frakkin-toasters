package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEventProcessor.buildAssaultRaptor;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEventProcessor.canBuildAssaultRaptor;

public class WeaponsArmedEventProcessor extends MutinyCardEventProcessor<WeaponsArmedEvent> {

    // A raptor in the "Reserves" and an assault raptor left in the supply, as "Raptor Specialist" needs.
    public static boolean canPlay(GameView game) {
        return canBuildAssaultRaptor(game);
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.WEAPONS_ARMED;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    // With no basestar on the main game board nobody launches, and nothing goes to the Cylon Fleet board (rule 9.6).
    @Override
    protected Followup resolve() {
        buildAssaultRaptor(game, ops);
        return all(LaunchRaidersEvent.fromEachBasestar(2), thenDiscard());
    }
}

package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEvent.Choice;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RAPTOR_SPECIALIST;

public class RaptorSpecialistEventProcessor extends SkillCardEventProcessor<RaptorSpecialistEvent> {

    public static boolean canPlay(GameView game) {
        return canChoose(game, Choice.BUILD_ASSAULT_RAPTOR) || canChoose(game, Choice.RETURN_RAPTOR);
    }

    // The raptor destroyed comes from the "Reserves", so without one there no assault raptor is built (rule 11.3.2.1),
    // and there are 4 assault raptors at most (A2.4.4).
    private static boolean canChoose(GameView game, Choice choice) {
        if (choice == null) {
            return false;
        }
        val galactica = game.boards().galactica();
        return switch (choice) {
            case BUILD_ASSAULT_RAPTOR -> raptorInReserves(game).isPresent()
                    && galactica.supply().stream().anyMatch(AssaultRaptor.class::isInstance);
            case RETURN_RAPTOR -> destroyedRaptor(game).isPresent();
        };
    }

    @Override
    public SkillCardType type() {
        return RAPTOR_SPECIALIST;
    }

    @Override
    protected boolean isValidPlay() {
        return canChoose(game, event.choice());
    }

    @Override
    protected Followup resolve() {
        switch (event.choice()) {
            case BUILD_ASSAULT_RAPTOR -> {
                ops.ship(raptorInReserves(game).orElseThrow().id()).destroy();
                ops.galactica().buildAssaultRaptor();
            }
            case RETURN_RAPTOR -> ops.ship(destroyedRaptor(game).orElseThrow().id()).repair();
        }
        return NONE;
    }

    // Raptors are identical, so the one with the lowest id.
    private static Optional<Raptor> raptorInReserves(GameView game) {
        return game.boards().galactica().reserves().stream()
                .filter(Raptor.class::isInstance)
                .map(Raptor.class::cast)
                .findFirst();
    }

    private static Optional<Raptor> destroyedRaptor(GameView game) {
        return game.removedComponents().items().stream()
                .filter(Raptor.class::isInstance)
                .map(Raptor.class::cast)
                .min((a, b) -> Integer.compare(a.id(), b.id()));
    }
}

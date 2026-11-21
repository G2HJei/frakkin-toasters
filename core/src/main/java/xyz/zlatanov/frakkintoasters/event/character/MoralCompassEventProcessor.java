package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardFollowups;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MORAL_COMPASS;

public class MoralCompassEventProcessor extends EventProcessor<MoralCompassEvent> {

    /**
     * @return the player who may change the chooser's choice to the other section: he can use the ability, the section
     * can be chosen, and he isn't the chooser, who wouldn't change his own choice
     */
    public static Optional<Integer> changer(GameView game, int chooser, CrisisCard card, Section other) {
        if (!CrisisCardFollowups.canChoose(game, card, other)) {
            return Optional.empty();
        }
        return game.players().stream()
                .filter(player -> player.number() != chooser)
                .filter(player -> CharacterAbilities.canUseMiracle(game, player, MORAL_COMPASS))
                .map(PlayerView::number)
                .findFirst();
    }

    @Override
    protected boolean isValid() {
        val cards = game.turn().crisisCards();
        return CharacterAbilities.canUseMiracle(game, player, MORAL_COMPASS)
                && event.section() != null
                && !cards.isEmpty()
                && cards.getLast() == event.card()
                && CrisisCardFollowups.canChoose(game, event.card(), event.section());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(MORAL_COMPASS);
        return ChooseCrisisSectionEventProcessor.resolve(game, ops, event.card(), event.section());
    }
}

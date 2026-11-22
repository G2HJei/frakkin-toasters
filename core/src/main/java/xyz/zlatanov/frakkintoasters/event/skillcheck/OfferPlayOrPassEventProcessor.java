package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class OfferPlayOrPassEventProcessor extends EventProcessor<OfferPlayOrPassEvent> {

    @Override
    protected boolean isValid() {
        return game.activeSkillCheck() != null && event.card() != null && eventClass(event.card()) != null;
    }

    @Override
    public Followup process() {
        val pass = single(new NoOpEvent(player.number()));
        if (canPlay(game, player, event.card())) {
            return one(single(new PlayerDecisionEvent<>(player.number(), eventClass(event.card()))), pass);
        }
        // The pass alone still asks him, so nobody learns he has no card: one(...) would run it without asking.
        return new Followup.OneOf(Set.of(pass));
    }

    /**
     * @return whether the player holds a card of the type and the skill check allows its play now
     */
    public static boolean canPlay(GameView game, PlayerView player, SkillCardType card) {
        val holds = player.skillCards().cards().stream().anyMatch(c -> c.type() == card);
        return holds && switch (card) {
            case POLITICAL_PROWESS -> PoliticalProwessEventProcessor.canPlay(game);
            case CHANGE_OF_PLANS -> ChangeOfPlansEventProcessor.canPlay(game);
            case DECLARE_EMERGENCY -> DeclareEmergencyEventProcessor.canPlay(game);
            default -> false;
        };
    }

    private static Class<? extends PlayerEvent> eventClass(SkillCardType card) {
        return switch (card) {
            case POLITICAL_PROWESS -> PoliticalProwessEvent.class;
            case CHANGE_OF_PLANS -> ChangeOfPlansEvent.class;
            case DECLARE_EMERGENCY -> DeclareEmergencyEvent.class;
            default -> null;
        };
    }
}

package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

public class ResearchLabEventProcessor extends LocationActionProcessor<ResearchLabEvent> {

    private static final List<SkillCardColor> ALLOWED_SKILL_CARD_COLORS = List.of(ENGINEERING, TACTICS);

    public static boolean canAct(GameView game) {
        // A color with no cards left can't be drawn (rule 8.2.2.5).
        return ALLOWED_SKILL_CARD_COLORS.stream().anyMatch(color -> game.decks().skillCardsLeft(color) > 0);
    }

    @Override
    protected boolean isValidAction() {
        return event.skillCardColor() != null
                && ALLOWED_SKILL_CARD_COLORS.contains(event.skillCardColor())
                && game.decks().skillCardsLeft(event.skillCardColor()) > 0;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).drawSkillCards(event.skillCardColor());
        return Followup.NONE;
    }
}

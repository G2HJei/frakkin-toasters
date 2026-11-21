package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.SkillSetOption;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.drawExactly;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class ReceiveSkillsStepEventProcessor extends EventProcessor<ReceiveSkillsStepEvent> {

    private static final Set<Location> DRAW_ONE_CARD = Set.of(SICKBAY, RESURRECTION_SHIP); // rules 9.3, 9.5

    @Override
    public Followup process() {
        ops.changeStep(RECEIVE_SKILLS);
        val current = game.currentPlayer();
        val cards = cardsToDraw(current);
        return cards == 0
                ? NONE
                : single(new PlayerDecisionEvent<>(current.number(), DrawSkillCardsEvent.class, drawExactly(cards)));
    }

    private int cardsToDraw(PlayerView player) {
        val location = game.locate(player.character());
        if (player.hasAbility(STARTS_ON_THE_BENCH) && player.turns() == 1) {
            // Samuel T. Anders skips the step on his first turn, and on his player's next turn when he replaces an
            // executed character (rule A2.1.1.14): his turns count from his selection.
            return 0;
        }
        if (player.hasAbility(GRIEVING) && location.filter(Location::isHazardousLocation).isPresent()) {
            return 0; // Sharon "Athena" Agathon draws nothing in a hazardous location
        }
        if (location.filter(DRAW_ONE_CARD::contains).isPresent()) {
            return 1;
        }
        if (player.character().type() == CYLON_LEADER) {
            return skillSetSize(player) + infiltrationCards(player); // rule 8.2.2.4
        }
        if (player.isRevealedCylon()) {
            return 2; // rule 8.2.2.3
        }
        // Karl "Helo" Agathon (alt.)'s "Family Commitments": one fewer when he isn't on Galactica, e.g. piloting
        val awayFromGalactica = location.filter(GALACTICA.locations()::contains).isEmpty();
        return skillSetSize(player) - (player.hasAbility(FAMILY_COMMITMENTS) && awayFromGalactica ? 1 : 0);
    }

    // An Infiltrating Cylon Leader draws 1 more (rule 6.2.3.2), Aaron Doral 2 more ("Industrious"). Neither applies in
    // "Sickbay", where every player draws 1.
    private static int infiltrationCards(PlayerView player) {
        if (!player.isInfiltrating()) {
            return 0;
        }
        return player.hasAbility(INDUSTRIOUS) ? 2 : 1;
    }

    // The player always draws the full count, however many cards they hold (rule 8.2.2).
    private static int skillSetSize(PlayerView player) {
        return player.character().skillSet().stream().mapToInt(SkillSetOption::count).sum();
    }
}

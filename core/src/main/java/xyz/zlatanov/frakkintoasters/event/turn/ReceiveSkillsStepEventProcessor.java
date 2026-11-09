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

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.drawExactly;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class ReceiveSkillsStepEventProcessor extends EventProcessor<ReceiveSkillsStepEvent> {

    private static final Set<Location> DRAW_ONE_CARD = Set.of(SICKBAY, RESURRECTION_SHIP); // rules 9.3, 9.5

    @Override
    public Followup process() {
        ops.changeStep(RECEIVE_SKILLS);
        val current = game.currentPlayer();
        return single(new PlayerDecisionEvent<>(current.number(), DrawSkillCardsEvent.class,
                drawExactly(cardsToDraw(current))));
    }

    // todo character abilities that change the count, e.g. "Family Commitments" and "Grieving"
    private int cardsToDraw(PlayerView player) {
        if (game.locate(player.character()).filter(DRAW_ONE_CARD::contains).isPresent()) {
            return 1;
        }
        if (player.character().type() == CYLON_LEADER) {
            return skillSetSize(player) + (player.isInfiltrating() ? 1 : 0); // rule 8.2.2.4
        }
        return player.isRevealedCylon() ? 2 : skillSetSize(player); // rule 8.2.2.3
    }

    // The player always draws the full count, however many cards they hold (rule 8.2.2).
    private static int skillSetSize(PlayerView player) {
        return player.character().skillSet().stream().mapToInt(SkillSetOption::count).sum();
    }
}

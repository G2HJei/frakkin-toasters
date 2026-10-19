package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

public class TiebreakCardsBeforeSkillCheckEventProcessor extends EventProcessor<TiebreakCardsBeforeSkillCheckEvent> {


    @Override
    public Followup process() {
        event.tiebreakWinners().forEach(this::breakTie);
        return Followup.NONE;
    }

    private void breakTie(SkillCardType type, Integer playerNumber) {
        for (int otherPlayerNumber = 1; otherPlayerNumber <= game.players().size(); otherPlayerNumber++) {
            if (otherPlayerNumber == playerNumber) {
                continue;
            }
            val cardToReturn = game.activeSkillCheck()
                    .playedBeforeSkillCheck(otherPlayerNumber)
                    .stream()
                    .filter(c -> c.type() == type)
                    .findFirst();
            if (cardToReturn.isPresent()) {
                ops.skillCheck().giveCardToPlayer(cardToReturn.get(), otherPlayerNumber);
            }
        }
    }
}

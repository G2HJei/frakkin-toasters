package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class DrawSkillCardsOfColorEventProcessorTest extends EventTestHarness<DrawSkillCardsOfColorEvent> {

    @Test
    void shouldDrawCardsOfColor() {
        val bait = new SkillCard(1, BAIT);
        val dradisContact = new SkillCard(2, DRADIS_CONTACT);
        treacheryDeck.nextCard(bait, dradisContact);

        execute(new DrawSkillCardsOfColorEvent(1, TREACHERY, 2));

        assertSkillCards(1, bait, dradisContact);
        assertNoSkillCards(2);
    }

    @Test
    void shouldDrawCardsOutsideSkillSet() {
        player(2).character(KARA_STARBUCK_THRACE);
        val politics = new SkillCard(1, CONSOLIDATE_POWER);
        politicsDeck.nextCard(politics);

        execute(new DrawSkillCardsOfColorEvent(2, POLITICS, 1));

        assertSkillCards(2, politics);
    }
}

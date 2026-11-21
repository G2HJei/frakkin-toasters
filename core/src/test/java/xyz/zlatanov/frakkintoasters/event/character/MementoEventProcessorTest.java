package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.BRENDAN_HOTDOG_COSTANZA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DOGFIGHT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EVASIVE_MANOEUVRES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.MAXIMUM_FIREPOWER;

class MementoEventProcessorTest extends EventTestHarness<MementoEvent> {

    private static final SkillCard EVASIVE = new SkillCard(1, EVASIVE_MANOEUVRES);
    private static final SkillCard FIREPOWER = new SkillCard(2, MAXIMUM_FIREPOWER);
    private static final SkillCard DOG = new SkillCard(3, DOGFIGHT);

    // He keeps 2 of the top 3 Piloting cards.
    @Test
    void drawsThreeAndDiscardsOne() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        pilotingDeck.nextCard(EVASIVE, FIREPOWER, DOG);

        execute(new MementoEvent(1));

        assertSkillCards(1, EVASIVE, FIREPOWER, DOG);
        assertTrue(game.turn().hasUsed(1, MEMENTO));
        assertFollowup(one(new DiscardSkillCardsEvent(1, List.of(EVASIVE)), new DiscardSkillCardsEvent(1, List.of(FIREPOWER)),
                new DiscardSkillCardsEvent(1, List.of(DOG))));
    }

    @Test
    void onceATurn() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        ops.player(1).useAbility(MEMENTO);

        assertInvalid(new MementoEvent(1));
    }

    @Test
    void onlyHotDog() {
        player(1).character(LAURA_ROSLIN);

        assertInvalid(new MementoEvent(1));
    }
}

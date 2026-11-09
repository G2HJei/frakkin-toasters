package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ENGINE_ROOM;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.A_BETTER_MACHINE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;

class EngineRoomEventProcessorTest extends EventTestHarness<EngineRoomEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(HELENA_CAIN);
        moveTo(ENGINE_ROOM, HELENA_CAIN);
    }

    @Test
    void shouldActivateEngineRoom() {
        val skillCard1 = new SkillCard(0, GUTS_AND_INITIATIVE);
        val skillCard2 = new SkillCard(1, A_BETTER_MACHINE);
        player(1).gainSkillCards(skillCard1, skillCard2);

        execute(new EngineRoomEvent(1, skillCard1, skillCard2));

        assertTrue(player(1).skillCards().cards().isEmpty());
        assertTrue(game.decks().tactics().discardedCards().contains(skillCard1));
        assertTrue(game.decks().treachery().discardedCards().contains(skillCard2));
        assertTrue(pegasusBoard.engineRoomActivated());
    }

    @Test
    void shouldNotBeUsedWhileDamaged() {
        val skillCard1 = new SkillCard(0, GUTS_AND_INITIATIVE);
        val skillCard2 = new SkillCard(1, A_BETTER_MACHINE);
        player(1).gainSkillCards(skillCard1, skillCard2);
        pegasusBoard.damage(ENGINE_ROOM);

        assertInvalid(new EngineRoomEvent(1, skillCard1, skillCard2));
    }

    @Test
    void shouldDiscard2EqualCards() {
        val skillCard = new SkillCard(0, GUTS_AND_INITIATIVE);
        player(1).gainSkillCards(skillCard, skillCard);

        execute(new EngineRoomEvent(1, skillCard, skillCard));

        assertTrue(player(1).skillCards().cards().isEmpty());
    }

    @Test
    void shouldDiscardCardsInHand() {
        val skillCard = new SkillCard(0, GUTS_AND_INITIATIVE);
        player(1).gainSkillCards(skillCard);

        assertInvalid(new EngineRoomEvent(1, skillCard, skillCard));
    }
}

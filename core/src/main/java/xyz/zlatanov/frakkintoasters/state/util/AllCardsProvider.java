package xyz.zlatanov.frakkintoasters.state.util;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

public class AllCardsProvider {

    public static Deck<CivilianShip> civilianShipsDeck() {
        val deck = new Deck<CivilianShip>();
        IntStream.range(0, 2).forEach(i -> deck.add(new CivilianShip(0, 0, 0)));
        IntStream.range(0, 2).forEach(i -> deck.add(new CivilianShip(0, 0, 2)));
        IntStream.range(0, 6).forEach(i -> deck.add(new CivilianShip(0, 0, 1)));
        deck.add(new CivilianShip(0, 1, 1));
        deck.add(new CivilianShip(1, 0, 1));
        deck.shuffle();
        return deck;
    }

    @SafeVarargs
    public static <T extends Enum<T>> Deck<T> genericDeck(Class<T> clazz, T... repeatedCards) {
        return new Deck<T>()
                .add(Arrays.asList(clazz.getEnumConstants()))
                .add(Arrays.asList(repeatedCards))
                .shuffle();
    }

    public static Deck<SkillCard> politicsCards() {
        return new Deck<SkillCard>()
                .add(skills(8, 1, CONSOLIDATE_POWER))
                .add(skills(6, 2, CONSOLIDATE_POWER))
                .add(skills(4, 3, INVESTIGATIVE_COMMITTEE))
                .add(skills(2, 4, INVESTIGATIVE_COMMITTEE))
                .add(skills(1, 5, INVESTIGATIVE_COMMITTEE))
                .add(skills(1, 1, SUPPORT_THE_PEOPLE))
                .add(skills(1, 2, SUPPORT_THE_PEOPLE))
                .add(skills(1, 3, PREVENTIVE_POLICY))
                .add(skills(1, 4, PREVENTIVE_POLICY))
                .add(skills(1, 5, PREVENTIVE_POLICY))
                .add(skills(1, 6, POLITICAL_PROWESS))
                //todo check correct quantities of the following
                .add(skills(1, 6, FORCE_THEIR_HAND))
                .add(skills(1, 6, POPULAR_INFLUENCE))
                .add(skills(1, 6, NEGOTIATION))
                .shuffle();
    }

    public static Deck<SkillCard> leadershipCards() {
        return new Deck<SkillCard>()
                .add(skills(8, 1, EXECUTIVE_ORDER))
                .add(skills(6, 2, EXECUTIVE_ORDER))
                .add(skills(4, 3, DECLARE_EMERGENCY))
                .add(skills(2, 4, DECLARE_EMERGENCY))
                .add(skills(1, 5, DECLARE_EMERGENCY))
                .add(skills(1, 1, MAJOR_VICTORY))
                .add(skills(1, 2, MAJOR_VICTORY))
                .add(skills(1, 3, AT_ANY_COST))
                .add(skills(1, 4, AT_ANY_COST))
                .add(skills(1, 5, AT_ANY_COST))
                .add(skills(3, 0, IRON_WILL))
                .add(skills(1, 6, STATE_OF_EMERGENCY))
                //todo check correct quantities of the deck
                .add(skills(1, 0, ALL_HANDS_ON_DECK))
                .add(skills(1, 3, RESTORE_ORDER))
                .add(skills(1, 5, CHANGE_OF_PLANS))
                .shuffle();
    }

    public static Deck<SkillCard> tacticsCards() {
        return new Deck<SkillCard>()
                .add(skills(8, 1, LAUNCH_SCOUT))
                .add(skills(6, 2, LAUNCH_SCOUT))
                .add(skills(4, 3, STRATEGIC_PLANNING))
                .add(skills(2, 4, STRATEGIC_PLANNING))
                .add(skills(1, 5, STRATEGIC_PLANNING))
                .add(skills(1, 1, GUTS_AND_INITIATIVE))
                .add(skills(1, 2, GUTS_AND_INITIATIVE))
                .add(skills(1, 3, CRITICAL_SITUATION))
                .add(skills(1, 4, CRITICAL_SITUATION))
                .add(skills(1, 5, CRITICAL_SITUATION))
                .add(skills(3, 0, TRUST_INSTINCTS))
                .add(skills(1, 6, SCOUTING_FOR_FUEL))
                //todo check quantities below
                .add(skills(1, 1, QUICK_THINKING))
                .add(skills(1, 3, UNORTHODOX_PLAN))
                .add(skills(1, 5, A_SECOND_CHANCE))
                .shuffle();
    }

    public static Deck<SkillCard> pilotingCards() {
        return new Deck<SkillCard>()
                .add(skills(8, 1, EVASIVE_MANOEUVRES))
                .add(skills(6, 2, EVASIVE_MANOEUVRES))
                .add(skills(4, 3, MAXIMUM_FIREPOWER))
                .add(skills(2, 4, MAXIMUM_FIREPOWER))
                .add(skills(1, 5, MAXIMUM_FIREPOWER))
                .add(skills(1, 1, FULL_THROTTLE))
                .add(skills(1, 2, FULL_THROTTLE))
                .add(skills(1, 3, RUN_INTERFERENCE))
                .add(skills(1, 4, RUN_INTERFERENCE))
                .add(skills(1, 5, RUN_INTERFERENCE))
                .add(skills(3, 0, PROTECT_THE_FLEET))
                .add(skills(1, 6, BEST_OF_THE_BEST))
                //todo check quantities below
                .add(skills(1, 0, DOGFIGHT))
                .add(skills(1, 3, COMBAT_VETERAN))
                .add(skills(1, 5, LAUNCH_RESERVES))
                .shuffle();
    }

    public static Deck<SkillCard> engineeringCards() {
        return new Deck<SkillCard>()
                .add(skills(8, 1, REPAIR))
                .add(skills(6, 2, REPAIR))
                .add(skills(4, 3, SCIENTIFIC_RESEARCH))
                .add(skills(2, 4, SCIENTIFIC_RESEARCH))
                .add(skills(1, 5, SCIENTIFIC_RESEARCH))
                .add(skills(1, 1, JURY_RIGGED))
                .add(skills(1, 2, JURY_RIGGED))
                .add(skills(1, 3, CALCULATIONS))
                .add(skills(1, 4, CALCULATIONS))
                .add(skills(1, 5, CALCULATIONS))
                .add(skills(3, 0, ESTABLISH_NETWORK))
                .add(skills(1, 6, BUILD_NUKE))
                // todo check quantities
                .add(skills(1, 0, INSTALL_UPGRADE))
                .add(skills(1, 3, RAPTOR_SPECIALIST))
                .add(skills(1, 5, TEST_THE_LIMITS))
                .shuffle();
    }

    public static Deck<SkillCard> treacheryCards() {
        return new Deck<SkillCard>()
                //todo check quantities below
                .add(skills(1, 0, DRAIDIS_CONTACT))
                .add(skills(1, 0, BAIT))
                .add(skills(1, 3, A_BETTER_MACHINE))
                .add(skills(1, 3, PERSONAL_VICES))
                .add(skills(1, 4, VIOLENT_OUTBURSTS))
                .add(skills(1, 5, EXPLOIT_A_WEAKNESS))
                .shuffle();
    }

    private static List<SkillCard> skills(int quantity, int value, SkillCardType type) {
        val result = new ArrayList<SkillCard>();
        for (int i = 0; i < quantity; i++) {
            result.add(new SkillCard(value, type));
        }
        return result;
    }
}

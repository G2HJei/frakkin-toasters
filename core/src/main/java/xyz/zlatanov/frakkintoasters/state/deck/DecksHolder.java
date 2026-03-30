package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.card.*;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

@Getter
@Accessors(fluent = true)
@Builder
public class DecksHolder {
    private Deck<CivilianShip>    civilianShips;
    private Deck<BasestarDamage>  basestarDamage;
    private Deck<PegasusDamage>   pegasusDamage;
    private Deck<GalacticaDamage> galacticaDamage;
    private Deck<DestinationCard> destination;
    private Deck<SkillCard>       politics;
    private Deck<SkillCard>       leadership;
    private Deck<SkillCard>       tactics;
    private Deck<SkillCard>       piloting;
    private Deck<SkillCard>       engineering;
    private Deck<SkillCard>       treachery;
    private Deck<QuorumCard>      quorum;
    private Deck<CrisisCard>      crisis;
    private Deck<SuperCrisisCard> superCrisis;
    private Deck<LoyaltyCard>     loyalty;
    private Deck<MutinyCard>      mutiny;
    private Deck<MotiveCard>      motive;

    public void setupCivilianShipsDeck() {
        IntStream.range(0, 2).forEach(i -> civilianShips.add(new CivilianShip(0, 0, 0)));
        IntStream.range(0, 2).forEach(i -> civilianShips.add(new CivilianShip(0, 0, 2)));
        IntStream.range(0, 6).forEach(i -> civilianShips.add(new CivilianShip(0, 0, 1)));
        civilianShips.add(new CivilianShip(0, 1, 1));
        civilianShips.add(new CivilianShip(1, 0, 1));
        civilianShips.shuffle();
    }

    public void setupExtraTokens() {
        basestarDamage.add(Arrays.asList(BasestarDamage.values()));
        basestarDamage.shuffle();
        pegasusDamage.add(Arrays.asList(PegasusDamage.values()));
        pegasusDamage.shuffle();
        galacticaDamage.add(Arrays.asList(GalacticaDamage.values()));
        galacticaDamage.shuffle();
        destination.add(Arrays.asList(DestinationCard.values()));
        //add repeated cards. todo: DOUBLE CHECK!!!
        destination.add(List.of(REMOTE_PLANET, ICY_MOON, BARREN_PLANET, TYLIUM_PLANET, TYLIUM_PLANET, TYLIUM_PLANET));
        destination.shuffle();
        quorum.add(Arrays.asList(QuorumCard.values()));
        quorum.shuffle();
        crisis.add(Arrays.asList(CrisisCard.values()));
        crisis.shuffle();
        superCrisis.add(Arrays.asList(SuperCrisisCard.values()));
        superCrisis.shuffle();
        //todo check correct card quantities
        loyalty.add(Arrays.asList(LoyaltyCard.values()));
        for (int i = 0; i < 11; i++) {
            loyalty.add(NOT_CYLON);
        }
        loyalty.shuffle();
        mutiny.add(Arrays.asList(MutinyCard.values()));
        mutiny.shuffle();
        motive.add(Arrays.asList(MotiveCard.values()));
        motive.shuffle();
    }

    public void setupSkillCards() {
        addCards(8, 1, CONSOLIDATE_POWER, politics);
        addCards(6, 2, CONSOLIDATE_POWER, politics);
        addCards(4, 3, INVESTIGATIVE_COMMITTEE, politics);
        addCards(2, 4, INVESTIGATIVE_COMMITTEE, politics);
        addCards(1, 5, INVESTIGATIVE_COMMITTEE, politics);
        addCards(1, 1, SUPPORT_THE_PEOPLE, politics);
        addCards(1, 2, SUPPORT_THE_PEOPLE, politics);
        addCards(1, 3, PREVENTIVE_POLICY, politics);
        addCards(1, 4, PREVENTIVE_POLICY, politics);
        addCards(1, 5, PREVENTIVE_POLICY, politics);
        addCards(1, 6, POLITICAL_PROWESS, politics);
        //todo check correct quantities of the following
        addCards(1, 6, FORCE_THEIR_HAND, politics);
        addCards(1, 6, POPULAR_INFLUENCE, politics);
        addCards(1, 6, NEGOTIATION, politics);
        politics.shuffle();

        addCards(8, 1, EXECUTIVE_ORDER, leadership);
        addCards(6, 2, EXECUTIVE_ORDER, leadership);
        addCards(4, 3, DECLARE_EMERGENCY, leadership);
        addCards(2, 4, DECLARE_EMERGENCY, leadership);
        addCards(1, 5, DECLARE_EMERGENCY, leadership);
        addCards(1, 1, MAJOR_VICTORY, leadership);
        addCards(1, 2, MAJOR_VICTORY, leadership);
        addCards(1, 3, AT_ANY_COST, leadership);
        addCards(1, 4, AT_ANY_COST, leadership);
        addCards(1, 5, AT_ANY_COST, leadership);
        addCards(3, 0, IRON_WILL, leadership);
        addCards(1, 6, STATE_OF_EMERGENCY, leadership);
        //todo check correct quantities of the following
        addCards(1, 0, ALL_HANDS_ON_DECK, leadership);
        addCards(1, 3, RESTORE_ORDER, leadership);
        addCards(1, 5, CHANGE_OF_PLANS, leadership);
        leadership.shuffle();

        addCards(8, 1, LAUNCH_SCOUT, tactics);
        addCards(6, 2, LAUNCH_SCOUT, tactics);
        addCards(4, 3, STRATEGIC_PLANNING, tactics);
        addCards(2, 4, STRATEGIC_PLANNING, tactics);
        addCards(1, 5, STRATEGIC_PLANNING, tactics);
        addCards(1, 1, GUTS_AND_INITIATIVE, tactics);
        addCards(1, 2, GUTS_AND_INITIATIVE, tactics);
        addCards(1, 3, CRITICAL_SITUATION, tactics);
        addCards(1, 4, CRITICAL_SITUATION, tactics);
        addCards(1, 5, CRITICAL_SITUATION, tactics);
        addCards(3, 0, TRUST_INSTINCTS, tactics);
        addCards(1, 6, SCOUTING_FOR_FUEL, tactics);
        //todo check quantities below
        addCards(1, 1, QUICK_THINKING, tactics);
        addCards(1, 3, UNORTHODOX_PLAN, tactics);
        addCards(1, 5, A_SECOND_CHANCE, tactics);
        tactics.shuffle();

        addCards(8, 1, EVASIVE_MANOEUVRES, piloting);
        addCards(6, 2, EVASIVE_MANOEUVRES, piloting);
        addCards(4, 3, MAXIMUM_FIREPOWER, piloting);
        addCards(2, 4, MAXIMUM_FIREPOWER, piloting);
        addCards(1, 5, MAXIMUM_FIREPOWER, piloting);
        addCards(1, 1, FULL_THROTTLE, piloting);
        addCards(1, 2, FULL_THROTTLE, piloting);
        addCards(1, 3, RUN_INTERFERENCE, piloting);
        addCards(1, 4, RUN_INTERFERENCE, piloting);
        addCards(1, 5, RUN_INTERFERENCE, piloting);
        addCards(3, 0, PROTECT_THE_FLEET, piloting);
        addCards(1, 6, BEST_OF_THE_BEST, piloting);
        //todo check quantities below
        addCards(1, 0, DOGFIGHT, piloting);
        addCards(1, 3, COMBAT_VETERAN, piloting);
        addCards(1, 5, LAUNCH_RESERVES, piloting);
        piloting.shuffle();

        addCards(8, 1, REPAIR, engineering);
        addCards(6, 2, REPAIR, engineering);
        addCards(4, 3, SCIENTIFIC_RESEARCH, engineering);
        addCards(2, 4, SCIENTIFIC_RESEARCH, engineering);
        addCards(1, 5, SCIENTIFIC_RESEARCH, engineering);
        addCards(1, 1, JURY_RIGGED, engineering);
        addCards(1, 2, JURY_RIGGED, engineering);
        addCards(1, 3, CALCULATIONS, engineering);
        addCards(1, 4, CALCULATIONS, engineering);
        addCards(1, 5, CALCULATIONS, engineering);
        addCards(3, 0, ESTABLISH_NETWORK, engineering);
        addCards(1, 6, BUILD_NUKE, engineering);
        // todo check quantities below
        addCards(1, 0, INSTALL_UPGRADE, engineering);
        addCards(1, 3, RAPTOR_SPECIALIST, engineering);
        addCards(1, 5, TEST_THE_LIMITS, engineering);
        engineering.shuffle();

        //todo check quantities below
        addCards(1, 0, DRAIDIS_CONTACT, treachery);
        addCards(1, 0, BAIT, treachery);
        addCards(1, 3, A_BETTER_MACHINE, treachery);
        addCards(1, 3, PERSONAL_VICES, treachery);
        addCards(1, 4, VIOLENT_OUTBURSTS, treachery);
        addCards(1, 5, EXPLOIT_A_WEAKNESS, treachery);
        treachery.shuffle();
    }

    private static void addCards(int quantity, int value, SkillCardType type, Deck<SkillCard> deck) {
        for (int i = 0; i < quantity; i++) {
            deck.add(new SkillCard(value, type));
        }
    }
}

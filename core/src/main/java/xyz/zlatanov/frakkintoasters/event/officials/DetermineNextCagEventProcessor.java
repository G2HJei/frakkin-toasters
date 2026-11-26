package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

public class DetermineNextCagEventProcessor extends EventProcessor<DetermineNextCagEvent> implements LineOfSuccessionSelector {

    // The line of succession for the CAG title, highest first, which the reference shows as well.
    public static final List<Character> LINE_OF_SUCCESSION = List.of(
            LEE_APOLLO_ADAMA,
            KARA_STARBUCK_THRACE,
            LOUANNE_KAT_KATRAINE,
            KARL_HELO_AGATHON_ALT,
            SHARON_BOOMER_VALERII,
            BRENDAN_HOTDOG_COSTANZA,
            SAMUEL_T_ANDERS,
            LEE_ADAMA,
            KARL_HELO_AGATHON,
            WILLIAM_ADAMA,
            HELENA_CAIN,
            SAUL_TIGH,
            FELIX_GAETA,
            ANASTASIA_DEE_DUALLA,
            LOUIS_HOSHI,
            TOM_ZAREK_ALT,
            CHIEF_GALEN_TYROL,
            CALLANDRA_CALLY_TYROL,
            SHERMAN_DOC_COTTLE,
            TOM_ZAREK,
            ELLEN_TIGH,
            GAIUS_BALTAR_ALT,
            GAIUS_BALTAR,
            TORY_FOSTER,
            ROMO_LAMPKIN,
            LAURA_ROSLIN);

    @Override
    public List<Character> lineOfSuccession() {
        return LINE_OF_SUCCESSION;
    }

    @Override
    public Followup process() {
        val brig = galacticaBoard.charactersIn(BRIG);
        // The highest in line outside the "Brig". When every human is in it, the highest in line there, until one of
        // them leaves it (rules 4.4.2.3 and 4.4.3.2).
        val next = Optional.ofNullable(nextInLine(game, event.excludedPlayerNumber(), character -> !brig.contains(character)))
                .orElseGet(() -> nextInLine(game, event.excludedPlayerNumber(), character -> true));
        ops.titles().assignCag(next);
        return Followup.NONE;
    }

    @Override
    public Character noNextInLine() {
        return null;
    }
}

package xyz.zlatanov.frakkintoasters.event.location.cylon;

// The four abilities listed on the "Basestar Bridge" location.
public enum BasestarBridgeAbility {
    // "The CAG must place 1 civilian ship (following all placement rules)."
    CAG_PLACES_CIVILIAN_SHIP,
    // "Roll a die: 1-3: Decrease the jump track by 1, 4-8: Increase the pursuit track by 1"
    JUMP_OR_PURSUIT,
    // "Place 1 Basestar or 3 Raiders in any 1 area on this game board."
    BASESTAR_OR_RAIDERS,
    // "Roll a die. If the result is less than the number of raiders on the main game board, draw 2 Galactica damage
    // tokens and choose 1 to resolve."
    GALACTICA_DAMAGE
}

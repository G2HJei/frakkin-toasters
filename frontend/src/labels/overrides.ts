// Names that the humanized constant gets wrong. Each key names a constant of an enum, or a type, in schema.json, which a
// test checks. Only names, never a card's printed text.

export const CONSTANT_LABELS: Record<string, Record<string, string>> = {
  Character: {
    ANASTASIA_DEE_DUALLA: 'Anastasia "Dee" Dualla',
    BRENDAN_HOTDOG_COSTANZA: 'Brendan "Hot Dog" Costanza',
    CALLANDRA_CALLY_TYROL: 'Callandra "Cally" Tyrol',
    GAIUS_BALTAR_ALT: 'Gaius Baltar (alternate)',
    KARA_STARBUCK_THRACE: 'Kara "Starbuck" Thrace',
    KARL_HELO_AGATHON: 'Karl "Helo" Agathon',
    KARL_HELO_AGATHON_ALT: 'Karl "Helo" Agathon (alternate)',
    LEE_APOLLO_ADAMA: 'Lee "Apollo" Adama',
    LOUANNE_KAT_KATRAINE: 'Louanne "Kat" Katraine',
    SHARON_ATHENA_AGATHON: 'Sharon "Athena" Agathon',
    SHARON_BOOMER_VALERII: 'Sharon "Boomer" Valerii',
    SHERMAN_DOC_COTTLE: 'Sherman "Doc" Cottle',
    SIMON_ONEIL: "Simon O'Neil",
    TOM_ZAREK_ALT: 'Tom Zarek (alternate)',
  },
  Location: {
    ADMIRALS_QUARTERS: "Admiral's Quarters",
    PRESIDENTS_OFFICE: "President's Office",
    GALACTICA_SPACE_12_OCLOCK: "Space 12 o'clock",
    GALACTICA_SPACE_2_OCLOCK: "Space 2 o'clock",
    GALACTICA_SPACE_4_OCLOCK: "Space 4 o'clock",
    GALACTICA_SPACE_6_OCLOCK: "Space 6 o'clock",
    GALACTICA_SPACE_8_OCLOCK: "Space 8 o'clock",
    GALACTICA_SPACE_10_OCLOCK: "Space 10 o'clock",
    CYLON_FLEET_SPACE_1: 'Fleet space 1',
    CYLON_FLEET_SPACE_2: 'Fleet space 2',
    CYLON_FLEET_SPACE_3: 'Fleet space 3',
    CYLON_FLEET_SPACE_4: 'Fleet space 4',
    CYLON_FLEET_SPACE_5_6: 'Fleet space 5–6',
    CYLON_FLEET_SPACE_7_8: 'Fleet space 7–8',
  },
  JumpPreparation: {
    MINUS_3: '−3 distance',
    MINUS_1: '−1 distance',
  },
  ShipType: {
    VIPER_MARK_VII: 'Viper Mk VII',
  },
};

export const TYPE_LABELS: Record<string, string> = {
  NoOpEvent: 'Pass',
  ActionEvent: 'Take an action',
  MoveEvent: 'Move',
  SelectCharacterEvent: 'Choose a character',
  DrawSkillCardsEvent: 'Draw Skill Cards',
  DiscardSkillCardsEvent: 'Discard Skill Cards',
  PlayCardsIntoSkillCheckEvent: 'Play cards into the skill check',
  PlayCardsBeforeSkillCheckEvent: 'Play cards before the skill check',
  PlayInterruptCardEvent: 'Play an interrupt card',
  UseRollAbilityEvent: 'Use an ability on the roll',
  ChooseCrisisSectionEvent: 'Choose a section',
  FTLControlEvent: 'FTL Control',
  PegasusCicEvent: 'Pegasus CIC',
  PlaceCivilianShipEvent: 'Place a civilian ship',
  ActivateViperEvent: 'Activate a viper',
  'DeckFact.CardsDrawn': 'Cards drawn',
  'SkillCheckFact.CardsDrawn': 'Skill check cards drawn',
};

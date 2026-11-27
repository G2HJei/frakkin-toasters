// The fallback wording: a constant or a type name as words.

const SMALL = new Set(['of', 'the', 'a', 'an', 'and', 'or', 'to', 'in', 'on', 'at', 'for', 'with', 'from', 'by']);
const UPPER = new Set(['ftl', 'cag', 'cic', 'ii', 'iii']);

/** `KARA_STARBUCK_THRACE` → `Kara Starbuck Thrace`, `GALACTICA_SPACE_1_OCLOCK` → `Galactica Space 1 O'clock`. */
export function humanizeConstant(constant: string): string {
  return constant
    .toLowerCase()
    .split('_')
    .filter((word) => word.length > 0)
    .map((word, index) => {
      if (UPPER.has(word)) {
        return word.toUpperCase();
      }
      if (word === 'oclock') {
        return "o'clock";
      }
      if (index > 0 && SMALL.has(word)) {
        return word;
      }
      return word.charAt(0).toUpperCase() + word.substring(1);
    })
    .join(' ');
}

/**
 * `MoveEvent` → `Move`, `DeckFact.CardsDrawn` → `Cards drawn`, `ResolveCrisisCardEvent` → `Resolve crisis card`,
 * `MakeCagEvent` → `Make CAG`.
 */
export function humanizeType(name: string): string {
  const simple = name.includes('.') ? name.substring(name.lastIndexOf('.') + 1) : name;
  const words = simple
    .replace(/Event$/, '')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1 $2')
    .split(' ')
    .filter((word) => word.length > 0);
  return words
    .map((word, index) => {
      // an acronym a type name writes as a word: "Cag" in MakeCagEvent
      if (UPPER.has(word.toLowerCase()) || /^[A-Z]{2,}$/.test(word)) {
        return word.toUpperCase();
      }
      return index === 0 ? word : word.toLowerCase();
    })
    .join(' ');
}

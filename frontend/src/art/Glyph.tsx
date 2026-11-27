// Our own line icons, drawn on a 24px grid and stroked in one color: the turn's steps (`StepIcon`) and the glyphs below.
// The UI draws their markup inline, the answers as an image (`strokedSvg`).

const GLYPHS = {
  // a map pin: where a player stands, a location an answer picks
  LOCATION: '<path d="M12 21.5c-3.7-4-6.5-7.5-6.5-11a6.5 6.5 0 0 1 13 0c0 3.5-2.8 7-6.5 11z"/><circle cx="12" cy="10.5" r="2.4"/>',
  // a card coming down onto a deck seen from the side
  DECK_TOP: '<path d="M12 2.5v7M8.5 6 12 9.5 15.5 6"/><path d="M4 13.5h16M4 17h16M4 20.5h16"/>',
  // a card pushed in under a deck seen from the side
  DECK_BOTTOM: '<path d="M4 3.5h16M4 7h16M4 10.5h16"/><path d="M12 21.5v-7M8.5 18 12 14.5 15.5 18"/>',
};

export type GlyphName = keyof typeof GLYPHS;

export function strokedSvg(markup: string, color: string): string {
  return (
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" color="${color}" fill="none" ` +
    `stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${markup}</svg>`
  );
}

export function glyphSvg(name: GlyphName, color: string): string {
  return strokedSvg(GLYPHS[name], color);
}

export function StrokedIcon({ markup, className }: { markup: string; className: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      dangerouslySetInnerHTML={{ __html: markup }}
    />
  );
}

export function Glyph({ name, className = '' }: { name: GlyphName; className?: string }) {
  return <StrokedIcon markup={GLYPHS[name]} className={`glyph ${className}`.trim()} />;
}

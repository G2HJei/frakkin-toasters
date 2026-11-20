import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { placeNear, roomBeside } from './nearPointer';

function popover(width: number, height: number) {
  const element = document.createElement('div');
  element.getBoundingClientRect = () => new DOMRect(0, 0, width, height);
  return element;
}

describe('placeNear', () => {
  const window_ = { width: window.innerWidth, height: window.innerHeight };

  beforeEach(() => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 1000 });
    Object.defineProperty(window, 'innerHeight', { configurable: true, value: 800 });
  });

  afterEach(() => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: window_.width });
    Object.defineProperty(window, 'innerHeight', { configurable: true, value: window_.height });
  });

  it('puts the popover right of and below the pointer when it fits', () => {
    const element = popover(300, 200);
    placeNear(element, { x: 100, y: 100 });
    expect([element.style.left, element.style.top]).toEqual(['114px', '114px']);
  });

  it('puts it left of and above the pointer near the right and bottom edges', () => {
    const element = popover(300, 200);
    placeNear(element, { x: 900, y: 700 });
    expect([element.style.left, element.style.top]).toEqual(['586px', '486px']);
  });

  it('slides it along the edge of the window when neither side fits', () => {
    const element = popover(300, 700);
    placeNear(element, { x: 100, y: 400 });
    expect([element.style.left, element.style.top]).toEqual(['114px', '92px']);
  });

  it('gives the room on the wider side of the pointer, which a popover that wide fills without covering it', () => {
    expect(roomBeside({ x: 300, y: 400 })).toBe(678);
    expect(roomBeside({ x: 700, y: 400 })).toBe(678);
    const element = popover(678, 200);
    placeNear(element, { x: 300, y: 100 });
    expect(element.style.left).toBe('314px');
  });
});

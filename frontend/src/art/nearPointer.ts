// Where a popover goes: next to the pointer, never off the window.
import { useLayoutEffect, type RefObject } from 'react';

export interface Point {
  x: number;
  y: number;
}

const GAP = 14;
const MARGIN = 8;

// after the pointer when it fits (right, below), else before it, else as close as the window's edges allow
function along(pointer: number, size: number, room: number): number {
  if (pointer + GAP + size <= room - MARGIN) {
    return pointer + GAP;
  }
  if (pointer - GAP - size >= MARGIN) {
    return pointer - GAP - size;
  }
  return Math.max(MARGIN, Math.min(pointer + GAP, room - MARGIN - size));
}

/** The widest a popover can be and still go beside the point, after or before it, without covering it. */
export function roomBeside(point: Point): number {
  return Math.max(window.innerWidth - point.x, point.x) - GAP - MARGIN;
}

export function placeNear(element: HTMLElement, point: Point) {
  // measured at the window's corner, where nothing squeezes it
  element.style.left = '0px';
  element.style.top = '0px';
  const { width, height } = element.getBoundingClientRect();
  element.style.left = `${along(point.x, width, window.innerWidth)}px`;
  element.style.top = `${along(point.y, height, window.innerHeight)}px`;
}

/**
 * Keeps a fixed element next to the point, before the browser paints it, and again whenever its size changes (images
 * load). A new point object places it anew, so each show gets one.
 */
export function usePlaceNear(ref: RefObject<HTMLElement | null>, point: Point | null) {
  useLayoutEffect(() => {
    const element = ref.current;
    if (!element || !point) {
      return;
    }
    placeNear(element, point);
    if (typeof ResizeObserver === 'undefined') {
      return;
    }
    const observer = new ResizeObserver(() => placeNear(element, point));
    observer.observe(element);
    return () => observer.disconnect();
  }, [ref, point]);
}

import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';

// zoom gives fractional scroll offsets, so the end is a few pixels wide
const SLACK = 8;

/**
 * Keeps a scrolling element at its end while what it shows grows, until the user scrolls up. Scrolling back down to
 * the end, or `toBottom`, follows again. `unseen` says that `content` changed since the user scrolled up. `scrollRef`
 * goes on the element that scrolls, `contentRef` on the one inside it that grows, and `content` changes with every new
 * entry.
 */
export function useFollowBottom(content: unknown) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  // the observers read the ref, the button the state
  const followingRef = useRef(true);
  const [following, setFollowing] = useState(true);
  const [unseen, setUnseen] = useState(false);
  // where the last scroll left the element, the user's or ours
  const lastTop = useRef(0);

  const follow = useCallback((value: boolean) => {
    followingRef.current = value;
    setFollowing(value);
    if (value) setUnseen(false);
  }, []);

  const toEnd = useCallback(() => {
    const element = scrollRef.current;
    if (!element) return;
    element.scrollTop = element.scrollHeight;
    lastTop.current = element.scrollTop;
  }, []);

  const toBottom = useCallback(() => {
    follow(true);
    toEnd();
  }, [follow, toEnd]);

  const onScroll = useCallback(() => {
    const element = scrollRef.current;
    if (!element) return;
    const top = element.scrollTop;
    if (element.scrollHeight - top - element.clientHeight <= SLACK) follow(true);
    // the event comes a frame after its scroll, and the end may have moved since (an image loaded, the panels above
    // grew): only a scroll up is the user leaving the end
    else if (top < lastTop.current) follow(false);
    lastTop.current = top;
  }, [follow]);

  useLayoutEffect(() => {
    if (followingRef.current) toEnd();
    else setUnseen(true);
  }, [content, toEnd]);

  // the height changes without a new entry too: images load, a turn opens or closes, the panels above grow
  useEffect(() => {
    const element = scrollRef.current;
    if (!element || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(() => {
      if (followingRef.current) toEnd();
    });
    observer.observe(element);
    if (contentRef.current) observer.observe(contentRef.current);
    return () => observer.disconnect();
  }, [toEnd]);

  return { scrollRef, contentRef, following, unseen, onScroll, toBottom };
}

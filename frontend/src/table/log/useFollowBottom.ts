import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';

// zoom gives fractional scroll offsets, so the end is a few pixels wide
const SLACK = 8;

/**
 * Keeps a scrolling element at its end while what it shows grows, until the user scrolls up. Scrolling back down to
 * the end, or `toBottom`, follows again. `scrollRef` goes on the element that scrolls, `contentRef` on the one inside
 * it that grows, and `content` changes with every new entry.
 */
export function useFollowBottom(content: unknown) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  // the observers read the ref, the button the state
  const followingRef = useRef(true);
  const [following, setFollowing] = useState(true);

  const follow = useCallback((value: boolean) => {
    followingRef.current = value;
    setFollowing(value);
  }, []);

  const toBottom = useCallback(() => {
    follow(true);
    const element = scrollRef.current;
    if (element) element.scrollTop = element.scrollHeight;
  }, [follow]);

  // a scroll event comes from the user and from toBottom alike, so the position decides
  const onScroll = useCallback(() => {
    const element = scrollRef.current;
    if (element) follow(element.scrollHeight - element.scrollTop - element.clientHeight <= SLACK);
  }, [follow]);

  useLayoutEffect(() => {
    const element = scrollRef.current;
    if (followingRef.current && element) element.scrollTop = element.scrollHeight;
  }, [content]);

  // the height changes without a new entry too: images load, a turn opens or closes, the panels above grow
  useEffect(() => {
    const element = scrollRef.current;
    if (!element || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(() => {
      if (followingRef.current) element.scrollTop = element.scrollHeight;
    });
    observer.observe(element);
    if (contentRef.current) observer.observe(contentRef.current);
    return () => observer.disconnect();
  }, []);

  return { scrollRef, contentRef, following, onScroll, toBottom };
}

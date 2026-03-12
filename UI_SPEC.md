# TypeMini UI Spec

This document captures the current agreed UI behavior for the Android app.

## Global

- The app is portrait-only.
- The app should support light and dark themes.
- Interactive controls must remain legible in dark theme. In particular, the practice screen top-right menu button must not blend into the background.

## Navigation

- The bottom tab bar is removed.
- The practice screen is the default entry screen.
- The practice screen top-right overflow menu contains:
  - `重新开始`
  - `历史`
  - `设置`
- `历史` opens the history screen.
- `设置` opens the settings screen.
- History, Settings, and Result screens may use a top app bar with back navigation to return to Practice.

## Practice Screen Layout

The practice screen is divided into exactly three vertical regions:

1. Top bar
2. Practice viewport
3. Keyboard

No other controls or helper blocks should appear on the practice home screen.

## Practice Top Bar

- The current practice title is shown in the top area, aligned to the right side.
- A top-right overflow/menu button is displayed next to the title.
- The overflow button opens the practice menu.
- The top bar does not show the old bottom-tab-style navigation.

## Practice Viewport

- The upper content region is a dedicated typing viewport.
- The viewport shows exactly 4 lines of word tokens.
- Only 4 lines are rendered; the screen must not rely on a scroll bar.
- Tokens must wrap to the next line when they exceed the available width.
- When the active typing line reaches the third visible line, the viewport advances so the active line stays anchored on the third line whenever possible.
- The practice viewport does not show:
  - extra buttons
  - helper descriptions
  - mode selectors
  - practice set selectors
- The practice viewport should not draw an explicit border.

## Keyboard

- A custom on-screen keyboard is always shown at the bottom of the practice screen.
- The system keyboard is not used for typing practice.
- The keyboard uses three alphabet rows plus a space key.
- The keyboard area should not draw an explicit border.
- The keyboard should be slightly shorter vertically than earlier versions.
- The keyboard should be wider, with reduced left/right padding.
- The keyboard area should have additional bottom padding to breathe above the system gesture area.

## Keyboard Feedback

- Every key press should produce a tap sound.
- Incorrect input should additionally produce an error sound.

## Settings Screen

- Practice set selection lives in the Settings screen.
- Typing mode selection lives in the Settings screen.
- These controls should no longer appear on the practice home screen.

## History Screen

- History remains a dedicated page reachable from the practice overflow menu.

## Result Screen

- Completed sessions still navigate to a result page.

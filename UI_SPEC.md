# TypeMini UI Spec

This document only captures implementation-level UI rules for the practice rendering area and the custom keyboard.

All product, flow, navigation, and screen-intent decisions should follow `DESIGN.md`.

## Practice Rendering

### Layout

- The practice screen is divided into exactly three vertical regions:
  1. top bar
  2. practice viewport
  3. keyboard
- No extra helper panels, progress blocks, or configuration controls should appear inside the main practice area.

### Top Bar

- The current practice title is shown in the top area.
- The current unit and article context is shown as subtitle text.
- A back-to-unit action is visible on the left side.
- A top-right overflow/menu button is displayed next to the title area.
- The overflow button opens the practice menu.

### Viewport

- The upper content region is a dedicated typing viewport.
- The viewport shows exactly 4 lines of word tokens.
- Only 4 lines are rendered; the screen must not rely on a scroll bar.
- Tokens must wrap to the next line when they exceed the available width.
- When the active typing line reaches the third visible line, the viewport advances so the active line stays anchored on the third line whenever possible.
- The practice viewport does not show:
  - extra buttons
  - helper descriptions
  - mode selectors
  - course selectors
- The practice viewport should not draw an explicit border.

## Keyboard

### General

- A custom on-screen keyboard is always shown at the bottom of the practice screen.
- The system keyboard is not used for typing practice.
- The keyboard area should not draw an explicit border.
- The keyboard should be slightly shorter vertically than earlier versions.
- The keyboard should be wider, with reduced left/right padding.
- The keyboard area should have additional bottom padding to breathe above the system gesture area.

### Letter Layer

- The default keyboard uses three alphabet rows based on a standard English layout.
- The keyboard includes a space key.
- The keyboard includes a one-shot `SHIFT` key.
- The keyboard includes a `BACK` key.
- The keyboard includes a `123` key to switch to symbols.
- The letter layer exposes direct keys for:
  - apostrophe
  - comma
  - period

### Symbol Layer

- The symbol layer is reachable from `123`.
- The symbol layer can return to letters through `ABC`.
- The symbol layer should expose common English typing symbols and digits needed for article practice.

### Feedback

- Every key press should produce a tap sound.
- Incorrect input should additionally produce an error sound.

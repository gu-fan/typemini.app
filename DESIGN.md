# Design Notes

## Product Goal

TypeMini is a mobile English typing-practice app built around short, structured learning units rather than isolated typing drills.

The design goal is not to feel like a generic keyboard trainer or a test dashboard. It should feel like a calm lesson flow:

- enter from home
- continue the next lesson
- focus on one article at a time
- see progress clearly
- finish each unit with a concise sense of completion

## Core Principles

### 1. Progress Should Be Obvious

The product is organized around units and articles, so the UI should always help the user answer:

- where am I
- what is next
- how much is done

This is why home, unit, result, and summary screens all expose progress in a direct way.

### 2. Practice Should Stay Focused

The practice screen is the emotional center of the app.

It should prioritize:

- the current article
- the active typing line
- the custom keyboard

Anything not required to finish the current article should stay secondary or move into the overflow menu.

### 3. Calm, Compact, And Mobile-First

TypeMini is designed for narrow phone screens first.

That means:

- large enough touch targets
- stable vertical structure
- low visual noise
- simple cards and rounded containers
- no dense dashboards or data-heavy layouts during practice

The app should feel approachable for short daily sessions.

### 4. Native Navigation Over Novelty

The app should behave like a clean Android app:

- launcher entry opens home
- system back behaves predictably
- unit pages return to home
- practice returns to the current unit
- top app bars and menus follow familiar Android patterns

The app can have its own visual identity without inventing surprising navigation behavior.

### 5. Completion Should Feel Lightweight

Results and summaries matter, but they should not become ceremony.

The desired feeling is:

- finish article
- review the outcome
- move forward quickly

Unit summaries should provide closure, not friction.

## Visual Direction

### Palette

TypeMini uses a soft academic palette rather than sharp productivity-tool contrast.

- bright but calm surfaces
- readable ink-like text
- gentle accent colors for progress and action emphasis
- subtle gradients in the app shell to avoid flatness

The product should feel more like a study companion than a scoreboard.

### Shape Language

Rounded cards and pill-like labels are part of the app identity.

They help the app feel:

- light
- approachable
- touch-friendly

Sharp separators and dense borders should be avoided when spacing and surface contrast already define structure.

### Typography

Typography should support two modes:

- editorial clarity on home, unit, and summary screens
- high legibility during practice

Titles should be clear and compact. Practice text should remain easy to scan and visually stable while typing.

## Screen Intent

### Home

The home screen combines four jobs:

- establish the app as an English practice course
- surface the next lesson to continue
- show overall course progress
- let users jump into any unit, history, or settings

Home should feel like a clean starting point, not a dashboard packed with options.

### Unit

The unit screen should explain a learning block at a glance:

- unit title
- short description
- progress count
- article list

Each article card should make it obvious whether the article is new or already completed, while still allowing replay.

### Practice

The practice screen should feel uninterrupted and controlled.

Primary visual regions:

- top context and actions
- typing viewport
- custom keyboard

The current article title and unit context should remain visible, but the main attention must stay on the active typing content.

### Result

The result screen is a brief checkpoint after an article.

It should answer:

- how well did I do
- what should I do next

The next step must be obvious:

- next article
- unit summary
- practice again

### Unit Summary

The unit summary is the course-level reward screen.

It should provide:

- completed article count
- average and best speed
- average accuracy
- per-article completion status

It should feel like closure for a unit, not a separate analytics product.

### History

History is a review surface, not a primary navigation hub.

It should preserve:

- article attempts
- unit context
- performance records

But it should remain secondary to the guided lesson flow.

### Settings

Settings should stay intentionally narrow.

Current emphasis:

- input mode

The course structure itself should not be hidden in settings.

## Keyboard Design

The custom keyboard is part of the product identity.

It should feel close enough to a real English phone keyboard that users do not need to re-learn basic behavior.

Required characteristics:

- alphabet layout based on standard English key rows
- symbol layer
- one-shot shift behavior
- explicit backspace
- support for apostrophe, comma, and period
- enough width and bottom breathing room for gesture navigation phones

Keyboard feedback should reinforce rhythm:

- tap sound for accepted input
- distinct error sound for incorrect input

## Content Strategy

TypeMini uses short English articles grouped into units instead of disconnected word sets.

That tradeoff favors:

- a clearer sense of progression
- more natural reading and typing rhythm
- better continuity between lessons
- meaningful unit completion

The content should stay short enough for mobile practice sessions while still feeling like real language.

## Technical Design Constraints

- Phone portrait layout is the primary target.
- The launcher entry should always land on home.
- The custom keyboard must replace the system keyboard during practice.
- Course progress and unit summaries should derive from saved article results.
- Navigation must remain stable across home, unit, practice, result, and summary screens.
- The app should remain usable on both emulator and physical Android devices.

## Near-Term Opportunities

- Caps lock behavior for shift
- Long-press or alternate symbol support
- More nuanced difficulty progression across units
- Better result comparisons against previous attempts
- More expressive unit-end summaries without increasing complexity

# DESIGN_SYSTEM.md — SIM Keeper

These values are the source of truth. The reference images are for mood only (their colour labels are garbled). Implement everything here in `ui/theme/` and use only these tokens in screens.

## Principles
1. **One look answers it.** Days-left is the biggest thing on every SIM surface.
2. **Colour means urgency, and only urgency.** Carrier colours appear only in small monogram tiles and tags.
3. **Never more than 3 taps** for set up, mark recharge or change a reminder.
4. **Calm copy.** "Airtel needs a recharge in 2 days", never "WARNING".
5. **Works for parents.** 48 dp touch targets, text scales with system font size, contrast ≥ 4.5:1.

## Colour tokens

### Brand and neutrals
| Token | Light | Dark |
| --- | --- | --- |
| primary | #0E6B5F | #7FD3C2 |
| onPrimary | #FFFFFF | #003731 |
| primaryContainer | #DCEFEA | #0E4F46 |
| onPrimaryContainer | #0A4F46 | #BFEDE3 |
| background | #F4F6F3 | #0E1513 |
| surface (cards) | #FFFFFF | #17201D |
| surfaceVariant (chips, dividers) | #EEF2EF | #22302B |
| onSurface (ink) | #10201B | #E6EEEA |
| onSurfaceVariant (secondary text) | #52615B | #A9B8B2 |
| outline | #D5DDD9 | #33433D |
| inverseSurface (snackbar, FAB) | #10201B | #E6EEEA |

On Android 12+ allow Material You dynamic colour for neutrals only if the user turns it on in Settings (default off), so the brand stays recognisable. Urgency colours are never dynamic.

### Urgency (status)
| Status | Rule | Content (light / dark) | Container (light / dark) |
| --- | --- | --- | --- |
| Safe | > 7 days | #1B7F4F / #6FD39C | #E3F3EA / #123B27 |
| Soon | 3–7 days | #A35A00 / #F2B35E | #FCEBD3 / #3E2A0E |
| Urgent | 0–2 days | #B42318 / #FF8A7E | #FDE4E1 / #4A1611 |
| Expired | < 0 days | #52615B / #A9B8B2 | #EEF2EF / #22302B |

### Carrier monogram tiles
| Carrier | Letter | Text | Tile bg (light) |
| --- | --- | --- | --- |
| Jio | J | #1D4ED8 | #E3EAFD |
| Airtel | A | #B42318 | #FDE4E1 |
| Vi | V | #6D28D9 | #EEE6FD |
| BSNL | B | #C2410C | #FDEBDD |
| Other | first letter | #52615B | #EEF2EF |
In dark mode use the same text colour lightened 30% on a 20% opacity tile.

## Typography
Bundle the fonts in `res/font/` (both are SIL Open Font License, download from Google Fonts): **Space Grotesk** (500, 700) for numbers and **Plus Jakarta Sans** (400, 500, 600, 700, 800) for everything else.

| Style | Font | Size / line | Weight | Use |
| --- | --- | --- | --- | --- |
| displayNumber | Space Grotesk | 56 / 56 sp | 700 | Detail ring number |
| ringNumber | Space Grotesk | 24 / 24 sp | 700 | Home card ring number |
| headlineLarge | Jakarta | 32 / 38 sp | 800 | Onboarding titles |
| headlineMedium | Jakarta | 28 / 34 sp | 800 | Screen titles |
| titleLarge | Jakarta | 18 / 24 sp | 800 | Detail headline |
| titleMedium | Jakarta | 17 / 22 sp | 800 | SIM name on cards |
| bodyLarge | Jakarta | 16 / 24 sp | 400 | Body copy |
| bodyMedium | Jakarta | 14 / 20 sp | 500 | Secondary lines |
| labelLarge | Jakarta | 16 / 20 sp | 700 | Buttons |
| labelMedium | Jakarta | 13 / 16 sp | 700 | Section labels, uppercase, +0.4 letter spacing |
| labelSmall | Jakarta | 11 / 14 sp | 700 | Carrier tags, "DAYS" under numbers |
Nothing smaller than 11 sp.

## Spacing, shape, elevation
- Grid: 4 dp base, use 8 / 12 / 16 / 20 / 24 / 32. Screen side padding 20 dp.
- Corner radius: cards 24–26 dp, bottom sheet 32 dp top, chips 18–20 dp (pill), buttons 28 dp (pill), monogram tile 14 dp.
- Elevation: cards use a 1 dp soft shadow (`0 1 2 rgba(16,32,27,0.06)`) in light, none in dark (use surface colour). FAB and snackbar 8 dp. No gradients, no glass, no emoji.

## Components
| Component | Spec |
| --- | --- |
| PrimaryButton | 56 dp tall, full width or hug, pill, primary bg, labelLarge onPrimary, optional leading 18 dp icon |
| SecondaryButton | 56 dp, pill, 1.5 dp outline, primary text |
| TextLink | labelLarge 15 sp, primary, 48 dp min touch |
| Chip (selectable) | 40 dp tall pill. Off: 1.5 dp outline, surface bg. On: 2 dp primary border, primaryContainer bg, primary text |
| PlanChip | 64 dp tall tile in a 4-column grid: big number (Space Grotesk 20) + "days" |
| DaysRing | Circular track in status container colour, progress arc in status colour, round cap, starts at 12 o'clock. Sizes: 76 dp (card, 8 dp stroke), 120 dp (detail, 12 dp stroke), 104 dp (widget). Progress = daysLeft / planDays, clamped 0–1. Centre: number + "DAYS" |
| SimCard (home) | surface card, 18 dp padding: DaysRing 76 · name (titleMedium) + carrier tag · "Slot 2 · ends 21 Oct 2026" · status line in status colour · chevron. Urgent/Soon cards add a footer row: "Cheapest plan to stay active ₹[price] · 28 days" + small "I recharged" button |
| CarrierTag | labelSmall uppercase, carrier text colour on its tile bg, 8 dp radius, 2×8 dp padding |
| MonogramTile | 44 dp, 14 dp radius, carrier letter 18 sp 800 |
| StatusBanner | 16 dp radius, 12×14 padding, status container bg + icon + one line |
| Switch | Material 3 switch in primary |
| Snackbar | inverseSurface, 18 dp radius, check icon in safe colour, action "Undo" |
| SectionLabel | labelMedium, onSurfaceVariant, uppercase |

## Icons
Material Symbols Rounded, outline style, 2 dp stroke look, 20–24 dp. Needed: arrow_back, settings, chevron_right, add, edit, check, calendar_month, warning, group, lock, notifications, share, backup, restore.

## Motion
- Ring fills from 0 to value on first appear: 700 ms, `FastOutSlowInEasing`.
- Cards enter with a 40 ms stagger, slide 12 dp + fade, spring (dampingRatio 0.8).
- "I recharged": ring animates to full and colour cross-fades to safe (500 ms), light confetti burst (≤ 1.2 s, max 40 particles), haptic `CONFIRM`.
- Respect "Remove animations" (system animator scale 0): skip all of the above.

## Accessibility
- Every icon-only button has a `contentDescription`.
- SIM cards are one merged semantics node: "Bank SIM, Airtel, 2 days left, ends 21 October 2026, needs recharge".
- Never convey urgency by colour alone: status line text always says it.
- Test with font scale 1.3 and 2.0; layouts must not clip.

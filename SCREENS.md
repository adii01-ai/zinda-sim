# SCREENS.md — SIM Keeper v1

Visual references (mood only; this file wins on content and behaviour):
- Clickable mockup: https://claude.ai/artifact/UoGTvVtoLmiDaCb5swbFek
- Exported PNG/PDF: `designs/SIM-Keeper-all-screens.pdf`

## Sample data (use in all @Previews; all screens must agree)
Today is Monday 19 Oct 2026.
| SIM | Carrier | Slot | Plan | Last recharge | Ends | Days left | Last used | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Bank SIM | Airtel | 2 | 28 d | 23 Sep 2026 | 21 Oct 2026 | 2 | 15 Oct 2026 | Urgent |
| Daily SIM | Jio | 1 | 84 d | 26 Sep 2026 | 19 Dec 2026 | 61 | 19 Oct 2026 | Safe |
| Mummy's phone | Vi | — (family) | 28 d | 3 Oct 2026 | 31 Oct 2026 | 12 | 19 Oct 2026 | Safe |

## Navigation
Single activity, Navigation Compose. No bottom nav, no drawer.
`Onboarding → Permission → SimsDetected → SetValidity (sheet, once per SIM) → Home`
`Home → SimDetail(simId)`, `Home → Settings`, `Home → AddSim`.
First launch decides start destination from DataStore flag `onboardingDone`.

---

## 1–3. Onboarding (pager, 3 pages)
- Top: logo tile + "SIM Keeper", "Skip" text link (goes to Permission).
- Illustration area (two SIM cards + ring, vector), then title + body, page dots, "Next →" button.
- Page 1: "Never lose your number again" / "Your bank OTPs, UPI and WhatsApp depend on that second SIM. SIM Keeper makes sure it never gets switched off."
- Page 2: "Every SIM, one glance" / "See how many days each SIM has left, and get a reminder 7, 3 and 1 day before it runs out."
- Page 3: "Private by design" / "Works 100% offline. No login, no account. Your data never leaves your phone." Button: "Get started".
- No sign-in link anywhere.

## 4. Permission
- Title "One small permission", body explains: read the SIM carrier name and slot only, to set things up automatically.
- Info card: lock icon, "Read SIM carrier name", "Used only to identify your SIMs. Nothing else is accessed."
- Buttons: "Allow" (system `READ_PHONE_STATE` dialog), text link "Add SIMs by hand instead".
- Denied → go to AddSim manual flow. Never ask again automatically.
- On Android 13+, ask `POST_NOTIFICATIONS` later, right after the first SIM is saved, with a one-line reason.

## 5. SIMs detected
- Success line "2 SIMs detected" with check icon (or "1 SIM detected").
- One card per active subscription: MonogramTile, carrier name, "SIM slot N", "Auto-detected" tag, name chips: Daily SIM / Bank SIM / Work / Custom… (Custom opens a text field). Defaults: slot 1 = Daily SIM, slot 2 = Bank SIM.
- Dashed button "Track a family member's SIM" (opens AddSim with family = true).
- Primary "Continue" → SetValidity for each SIM in turn. Link "Add a SIM by hand".

## 6. Set validity (modal bottom sheet)
- Header: MonogramTile, SIM name, "Airtel · slot 2", step "1 of 2" if several.
- "How long is your plan?" → PlanChip grid: 28 / 56 / 84 / 365, plus "Custom" chip (number input).
- "When did you last recharge?" → chips: Today / 1 week ago / 3 weeks ago / 1 month ago / "Pick date" (Material date picker, max = today).
- Alternative toggle link: "I know the exact end date" → date picker for end date.
- Live preview card in status container colour: "Plan ends on 21 Oct 2026" + big number "2" + "days left". The preview must always match the selection (28 days + Today = ends 16 Nov 2026, 28 days left).
- "When did you last use this SIM?" collapsed row, default Today.
- Primary "Save" (last SIM: "Save and finish" → Home).

## 7. Home
- Top: "Mon, 19 Oct" small, "Your SIMs" headline, settings icon button.
- StatusBanner: urgent/soon count → "1 SIM needs a recharge soon" (urgent colours) or "All your SIMs are safe" (safe colours).
- Section "YOUR SIMS": SimCards for SIMs in this phone, sorted by days left ascending.
- Urgent/soon card footer: "Cheapest plan to stay active · ₹[price] · 28 days" + **"I recharged"** button. (Not "Add a SIM".)
- Section "FAMILY": family SIM cards (no slot shown; line reads "Family SIM · ends 31 Oct 2026").
- Extended FAB bottom-right: "+ Add SIM" → AddSim.
- "I recharged" tapped → confirmation flow (screen 9), then card turns safe; snackbar "Bank SIM is safe till 16 Nov" with Undo.
- Ring animates on first appear.

## 8. SIM detail
- Top bar: back, SIM name + carrier tag, edit icon (opens SetValidity sheet in edit mode; overflow has "Delete SIM" with confirm dialog).
- Hero card: DaysRing 120 + "Recharge by Wed, 21 Oct" + one calm consequence line: "After that, outgoing calls and data stop. Your OTPs may stop soon after."
- Two tiles: "PLAN ENDS 21 Oct 2026 · 28-day plan" and "90-DAY NO-USE LIMIT 13 Jan 2027 · I used it today" (button updates lastUsed = today; label becomes "Updated").
- Cheapest plan card: "₹[price] · 28 days", "Prices as of [month year]", carrier name. Tapping it shows a sheet with up to 3 cheapest plans from JSON. No buy button.
- "Remind me" chips: 7 days / 3 days / 1 day / On the day (toggle, persisted per SIM).
- "History": recharge rows (date, plan length, ₹ amount or "—"), newest first, "See all" if > 3.
- Sticky primary "I recharged".

## 9. Recharged confirmation (full-screen dialog)
- Ring animates to full in safe colour with "28 days" inside (not "100%").
- "Recharged!" + "Your Airtel SIM is safe for 28 more days."
- Card "New plan ends on 16 Nov 2026".
- Optional fields: amount paid (₹, numeric), plan length chips (prefilled with last).
- Buttons: "Done" (primary), "Undo" (text).

## 10. Settings
- Share card (primary bg): "Check your parents' SIMs" / "Share SIM Keeper on WhatsApp" → `ACTION_SEND` with Play Store link, package `com.whatsapp` if installed, else chooser.
- APPEARANCE: Theme segmented (System / Light / Dark), Dynamic colour switch (Android 12+ only), Language (English / हिन्दी / मराठी via per-app language API).
- REMINDERS: Reminder time (default 10:00 AM, time picker), Quiet hours switch (10 PM–8 AM, default on).
- YOUR DATA: Back up to a file, Restore from a file (v1.1: show "Coming soon" disabled rows in v1).
- Footer: lock icon "Your data never leaves this phone", "Carrier rules last checked <date from JSON> · Version x.y".
- No log out, no account, no support banner.

## 11. Notification
- Channel "Recharge reminders" (high importance for 1-day and on-day, default otherwise).
- Title: "Bank SIM (Airtel): 2 days left". Body: "Recharge by 21 Oct to keep your OTPs working." Calm, no "deactivated" wording before expiry.
- Actions: "I recharged" (opens confirmation), "Remind me tomorrow" (snooze 24 h). Tap → SimDetail.

## 12. Widgets (Glance)
- 4×2 "My SIMs": header + up to 3 tiles (days number in status colour, carrier, SIM name). Tap tile → SimDetail.
- 2×2 single SIM: DaysRing 104 + number + "days" + SIM name. Configuration activity picks the SIM.
- Update via WorkManager daily and after any data change.

## 13. Empty state
- Illustration, "No SIMs added yet", "Add your first SIM to start tracking expiry dates and reminders.", primary "Add your first SIM".

## 14. Add SIM (manual)
- Fields: Carrier (dropdown: Jio, Airtel, Vi, BSNL, Other), Name, "This SIM is in someone else's phone" switch (family) with Owner name field, then the SetValidity content inline. No phone number field.

## Dark mode
Every screen must have a dark version using the dark tokens. Provide `@Preview(uiMode = UI_MODE_NIGHT_YES)` for Home, SimDetail and Settings.

## Fixes carried over from the image review (do not repeat these mistakes)
1. No garbled dates or labels: all text comes from strings and real formatting.
2. Light SIM detail exists (screen 8) and dark detail is its own screen.
3. Home urgent footer button says "I recharged", not "Add a SIM".
4. Family SIMs appear only under FAMILY and never show a slot.
5. Set validity preview always matches the selected chips.
6. Confirmation ring shows "28 days", not "100%".
7. Notification copy is calm: "Recharge by 21 Oct to keep your OTPs working."

# PRD — SIM Keeper v1

Full editable version: https://claude.ai/code/artifact/7dc1328c-8392-4526-89bf-6b421bae58ec

## One line
An offline Android app that stops people in India from losing a phone number because they forgot to recharge it.

## Problem
Most Indian phones hold two SIMs. One is used daily; the second is kept for bank OTPs, UPI, Aadhaar or an old WhatsApp and is forgotten. If it goes too long without a recharge or without use, the carrier cuts service and can eventually reissue the number. Losing it locks people out of bank and UPI accounts.

## Gap
iPhone has KeepSim and SimAlive. On Android there is no dedicated app, only generic expiry reminders and carrier apps that show one carrier each.

## Users
| User | Situation | Need |
| --- | --- | --- |
| Student / young professional | Daily Jio SIM + old Airtel SIM linked to bank | Reminder before the Airtel SIM lapses |
| Parent / grandparent | Two SIMs, doesn't track dates | A family member sets it up for them |
| Worker away from home | Home-state SIM kept for family and bank | Cheapest plan to keep it alive |
| Freelancer / shop owner | Business + personal SIMs | Both countdowns on one screen |

## Goals
- Setup in under 60 seconds from install to seeing both countdowns.
- Every SIM shows a correct deadline and gets a reminder before it.
- Polished enough for a 4.5+ Play Store rating.
- Grow through Play Store search ("SIM validity", "recharge reminder") and family sharing.

## Non-goals (v1)
Recharging or payments, reading SMS/call logs, accounts, cloud sync, servers, analytics, AI.

## v1 features
| Feature | Priority |
| --- | --- |
| SIM detection (carrier + slot via `SubscriptionManager`), manual add fallback | Must |
| Validity setup: last recharge date + plan length (28/56/84/365/custom) or exact end date | Must |
| Home: one card per SIM with ring, days left, end date, urgency colour | Must |
| Two deadlines: plan expiry and 90-day no-use limit; "I used it today" | Must |
| Reminders: 7, 3, 1 days before and on the day (per-SIM toggles) | Must |
| "I recharged" one-tap restart with Undo | Must |
| Cheapest plan card from bundled JSON | Must |
| Home screen widgets (4×2 all SIMs, 2×2 single SIM) | Must |
| Light / dark / system theme, Material You on Android 12+ | Must |
| Recharge history per SIM | Should |
| Family SIMs (tracked SIMs not in this phone) | Should |
| Share "Check your parents' SIMs" on WhatsApp | Should |
| Hindi and Marathi strings | Should |
| Backup / restore to a local JSON file | Later (v1.1) |

## Success metrics
| Metric | Target |
| --- | --- |
| Setup completion | 80% of testers reach Home with ≥1 SIM |
| Day-30 retention | 35% (Play Console) |
| Rating | ≥ 4.5 |

## Risks
| Risk | Fallback |
| --- | --- |
| Carrier rules wrong or changed | Rules in JSON with "last checked" date; user can always set an exact end date |
| Xiaomi/Oppo/Vivo kill background work | Daily WorkManager check + one-time "allow background" guide |
| Permission denied | Manual add flow |
| Plan prices change | Show "Prices as of <month>", update per release |

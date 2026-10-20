# CARRIER_RULES.md — SIM Keeper

> **VERIFY BEFORE LAUNCH.** These figures come from news reports and a forum repost, not TRAI's official notice or carrier pages. Check each carrier's support page and update `lastChecked` before every release.

## What we know (as reported, Jan 2025)
- A prepaid SIM with no calls, SMS or data for **90 days** can be deactivated and its number reissued. If the balance is at least **₹20**, that amount may be deducted to extend activation by **30 days**. Source: [Business Standard](https://www.business-standard.com/technology/tech-news/trai-new-rules-jio-airtel-vi-bsnl-sim-to-stop-working-after-this-period-nc-125012001030_1.html)
- Carrier grace periods after plan expiry (source: [forum repost](https://community.iqoo.com/in/thread/80206), unverified):

| Carrier | Reported behaviour |
| --- | --- |
| Jio | Stays active about 90 days without recharge, then needs a reactivation plan |
| Airtel | 15-day grace after the 90 days, then the number is closed |
| Vi | 90-day grace, then a minimum ₹49 plan to reactivate |
| BSNL | Up to 180 days |

## How the app uses this
- The app only **informs**. Deadlines are computed from what the user entered (`DATA_MODEL.md`). Grace info is shown as a one-line note on SIM detail after expiry, e.g. "Airtel usually gives 15 more days after this. Recharge soon to keep your number."
- The 90-day no-use rule is applied to every carrier (constant `NO_USE_LIMIT_DAYS = 90`).

## assets/carrier_rules.json
```json
{
  "lastChecked": "2026-10-19",
  "noUseLimitDays": 90,
  "carriers": {
    "JIO":    { "graceNote": "Jio usually keeps the number for about 90 days without a recharge.", "verified": false },
    "AIRTEL": { "graceNote": "Airtel usually gives 15 more days after the 90-day period.",        "verified": false },
    "VI":     { "graceNote": "Vi usually allows 90 days, then needs at least a ₹49 plan.",         "verified": false },
    "BSNL":   { "graceNote": "BSNL usually allows up to 180 days.",                                "verified": false },
    "OTHER":  { "graceNote": "Check with your carrier how long you have after expiry.",           "verified": false }
  }
}
```

## assets/carrier_plans.json
Fill in real prices from each carrier's website before release. Until then use `null` and the UI shows "₹—" with "Check your carrier app".
```json
{
  "pricesAsOf": "2026-10",
  "plans": [
    { "carrier": "JIO",    "rupees": null, "days": 28, "note": "Cheapest plan with validity" },
    { "carrier": "AIRTEL", "rupees": null, "days": 28, "note": "Cheapest plan with validity" },
    { "carrier": "VI",     "rupees": null, "days": 28, "note": "Cheapest plan with validity" },
    { "carrier": "BSNL",   "rupees": null, "days": 28, "note": "Cheapest plan with validity" }
  ]
}
```
"Cheapest plan" = lowest `rupees` for that carrier among non-null entries. Never fetch prices from the internet.

## Legal note
Carrier names appear as plain text only. No logos, no wording that suggests the app is official or endorsed by any carrier.

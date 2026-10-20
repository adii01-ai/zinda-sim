# PLAYSTORE_RELEASE.md — SIM Keeper

## Before the first upload
- [ ] Final `applicationId` chosen (e.g. `in.yourname.simkeeper`). It can never change after publishing.
- [ ] `versionCode` 1, `versionName` "1.0.0". Increase `versionCode` on every upload.
- [ ] Release build: `isMinifyEnabled = true`, `isShrinkResources = true`, ProGuard rules for kotlinx.serialization and Room checked.
- [ ] Create an upload keystore (keep it safe, never commit it):
  `keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`
- [ ] Signing config reads passwords from `~/.gradle/gradle.properties` or environment variables, not from the repo.
- [ ] Build the bundle: `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
- [ ] Use Play App Signing (default).

## Play Console account
- [ ] Google Play developer account (one-time US$25 fee), identity verified.
- [ ] New personal accounts must run a **closed test with at least 12 testers opted in for 14 days** before applying for production access. Recruit testers early (classmates, family).

## Store listing
- Title: "SIM Keeper: SIM Validity Alert" (≤ 30 chars) — or the final name.
- Short description (≤ 80): "Never lose your number. Recharge reminders for every SIM, works offline."
- Full description: problem, how it works (3 steps), features list, privacy ("No login. No internet. Your data never leaves your phone."), note that it works with all Indian carriers and is not affiliated with any carrier.
- Graphics: 512×512 icon, 1024×500 feature graphic, 8 phone screenshots (1080×1920 or larger) telling the story: problem → home → detail → reminder → widget → privacy → family → dark mode. Optional 30 s video.
- Category: Tools. Tags: SIM, recharge reminder, dual SIM.

## Policy forms
- [ ] Privacy policy URL (a simple GitHub Pages page): no data collected, no data shared, what the phone-state permission is used for.
- [ ] Data safety: "No data collected", "No data shared".
- [ ] Permissions declaration: `READ_PHONE_STATE` used only to read SIM carrier name and slot for setup.
- [ ] Content rating questionnaire (Everyone). Target audience 18+ (avoids Families policy).
- [ ] Ads: "No ads".
- [ ] Not a government or carrier app; no carrier logos or official-looking claims.

## Launch
- [ ] Closed test → fix reported issues → production with staged rollout 20% → 100% after 3 days without crashes.
- [ ] Watch Android vitals (crash rate < 1%, ANR < 0.5%).
- [ ] Reply to every review in the first month.
- [ ] Growth: WhatsApp family share, Reels/Shorts "Your bank SIM can be deactivated after 90 days", r/india post.

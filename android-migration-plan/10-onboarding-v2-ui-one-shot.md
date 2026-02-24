# Android Onboarding V2 One-Shot UI Migration Plan

## Context
- iOS (`Screentox`) has a mature onboarding flow with polished transitions and a custom design system.
- Android currently has a minimal permission-only onboarding.
- Goal: port iOS onboarding UX into Android Compose with strong visual/motion parity.
- Constraint: **frontend/UI only** for this pass. No backend parity work.
- Architecture requirement: do **not** use the iOS "3 screens rendered at once" pattern.

## Scope

### In Scope
- Rebuild onboarding UI flow in `ui/onboarding/v2` using Compose.
- Match iOS transition direction and timing curves.
- Port/adapt onboarding screens listed below.
- Persist onboarding progress and local answers in `PreferencesManager` (DataStore).
- Integrate into app entry: show onboarding for first-time users, then route to existing main app.

### Out of Scope
- Analytics instrumentation (Mixpanel or other analytics).
- Experiment infra and runtime experiment toggles.
- Backend submission/remote persistence for onboarding answers.
- Subscription/paywall behavior parity.
- Auth backend integration changes.
- Walkthrough sequence after onboarding.

## Screens to Port (17 of 18 total flow entries; 1 skipped + 1 fixed variant)

### Port / Adapt List (17)
1. Welcome
2. UserWhy
3. UserHow
4. UserWhat
5. Question (Age)
6. Question (Screen Time Estimate)
7. Loading
8. ShockPage1
9. ShockPage2
10. Rating
11. PermissionsSetup (adapt existing Android 3-permission flow into V2 UI shell)
12. NotificationPermission
13. SevenDayChallenge (fixed path, no experiment toggle)
14. PostPaywallMessage (UI placeholder only, Continue action)
15. ChooseUsername (local validation + local persistence only)
16. SelectApps (adapt existing Android picker components)
17. Acquisition

### Skipped / Fixed in This Pass
- Skip: ShareAcrossDevice (iOS-specific)
- Fixed variant: always use SevenDayChallenge path (do not branch to ThisWeek)

## Architecture

### Navigation + Animation (Single Active Screen)
- Keep only one active screen composable in memory.
- Use a coroutine-driven `Animatable<Float>` offset normalized to `[-1, +1]`.
- Forward navigation:
1. Animate current from `0 -> -1` (current exits left).
2. Swap active screen index.
3. Set offset to `+1`.
4. Animate `+1 -> 0` (new enters from right).
- Back navigation:
1. Animate current from `0 -> +1` (current exits right).
2. Swap active screen index.
3. Set offset to `-1`.
4. Animate `-1 -> 0` (new enters from left).

### Timing Curves (Match iOS)
- `introIn`: 580ms, cubic(0.3, 0.0, 0.2, 1.0)
- `introOut`: 460ms, cubic(0.3, 0.0, 0.2, 1.0)
- `drawerIn`: 570ms, cubic(0.8, 0.0, 0.5, 1.05) for question screens
- `drawerOut`: 430ms, cubic(0.5, -0.2, 0.6, 1.1) for question screens

### State Model
- One Hilt-injected `OnboardingViewModel` manages:
  - `currentIndex`
  - `navigationIndex` (target screen during transition)
  - `offset`
  - `isAnimating` (blocks interactions while transitioning)
  - `answers` (local answer map)
  - persistence/resume via `PreferencesManager`

## Design System
- Font: copy `FFF-AcidGrotesk-Soft-VF.ttf` into `res/font`.
- Colors:
  - Reuse existing app tokens where valid.
  - Add onboarding-specific tokens for gradients and neutrals.
- Typography:
  - Add `OnboardingTypography` mapped to iOS scale (`title2=40sp`, `h1=32sp`, `h2=24sp`, `p1=20sp`, etc).
- Shared components:
  - `OnboardingContinueButton`
  - `OnboardingProgressBar`
  - `OnboardingBackgroundGradient`
  - `SurveyListItem`
  - `FullButton`
  - `ReviewCard`
  - `LaurelBadge`
  - `SlideToConfirmButton`

## Proposed File Structure
`app/src/main/java/com/bepresent/android/ui/onboarding/v2/`

```text
OnboardingV2Screen.kt
OnboardingViewModel.kt
OnboardingScreenType.kt
OnboardingTokens.kt
animation/
  OnboardingAnimations.kt
components/
  OnboardingContinueButton.kt
  OnboardingProgressBar.kt
  OnboardingBackgroundGradient.kt
  SurveyListItem.kt
  ReviewCard.kt
  LaurelBadge.kt
  SlideToConfirmButton.kt
screens/
  WelcomeScreen.kt
  UserWhyScreen.kt
  UserHowScreen.kt
  UserWhatScreen.kt
  QuestionScreen.kt
  LoadingScreen.kt
  ShockPage1Screen.kt
  ShockPage2Screen.kt
  RatingScreen.kt
  PermissionsSetupScreen.kt
  NotificationPermissionScreen.kt
  SevenDayChallengeScreen.kt
  PostPaywallMessageScreen.kt
  ChooseUsernameScreen.kt
  AppPickerOnboardingScreen.kt
  AcquisitionScreen.kt
util/
  GetYears.kt
```

## Assets to Copy

### Drawables (`android/app/src/main/res/drawable/`)
Source root:
`screentox/swift/screentox/Screentox/Core/UI/Assets.xcassets/`

- `user_why_phone.png` from `user-why-phonev2.imageset`
- `user_how_phone.png` from `user-how-phonev2.imageset`
- `left_laurel.png` from `left-laurel.imageset`
- `right_laurel.png` from `right-laurel.imageset`
- `five_stars.png` from `5-stars.imageset`
- `jack_and_charles.png` from `jack-and-charles.imageset`
- `blue_check.png` from `blue-check.imageset`
- `notifications_mask.png` from `notifications-opacity-mask.imageset`

### Raw animation assets (`android/app/src/main/res/raw/`)
- `exploding_head.json` from `Features/Onboarding/V2/.../ShockPage/exploding-head-json.json`
- `confetti_animation.lottie` from `Features/Onboarding/V2/.../Confetti/confetti-animation.lottie`

### Font (`android/app/src/main/res/font/`)
- `fff_acid_grotesk_soft_vf.ttf` from:
  `screentox/swift/screentox/Screentox/Fonts/FFF-AcidGrotesk-Soft-VF.ttf`

## Existing Files to Modify
- `app/build.gradle.kts`
  - add `com.google.android.play:review-ktx`
  - add `com.airbnb.android:lottie-compose`
- `app/src/main/java/com/bepresent/android/data/datastore/PreferencesManager.kt`
  - add onboarding V2 progress and answers keys
- `app/src/main/java/com/bepresent/android/MainActivity.kt`
  - switch onboarding entry from old `OnboardingScreen` to `OnboardingV2Screen`

## Implementation Order

### Phase 1: Foundation
1. Add dependencies (`review-ktx`, `lottie-compose`).
2. Copy font and create `FontFamily`.
3. Create `OnboardingScreenType` sealed class + metadata helpers.
4. Create tokens and typography for onboarding.
5. Create animation constants/easing.
6. Build `OnboardingViewModel` (state, navigation, persistence contract).
7. Build `OnboardingV2Screen` host with offset-driven transition pipeline.
8. Extend `PreferencesManager` for V2 keys.

### Phase 2: Shared UI Components
1. Continue button, progress bar, background gradient.
2. List item, review card, laurel badge.
3. Slide-to-confirm component.

### Phase 3: Screen Buildout
1. Build all 17 scoped screens.
2. Reuse `QuestionScreen` for age and screen-time variants.
3. Adapt permissions and app picker screens to existing Android capabilities.
4. Keep paywall/auth side effects as UI-only placeholders.

### Phase 4: Integration and Polish
1. Wire onboarding V2 into `MainActivity`.
2. Final asset plumbing and naming consistency.
3. Tune spacing, typography, and animation feel.
4. Edge-case handling (double taps, back spam, lifecycle resume).

## Verification Checklist
1. Project builds with no compile errors.
2. Fresh install shows onboarding V2 entry.
3. All scoped screens render and navigate correctly.
4. Forward/back transitions follow left-exit/right-enter and reverse behavior.
5. Question screens use drawer timing profile.
6. App kill mid-onboarding resumes at saved index with saved local answers.
7. Completion sets onboarding flag and skips onboarding on relaunch.
8. Back navigation behavior is consistent and does not break state.

## Definition of Done
- Android onboarding V2 is fully wired and replaces old onboarding entry.
- All 17 scoped screens are present with iOS-style visual/motion parity.
- No backend logic is required for onboarding completion.
- Progress persistence, resume, and completion behavior are stable.

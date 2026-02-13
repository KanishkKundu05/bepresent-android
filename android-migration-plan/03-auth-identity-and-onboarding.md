# Android Migration Plan: Auth, Identity, and Onboarding

## Scope
Migrate sign-in, credential persistence, and onboarding progression logic to Android equivalents.

## iOS Source of Truth

- Auth manager: `swift/Screentox/Screentox/Core/Auth/Authentication.swift`
- Keychain: `swift/Screentox/Screentox/Core/Storage/KeyChainService.swift`
- Startup/keychain reconciliation: `swift/Screentox/Screentox/Screens/SplashView.swift`
- Onboarding auth flows:
  - `swift/Screentox/Screentox/Features/Onboarding/V2/View/Screens/Onboarding/UserWhat/AuthViewModel.swift`
  - `swift/Screentox/Screentox/Features/Onboarding/V2/View/Screens/Onboarding/RegularAuth/RegularAuthViewModel.swift`
  - `swift/Screentox/Screentox/Features/Onboarding/V2/View/Screens/Onboarding/ChooseUsername/ChooseUsernameViewModel.swift`
- Server endpoints: `server/routes/createAccount.ts`, `server/routes/accounts.ts`, `server/routes/authenticate.ts`

## Required Android Capabilities

- Email-based account creation/sign-in path.
- Google sign-in path.
- Apple sign-in fallback strategy (web OAuth flow if required by product policy).
- Secure local storage for token + serialized user.
- Profile completion (username) and first-run onboarding state.

## Android Implementation

- Use Credential Manager + Google Identity APIs for Google auth.
- Store token in EncryptedSharedPreferences with Android Keystore-backed master key.
- Persist user profile and onboarding flags in DataStore.
- Create `AuthRepository` that wraps:
  - `/createAccount/apple` equivalent requests
  - username update calls
  - account delete and logout cleanup

## Migration Tasks

1. Implement secure token/user storage abstraction.
2. Build auth screen state machine:
   - unauthenticated
   - loading
   - authenticated
   - profile-incomplete
3. Port onboarding gate logic from AppStorage keys to DataStore keys.
4. Implement logout cleanup parity:
   - clear secure credentials
   - clear app-intention/session local state
   - reset analytics identity
5. Add integration tests for:
   - new sign-in
   - returning user resume
   - logout/login switch user behavior

## Identity and Tracking Side Effects

On sign-in, preserve behavior parity for:

- analytics identify
- paywall/subscription identity
- attribution SDK user IDs

This behavior is currently orchestrated in `ScreentoxApp.setAppState()` and view `onAppear` blocks.

## Risks

- Mixed auth methods can create duplicate users if provider IDs and email linking are inconsistent.
- Overlapping old/new onboarding flags can regress returning-user flow.

## Mitigations

- Add server-side linking checks in QA environment for Google/email collisions.
- Use one migration function to map old onboarding flags to Android equivalents.

## Definition of Done

- User can sign in, persist session, complete onboarding, set username, and resume without re-auth.
- Logout fully clears secure state and resets SDK identities.

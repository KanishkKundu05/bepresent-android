# UI Module

Jetpack Compose UI layer following MVVM architecture with Hilt dependency injection.

## Directory Structure

```
ui/
├── theme/              # Material 3 theming
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
├── components/         # Reusable composables
│   ├── ScreenTimeCard.kt
│   ├── IntentionCard.kt
│   ├── IntentionRow.kt
│   └── SessionCta.kt
├── dashboard/          # Main screen
│   ├── DashboardScreen.kt
│   └── DashboardViewModel.kt
├── session/            # Session configuration
│   └── SessionConfigSheet.kt
├── intention/          # Intention configuration
│   └── IntentionConfigSheet.kt
├── picker/             # App selection
│   └── AppPickerSheet.kt
├── onboarding/         # First-run experience
│   ├── OnboardingScreen.kt
│   └── OnboardingEntryPoint.kt
├── profile/            # User profile
│   ├── ProfileScreen.kt
│   └── ProfileViewModel.kt
├── partner/            # Accountability partner
│   ├── PartnerScreen.kt
│   ├── PartnerViewModel.kt
│   └── AddPartnerSheet.kt
├── leaderboard/        # Social leaderboard
│   ├── LeaderboardScreen.kt
│   └── LeaderboardViewModel.kt
└── auth/               # Authentication
    └── AuthViewModel.kt
```

## Architecture

### MVVM Pattern
- **Screen**: Stateless composable receiving UI state and callbacks
- **ViewModel**: Holds UI state as `StateFlow`, exposes actions, injects managers
- **Managers**: Feature layer classes handling business logic

### State Management
ViewModels combine multiple flows into a single `UiState` data class:

```kotlin
val uiState: StateFlow<DashboardUiState> = combine(
    screenTime,
    intentions,
    activeSession,
    ...
) { ... }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
```

## Key Screens

### DashboardScreen
Main hub showing:
- Permission status banner
- Screen time card (total + per-app breakdown)
- Active session card with controls (cancel/give up/complete)
- Intention row (horizontal scroll of tracked apps)
- Session start CTA

Sheet flows:
1. Add Intention: AppPickerSheet → IntentionConfigSheet
2. Edit Intention: IntentionConfigSheet (with delete option)
3. Start Session: SessionConfigSheet → AppPickerSheet (for blocked apps)

### OnboardingScreen
Permission request flow for:
1. Usage Access (critical)
2. Notifications (recommended)
3. Battery Optimization (recommended, with OEM-specific instructions)

### ProfileScreen
User stats and settings:
- Total XP, coins, streaks
- Auth state (login/logout via Auth0)
- Sync status

### LeaderboardScreen
Social comparison:
- Friends ranking by XP
- Weekly/all-time views
- Data fetched from Convex

## Theming

Material 3 with:
- Dynamic color support (Android 12+)
- Light/dark mode auto-switching
- Custom color palette defined in `Color.kt`
- Edge-to-edge display with status bar tinting

## Sheet Pattern

Bottom sheets use `ModalBottomSheet` with:
- `onDismiss` callback for cancellation
- Action callbacks (`onSave`, `onDelete`, etc.)
- Local state for form fields
- Parent screen manages sheet visibility via `remember { mutableStateOf(false) }`

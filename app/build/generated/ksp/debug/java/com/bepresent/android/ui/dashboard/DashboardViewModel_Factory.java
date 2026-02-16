package com.bepresent.android.ui.dashboard;

import com.bepresent.android.data.datastore.PreferencesManager;
import com.bepresent.android.data.usage.UsageStatsRepository;
import com.bepresent.android.features.intentions.IntentionManager;
import com.bepresent.android.features.sessions.SessionManager;
import com.bepresent.android.permissions.PermissionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<UsageStatsRepository> usageStatsRepositoryProvider;

  private final Provider<IntentionManager> intentionManagerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  private final Provider<PermissionManager> permissionManagerProvider;

  public DashboardViewModel_Factory(Provider<UsageStatsRepository> usageStatsRepositoryProvider,
      Provider<IntentionManager> intentionManagerProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<PermissionManager> permissionManagerProvider) {
    this.usageStatsRepositoryProvider = usageStatsRepositoryProvider;
    this.intentionManagerProvider = intentionManagerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
    this.permissionManagerProvider = permissionManagerProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(usageStatsRepositoryProvider.get(), intentionManagerProvider.get(), sessionManagerProvider.get(), preferencesManagerProvider.get(), permissionManagerProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<UsageStatsRepository> usageStatsRepositoryProvider,
      Provider<IntentionManager> intentionManagerProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<PreferencesManager> preferencesManagerProvider,
      Provider<PermissionManager> permissionManagerProvider) {
    return new DashboardViewModel_Factory(usageStatsRepositoryProvider, intentionManagerProvider, sessionManagerProvider, preferencesManagerProvider, permissionManagerProvider);
  }

  public static DashboardViewModel newInstance(UsageStatsRepository usageStatsRepository,
      IntentionManager intentionManager, SessionManager sessionManager,
      PreferencesManager preferencesManager, PermissionManager permissionManager) {
    return new DashboardViewModel(usageStatsRepository, intentionManager, sessionManager, preferencesManager, permissionManager);
  }
}

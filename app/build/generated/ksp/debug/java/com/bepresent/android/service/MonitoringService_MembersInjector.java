package com.bepresent.android.service;

import com.bepresent.android.data.db.AppIntentionDao;
import com.bepresent.android.data.db.PresentSessionDao;
import com.bepresent.android.data.usage.UsageStatsRepository;
import com.bepresent.android.features.sessions.SessionManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MonitoringService_MembersInjector implements MembersInjector<MonitoringService> {
  private final Provider<UsageStatsRepository> usageStatsRepositoryProvider;

  private final Provider<AppIntentionDao> intentionDaoProvider;

  private final Provider<PresentSessionDao> sessionDaoProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public MonitoringService_MembersInjector(
      Provider<UsageStatsRepository> usageStatsRepositoryProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PresentSessionDao> sessionDaoProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.usageStatsRepositoryProvider = usageStatsRepositoryProvider;
    this.intentionDaoProvider = intentionDaoProvider;
    this.sessionDaoProvider = sessionDaoProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  public static MembersInjector<MonitoringService> create(
      Provider<UsageStatsRepository> usageStatsRepositoryProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PresentSessionDao> sessionDaoProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new MonitoringService_MembersInjector(usageStatsRepositoryProvider, intentionDaoProvider, sessionDaoProvider, sessionManagerProvider);
  }

  @Override
  public void injectMembers(MonitoringService instance) {
    injectUsageStatsRepository(instance, usageStatsRepositoryProvider.get());
    injectIntentionDao(instance, intentionDaoProvider.get());
    injectSessionDao(instance, sessionDaoProvider.get());
    injectSessionManager(instance, sessionManagerProvider.get());
  }

  @InjectedFieldSignature("com.bepresent.android.service.MonitoringService.usageStatsRepository")
  public static void injectUsageStatsRepository(MonitoringService instance,
      UsageStatsRepository usageStatsRepository) {
    instance.usageStatsRepository = usageStatsRepository;
  }

  @InjectedFieldSignature("com.bepresent.android.service.MonitoringService.intentionDao")
  public static void injectIntentionDao(MonitoringService instance, AppIntentionDao intentionDao) {
    instance.intentionDao = intentionDao;
  }

  @InjectedFieldSignature("com.bepresent.android.service.MonitoringService.sessionDao")
  public static void injectSessionDao(MonitoringService instance, PresentSessionDao sessionDao) {
    instance.sessionDao = sessionDao;
  }

  @InjectedFieldSignature("com.bepresent.android.service.MonitoringService.sessionManager")
  public static void injectSessionManager(MonitoringService instance,
      SessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }
}

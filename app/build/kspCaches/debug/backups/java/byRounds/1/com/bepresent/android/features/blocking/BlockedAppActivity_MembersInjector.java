package com.bepresent.android.features.blocking;

import com.bepresent.android.data.datastore.PreferencesManager;
import com.bepresent.android.data.db.AppIntentionDao;
import com.bepresent.android.data.db.PresentSessionDao;
import com.bepresent.android.features.intentions.IntentionManager;
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
public final class BlockedAppActivity_MembersInjector implements MembersInjector<BlockedAppActivity> {
  private final Provider<IntentionManager> intentionManagerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<AppIntentionDao> intentionDaoProvider;

  private final Provider<PresentSessionDao> sessionDaoProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public BlockedAppActivity_MembersInjector(Provider<IntentionManager> intentionManagerProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PresentSessionDao> sessionDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.intentionManagerProvider = intentionManagerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.intentionDaoProvider = intentionDaoProvider;
    this.sessionDaoProvider = sessionDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public static MembersInjector<BlockedAppActivity> create(
      Provider<IntentionManager> intentionManagerProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PresentSessionDao> sessionDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new BlockedAppActivity_MembersInjector(intentionManagerProvider, sessionManagerProvider, intentionDaoProvider, sessionDaoProvider, preferencesManagerProvider);
  }

  @Override
  public void injectMembers(BlockedAppActivity instance) {
    injectIntentionManager(instance, intentionManagerProvider.get());
    injectSessionManager(instance, sessionManagerProvider.get());
    injectIntentionDao(instance, intentionDaoProvider.get());
    injectSessionDao(instance, sessionDaoProvider.get());
    injectPreferencesManager(instance, preferencesManagerProvider.get());
  }

  @InjectedFieldSignature("com.bepresent.android.features.blocking.BlockedAppActivity.intentionManager")
  public static void injectIntentionManager(BlockedAppActivity instance,
      IntentionManager intentionManager) {
    instance.intentionManager = intentionManager;
  }

  @InjectedFieldSignature("com.bepresent.android.features.blocking.BlockedAppActivity.sessionManager")
  public static void injectSessionManager(BlockedAppActivity instance,
      SessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }

  @InjectedFieldSignature("com.bepresent.android.features.blocking.BlockedAppActivity.intentionDao")
  public static void injectIntentionDao(BlockedAppActivity instance, AppIntentionDao intentionDao) {
    instance.intentionDao = intentionDao;
  }

  @InjectedFieldSignature("com.bepresent.android.features.blocking.BlockedAppActivity.sessionDao")
  public static void injectSessionDao(BlockedAppActivity instance, PresentSessionDao sessionDao) {
    instance.sessionDao = sessionDao;
  }

  @InjectedFieldSignature("com.bepresent.android.features.blocking.BlockedAppActivity.preferencesManager")
  public static void injectPreferencesManager(BlockedAppActivity instance,
      PreferencesManager preferencesManager) {
    instance.preferencesManager = preferencesManager;
  }
}

package com.bepresent.android.features.sessions;

import android.content.Context;
import com.bepresent.android.data.datastore.PreferencesManager;
import com.bepresent.android.data.db.AppIntentionDao;
import com.bepresent.android.data.db.PresentSessionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<PresentSessionDao> sessionDaoProvider;

  private final Provider<AppIntentionDao> intentionDaoProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public SessionManager_Factory(Provider<Context> contextProvider,
      Provider<PresentSessionDao> sessionDaoProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.contextProvider = contextProvider;
    this.sessionDaoProvider = sessionDaoProvider;
    this.intentionDaoProvider = intentionDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(contextProvider.get(), sessionDaoProvider.get(), intentionDaoProvider.get(), preferencesManagerProvider.get());
  }

  public static SessionManager_Factory create(Provider<Context> contextProvider,
      Provider<PresentSessionDao> sessionDaoProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new SessionManager_Factory(contextProvider, sessionDaoProvider, intentionDaoProvider, preferencesManagerProvider);
  }

  public static SessionManager newInstance(Context context, PresentSessionDao sessionDao,
      AppIntentionDao intentionDao, PreferencesManager preferencesManager) {
    return new SessionManager(context, sessionDao, intentionDao, preferencesManager);
  }
}

package com.bepresent.android.features.intentions;

import android.content.Context;
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
public final class IntentionManager_Factory implements Factory<IntentionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<AppIntentionDao> intentionDaoProvider;

  private final Provider<PresentSessionDao> sessionDaoProvider;

  public IntentionManager_Factory(Provider<Context> contextProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PresentSessionDao> sessionDaoProvider) {
    this.contextProvider = contextProvider;
    this.intentionDaoProvider = intentionDaoProvider;
    this.sessionDaoProvider = sessionDaoProvider;
  }

  @Override
  public IntentionManager get() {
    return newInstance(contextProvider.get(), intentionDaoProvider.get(), sessionDaoProvider.get());
  }

  public static IntentionManager_Factory create(Provider<Context> contextProvider,
      Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PresentSessionDao> sessionDaoProvider) {
    return new IntentionManager_Factory(contextProvider, intentionDaoProvider, sessionDaoProvider);
  }

  public static IntentionManager newInstance(Context context, AppIntentionDao intentionDao,
      PresentSessionDao sessionDao) {
    return new IntentionManager(context, intentionDao, sessionDao);
  }
}

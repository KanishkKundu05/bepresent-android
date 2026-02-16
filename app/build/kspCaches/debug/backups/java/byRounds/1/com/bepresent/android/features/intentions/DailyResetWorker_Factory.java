package com.bepresent.android.features.intentions;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bepresent.android.data.datastore.PreferencesManager;
import com.bepresent.android.data.db.AppIntentionDao;
import dagger.internal.DaggerGenerated;
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
public final class DailyResetWorker_Factory {
  private final Provider<AppIntentionDao> intentionDaoProvider;

  private final Provider<PreferencesManager> preferencesManagerProvider;

  public DailyResetWorker_Factory(Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    this.intentionDaoProvider = intentionDaoProvider;
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  public DailyResetWorker get(Context appContext, WorkerParameters params) {
    return newInstance(appContext, params, intentionDaoProvider.get(), preferencesManagerProvider.get());
  }

  public static DailyResetWorker_Factory create(Provider<AppIntentionDao> intentionDaoProvider,
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new DailyResetWorker_Factory(intentionDaoProvider, preferencesManagerProvider);
  }

  public static DailyResetWorker newInstance(Context appContext, WorkerParameters params,
      AppIntentionDao intentionDao, PreferencesManager preferencesManager) {
    return new DailyResetWorker(appContext, params, intentionDao, preferencesManager);
  }
}

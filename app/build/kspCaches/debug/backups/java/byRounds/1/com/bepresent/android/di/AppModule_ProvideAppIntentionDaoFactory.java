package com.bepresent.android.di;

import com.bepresent.android.data.db.AppIntentionDao;
import com.bepresent.android.data.db.BePresentDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideAppIntentionDaoFactory implements Factory<AppIntentionDao> {
  private final Provider<BePresentDatabase> databaseProvider;

  public AppModule_ProvideAppIntentionDaoFactory(Provider<BePresentDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AppIntentionDao get() {
    return provideAppIntentionDao(databaseProvider.get());
  }

  public static AppModule_ProvideAppIntentionDaoFactory create(
      Provider<BePresentDatabase> databaseProvider) {
    return new AppModule_ProvideAppIntentionDaoFactory(databaseProvider);
  }

  public static AppIntentionDao provideAppIntentionDao(BePresentDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAppIntentionDao(database));
  }
}

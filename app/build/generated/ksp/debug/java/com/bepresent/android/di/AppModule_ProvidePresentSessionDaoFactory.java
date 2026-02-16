package com.bepresent.android.di;

import com.bepresent.android.data.db.BePresentDatabase;
import com.bepresent.android.data.db.PresentSessionDao;
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
public final class AppModule_ProvidePresentSessionDaoFactory implements Factory<PresentSessionDao> {
  private final Provider<BePresentDatabase> databaseProvider;

  public AppModule_ProvidePresentSessionDaoFactory(Provider<BePresentDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PresentSessionDao get() {
    return providePresentSessionDao(databaseProvider.get());
  }

  public static AppModule_ProvidePresentSessionDaoFactory create(
      Provider<BePresentDatabase> databaseProvider) {
    return new AppModule_ProvidePresentSessionDaoFactory(databaseProvider);
  }

  public static PresentSessionDao providePresentSessionDao(BePresentDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePresentSessionDao(database));
  }
}

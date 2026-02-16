package com.bepresent.android.data.usage;

import android.content.Context;
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
public final class UsageStatsRepository_Factory implements Factory<UsageStatsRepository> {
  private final Provider<Context> contextProvider;

  public UsageStatsRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UsageStatsRepository get() {
    return newInstance(contextProvider.get());
  }

  public static UsageStatsRepository_Factory create(Provider<Context> contextProvider) {
    return new UsageStatsRepository_Factory(contextProvider);
  }

  public static UsageStatsRepository newInstance(Context context) {
    return new UsageStatsRepository(context);
  }
}

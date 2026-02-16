package com.bepresent.android.features.intentions;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DailyResetWorker_AssistedFactory_Impl implements DailyResetWorker_AssistedFactory {
  private final DailyResetWorker_Factory delegateFactory;

  DailyResetWorker_AssistedFactory_Impl(DailyResetWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public DailyResetWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<DailyResetWorker_AssistedFactory> create(
      DailyResetWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DailyResetWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<DailyResetWorker_AssistedFactory> createFactoryProvider(
      DailyResetWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DailyResetWorker_AssistedFactory_Impl(delegateFactory));
  }
}

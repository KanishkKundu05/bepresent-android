package com.bepresent.android;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class BePresentApp_MembersInjector implements MembersInjector<BePresentApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public BePresentApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<BePresentApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new BePresentApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(BePresentApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.bepresent.android.BePresentApp.workerFactory")
  public static void injectWorkerFactory(BePresentApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}

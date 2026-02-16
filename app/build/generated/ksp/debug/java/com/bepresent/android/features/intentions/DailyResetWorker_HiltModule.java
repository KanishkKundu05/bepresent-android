package com.bepresent.android.features.intentions;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = DailyResetWorker.class
)
public interface DailyResetWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.bepresent.android.features.intentions.DailyResetWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(DailyResetWorker_AssistedFactory factory);
}

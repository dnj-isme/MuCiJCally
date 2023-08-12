package edu.bluejack22_2.MuCiJCally.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.adapters.LastPlayedComponentAdapter
import edu.bluejack22_2.MuCiJCally.model.factories.MainViewModelFactory
import edu.bluejack22_2.MuCiJCally.others.FirebaseMusicSource
import edu.bluejack22_2.MuCiJCally.repository.MusicRepository
import edu.bluejack22_2.MuCiJCally.utility.MusicPlayer
import edu.bluejack22_2.MuCiJCally.viewmodel.MainViewModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

   @Singleton
   @Provides
   fun provideMainViewModelFactory(
       musicServiceConnection: MusicPlayer
   ) = MainViewModelFactory(musicServiceConnection)

    @Singleton
    @Provides
    fun provideFirebaseMusicSource(musicRepository: MusicRepository) = FirebaseMusicSource(musicRepository)

    @Singleton
    @Provides
    fun provideMusicDatabase() = MusicRepository

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context)
        .setDefaultRequestOptions(
            RequestOptions().placeholder(R.drawable.mucijcally)
                .error(R.drawable.mucijcally)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )

    @Singleton
    @Provides
    fun provideLastPlayedComponentAdapter() = LastPlayedComponentAdapter()

    @Singleton
    @Provides
    fun provideMusicPlayer(
        @ApplicationContext context: Context
    ) = MusicPlayer(context)

}
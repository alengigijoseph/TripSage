package com.example.hck.di

import android.content.Context
import com.example.hck.data.ApiCalls
import com.example.hck.data.repo.MapsRepositoryImpl
import com.example.hck.domain.repo.MapsRepository
import com.example.hck.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideApi(): ApiCalls {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCalls::class.java)
    }

    @Provides
    @Singleton
    fun provideMapsRepository(api: ApiCalls):MapsRepository {
        return MapsRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

}
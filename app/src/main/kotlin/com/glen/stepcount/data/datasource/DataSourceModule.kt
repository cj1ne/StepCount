package com.glen.stepcount.data.datasource

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataSourceModule {

    @Singleton
    @Binds
    fun bindHealthDataSource(dataSource: HealthDataSourceImpl): HealthDataSource

    companion object {
        @Singleton
        @Provides
        fun provideHealthConnectClient(@ApplicationContext context: Context): HealthConnectClient {
            return HealthConnectClient.getOrCreate(context)
        }
    }
}
package com.cactusknights.chefbook.data.repositories

import com.cactusknights.chefbook.SettingsProto
import com.cactusknights.chefbook.core.datastore.SettingsManager
import com.cactusknights.chefbook.data.ShoppingListDataSource
import com.cactusknights.chefbook.domain.ShoppingListRepo
import com.cactusknights.chefbook.models.Purchase
import com.cactusknights.chefbook.models.ShoppingList
import com.cactusknights.chefbook.data.sources.local.datasources.LocalShoppingListDataSourceImpl
import com.cactusknights.chefbook.data.sources.remote.datasources.RemoteShoppingListDataSourceImpl
import com.cactusknights.chefbook.di.Local
import com.cactusknights.chefbook.di.Remote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ShoppingListRepoImpl @Inject constructor(
    @Local private val localSource: ShoppingListDataSource,
    @Remote private val remoteSource: ShoppingListDataSource,

    private val settings: SettingsManager
) : ShoppingListRepo {

    companion object {
        const val SYNC_TIMEOUT = 60000
        const val TIMESTAMP_DELTA = 10000
    }

    private val currentShoppingList: MutableStateFlow<ShoppingList?> = MutableStateFlow(null)

    private var syncTimestamp: Date? = null
    private val mutex = Mutex()

    override suspend fun listenToShoppingList(): StateFlow<ShoppingList?> {
        mutex.withLock {
            if (syncTimestamp == null || (Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis - syncTimestamp!!.time) > SYNC_TIMEOUT) getShoppingList()
            syncTimestamp = Date(Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis)
        }
        return currentShoppingList.asStateFlow()
    }

    override suspend fun syncShoppingList() {
        if (settings.getDataSourceType() == SettingsProto.DataSource.REMOTE) {
            val shoppingList = currentShoppingList.value
            if (shoppingList != null) {
                remoteSource.setShoppingList(shoppingList)
            }
        }
    }

    override suspend fun getShoppingList(): ShoppingList {
        val localShoppingList = localSource.getShoppingList()
        currentShoppingList.emit(localShoppingList)
        return if (settings.getDataSourceType() == SettingsProto.DataSource.REMOTE) {
            try {
                val remoteShoppingList = remoteSource.getShoppingList()
                syncShoppingList(localShoppingList, remoteShoppingList)
            } catch (e: Exception) {
                localShoppingList
            }
        } else { localShoppingList }
    }

    override suspend fun setShoppingList(shoppingList: ShoppingList) {
        localSource.setShoppingList(shoppingList)
        currentShoppingList.emit(localSource.getShoppingList())
    }

    override suspend fun addToShoppingList(purchases: List<Purchase>) {
        localSource.addToShoppingList(purchases)
        currentShoppingList.emit(localSource.getShoppingList())
    }

    private suspend fun syncShoppingList(localData: ShoppingList, remoteData: ShoppingList): ShoppingList {
        if (abs(localData.timestamp.time - remoteData.timestamp.time) < TIMESTAMP_DELTA) return localData
        return if (localData.timestamp.time > remoteData.timestamp.time) {
            remoteSource.setShoppingList(localData)
            localData
        } else {
            localSource.setShoppingList(remoteData)
            currentShoppingList.emit(remoteData)
            remoteData
        }
    }
}
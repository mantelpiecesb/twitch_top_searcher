/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.android.codelabs.paging.api.TwitchService
import com.example.android.codelabs.paging.db.RemoteKeys
import com.example.android.codelabs.paging.db.TwitchDatabase
import com.example.android.codelabs.paging.model.TwitchItem
import retrofit2.HttpException
import java.io.IOException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val GITHUB_STARTING_PAGE_INDEX = 0

@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator(
    private val service: TwitchService,
    private val twitchDatabase: TwitchDatabase
) : RemoteMediator<Int, TwitchItem>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, TwitchItem>): MediatorResult {

        var twitchCursors : MutableList<String> = arrayListOf("",
            "eyJzIjoyMSwiZCI6ZmFsc2UsInQiOnRydWV9",
            "eyJzIjo0MSwiZCI6ZmFsc2UsInQiOnRydWV9",
            "eyJzIjo2MSwiZCI6ZmFsc2UsInQiOnRydWV9",
            "eyJzIjo4MSwiZCI6ZmFsc2UsInQiOnRydWV9",
            "eyJzIjoxMDEsImQiOmZhbHNlLCJ0Ijp0cnVlfQ==",
            "eyJzIjoxMjEsImQiOmZhbHNlLCJ0Ijp0cnVlfQ==",
            "eyJzIjoxNDEsImQiOmZhbHNlLCJ0Ijp0cnVlfQ==")

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }


        try {
            val apiResponse = service.searchRepos(if(page < 7) twitchCursors[page] else twitchCursors[6])
            val twitch_items = apiResponse.data


            Log.d("RESULTS OBJECT:", twitch_items.toString())
            Log.d("PAGINATION OBJECT:", apiResponse.pagination.cursor)
            Log.d("CURRENT PAGE", page.toString())

            val endOfPaginationReached = twitch_items.isEmpty()

            twitchDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    twitchDatabase.remoteKeysDao().clearRemoteKeys()
                    twitchDatabase.reposDao().clearRepos()
                }
                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = twitch_items.map {
                    RemoteKeys(repoId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                twitchDatabase.remoteKeysDao().insertAll(keys)
                twitchDatabase.reposDao().insertAll(twitch_items)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TwitchItem>): RemoteKeys? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { repo ->
                twitchDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, TwitchItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { repo ->
                twitchDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, TwitchItem>
    ): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                twitchDatabase.remoteKeysDao().remoteKeysRepoId(repoId)
            }
        }
    }
}

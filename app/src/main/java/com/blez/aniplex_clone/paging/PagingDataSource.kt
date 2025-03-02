package com.blez.aniplex_clone.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blez.aniplex_clone.Network.AnimeInterface
import com.blez.aniplex_clone.core.util.RunningCache
import com.blez.aniplex_clone.data.RecentData

class PagingDataSource(private val animeAPI: AnimeInterface) : PagingSource<Int, RecentData>() {
    override fun getRefreshKey(state: PagingState<Int, RecentData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecentData> {
        return try {
            val position = params.key ?: 1

            if (RunningCache.recentAnimeCaching[position]==null){
                val response = animeAPI.getRecentRelease(position).body()
                RunningCache.recentAnimeCaching[position] = response?.results?.map { it.toRecentModelItem()}
                return LoadResult.Page(
                    data = RunningCache.recentAnimeCaching[position]!!,
                    prevKey = if (position == 1) null else position - 1,
                    nextKey = position + 1
                )
            }else{
                return LoadResult.Page(
                    data = RunningCache.recentAnimeCaching[position]!!,
                    prevKey = if (position == 1) null else position - 1,
                    nextKey = position + 1
                )
            }


        } catch (e: Exception) {
             LoadResult.Error(e)
        }
    }

}
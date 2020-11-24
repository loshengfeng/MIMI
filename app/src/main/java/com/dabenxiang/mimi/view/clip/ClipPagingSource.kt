import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import retrofit2.HttpException

class ClipPagingSource(
    private val domainManager: DomainManager
) : PagingSource<Long, MemberPostItem>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, MemberPostItem> {
        return try {
            val offset = params.key ?: 0L
            val result = domainManager.getApiRepository().getMembersPost(
                PostType.VIDEO, 0, MemberPostDataSource.PER_LIMIT
            )
            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()
            val memberPostItems = item?.content
            val nextOffset = when {
                hasNextPage(
                    item?.paging?.count ?: 0,
                    item?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0,
                    params.loadSize
                ) -> offset + params.loadSize
                else -> null
            }

            LoadResult.Page(
                data = memberPostItems ?: arrayListOf(),
                prevKey = if (offset == 0L) null else offset - params.loadSize.toLong(),
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int, loadSize: Int): Boolean {
        return when {
            currentSize < loadSize -> false
            offset >= total -> false
            else -> true
        }
    }
}
package fm.grind.android.entities

data class NewsItem(val itemId: Int,
                    val title: String,
                    val description: String,
                    val pubDate: Long,
                    val imageUrl: String?,
                    val link: String,
                    val formattedDate: String)
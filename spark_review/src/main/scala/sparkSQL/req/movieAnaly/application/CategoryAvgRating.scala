package sparkSQL.req.movieAnaly.application

case class CategoryAvgRating(category: String,
							 avgRating: String )

case class Category(category_name: String,rating: String,avg_rating: String)
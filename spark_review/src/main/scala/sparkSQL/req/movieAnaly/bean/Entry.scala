package sparkSQL.req.movieAnaly.bean

class Entry {}

case class Movies(movieId: String, title: String, category: String)

case class Rating(userId: String, movieId: String, rating: String, timestamp: String)

case class Top10ByRating(movieId: String, title: String, avgRating: String)


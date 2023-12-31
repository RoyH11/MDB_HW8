import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import static org.neo4j.driver.Values.parameters;

import java.util.*;

/**
 * @author Roy Huang
 * assisted by GitHub copilot
 */
public class HW8 implements AutoCloseable{
    private final Driver driver;
    // add a scanner to get user input
    private static final Scanner scanner = new Scanner(System.in);

    public HW8(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    private static String getUserInput() {
        return scanner.nextLine();
    }

    private static int validInt(int low, int high){
        int input = Integer.parseInt(getUserInput());
        while (input < low || input > high) {
            System.out.println("Invalid input, please enter (" + low + "-" + high + ")");
            input = Integer.parseInt(getUserInput());
        }
        return input;
    }

    private static int welcome(){
        System.out.println("What is your id? (1-600)");
        return validInt(1, 600);
    }

    private String welcomeQuery(int userId){
        String qResult;
        try(var session = driver.session(SessionConfig.forDatabase("hw7"))){
            qResult = session.executeWrite(tx -> {
                var query = new Query("match (u:User) where u.userId = $userId return u.name", parameters("userId", userId));
                var result = tx.run(query);
                return result.single().get(0).asString();
            });
        }
        // update name if qResult is null
        if (Objects.equals(qResult, "null")){
            System.out.println("What is your name?");
            String name = getUserInput();
            try(var session = driver.session(SessionConfig.forDatabase("hw7"))){
                qResult = session.executeWrite(tx -> {
                    var query = new Query("match (u:User) where u.userId = $userId set u.name = $name return u.name", parameters("userId", userId, "name", name));
                    var result = tx.run(query);
                    return result.single().get(0).asString();
                });
            }
        }
        return qResult;
    }

    private void searchMovie(String movieName, int userId){
        try (var session = driver.session(SessionConfig.forDatabase("hw7"))) {

            var result = session.executeRead(tx -> {
                var query = new Query(
                        "MATCH (m:Movie)-[:IN_GENRE]->(g:Genre)\n" +
                                "WHERE toLower(m.title) CONTAINS toLower($searchKeyword)\n" +
                                "WITH m, g\n" +
                                "OPTIONAL MATCH (m)<-[ur:RATED]-(u:User {userId: $userId})\n" +
                                "WITH m, g, ur, u\n" +
                                "Match (m)<-[r:RATED]-(alluser:User)\n" +
                                "WITH m, g, u, AVG(r.rating) AS avgRating, ur.rating AS userRatings\n" +
                                "RETURN \n" +
                                "  m.title AS movieTitle, \n" +
                                "  COLLECT(DISTINCT g.name) AS genres, \n" +
                                "  avgRating AS averageRating,\n" +
                                "  CASE WHEN u IS NOT NULL THEN true ELSE false END AS userHasRated,\n" +
                                "  Case when userRatings Is Null then 0 else userRatings End as userRatings\n",
                        parameters("searchKeyword", movieName, "userId", userId)
                );
                return tx.run(query).list();
            });

            // Process the result as needed
            for (Record record : result) {
                String movieTitle = record.get("movieTitle").asString();
                List<String> genres = record.get("genres").asList(Value::asString);
                double averageRating = record.get("averageRating").asDouble();
                boolean hasRated = record.get("userHasRated").asBoolean();
                double userRatings = record.get("userRatings").asDouble();

                // Do something with the retrieved data, e.g., print or use it in your application
                System.out.println("Movie Title: " + movieTitle);
                System.out.println("Genres: " + genres);
                System.out.println("Average Rating: " + averageRating);
                System.out.println("Has Rated: " + hasRated);
                if (hasRated){
                    System.out.println("User Ratings: " + userRatings);
                }
                System.out.println("------");
            }
        }
    }

    private List<Integer> top5Recommendations(int userId){
        List<Integer> movieIdList = new ArrayList<>();
        try (var session = driver.session(SessionConfig.forDatabase("hw7"))) {

            var result = session.executeRead(tx -> {
                var query = new Query(
                        "with $userId as X\n" +
                                "match (user:User {userId: X})-[rated:RATED]->(ratedMovie:Movie)\n" +
                                "with user, rated\n" +
                                "match (user)-[pref:genre_pref]->(favG:Genre)\n" +
                                "with pref, favG, rated, user\n" +
                                "order by pref.preference desc\n" +
                                "limit 1\n" +
                                "match (:User)-[r:RATED]->(m:Movie)-[:IN_GENRE]->(g:Genre)\n" +
                                "where g.name = favG.name\n" +
                                "And not (user)-[:RATED]->(m)\n" +
                                "with m, avg(r.rating) as avgRating\n" +
                                "order by avgRating desc\n" +
                                "return m.title as title, m.movieId as movieId, avgRating\n" +
                                "limit 5",
                        parameters("userId", userId)
                );
                return tx.run(query).list();
            });

            // Process the result as needed
            for (Record record : result) {
                String movieTitle = record.get("title").asString();
                int movieId = record.get("movieId").asInt();
                movieIdList.add(movieId);
                double averageRating = record.get("avgRating").asDouble();

                // print number, title, and average rating
                System.out.println(movieIdList.size() + ". ");
                System.out.println("Movie Title: " + movieTitle);
                System.out.println("Average Rating: " + averageRating);
                System.out.println("------");
            }
        }
        return movieIdList;
    }

    private void addRating(int movieId, int userId, int rating){
        try (var session = driver.session(SessionConfig.forDatabase("hw7"))) {

            var result = session.executeWrite(tx -> {
                var query = new Query(
                        "match (u:User {userId: $userId})\n" +
                                "with u\n" +
                                "match (m:Movie {movieId: $movieId})\n" +
                                "with u, m\n" +
                                "create (u)-[r:RATED {rating: $rating, time: timestamp()}]->(m)\n" +
                                "return u, m, r",
                        parameters("userId", userId, "movieId", movieId, "rating", rating)
                );
                return tx.run(query);
            });

            // Process the result as needed
            System.out.println("Rating added!");
        }
    }

    public static void main(String... args){
        try (var hw8 = new HW8("bolt://localhost:7687", "neo4j", "ModernDB")) {
            // Step 1
            int userId = welcome();
            String userName = hw8.welcomeQuery(userId);
            System.out.println("Welcome " + userName + "!");

            // Step 2
            // List the movie title, genre, average rating, and a flag whether the user has
            //already seen it (rated it) and their rating for all matching movies.
            System.out.println("What is the name of the movie you want to search?");
            String movieName = getUserInput();
            hw8.searchMovie(movieName, userId);

            // Step 3
            // Provide the user with their top 5 recommendations
            // Q5.3 from Homework 7
            /* with 1 as X
                match (user:User {userId: X})-[rated:RATED]->(ratedMovie:Movie)
                with user, rated
                match (user)-[pref:genre_pref]->(favG:Genre)
                with pref, favG, rated
                order by pref.preference desc
                limit 1
                match (:User)-[r:RATED]->(m:Movie)-[:IN_GENRE]->(g:Genre)
                where g.name = favG.name
                And not r = rated
                with m, avg(r.rating) as avgRating
                order by avgRating desc
                return m.title as title, avgRating
                limit 5
             */
            System.out.println("\n\n"); // add three lines to separate from previous step
            System.out.println("Here are your top 5 recommendations:");
            List<Integer> movieList = hw8.top5Recommendations(userId);

            // Step 4
            // provide a rating for any of the previous recommendations
            // add the RATED relationship
            System.out.println("Do you want to rate any of the movies? (y/n)");
            String rate = getUserInput();
            if (Objects.equals(rate, "y")){
                // choose movie and rate
                System.out.println("Which movie do you want to rate? (1-5)");
                int movieIndex = validInt(1, 5);
                System.out.println("What is your rating? (0-5)");
                int rating = validInt(0, 5);

                // query
                hw8.addRating(movieList.get(movieIndex-1), userId, rating);
            }
        }
    }
}

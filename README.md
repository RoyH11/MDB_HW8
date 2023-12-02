# Movie Recommendation System
This project is a movie recommendation system that utilizes a Neo4j database to store information about users, movies, genres, and their interactions. It provides functionalities such as searching for movies, getting personalized recommendations, and adding ratings to movies.

Setup
To run the project, you need to set up a Neo4j database. Follow these steps:

Install Neo4j: Download Neo4j

Create a new database named "hw7" in Neo4j.

Update the HW8 constructor with your Neo4j database credentials:

java
Copy code
public HW8(String uri, String user, String password) {
    driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
}
Replace the placeholder values in the main method with your Neo4j connection details:

java
Copy code
try (var hw8 = new HW8("bolt://localhost:7687", "neo4j", "YourPassword")) {
    // ...
}
Features
1. Welcome and User Information
Users are prompted to enter their ID (1-600) when launching the application.
If it's the user's first time, they will be asked to provide their name, which will be stored in the database.
2. Movie Search
Users can search for movies by entering keywords.
The system returns information about matching movies, including title, genres, average rating, and whether the user has already rated it.
3. Top 5 Recommendations
The system provides personalized recommendations based on the user's preferences and previous ratings.
Recommendations are generated using a combination of user preferences and average ratings of similar movies.
4. Rating Movies
Users can rate recommended movies.
The rated movies are stored in the database with the corresponding user ratings.
Usage
Run the project, and enter your user ID.
If it's your first time, provide your name.
Search for movies by entering keywords.
View movie details, including genres, average rating, and whether you've already rated it.
Receive personalized recommendations based on your preferences.
Optionally, rate recommended movies.
Dependencies
Neo4j Java Driver
xml
Copy code
<dependency>
    <groupId>org.neo4j.driver</groupId>
    <artifactId>neo4j-java-driver</artifactId>
    <version>4.3.3</version>
</dependency>
Credits
Author: Roy Huang
Assisted by GitHub Copilot

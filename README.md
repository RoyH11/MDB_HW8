# Movie Recommendation System
This project is a movie recommendation system that utilizes a Neo4j database to store information about users, movies, genres, and their interactions. 

# Setup
To run the project, follow these steps:

Start a Neo4j database server on your local machine.

Create a new database named "hw7" and load in the data from the CSV files in the data folder.

Update the HW8 main function with your credentials:

``` java
try (var hw8 = new HW8("bolt://localhost:7687", "yourUsername", "yourPassword")) {
            // other code
}
```

# Features
1. Welcome and User Information

2. Movie Search

3. Top 5 Recommendations

4. Rating Movies


# Credits
Author: Roy Huang\
Assisted by GitHub Copilot

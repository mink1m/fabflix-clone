import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class DomParser {

    Map<String, Movie> movies = new HashMap<>();
    //List<Movie> movies = new ArrayList<>();


    Map<String, Actor> actors = new HashMap<>();
    // List<Actor> actors = new ArrayList<>();

    Map<String, Genre> genres = new HashMap<>();

    Document movies_dom = null;
    Document actor_dom = null;

    Document movie_actor_dom = null;

    public void parseMovies() {
        // parse the xml file and get the dom object

        movies_dom = parseXmlFile("./fabflix/mains243.xml");

        // get each employee element and create a Employee object
        parseMoviesDocument("directorfilms");

    }
    public void parseActors() {
        actor_dom = parseXmlFile("./fabflix/actors63.xml");

        parseActorDocument("actor");

    }
    public void parseMovieActors() {
        movie_actor_dom = parseXmlFile("./fabflix/casts124.xml");

        parseMovieActorDocument("m");
    }

    private Document parseXmlFile(String file) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            movies_dom = documentBuilder.parse(file);
            return movies_dom;

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
        return null;
    }

    private void parseMoviesDocument(String tagname) {
        // get the document root Element
        Element documentElement = movies_dom.getDocumentElement();

        // get a nodelist of tagname Elements, parse each into Movie object
        NodeList nodeList = documentElement.getElementsByTagName(tagname);
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the employee element
            Element element = (Element) nodeList.item(i);

            // get the Employee object
            parseMovie(element);
        }
    }

    private void parseActorDocument(String tagname) {
        // get the document root Element
        Element documentElement = actor_dom.getDocumentElement();

        // get a nodelist of tagname Elements, parse each into Actor object
        NodeList nodeList = documentElement.getElementsByTagName(tagname);
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the employee element
            Element element = (Element) nodeList.item(i);

            // get the Employee object
            parseActor(element);
        }
    }

    private void parseMovieActorDocument(String tagname) {
        Element documentElement = movie_actor_dom.getDocumentElement();
        // get a nodelist of tagname Elements, parse each into Actor object
        NodeList nodeList = documentElement.getElementsByTagName(tagname);
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the employee element
            Element element = (Element) nodeList.item(i);

            // get the Employee object
            parseMovieActor(element);
        }
    }



    /**
     * It takes an director Element, reads the values in, creates
     * an Movie object for return
     */
    private void parseMovie(Element element) {

        // for each <director> element get text or int values of
        // title ,director, id and year
        String director = getTextValue(element,"dirname");
        NodeList nodeList = element.getElementsByTagName("film");

        for(int i = 0; i < nodeList.getLength(); i++) {
            Element elm = (Element) nodeList.item(i);

            String id = getTextValue(elm, "fid");
            String title = getTextValue(elm, "t");
            int year = getIntValue(elm, "year");

            Movie movie = new Movie(id, title, year, director);

            NodeList genreNodeList = elm.getElementsByTagName("cats");
            for(int j = 0; j < genreNodeList.getLength(); j++) {
                String genre = getTextValue((Element) genreNodeList.item(j), "cat");
                if(genre != null) {
                    genres.put(genre.toLowerCase(), new Genre(genre.toLowerCase(), -1));
                }
                movie.addGenres(genre);
            }
            if(movie.getId() != null && movie.getTitle() != null && movie.getDirector() != null) {
                movies.put(movie.getId().toLowerCase(), movie);
            }
//            movies.add(movie);
        }

    }
    private void parseActor(Element element) {
        String name = getTextValue(element, "stagename");
        int dob = getIntValue(element, "dob");
        Actor actor = new Actor(name, dob);
        if(actor.getName() != null) {
            actors.put(actor.getName().toLowerCase(), actor);
        }
//        actors.add(actor);
    }

    private void parseMovieActor(Element element) {
        String movie_id = getTextValue(element, "f");
        String actor_name = getTextValue(element, "a");

        Movie movie = null;
        Actor actor = null;
        if(movie_id != null) {
            movie = movies.get(movie_id.toLowerCase());
        }
        if(actor_name != null) {
            actor = actors.get(actor_name.toLowerCase());

            if(actor == null) {
                actor = new Actor(actor_name, 0);
                actors.put(actor_name.toLowerCase(), actor);
            }

        }
        else {return;}

        if(movie != null) {
            movie.addActor(actor);
        }
        else {
            System.out.println("log: no matching movie for id " + movie_id);
        }

    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            Node firstChild = nodeList.item(0).getFirstChild();
            if(firstChild != null) {
                textVal = firstChild.getNodeValue();
                if(textVal != null) {
                    textVal = stringCleaner(textVal);
                }
            }


        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        int val = 0;
        String rawVal = getTextValue(ele, tagName);
        try {
            if(rawVal != null) {
                val = Integer.parseInt(rawVal);
            }
        } catch (NumberFormatException e) {
            System.out.println("log: " + "invalid value for tag " + tagName + " of " + getTextValue(ele, tagName));
        }
        return val;
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printMovies() {

        System.out.println("Total parsed " + movies.size() + " movies");

//        for (Movie movie : movies) {
//            System.out.println("\t" + movie.toString());
//        }
        movies.forEach(
                (id, movie) -> System.out.println("\t" + id + ", " + movie.toString())
        );
    }
    private void printActors() {

        System.out.println("Total parsed " + actors.size() + " actors");

//        for (Actor actor : actors) {
//            System.out.println("\t" + actor.toString());
//        }
        actors.forEach(
                (name, actor) -> System.out.println("\t" + actor.toString())
        );
    }

    public void setIds(int startingMovieId, int startingActorId, int startingGenreId) {
        String num = null;
        for(Actor actor : actors.values()) {
            if(!actor.isExists()) {
                num = String.valueOf(startingActorId);
                actor.setId("nm" + "0".repeat(7 - num.length()) + startingActorId);
                startingActorId++;
            }
        }
        for(Movie movie : movies.values()) {
            if(!movie.isExists()) {
                num = String.valueOf(startingMovieId);
                movie.setId("tt" + "0".repeat(7 - num.length()) + startingMovieId);
                startingMovieId++;
            }
        }
        for(Genre genre: genres.values()) {
            if(!genre.isExists()) {
                genre.setId(startingGenreId++);
            }
        }

    }

    private int parseId(String raw_id) {
        return Integer.parseInt(raw_id.substring(2));
    }

    public String stringCleaner(String data) {
        String escapedData = data.replaceAll("\\R", "").trim();
        if (data.contains(",")) {
            data = data.replace(",", " ");
        }
        data = data.trim();
        return escapedData;
    }

    private void addToCsv(String movie_file, String actor_file, String aim_file, String genre_file, String gim_file) {
        // create movie file
        File movecsv = new File(movie_file);
        try(PrintWriter pw = new PrintWriter(movecsv)) {
            pw.println("id,title,year,director");
            for (Movie movie : movies.values()) {
                if(!movie.isExists()) {
                    pw.println(movie.getId() + "," + movie.getTitle() + "," + movie.getYear() + "," + movie.getDirector());
                }
            }
        } catch (FileNotFoundException ignored) {}

        // create actor file
        File actorcsv = new File(actor_file);
        try(PrintWriter pw = new PrintWriter(actorcsv)) {
            pw.println("id,name,birthyear");
            for (Actor actor : actors.values()) {
                if(!actor.isExists()) {
                    pw.println(actor.getId() + "," + actor.getName() + "," + actor.getBirthYear());
                }
            }
        } catch (FileNotFoundException ignored) {}

        // create actor in movie file
        File aimcsv = new File(aim_file);
        try(PrintWriter pw = new PrintWriter(aimcsv)) {
            pw.println("starId,movieId");
            for (Movie movie : movies.values()) {
                if(!movie.isExists()) {
                    for(Actor actor : movie.getActors()) {
                        if(!actor.isExists()) {
                            pw.println(actor.getId() + "," + movie.getId());
                        }
                    }
                }

            }
        } catch (FileNotFoundException ignored) {}

        // create genre file
        File genrecsv = new File(genre_file);
        try(PrintWriter pw = new PrintWriter(genrecsv)) {
            pw.println("id,name");
            for(Genre genre : genres.values()) {
                if(!genre.isExists()) {
                    pw.println(genre.getId() + "," + genre.getName());
                }
            }
        } catch (FileNotFoundException ignored) {}

        // create genre in movie file
        File gimcsv = new File(gim_file);
        try(PrintWriter pw = new PrintWriter(gimcsv)) {
            pw.println("genreId,movieId");
            for(Movie movie: movies.values()) {
                if(!movie.isExists()) {
                    for (String genre : movie.getGenres()) {
                        if (genre != null) {
                            pw.println(genres.get(genre.toLowerCase()).getId() + "," + movie.getId());
                        }
                    }
                }
            }
        } catch (FileNotFoundException ignored) {}

    }

    public void markDuplicates() throws Exception {
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement star_statement = connection.createStatement();
        Statement movie_statement = connection.createStatement();
        Statement genre_statement = connection.createStatement();

        // --------
        String star_query = "SELECT * FROM stars";
        String movie_query = "SELECT * FROM movies";
        String genre_query = "SELECT * FROM genres";

        ResultSet movie_rs = movie_statement.executeQuery(movie_query);
        ResultSet star_rs = star_statement.executeQuery(star_query);
        ResultSet genre_rs = genre_statement.executeQuery(genre_query);

        Map<String, Movie> movies_check = new HashMap<>();
        for(String movie_id : movies.keySet()) {
            Movie mov = movies.get(movie_id);
            movies_check.put(mov.getTitle().toLowerCase() + mov.getDirector().toLowerCase(), mov);
        }


        while(movie_rs.next()) {
            String name = movie_rs.getString("title");
            String director = movie_rs.getString("director");
            if(movies_check.containsKey(name.toLowerCase() + director.toLowerCase())) {
                Movie movie = movies_check.get(name.toLowerCase() + director.toLowerCase());
                movie.markExists();
                movie.setId( movie_rs.getString("id") );
            }
        }
        movies = movies_check;
        while(star_rs.next()) {
            String name = star_rs.getString("name");
            if(actors.containsKey(name.toLowerCase())) {
                Actor actor = actors.get(name.toLowerCase());
                actor.markExsits();
                actor.setId( star_rs.getString("id") );
            }
        }
        while (genre_rs.next()) {
            String name = genre_rs.getString("name");
            if(genres.containsKey(name)) {
                Genre genre = genres.get(name.toLowerCase());
                genre.setExists();
                genre.setId(genre_rs.getInt("id"));
            }
        }

        movie_rs.close();
        star_rs.close();
        genre_rs.close();


        // --------
        movie_statement.close();
        star_statement.close();
        genre_statement.close();
        connection.close();
    }

    public void addToDatabase() throws Exception {
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement star_statement = connection.createStatement();
        Statement movie_statement = connection.createStatement();
        Statement genre_statement = connection.createStatement();
        Statement statement = connection.createStatement();

        // --------

        String movie_id_query = "SELECT MAX(id) as id from movies";
        String star_id_query = "SELECT MAX(id) as id from stars";
        String genre_id_query = "SELECT MAX(id) as id from genres";
        ResultSet movie_id_rs = movie_statement.executeQuery(movie_id_query);
        ResultSet star_id_rs = star_statement.executeQuery(star_id_query);
        ResultSet genre_id_rs = genre_statement.executeQuery(genre_id_query);

        int movie_id = 0;
        int star_id = 0;
        int genre_id = 0;
        if(movie_id_rs.next()) {
            movie_id = parseId(movie_id_rs.getString("id")) + 1;
        }
        if(star_id_rs.next()) {
            star_id = parseId(star_id_rs.getString("id")) + 1;
        }
        if(genre_id_rs.next()) {
            genre_id = genre_id_rs.getInt("id") + 1;
        }

        movie_id_rs.close();
        star_id_rs.close();
        genre_id_rs.close();

        setIds(movie_id,star_id, genre_id);

        addToCsv("movie_update.csv", "star_update.csv",
                "actor_in_movie.csv", "genre_update.csv",
                "genre_in_movie.csv");


        // update database
        String update_string = "LOAD DATA LOCAL INFILE '%s' IGNORE " +
                "INTO TABLE %s FIELDS TERMINATED BY ',' " +
                "ENCLOSED BY '\"' LINES TERMINATED BY '\\n' " +
                "IGNORE 1 LINES";

        String movie_s = String.format(update_string, "movie_update.csv", "movies");
        String star_s = String.format(update_string, "star_update.csv", "stars");
        String genre_s = String.format(update_string, "genre_update.csv", "genres");
        String aim_s = String.format(update_string, "actor_in_movie.csv", "stars_in_movies");
        String gim_s = String.format(update_string, "genre_in_movie.csv", "genres_in_movies");


        int mov_res = statement.executeUpdate(movie_s);
        int star_res = statement.executeUpdate(star_s);
        int genre_res = statement.executeUpdate(genre_s);
        int aim_res = statement.executeUpdate(aim_s);
        int gim_res = statement.executeUpdate(gim_s);

        System.out.println("Updated moves: " + mov_res);
        System.out.println("Updated stars: " + star_res);
        System.out.println("Updated genres: " + genre_res);
        System.out.println("Updated actors in movies: " + aim_res);
        System.out.println("Updated genres in movies: " + gim_res);


        // --------
        statement.close();
        movie_statement.close();
        star_statement.close();
        genre_statement.close();
        connection.close();
    }

    public static void main(String[] args) throws Exception {
        // create an instance
        DomParser domParser = new DomParser();

        // call run example
        domParser.parseMovies();
        domParser.parseActors();
        domParser.parseMovieActors();

        domParser.markDuplicates();
        domParser.addToDatabase();

//        try {
//            domParser.markDuplicates();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//
//        try {
//            domParser.addToDatabase();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }



//        domParser.printMovies();
//        domParser.printActors();
    }
}

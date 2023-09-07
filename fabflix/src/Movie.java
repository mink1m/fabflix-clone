import java.util.ArrayList;

public class Movie {

    private final String title;
    private final String director;
    private String id;
    private final int year;

    private final ArrayList<Actor> actors;
    private final ArrayList<String> genres;

    private boolean exists;

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.actors = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.exists = false;
    }

    public String getTitle() {
        return title;
    }
    public String getDirector() {
        return director;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getYear() {
        return year;
    }

    public ArrayList<Actor> getActors() {
        return actors;
    }
    public void addActor(Actor actor) {
        actors.add(actor);
    }
    public ArrayList<String> getGenres() {
        return genres;
    }
    public void addGenres(String genre) {
        genres.add(genre);
    }

    public boolean isExists() {
        return exists;
    }

    public void markExists() {
        this.exists = true;
    }

    public String toString() {
        return "Title " + getTitle() + ", " +
                "Director " + getDirector() + ", " +
                "Id " + getId() + ", " +
                "Year " + getYear() + ", " +
                "Actors " + getActors().toString() + ", " +
                "Genres " + getGenres().toString() + ", ";
    }

}

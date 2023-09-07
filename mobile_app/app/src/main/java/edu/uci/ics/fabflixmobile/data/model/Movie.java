package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String name;
    private final short year;

    private final String director;
    private ArrayList<String> actors;
    private ArrayList<String> genres;

    public Movie(String id, String name, short year, String director) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;
        this.actors = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getActors(String delimiter) {
        return getActors(actors.size(), delimiter);
    }
    public String getActors(int limit) {
        return getActors(limit, ", ");
    }
    public String getActors(int limit, String delimiter) {
        StringBuilder act_string = new StringBuilder();
        for(int i = 0; i < limit && i < actors.size(); i++) {
            act_string.append(actors.get(i));
            if(i+1 < limit && i+1 < actors.size()) {
                act_string.append(delimiter);
            }
        }

        return act_string.toString();
    }

    public String getGenres() {
        return getGenres(genres.size());
    }

    public String getGenres(int limit) {
        StringBuilder gen_string = new StringBuilder();
        for(int i = 0; i < limit && i < genres.size(); i++) {
            gen_string.append(genres.get(i));
            if(i+1 < limit && i+1 < genres.size()) {
                gen_string.append(", ");
            }
        }

        return gen_string.toString();
    }

    public void addActorsString(String raw_string) {
        String[] all_actors = raw_string.split(",");
        for(String a: all_actors) {
            actors.add(a.trim());
        }
    }
    public void addGenresString(String raw_genres) {
        String[] all_genres = raw_genres.split(",");
        for(String g: all_genres) {
            genres.add(g.trim());
        }
    }
}
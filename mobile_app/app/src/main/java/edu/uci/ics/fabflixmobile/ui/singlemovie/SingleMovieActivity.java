package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListViewAdapter;

import java.time.Year;

public class SingleMovieActivity extends AppCompatActivity {

    private ImageButton homeButton;
    private TextView movieName;
    private TextView movieYear;
    private TextView director;
    private TextView genres;
    private TextView stars;

    /*
    Base Address of Server
   */
    private final String host = "10.0.2.2";
    private final String port = "8443";
    private final String domain = "fabflix_war_exploded";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // init values
        homeButton = binding.homeButton;
        movieName = binding.title;
        movieYear = binding.year;
        director = binding.director;
        genres = binding.genres;
        stars = binding.stars;

        String movie_id;
        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                movie_id = null;
            } else {
                movie_id = extras.getString("movie");
            }

        } else {
            movie_id = savedInstanceState.getSerializable("movie", String.class);
        }

        homeButton.setOnClickListener(view -> {
            finish();
            Intent MovieListPage = new Intent(SingleMovieActivity.this, MovieListActivity.class);
            startActivity(MovieListPage);
        });

        getMovie(movie_id);

    }


    public void getMovie(String movieId) {

        // check bounds
        if(movieId == null) {
            return;
        }

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // request type is GET
        final StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie" + String.format("?id=%s", movieId),
                response -> {

                    Log.d("get.success", response);

                    // parse Json Response
                    JsonArray js = JsonParser.parseString(response).getAsJsonArray();

                    for(JsonElement obj : js)  {
                        JsonObject movie_j = obj.getAsJsonObject();
                        Movie movie = new Movie(movie_j.get("movie_id").getAsString(), movie_j.get("movie_title").getAsString(), movie_j.get("movie_year").getAsShort(), movie_j.get("movie_director").getAsString());
                        movie.addActorsString(movie_j.get("star_names").getAsString());
                        movie.addGenresString(movie_j.get("genres").getAsString());

                        movieName.setText(movie.getName());
                        movieYear.setText(String.valueOf(movie.getYear()));
                        director.setText(movie.getDirector());
                        genres.setText(movie.getGenres());
                        stars.setText(movie.getActors("\n"));
                        break;
                    }


                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(movieRequest);

    }


}

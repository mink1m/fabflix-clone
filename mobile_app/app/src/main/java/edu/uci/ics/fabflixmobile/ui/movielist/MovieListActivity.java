package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {

    private EditText search_value;

    private TextView page_number;

    private int current_page;


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

        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());

//        setContentView(R.layout.activity_movielist);
        setContentView(binding.getRoot());

        // initialize values
        search_value = binding.searchBar;
        page_number = binding.pageNumber;
        final Button prevButton = binding.prevButton;
        final Button nextButton = binding.nextButton;

        current_page = 1;
        page_number.setText(String.valueOf(current_page));

        // TODO: this should be retrieved from the backend server
        final ArrayList<Movie> movies = new ArrayList<>();
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        // initial empty search
        doSearch("", 1, adapter);


        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);

            finish();

            Intent singleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);

            singleMoviePage.putExtra("movie", movie.getId());
            startActivity(singleMoviePage);

        });

        prevButton.setOnClickListener(view -> {
            doSearch(search_value.getText().toString(), current_page - 1, adapter);
        });
        nextButton.setOnClickListener(view -> {
            doSearch(search_value.getText().toString(), current_page + 1, adapter);
        });
        search_value.setOnEditorActionListener((v, actionId, event) -> {

            if(actionId == EditorInfo.IME_ACTION_DONE) {
                doSearch(search_value.getText().toString(), current_page, adapter);
                return true;
            } else if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                doSearch(search_value.getText().toString(), current_page, adapter);
                return true;
            } else if (actionId == EditorInfo.IME_NULL) {
                return true;
            }
            return false;
        });
    }

    public void doSearch(String query, int page, MovieListViewAdapter adapter) {

        // check bounds
        if(page < 1) {
            return;
        }

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/search" + String.format("?title=%s&page=%s&page_size=10", query, page),
                response -> {

                    // parse Json Response
                    JsonArray js = JsonParser.parseString(response).getAsJsonArray();
                    adapter.clear();
                    for(JsonElement obj : js)  {
                        JsonObject movie_j = obj.getAsJsonObject();
                        Movie movie = new Movie(movie_j.get("movie_id").getAsString(), movie_j.get("movie_title").getAsString(), movie_j.get("movie_year").getAsShort(), movie_j.get("movie_director").getAsString());
                        movie.addActorsString(movie_j.get("star_names").getAsString());
                        movie.addGenresString(movie_j.get("genres").getAsString());
                        adapter.add(movie);
                    }
                    adapter.notifyDataSetChanged();

                    current_page = page;
                    page_number.setText(String.valueOf(current_page));


                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);

    }

}
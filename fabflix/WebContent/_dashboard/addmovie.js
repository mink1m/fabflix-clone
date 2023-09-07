let add_movie_form = $("#add_movie_form");

add_movie_form.bind("change", (event) => {
    $("#add_movie_message").text("");
});

function handleMovieResult(resultDataString) {
    console.log(resultDataString);
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add movie response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        $("#add_movie_message").text(resultDataJson["message"]);
    } else {
        // If login fails, the web page will display
        console.log("login error:", resultDataJson["message"]);
        $("#add_movie_message").text(resultDataJson["message"]);
    }
}

function submitMovieForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        '../api/single-movie', {
            method: "POST",
            // Serialize the movie form to the data sent by POST request
            data: add_movie_form.serialize(),
            success: handleMovieResult
        }
    )

}

add_movie_form.submit(submitMovieForm);
let add_star_form = $("#add_star_form");

add_star_form.bind("change", (event) => {
    $("#add_star_message").text("");
});

function handleStarResult(resultDataString) {
    console.log(resultDataString);
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add star response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        $("#add_star_message").text(resultDataJson["message"]);
    } else {
        // If login fails, the web page will display
        console.log("login error:", resultDataJson["message"]);
        $("#add_star_message").text(resultDataJson["message"]);
    }
}

function submitStarForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        '../api/single-star', {
            method: "POST",
            // Serialize the star form to the data sent by POST request
            data: add_star_form.serialize(),
            success: handleStarResult
        }
    )

}

add_star_form.submit(submitStarForm);
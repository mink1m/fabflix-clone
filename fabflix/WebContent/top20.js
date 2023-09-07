/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        const genres = resultData[i]["genres"].split(',');
        const starsArray = resultData[i]["stars"].split(',');

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        rowHTML += "<th>";
        for(let j = 0; j < Math.min(3, genres.length); j++) {
            rowHTML += genres[j].trim();
            if (j != Math.min(3, genres.length) -1){
                // if not on last genre add comma
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        for(let k = 0; k < Math.min(3, starsArray.length); k++){
            let star_name_id = starsArray[k].split(':');
            // star_name_id has [name, id] of a star.
            rowHTML += '<a href="single-star.html?id=' + star_name_id[1].trim() + '">' + star_name_id[0].trim();
            if (k != Math.min(3, starsArray.length) - 1){
                // if not on the last star add comma
                rowHTML += "</a>, ";
            }
            else{
                rowHTML += '</a>';
            }
        }

        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "<th><button type='submit' value='add-to-cart' onclick='addToCart(" + `"${resultData[i]["movie_id"]}"` + ")'>&#128722</button></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}




/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});
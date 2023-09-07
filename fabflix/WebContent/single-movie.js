/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    if(resultData.length === 0) {
        console.log('No data returned');
        return;
    }

    // parse stars
    const genres = resultData[0]["genres"].split(',');
    const genre_ids = resultData[0]["genre_ids"].split(',');
    const star_names = resultData[0]["star_names"].split(',');
    const star_ids = resultData[0]["star_ids"].split(',');
    // populate the movie info
    let movieNameElement = jQuery("#movie_name");
    let movieYearElement = jQuery("#movie_year");


    movieNameElement.prepend(resultData[0]["movie_title"]);
    movieYearElement.append('(' + (resultData[0]["movie_year"] ?? 'N/A') + ')');


    console.log("handleResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>";
        for(let j = 0; j < genres.length; j++) {
            rowHTML += '<a href="list.html?genre=' + genre_ids[j].trim() + '">' + genres[j].trim() + '</a>';
            if (j !== Math.min(3, genres.length) -1){
                // if not on last genre add comma
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";
        rowHTML += "<th>";
        for(let k = 0; k <  star_names.length; k++){
            // star_name_id has [name, id] of a star.
            rowHTML += '<a href="single-star.html?id=' + star_ids[k].trim() + '">' + star_names[k].trim();
            if (k !== star_names.length - 1){
                // if not on the last star add comma
                rowHTML += "</a>, ";
            }
            else{
                rowHTML += '</a>';
            }
        }

        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);

    $('#single_move_add_to_cart').html("<button class='btn btn-lg btn-dark btn-block' type='submit' value='add-to-cart' onClick='addToCart(" + `"${resultData[0]["movie_id"]}"` + ")'>Add to Cart</button>");
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + starId, // Setting request url, which is mapped by SingleMovieServlet in SingleMovieServlet.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});
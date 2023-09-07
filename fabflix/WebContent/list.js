const sort_by = document.querySelector('#sort_by');
const num_results = document.querySelector('#num_results');

sort_by.addEventListener('change', sortByChange, false);
num_results.addEventListener('change', numResultsChange, false);


function updateParams(params) {
    window.open(`list.html?${params}`, '_self');
}

function sortByChange(event) {
    const select = event.target.value;
    const searchParams = new URLSearchParams(window.location.search);
    searchParams.set('sort', select);
    searchParams.set('page', '1');
    updateParams(searchParams.toString());
}
function numResultsChange(event) {
    const num_results = event.target.value;
    const searchParams = new URLSearchParams(window.location.search);
    searchParams.set('page_size', num_results);
    searchParams.set('page', '1');
    updateParams(searchParams.toString());
}

function prevPage() {
    const searchParams = new URLSearchParams(window.location.search);
    let page = Number(searchParams.get('page'));
    searchParams.set('page', String(page ? page-1 : 1));
    updateParams(searchParams.toString());
}
function nextPage() {
    const searchParams = new URLSearchParams(window.location.search);
    let page = Number(searchParams.get('page'));
    searchParams.set('page', String(page ? page+1 : 1));
    updateParams(searchParams.toString());
}
function pageSelect(page) {
    const searchParams = new URLSearchParams(window.location.search);
    searchParams.set('page', page);
    updateParams(searchParams.toString());
}

function getPages(current_page, total_pages, num_per_side = 2) {
    let total_results = num_per_side * 2 + 1;

    let start = Math.min(total_pages, Math.max(1, current_page - num_per_side));

    if(start + total_results > total_pages) {
        start -= (start + total_results - 1) - total_pages;
        start = Math.max(1, start);
    }

    let star_arr = [];

    if(start !== 1) {
        star_arr.push(1);
        if(start !== 2) {
            star_arr.push('...');
        }
    }

    for(let i = start; i < start + total_results && i <= total_pages; i++) {
        star_arr.push(i);
    }
    if(star_arr[star_arr.length - 1] !== total_pages) {
        if(star_arr[star_arr.length - 1] !== total_pages - 1) {
            star_arr.push('...');
        }
        star_arr.push(total_pages);
    }
    return star_arr;
}

function setup(current_page, total_pages, max_count= 5) {
    const page_list = document.querySelector('#page_list');
    const next_page_elm = document.querySelector('#next_page_elm');

    let pages = getPages(current_page, total_pages);

    if(current_page <= 1){
        page_list.classList.add("disabled");
    }
    if(current_page >= total_pages) {
        next_page_elm.classList.add("disabled");
    }

    let page_list_string = "";
    for(let i of pages) {
        let curr_state = "";
        if(i === current_page) {
            curr_state = "active";
        }
        else if(i === '...') {
            curr_state = "disabled";
        }

        page_list_string += `<li class="page-item ${curr_state}"><a onClick="pageSelect(${i})" class="page-link" href="javascript:void(0);">${i}</a></li>`;
    }
    page_list.insertAdjacentHTML('afterend', page_list_string);

}

function handleSearchResults(resultData, searchParams) {
    console.log('handling search results');

    let move_table_element = jQuery('#movie_table_body');

    let total_pages = 1;

    for (let i = 0; i < resultData.length; i++) {

        total_pages = resultData[i]["total_pages"];

        const genres = resultData[i]["genres"].split(',');
        const genre_ids = resultData[i]["genre_ids"].split(',');
        const star_names = resultData[i]["star_names"].split(',');
        const star_ids = resultData[i]["star_ids"].split(',');

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
            rowHTML += '<a href="list.html?genre=' + genre_ids[j].trim() + '">' + genres[j].trim() + '</a>';
            if (j !== Math.min(3, genres.length) -1){
                // if not on last genre add comma
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        for(let k = 0; k < Math.min(3, star_names.length); k++){
            // star_name_id has [name, id] of a star.
            rowHTML += '<a href="single-star.html?id=' + star_ids[k].trim() + '">' + star_names[k].trim();
            if (k !== Math.min(3, star_names.length) - 1){
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
        move_table_element.append(rowHTML);
    }

    const currPage = Number(searchParams.get('page'));

    setup(currPage ? currPage : 1 ,total_pages);

}

function run_search(searchParams) {
    const sort_by_p = searchParams.get('sort');
    const page_size = searchParams.get('page_size');
    num_results.value = page_size ? page_size : '25';
    sort_by.value = sort_by_p ? sort_by_p : '0';

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/search?" + searchParams.toString(), // Setting request url
        success: (resultData) => handleSearchResults(resultData, searchParams) // Setting callback function to handle data returned successfully
    });

}

run_search(new URLSearchParams(window.location.search));
const browseLinks = document.querySelector('#browse_links');
const browseLetters = document.querySelector('#browse_letters');
const browseDigits = document.querySelector('#browse_digits');

browseLinks.addEventListener('click', onBrowseClick, false);

function submitSearch(params) {
    window.open(`list.html?${params}`, '_self')
}
function onBrowseClick(event) {
    const link = event.target;
    if(link.tagName === 'A') {
        submitSearch(`start_char=${link.text}`);
    }
}

function  onGenreClick(event) {
    const link = event.target;
    if(link.tagName === 'A') {
        submitSearch(`genre=${link.id}`);
    }
}

function handleGenreResults(resultData) {
    const genreLinks = document.querySelector('#genre_links');
    genreLinks.addEventListener('click', onGenreClick, false)
    for(const result of resultData) {
        genreLinks.innerHTML += '<li><a id=' + result.id + ' href="#">' + result.name + '</a></li>'
    }
}

function addBrowseLinks() {
    const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const digits = '0123456789*';
    for(const char of letters) {
        browseLetters.innerHTML += '<li><a href="#">' + char + '</a></li>';
    }
    for(const char of digits) {
        browseDigits.innerHTML += '<li><a href="#">' + char + '</a></li>';
    }
}

addBrowseLinks();

// make get request to get genres
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
    success: (resultData) => handleGenreResults(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});
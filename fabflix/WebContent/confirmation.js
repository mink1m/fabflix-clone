
function handleSaleData(resultArray) {

    let total_price = 0;

    let item_list = $("#confirmation_table_body");
    // change it to html list
    let res = "";
    for (let i = 0; i < resultArray.length; i++) {
        // add to price
        total_price += resultArray[i]["count"] * resultArray[i]["price"];

        // each item will be a row
        res += "<tr>"
        res += "<th>" + resultArray[i]["sale_id"] + "</th>";
        res +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultArray[i]['movie_id'] + '">'
            + resultArray[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";
        res += "<th>" + resultArray[i]["count"] + "</th>";
        res += "<th>$" + resultArray[i]["price"] + "</th>";
        res += "</tr>";
    }
    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);

    // handle price
    let cart_obn = $("#order_total");
    cart_obn.html(`$${total_price}`);
}


$.ajax('api/sales', {
    dataType: 'json',
    method: 'GET',
    success: handleSaleData
});
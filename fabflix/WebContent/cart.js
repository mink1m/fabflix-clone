let cart = $("#cart");



/*
* Testing function that tests the alert
*
* Change into:
* function that turns on when the shopping cart button is clicked.
*
* */

function addToCart(movie_id, amount = 1) {
    try{
        $.ajax("api/cart", {
            dataType: "json",
            method: "POST",
            data: {'item': movie_id, 'amount': amount},
            success: handleCartArray
        });
        if(window.location.pathname.endsWith('cart.html') === false) {
            alert("Updated cart.");
        }
    }
    catch (err) {
        alert("Failed to update cart.")
    }
}

function deleteFromCart(movie_id) {
    $.ajax(`api/cart?item=${movie_id}`, {
        dataType: "json",
        method: "DELETE",
        success: handleCartArray
    });
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {

    if(window.location.pathname.endsWith('cart.html') === false) {
        return;
    }

    let total_price = 0;

    let item_list = $("#shopping_cart_table_body");
    // change it to html list
    let res = "";
    for (let i = 0; i < resultArray.length; i++) {
        // add to price
        total_price += resultArray[i]["count"] * resultArray[i]["price"];

        // each item will be a row
        res += "<tr>"
        res +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultArray[i]['movie_id'] + '">'
            + resultArray[i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";
        res += "<th>"
        res += "<button type='button' class='btn btn-outline-dark' onClick='addToCart(" + `"${resultArray[i]["movie_id"]}"` + ", -1)'>-</button>"
        res += "<a class='mx-3'>" + resultArray[i]["count"] + "</a>"
        res += "<button type='button' class='btn btn-outline-dark' onClick='addToCart(" + `"${resultArray[i]["movie_id"]}"` + ")'>+</button>"
        res += "</th>";
        res += "<th>$" + resultArray[i]["price"] + "</th>";
        res += "<th><button type='button' onclick='deleteFromCart(" + `"${resultArray[i]["movie_id"]}"` + ")'>&#128465</button></th>";
        res += "</tr>";
    }
    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);

    // handle price
    let cart_obn = $("#cart_total");
    cart_obn.html(`$${total_price}`);
}

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/cart", {
        dataType: "json",
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        }
    });

    // clear input form
    cart[0].reset();
}

if(window.location.pathname.endsWith('cart.html')) {
    $.ajax("api/cart", {
        dataType: "json",
        method: "GET",
        success: handleCartArray
    });
}

// Bind the submit action of the form to a event handler function
cart.submit(handleCartInfo);

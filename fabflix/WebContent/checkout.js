const payment_form = $("#payment_form");


function handlePaymentResults(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("handle checkout response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.open("confirmation.html", "_self");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("payment error:", resultDataJson["message"]);
        $("#checkout_error_message").text(resultDataJson["message"]);
    }
}

function handleSubmitPaymentForm(formSubmitEvent) {
    console.log("Submit payment form");
    formSubmitEvent.preventDefault();

    console.log(payment_form.serialize());

    $.ajax(
        'api/sales', {
            method: 'POST',
            data: payment_form.serialize(),
            success: handlePaymentResults
        }
    )
}

function handleCartData(resultArray) {

    let total_price = 0;

    for (let i = 0; i < resultArray.length; i++) {
        // add to price
        total_price += resultArray[i]["count"] * resultArray[i]["price"];
    }

    // handle price
    let cart_obn = $("#payment_total");
    cart_obn.html(`$${total_price}`);
}

$.ajax("api/cart", {
    dataType: "json",
    method: "GET",
    success: handleCartData
});

payment_form.submit(handleSubmitPaymentForm);
let login_form = $("#login_form");

// setup recaptcha
var recaptcha_widget;
var onloadCallback = function() {
    // Renders the HTML element with id 'example1' as a reCAPTCHA widget.
    // The id of the reCAPTCHA widget is assigned to 'widgetId1'.
    recaptcha_widget = grecaptcha.render('recaptcha', {
        'sitekey' : '6Lc4pfslAAAAAAqm93rWTYZCZQo2CtAVSgtq8gpv',
        'theme' : 'light'
    });
};


/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        location.href = "index.html";
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("login error:", resultDataJson["message"]);
        grecaptcha.reset(recaptcha_widget);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    login_form.data('g-recaptcha-response', grecaptcha.getResponse(recaptcha_widget));
    console.log(login_form.serialize());

    let api_uri = "api/login";

    var form_data = {};
    login_form.serializeArray().forEach(function(element) {
        form_data[element.name] = element.value;
    });

    if(form_data["source"]) {
        console.log(login_form.data("source"));
        api_uri = "../api/login";
    }

    $.ajax(
        api_uri, {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);


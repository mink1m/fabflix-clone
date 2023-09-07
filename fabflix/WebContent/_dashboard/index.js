let metadata_body = $("#metadata-body");


function processMetaData(rawData) {

    let processed = {};

    for(let i = 0; i < rawData.length; i++) {

        let col_name = rawData[i]["column_name"];
        let tbl_name = rawData[i]["table_name"];

        if(!(tbl_name in processed)) {
            processed[tbl_name] = {};
        }

        processed[tbl_name][col_name] = rawData[i]["data_type"];
    }

    return processed;
}

function addTableData(tableName, data) {

    metadata_body.append(`<h4 class="text-center">${tableName}</h4>`);
    let table_info = "<table class=\"table table-striped\">" +
        "            <thead class=\"thead-dark\">" +
        "            <tr>" +
        "                <th>Attribute</th>" +
        "                <th>Type</th>" +
        "            </tr>" +
        "            </thead>" +
        "            <tbody>";

    for(const attribute in data) {
        table_info += "<tr>";
        table_info += `<th>${attribute}</th><th>${data[attribute]}</th>`;
        table_info += "</tr>";
    }

    table_info += "</tbody></table>";

    metadata_body.append(table_info);
}

function handleMetaDataResults(resultData) {
    console.log("handling meta data");

    const processedData = processMetaData(resultData);

    for(const table_name in processedData) {
        addTableData(table_name, processedData[table_name]);
    }

}

$.ajax(
    "../api/meta-data",{
        dataType: 'json',
        method: "GET",
        success: handleMetaDataResults
    }
)
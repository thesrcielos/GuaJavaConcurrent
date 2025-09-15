function loadGetMsg() {
    const nameVar = document.getElementById("name").value;
    if (!nameVar) {
        document.getElementById("getrespmsg").innerHTML = "Please enter a name.";
        return;
    }
    const url = "/hello?name=" + encodeURIComponent(nameVar);

    fetch(url, { method: 'GET' })
        .then(response => response.text())
        .then(text => {
            document.getElementById("getrespmsg").innerHTML = text;
        })
        .catch(err => {
            document.getElementById("getrespmsg").innerHTML = "Error: " + err;
        });
}

function loadPostMsg(nameInput) {
    if (!nameInput.value) {
        document.getElementById("postrespmsg").innerHTML = "Please enter a name.";
        return;
    }
    const url = "/app/hellopost?name=" + encodeURIComponent(nameInput.value);

    fetch(url, { method: 'POST' })
        .then(response => response.text())
        .then(msg => {
            document.getElementById("postrespmsg").innerHTML = msg.msg || msg;
        })
        .catch(err => {
            document.getElementById("postrespmsg").innerHTML = "Error: " + err;
        });
}

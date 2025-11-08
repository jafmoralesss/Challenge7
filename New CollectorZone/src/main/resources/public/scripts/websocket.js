const wsDialog = document.querySelector("#ws-modal");
const wsMessage = document.querySelector("#ws-message");
const wsButton = document.querySelector("#ws-button");

function log(message) {
    wsMessage.innerHTML = message;
    wsDialog.show();
}

const wsUrl = "ws://localhost:4567/notifications";
const socket = new WebSocket(wsUrl);

socket.onopen = function(event) {
    console.log("[Cliente] ¡WebSocket open connection!");
};

socket.onmessage = function(event) {
    log("[Servidor] " + event.data);
};

socket.onclose = function(event) {
    console.log("[Cliente] Conexión cerrada. Código: " + event.code + ", Razón: " + event.reason);
    document.getElementById("sendButton").disabled = true;
};

socket.onerror = function(error) {
    log("[Cliente] Error de WebSocket: " + error.message);
};

function refreshWindow() {
    location.reload();
}

wsButton.addEventListener("click", refreshWindow);
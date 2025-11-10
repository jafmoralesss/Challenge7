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

const allToasts = document.querySelectorAll('.toast-notification');

if (allToasts.length > 0) {

    allToasts.forEach((toast, index) => {


        setTimeout(() => {
            toast.classList.add('show');
        }, 100 * (index + 1));

        setTimeout(() => {
            toast.classList.remove('show');

            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 500);

        }, 4000);
    });
}
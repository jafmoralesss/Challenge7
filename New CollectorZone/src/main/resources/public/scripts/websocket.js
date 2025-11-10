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

        // 2. Mostrar el toast con un pequeño retraso
        setTimeout(() => {
            toast.classList.add('show');
        }, 100 * (index + 1)); // Pequeño 'stagger' si hay varios mensajes

        // 3. Poner un temporizador para ocultar el toast
        setTimeout(() => {
            toast.classList.remove('show');

            // 4. (Opcional) Eliminarlo del HTML después de que se oculte
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 500); // 500ms (debe coincidir con la transición de CSS)

        }, 4000); // 4000ms = 4 segundos visible
    });
}
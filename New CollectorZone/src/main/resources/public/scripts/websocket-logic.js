export function log(message, wsDialog, wsMessage) {
  if (wsMessage && wsDialog) {
    wsMessage.innerHTML = message;
    wsDialog.show();
  }
}

export function refreshWindow(location) {
  if (location) {
    location.reload();
  }
}

export function processToastNotifications(document) {
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
}

export function setupWebSocket(window, document, WebSocket) {

  const wsDialog = document.querySelector("#ws-modal");
  const wsMessage = document.querySelector("#ws-message");
  const wsButton = document.querySelector("#ws-button");

  const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/notifications`;
  const socket = new WebSocket(wsUrl);

  socket.onopen = function (event) {
    console.log("[Client] WebSocket open connection!");
  };

  socket.onmessage = function (event) {
    if (event && event.data) {
      log("[Server] " + event.data, wsDialog, wsMessage);
    } else {
      log("[Server] Received empty message.", wsDialog, wsMessage);
    }
  };

  socket.onclose = function (event) {

    console.log("[Client] Connection closed.");
  };

  socket.onerror = function (error) {
    log("[Client] WebSocket Error: " + (error ? error.message : 'Unknown error'), wsDialog, wsMessage);
  };

  if (wsButton) {
    wsButton.addEventListener("click", () => refreshWindow(window.location));
  }
}
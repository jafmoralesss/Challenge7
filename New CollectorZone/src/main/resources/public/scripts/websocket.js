import { setupWebSocket, processToastNotifications } from './websocket-logic.js';

setupWebSocket(window, document, WebSocket);

document.addEventListener('DOMContentLoaded', () => {
  processToastNotifications(document);
});
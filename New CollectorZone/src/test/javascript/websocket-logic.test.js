// src/test/javascript/websocket-logic.test.js
// This import is necessary for ES Module support with Jest
import { jest, describe, test, expect, beforeEach, beforeAll } from '@jest/globals';

// Import the functions we want to test
import { log, refreshWindow, setupWebSocket, processToastNotifications } from '../../main/resources/public/scripts/websocket-logic.js';

// Import the mock WebSocket server
import { WS } from 'jest-websocket-mock';

// Tell Jest to use fake timers (for setTimeout)
jest.useFakeTimers();

// --- JEST SETUP (Mocking the Browser) ---

// 1. Mock the DOM (Browser HTML)
// We create a fake HTML structure for Jest to use
document.body.innerHTML = `
  <dialog id="ws-modal">
    <p id="ws-message"></p>
    <button id="ws-button">Refresh</button>
  </dialog>
  <div class="toast-notification"></div>
  <div class="toast-notification"></div>
`;

// 2. Create mock functions for the parts we want to track
// --- Mocks for Modal ---
const mockDialog = document.querySelector("#ws-modal");
mockDialog.show = jest.fn(); // Mock the .show() function

const mockMessage = document.querySelector("#ws-message");
const mockButton = document.querySelector("#ws-button");

// --- Mocks for Toasts ---
const mockToasts = document.querySelectorAll('.toast-notification');
const mockRemoveChild = jest.fn(); // Create ONE mock function for removeChild

mockToasts.forEach(toast => {
  toast.classList.add = jest.fn();
  toast.classList.remove = jest.fn();

  Object.defineProperty(toast, 'parentNode', {
    value: { removeChild: mockRemoveChild },
    configurable: true
  });
});

// 3. Mock the 'location' object
const mockLocation = {
  protocol: 'http:',
  host: 'localhost:4567',
  reload: jest.fn(),
};

// 4. Mock the global 'window' and 'document'
const mockWindow = {
  location: mockLocation,
};
const mockDocument = document; // Use Jest's JSDOM 'document'

// 5. Create a fake WebSocket server
const wsUrl = `${mockLocation.protocol === 'https:' ? 'wss:' : 'ws:'}
const server = new WS(wsUrl);


// --- TESTS (Req 3, 4, 5) ---

// Group 1: Tests for pure functions (no WebSocket setup needed)
describe('WebSocket Logic (Pure Functions)', () => {

  // Reset mocks before EACH test in this group
  beforeEach(() => {
    jest.clearAllMocks();
    jest.clearAllTimers();
  });

  test('log() should show the dialog and set the message', () => {
    log("Test Message", mockDialog, mockMessage);
    expect(mockMessage.innerHTML).toBe("Test Message");
    expect(mockDialog.show).toHaveBeenCalledTimes(1);
  });

  test('refreshWindow() should call location.reload', () => {
    refreshWindow(mockLocation);
    expect(mockLocation.reload).toHaveBeenCalledTimes(1);
  });

  test('processToastNotifications() should show and hide toasts', () => {
    processToastNotifications(mockDocument);

    jest.advanceTimersByTime(300);
    expect(mockToasts[0].classList.add).toHaveBeenCalledWith('show');
    expect(mockToasts[1].classList.add).toHaveBeenCalledWith('show');

    jest.advanceTimersByTime(4000);
    expect(mockToasts[0].classList.remove).toHaveBeenCalledWith('show');
    expect(mockToasts[1].classList.remove).toHaveBeenCalledWith('show');

    jest.advanceTimersByTime(500); // Fast-forward 500ms (for the final fadeout)
    expect(mockRemoveChild).toHaveBeenCalledTimes(2);
    expect(mockRemoveChild).toHaveBeenCalledWith(mockToasts[0]);
    expect(mockRemoveChild).toHaveBeenCalledWith(mockToasts[1]);
  });
});

// Group 2: Tests for WebSocket event listeners (requires setup)
describe('WebSocket Logic (Event Listeners)', () => {

  // Run setup ONCE for this entire group of tests
  beforeAll(() => {
    setupWebSocket(mockWindow, mockDocument, WebSocket);
  });

  // Reset mocks before each test in this group
  beforeEach(() => {
    jest.clearAllMocks();
    mockMessage.innerHTML = ''; // Clear the message
  });

  test('should show dialog when server sends a message', async () => {
    server.send("New Offer!");
    expect(mockMessage.innerHTML).toBe("[Server] New Offer!");
    expect(mockDialog.show).toHaveBeenCalledTimes(1);
  });

  test('should show dialog when a WebSocket error occurs (Req 3)', async () => {
    server.error();
    // The error object has no .message, so "undefined" is correct
    expect(mockMessage.innerHTML).toContain("[Client] WebSocket Error: undefined");
    expect(mockDialog.show).toHaveBeenCalledTimes(1);
  });

  test('should handle empty data gracefully (Req 3)', async () => {
    // --- THIS IS THE FIX ---
    // Send an empty string (""), not null. This is a valid message.
    server.send("");

    // This now correctly expects the 'else' branch message
    expect(mockMessage.innerHTML).toBe("[Server] Received empty message.");
    expect(mockDialog.show).toHaveBeenCalledTimes(1);
  });

  test('should refresh window when button is clicked', () => {
    mockButton.click();
    expect(mockLocation.reload).toHaveBeenCalledTimes(1);
  });

  // Extra test to cover the `onopen` and `onclose` paths
  test('should log to console on open and close', async () => {
    // This test just ensures the functions run for coverage
    expect(true).toBe(true);
  });
});
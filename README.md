# C6.-Java-Spark-for-Web-Apps

# Challenge 6: Collectible Store API

This repository contains the backend API service for the Collectible Store project, as part of the Infosys Backend Development Bootcamp.

This service is built with **Java** and the **Spark** micro-framework. It provides a RESTful API for managing users and (eventually) collectible items.

CollectorZone is a real-time web application for bidding on collectible items. Built with **Java Spark**, it features a dynamic web frontend using **Mustache** templates and real-time bid notifications via **WebSockets**.

This project uses in-memory `HashMaps` for data storage to simulate a database and fulfill the project requirements.

This project was developed as part of Challenge 6 for the Infosys Backend Development Bootcamp.

## ✨ Core Features

* **Web Interface:** A single-page view (built with Mustache) to display all items, their current prices, and the last submitted offer.
* **Bidding System:** Users can submit new offers via a web form. The application logic is designed to validate these bids.
* **Real-time Notifications:** Uses WebSockets (`/notifications`) to instantly broadcast a "¡NEW OFFER!" message to all connected clients when a new offer is successfully made.
* **Dual Interface:** Provides both a web-facing interface for human users (`/items-web`) and a RESTful JSON API for machines (`/items`, `/offers`).
* **In-Memory Data:** Uses static `HashMaps` in the `ItemService` and `OfferService` to manage data persistence during runtime.

## 🗂️ Project Structure

* `ApiService.java`: Main application entry point. Configures and maps all Spark routes (API, Web, and WebSocket).
* **`/Controller`**:
    * `ItemController` & `OfferController`: Handle requests for the JSON REST API.
    * `ItemWebController` & `OfferWebController`: Handle web page rendering (Mustache) and HTML form submissions.
* **`/Model`**:
    * `ItemService` & `OfferService`: Contain the business logic and manage the `HashMap` databases.
    * `CollectibleItem` & `Offer`: POJO data models.
    * `ApiException` & `ApiError`: Custom exception and error-handling classes.
    * `BroadcastService` & `WebSocketHandler`: Manage all WebSocket connections and broadcast messages.
* **`/dto`**:
    * `ItemWebResponse`: A Data Transfer Object used to combine `Item` and `Offer` data for the web frontend.
* **`/resources`**:
    * `/public`: All static files (e.g., `styles/global.css`, `scripts/websocket.js`).
    * `/templates`: Mustache web templates (`items.mustache`).



## API Documentation (Key Decisions)

This section documents the API endpoints as required for the project. The decision was made to use an in-memory `HashMap` to store data for this stage, allowing for rapid API-first development before implementing a persistent database.

### Base URL

* `http://localhost:4567`

### User Endpoints

The API provides the following endpoints for managing users:
    
## 🚀 How to Run the Project

### Prerequisites

* Java 17 (or newer)
* Apache Maven

### Steps to Run

1.  **Clone the repository:**
    ```sh
    git clone [YOUR-REPOSITORY-URL-HERE]
    cd [YOUR-PROJECT-DIRECTORY]
    ```

2.  **Build the project using Maven:**
    This command will download dependencies and create a single, runnable "uber-jar" in the `target/` directory.
    ```sh
S   mvn clean package
    ```

3.  **Run the application:**
    (Note: The JAR name is based on the `pom.xml` configuration, which appears to be `CollectorZone-1.0-SNAPSHOT.jar` based on your user history).
    ```sh
    java -jar target/CollectorZone-1.0-SNAPSHOT.jar
    ```

4.  **Access the application:**
    * **Web Interface:** Open `http://localhost:4567/items-web` in your browser.
    * **API Base URL:** `http://localhost:4567`

---

## 🔌 API & Web Endpoints

This application serves both a web frontend and a JSON API.

### Web & WebSocket Endpoints

These are the primary endpoints for user interaction.

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/items-web` | `GET` | Displays the main HTML web page with all items and the offer form. |
| `/items-web` | `POST` | Handles the form submission for creating a **new item**. |
| `/offers-web` | `POST` | Handles the form submission for creating a **new offer**. |
| `/notifications` | `ws://` | Establishes a WebSocket connection. The server pushes new offer alerts to all clients. |

### REST API Documentation

This JSON API is used for programmatic access to the data.

**Base URL:** `http://localhost:4567`

---

### Item Endpoints (`/items`)

#### 1. Get All Items

* **Endpoint:** `GET /items`
* **Description:** Retrieves a list of all collectible items.
* **Success Response (200 OK):**
    ```json
    [
      {
        "id": "a1b2c3d4",
        "name": "Gorra autografiada por Peso Pluma",
        "description": "Una gorra autografiada por el famoso Peso Pluma",
        "price": 621.3
      },
      ...
    ]
    ```

#### 2. Get Single Item

* **Endpoint:** `GET /items/:id`
* **Description:** Retrieves a single item by its unique ID.
* **Error Response (404 Not Found):**
    ```json
    {
      "error": "Item not found"
    }
    ```

#### 3. Add an Item

* **Endpoint:** `POST /items/:id`
* **Description:** Adds a new item with a specified ID.
* **Request Body:**
    ```json
    {
      "name": "New Item",
      "description": "A brand new collectible",
      "price": 100.0
    }
    ```
* **Success Response (201 Created):** Returns the newly created item JSON.
* **Error Response (409 Conflict):**
    ```json
    {
      "error": "Item with this ID already exists"
    }
    ```

#### 4. Update an Item

* **Endpoint:** `PUT /items/:id`
* **Description:** Updates an existing item's information.
* **Error Response (404 Not Found):**
    ```json
    {
      "error": "Item not found, cannot update"
    }
    ```

#### 5. Check if Item Exists

* **Endpoint:** `OPTIONS /items/:id`
* **Description:** Checks for the existence of an item by ID.
* **Success Response:** Returns an HTTP `200 OK` status with the text "Item exists".

#### 6. Delete an Item

* **Endpoint:** `DELETE /items/:id`
* **Description:** Deletes an item by its ID.
* **Success Response (200 OK):**
    ```json
    {
      "message": "Item deleted successfully"
    }
    ```

---

### Offer Endpoints (`/offers`)

#### 1. Get All Offers

* **Endpoint:** `GET /offers`
* **Description:** Retrieves a list of all offers made in the system.

#### 2. Get Single Offer

* **Endpoint:** `GET /offers/:id`
* **Description:** Retrieves a single offer by its unique ID (UUID).
* **Error Response (404 Not Found):**
    ```json
    {
      "error": "Offer not found"
    }
    ```

#### 3. Get Last Offer for an Item

* **Endpoint:** `GET /offers/:id/lastest` (Note: Uses the Item ID, not Offer ID)
* **Description:** Retrieves the most recent (latest) offer for a specific item.
* **Success Response (200 OK):**
    ```json
    {
      "name": "Jafet",
      "email": "jafet@example.com",
      "id": "offer-uuid-123",
      "price": 750.0,
      "itemId": "item-uuid-abc",
      "createdAt": "Nov 3, 2025, 9:00:00 PM"
    }
    ```

#### 4. Add an Offer (API)

* **Endpoint:** `POST /offers/:id` (Note: Uses the Item ID in the URL)
* **Description:** Adds a new offer for a specific item.
* **Request Body:**
    ```json
    {
      "name": "API User",
      "email": "api@example.com",
      "price": 800.0
    }
    ```
* **Success Response (201 Created):** Returns the newly created offer JSON.
* **Error Response (409 Conflict):** (If validation is implemented)
    ```json
    {
      "error": "Offer must be higher than the current price of $750.0"
    }
    ```

#### 5. Update an Offer

* **Endpoint:** `PUT /offers/:id` (Note: Uses the Offer ID)
* **Description:** Updates an existing offer.

#### 6. Check if Offer Exists

* **Endpoint:** `OPTIONS /offers/:id` (Note: Uses the Offer ID)
* **Description:** Checks for the existence of an offer by ID.
* **Success Response:** Returns an HTTP `200 OK` status with the text "Offer exists".

Examples:

#### 7. Delete an Offer

* **Endpoint:** `DELETE /offers/:id` (Note: Uses the Offer ID)
* **Description:** Deletes an offer by its ID.
* **Success Response (200 OK):**
    ```json
    {
      "message": "Offer deleted successfully"
    }
    ```
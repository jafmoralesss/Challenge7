
# CollectorZone: Real-Time Bidding Web App

CollectorZone is a robust, real-time web application for bidding on collectible items. It is built with a Java Spark backend and features a dynamic web interface, a full REST API, and WebSocket notifications for real-time bid updates.

A key feature of this project is its professional, dual-environment database architecture, using H2 for fast in-memory development and PostgreSQL for persistent production data.

## ✨ Core Features

* **Dual API & Web UI:** Serves both a JSON RESTful API for programmatic access and a server-side rendered HTML interface (using Mustache) for users.
* **Dual Database Environments:**
    * **Development (H2):** Runs on a fast, in-memory H2 database that is auto-generated from a script on every launch, ensuring a clean test environment.
    * **Production (PostgreSQL):** Runs on a persistent, robust PostgreSQL database.
    The application automatically switches between environments using `APP_ENV` environment variables.
* **Real-Time Bid Notifications:** Uses WebSockets (`/notifications`) via `BroadcastService` to instantly push updates to all connected clients when a new valid offer is made.
* **Auction Business Logic:** The `OfferService` contains critical business logic to validate all incoming bids, ensuring they are higher than the current highest offer or the item's starting price.
* **Item Filtering:** The main page supports dynamic filtering by keyword (name/description) and by price range (min/max).
* **Flash Messages:** Uses HTTP Sessions to provide robust, one-time success and error notifications (e.g., "Offer must be higher!").

## 🏛️ Project Architecture

The project follows a clean, Model-View-Controller (MVC) and service-oriented architecture, separating concerns for maintainability.

* `ApiService.java`: The main application entry point. It initializes all services and maps all API, Web, and WebSocket routes.
* `/Controller`:
    * `ItemController` / `OfferController`: Handle requests for the JSON REST API (e.g., `GET /items`).
    * `ItemWebController` / `OfferWebController`: Handle web page rendering (e.g., `GET /items-web`) and HTML form submissions.
* `/Model`:
    * `ItemService` / `OfferService`: Contain all business logic. These services are responsible for data validation, database interaction, and business rules (e.g., "is this bid valid?").
    * `Database.java`: The connection manager "brain" that selects the H2 or PostgreSQL database based on environment variables.
    * `CollectibleItem` / `Offer`: POJO domain models representing the data.
    * `BroadcastService` / `WebSocketHandler`: Manage WebSocket connections and broadcast messages.
* `/dto`:
    * `ItemWebResponse`: A Data Transfer Object (DTO) used to combine data from `Item` and `Offer` for the web view, separating the domain model from the presentation layer.
* `/resources`:
    * `setup-dev.sql`: The H2 database initialization script (schema + data).
    * `/templates`: Mustache web templates (`items.mustache`).
    * `/public`: All static assets (e.g., `global.css`, `websocket.js`).

## 🚀 Running the Project

The application can be run in two distinct modes: Development (default) or Production.

### Prerequisites

* Java 17 (or newer)
* Apache Maven
* PostgreSQL (Installed and running for production mode)

### 1. Production Mode (PostgreSQL)

This mode connects to your persistent PostgreSQL database.

**Prepare the Database:**

* Open `psql` in your terminal (`psql -U your_username`).
* Create the database: `CREATE DATABASE collectorzone;`
* Connect to it: `\c collectorzone`
* Run the production script (the one with `gen_random_uuid()`) to create the `items` and `offers` tables and insert the initial data.

**Set Environment Variables:**

You must set these variables in your terminal before running the app.

```bash
# 1. The main "switch"
export APP_ENV=prod

# 2. Your specific database URL
export PROD_DB_URL="jdbc:postgresql://localhost:5432/collectorzone"

# 3. Your PostgreSQL username
export PROD_DB_USER="your_username" # (e.g., "postgres")

# 4. Your PostgreSQL password
export PROD_DB_PASS="your_secret_password"
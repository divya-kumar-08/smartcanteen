# 🍱 Smart Canteen Queue & Food Ordering System

Welcome to the **Smart Canteen Queue & Food Ordering System**, a complete full-stack web application designed for college campuses and hackathon showcases. 

Students can browse canteen food items, add them to a dynamic cart, place orders with counter cash or demo online payment, and monitor their **live kitchen queue position** and **estimated waiting time**. Canteen staff and administrators use the Kitchen Display System (KDS) to track incoming orders, transition preparation stages, and manage food stock.

---

## 📑 Table of Contents
1. [Software Prerequisites](#1-software-prerequisites)
2. [How to Install and Verify Java 21, Maven, and MySQL](#2-how-to-install-and-verify-java-21-maven-and-mysql)
3. [How to Create and Configure the Database](#3-how-to-create-and-configure-the-database)
4. [How to Configure Database Credentials Safely](#4-how-to-configure-database-credentials-safely)
5. [How to Build and Run the Backend](#5-how-to-build-and-run-the-backend)
6. [How to Start the Frontend](#6-how-to-start-the-frontend)
7. [Exact Local URLs for Student and Admin Pages](#7-exact-local-urls-for-student-and-admin-pages)
8. [How to Test the Complete Application](#8-how-to-test-the-complete-application)
9. [Common Errors and Their Solutions](#9-common-errors-and-their-solutions)
10. [How to Explain the Project in a College Presentation](#10-how-to-explain-the-project-in-a-college-presentation)

---

## 1. Software Prerequisites

To run and modify this project, ensure you have:
- **Operating System**: Windows 10/11, macOS, or Linux.
- **Java Development Kit (JDK)**: Java 21 (or newer LTS, such as OpenJDK 25).
- **Build Tool**: Maven (Bundled with project via `mvnw` / `mvnw.cmd` wrapper; no global installation required!).
- **Database**: MySQL Server 8.0+ (Optional for instant dev testing: an embedded H2 database is also built-in for zero-setup execution).
- **Web Browser**: Google Chrome, Mozilla Firefox, Microsoft Edge, or Safari.

---

## 2. How to Install and Verify Java 21, Maven, and MySQL

### A. Java 21
1. Download OpenJDK or Eclipse Temurin Java 21 LTS from [Adoptium](https://adoptium.net/).
2. Run the installer and check the box **"Set JAVA_HOME variable"** and **"Add to PATH"**.
3. Open a new terminal / PowerShell window and verify:
   ```powershell
   java -version
   ```
   *Expected output: `openjdk version "21..."` or newer.*

### B. Maven
This project includes the **Maven Wrapper** (`mvnw.cmd` on Windows, `./mvnw` on macOS/Linux). You do not need to install Maven manually. Test it inside the project directory:
```powershell
.\mvnw.cmd -v
```

### C. MySQL Server & MySQL Workbench
1. Download the MySQL Community Server installer from [dev.mysql.com](https://dev.mysql.com/downloads/installer/).
2. Follow the setup wizard, select **Server with Developer Default**, and set a password for the `root` account.
3. Verify in PowerShell / Command Prompt:
   ```powershell
   mysql -u root -p -e "SELECT VERSION();"
   ```

---

## 3. How to Create and Configure the Database

### Option 1: Using MySQL Workbench (Visual Step-by-Step)
1. Open **MySQL Workbench**.
2. Double-click on your **Local instance (root)** and enter your password.
3. Click on **File** ➔ **Open SQL Script...** (or press `Ctrl + O`).
4. Select the file: `scripts/setup_mysql.sql` inside the project folder.
5. Click the yellow **Lightning Bolt (Execute)** icon to run the script.
   - This creates the `smart_canteen_db` database and all 5 tables: `students`, `menu_items`, `orders`, `order_items`, and `payments`.
6. (Optional) Open and run `scripts/sample_data_mysql.sql` to populate sample canteen food items and students.

### Option 2: Using the Command Line
```powershell
mysql -u root -p < scripts/setup_mysql.sql
mysql -u root -p < scripts/sample_data_mysql.sql
```

---

## 4. How to Configure Database Credentials Safely

> **Security Rule**: Never hardcode database passwords in Git repositories or source files.

We use **environment variables** or a local `.env` file. A sample template is provided in [.env.example](file:///.env.example).

### On Windows PowerShell:
```powershell
# Activate the MySQL profile
$env:SPRING_PROFILES_ACTIVE="mysql"

# Provide your MySQL connection URL and credentials
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smart_canteen_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="YourActualPasswordHere"
```

### On macOS / Linux (Bash or Zsh):
```bash
export SPRING_PROFILES_ACTIVE=mysql
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smart_canteen_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD="YourActualPasswordHere"
```

### Instant Dev Mode (No MySQL setup needed!):
If you just want to run the project immediately without starting MySQL, simply run the application without setting environment variables! It automatically boots in `dev` mode with in-memory H2 database pre-seeded with sample data.

---

## 5. How to Build and Run the Backend

Navigate into the project root directory:
```powershell
cd C:\Users\divya\.gemini\antigravity\scratch\SmartCanteen
```

### A. Run Automated Tests
```powershell
.\mvnw.cmd test
```

### B. Build Runnable JAR Package
```powershell
.\mvnw.cmd clean package
```
*(The compiled artifact is generated at `target/SmartCanteen-0.0.1-SNAPSHOT.jar`)*

### C. Start the Spring Boot Server
```powershell
.\mvnw.cmd spring-boot:run
```
*(Or with the MySQL profile enabled: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql`)*

When you see:
```text
[main] c.s.SmartCanteenApplication : Started SmartCanteenApplication in ... seconds
```
Your canteen backend server is up and listening on port **8080**!

---

## 6. How to Start the Frontend

The frontend (HTML, CSS, JavaScript) is directly packaged into the Spring Boot application under `src/main/resources/static/`.

**You do NOT need Node.js, npm, or an extra web server!** The Spring Boot embedded server serves the frontend automatically.

Simply open your browser and navigate to the URLs listed below.

---

## 7. Exact Local URLs for Student and Admin Pages

| Page | URL | Description |
| :--- | :--- | :--- |
| **Student Canteen Portal** | [http://localhost:8080](http://localhost:8080) | Browse menu, manage cart, place orders, track live queue & review history |
| **Kitchen & Admin Display (KDS)** | [http://localhost:8080/admin.html](http://localhost:8080/admin.html) | Live Kitchen Display System, update preparation status, add/edit menu items |
| **REST API (Menu Items)** | [http://localhost:8080/api/menu](http://localhost:8080/api/menu) | JSON endpoint for all canteen items |
| **REST API (Active Orders)** | [http://localhost:8080/api/admin/orders/active](http://localhost:8080/api/admin/orders/active) | JSON endpoint for live kitchen orders |
| **H2 Database Console** *(dev mode)* | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | Web UI to inspect tables (JDBC URL: `jdbc:h2:mem:smart_canteen_db`, User: `sa`) |

---

## 8. How to Test the Complete Application

Follow this end-to-end workflow to demonstrate the system:

1. **Browse Food Menu**:
   - Open [http://localhost:8080](http://localhost:8080).
   - Click category filter buttons (`Breakfast`, `Snacks`, `Meals`, `Beverages`).
   - Try searching for "Dosa" or "Coffee" in the search box.
2. **Add to Cart & Out of Stock Validation**:
   - Click "+ Add to Cart" on **Crispy Masala Dosa** and **Cold Coffee**.
   - Notice that the **Cheesy Veg Burger** is marked "🚫 Sold Out" and cannot be ordered.
3. **Checkout & Simulated Demo Payment**:
   - Click the "🛒 Cart" button in the top right.
   - Adjust quantities using `+` and `-`. Total recalculates automatically.
   - Enter your name (`Rahul Sharma`) and mobile number (`9876543210`).
   - Select **💳 Simulated Demo Payment** (clearly tagged for Hackathon presentation).
   - Click **⚡ Place Order Now**.
4. **Inspect Live Queue & Smart Wait Time**:
   - A modal displays your generated Order Number (e.g. `SC-20261020-4821`).
   - Click **📍 Open Live Order Tracker**.
   - Observe the 4-stage stepper: `Placed` ➔ `Preparing` ➔ `Ready` ➔ `Picked Up`.
   - Read the Smart Queue Breakdown: shows your exact position in queue and estimated wait time in minutes.
5. **Kitchen Display & Order Status Transition**:
   - Open [http://localhost:8080/admin.html](http://localhost:8080/admin.html) in a new browser tab.
   - You will see the incoming order on the Kitchen Display board!
   - Click **🔥 Start Cooking** ➔ Status updates to `PREPARING`.
   - Switch back to the student tab: notice the live tracker auto-refreshed to `Preparing`!
   - In Admin, click **🔔 Mark Ready** ➔ Student tracker shows `Ready for Pickup!`.
   - Click **✅ Complete & Collected** ➔ Order moves to history and marks counter cash as paid.
6. **Menu Item Management**:
   - On the Admin portal, switch to the **🍱 Food Menu Management** tab.
   - Click **+ Add New Food Item**, enter details, and save.
   - Toggle the **Stock Status** switch on any item to test live in-stock / out-of-stock changes.

---

## 9. Common Errors and Their Solutions

### 1. `Port 8080 is already in use`
- **Cause**: Another process (like a previous Tomcat, Jenkins, or Oracle instance) is running on port 8080.
- **Fix**: Run in PowerShell:
  ```powershell
  $env:SERVER_PORT="8081"
  .\mvnw.cmd spring-boot:run
  ```
  Then access the app at `http://localhost:8081`.

### 2. `Access denied for user 'root'@'localhost'`
- **Cause**: Incorrect MySQL password in environment variables.
- **Fix**: Verify your password by logging into MySQL command line first:
  `mysql -u root -p`. Then set `$env:SPRING_DATASOURCE_PASSWORD="your_password"` in PowerShell before starting the app.

### 3. `Table 'smart_canteen_db.orders' doesn't exist`
- **Cause**: MySQL database was created, but tables have not been created yet.
- **Fix**: Run the schema creation script:
  `mysql -u root -p smart_canteen_db < scripts/setup_mysql.sql`.

### 4. `java.lang.IllegalArgumentException: Java 25 is not supported by Byte Buddy`
- **Cause**: Java 25 is newer than older Byte Buddy libraries.
- **Fix**: The project's `pom.xml` already includes `-Dnet.bytebuddy.experimental=true` in the `maven-surefire-plugin` configuration. No action needed!

---

## 10. How to Explain the Project in a College Presentation

When presenting this project to professors, judges, or peers, highlight these 4 key pillars:

### 1. The Core Problem
*“In typical college canteens, students waste 15 to 25 minutes standing in crowded queues during short class breaks just to order and wait for food. Kitchen staff are overwhelmed with paper slips and cash counting errors.”*

### 2. Our Architecture
- **Backend (Spring Boot 3 + Java 21)**: Built following the 3-Tier Layered Architecture:
  - **Controllers**: Expose RESTful endpoints adhering to HTTP standards (`GET`, `POST`, `PUT`, `PATCH`).
  - **Services**: Enforce business validation, transaction boundaries (`@Transactional`), and order integrity (totals calculated strictly on the backend, never trusted from client inputs).
  - **Repositories & JPA**: Provide type-safe database queries and automated schema mapping.
- **Database (MySQL 8)**: Normalized relational schema with foreign key constraints, indexes on lookup fields (`order_number`, `status`, `phone`), and `DECIMAL(10,2)` for financial accuracy.
- **Frontend**: Responsive single-page application built with modern vanilla HTML5, CSS3, and JavaScript, featuring real-time polling every 5 seconds.

### 3. The Smart Queue Waiting-Time Estimator Algorithm
Explain how your queue estimator works:
$$\text{Total Wait Time} = \text{Base Order Preparation Time} + \left\lceil \frac{\sum \text{Backlog Time of Active Orders Ahead}}{\text{Kitchen Concurrency Factor}} \right\rceil$$
- **Active Orders Ahead**: Only counts orders with status `PLACED` or `PREPARING` that were created before the student's order. Completed and Cancelled orders are excluded.
- **Concurrency Factor**: Kitchen has 2 parallel cooking stations, dividing ahead queue delays realistically.
- **Transparency**: The UI explains the exact calculation to the student to build trust.

### 4. Simulated Demo Payment vs Real Payments
*“For security and presentation ethics, we clearly labeled simulated payments. We avoided fake promises of PCI-DSS compliance and instead engineered a transparent simulation for hackathon judges to verify the complete order-to-payment workflow.”*

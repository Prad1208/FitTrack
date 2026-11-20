# FitTrack - AI-Powered Fitness, Calorie Deficit & Gym Tracker

FitTrack is a full-stack, enterprise-grade fitness ecosystem combining precise calorie deficit calculation, progressive overload gym tracking, running metrics, visual analytics, and Google Gemini AI coaching.

---

## 🛠️ Tech Stack

* **Frontend**: React.js (Vite) + Tailwind CSS + Lucide Icons + Recharts
* **Backend**: Java 21 + Spring Boot 3.3.x + Spring Data JPA + Hibernate
* **Database**: MySQL 8.0
* **Security**: Spring Security 6 + Stateless JWT (JSON Web Tokens)
* **AI Engine**: Google Gemini API (`gemini-2.5-flash`)
* **API Documentation**: Springdoc OpenAPI / Swagger (`/swagger-ui.html`)
* **DevOps**: Docker & Docker Compose

---

## 🚀 Quick Start with Docker Compose

The fastest way to spin up the entire application (MySQL + Spring Boot + React):

```bash
# 1. Clone or navigate to the directory
cd fittrack

# 2. Set your Gemini API Key in .env (or export it)
export GEMINI_API_KEY="your-gemini-api-key"

# 3. Launch all containers
docker-compose up --build
```

* **Frontend**: [http://localhost:5173](http://localhost:5173)
* **Backend API**: [http://localhost:8080](http://localhost:8080)
* **Swagger UI Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **MySQL Database**: `localhost:3306` (`fittrack_user` / `fittrack_password`)

---

## 💻 Running Locally Without Docker

### 1. Start MySQL
Ensure MySQL is running on port `3306` with database `fittrack_db`:
```sql
CREATE DATABASE fittrack_db;
CREATE USER 'fittrack_user'@'localhost' IDENTIFIED BY 'fittrack_password';
GRANT ALL PRIVILEGES ON fittrack_db.* TO 'fittrack_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Run Spring Boot Backend (Java 21)
```bash
cd fittrack-backend
./mvnw spring-boot:run
```
*(On Windows: `mvnw.cmd spring-boot:run` or open in IntelliJ IDEA / VS Code)*

### 3. Run React Frontend (Vite)
```bash
cd fittrack-frontend
npm install
npm run dev
```

---

## ⚡ Default Demo Account

The backend automatically seeds a demo user on first boot:
* **Email**: `demo@fittrack.com`
* **Password**: `password123`
*(Or use the 1-Click "Instant Demo Sign In" button on the login screen!)*

---

## 🧠 Core Features & Architecture

1. **Central Dashboard**: Daily calorie intake, burn estimate, calorie deficit gauge ($TDEE - Intake$), macronutrient progress bars, today's workout & running cards.
2. **Nutrition & Food Tracker**: Search pre-seeded staple foods (chicken, eggs, oats, paneer, tofu, rice, etc.) with accurate per-100g nutritional profile, log exact grams for Breakfast, Lunch, Dinner, or Snacks, or add custom foods.
3. **Calorie Deficit Calculation Engine**: Mifflin-St Jeor BMR formula + TDEE multiplier + strict anti-double-counting policy.
4. **Gym Workout Tracker**: Targeted muscle building across **Chest, Back, Shoulders, Legs, Biceps, Triceps, Abs & Core**. Log sets, reps, weight, RPE, with auto-calculated 1RM (Epley formula), total volume load, and an integrated Rest Timer (60s, 90s, 120s, 180s).
5. **Running Tracker**: Distance, duration, pace calculation ($\text{min/km}$), estimated calorie burn ($1.036 \times Weight \times Distance$), 2K/5K/10K milestone goals, and personal best records.
6. **AI Fitness & Coaching Suite (Google Gemini API)**:
   * **AI Gym Workout Planner**: Structured periodized routines (PPL, Upper/Lower, Bro Split, Full Body).
   * **AI Nutrition & Meal Suggestions**: Tailored meals matching exact remaining calories and protein targets with gram quantities.
   * **AI Running Coach**: Progressive running schedules (Couch to 5K, 10K, HIIT intervals).
   * **AI Fitness Assistant**: Grounded chatbot answering form, recovery, substitutions, and plateau questions.
7. **Progress & Analytics (Recharts)**: Calorie Deficit progression area chart, muscle volume distribution bar chart, and protein consistency tracking.

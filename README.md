# SteadyMind

SteadyMind is a compassionate, self-guided web application designed to help individuals track their urges, practice delaying compulsions, and build healthier habits at their own pace.

## Problem Statement
Living with strong urges or compulsions (such as in OCD, anxiety, or habit disorders) can be overwhelming. Standard therapy isn't always accessible 24/7. People need a private, secure, and structured way to log their moments of high anxiety, practice "sitting with the discomfort" (delaying compulsions), and track their progress over time.

## Key Features
- **Moment Logs:** Track triggers, obsessions, compulsions, and anxiety intensity (before and after).
- **Delay Timer:** A guided timer to practice delaying a compulsion (ERP technique), complete with coping strategy logging.
- **Growth Challenges:** Set and track exposure tasks.
- **Progress Analytics:** Visual dashboard of urge trends, most common triggers, and intensity distribution.
- **AI Wellness Coach:** A controlled, non-diagnostic AI assistant to help reflect on progress and suggest next steps safely.

## Tech Stack
- **Backend:** Java 25, Spring Boot 4.x, Spring MVC, Spring Data JPA
- **Database:** H2 (Test), MySQL (Production ready)
- **Frontend:** HTML5, Thymeleaf, Vanilla CSS, Bootstrap 5.3, Chart.js
- **AI Integration:** Spring AI with OpenAI GPT models

## Architecture Overview
The application follows a standard Layered Architecture:
- **Controllers:** Handle HTTP requests and routing (Thymeleaf MVC + REST APIs).
- **Services:** Contain business logic, analytics calculations, and AI prompt engineering.
- **Repositories:** Spring Data JPA interfaces for database operations.
- **Entities / DTOs:** Data modeling and safe data transfer objects.

## AI Integration & Safety Guardrails
The AI Coach uses a strict classification pipeline:
1. **Intent Analysis:** Determines if the user is seeking reassurance, in crisis, or asking a safe reflection question.
2. **Safety Routing:** If crisis/self-harm or reassurance-seeking is detected, the AI uses hardcoded static safe responses and *does not* generate a generative response.
3. **Structured Prompting:** Generative responses are strictly instructed to never diagnose, cure, or give medical advice.
4. **UI Disclaimers:** All AI and analytics screens include prominent disclaimers that the tool is for self-reflection only.

## Setup Instructions

### Environment Variables
For AI features, set your OpenAI API key in the environment or `application.properties`:
```
SPRING_AI_OPENAI_API_KEY=your_api_key_here
```
*Note: The project uses a `mock-ai` profile by default for local testing without an API key.*

### Database Setup
The project defaults to an H2 in-memory database for immediate testing. To use MySQL, update `application.properties` with your database credentials.

### How to Run Locally
1. Clone the repository.
2. Navigate to the project root.
3. Run with Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access the application at `http://localhost:8080`.

### Testing
To run the automated test suite:
```bash
./mvnw clean test
```

## Manual Demo Flow (For Interviews)
1. Navigate to `http://localhost:8080`.
2. Click **Get Started** to view the mockup register page, then proceed to **Log In**.
3. On the **Dashboard**, view the summary of your progress.
4. Click **Log a Moment** to create an Urge Log. Note the trigger and intensity.
5. Navigate to the **Delay Timer** via the Quick Actions.
6. Start a 5-minute delay timer, select a coping strategy, and complete it.
7. Go to the **AI Coach** and ask a reflection question like "How am I doing with my triggers this week?"
8. Navigate to **Progress Analytics** to view Chart.js visualizations.
9. Click **Download Report** to see the printable progress report.

## Screenshots
*(Add screenshots of Dashboard, AI Coach, and Delay Timer here)*

---

*This project is for demonstration and educational purposes only. It is not a medical device.*

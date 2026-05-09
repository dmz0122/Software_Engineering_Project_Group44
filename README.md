# TA Recruit

Sprint 4 implementation of the Group 44 TA management system.

## Tech Stack

- Java 21
- Maven
- Java Swing
- CSV file persistence in `data/`

## Implemented Scope

- Applicant profile creation and update
- CV upload, open and download
- Browse available jobs
- View job detail
- Apply for jobs with duplicate protection
- Organiser review of applicants
- Organiser vacancy publishing with preview
- Organiser selection of applicants with status update
- Admin workload monitoring by semester
- Admin skill matching with explainable analytics
- Applicant missing-skill insights by semester
- AI-assisted workload suggestions for admins
- Applicant list CSV export for recruitment analysis
- Activity log viewing for admin audit and support

## Run

```bash
mvn exec:java
```

## AI Runtime

- Runtime AI settings live in `src/main/resources/llm.properties`
- Set `ai.enabled=false` if you want to run the app without live LLM calls
- The application still keeps baseline rule-based analytics when AI is disabled

## Demo Accounts

- Applicant without profile: `newta@school.edu / password123`
- Applicant with seeded data: `amy@school.edu / password123`
- Applicant with seeded data: `bob@school.edu / password123`
- Module organiser: `mo@school.edu / password123`
- Admin: `admin@school.edu / password123`

## Data Files

The app creates and updates CSV files in `data/`:

- `users.csv`
- `profiles.csv`
- `jobs.csv`
- `applications.csv`
- `notifications.csv`
- `activity_logs.csv`
- uploaded CVs in `data/uploads/`

# TA Recruit

Sprint 2 implementation of the Group 44 TA management system.

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

## Run

```bash
mvn exec:java
```

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
- uploaded CVs in `data/uploads/`

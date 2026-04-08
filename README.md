# TA Recruit

Sprint 1 implementation of the Group 44 TA management system.

## Tech Stack

- Java 21
- Maven
- Java Swing
- CSV file persistence in `data/`

## Implemented Sprint 1 Scope

- Applicant profile creation and update
- CV upload, open and download
- Browse available jobs
- View job detail
- Organiser review of applicants
- Organiser selection of applicants with status update

## Run

```bash
mvn exec:java
```

## Demo Accounts

- Applicant without profile: `newta@school.edu / password123`
- Applicant with seeded data: `amy@school.edu / password123`
- Applicant with seeded data: `bob@school.edu / password123`
- Module organiser: `mo@school.edu / password123`

## Data Files

The app creates and updates CSV files in `data/`:

- `users.csv`
- `profiles.csv`
- `jobs.csv`
- `applications.csv`
- `notifications.csv`
- uploaded CVs in `data/uploads/`

## Team Members

| Name | QMID | GitHub Username |
|------|------|------|
| Mingzhe Dai | QMID 231220976 | azhe616 |
| Yichen Wu | QMID 231220884 | qwdqd122 |
| Jingwen Zhang | QMID 231222914 | zhangjingwen1201 |
| Linxuan Cai | QMID 231221641 | cailinxuan |
| Zhuqing Zhang | QMID 231221308 | zzq1205 |
| Weicheng Sun | QMID 231220552 | Sun-Weicheng |
# Instaclone

Youtube demo: [(https://www.youtube.com/watch?v=QD45dtUXiKw)]

Instaclone is a web application inspired by Instagram, built with a Spring Boot Java backend, an Angular frontend, and a MySQL database. It provides a platform for users to share photos, interact with friends, and experience the core features of a social media app.

## Key Features

- **User Authentication:** Secure user registration and login with JWT (JSON Web Tokens).
- **Image Sharing:** Create and share posts with photos, captions, and location information.
- **Interactions:** Like and comment on posts, view who liked your posts.
- **Profile Management:** Edit your bio, change your profile picture, and delete your profile image.
- **Real-time Chat:** Engage in direct messaging with other users using WebSockets.

## Technologies Used

- **Backend:** Spring Boot, Spring Security, Spring Data JPA, MySQL
- **Frontend:** Angular, TypeScript, HTML, CSS
- **Authentication:** JWT (JSON Web Tokens)
- **Real-time Communication:** WebSockets (Stomp)
- **Database:** MySQL

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Node.js and npm (or yarn)
- MySQL database server

### Installation and Running

**Clone the Repository:**
   ```bash
   git clone https://github.com/Quolen/Instaclone.git
```
   
**Change into the project backend directory**
```bash
cd backend
./mvnw spring-boot:run 
```

**Change into the project frontend directory**
```bash
cd frontend
npm install
ng serve
```

TEAM TASK MANAGER

OVERVIEW
Team Task Manager is a full-stack web application designed to help teams efficiently organize projects, assign tasks, and track progress. The application features a robust role-based access control system, ensuring secure and structured collaboration between Administrators and Members.

TECH STACK
Frontend:
- React.js (v19)
- Vite (Build Tool)
- React Router DOM (Navigation)
- Axios (API requests)
- Express.js (Custom production server for static routing on Railway)

Backend:
- Java 21
- Spring Boot (v3.2.5)
- Spring Security (JWT-based Authentication)
- Spring Data JPA & Hibernate
- MySQL (Database)

FEATURES
- User Authentication: Secure signup and login using JWT (JSON Web Tokens).
- Role-Based Access Control: 
  - Admin: Can create projects, manage team members, and oversee all tasks.
  - Member: Can view assigned tasks, update task statuses, and track project progress.
- Project Management: Create and organize tasks within specific projects.
- Task Tracking: Update task statuses (e.g., Pending, In Progress, Completed).

GETTING STARTED (LOCAL DEVELOPMENT)

Prerequisites:
- Java 21
- Node.js (v20+)
- MySQL Database

Backend Setup:
1. Navigate to the "backend" directory.
2. The "src/main/resources/application.properties" uses environment variables. Set them in your environment or replace them locally for testing:
   MYSQLHOST=localhost
   MYSQLPORT=3306
   MYSQLDATABASE=task_manager_db
   MYSQLUSER=root
   MYSQLPASSWORD=your_local_password
3. Run the Spring Boot application using your IDE or Maven:
   ./mvnw spring-boot:run

Frontend Setup:
1. Navigate to the "frontend" directory.
2. Install dependencies:
   npm install
3. Start the Vite development server:
   npm run dev
4. Access the application at https://team-task-manager-omega-one.vercel.app.

DEPLOYMENT (RAILWAY)
This application is fully configured for deployment on Railway.app.

1. Database: Provision a Railway MySQL plugin.
2. Backend: Deploy the backend directory. In the variables tab, copy the raw variables from the MySQL plugin (MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD).
3. Frontend: Deploy the frontend directory. Ensure you set the VITE_API_URL variable to point to your live Backend URL (e.g., https://your-backend.up.railway.app).
4. Networking: The frontend utilizes a custom server.js Express file to perfectly bind to Railway's internal ingress network and seamlessly support React Router fallback routing.

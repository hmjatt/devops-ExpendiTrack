# DevOps-ExpendiTrack

## Overview

ExpenseTracker is an ambitious project aiming to leverage the Spring Boot framework, MySQL database, and React to create a robust web application dedicated to seamless budgeting and expense tracking. This comprehensive solution provides an intuitive user experience and efficient financial management. The project comprises various components and tasks that collectively contribute to its functionality.

### Components

**Agile-Board:**

- Jira
  - <https://jill.hc-sc.gc.ca/jira/browse/DO-469>

**Front-End:**

- ReactJS

**Backend:**

- Java (Spring Boot Maven)

**Database:**

- MySQL

**Internationalization:**

- i18next
- react-i18next

**API Documentation:**

- Swagger

**Test Cases:**

- Confluence
  - ***Backend:***
    - <https://jill.hc-sc.gc.ca/confluence/display/~seid/User+Test>
    - <https://jill.hc-sc.gc.ca/confluence/display/~seid/Budget++test>
    - <https://jill.hc-sc.gc.ca/confluence/display/~seid/Expenses+test>
  
  - ***Frontend:***
    - <https://jill.hc-sc.gc.ca/confluence/display/~hmatharo/Frontend+Testing+Documentation>

### CI/CD Pipeline

- Code Repository:
  - GitHub

- Testing (run tests on each pull request):
  - **Unit Testing, Integration Test:**
    - Backend -> JUnit
    - Frontend -> Jest and React Testing Library
  - **End-to-end testing (E2E):**
    - Selenium (inside Docker container)

- Build Tools:
  - Maven for Spring Boot
  - NPM for React

- Container:
  - Docker

- Image/Artifact Repository:
  - JFrog Artifactory

- Build Automation:
  - GitHub Actions
  - Jenkins

- Deployment Server:
  - Azure VM

- Code Quality Assurance tool:
  - SonarQube

- Infrastructure-as-code:
  - Terraform

## Running the App Locally

### Prerequisites
- Java (version 17)
- Maven (version 3.8+)
- Node.js (version 20.x)
- npm or yarn
- MySQL
- Docker

### Backend Setup
1. **Clone the repository:**
    ```sh
    git clone https://github.com/your-repo/devops-expenditrack.git
    cd devops-expenditrack/backend
    ```
2. **Build the backend:**
    ```sh
    mvn clean install
    ```
3. **Run the backend server:**
    ```sh
    mvn spring-boot:run
    ```

### Frontend Setup
1. **Clone the repository:**
    ```sh
    git clone https://github.com/your-repo/devops-expenditrack.git
    cd devops-expenditrack/frontend
    ```
2. **Install dependencies:**
    ```sh
    npm install
    ```
3. **Run the frontend server:**
    ```sh
    npm start
    ```

### Database Setup
1. **Set up MySQL:**
    - Create a database named `Expendi`.
    - Set up environment variables for MySQL:
      ```sh
      export MYSQL_DATABASE=Expendi
      export MYSQL_USER=sa
      export MYSQL_PASSWORD=password
      export MYSQL_ROOT_PASSWORD=password
      ```
    - Run the SQL scripts located in `db/scripts` to initialize the schema and data.

### Docker Setup
1. **Build and run Docker containers:**
    ```sh
    docker-compose up --build
    ```

## CI/CD Pipeline Overview

The CI/CD pipeline for ExpenseTracker ensures continuous integration and deployment through a series of automated steps:

1. **Triggering the Pipeline:**
   - The pipeline is triggered on pull requests targeting the `main` and `Analytics` branches.

2. **Backend Testing:**
   - Uses GitHub Actions to set up JDK 17, build the backend using Maven, and run backend tests.

3. **Frontend Testing:**
   - Uses GitHub Actions to set up Node.js, install dependencies, build the frontend, and run frontend tests.

4. **Selenium End-to-End Testing:**
   - Uses Docker Compose to start necessary services (database, backend, frontend) and runs Selenium tests to verify end-to-end functionality.
   - Logs are monitored for test completion, and services are cleaned up after tests.

5. **Continuous Deployment:**
   - Jenkins is configured to handle continuous deployment, integrating with Terraform for infrastructure provisioning and SonarQube for code quality analysis.

## Branches and Features

### Main Branch (`main`)
The `main` branch serves as the primary branch containing the basic version of the application. It includes the following features:
- Basic application setup
- Initial GitHub workflow for Selenium tests
- Initial commit and basic configuration files
- Docker-compose setup for deployment
- Exceptions tests and other fundamental features

### Analytics Branch (`analytics`)
The `analytics` branch focuses on adding analytics and charts to the application. Features include:
- Integration of analytics tools and libraries
- Development of dashboards and charts for data visualization
- Enhancements to support backend data analysis and reporting
- UI/UX improvements for displaying analytical data

### Jenkins Branch (`jenkins`)
The `jenkins` branch includes the following features:
- Jenkins pipeline setup for continuous integration and deployment
- Adding Jenkins configuration files and scripts
- Integration with backend, frontend, database, and Selenium branches
- Deployment and testing automation using Jenkins

### SonarQube Branch (`sonarqube`)
The `sonarqube` branch focuses on integrating SonarQube for code quality analysis. Features include:
- SonarQube analysis for backend and frontend code
- Dockerfile setup for SonarQube integration
- Jenkins pipeline stages for SonarQube code analysis
- Automated code quality checks and reports

### Terraform Branch (`terraform`)
The `terraform` branch is dedicated to deploying the application using Terraform. Key features include:
- Terraform scripts for infrastructure provisioning
- Jenkins pipeline integration for automated deployment using Terraform
- Environment-specific configurations for deploying different stages
- Enhancements to support backend, frontend, and database deployments

## Getting Started
To get started with the ExpenseTracker project, follow the instructions in the respective branches' README files for setup and deployment details.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

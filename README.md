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

- Testing(run tests on each pull request):
  - **Unit Testing, Integration Test:**
    - Backend -> JUnit
    - Frontend -> Jest and React Testing Library
  - **End to end testing (E2E):**
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

## Contributing
Contributions are welcome! Please read the [CONTRIBUTING](CONTRIBUTING.md) file for guidelines on how to contribute to this project.

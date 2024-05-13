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

**Internationalizaion:**

- i18next
- react-i18next

**Api Documentation:**

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
    - Selenium(inside Docker container)

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

- infrastructure-as-code:
  - Terraform

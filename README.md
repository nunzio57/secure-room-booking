# Secure Room Booking System

## Project Overview

This project is an enterprise-grade Web Application designed for the management of university room reservations. The primary focus of this development was to implement a secure 3-tier architecture, integrating industry-standard protocols for authentication and authorization.

The application moves beyond simple role-based access control (RBAC) by implementing **Attribute-Based Access Control (ABAC)** using **XACML**, centralized identity management via **OpenLDAP**, and perimeter security through an **Apache Reverse Proxy** with HTTPS enforcement.

## Architectural Design

The system follows a modular monolithic architecture backed by containerized services:

1.  **Presentation & Perimeter Layer:**
    * **Apache HTTP Server 2.4:** Acts as a Reverse Proxy and SSL/TLS termination point. It handles incoming HTTPS requests, enforces security headers (HSTS, X-Frame-Options), and forwards sanitized traffic to the backend.
2.  **Application Layer:**
    * **Spring Boot 3.2 (Java 17):** Handles business logic, input validation, and serves dynamic HTML content via Thymeleaf. It hosts the embedded XACML Policy Decision Point (PDP).
3.  **Data & Identity Layer:**
    * **OpenLDAP (Docker):** Stores user identities and group memberships.
    * **PostgreSQL (Docker):** Provides persistent storage for reservation data.

## Security Features

### 1. Authentication via LDAP
The application delegates user authentication to an external OpenLDAP directory running in a Docker container.
* **Standard:** Usage of `inetOrgPerson` for users and `groupOfNames` for groups.
* **Integration:** Spring Security LDAP is configured to perform a bind authentication using a service manager account.
* **Role Mapping:** LDAP groups (e.g., `cn=ADMINISTRATORS`) are automatically mapped to internal application authorities (e.g., `ROLE_ADMINISTRATORS`) via specific search filters.

### 2. Authorization via XACML (ABAC)
Fine-grained authorization is implemented using the **OASIS XACML 3.0 standard**.
* **Library:** Integration of the **WSO2 Balana** engine directly into the Spring Boot service layer.
* **Architecture Components:**
    * **PEP (Policy Enforcement Point):** Implemented in the Spring `RoomController`. It intercepts critical requests (e.g., `DELETE /booking`).
    * **PDP (Policy Decision Point):** A dedicated `XacmlService` that evaluates requests against active policies.
    * **PAP (Policy Administration Point):** XML policy files stored securely in `src/main/resources/policies`.
    * **PIP (Policy Information Point):** The Spring Security Context acts as the attribute source for user roles.
* **Policy Logic:** A "Deny-by-default" strategy is strictly enforced. Only requests matching specific attributes (Role=`ADMIN` AND Action=`delete`) result in a `Permit` decision.

### 3. Network Security & Hardening
* **Reverse Proxy:** The backend application runs on `localhost:8081` and is not directly exposed to the external network. Access is only possible through the Apache Web Server.
* **HTTPS:** All traffic is encrypted using TLS 1.2/1.3. HTTP traffic is permanently redirected to HTTPS (Status 301).
* **Secret Management:** Sensitive credentials (database passwords, LDAP secrets) are not hardcoded. They are injected via Environment Variables defined in a `.env` file (excluded from version control).

## Technology Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.2.0, Spring Security, Spring Data JPA
* **Template Engine:** Thymeleaf
* **Identity Provider:** OpenLDAP (osixia/openldap image)
* **Database:** PostgreSQL 15
* **Authorization Engine:** WSO2 Balana (XACML 3.0)
* **Web Server:** Apache HTTP Server 2.4
* **Containerization:** Docker & Docker Compose


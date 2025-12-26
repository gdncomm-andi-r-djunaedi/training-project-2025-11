# training-project-2025-11
requirements : https://gdncomm.atlassian.net/wiki/spaces/GDNIT/pages/1787920458/2025+QA+to+BE+Conversion+Program+-+Final+Project

Documentation of Chaithra.R project: https://gdncomm.atlassian.net/wiki/spaces/~61c0168768926d0068861b64/pages/1800208505/Design+documentation+for+Online+Marketplace
# 2025 QA to BE Conversion Program - Final Project

## Table of Contents

- [Objective](#objective)
- [Constraint](#constraint)
- [Business Requirement](#business-requirement)
- [Use Case API](#use-case-api)
- [Technical Requirement](#technical-requirement)
- [Architecture Diagrams](#architecture-diagrams)
- [Process](#process)
- [Project Competition](#project-competition)
- [Extra Challenge](#extra-challenge)

## Objective

To get hands-on experience in building end to end Java and Spring project from scratch:

- Learning opportunity to incorporate all the material learned during bootcamp
- Simulate real world software engineering process

## Constraint

- **Individual project**
- Will **not be scored or evaluated** (but best projects will be selected for presentation)
- Can use cursor or antigravity (or other AI vibe code tools), but you need to **understand how the generated code works**

## Business Requirement

Build **online marketplace** platform

## Use Case API

### 1. Customer Registration and Login

- Customer can **register**, **login**
  - Customer password should be **hashed** using built-in password hash library from spring framework
  - Password validation should also use this built-in validation library from spring framework
  - Login can use oauth token in **JWT/JWS**, use symmetric/asymmetric key

### 2. Product Search and Browsing

- Customer can **search product**, **view product list** and **view product detail**
  - Search product and view product list result should be **paginated**
  - Search can support **wildcard search**

### 3. Shopping Cart Management

- Customer can **add product to shopping cart**
  - Must be **logged-in**
  - No need for inventory stock checking, assume stock is **unlimited**

- Customer can **view shopping cart** and **delete product from shopping cart**
  - Must be **logged-in**
  - Login session validation via **jwt cookie** or **jwt header**

### 4. Logout

- Customer can **logout**
  - Invalidate jwt token and/or cookie after logout

## Technical Requirement

### Microservices Architecture

- Minimum **4 microservices**:
  - **api gateway**
  - **member**
  - **product**
  - **cart**

### Development Scope

- Only need to develop **API**, no need to build UI or client side

### Authentication & Authorization

- Use **API gateway** for authN and authZ implemented also as java web

### Search Implementation

- Search can be performed in **postgres** or **mongodb** directly
- Use **elasticsearch** is optional, only if you want more challenge

### Technology Stack

- Use **java** and **spring**
- Use **postgres** for relational, **mongo** for document and **redis** for cache
- You can decide between relational or document based, which one is more suitable for each microservice domain model structure

### Testing

- Write **unit test** and **integration test**

### Data Size

- **member**: minimum **5,000 members**
- **product**: minimum **50,000 products**

## Architecture Diagrams

### Deployment Architecture

![Deployment Diagram](Screenshot%202025-11-27%20at%2014.53.50%20(1).png)

The deployment diagram shows:
- API Gateway as the single entry point for all client requests
- Three backend microservices: Member, Product, and Cart
- All requests flow through the API Gateway to backend services

### Authentication & Authorization Flow

![Authentication Sequence Diagram](auth-sequence-diagram_2%20(1).png)

The sequence diagram illustrates:

**Registration Flow:**
1. Customer sends POST /register to API Gateway
2. API Gateway forwards registration to Member Service
3. Member Service hashes password and stores user + hash
4. Returns 201 Created

**Login Flow:**
1. Customer sends POST /login to API Gateway
2. API Gateway forwards credentials to Member Service
3. Member Service queries user and verifies password
4. API Gateway creates & signs JWT
5. Returns 200 OK with JWT in body and Set-Cookie header

**Authorization Options:**
- **Option 1: Cookie-Based** - JWT extracted from Cookie
- **Option 2: Header-Based** - JWT extracted from Authorization header (Bearer token)

**Logout Options:**
- **Option 1: Cookie-Based** - Send Set-Cookie with Max-Age=0
- **Option 2: Header-Based** - Client discards token (stateless)

**Key Security Points:**
- Password hashing: Only in Member Service (bcrypt/argon2)
- JWT creation: Only in API Gateway (login with secret key)
- JWT validation: Only in API Gateway (validates signature & expiration)
- JWT delivery: Both in response body AND Set-Cookie header
- JWT extraction: Gateway checks BOTH Cookie AND Authorization header
- Cookie attributes: HttpOnly, Secure, SameSite=Strict
- Header format: Authorization: Bearer <JWT>
- JWT payload: {user_id, roles, exp, iat}
- Services trust: Gateway sends validated user_id to services
- Cookie logout: Gateway sends Set-Cookie with Max-Age=0
- Header logout: Client discards JWT token from storage
- Stateless auth: No server-side session storage needed

## Process

### Repository and Git Flow

1. Fork this repo to your gdncomm gh account, and PR back to this repo:
   - `gdncomm-andi-r-djunaedi/training-project-2025-11`

2. Follow **blibli git flow** with release branch and feature branch on your fork repo

3. PR back the release branch to the original repo

4. Can add your squad lead and team member as code reviewer to help review your code

### Timeline

- **Design and development timeline**: 5 days (1-5 December 2025)

## Project Competition

Best **3 project from each development site** (best 3 from Jakarta and best 3 from Bangalore with total 6 project) will be selected to present their project, you can consider this as **Mini Hackathon**

### Judge

- Phani
- Andi
- Shandy

### Project Evaluation Criteria

Projects will be evaluated based on:

1. **Functional completeness and correctness**, zero or minimum bugs

2. **System design** (API Design, DB Design)

3. **Code cleanliness, structure and organization**

4. **Non Functional aspect consideration** such as:
   - Security
   - Performance
   - Observability
   - Testability
   - etc

## Extra Challenge

**Optional, for those who likes to live on the bleeding edge:**

1. All microservices **dockerized**, can run on **Kubernetes**

2. Use **ElasticSearch** or **SolR** for Search data store

3. Use **gRPC** instead of REST API

4. Implement **Design Pattern** in your code, should be well suited with the problem solved but not reinvent the wheel

---

**Source:** This document is converted from : https://gdncomm.atlassian.net/wiki/spaces/GDNIT/pages/1787920458/2025+QA+to+BE+Conversion+Program+-+Final+Project

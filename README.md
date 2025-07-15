# SocialApp - Secure Social Media Platform

A secure, full-stack social media application with end-to-end encrypted messaging, OTP verification, admin moderation, and HTTPS security.  
**Built for the FCS course by Group 29 (Sharad Jain & Akash Singh).**

---

## 🚀 Features

- **Secure Authentication**: OTP-based signup, JWT login sessions  
- **Encrypted Messaging**: One-to-one and group chats with file sharing  
- **Admin Dashboard**: Approve/suspend users, moderate content  
- **Responsive UI**: Angular frontend with TypeScript interfaces  
- **Security**: HTTPS (SSL/TLS), input validation, audit logging  

---

## 🛠 Tech Stack

| Component    | Technology                        |
|--------------|------------------------------------|
| Frontend     | Angular (TypeScript)              |
| Backend      | Spring Boot (Java)                |
| Database     | MySQL                             |
| Web Servers  | Nginx (Frontend), Tomcat (Backend)|
| Hosting      | Ubuntu VM                         |

---

## ⚙️ Quick Setup

### 📦 Prerequisites

- Java 23  
- MySQL 8.0+  
- Node.js 18+ & Angular CLI  
- Nginx (for frontend hosting)  

---

## 📥 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/DOUBLEAKASHSINGH/SocialApp.git --recursive
cd SocialApp
```

---

### 2. Backend Setup (Spring Boot)

```bash
cd backend
mvn clean install
java -jar target/fcs29-0.0.1-SNAPSHOT.jar
```

> **Configure:** Edit `application.properties` for MySQL credentials.

---

### 3. Frontend Setup (Angular)

```bash
cd ../frontend
npm install
ng build --prod
```

> **Deploy:** Copy `dist/` contents to `/var/www/html` and configure Nginx (see below).

---

### 4. Database Setup

Run the provided SQL script to initialize tables:

```bash
mysql -u root -p fcs29 < script.sql
```

---

## 🌐 Nginx Configuration

Add this to your Nginx config (`/etc/nginx/sites-available/default`):

```nginx
server {
    listen 443 ssl;
    server_name your_domain.com;

    ssl_certificate /etc/ssl/certs/helloworld.crt;
    ssl_certificate_key /etc/ssl/private/helloworld.key;

    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

> Full config available in `nginx.txt`

---

## 🔧 Java Backend Service

```bash
sudo systemctl enable fcs29    # Auto-start on boot
sudo systemctl restart fcs29
```

---

## 📘 User Guide

- **Sign Up**: OTP sent to email (check spam folder)  
- **Login**: Admin must verify your account first  
- **Chat**: Send messages/media in groups or 1:1 chats  
- **Admin**: Access `/admin` to manage users  

> **Note**: Browsers may warn about self-signed HTTPS certificates. Click **"Advanced" → "Proceed"**.

---

## 🛡 Security Highlights

- HTTPS with TLS 1.3  
- BCrypt password hashing  
- Rate limiting to prevent brute-force attacks  
- XSS/SQL Injection protection  

---

## 📄 Documentation

- **Project Report** – Technical deep dive  
- **User Manual** – Step-by-step usage  

---

## 🤝 Contributing

Pull requests welcome. For major changes, open an issue first to discuss what you'd like to change.

---

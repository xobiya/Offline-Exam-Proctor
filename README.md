# 🖥️ Offline Exam Proctor System

A secure, offline desktop-based exam monitoring system developed using Java, Java Swing, and MySQL. This project enables administrators to manage exams and monitor student behavior during tests—without needing an internet connection.

## 📌 Project Overview

**Platform:** Desktop Application  
**Technology Stack:** Java, Java Swing, MySQL  
**IDE:** Apache NetBeans  
**Database:** MySQL (Local)  
**Architecture:** Layered (MVC)

## 🎯 Objective

The Offline Exam Proctor System ensures secure, monitored exams in an offline environment. It allows:
- Admins to manage users, exams, and questions
- Students to take assigned exams
- Behavior monitoring through screenshot capture and activity logging
- Auto-evaluation and result management

---

## ⚙️ Features

### 👤 Authentication
- Admin & Student login/logout  
- Password encryption

### 📝 Exam & Question Management
- Create, edit, delete exams
- Assign exams to students
- Add/edit/delete MCQs

### 👨‍🎓 Student Management
- Register/edit/remove students
- Reset passwords

### 🧑‍💻 Exam Interface
- Timer-enabled exam window
- View assigned exams
- Submit answers

### 🎥 Proctoring & Monitoring
- Periodic screenshot capture
- Log inactivity or window switch events

### 📊 Result Management
- Auto-evaluation for MCQs
- Result storage and export (CSV/PDF)
- View performance reports

---

## 🧪 Non-Functional Requirements

- ⚡ **Performance:** <1s response time, supports 50+ users
- 🔒 **Security:** Encrypted credentials & role-based access
- 🎯 **Usability:** Intuitive UI for both admins and students
- 💾 **Reliability:** Prevent data loss on unexpected shutdown
- 💻 **Portability:** Cross-platform via Java Runtime
- 🛠️ **Maintainability:** Follows MVC, modular codebase

---

## 🧱 System Architecture

- **Presentation Layer:** Java Swing (GUI)
- **Business Logic Layer:** Core logic (e.g., timers, grading)
- **Data Access Layer:** JDBC + MySQL
- **Database:** MySQL tables for users, exams, questions, results, logs

---


---

## 🛠️ Setup Instructions

1. Install **Java JDK (8 or higher)**  
2. Install **MySQL Server (local instance)**  
3. Clone the repository:
   ```bash
   git clone https://github.com/xobiya/Offline-Exam-Proctor.git


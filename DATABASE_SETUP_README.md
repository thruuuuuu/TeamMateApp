# TeamMate System - Database Integration Setup Guide

## Overview
This guide will help you set up the database-enabled version of the TeamMate system.

## Prerequisites

1. **MySQL Server** (Version 5.7 or higher)
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Or use XAMPP/WAMP which includes MySQL

2. **MySQL JDBC Driver** (mysql-connector-java)
   - Download from: https://dev.mysql.com/downloads/connector/j/
   - Or add Maven dependency (see below)

3. **Java Development Kit** (JDK 8 or higher)

## Setup Steps

### Step 1: Install MySQL

1. Install MySQL Server on your system
2. Start the MySQL service
3. Note your MySQL root password (or create a new user)

### Step 2: Create Database

1. Open MySQL command line or MySQL Workbench
2. Run the SQL script located at `SQL/schema.sql`

```bash
mysql -u root -p < SQL/schema.sql
```

Or in MySQL Workbench:
- Open `SQL/schema.sql`
- Execute the script

This will:
- Create the `teammate_db` database
- Create all necessary tables (organizers, participants, teams, etc.)
- Insert a default organizer account (ID: ORG001, Password: admin123)

### Step 3: Add MySQL JDBC Driver

#### Option A: Manual JAR Addition
1. Download `mysql-connector-java-8.x.x.jar`
2. Add it to your project's classpath
   - In IntelliJ IDEA: File → Project Structure → Libraries → + → Java → Select JAR
   - In Eclipse: Right-click project → Build Path → Add External Archives

#### Option B: Maven (if using Maven)
Add to your `pom.xml`:

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### Step 4: Configure Database Connection

Edit `Database/DatabaseConnection.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/teammate_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_mysql_password"; // Change this!
```

### Step 5: Run the Application

```bash
javac -d bin src/**/*.java
java -cp bin Main.TeamMateApp
```

## File Structure

```
src/
├── Database/
│   ├── DatabaseConnection.java      # Database connection manager
│   ├── ParticipantDAO.java         # Participant data access
│   ├── TeamDAO.java                # Team data access
│   └── AuthenticationService.java  # Login/registration handling
├── Entity/
│   ├── Participant.java            # UPDATED with equals/hashCode
│   ├── Team.java
│   └── Organizer.java              # NEW - Organizer entity
├── Main/
│   ├── TeamMateApp.java           # UPDATED with authentication
│   ├── FormationStatistics.java   # UPDATED with getters
│   └── ...
├── Manager/
│   └── TeamManager.java           # UPDATED to use database
└── SQL/
    └── schema.sql                 # Database creation script
```

## Key Changes from Original System

### 1. **Authentication System**
- Organizers must log in with ID and password
- Participants log in with ID and password (format: Name + ID)
- New registration flow for both user types

### 2. **Data Persistence**
- All participant data stored in MySQL database
- Teams saved to database when formed
- CSV upload validates and inserts into database

### 3. **Password Format**
- Organizers: Custom password during registration
- Participants: Auto-generated as `[Name][ID]`
   - Example: John Doe with ID P12345 → Password: `JohnDoeP12345`

### 4. **Workflow**

#### Organizer Workflow:
1. Register/Login
2. Upload participants CSV → Validates and saves to DB
3. Form teams → Saves to database automatically
4. View teams/participants from database
5. Export teams to CSV (optional)

#### Participant Workflow:
1. Register (complete survey) → Saved to DB with auto-password
2. Login with ID and password
3. View profile and team assignment from database
4. Update profile → Updates in database

## Default Credentials

### Default Organizer Account:
- **ID:** ORG001
- **Password:** admin123
- **Email:** admin@teammate.com

You can create additional organizer accounts through the registration menu.

## Database Schema

### Tables Created:

1. **organizers** - Organizer accounts
2. **participants** - Participant profiles
3. **teams** - Team information
4. **team_members** - Team-participant relationships
5. **team_formations** - Formation history
6. **formation_teams** - Formation-team relationships

## Features

### ✅ What Works Now:

1. **CSV Upload with Database Storage**
   - Upload CSV → Validate → Insert into database
   - Automatic password generation for participants

2. **Team Formation**
   - Load participants from database
   - Form balanced teams
   - Save teams to database automatically

3. **Authentication**
   - Secure login for organizers
   - Secure login for participants
   - Registration for new users

4. **Profile Management**
   - View participant info from database
   - Update profile (saves to database)
   - View team assignments from database

5. **Data Export**
   - Export teams to CSV from database
   - Include remaining participants

## Troubleshooting

### Issue: "Failed to connect to database"
**Solution:**
- Verify MySQL is running
- Check username/password in `DatabaseConnection.java`
- Ensure database `teammate_db` exists

### Issue: "Table doesn't exist"
**Solution:**
- Run the `schema.sql` script to create tables
- Check that you're connected to the correct database

### Issue: "Duplicate entry" error
**Solution:**
- Participant IDs or emails must be unique
- Check CSV for duplicate entries

### Issue: MySQL JDBC Driver not found
**Solution:**
- Download and add `mysql-connector-java.jar` to classpath
- Or add Maven dependency

## Testing the System

### Test Data
A sample CSV file format:

```csv
ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType
P001,John Doe,john@test.com,Chess,7,Strategist,85,Balanced
P002,Jane Smith,jane@test.com,FIFA,8,Attacker,92,Leader
P003,Bob Johnson,bob@test.com,Basketball,6,Defender,65,Thinker
```

### Quick Test:
1. Login as organizer (ORG001 / admin123)
2. Upload test CSV
3. Form teams with size 3
4. View formed teams
5. Login as participant (use ID and generated password)
6. View team assignment

## Additional Notes

- All database operations are logged in `logs/teammate_system.log`
- Passwords are stored in plain text (for educational purposes)
   - In production, use proper password hashing (BCrypt, Argon2, etc.)
- Connection pooling is not implemented
   - For production, consider using HikariCP or Apache DBCP

## Support

For issues or questions:
1. Check the log file: `logs/teammate_system.log`
2. Verify database connection settings
3. Ensure all required tables exist

## Future Enhancements

Potential improvements:
- Password hashing for security
- Connection pooling
- Email verification
- Forgot password functionality
- Team formation history viewer
- Advanced analytics dashboard
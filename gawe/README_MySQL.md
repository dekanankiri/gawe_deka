# GAWE Employee Management System - MySQL Integration

## Overview
This version of the GAWE application has been modified to use MySQL database instead of in-memory storage, providing persistent data storage and better scalability.

## Prerequisites

### 1. MySQL Installation
- Install MySQL Server 8.0 or later
- Ensure MySQL service is running
- Default configuration assumes:
  - Host: `localhost`
  - Port: `3306`
  - Username: `root`
  - Password: `root` (default password - change in code if different)

### 2. Java Dependencies
- Java 17 or later
- Maven for dependency management
- JavaFX 17

## Database Setup

### Option 1: Automatic Setup (Recommended)
The application will automatically:
1. Create the `gawe_db` database if it doesn't exist
2. Create all required tables
3. Insert sample data for testing

Simply run the application and it will handle database initialization.

### Option 2: Manual Setup
If you prefer manual setup or encounter issues:

1. **Create Database:**
   ```sql
   CREATE DATABASE gawe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Run Setup Script:**
   Execute the `database_setup.sql` file in your MySQL client:
   ```bash
   mysql -u root -p < database_setup.sql
   ```

## Configuration

### Database Connection Settings
Edit the following constants in `MySQLDatabaseManager.java` if your MySQL setup differs:

```java
private static final String DB_HOST = "localhost";
private static final String DB_PORT = "3306";
private static final String DB_NAME = "gawe_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root"; // Default MySQL root password
```

### Connection Pool Settings
The application uses HikariCP for connection pooling with these default settings:
- Maximum pool size: 20 connections
- Minimum idle connections: 5
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Maximum lifetime: 30 minutes

## Running the Application

### 1. Compile and Run
```bash
cd gawe
mvn clean compile
mvn javafx:run
```

### 2. Alternative: Run with Maven
```bash
mvn clean javafx:run
```

### 3. Login Credentials
Use these sample credentials to test the application:

**Manager:**
- ID: `MNG001`
- Password: `password123`

**Supervisors:**
- ID: `SUP001` (HR), `SUP002` (Marketing), `SUP003` (Sales), `SUP004` (IT), `SUP005` (Finance)
- Password: `password123`

**Employees:**
- ID: `EMP001` to `EMP010`
- Password: `password123`

## Database Schema

### Core Tables
1. **employees** - Employee information and credentials
2. **kpi** - Key Performance Indicators by division
3. **reports** - Monthly division reports
4. **employee_evaluations** - Individual employee evaluations
5. **monthly_evaluations** - Monthly performance evaluations
6. **attendance** - Daily attendance records
7. **meetings** - Meeting information
8. **meeting_participants** - Meeting participant relationships
9. **leave_requests** - Leave request applications
10. **salary_history** - Monthly salary calculations

### Key Features
- **Foreign Key Constraints** - Ensures data integrity
- **Unique Constraints** - Prevents duplicate records
- **Indexes** - Optimized for common queries
- **ENUM Types** - Controlled vocabulary for status fields
- **Timestamps** - Automatic creation and update tracking

## Features

### Role-Based Access Control
- **Manager** - Full system access, KPI management, report review
- **Supervisor** - Team management, employee evaluation, report submission
- **Employee** - Personal data, attendance, leave requests

### Integrated Workflows
- **Leave Requests** - Employee → Supervisor → Manager approval chain
- **Meeting Management** - Organizers can invite participants across roles
- **Performance Tracking** - KPI scores affect salary calculations
- **Report Management** - Monthly reports with approval workflow

### Data Persistence
- All data is stored in MySQL database
- Automatic backup and recovery capabilities
- Concurrent user support with connection pooling
- Transaction support for data consistency

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running: `sudo systemctl status mysql`
   - Check credentials in `MySQLDatabaseManager.java`
   - Ensure database exists: `SHOW DATABASES;`

2. **Permission Denied**
   - Grant privileges: `GRANT ALL PRIVILEGES ON gawe_db.* TO 'root'@'localhost';`
   - Flush privileges: `FLUSH PRIVILEGES;`

3. **Port Already in Use**
   - Change server port in `GaweServer.java`
   - Or kill existing process: `sudo lsof -ti:8080 | xargs kill -9`

4. **JavaFX Module Issues**
   - Ensure JavaFX is properly installed
   - Check module path configuration

### Performance Optimization

1. **Database Indexes**
   - All frequently queried columns are indexed
   - Composite indexes for multi-column queries

2. **Connection Pooling**
   - HikariCP provides efficient connection management
   - Configurable pool sizes based on load

3. **Query Optimization**
   - Prepared statements prevent SQL injection
   - Efficient JOIN operations for related data

## Development Notes

### Adding New Features
1. Create database migration scripts
2. Update model classes
3. Add DAO methods in `MySQLDataStore`
4. Update UI components
5. Test with sample data

### Database Migrations
When adding new tables or columns:
1. Create migration SQL script
2. Update `createTables()` method
3. Add sample data insertion
4. Test with fresh database

### Security Considerations
- Use prepared statements (already implemented)
- Validate user input
- Implement proper authentication
- Consider password hashing for production

## Production Deployment

### Database Security
1. **Change Default Passwords**
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'strong_password';
   ```

2. **Create Application User**
   ```sql
   CREATE USER 'gawe_app'@'localhost' IDENTIFIED BY 'app_password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON gawe_db.* TO 'gawe_app'@'localhost';
   ```

3. **Enable SSL**
   - Configure MySQL SSL
   - Update connection string with SSL parameters

### Application Configuration
1. **Environment Variables**
   - Move database credentials to environment variables
   - Use configuration files for different environments

2. **Logging**
   - Configure proper logging levels
   - Set up log rotation

3. **Monitoring**
   - Monitor database connections
   - Track application performance
   - Set up alerts for errors

## Support

For issues or questions:
1. Check the troubleshooting section
2. Verify database connectivity
3. Review application logs
4. Test with sample data

The application includes comprehensive error handling and logging to help diagnose issues quickly.
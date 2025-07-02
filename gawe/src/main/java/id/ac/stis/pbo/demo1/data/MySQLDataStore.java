package id.ac.stis.pbo.demo1.data;

import id.ac.stis.pbo.demo1.database.DatabaseConnection;
import id.ac.stis.pbo.demo1.database.DatabaseException;
import id.ac.stis.pbo.demo1.models.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * MySQL implementation of IDataStore interface
 */
public class MySQLDataStore implements IDataStore {
    private static final Logger logger = Logger.getLogger(MySQLDataStore.class.getName());
    private final DatabaseConnection dbConnection;

    // Monthly Evaluation inner class
    public static class MonthlyEvaluation {
        private int id;
        private String employeeId;
        private String supervisorId;
        private int month;
        private int year;
        private double punctualityScore;
        private double attendanceScore;
        private double productivityScore;
        private double overallRating;
        private String comments;
        private Date evaluationDate;

        // Constructors and getters/setters
        public MonthlyEvaluation() {}

        public MonthlyEvaluation(String employeeId, String supervisorId, int month, int year,
                                 double punctualityScore, double attendanceScore, double productivityScore,
                                 double overallRating, String comments) {
            this.employeeId = employeeId;
            this.supervisorId = supervisorId;
            this.month = month;
            this.year = year;
            this.punctualityScore = punctualityScore;
            this.attendanceScore = attendanceScore;
            this.productivityScore = productivityScore;
            this.overallRating = overallRating;
            this.comments = comments;
            this.evaluationDate = new Date();
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getSupervisorId() { return supervisorId; }
        public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public double getPunctualityScore() { return punctualityScore; }
        public void setPunctualityScore(double punctualityScore) { this.punctualityScore = punctualityScore; }
        public double getAttendanceScore() { return attendanceScore; }
        public void setAttendanceScore(double attendanceScore) { this.attendanceScore = attendanceScore; }
        public double getProductivityScore() { return productivityScore; }
        public void setProductivityScore(double productivityScore) { this.productivityScore = productivityScore; }
        public double getOverallRating() { return overallRating; }
        public void setOverallRating(double overallRating) { this.overallRating = overallRating; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        public Date getEvaluationDate() { return evaluationDate; }
        public void setEvaluationDate(Date evaluationDate) { this.evaluationDate = evaluationDate; }
    }

    public MySQLDataStore() {
        this.dbConnection = DatabaseConnection.getInstance();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Initialize database using MySQLDatabaseManager
            id.ac.stis.pbo.demo1.database.MySQLDatabaseManager dbManager = 
                new id.ac.stis.pbo.demo1.database.MySQLDatabaseManager();
            dbManager.initializeDatabase();
            logger.info("MySQL database initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize MySQL database: " + e.getMessage());
            throw new DatabaseException.InitializationException("Database initialization failed", e);
        }
    }

    @Override
    public Employee authenticateUser(String employeeId, String password) {
        String query = "SELECT * FROM employees WHERE id = ? AND password = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
        } catch (SQLException e) {
            logger.severe("Error authenticating user: " + e.getMessage());
            throw new DatabaseException.AuthenticationException("Authentication failed");
        }
        
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees ORDER BY nama";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all employees: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get employees", e);
        }
        
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByRole(String role) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE role = ? ORDER BY nama";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting employees by role: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get employees by role", e);
        }
        
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByDivision(String divisi) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE divisi = ? ORDER BY nama";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting employees by division: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get employees by division", e);
        }
        
        return employees;
    }

    @Override
    public Employee getEmployeeById(String id) {
        String query = "SELECT * FROM employees WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
        } catch (SQLException e) {
            logger.severe("Error getting employee by ID: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get employee by ID", e);
        }
        
        return null;
    }

    @Override
    public void updateEmployee(Employee employee) {
        String query = """
            UPDATE employees SET nama = ?, password = ?, role = ?, divisi = ?, jabatan = ?, 
            tgl_masuk = ?, sisa_cuti = ?, gaji_pokok = ?, kpi_score = ?, supervisor_rating = ?, 
            layoff_risk = ? WHERE id = ?
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employee.getNama());
            pstmt.setString(2, employee.getPassword());
            pstmt.setString(3, employee.getRole());
            pstmt.setString(4, employee.getDivisi());
            pstmt.setString(5, employee.getJabatan());
            pstmt.setDate(6, new java.sql.Date(employee.getTglMasuk().getTime()));
            pstmt.setInt(7, employee.getSisaCuti());
            pstmt.setDouble(8, employee.getGajiPokok());
            pstmt.setDouble(9, employee.getKpiScore());
            pstmt.setDouble(10, employee.getSupervisorRating());
            pstmt.setBoolean(11, employee.isLayoffRisk());
            pstmt.setString(12, employee.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating employee: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to update employee", e);
        }
    }

    @Override
    public List<KPI> getAllKPI() {
        List<KPI> kpiList = new ArrayList<>();
        String query = "SELECT * FROM kpi ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                kpiList.add(mapResultSetToKPI(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all KPI: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get KPI", e);
        }
        
        return kpiList;
    }

    @Override
    public List<KPI> getKPIByDivision(String divisi) {
        List<KPI> kpiList = new ArrayList<>();
        String query = "SELECT * FROM kpi WHERE divisi = ? ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                kpiList.add(mapResultSetToKPI(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting KPI by division: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get KPI by division", e);
        }
        
        return kpiList;
    }

    @Override
    public boolean saveKPI(String divisi, int bulan, int tahun, double score, String managerId) {
        String query = """
            INSERT INTO kpi (divisi, bulan, tahun, score, manager_id) 
            VALUES (?, ?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE score = VALUES(score), manager_id = VALUES(manager_id)
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            pstmt.setInt(2, bulan);
            pstmt.setInt(3, tahun);
            pstmt.setDouble(4, score);
            pstmt.setString(5, managerId);
            
            int result = pstmt.executeUpdate();
            
            // Update employee layoff risk based on KPI
            updateEmployeeLayoffRisk(divisi, score);
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving KPI: " + e.getMessage());
            return false;
        }
    }

    private void updateEmployeeLayoffRisk(String divisi, double kpiScore) {
        String query = "UPDATE employees SET layoff_risk = ?, kpi_score = ? WHERE divisi = ? AND role = 'pegawai'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setBoolean(1, kpiScore < 60.0);
            pstmt.setDouble(2, kpiScore);
            pstmt.setString(3, divisi);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating layoff risk: " + e.getMessage());
        }
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports ORDER BY upload_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all reports: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get reports", e);
        }
        
        return reports;
    }

    @Override
    public List<Report> getPendingReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE status = 'pending' ORDER BY upload_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting pending reports: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get pending reports", e);
        }
        
        return reports;
    }

    @Override
    public List<Report> getReportsByDivision(String divisi) {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE divisi = ? ORDER BY upload_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting reports by division: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get reports by division", e);
        }
        
        return reports;
    }

    @Override
    public boolean saveReport(String supervisorId, String divisi, int bulan, int tahun, String filePath) {
        String query = "INSERT INTO reports (supervisor_id, divisi, bulan, tahun, file_path) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, supervisorId);
            pstmt.setString(2, divisi);
            pstmt.setInt(3, bulan);
            pstmt.setInt(4, tahun);
            pstmt.setString(5, filePath);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving report: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateReportStatus(int reportId, String status, String managerNotes, String reviewedBy) {
        String query = "UPDATE reports SET status = ?, manager_notes = ?, reviewed_by = ?, reviewed_date = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, managerNotes);
            pstmt.setString(3, reviewedBy);
            pstmt.setInt(4, reportId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error updating report status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Attendance> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance ORDER BY tanggal DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all attendance: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get attendance", e);
        }
        
        return attendanceList;
    }

    @Override
    public List<Attendance> getAttendanceByEmployee(String employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY tanggal DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting attendance by employee: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get attendance by employee", e);
        }
        
        return attendanceList;
    }

    @Override
    public List<Attendance> getTodayAttendance(String employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? AND tanggal = CURDATE()";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting today's attendance: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get today's attendance", e);
        }
        
        return attendanceList;
    }

    @Override
    public boolean saveAttendance(String employeeId, Date tanggal, String jamMasuk, String jamKeluar, String status) {
        String query = """
            INSERT INTO attendance (employee_id, tanggal, jam_masuk, jam_keluar, status, is_late) 
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE jam_masuk = VALUES(jam_masuk), jam_keluar = VALUES(jam_keluar), 
            status = VALUES(status), is_late = VALUES(is_late)
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setDate(2, new java.sql.Date(tanggal.getTime()));
            pstmt.setTime(3, jamMasuk != null ? Time.valueOf(jamMasuk + ":00") : null);
            pstmt.setTime(4, jamKeluar != null ? Time.valueOf(jamKeluar + ":00") : null);
            pstmt.setString(5, status);
            pstmt.setBoolean(6, isLateArrival(jamMasuk));
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving attendance: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateAttendanceClockOut(String employeeId, String jamKeluar) {
        String query = "UPDATE attendance SET jam_keluar = ? WHERE employee_id = ? AND tanggal = CURDATE()";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setTime(1, Time.valueOf(jamKeluar + ":00"));
            pstmt.setString(2, employeeId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error updating clock out: " + e.getMessage());
            return false;
        }
    }

    private boolean isLateArrival(String jamMasuk) {
        if (jamMasuk == null) return false;
        try {
            String[] parts = jamMasuk.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour > 8 || (hour == 8 && minute > 30); // Late if after 08:30
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Meeting> getAllMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT m.*, GROUP_CONCAT(mp.participant_id) as participants 
            FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            GROUP BY m.id 
            ORDER BY m.tanggal ASC
        """;
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                meetings.add(mapResultSetToMeeting(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all meetings: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get meetings", e);
        }
        
        return meetings;
    }

    @Override
    public List<Meeting> getMeetingsByEmployee(String employeeId) {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT DISTINCT m.*, GROUP_CONCAT(mp2.participant_id) as participants 
            FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            LEFT JOIN meeting_participants mp2 ON m.id = mp2.meeting_id 
            WHERE m.organizer_id = ? OR mp.participant_id = ? 
            GROUP BY m.id 
            ORDER BY m.tanggal ASC
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                meetings.add(mapResultSetToMeeting(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting meetings by employee: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get meetings by employee", e);
        }
        
        return meetings;
    }

    @Override
    public List<Meeting> getUpcomingMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT m.*, GROUP_CONCAT(mp.participant_id) as participants 
            FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            WHERE m.tanggal >= CURDATE() 
            GROUP BY m.id 
            ORDER BY m.tanggal ASC
        """;
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                meetings.add(mapResultSetToMeeting(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting upcoming meetings: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get upcoming meetings", e);
        }
        
        return meetings;
    }

    @Override
    public boolean saveMeeting(String title, String description, Date tanggal, String waktuMulai,
                              String waktuSelesai, String lokasi, String organizerId, List<String> participantIds) {
        String meetingQuery = """
            INSERT INTO meetings (title, description, tanggal, waktu_mulai, waktu_selesai, lokasi, organizer_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        String participantQuery = "INSERT INTO meeting_participants (meeting_id, participant_id) VALUES (?, ?)";
        
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement meetingStmt = conn.prepareStatement(meetingQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement participantStmt = conn.prepareStatement(participantQuery)) {
                
                // Insert meeting
                meetingStmt.setString(1, title);
                meetingStmt.setString(2, description);
                meetingStmt.setDate(3, new java.sql.Date(tanggal.getTime()));
                meetingStmt.setTime(4, Time.valueOf(waktuMulai + ":00"));
                meetingStmt.setTime(5, Time.valueOf(waktuSelesai + ":00"));
                meetingStmt.setString(6, lokasi);
                meetingStmt.setString(7, organizerId);
                
                int result = meetingStmt.executeUpdate();
                
                if (result > 0) {
                    ResultSet rs = meetingStmt.getGeneratedKeys();
                    if (rs.next()) {
                        int meetingId = rs.getInt(1);
                        
                        // Insert participants
                        if (participantIds != null) {
                            for (String participantId : participantIds) {
                                participantStmt.setInt(1, meetingId);
                                participantStmt.setString(2, participantId);
                                participantStmt.executeUpdate();
                            }
                        }
                    }
                }
                
                conn.commit();
                return result > 0;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.severe("Error saving meeting: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateMeetingStatus(int meetingId, String status) {
        String query = "UPDATE meetings SET status = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, meetingId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error updating meeting status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests ORDER BY request_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all leave requests: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get leave requests", e);
        }
        
        return leaveRequests;
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsByEmployee(String employeeId) {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests WHERE employee_id = ? ORDER BY request_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
            
            logger.info("Retrieved " + leaveRequests.size() + " leave requests for employee: " + employeeId);
        } catch (SQLException e) {
            logger.severe("Error getting leave requests by employee: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get leave requests by employee", e);
        }
        
        return leaveRequests;
    }

    @Override
    public List<LeaveRequest> getPendingLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests WHERE status = 'pending' ORDER BY request_date ASC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting pending leave requests: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get pending leave requests", e);
        }
        
        return leaveRequests;
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsForApproval(String approverId) {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        
        // Get approver details
        Employee approver = getEmployeeById(approverId);
        if (approver == null) return leaveRequests;
        
        String query;
        if (approver.getRole().equals("supervisor")) {
            // Supervisors approve employees in their division
            query = """
                SELECT lr.* FROM leave_requests lr 
                JOIN employees e ON lr.employee_id = e.id 
                WHERE lr.status = 'pending' AND e.role = 'pegawai' AND e.divisi = ? 
                ORDER BY lr.request_date ASC
            """;
        } else if (approver.getRole().equals("manajer")) {
            // Managers approve supervisors and can approve any employee
            query = """
                SELECT lr.* FROM leave_requests lr 
                JOIN employees e ON lr.employee_id = e.id 
                WHERE lr.status = 'pending' AND (e.role = 'supervisor' OR e.role = 'pegawai') 
                ORDER BY lr.request_date ASC
            """;
        } else {
            return leaveRequests; // Regular employees can't approve
        }
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            if (approver.getRole().equals("supervisor")) {
                pstmt.setString(1, approver.getDivisi());
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
            
            logger.info("Retrieved " + leaveRequests.size() + " leave requests for approval by: " + approverId);
        } catch (SQLException e) {
            logger.severe("Error getting leave requests for approval: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get leave requests for approval", e);
        }
        
        return leaveRequests;
    }

    @Override
    public boolean saveLeaveRequest(String employeeId, String leaveType, Date startDate, Date endDate, String reason) {
        // Calculate total days
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int totalDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        
        String query = """
            INSERT INTO leave_requests (employee_id, leave_type, start_date, end_date, total_days, reason) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, leaveType);
            pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(4, new java.sql.Date(endDate.getTime()));
            pstmt.setInt(5, totalDays);
            pstmt.setString(6, reason);
            
            int result = pstmt.executeUpdate();
            
            logger.info("Leave request saved for employee: " + employeeId + ", result: " + result);
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving leave request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean approveLeaveRequest(int leaveRequestId, String approverId, String notes) {
        String query = """
            UPDATE leave_requests SET status = 'approved', approver_id = ?, approver_notes = ?, approval_date = CURRENT_TIMESTAMP 
            WHERE id = ?
        """;
        
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, approverId);
                pstmt.setString(2, notes);
                pstmt.setInt(3, leaveRequestId);
                
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    // Get leave request details to deduct leave days
                    LeaveRequest request = getLeaveRequestById(leaveRequestId);
                    if (request != null) {
                        // Deduct leave days from employee
                        String updateEmployeeQuery = "UPDATE employees SET sisa_cuti = sisa_cuti - ? WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateEmployeeQuery)) {
                            updateStmt.setInt(1, request.getTotalDays());
                            updateStmt.setString(2, request.getEmployeeId());
                            updateStmt.executeUpdate();
                        }
                    }
                }
                
                conn.commit();
                return result > 0;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.severe("Error approving leave request: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rejectLeaveRequest(int leaveRequestId, String approverId, String notes) {
        String query = """
            UPDATE leave_requests SET status = 'rejected', approver_id = ?, approver_notes = ?, approval_date = CURRENT_TIMESTAMP 
            WHERE id = ?
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, approverId);
            pstmt.setString(2, notes);
            pstmt.setInt(3, leaveRequestId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error rejecting leave request: " + e.getMessage());
            return false;
        }
    }

    private LeaveRequest getLeaveRequestById(int id) {
        String query = "SELECT * FROM leave_requests WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLeaveRequest(rs);
            }
        } catch (SQLException e) {
            logger.severe("Error getting leave request by ID: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public List<SalaryHistory> getAllSalaryHistory() {
        List<SalaryHistory> salaryHistories = new ArrayList<>();
        String query = "SELECT * FROM salary_history ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                salaryHistories.add(mapResultSetToSalaryHistory(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all salary history: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get salary history", e);
        }
        
        return salaryHistories;
    }

    @Override
    public List<SalaryHistory> getSalaryHistoryByEmployee(String employeeId) {
        List<SalaryHistory> salaryHistories = new ArrayList<>();
        String query = "SELECT * FROM salary_history WHERE employee_id = ? ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                salaryHistories.add(mapResultSetToSalaryHistory(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting salary history by employee: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get salary history by employee", e);
        }
        
        return salaryHistories;
    }

    @Override
    public String getSupervisorByDivision(String divisi) {
        String query = "SELECT id FROM employees WHERE role = 'supervisor' AND divisi = ? LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            logger.severe("Error getting supervisor by division: " + e.getMessage());
        }
        
        return "SUP001"; // Default fallback
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = dbConnection.getConnection()) {
            // Get employee counts
            stats.put("totalEmployees", getEmployeeCount(conn, null));
            stats.put("totalManagers", getEmployeeCount(conn, "manajer"));
            stats.put("totalSupervisors", getEmployeeCount(conn, "supervisor"));
            stats.put("totalEmployeesRegular", getEmployeeCount(conn, "pegawai"));
            
            // Get layoff risk count
            stats.put("layoffRiskEmployees", getLayoffRiskCount(conn));
            
            // Get pending counts
            stats.put("pendingReports", getPendingCount(conn, "reports"));
            stats.put("pendingLeaveRequests", getPendingCount(conn, "leave_requests"));
            stats.put("upcomingMeetings", getUpcomingMeetingsCount(conn));
            
            // Get average KPI by division
            stats.put("avgKpiByDivision", getAvgKpiByDivision(conn));
            
        } catch (SQLException e) {
            logger.severe("Error getting dashboard stats: " + e.getMessage());
        }
        
        return stats;
    }

    private int getEmployeeCount(Connection conn, String role) throws SQLException {
        String query = role != null ? "SELECT COUNT(*) FROM employees WHERE role = ?" : "SELECT COUNT(*) FROM employees";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (role != null) {
                pstmt.setString(1, role);
            }
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getLayoffRiskCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM employees WHERE layoff_risk = true";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getPendingCount(Connection conn, String table) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + table + " WHERE status = 'pending'";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getUpcomingMeetingsCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM meetings WHERE tanggal >= CURDATE()";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Map<String, Double> getAvgKpiByDivision(Connection conn) throws SQLException {
        Map<String, Double> avgKpi = new HashMap<>();
        String query = "SELECT divisi, AVG(score) as avg_score FROM kpi GROUP BY divisi";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                avgKpi.put(rs.getString("divisi"), rs.getDouble("avg_score"));
            }
        }
        
        return avgKpi;
    }

    // Additional methods for monthly evaluations
    public boolean hasMonthlyEvaluation(String employeeId, int month, int year) {
        String query = "SELECT COUNT(*) FROM monthly_evaluations WHERE employee_id = ? AND month = ? AND year = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.severe("Error checking monthly evaluation: " + e.getMessage());
            return false;
        }
    }

    public boolean saveMonthlyEmployeeEvaluation(String employeeId, String supervisorId,
                                                int month, int year,
                                                double punctualityScore, double attendanceScore,
                                                double productivityScore, double overallRating,
                                                String comments) {
        String query = """
            INSERT INTO monthly_evaluations 
            (employee_id, supervisor_id, month, year, punctuality_score, attendance_score, productivity_score, overall_rating, comments) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
            punctuality_score = VALUES(punctuality_score), 
            attendance_score = VALUES(attendance_score), 
            productivity_score = VALUES(productivity_score), 
            overall_rating = VALUES(overall_rating), 
            comments = VALUES(comments)
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, supervisorId);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            pstmt.setDouble(5, punctualityScore);
            pstmt.setDouble(6, attendanceScore);
            pstmt.setDouble(7, productivityScore);
            pstmt.setDouble(8, overallRating);
            pstmt.setString(9, comments);
            
            int result = pstmt.executeUpdate();
            
            // Update employee supervisor rating with latest monthly evaluation
            if (result > 0) {
                updateEmployee(employeeId, overallRating);
            }
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving monthly evaluation: " + e.getMessage());
            return false;
        }
    }

    private void updateEmployee(String employeeId, double supervisorRating) {
        String query = "UPDATE employees SET supervisor_rating = ?, layoff_risk = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            Employee emp = getEmployeeById(employeeId);
            boolean layoffRisk = emp != null && (emp.getKpiScore() < 60 || supervisorRating < 60);
            
            pstmt.setDouble(1, supervisorRating);
            pstmt.setBoolean(2, layoffRisk);
            pstmt.setString(3, employeeId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating employee: " + e.getMessage());
        }
    }

    public boolean saveEmployeeEvaluation(String employeeId, String supervisorId,
                                         double punctualityScore, double attendanceScore,
                                         double overallRating, String comments) {
        String query = """
            INSERT INTO employee_evaluations 
            (employee_id, supervisor_id, punctuality_score, attendance_score, overall_rating, comments) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, supervisorId);
            pstmt.setDouble(3, punctualityScore);
            pstmt.setDouble(4, attendanceScore);
            pstmt.setDouble(5, overallRating);
            pstmt.setString(6, comments);
            
            int result = pstmt.executeUpdate();
            
            // Update employee supervisor rating
            if (result > 0) {
                updateEmployee(employeeId, overallRating);
            }
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving employee evaluation: " + e.getMessage());
            return false;
        }
    }

    public List<EmployeeEvaluation> getEvaluationsByEmployee(String employeeId) {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations WHERE employee_id = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToEmployeeEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by employee: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get evaluations by employee", e);
        }
        
        return evaluations;
    }

    public List<EmployeeEvaluation> getEvaluationsBySupervisor(String supervisorId) {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations WHERE supervisor_id = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, supervisorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToEmployeeEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by supervisor: " + e.getMessage());
            throw new DatabaseException.QueryException("Failed to get evaluations by supervisor", e);
        }
        
        return evaluations;
    }

    @Override
    public void close() {
        // DatabaseConnection is a singleton, so we don't close it here
        logger.info("MySQLDataStore closed");
    }

    // Helper methods to map ResultSet to model objects
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getString("id"));
        emp.setNama(rs.getString("nama"));
        emp.setPassword(rs.getString("password"));
        emp.setRole(rs.getString("role"));
        emp.setDivisi(rs.getString("divisi"));
        emp.setJabatan(rs.getString("jabatan"));
        emp.setTglMasuk(rs.getDate("tgl_masuk"));
        emp.setSisaCuti(rs.getInt("sisa_cuti"));
        emp.setGajiPokok(rs.getDouble("gaji_pokok"));
        emp.setKpiScore(rs.getDouble("kpi_score"));
        emp.setSupervisorRating(rs.getDouble("supervisor_rating"));
        emp.setLayoffRisk(rs.getBoolean("layoff_risk"));
        return emp;
    }

    private KPI mapResultSetToKPI(ResultSet rs) throws SQLException {
        KPI kpi = new KPI();
        kpi.setId(rs.getInt("id"));
        kpi.setDivisi(rs.getString("divisi"));
        kpi.setBulan(rs.getInt("bulan"));
        kpi.setTahun(rs.getInt("tahun"));
        kpi.setScore(rs.getDouble("score"));
        kpi.setManagerId(rs.getString("manager_id"));
        kpi.setCreatedDate(rs.getTimestamp("created_date"));
        kpi.setNotes(rs.getString("notes"));
        return kpi;
    }

    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setSupervisorId(rs.getString("supervisor_id"));
        report.setDivisi(rs.getString("divisi"));
        report.setBulan(rs.getInt("bulan"));
        report.setTahun(rs.getInt("tahun"));
        report.setFilePath(rs.getString("file_path"));
        report.setUploadDate(rs.getTimestamp("upload_date"));
        report.setStatus(rs.getString("status"));
        report.setManagerNotes(rs.getString("manager_notes"));
        report.setReviewedBy(rs.getString("reviewed_by"));
        report.setReviewedDate(rs.getTimestamp("reviewed_date"));
        return report;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id"));
        attendance.setEmployeeId(rs.getString("employee_id"));
        attendance.setTanggal(rs.getDate("tanggal"));
        
        Time jamMasuk = rs.getTime("jam_masuk");
        attendance.setJamMasuk(jamMasuk != null ? jamMasuk.toString().substring(0, 5) : null);
        
        Time jamKeluar = rs.getTime("jam_keluar");
        attendance.setJamKeluar(jamKeluar != null ? jamKeluar.toString().substring(0, 5) : null);
        
        attendance.setStatus(rs.getString("status"));
        attendance.setKeterangan(rs.getString("keterangan"));
        attendance.setLate(rs.getBoolean("is_late"));
        return attendance;
    }

    private Meeting mapResultSetToMeeting(ResultSet rs) throws SQLException {
        Meeting meeting = new Meeting();
        meeting.setId(rs.getInt("id"));
        meeting.setTitle(rs.getString("title"));
        meeting.setDescription(rs.getString("description"));
        meeting.setTanggal(rs.getDate("tanggal"));
        
        Time waktuMulai = rs.getTime("waktu_mulai");
        meeting.setWaktuMulai(waktuMulai != null ? waktuMulai.toString().substring(0, 5) : null);
        
        Time waktuSelesai = rs.getTime("waktu_selesai");
        meeting.setWaktuSelesai(waktuSelesai != null ? waktuSelesai.toString().substring(0, 5) : null);
        
        meeting.setLokasi(rs.getString("lokasi"));
        meeting.setOrganizerId(rs.getString("organizer_id"));
        meeting.setStatus(rs.getString("status"));
        meeting.setCreatedDate(rs.getTimestamp("created_date"));
        
        // Parse participants
        String participants = rs.getString("participants");
        if (participants != null && !participants.isEmpty()) {
            meeting.setParticipantIds(Arrays.asList(participants.split(",")));
        } else {
            meeting.setParticipantIds(new ArrayList<>());
        }
        
        return meeting;
    }

    private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(rs.getInt("id"));
        leaveRequest.setEmployeeId(rs.getString("employee_id"));
        leaveRequest.setLeaveType(rs.getString("leave_type"));
        leaveRequest.setStartDate(rs.getDate("start_date"));
        leaveRequest.setEndDate(rs.getDate("end_date"));
        leaveRequest.setTotalDays(rs.getInt("total_days"));
        leaveRequest.setReason(rs.getString("reason"));
        leaveRequest.setStatus(rs.getString("status"));
        leaveRequest.setApproverId(rs.getString("approver_id"));
        leaveRequest.setApproverNotes(rs.getString("approver_notes"));
        leaveRequest.setRequestDate(rs.getTimestamp("request_date"));
        leaveRequest.setApprovalDate(rs.getTimestamp("approval_date"));
        return leaveRequest;
    }

    private SalaryHistory mapResultSetToSalaryHistory(ResultSet rs) throws SQLException {
        SalaryHistory salaryHistory = new SalaryHistory();
        salaryHistory.setId(rs.getInt("id"));
        salaryHistory.setEmployeeId(rs.getString("employee_id"));
        salaryHistory.setBulan(rs.getInt("bulan"));
        salaryHistory.setTahun(rs.getInt("tahun"));
        salaryHistory.setBaseSalary(rs.getDouble("base_salary"));
        salaryHistory.setKpiBonus(rs.getDouble("kpi_bonus"));
        salaryHistory.setSupervisorBonus(rs.getDouble("supervisor_bonus"));
        salaryHistory.setPenalty(rs.getDouble("penalty"));
        salaryHistory.setTotalSalary(rs.getDouble("total_salary"));
        salaryHistory.setKpiScore(rs.getDouble("kpi_score"));
        salaryHistory.setSupervisorRating(rs.getDouble("supervisor_rating"));
        salaryHistory.setPaymentDate(rs.getTimestamp("payment_date"));
        salaryHistory.setNotes(rs.getString("notes"));
        return salaryHistory;
    }

    private EmployeeEvaluation mapResultSetToEmployeeEvaluation(ResultSet rs) throws SQLException {
        EmployeeEvaluation evaluation = new EmployeeEvaluation();
        evaluation.setId(rs.getInt("id"));
        evaluation.setEmployeeId(rs.getString("employee_id"));
        evaluation.setSupervisorId(rs.getString("supervisor_id"));
        evaluation.setPunctualityScore(rs.getDouble("punctuality_score"));
        evaluation.setAttendanceScore(rs.getDouble("attendance_score"));
        evaluation.setOverallRating(rs.getDouble("overall_rating"));
        evaluation.setEvaluationDate(rs.getTimestamp("evaluation_date"));
        evaluation.setComments(rs.getString("comments"));
        return evaluation;
    }
}
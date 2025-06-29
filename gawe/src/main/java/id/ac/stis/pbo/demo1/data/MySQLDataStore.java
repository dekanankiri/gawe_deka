package id.ac.stis.pbo.demo1.data;

import id.ac.stis.pbo.demo1.database.MySQLDatabaseManager;
import id.ac.stis.pbo.demo1.models.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * MySQL-based data store for the GAWE application
 * Provides persistent storage with proper role interactions
 */
public class MySQLDataStore {
    private static final Logger logger = Logger.getLogger(MySQLDataStore.class.getName());
    private MySQLDatabaseManager dbManager;

    /**
     * Initialize the MySQL data store
     */
    public MySQLDataStore() {
        try {
            dbManager = new MySQLDatabaseManager();
            dbManager.initializeDatabase();
            logger.info("MySQL DataStore initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize MySQL DataStore: " + e.getMessage());
            throw new RuntimeException("MySQL DataStore initialization failed", e);
        }
    }

    /**
     * Close database connections
     */
    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    // Authentication
    public Employee authenticateUser(String employeeId, String password) {
        return dbManager.authenticateUser(employeeId, password);
    }

    // Employee operations
    public List<Employee> getAllEmployees() {
        return dbManager.getAllEmployees();
    }

    public List<Employee> getEmployeesByRole(String role) {
        return dbManager.getEmployeesByRole(role);
    }

    public List<Employee> getEmployeesByDivision(String divisi) {
        return dbManager.getEmployeesByDivision(divisi);
    }

    public Employee getEmployeeById(String id) {
        return dbManager.getEmployeeById(id);
    }

    public void updateEmployee(Employee employee) {
        String query = "UPDATE employees SET nama = ?, role = ?, divisi = ?, jabatan = ?, sisa_cuti = ?, gaji_pokok = ?, kpi_score = ?, supervisor_rating = ?, layoff_risk = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employee.getNama());
            pstmt.setString(2, employee.getRole());
            pstmt.setString(3, employee.getDivisi());
            pstmt.setString(4, employee.getJabatan());
            pstmt.setInt(5, employee.getSisaCuti());
            pstmt.setDouble(6, employee.getGajiPokok());
            pstmt.setDouble(7, employee.getKpiScore());
            pstmt.setDouble(8, employee.getSupervisorRating());
            pstmt.setBoolean(9, employee.isLayoffRisk());
            pstmt.setString(10, employee.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating employee: " + e.getMessage());
        }
    }

    // KPI operations
    public List<KPI> getAllKPI() {
        List<KPI> kpiList = new ArrayList<>();
        String query = "SELECT * FROM kpi ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                kpiList.add(mapResultSetToKPI(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all KPI: " + e.getMessage());
        }
        
        return kpiList;
    }

    public List<KPI> getKPIByDivision(String divisi) {
        List<KPI> kpiList = new ArrayList<>();
        String query = "SELECT * FROM kpi WHERE divisi = ? ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                kpiList.add(mapResultSetToKPI(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting KPI by division: " + e.getMessage());
        }
        
        return kpiList;
    }

    public boolean saveKPI(String divisi, int bulan, int tahun, double score, String managerId) {
        String query = "INSERT INTO kpi (divisi, bulan, tahun, score, manager_id) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE score = ?, manager_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            pstmt.setInt(2, bulan);
            pstmt.setInt(3, tahun);
            pstmt.setDouble(4, score);
            pstmt.setString(5, managerId);
            pstmt.setDouble(6, score);
            pstmt.setString(7, managerId);
            
            int result = pstmt.executeUpdate();
            
            // Update employee layoff risk based on KPI
            updateEmployeeKPIScores(divisi, score);
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving KPI: " + e.getMessage());
            return false;
        }
    }

    private void updateEmployeeKPIScores(String divisi, double kpiScore) {
        String query = "UPDATE employees SET kpi_score = ?, layoff_risk = ? WHERE divisi = ? AND role = 'pegawai'";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            boolean layoffRisk = kpiScore < 60.0;
            pstmt.setDouble(1, kpiScore);
            pstmt.setBoolean(2, layoffRisk);
            pstmt.setString(3, divisi);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating employee KPI scores: " + e.getMessage());
        }
    }

    // Report operations
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports ORDER BY upload_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all reports: " + e.getMessage());
        }
        
        return reports;
    }

    public List<Report> getPendingReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE status = 'pending' ORDER BY upload_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting pending reports: " + e.getMessage());
        }
        
        return reports;
    }

    public List<Report> getReportsByDivision(String divisi) {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE divisi = ? ORDER BY upload_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting reports by division: " + e.getMessage());
        }
        
        return reports;
    }

    public boolean saveReport(String supervisorId, String divisi, int bulan, int tahun, String filePath) {
        String query = "INSERT INTO reports (supervisor_id, divisi, bulan, tahun, file_path) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
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

    public boolean updateReportStatus(int reportId, String status, String managerNotes, String reviewedBy) {
        String query = "UPDATE reports SET status = ?, manager_notes = ?, reviewed_by = ?, reviewed_date = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
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

    // Employee Evaluation operations
    public List<EmployeeEvaluation> getAllEvaluations() {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                evaluations.add(mapResultSetToEmployeeEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all evaluations: " + e.getMessage());
        }
        
        return evaluations;
    }

    public List<EmployeeEvaluation> getEvaluationsByEmployee(String employeeId) {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations WHERE employee_id = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToEmployeeEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by employee: " + e.getMessage());
        }
        
        return evaluations;
    }

    public List<EmployeeEvaluation> getEvaluationsBySupervisor(String supervisorId) {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations WHERE supervisor_id = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, supervisorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToEmployeeEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by supervisor: " + e.getMessage());
        }
        
        return evaluations;
    }

    public boolean saveEmployeeEvaluation(String employeeId, String supervisorId,
                                                 double punctualityScore, double attendanceScore,
                                                 double overallRating, String comments) {
        String query = "INSERT INTO employee_evaluations (employee_id, supervisor_id, punctuality_score, attendance_score, overall_rating, comments) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, supervisorId);
            pstmt.setDouble(3, punctualityScore);
            pstmt.setDouble(4, attendanceScore);
            pstmt.setDouble(5, overallRating);
            pstmt.setString(6, comments);
            
            int result = pstmt.executeUpdate();
            
            // Update employee supervisor rating
            updateEmployeeSupervisorRating(employeeId, overallRating);
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving employee evaluation: " + e.getMessage());
            return false;
        }
    }

    private void updateEmployeeSupervisorRating(String employeeId, double rating) {
        String query = "UPDATE employees SET supervisor_rating = ?, layoff_risk = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Get current KPI score to determine layoff risk
            Employee employee = getEmployeeById(employeeId);
            boolean layoffRisk = (employee != null && employee.getKpiScore() < 60) || rating < 60;
            
            pstmt.setDouble(1, rating);
            pstmt.setBoolean(2, layoffRisk);
            pstmt.setString(3, employeeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating supervisor rating: " + e.getMessage());
        }
    }

    // Monthly Evaluation operations
    public boolean hasMonthlyEvaluation(String employeeId, int month, int year) {
        String query = "SELECT COUNT(*) FROM monthly_evaluations WHERE employee_id = ? AND month = ? AND year = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("Error checking monthly evaluation: " + e.getMessage());
        }
        
        return false;
    }

    public boolean saveMonthlyEmployeeEvaluation(String employeeId, String supervisorId,
                                                        int month, int year,
                                                        double punctualityScore, double attendanceScore,
                                                        double productivityScore, double overallRating,
                                                        String comments) {
        String query = "INSERT INTO monthly_evaluations (employee_id, supervisor_id, month, year, punctuality_score, attendance_score, productivity_score, overall_rating, comments) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
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
            updateEmployeeSupervisorRating(employeeId, overallRating);
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving monthly evaluation: " + e.getMessage());
            return false;
        }
    }

    public List<MonthlyEvaluation> getAllMonthlyEvaluations() {
        List<MonthlyEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM monthly_evaluations ORDER BY year DESC, month DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                evaluations.add(mapResultSetToMonthlyEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all monthly evaluations: " + e.getMessage());
        }
        
        return evaluations;
    }

    public List<MonthlyEvaluation> getMonthlyEvaluationsByEmployee(String employeeId) {
        List<MonthlyEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM monthly_evaluations WHERE employee_id = ? ORDER BY year DESC, month DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToMonthlyEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting monthly evaluations by employee: " + e.getMessage());
        }
        
        return evaluations;
    }

    public List<MonthlyEvaluation> getMonthlyEvaluationsBySupervisor(String supervisorId) {
        List<MonthlyEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM monthly_evaluations WHERE supervisor_id = ? ORDER BY year DESC, month DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, supervisorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToMonthlyEvaluation(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting monthly evaluations by supervisor: " + e.getMessage());
        }
        
        return evaluations;
    }

    // Attendance operations
    public List<Attendance> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance ORDER BY tanggal DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all attendance: " + e.getMessage());
        }
        
        return attendanceList;
    }

    public List<Attendance> getAttendanceByEmployee(String employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY tanggal DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting attendance by employee: " + e.getMessage());
        }
        
        return attendanceList;
    }

    public List<Attendance> getTodayAttendance(String employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? AND tanggal = CURDATE()";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting today's attendance: " + e.getMessage());
        }
        
        return attendanceList;
    }

    public boolean saveAttendance(String employeeId, Date tanggal, String jamMasuk, String jamKeluar, String status) {
        String query = "INSERT INTO attendance (employee_id, tanggal, jam_masuk, jam_keluar, status, is_late) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE jam_masuk = ?, jam_keluar = ?, status = ?, is_late = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            java.sql.Date sqlDate = new java.sql.Date(tanggal.getTime());
            Time jamMasukTime = jamMasuk != null ? Time.valueOf(jamMasuk + ":00") : null;
            Time jamKeluarTime = jamKeluar != null ? Time.valueOf(jamKeluar + ":00") : null;
            boolean isLate = isLateArrival(jamMasuk);
            
            pstmt.setString(1, employeeId);
            pstmt.setDate(2, sqlDate);
            pstmt.setTime(3, jamMasukTime);
            pstmt.setTime(4, jamKeluarTime);
            pstmt.setString(5, status);
            pstmt.setBoolean(6, isLate);
            pstmt.setTime(7, jamMasukTime);
            pstmt.setTime(8, jamKeluarTime);
            pstmt.setString(9, status);
            pstmt.setBoolean(10, isLate);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving attendance: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAttendanceClockOut(String employeeId, String jamKeluar) {
        String query = "UPDATE attendance SET jam_keluar = ? WHERE employee_id = ? AND tanggal = CURDATE()";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            Time jamKeluarTime = Time.valueOf(jamKeluar + ":00");
            pstmt.setTime(1, jamKeluarTime);
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

    // Meeting operations
    public List<Meeting> getAllMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        String query = "SELECT * FROM meetings ORDER BY tanggal ASC, waktu_mulai ASC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                meetings.add(mapResultSetToMeeting(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all meetings: " + e.getMessage());
        }
        
        return meetings;
    }

    public List<Meeting> getMeetingsByEmployee(String employeeId) {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT DISTINCT m.* FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            WHERE m.organizer_id = ? OR mp.participant_id = ? 
            ORDER BY m.tanggal ASC, m.waktu_mulai ASC
        """;
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                meetings.add(mapResultSetToMeeting(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting meetings by employee: " + e.getMessage());
        }
        
        return meetings;
    }

    public List<Meeting> getUpcomingMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        String query = "SELECT * FROM meetings WHERE tanggal >= CURDATE() ORDER BY tanggal ASC, waktu_mulai ASC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                meetings.add(mapResultSetToMeeting(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting upcoming meetings: " + e.getMessage());
        }
        
        return meetings;
    }

    public boolean saveMeeting(String title, String description, Date tanggal, String waktuMulai,
                                      String waktuSelesai, String lokasi, String organizerId, List<String> participantIds) {
        String insertMeetingQuery = "INSERT INTO meetings (title, description, tanggal, waktu_mulai, waktu_selesai, lokasi, organizer_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertParticipantQuery = "INSERT INTO meeting_participants (meeting_id, participant_id) VALUES (?, ?)";
        
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement meetingStmt = conn.prepareStatement(insertMeetingQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement participantStmt = conn.prepareStatement(insertParticipantQuery)) {
                
                java.sql.Date sqlDate = new java.sql.Date(tanggal.getTime());
                Time waktuMulaiTime = Time.valueOf(waktuMulai + ":00");
                Time waktuSelesaiTime = Time.valueOf(waktuSelesai + ":00");
                
                meetingStmt.setString(1, title);
                meetingStmt.setString(2, description);
                meetingStmt.setDate(3, sqlDate);
                meetingStmt.setTime(4, waktuMulaiTime);
                meetingStmt.setTime(5, waktuSelesaiTime);
                meetingStmt.setString(6, lokasi);
                meetingStmt.setString(7, organizerId);
                
                int result = meetingStmt.executeUpdate();
                
                if (result > 0) {
                    ResultSet rs = meetingStmt.getGeneratedKeys();
                    if (rs.next()) {
                        int meetingId = rs.getInt(1);
                        
                        // Add participants
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

    public boolean updateMeetingStatus(int meetingId, String status) {
        String query = "UPDATE meetings SET status = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, meetingId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error updating meeting status: " + e.getMessage());
            return false;
        }
    }

    // Leave Request operations
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests ORDER BY request_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all leave requests: " + e.getMessage());
        }
        
        return leaveRequests;
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(String employeeId) {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests WHERE employee_id = ? ORDER BY request_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting leave requests by employee: " + e.getMessage());
        }
        
        return leaveRequests;
    }

    public List<LeaveRequest> getPendingLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests WHERE status = 'pending' ORDER BY request_date ASC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting pending leave requests: " + e.getMessage());
        }
        
        return leaveRequests;
    }

    public List<LeaveRequest> getLeaveRequestsForApproval(String approverId) {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        Employee approver = getEmployeeById(approverId);
        if (approver == null) return leaveRequests;

        String query = """
            SELECT lr.* FROM leave_requests lr 
            JOIN employees e ON lr.employee_id = e.id 
            WHERE lr.status = 'pending' AND (
                (? = 'supervisor' AND e.role = 'pegawai' AND e.divisi = ?) OR
                (? = 'manajer' AND (e.role = 'supervisor' OR e.role = 'pegawai'))
            )
            ORDER BY lr.request_date ASC
        """;
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, approver.getRole());
            pstmt.setString(2, approver.getDivisi());
            pstmt.setString(3, approver.getRole());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting leave requests for approval: " + e.getMessage());
        }
        
        return leaveRequests;
    }

    public boolean saveLeaveRequest(String employeeId, String leaveType, Date startDate, Date endDate, String reason) {
        // Calculate total days
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int totalDays = (int) ChronoUnit.DAYS.between(start, end) + 1;

        String query = "INSERT INTO leave_requests (employee_id, leave_type, start_date, end_date, total_days, reason) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            java.sql.Date sqlStartDate = new java.sql.Date(startDate.getTime());
            java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, leaveType);
            pstmt.setDate(3, sqlStartDate);
            pstmt.setDate(4, sqlEndDate);
            pstmt.setInt(5, totalDays);
            pstmt.setString(6, reason);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving leave request: " + e.getMessage());
            return false;
        }
    }

    public boolean approveLeaveRequest(int leaveRequestId, String approverId, String notes) {
        String query = "UPDATE leave_requests SET status = 'approved', approver_id = ?, approver_notes = ?, approval_date = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, approverId);
            pstmt.setString(2, notes);
            pstmt.setInt(3, leaveRequestId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // Deduct leave days from employee
                LeaveRequest leaveRequest = getLeaveRequestById(leaveRequestId);
                if (leaveRequest != null) {
                    deductLeaveDays(leaveRequest.getEmployeeId(), leaveRequest.getTotalDays());
                }
            }
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error approving leave request: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectLeaveRequest(int leaveRequestId, String approverId, String notes) {
        String query = "UPDATE leave_requests SET status = 'rejected', approver_id = ?, approver_notes = ?, approval_date = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
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
        
        try (Connection conn = dbManager.getConnection();
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

    private void deductLeaveDays(String employeeId, int days) {
        String query = "UPDATE employees SET sisa_cuti = sisa_cuti - ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, days);
            pstmt.setString(2, employeeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error deducting leave days: " + e.getMessage());
        }
    }

    // Salary History operations
    public static List<SalaryHistory> getAllSalaryHistory() {
        List<SalaryHistory> salaryHistory = new ArrayList<>();
        String query = "SELECT * FROM salary_history ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                salaryHistory.add(mapResultSetToSalaryHistory(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all salary history: " + e.getMessage());
        }
        
        return salaryHistory;
    }

    public static List<SalaryHistory> getSalaryHistoryByEmployee(String employeeId) {
        List<SalaryHistory> salaryHistory = new ArrayList<>();
        String query = "SELECT * FROM salary_history WHERE employee_id = ? ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                salaryHistory.add(mapResultSetToSalaryHistory(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting salary history by employee: " + e.getMessage());
        }
        
        return salaryHistory;
    }

    public static boolean saveSalaryHistory(String employeeId, int bulan, int tahun, double baseSalary,
                                            double kpiBonus, double supervisorBonus, double penalty,
                                            double totalSalary, double kpiScore, double supervisorRating) {
        String query = "INSERT INTO salary_history (employee_id, bulan, tahun, base_salary, kpi_bonus, supervisor_bonus, penalty, total_salary, kpi_score, supervisor_rating) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE base_salary = ?, kpi_bonus = ?, supervisor_bonus = ?, penalty = ?, total_salary = ?, kpi_score = ?, supervisor_rating = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setInt(2, bulan);
            pstmt.setInt(3, tahun);
            pstmt.setDouble(4, baseSalary);
            pstmt.setDouble(5, kpiBonus);
            pstmt.setDouble(6, supervisorBonus);
            pstmt.setDouble(7, penalty);
            pstmt.setDouble(8, totalSalary);
            pstmt.setDouble(9, kpiScore);
            pstmt.setDouble(10, supervisorRating);
            pstmt.setDouble(11, baseSalary);
            pstmt.setDouble(12, kpiBonus);
            pstmt.setDouble(13, supervisorBonus);
            pstmt.setDouble(14, penalty);
            pstmt.setDouble(15, totalSalary);
            pstmt.setDouble(16, kpiScore);
            pstmt.setDouble(17, supervisorRating);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving salary history: " + e.getMessage());
            return false;
        }
    }

    // Utility methods
    public String getSupervisorByDivision(String divisi) {
        return dbManager.getSupervisorByDivision(divisi);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Employee> allEmployees = getAllEmployees();
        stats.put("totalEmployees", allEmployees.size());
        stats.put("totalManagers", getEmployeesByRole("manajer").size());
        stats.put("totalSupervisors", getEmployeesByRole("supervisor").size());
        stats.put("totalEmployeesRegular", getEmployeesByRole("pegawai").size());

        long layoffRiskCount = allEmployees.stream()
                .filter(Employee::isLayoffRisk)
                .count();
        stats.put("layoffRiskEmployees", layoffRiskCount);

        stats.put("pendingReports", getPendingReports().size());
        stats.put("pendingLeaveRequests", getPendingLeaveRequests().size());
        stats.put("upcomingMeetings", getUpcomingMeetings().size());

        // Average KPI by division
        Map<String, Double> avgKpiByDivision = new HashMap<>();
        String[] divisions = {"HR", "Marketing", "Sales", "IT", "Finance"};
        for (String division : divisions) {
            List<KPI> divisionKpis = getKPIByDivision(division);
            if (!divisionKpis.isEmpty()) {
                double avgKpi = divisionKpis.stream()
                        .mapToDouble(KPI::getScore)
                        .average()
                        .orElse(0.0);
                avgKpiByDivision.put(division, avgKpi);
            }
        }
        stats.put("avgKpiByDivision", avgKpiByDivision);

        return stats;
    }

    // Helper methods to map ResultSet to model objects
    private static KPI mapResultSetToKPI(ResultSet rs) throws SQLException {
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

    private static Report mapResultSetToReport(ResultSet rs) throws SQLException {
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

    private static EmployeeEvaluation mapResultSetToEmployeeEvaluation(ResultSet rs) throws SQLException {
        EmployeeEvaluation eval = new EmployeeEvaluation();
        eval.setId(rs.getInt("id"));
        eval.setEmployeeId(rs.getString("employee_id"));
        eval.setSupervisorId(rs.getString("supervisor_id"));
        eval.setPunctualityScore(rs.getDouble("punctuality_score"));
        eval.setAttendanceScore(rs.getDouble("attendance_score"));
        eval.setOverallRating(rs.getDouble("overall_rating"));
        eval.setEvaluationDate(rs.getTimestamp("evaluation_date"));
        eval.setComments(rs.getString("comments"));
        return eval;
    }

    private static MonthlyEvaluation mapResultSetToMonthlyEvaluation(ResultSet rs) throws SQLException {
        MonthlyEvaluation eval = new MonthlyEvaluation();
        eval.setId(rs.getInt("id"));
        eval.setEmployeeId(rs.getString("employee_id"));
        eval.setSupervisorId(rs.getString("supervisor_id"));
        eval.setMonth(rs.getInt("month"));
        eval.setYear(rs.getInt("year"));
        eval.setPunctualityScore(rs.getDouble("punctuality_score"));
        eval.setAttendanceScore(rs.getDouble("attendance_score"));
        eval.setProductivityScore(rs.getDouble("productivity_score"));
        eval.setOverallRating(rs.getDouble("overall_rating"));
        eval.setComments(rs.getString("comments"));
        eval.setEvaluationDate(rs.getTimestamp("evaluation_date"));
        return eval;
    }

    private static Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id"));
        attendance.setEmployeeId(rs.getString("employee_id"));
        attendance.setTanggal(rs.getDate("tanggal"));
        
        Time jamMasuk = rs.getTime("jam_masuk");
        if (jamMasuk != null) {
            attendance.setJamMasuk(jamMasuk.toString().substring(0, 5)); // HH:MM format
        }
        
        Time jamKeluar = rs.getTime("jam_keluar");
        if (jamKeluar != null) {
            attendance.setJamKeluar(jamKeluar.toString().substring(0, 5)); // HH:MM format
        }
        
        attendance.setStatus(rs.getString("status"));
        attendance.setKeterangan(rs.getString("keterangan"));
        attendance.setLate(rs.getBoolean("is_late"));
        return attendance;
    }

    private static Meeting mapResultSetToMeeting(ResultSet rs) throws SQLException {
        Meeting meeting = new Meeting();
        meeting.setId(rs.getInt("id"));
        meeting.setTitle(rs.getString("title"));
        meeting.setDescription(rs.getString("description"));
        meeting.setTanggal(rs.getDate("tanggal"));
        
        Time waktuMulai = rs.getTime("waktu_mulai");
        if (waktuMulai != null) {
            meeting.setWaktuMulai(waktuMulai.toString().substring(0, 5)); // HH:MM format
        }
        
        Time waktuSelesai = rs.getTime("waktu_selesai");
        if (waktuSelesai != null) {
            meeting.setWaktuSelesai(waktuSelesai.toString().substring(0, 5)); // HH:MM format
        }
        
        meeting.setLokasi(rs.getString("lokasi"));
        meeting.setOrganizerId(rs.getString("organizer_id"));
        meeting.setStatus(rs.getString("status"));
        meeting.setCreatedDate(rs.getTimestamp("created_date"));
        
        // Load participants
        meeting.setParticipantIds(getMeetingParticipants(meeting.getId()));
        
        return meeting;
    }

    private static List<String> getMeetingParticipants(int meetingId) {
        List<String> participants = new ArrayList<>();
        String query = "SELECT participant_id FROM meeting_participants WHERE meeting_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, meetingId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                participants.add(rs.getString("participant_id"));
            }
        } catch (SQLException e) {
            logger.severe("Error getting meeting participants: " + e.getMessage());
        }
        
        return participants;
    }

    private static LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
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

    private static SalaryHistory mapResultSetToSalaryHistory(ResultSet rs) throws SQLException {
        SalaryHistory salary = new SalaryHistory();
        salary.setId(rs.getInt("id"));
        salary.setEmployeeId(rs.getString("employee_id"));
        salary.setBulan(rs.getInt("bulan"));
        salary.setTahun(rs.getInt("tahun"));
        salary.setBaseSalary(rs.getDouble("base_salary"));
        salary.setKpiBonus(rs.getDouble("kpi_bonus"));
        salary.setSupervisorBonus(rs.getDouble("supervisor_bonus"));
        salary.setPenalty(rs.getDouble("penalty"));
        salary.setTotalSalary(rs.getDouble("total_salary"));
        salary.setKpiScore(rs.getDouble("kpi_score"));
        salary.setSupervisorRating(rs.getDouble("supervisor_rating"));
        salary.setPaymentDate(rs.getTimestamp("payment_date"));
        salary.setNotes(rs.getString("notes"));
        return salary;
    }

    // MonthlyEvaluation inner class
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
}
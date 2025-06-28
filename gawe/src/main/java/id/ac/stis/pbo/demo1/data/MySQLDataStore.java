package id.ac.stis.pbo.demo1.data;

import id.ac.stis.pbo.demo1.database.MySQLDatabaseManager;
import id.ac.stis.pbo.demo1.models.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * MySQL-based data store for GAWE application
 * Replaces in-memory DataStore with database operations
 */
public class MySQLDataStore {
    private static final Logger logger = Logger.getLogger(MySQLDataStore.class.getName());
    private static MySQLDatabaseManager dbManager;

    /**
     * Initialize the MySQL data store
     */
    public static void initialize() {
        dbManager = new MySQLDatabaseManager();
        dbManager.initializeDatabase();
        logger.info("MySQL DataStore initialized successfully");
    }

    // Authentication
    public static Employee authenticateUser(String employeeId, String password) {
        return dbManager.authenticateUser(employeeId, password);
    }

    // Employee operations
    public static List<Employee> getAllEmployees() {
        return dbManager.getAllEmployees();
    }

    public static List<Employee> getEmployeesByRole(String role) {
        return dbManager.getEmployeesByRole(role);
    }

    public static List<Employee> getEmployeesByDivision(String divisi) {
        return dbManager.getEmployeesByDivision(divisi);
    }

    public static Employee getEmployeeById(String id) {
        return dbManager.getEmployeeById(id);
    }

    public static String getSupervisorByDivision(String divisi) {
        return dbManager.getSupervisorByDivision(divisi);
    }

    // KPI operations
    public static List<KPI> getAllKPI() {
        List<KPI> kpiList = new ArrayList<>();
        String query = "SELECT * FROM kpi ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<KPI> getKPIByDivision(String divisi) {
        List<KPI> kpiList = new ArrayList<>();
        String query = "SELECT * FROM kpi WHERE divisi = ? ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean saveKPI(String divisi, int bulan, int tahun, double score, String managerId) {
        String query = """
            INSERT INTO kpi (divisi, bulan, tahun, score, manager_id, notes) 
            VALUES (?, ?, ?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE score = VALUES(score), manager_id = VALUES(manager_id), notes = VALUES(notes)
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, divisi);
            pstmt.setInt(2, bulan);
            pstmt.setInt(3, tahun);
            pstmt.setDouble(4, score);
            pstmt.setString(5, managerId);
            pstmt.setString(6, "KPI updated for " + divisi + " - " + bulan + "/" + tahun);
            
            int result = pstmt.executeUpdate();
            
            // Update employee layoff risk based on KPI
            updateEmployeeKPIScores(divisi, score);
            
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Error saving KPI: " + e.getMessage());
            return false;
        }
    }

    private static void updateEmployeeKPIScores(String divisi, double kpiScore) {
        String query = "UPDATE employees SET kpi_score = ?, layoff_risk = ? WHERE divisi = ? AND role = 'pegawai'";
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            boolean layoffRisk = kpiScore < 60;
            pstmt.setDouble(1, kpiScore);
            pstmt.setBoolean(2, layoffRisk);
            pstmt.setString(3, divisi);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating employee KPI scores: " + e.getMessage());
        }
    }

    // Report operations
    public static List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports ORDER BY upload_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<Report> getPendingReports() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE status = 'pending' ORDER BY upload_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<Report> getReportsByDivision(String divisi) {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE divisi = ? ORDER BY upload_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean saveReport(String supervisorId, String divisi, int bulan, int tahun, String filePath) {
        String query = """
            INSERT INTO reports (supervisor_id, divisi, bulan, tahun, file_path, status) 
            VALUES (?, ?, ?, ?, ?, 'pending')
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean updateReportStatus(int reportId, String status, String managerNotes, String reviewedBy) {
        String query = """
            UPDATE reports 
            SET status = ?, manager_notes = ?, reviewed_by = ?, reviewed_date = CURRENT_TIMESTAMP 
            WHERE id = ?
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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
    public static List<EmployeeEvaluation> getAllEvaluations() {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<EmployeeEvaluation> getEvaluationsByEmployee(String employeeId) {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations WHERE employee_id = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<EmployeeEvaluation> getEvaluationsBySupervisor(String supervisorId) {
        List<EmployeeEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM employee_evaluations WHERE supervisor_id = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean saveEmployeeEvaluation(String employeeId, String supervisorId,
                                                 double punctualityScore, double attendanceScore,
                                                 double overallRating, String comments) {
        String query = """
            INSERT INTO employee_evaluations (employee_id, supervisor_id, punctuality_score, attendance_score, overall_rating, comments) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    private static void updateEmployeeSupervisorRating(String employeeId, double rating) {
        String query = "UPDATE employees SET supervisor_rating = ?, layoff_risk = ? WHERE id = ?";
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Get current KPI score to determine layoff risk
            Employee emp = getEmployeeById(employeeId);
            boolean layoffRisk = (emp != null && emp.getKpiScore() < 60) || rating < 60;
            
            pstmt.setDouble(1, rating);
            pstmt.setBoolean(2, layoffRisk);
            pstmt.setString(3, employeeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating employee supervisor rating: " + e.getMessage());
        }
    }

    // Monthly Evaluation operations
    public static boolean hasMonthlyEvaluation(String employeeId, int month, int year) {
        String query = "SELECT COUNT(*) FROM monthly_evaluations WHERE employee_id = ? AND month = ? AND year = ?";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean saveMonthlyEmployeeEvaluation(String employeeId, String supervisorId,
                                                        int month, int year,
                                                        double punctualityScore, double attendanceScore,
                                                        double productivityScore, double overallRating,
                                                        String comments) {
        String query = """
            INSERT INTO monthly_evaluations (employee_id, supervisor_id, month, year, punctuality_score, attendance_score, productivity_score, overall_rating, comments) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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
            logger.severe("Error saving monthly employee evaluation: " + e.getMessage());
            return false;
        }
    }

    public static List<DataStore.MonthlyEvaluation> getMonthlyEvaluationsByEmployee(String employeeId) {
        List<DataStore.MonthlyEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM monthly_evaluations WHERE employee_id = ? ORDER BY year DESC, month DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<DataStore.MonthlyEvaluation> getMonthlyEvaluationsBySupervisor(String supervisorId) {
        List<DataStore.MonthlyEvaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM monthly_evaluations WHERE supervisor_id = ? ORDER BY year DESC, month DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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
    public static List<Attendance> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance ORDER BY tanggal DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<Attendance> getAttendanceByEmployee(String employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY tanggal DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<Attendance> getTodayAttendance(String employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? AND tanggal = CURDATE()";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean saveAttendance(String employeeId, Date tanggal, String jamMasuk, String jamKeluar, String status) {
        String query = """
            INSERT INTO attendance (employee_id, tanggal, jam_masuk, jam_keluar, status, is_late) 
            VALUES (?, ?, ?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE jam_masuk = VALUES(jam_masuk), jam_keluar = VALUES(jam_keluar), status = VALUES(status), is_late = VALUES(is_late)
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            boolean isLate = isLateArrival(jamMasuk);
            
            pstmt.setString(1, employeeId);
            pstmt.setDate(2, new java.sql.Date(tanggal.getTime()));
            pstmt.setTime(3, jamMasuk != null ? Time.valueOf(jamMasuk + ":00") : null);
            pstmt.setTime(4, jamKeluar != null ? Time.valueOf(jamKeluar + ":00") : null);
            pstmt.setString(5, status);
            pstmt.setBoolean(6, isLate);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving attendance: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAttendanceClockOut(String employeeId, String jamKeluar) {
        String query = "UPDATE attendance SET jam_keluar = ? WHERE employee_id = ? AND tanggal = CURDATE()";
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setTime(1, Time.valueOf(jamKeluar + ":00"));
            pstmt.setString(2, employeeId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error updating attendance clock out: " + e.getMessage());
            return false;
        }
    }

    private static boolean isLateArrival(String jamMasuk) {
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
    public static List<Meeting> getAllMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT m.*, GROUP_CONCAT(mp.participant_id) as participants 
            FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            GROUP BY m.id 
            ORDER BY m.tanggal, m.waktu_mulai
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<Meeting> getMeetingsByEmployee(String employeeId) {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT m.*, GROUP_CONCAT(mp.participant_id) as participants 
            FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            WHERE m.organizer_id = ? OR mp.participant_id = ? 
            GROUP BY m.id 
            ORDER BY m.tanggal, m.waktu_mulai
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<Meeting> getUpcomingMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        String query = """
            SELECT m.*, GROUP_CONCAT(mp.participant_id) as participants 
            FROM meetings m 
            LEFT JOIN meeting_participants mp ON m.id = mp.meeting_id 
            WHERE m.tanggal >= CURDATE() 
            GROUP BY m.id 
            ORDER BY m.tanggal, m.waktu_mulai
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static boolean saveMeeting(String title, String description, Date tanggal, String waktuMulai,
                                      String waktuSelesai, String lokasi, String organizerId, List<String> participantIds) {
        String insertMeetingQuery = """
            INSERT INTO meetings (title, description, tanggal, waktu_mulai, waktu_selesai, lokasi, organizer_id, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, 'scheduled')
        """;
        
        String insertParticipantQuery = "INSERT INTO meeting_participants (meeting_id, participant_id) VALUES (?, ?)";
        
        try (Connection conn = dbManager.dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement meetingStmt = conn.prepareStatement(insertMeetingQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement participantStmt = conn.prepareStatement(insertParticipantQuery)) {
                
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

    public static boolean updateMeetingStatus(int meetingId, String status) {
        String query = "UPDATE meetings SET status = ? WHERE id = ?";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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
    public static List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests ORDER BY request_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<LeaveRequest> getLeaveRequestsByEmployee(String employeeId) {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests WHERE employee_id = ? ORDER BY request_date DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<LeaveRequest> getPendingLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String query = "SELECT * FROM leave_requests WHERE status = 'pending' ORDER BY request_date";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    public static List<LeaveRequest> getLeaveRequestsForApproval(String approverId) {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        
        Employee approver = getEmployeeById(approverId);
        if (approver == null) return leaveRequests;
        
        String query;
        if (approver.getRole().equals("supervisor")) {
            // Supervisors approve employees in their division
            query = """
                SELECT lr.* FROM leave_requests lr 
                JOIN employees e ON lr.employee_id = e.id 
                WHERE lr.status = 'pending' AND e.role = 'pegawai' AND e.divisi = ? 
                ORDER BY lr.request_date
            """;
        } else if (approver.getRole().equals("manajer")) {
            // Managers approve supervisors and can approve any employee
            query = """
                SELECT lr.* FROM leave_requests lr 
                JOIN employees e ON lr.employee_id = e.id 
                WHERE lr.status = 'pending' AND (e.role = 'supervisor' OR e.role = 'pegawai') 
                ORDER BY lr.request_date
            """;
        } else {
            return leaveRequests;
        }
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            if (approver.getRole().equals("supervisor")) {
                pstmt.setString(1, approver.getDivisi());
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                leaveRequests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting leave requests for approval: " + e.getMessage());
        }
        
        return leaveRequests;
    }

    public static boolean saveLeaveRequest(String employeeId, String leaveType, Date startDate, Date endDate, String reason) {
        // Calculate total days
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int totalDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        
        String query = """
            INSERT INTO leave_requests (employee_id, leave_type, start_date, end_date, total_days, reason, status) 
            VALUES (?, ?, ?, ?, ?, ?, 'pending')
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            pstmt.setString(2, leaveType);
            pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(4, new java.sql.Date(endDate.getTime()));
            pstmt.setInt(5, totalDays);
            pstmt.setString(6, reason);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving leave request: " + e.getMessage());
            return false;
        }
    }

    public static boolean approveLeaveRequest(int leaveRequestId, String approverId, String notes) {
        String query = """
            UPDATE leave_requests 
            SET status = 'approved', approver_id = ?, approver_notes = ?, approval_date = CURRENT_TIMESTAMP 
            WHERE id = ?
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, approverId);
                pstmt.setString(2, notes);
                pstmt.setInt(3, leaveRequestId);
                
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    // Deduct leave days from employee
                    LeaveRequest leaveRequest = getLeaveRequestById(leaveRequestId);
                    if (leaveRequest != null) {
                        String updateEmployeeQuery = "UPDATE employees SET sisa_cuti = sisa_cuti - ? WHERE id = ?";
                        try (PreparedStatement empStmt = conn.prepareStatement(updateEmployeeQuery)) {
                            empStmt.setInt(1, leaveRequest.getTotalDays());
                            empStmt.setString(2, leaveRequest.getEmployeeId());
                            empStmt.executeUpdate();
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

    public static boolean rejectLeaveRequest(int leaveRequestId, String approverId, String notes) {
        String query = """
            UPDATE leave_requests 
            SET status = 'rejected', approver_id = ?, approver_notes = ?, approval_date = CURRENT_TIMESTAMP 
            WHERE id = ?
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    private static LeaveRequest getLeaveRequestById(int id) {
        String query = "SELECT * FROM leave_requests WHERE id = ?";
        
        try (Connection conn = dbManager.dataSource.getConnection();
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

    // Salary History operations
    public static List<SalaryHistory> getAllSalaryHistory() {
        List<SalaryHistory> salaryHistories = new ArrayList<>();
        String query = "SELECT * FROM salary_history ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                salaryHistories.add(mapResultSetToSalaryHistory(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting all salary history: " + e.getMessage());
        }
        
        return salaryHistories;
    }

    public static List<SalaryHistory> getSalaryHistoryByEmployee(String employeeId) {
        List<SalaryHistory> salaryHistories = new ArrayList<>();
        String query = "SELECT * FROM salary_history WHERE employee_id = ? ORDER BY tahun DESC, bulan DESC";
        
        try (Connection conn = dbManager.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                salaryHistories.add(mapResultSetToSalaryHistory(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error getting salary history by employee: " + e.getMessage());
        }
        
        return salaryHistories;
    }

    public static boolean saveSalaryHistory(String employeeId, int bulan, int tahun, double baseSalary,
                                            double kpiBonus, double supervisorBonus, double penalty,
                                            double totalSalary, double kpiScore, double supervisorRating) {
        String query = """
            INSERT INTO salary_history (employee_id, bulan, tahun, base_salary, kpi_bonus, supervisor_bonus, penalty, total_salary, kpi_score, supervisor_rating, notes) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE 
            base_salary = VALUES(base_salary), kpi_bonus = VALUES(kpi_bonus), supervisor_bonus = VALUES(supervisor_bonus), 
            penalty = VALUES(penalty), total_salary = VALUES(total_salary), kpi_score = VALUES(kpi_score), 
            supervisor_rating = VALUES(supervisor_rating), notes = VALUES(notes)
        """;
        
        try (Connection conn = dbManager.dataSource.getConnection();
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
            pstmt.setString(11, "Salary calculation for month " + bulan + "/" + tahun);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error saving salary history: " + e.getMessage());
            return false;
        }
    }

    // Dashboard statistics
    public static Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = dbManager.dataSource.getConnection()) {
            // Total employees by role
            String employeeStatsQuery = """
                SELECT role, COUNT(*) as count FROM employees GROUP BY role
            """;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(employeeStatsQuery)) {
                
                int totalEmployees = 0;
                while (rs.next()) {
                    String role = rs.getString("role");
                    int count = rs.getInt("count");
                    totalEmployees += count;
                    
                    switch (role) {
                        case "manajer" -> stats.put("totalManagers", count);
                        case "supervisor" -> stats.put("totalSupervisors", count);
                        case "pegawai" -> stats.put("totalEmployeesRegular", count);
                    }
                }
                stats.put("totalEmployees", totalEmployees);
            }
            
            // Layoff risk employees
            String layoffQuery = "SELECT COUNT(*) FROM employees WHERE layoff_risk = true";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(layoffQuery)) {
                if (rs.next()) {
                    stats.put("layoffRiskEmployees", rs.getInt(1));
                }
            }
            
            // Pending reports
            String pendingReportsQuery = "SELECT COUNT(*) FROM reports WHERE status = 'pending'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(pendingReportsQuery)) {
                if (rs.next()) {
                    stats.put("pendingReports", rs.getInt(1));
                }
            }
            
            // Pending leave requests
            String pendingLeaveQuery = "SELECT COUNT(*) FROM leave_requests WHERE status = 'pending'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(pendingLeaveQuery)) {
                if (rs.next()) {
                    stats.put("pendingLeaveRequests", rs.getInt(1));
                }
            }
            
            // Upcoming meetings
            String upcomingMeetingsQuery = "SELECT COUNT(*) FROM meetings WHERE tanggal >= CURDATE()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(upcomingMeetingsQuery)) {
                if (rs.next()) {
                    stats.put("upcomingMeetings", rs.getInt(1));
                }
            }
            
            // Average KPI by division
            Map<String, Double> avgKpiByDivision = new HashMap<>();
            String avgKpiQuery = """
                SELECT divisi, AVG(score) as avg_score 
                FROM kpi 
                WHERE tahun = YEAR(CURDATE()) 
                GROUP BY divisi
            """;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(avgKpiQuery)) {
                while (rs.next()) {
                    avgKpiByDivision.put(rs.getString("divisi"), rs.getDouble("avg_score"));
                }
            }
            stats.put("avgKpiByDivision", avgKpiByDivision);
            
        } catch (SQLException e) {
            logger.severe("Error getting dashboard stats: " + e.getMessage());
        }
        
        return stats;
    }

    // Mapping methods
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

    private static DataStore.MonthlyEvaluation mapResultSetToMonthlyEvaluation(ResultSet rs) throws SQLException {
        DataStore.MonthlyEvaluation eval = new DataStore.MonthlyEvaluation();
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
        meeting.setWaktuMulai(rs.getTime("waktu_mulai").toString().substring(0, 5));
        meeting.setWaktuSelesai(rs.getTime("waktu_selesai").toString().substring(0, 5));
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

    public static void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}
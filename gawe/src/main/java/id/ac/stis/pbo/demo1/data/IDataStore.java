package id.ac.stis.pbo.demo1.data;

import id.ac.stis.pbo.demo1.models.*;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Interface defining data store operations for GAWE application
 */
public interface IDataStore {
    // Authentication
    Employee authenticateUser(String employeeId, String password);
    
    // Employee operations
    List<Employee> getAllEmployees();
    List<Employee> getEmployeesByRole(String role);
    List<Employee> getEmployeesByDivision(String divisi);
    Employee getEmployeeById(String id);
    void updateEmployee(Employee employee);
    
    // KPI operations
    List<KPI> getAllKPI();
    List<KPI> getKPIByDivision(String divisi);
    boolean saveKPI(String divisi, int bulan, int tahun, double score, String managerId);
    
    // Report operations
    List<Report> getAllReports();
    List<Report> getPendingReports();
    List<Report> getReportsByDivision(String divisi);
    boolean saveReport(String supervisorId, String divisi, int bulan, int tahun, String filePath);
    boolean updateReportStatus(int reportId, String status, String managerNotes, String reviewedBy);
    
    // Attendance operations
    List<Attendance> getAllAttendance();
    List<Attendance> getAttendanceByEmployee(String employeeId);
    List<Attendance> getTodayAttendance(String employeeId);
    boolean saveAttendance(String employeeId, Date tanggal, String jamMasuk, String jamKeluar, String status);
    boolean updateAttendanceClockOut(String employeeId, String jamKeluar);
    
    // Meeting operations
    List<Meeting> getAllMeetings();
    List<Meeting> getMeetingsByEmployee(String employeeId);
    List<Meeting> getUpcomingMeetings();
    boolean saveMeeting(String title, String description, Date tanggal, String waktuMulai,
                       String waktuSelesai, String lokasi, String organizerId, List<String> participantIds);
    boolean updateMeetingStatus(int meetingId, String status);
    
    // Leave Request operations
    List<LeaveRequest> getAllLeaveRequests();
    List<LeaveRequest> getLeaveRequestsByEmployee(String employeeId);
    List<LeaveRequest> getPendingLeaveRequests();
    List<LeaveRequest> getLeaveRequestsForApproval(String approverId);
    boolean saveLeaveRequest(String employeeId, String leaveType, Date startDate, Date endDate, String reason);
    boolean approveLeaveRequest(int leaveRequestId, String approverId, String notes);
    boolean rejectLeaveRequest(int leaveRequestId, String approverId, String notes);
    
    // Salary History operations
    List<SalaryHistory> getAllSalaryHistory();
    List<SalaryHistory> getSalaryHistoryByEmployee(String employeeId);
    
    // Utility operations
    String getSupervisorByDivision(String divisi);
    Map<String, Object> getDashboardStats();
    
    // Resource cleanup
    void close();
}

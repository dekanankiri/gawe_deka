-- GAWE Application MySQL Database Setup Script
-- Run this script to create the database and tables

-- Create database
CREATE DATABASE IF NOT EXISTS gawe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gawe_db;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS meeting_participants;
DROP TABLE IF EXISTS meetings;
DROP TABLE IF EXISTS salary_history;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS monthly_evaluations;
DROP TABLE IF EXISTS employee_evaluations;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS kpi;
DROP TABLE IF EXISTS employees;

-- Create employees table
CREATE TABLE employees (
    id VARCHAR(20) PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('manajer', 'supervisor', 'pegawai') NOT NULL,
    divisi VARCHAR(50) NOT NULL,
    jabatan VARCHAR(100) NOT NULL,
    tgl_masuk DATE NOT NULL,
    sisa_cuti INT DEFAULT 12,
    gaji_pokok DECIMAL(15,2) NOT NULL,
    kpi_score DECIMAL(5,2) DEFAULT 0.0,
    supervisor_rating DECIMAL(5,2) DEFAULT 0.0,
    layoff_risk BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create KPI table
CREATE TABLE kpi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    divisi VARCHAR(50) NOT NULL,
    bulan INT NOT NULL,
    tahun INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    manager_id VARCHAR(20) NOT NULL,
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY unique_kpi (divisi, bulan, tahun)
);

-- Create reports table
CREATE TABLE reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supervisor_id VARCHAR(20) NOT NULL,
    divisi VARCHAR(50) NOT NULL,
    bulan INT NOT NULL,
    tahun INT NOT NULL,
    file_path TEXT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'reviewed', 'approved', 'rejected') DEFAULT 'pending',
    manager_notes TEXT,
    reviewed_by VARCHAR(20),
    reviewed_date TIMESTAMP NULL,
    FOREIGN KEY (supervisor_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES employees(id) ON DELETE SET NULL
);

-- Create employee evaluations table
CREATE TABLE employee_evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    supervisor_id VARCHAR(20) NOT NULL,
    punctuality_score DECIMAL(5,2) NOT NULL,
    attendance_score DECIMAL(5,2) NOT NULL,
    overall_rating DECIMAL(5,2) NOT NULL,
    evaluation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comments TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (supervisor_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Create monthly evaluations table
CREATE TABLE monthly_evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    supervisor_id VARCHAR(20) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    punctuality_score DECIMAL(5,2) NOT NULL,
    attendance_score DECIMAL(5,2) NOT NULL,
    productivity_score DECIMAL(5,2) NOT NULL,
    overall_rating DECIMAL(5,2) NOT NULL,
    comments TEXT,
    evaluation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (supervisor_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY unique_monthly_eval (employee_id, month, year)
);

-- Create attendance table
CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    tanggal DATE NOT NULL,
    jam_masuk TIME,
    jam_keluar TIME,
    status ENUM('hadir', 'sakit', 'izin', 'alpha') NOT NULL,
    keterangan TEXT,
    is_late BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY unique_attendance (employee_id, tanggal)
);

-- Create meetings table
CREATE TABLE meetings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    tanggal DATE NOT NULL,
    waktu_mulai TIME NOT NULL,
    waktu_selesai TIME NOT NULL,
    lokasi VARCHAR(200) NOT NULL,
    organizer_id VARCHAR(20) NOT NULL,
    status ENUM('scheduled', 'ongoing', 'completed', 'cancelled') DEFAULT 'scheduled',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Create meeting participants table
CREATE TABLE meeting_participants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    meeting_id INT NOT NULL,
    participant_id VARCHAR(20) NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY unique_participant (meeting_id, participant_id)
);

-- Create leave requests table
CREATE TABLE leave_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    approver_id VARCHAR(20),
    approver_notes TEXT,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approval_date TIMESTAMP NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (approver_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- Create salary history table
CREATE TABLE salary_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    bulan INT NOT NULL,
    tahun INT NOT NULL,
    base_salary DECIMAL(15,2) NOT NULL,
    kpi_bonus DECIMAL(15,2) DEFAULT 0.0,
    supervisor_bonus DECIMAL(15,2) DEFAULT 0.0,
    penalty DECIMAL(15,2) DEFAULT 0.0,
    total_salary DECIMAL(15,2) NOT NULL,
    kpi_score DECIMAL(5,2) NOT NULL,
    supervisor_rating DECIMAL(5,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY unique_salary (employee_id, bulan, tahun)
);

-- Create indexes for better performance
CREATE INDEX idx_employees_role ON employees(role);
CREATE INDEX idx_employees_divisi ON employees(divisi);
CREATE INDEX idx_kpi_divisi_date ON kpi(divisi, tahun, bulan);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_divisi ON reports(divisi);
CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, tanggal);
CREATE INDEX idx_meetings_date ON meetings(tanggal);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_salary_history_employee_date ON salary_history(employee_id, tahun, bulan);

-- Display success message
SELECT 'Database setup completed successfully!' as message;
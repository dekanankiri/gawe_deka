package id.ac.stis.pbo.demo1.ui;

import id.ac.stis.pbo.demo1.data.DataStore;
import id.ac.stis.pbo.demo1.models.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

/**
 * Enhanced Manager Dashboard with comprehensive features and fixes
 */
public class ManagerDashboard extends Application {
    private final Employee manager;
    private StackPane contentArea;
    private DecimalFormat df = new DecimalFormat("#.##");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public ManagerDashboard(Employee manager) {
        this.manager = manager;
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Create header, navigation, and content areas
        HBox header = createHeader(stage);
        VBox navigation = createNavigation();
        contentArea = createContentArea();

        root.setTop(header);
        root.setLeft(navigation);
        root.setCenter(contentArea);

        // Show default dashboard content
        showDashboardContent();

        Scene scene = new Scene(root, 1400, 800);
        stage.setScene(scene);
        stage.setTitle("GAWE - Manager Dashboard");
        stage.show();
    }

    private HBox createHeader(Stage primaryStage) {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2c3e50;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("GAWE - Manager Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Welcome, " + manager.getNama() + " (General Manager)");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        userLabel.setTextFill(Color.WHITE);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-padding: 8 15;
            -fx-font-weight: bold;
        """);
        logoutBtn.setOnAction(e -> {
            primaryStage.close();
            new id.ac.stis.pbo.demo1.HelloApplication().start(new Stage());
        });

        header.getChildren().addAll(titleLabel, spacer, userLabel, logoutBtn);
        return header;
    }

    private VBox createNavigation() {
        VBox navigation = new VBox(10);
        navigation.setPadding(new Insets(20));
        navigation.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 0 1 0 0;");
        navigation.setPrefWidth(250);

        // Personal Features (like Employee)
        Label personalLabel = new Label("Personal");
        personalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        personalLabel.setTextFill(Color.web("#7f8c8d"));

        Button dashboardBtn = createNavButton("📊 Dashboard");
        dashboardBtn.setOnAction(e -> showDashboardContent());

        Button attendanceBtn = createNavButton("⏰ My Attendance");
        attendanceBtn.setOnAction(e -> showMyAttendance());

        Button meetingsBtn = createNavButton("📅 My Meetings");
        meetingsBtn.setOnAction(e -> showMyMeetings());

        Button leaveBtn = createNavButton("🏖️ My Leave Requests");
        leaveBtn.setOnAction(e -> showMyLeaveRequests());

        Button salaryBtn = createNavButton("💰 My Salary History");
        salaryBtn.setOnAction(e -> showMySalaryHistory());

        // Manager Features
        Label managerLabel = new Label("Manager Functions");
        managerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        managerLabel.setTextFill(Color.web("#7f8c8d"));

        Button kpiBtn = createNavButton("📈 KPI Management");
        kpiBtn.setOnAction(e -> showKPIManagement());

        Button reportsBtn = createNavButton("📋 Review Reports");
        reportsBtn.setOnAction(e -> showReportReview());

        Button employeesBtn = createNavButton("👥 Employee Overview");
        employeesBtn.setOnAction(e -> showEmployeeOverview());

        Button leaveApprovalBtn = createNavButton("✅ Leave Approvals");
        leaveApprovalBtn.setOnAction(e -> showLeaveApprovals());

        Button scheduleMeetingBtn = createNavButton("📅 Schedule Meeting");
        scheduleMeetingBtn.setOnAction(e -> showScheduleMeeting());

        Button analyticsBtn = createNavButton("📊 Analytics");
        analyticsBtn.setOnAction(e -> showAnalytics());

        navigation.getChildren().addAll(
                personalLabel, new Separator(),
                dashboardBtn, attendanceBtn, meetingsBtn, leaveBtn, salaryBtn,
                new Separator(), managerLabel, new Separator(),
                kpiBtn, reportsBtn, employeesBtn, leaveApprovalBtn, scheduleMeetingBtn, analyticsBtn
        );

        return navigation;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #495057;
            -fx-font-size: 14px;
            -fx-alignment: center-left;
            -fx-padding: 15 20;
            -fx-pref-width: 210;
            -fx-cursor: hand;
        """);
        button.setOnMouseEntered(e -> button.setStyle("""
            -fx-background-color: #e9ecef;
            -fx-text-fill: #495057;
            -fx-font-size: 14px;
            -fx-alignment: center-left;
            -fx-padding: 15 20;
            -fx-pref-width: 210;
            -fx-cursor: hand;
        """));
        button.setOnMouseExited(e -> button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #495057;
            -fx-font-size: 14px;
            -fx-alignment: center-left;
            -fx-padding: 15 20;
            -fx-pref-width: 210;
            -fx-cursor: hand;
        """));
        return button;
    }

    private StackPane createContentArea() {
        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        return contentArea;
    }

    private void showDashboardContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Manager Dashboard Overview");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Personal and company statistics
        HBox statsRow = createDashboardStats();

        // Quick actions
        VBox quickActions = createQuickActions();

        // Daily overview
        HBox dailyOverview = createDailyOverview();

        content.getChildren().addAll(titleLabel, statsRow, quickActions, dailyOverview);
        contentArea.getChildren().add(content);
    }

    private HBox createDashboardStats() {
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);

        // Personal stats
        VBox personalSalaryCard = createStatCard("My Salary",
                "Rp " + String.format("%,.0f", manager.calculateGajiBulanan()),
                "#27ae60");
        VBox leaveCard = createStatCard("My Leave Days",
                manager.getSisaCuti() + " days",
                "#f39c12");

        // Company stats
        Map<String, Object> stats = DataStore.getDashboardStats();

        VBox totalEmployeesCard = createStatCard("Total Employees",
                stats.get("totalEmployees").toString(),
                "#3498db");
        VBox layoffRiskCard = createStatCard("Layoff Risk",
                stats.get("layoffRiskEmployees").toString(),
                "#e74c3c");
        VBox pendingReportsCard = createStatCard("Pending Reports",
                stats.get("pendingReports").toString(),
                "#9b59b6");

        List<LeaveRequest> pendingLeaveRequests = DataStore.getLeaveRequestsForApproval(manager.getId());
        VBox pendingLeaveCard = createStatCard("Pending Leave",
                String.valueOf(pendingLeaveRequests.size()),
                "#f39c12");

        statsRow.getChildren().addAll(personalSalaryCard, leaveCard, totalEmployeesCard,
                layoffRiskCard, pendingReportsCard, pendingLeaveCard);
        return statsRow;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            -fx-border-color: %s;
            -fx-border-width: 0 0 3 0;
        """, color));
        card.setPrefWidth(180);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private VBox createQuickActions() {
        VBox actionsBox = new VBox(15);

        Label titleLabel = new Label("Quick Actions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER);

        // Check if already clocked in today
        boolean alreadyClockedIn = hasAttendanceToday();

        Button clockInBtn = new Button("⏰ Clock In");
        clockInBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
        """);
        clockInBtn.setOnAction(e -> clockIn());
        clockInBtn.setDisable(alreadyClockedIn);

        Button clockOutBtn = new Button("🏃 Clock Out");
        clockOutBtn.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
        """);
        clockOutBtn.setOnAction(e -> clockOut());
        clockOutBtn.setDisable(!alreadyClockedIn || hasCompletedAttendanceToday());

        Button requestLeaveBtn = new Button("🏖️ Request Leave");
        requestLeaveBtn.setStyle("""
            -fx-background-color: #f39c12;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
        """);
        requestLeaveBtn.setOnAction(e -> showLeaveRequestDialog());

        Button scheduleBtn = new Button("📅 Schedule Meeting");
        scheduleBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
        """);
        scheduleBtn.setOnAction(e -> showScheduleMeetingDialog());

        Button setKpiBtn = new Button("📈 Set KPI");
        setKpiBtn.setStyle("""
            -fx-background-color: #9b59b6;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
        """);
        setKpiBtn.setOnAction(e -> showKPIManagement());

        buttonsRow.getChildren().addAll(clockInBtn, clockOutBtn, requestLeaveBtn, scheduleBtn, setKpiBtn);

        actionsBox.getChildren().addAll(titleLabel, buttonsRow);
        return actionsBox;
    }

    private boolean hasAttendanceToday() {
        List<Attendance> todayAttendance = DataStore.getTodayAttendance(manager.getId());
        return !todayAttendance.isEmpty();
    }

    private boolean hasCompletedAttendanceToday() {
        List<Attendance> todayAttendance = DataStore.getTodayAttendance(manager.getId());
        return !todayAttendance.isEmpty() &&
                todayAttendance.get(0).getJamKeluar() != null;
    }

    private HBox createDailyOverview() {
        HBox overviewRow = new HBox(20);
        overviewRow.setAlignment(Pos.CENTER);

        // Today's meetings
        VBox meetingsBox = createTodaysMeetingsPreview();

        // Recent activities
        VBox activitiesBox = createRecentActivitiesPreview();

        overviewRow.getChildren().addAll(meetingsBox, activitiesBox);
        return overviewRow;
    }

    private VBox createTodaysMeetingsPreview() {
        VBox meetingsBox = new VBox(15);
        meetingsBox.setPadding(new Insets(20));
        meetingsBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);
        meetingsBox.setPrefWidth(400);

        Label titleLabel = new Label("Today's Meetings");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        ListView<String> meetingsList = new ListView<>();
        meetingsList.setPrefHeight(150);

        List<Meeting> todaysMeetings = DataStore.getMeetingsByEmployee(manager.getId());
        Calendar today = Calendar.getInstance();

        ObservableList<String> meetingItems = FXCollections.observableArrayList();
        for (Meeting meeting : todaysMeetings) {
            Calendar meetingCal = Calendar.getInstance();
            meetingCal.setTime(meeting.getTanggal());

            if (meetingCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    meetingCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                meetingItems.add("📅 " + meeting.getTitle() + " at " + meeting.getWaktuMulai() +
                        " (" + meeting.getLokasi() + ")");
            }
        }

        if (meetingItems.isEmpty()) {
            meetingItems.add("No meetings scheduled for today");
        }

        meetingsList.setItems(meetingItems);

        meetingsBox.getChildren().addAll(titleLabel, meetingsList);
        return meetingsBox;
    }

    private VBox createRecentActivitiesPreview() {
        VBox activitiesBox = new VBox(15);
        activitiesBox.setPadding(new Insets(20));
        activitiesBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);
        activitiesBox.setPrefWidth(400);

        Label titleLabel = new Label("Recent Activities");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        ListView<String> activitiesList = new ListView<>();
        activitiesList.setPrefHeight(150);

        ObservableList<String> activities = FXCollections.observableArrayList();

        // Add recent reports
        List<Report> pendingReports = DataStore.getPendingReports();
        for (Report report : pendingReports.stream().limit(3).toList()) {
            activities.add("📋 New report from " + report.getDivisi() + " division - " +
                    report.getMonthName() + " " + report.getTahun());
        }

        // Add layoff risk employees
        List<Employee> layoffRiskEmployees = DataStore.getAllEmployees().stream()
                .filter(Employee::isLayoffRisk)
                .limit(3)
                .toList();
        for (Employee emp : layoffRiskEmployees) {
            activities.add("⚠️ " + emp.getNama() + " (" + emp.getDivisi() + ") is at layoff risk");
        }

        // Add pending leave requests
        List<LeaveRequest> pendingLeaveRequests = DataStore.getLeaveRequestsForApproval(manager.getId());
        for (LeaveRequest leave : pendingLeaveRequests.stream().limit(2).toList()) {
            Employee requester = DataStore.getEmployeeById(leave.getEmployeeId());
            if (requester != null) {
                activities.add("🏖️ Leave request from " + requester.getNama() + " (" + leave.getTotalDays() + " days)");
            }
        }

        if (activities.isEmpty()) {
            activities.add("No recent activities");
        }

        activitiesList.setItems(activities);

        activitiesBox.getChildren().addAll(titleLabel, activitiesList);
        return activitiesBox;
    }

    // Personal Features (like Employee Dashboard)
    private void showMyAttendance() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Attendance");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        TableView<Attendance> attendanceTable = createAttendanceTable();

        content.getChildren().addAll(titleLabel, attendanceTable);
        contentArea.getChildren().add(content);
    }

    private TableView<Attendance> createAttendanceTable() {
        TableView<Attendance> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Attendance, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getTanggal())));

        TableColumn<Attendance, String> clockInCol = new TableColumn<>("Clock In");
        clockInCol.setCellValueFactory(new PropertyValueFactory<>("jamMasuk"));

        TableColumn<Attendance, String> clockOutCol = new TableColumn<>("Clock Out");
        clockOutCol.setCellValueFactory(new PropertyValueFactory<>("jamKeluar"));

        TableColumn<Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Attendance, String> lateCol = new TableColumn<>("Late");
        lateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isLate() ? "Yes" : "No"));

        table.getColumns().addAll(dateCol, clockInCol, clockOutCol, statusCol, lateCol);

        List<Attendance> myAttendance = DataStore.getAttendanceByEmployee(manager.getId());
        ObservableList<Attendance> attendanceData = FXCollections.observableArrayList(myAttendance);
        table.setItems(attendanceData);

        return table;
    }

    private void showMyMeetings() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Meetings");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Button newMeetingBtn = new Button("➕ Schedule New Meeting");
        newMeetingBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);
        newMeetingBtn.setOnAction(e -> showScheduleMeetingDialog());

        TableView<Meeting> meetingsTable = createMeetingsTable();

        content.getChildren().addAll(titleLabel, newMeetingBtn, meetingsTable);
        contentArea.getChildren().add(content);
    }

    private TableView<Meeting> createMeetingsTable() {
        TableView<Meeting> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Meeting, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Meeting, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getTanggal())));
        dateCol.setPrefWidth(100);

        TableColumn<Meeting, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getWaktuMulai() + " - " + cellData.getValue().getWaktuSelesai()));
        timeCol.setPrefWidth(120);

        TableColumn<Meeting, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        locationCol.setPrefWidth(150);

        TableColumn<Meeting, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(titleCol, dateCol, timeCol, locationCol, statusCol);

        List<Meeting> myMeetings = DataStore.getMeetingsByEmployee(manager.getId());
        ObservableList<Meeting> meetingData = FXCollections.observableArrayList(myMeetings);
        table.setItems(meetingData);

        return table;
    }

    private void showMyLeaveRequests() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Leave Requests");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Button newRequestBtn = new Button("➕ New Leave Request");
        newRequestBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);
        newRequestBtn.setOnAction(e -> showLeaveRequestDialog());

        TableView<LeaveRequest> leaveTable = createMyLeaveRequestsTable();

        content.getChildren().addAll(titleLabel, newRequestBtn, leaveTable);
        contentArea.getChildren().add(content);
    }

    private TableView<LeaveRequest> createMyLeaveRequestsTable() {
        TableView<LeaveRequest> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<LeaveRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        TableColumn<LeaveRequest, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getStartDate())));

        TableColumn<LeaveRequest, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getEndDate())));

        TableColumn<LeaveRequest, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("totalDays"));

        TableColumn<LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<LeaveRequest, String> notesCol = new TableColumn<>("Approval Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("approverNotes"));

        table.getColumns().addAll(typeCol, startDateCol, endDateCol, daysCol, statusCol, notesCol);

        List<LeaveRequest> myLeaveRequests = DataStore.getLeaveRequestsByEmployee(manager.getId());
        ObservableList<LeaveRequest> leaveData = FXCollections.observableArrayList(myLeaveRequests);
        table.setItems(leaveData);

        return table;
    }

    private void showMySalaryHistory() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Salary History");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        VBox salaryBreakdown = createSalaryBreakdown();
        TableView<SalaryHistory> salaryTable = createSalaryHistoryTable();

        content.getChildren().addAll(titleLabel, salaryBreakdown, salaryTable);
        contentArea.getChildren().add(content);
    }

    private VBox createSalaryBreakdown() {
        VBox breakdownBox = new VBox(15);
        breakdownBox.setPadding(new Insets(20));
        breakdownBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        Label breakdownTitle = new Label("Current Monthly Salary Breakdown");
        breakdownTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane salaryGrid = new GridPane();
        salaryGrid.setHgap(20);
        salaryGrid.setVgap(15);

        double baseSalary = manager.getGajiPokok();
        double totalSalary = manager.calculateGajiBulanan();

        salaryGrid.add(new Label("Base Salary:"), 0, 0);
        salaryGrid.add(new Label("Rp " + String.format("%,.0f", baseSalary)), 1, 0);

        salaryGrid.add(new Label("Total Monthly Salary:"), 0, 1);
        Label totalLabel = new Label("Rp " + String.format("%,.0f", totalSalary));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.web("#27ae60"));
        salaryGrid.add(totalLabel, 1, 1);

        breakdownBox.getChildren().addAll(breakdownTitle, salaryGrid);
        return breakdownBox;
    }

    private TableView<SalaryHistory> createSalaryHistoryTable() {
        TableView<SalaryHistory> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<SalaryHistory, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<SalaryHistory, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<SalaryHistory, String> totalCol = new TableColumn<>("Total Salary");
        totalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getTotalSalary())));

        table.getColumns().addAll(monthCol, yearCol, totalCol);

        List<SalaryHistory> mySalaryHistory = DataStore.getSalaryHistoryByEmployee(manager.getId());
        ObservableList<SalaryHistory> salaryData = FXCollections.observableArrayList(mySalaryHistory);
        table.setItems(salaryData);

        return table;
    }

    // Manager-specific features
    private void showKPIManagement() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("KPI Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        VBox kpiForm = createKPIForm();
        TableView<KPI> kpiTable = createKPITable();

        content.getChildren().addAll(titleLabel, kpiForm, kpiTable);
        contentArea.getChildren().add(content);
    }

    private VBox createKPIForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        Label formTitle = new Label("Set Division KPI");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        ComboBox<String> divisionCombo = new ComboBox<>();
        divisionCombo.getItems().addAll("HR", "Marketing", "Sales", "IT", "Finance");
        divisionCombo.setPromptText("Select Division");

        ComboBox<Integer> monthCombo = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthCombo.getItems().add(i);
        }
        monthCombo.setPromptText("Select Month");

        ComboBox<Integer> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll(2024, 2025);
        yearCombo.setValue(2024);

        TextField scoreField = new TextField();
        scoreField.setPromptText("KPI Score (0-100)");

        Button saveBtn = new Button("Save KPI");
        saveBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);

        saveBtn.setOnAction(e -> {
            try {
                String division = divisionCombo.getValue();
                Integer month = monthCombo.getValue();
                Integer year = yearCombo.getValue();
                double score = Double.parseDouble(scoreField.getText());

                if (division != null && month != null && year != null && score >= 0 && score <= 100) {
                    boolean success = DataStore.saveKPI(division, month, year, score, manager.getId());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "KPI saved successfully!");
                        showKPIManagement(); // Refresh the view
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to save KPI.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please fill all fields with valid values.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Score", "Please enter a valid numeric score.");
            }
        });

        formGrid.add(new Label("Division:"), 0, 0);
        formGrid.add(divisionCombo, 1, 0);
        formGrid.add(new Label("Month:"), 0, 1);
        formGrid.add(monthCombo, 1, 1);
        formGrid.add(new Label("Year:"), 0, 2);
        formGrid.add(yearCombo, 1, 2);
        formGrid.add(new Label("Score:"), 0, 3);
        formGrid.add(scoreField, 1, 3);
        formGrid.add(saveBtn, 1, 4);

        formBox.getChildren().addAll(formTitle, formGrid);
        return formBox;
    }

    private TableView<KPI> createKPITable() {
        TableView<KPI> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<KPI, String> divisionCol = new TableColumn<>("Division");
        divisionCol.setCellValueFactory(new PropertyValueFactory<>("divisi"));

        TableColumn<KPI, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<KPI, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<KPI, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getScore()) + "%"));

        table.getColumns().addAll(divisionCol, monthCol, yearCol, scoreCol);

        ObservableList<KPI> kpiData = FXCollections.observableArrayList(DataStore.getAllKPI());
        table.setItems(kpiData);

        return table;
    }

    private void showReportReview() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Report Review");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        TableView<Report> reportsTable = createReportsTable();

        content.getChildren().addAll(titleLabel, reportsTable);
        contentArea.getChildren().add(content);
    }

    private TableView<Report> createReportsTable() {
        TableView<Report> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Report, String> divisionCol = new TableColumn<>("Division");
        divisionCol.setCellValueFactory(new PropertyValueFactory<>("divisi"));

        TableColumn<Report, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<Report, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<Report, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Report, String> uploadDateCol = new TableColumn<>("Upload Date");
        uploadDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getUploadDate())));

        TableColumn<Report, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<Report, Void>() {
            private final Button reviewBtn = new Button("Review");

            {
                reviewBtn.setStyle("""
                    -fx-background-color: #3498db;
                    -fx-text-fill: white;
                    -fx-padding: 5 10;
                    -fx-background-radius: 3;
                """);
                reviewBtn.setOnAction(e -> {
                    Report report = getTableView().getItems().get(getIndex());
                    showReportReviewDialog(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Report report = getTableView().getItems().get(getIndex());
                    if ("pending".equals(report.getStatus())) {
                        setGraphic(reviewBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(divisionCol, monthCol, yearCol, statusCol, uploadDateCol, actionCol);

        ObservableList<Report> reportData = FXCollections.observableArrayList(DataStore.getAllReports());
        table.setItems(reportData);

        return table;
    }

    private void showReportReviewDialog(Report report) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Review Report");
        dialog.setHeaderText("Review report for " + report.getDivisi() + " - " +
                report.getMonthName() + " " + report.getTahun());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label fileLabel = new Label("File: " + report.getFilePath());

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter review notes...");
        notesArea.setPrefRowCount(4);

        content.getChildren().addAll(
                new Label("Report Details:"),
                fileLabel,
                new Label("Manager Notes:"),
                notesArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String notes = notesArea.getText();
                boolean success = DataStore.updateReportStatus(report.getId(), "reviewed", notes, manager.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Report reviewed successfully!");
                    showReportReview(); // Refresh the view
                }
            }
        });
    }

    private void showEmployeeOverview() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Employee Overview");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        TableView<Employee> employeeTable = createEmployeeTable();

        content.getChildren().addAll(titleLabel, employeeTable);
        contentArea.getChildren().add(content);
    }

    private TableView<Employee> createEmployeeTable() {
        TableView<Employee> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Employee, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nama"));

        TableColumn<Employee, String> divisionCol = new TableColumn<>("Division");
        divisionCol.setCellValueFactory(new PropertyValueFactory<>("divisi"));

        TableColumn<Employee, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<Employee, String> kpiCol = new TableColumn<>("KPI Score");
        kpiCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getKpiScore()) + "%"));

        TableColumn<Employee, String> supervisorCol = new TableColumn<>("Supervisor Rating");
        supervisorCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getSupervisorRating()) + "%"));

        TableColumn<Employee, String> salaryCol = new TableColumn<>("Monthly Salary");
        salaryCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().calculateGajiBulanan())));

        TableColumn<Employee, String> leaveCol = new TableColumn<>("Leave Days");
        leaveCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getSisaCuti())));

        TableColumn<Employee, String> riskCol = new TableColumn<>("Layoff Risk");
        riskCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isLayoffRisk() ? "⚠️ YES" : "✅ NO"));

        table.getColumns().addAll(idCol, nameCol, divisionCol, roleCol, kpiCol, supervisorCol, salaryCol, leaveCol, riskCol);

        ObservableList<Employee> employeeData = FXCollections.observableArrayList(DataStore.getAllEmployees());
        table.setItems(employeeData);

        return table;
    }

    private void showLeaveApprovals() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Leave Request Approvals (Supervisors)");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        TableView<LeaveRequest> leaveApprovalsTable = createLeaveApprovalsTable();

        content.getChildren().addAll(titleLabel, leaveApprovalsTable);
        contentArea.getChildren().add(content);
    }

    private TableView<LeaveRequest> createLeaveApprovalsTable() {
        TableView<LeaveRequest> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<LeaveRequest, String> employeeCol = new TableColumn<>("Supervisor");
        employeeCol.setCellValueFactory(cellData -> {
            Employee emp = DataStore.getEmployeeById(cellData.getValue().getEmployeeId());
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getNama() : "Unknown");
        });

        TableColumn<LeaveRequest, String> divisionCol = new TableColumn<>("Division");
        divisionCol.setCellValueFactory(cellData -> {
            Employee emp = DataStore.getEmployeeById(cellData.getValue().getEmployeeId());
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getDivisi() : "Unknown");
        });

        TableColumn<LeaveRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        TableColumn<LeaveRequest, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getStartDate())));

        TableColumn<LeaveRequest, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getEndDate())));

        TableColumn<LeaveRequest, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("totalDays"));

        TableColumn<LeaveRequest, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<LeaveRequest, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<LeaveRequest, Void>() {
            private final Button approveBtn = new Button("✅ Approve");
            private final Button rejectBtn = new Button("❌ Reject");
            private final HBox actions = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");

                approveBtn.setOnAction(e -> {
                    LeaveRequest leave = getTableView().getItems().get(getIndex());
                    showApprovalDialog(leave, true);
                });

                rejectBtn.setOnAction(e -> {
                    LeaveRequest leave = getTableView().getItems().get(getIndex());
                    showApprovalDialog(leave, false);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LeaveRequest leave = getTableView().getItems().get(getIndex());
                    if ("pending".equals(leave.getStatus())) {
                        setGraphic(actions);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(employeeCol, divisionCol, typeCol, startDateCol, endDateCol, daysCol, reasonCol, actionCol);

        List<LeaveRequest> pendingLeaveRequests = DataStore.getLeaveRequestsForApproval(manager.getId());
        ObservableList<LeaveRequest> leaveData = FXCollections.observableArrayList(pendingLeaveRequests);
        table.setItems(leaveData);

        return table;
    }

    private void showApprovalDialog(LeaveRequest leaveRequest, boolean isApproval) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isApproval ? "Approve Leave Request" : "Reject Leave Request");

        Employee requester = DataStore.getEmployeeById(leaveRequest.getEmployeeId());
        dialog.setHeaderText((isApproval ? "Approve" : "Reject") + " leave request for " +
                (requester != null ? requester.getNama() : "Unknown Employee"));

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label detailsLabel = new Label("Leave Details:");
        detailsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        TextArea detailsArea = new TextArea();
        detailsArea.setText("Type: " + leaveRequest.getLeaveType() + "\n" +
                "Start Date: " + sdf.format(leaveRequest.getStartDate()) + "\n" +
                "End Date: " + sdf.format(leaveRequest.getEndDate()) + "\n" +
                "Total Days: " + leaveRequest.getTotalDays() + "\n" +
                "Reason: " + leaveRequest.getReason());
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(5);

        Label notesLabel = new Label("Notes (optional):");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter approval/rejection notes...");
        notesArea.setPrefRowCount(3);

        content.getChildren().addAll(detailsLabel, detailsArea, notesLabel, notesArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String notes = notesArea.getText();
                boolean success;

                if (isApproval) {
                    success = DataStore.approveLeaveRequest(leaveRequest.getId(), manager.getId(), notes);
                } else {
                    success = DataStore.rejectLeaveRequest(leaveRequest.getId(), manager.getId(), notes);
                }

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Leave request " + (isApproval ? "approved" : "rejected") + " successfully!");
                    showLeaveApprovals(); // Refresh
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to " + (isApproval ? "approve" : "reject") + " leave request.");
                }
            }
        });
    }

    private void showScheduleMeeting() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Schedule Company Meeting");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        VBox meetingForm = createMeetingForm();

        content.getChildren().addAll(titleLabel, meetingForm);
        contentArea.getChildren().add(content);
    }

    private VBox createMeetingForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        Label formTitle = new Label("Schedule New Meeting");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField titleField = new TextField();
        titleField.setPromptText("Meeting title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Meeting description");
        descriptionArea.setPrefRowCount(3);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(1)); // Default to tomorrow
        // Prevent past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // Time pickers using ComboBox
        ComboBox<String> startTimeCombo = new ComboBox<>();
        ComboBox<String> endTimeCombo = new ComboBox<>();

        // Populate time options (8:00 AM to 6:00 PM)
        for (int hour = 8; hour <= 18; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                String timeStr = String.format("%02d:%02d", hour, minute);
                startTimeCombo.getItems().add(timeStr);
                endTimeCombo.getItems().add(timeStr);
            }
        }
        startTimeCombo.setValue("09:00");
        endTimeCombo.setValue("10:00");

        TextField locationField = new TextField();
        locationField.setPromptText("Meeting location");

        CheckBox allCompanyCheckBox = new CheckBox("All Company Meeting");

        ListView<Employee> participantsList = new ListView<>();
        participantsList.setPrefHeight(150);
        participantsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        List<Employee> availableParticipants = DataStore.getAllEmployees();
        ObservableList<Employee> participantData = FXCollections.observableArrayList(availableParticipants);
        participantsList.setItems(participantData);

        // Disable participant selection if all company is selected
        allCompanyCheckBox.setOnAction(e -> {
            if (allCompanyCheckBox.isSelected()) {
                participantsList.getSelectionModel().selectAll();
                participantsList.setDisable(true);
            } else {
                participantsList.getSelectionModel().clearSelection();
                participantsList.setDisable(false);
            }
        });

        Button scheduleMeetingBtn = new Button("Schedule Meeting");
        scheduleMeetingBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);

        scheduleMeetingBtn.setOnAction(e -> {
            if (titleField.getText().isEmpty() || datePicker.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Incomplete", "Please fill in required fields.");
                return;
            }

            // Validate that end time is after start time
            String startTime = startTimeCombo.getValue();
            String endTime = endTimeCombo.getValue();

            if (startTime != null && endTime != null) {
                LocalTime start = LocalTime.parse(startTime);
                LocalTime end = LocalTime.parse(endTime);

                if (!end.isAfter(start)) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Time", "End time must be after start time.");
                    return;
                }
            }

            Date meetingDate = java.sql.Date.valueOf(datePicker.getValue());
            List<String> participantIds;

            if (allCompanyCheckBox.isSelected()) {
                participantIds = DataStore.getAllEmployees().stream()
                        .map(Employee::getId)
                        .toList();
            } else {
                participantIds = participantsList.getSelectionModel().getSelectedItems()
                        .stream()
                        .map(Employee::getId)
                        .toList();
            }

            boolean success = DataStore.saveMeeting(
                    titleField.getText(),
                    descriptionArea.getText(),
                    meetingDate,
                    startTime,
                    endTime,
                    locationField.getText(),
                    manager.getId(),
                    participantIds
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Meeting scheduled successfully!");
                titleField.clear();
                descriptionArea.clear();
                datePicker.setValue(LocalDate.now().plusDays(1));
                startTimeCombo.setValue("09:00");
                endTimeCombo.setValue("10:00");
                locationField.clear();
                allCompanyCheckBox.setSelected(false);
                participantsList.getSelectionModel().clearSelection();
                participantsList.setDisable(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to schedule meeting.");
            }
        });

        formGrid.add(new Label("Title:"), 0, 0);
        formGrid.add(titleField, 1, 0);
        formGrid.add(new Label("Description:"), 0, 1);
        formGrid.add(descriptionArea, 1, 1);
        formGrid.add(new Label("Date:"), 0, 2);
        formGrid.add(datePicker, 1, 2);
        formGrid.add(new Label("Start Time:"), 0, 3);
        formGrid.add(startTimeCombo, 1, 3);
        formGrid.add(new Label("End Time:"), 0, 4);
        formGrid.add(endTimeCombo, 1, 4);
        formGrid.add(new Label("Location:"), 0, 5);
        formGrid.add(locationField, 1, 5);
        formGrid.add(allCompanyCheckBox, 1, 6);
        formGrid.add(new Label("Participants:"), 0, 7);
        formGrid.add(participantsList, 1, 7);
        formGrid.add(scheduleMeetingBtn, 1, 8);

        formBox.getChildren().addAll(formTitle, formGrid);
        return formBox;
    }

    private void showAnalytics() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Company Analytics Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        VBox performanceSummary = createPerformanceSummary();

        content.getChildren().addAll(titleLabel, performanceSummary);
        contentArea.getChildren().add(content);
    }

    private VBox createPerformanceSummary() {
        VBox summaryBox = new VBox(15);

        Label summaryTitle = new Label("Division Performance Summary");
        summaryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(15);
        summaryGrid.setPadding(new Insets(20));
        summaryGrid.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        String[] divisions = {"HR", "Marketing", "Sales", "IT", "Finance"};

        // Headers
        summaryGrid.add(new Label("Division"), 0, 0);
        summaryGrid.add(new Label("Avg KPI"), 1, 0);
        summaryGrid.add(new Label("Employees"), 2, 0);
        summaryGrid.add(new Label("At Risk"), 3, 0);
        summaryGrid.add(new Label("Avg Salary"), 4, 0);

        for (int i = 0; i < divisions.length; i++) {
            String division = divisions[i];

            List<Employee> divisionEmployees = DataStore.getEmployeesByDivision(division);
            List<KPI> divisionKPIs = DataStore.getKPIByDivision(division);

            double avgKPI = divisionKPIs.stream()
                    .mapToDouble(KPI::getScore)
                    .average()
                    .orElse(0.0);

            long atRiskCount = divisionEmployees.stream()
                    .filter(Employee::isLayoffRisk)
                    .count();

            double avgSalary = divisionEmployees.stream()
                    .filter(emp -> emp.getRole().equals("pegawai"))
                    .mapToDouble(Employee::calculateGajiBulanan)
                    .average()
                    .orElse(0.0);

            summaryGrid.add(new Label(division), 0, i + 1);
            summaryGrid.add(new Label(df.format(avgKPI) + "%"), 1, i + 1);
            summaryGrid.add(new Label(String.valueOf(divisionEmployees.size())), 2, i + 1);
            summaryGrid.add(new Label(String.valueOf(atRiskCount)), 3, i + 1);
            summaryGrid.add(new Label("Rp " + String.format("%,.0f", avgSalary)), 4, i + 1);
        }

        summaryBox.getChildren().addAll(summaryTitle, summaryGrid);
        return summaryBox;
    }

    // Common helper methods
    private void clockIn() {
        // Check if already clocked in today
        if (hasAttendanceToday()) {
            showAlert(Alert.AlertType.WARNING, "Already Clocked In", "You have already clocked in today.");
            return;
        }

        LocalTime now = LocalTime.now();
        String timeStr = String.format("%02d:%02d", now.getHour(), now.getMinute());

        boolean success = DataStore.saveAttendance(manager.getId(), new Date(), timeStr, null, "hadir");
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock In", "Successfully clocked in at " + timeStr);
            showDashboardContent(); // Refresh to update buttons
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to clock in.");
        }
    }

    private void clockOut() {
        // Check if already clocked out today
        if (hasCompletedAttendanceToday()) {
            showAlert(Alert.AlertType.WARNING, "Already Clocked Out", "You have already clocked out today.");
            return;
        }

        LocalTime now = LocalTime.now();
        String timeStr = String.format("%02d:%02d", now.getHour(), now.getMinute());

        boolean success = DataStore.updateAttendanceClockOut(manager.getId(), timeStr);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock Out", "Successfully clocked out at " + timeStr);
            showDashboardContent(); // Refresh to update buttons
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to clock out.");
        }
    }

    private void showLeaveRequestDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Leave");
        dialog.setHeaderText("Submit a new leave request");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        ComboBox<String> leaveTypeCombo = new ComboBox<>();
        leaveTypeCombo.getItems().addAll("Annual Leave", "Sick Leave", "Personal Leave", "Emergency Leave");
        leaveTypeCombo.setValue("Annual Leave");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Prevent past dates and weekends
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now()) ||
                        date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now()) ||
                        date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter reason for leave...");
        reasonArea.setPrefRowCount(3);

        content.getChildren().addAll(
                new Label("Leave Type:"), leaveTypeCombo,
                new Label("Start Date (No weekends):"), startDatePicker,
                new Label("End Date (No weekends):"), endDatePicker,
                new Label("Reason:"), reasonArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                    // Validate that dates are not weekends
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();

                    if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                            startDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
                            endDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                            endDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        showAlert(Alert.AlertType.WARNING, "Invalid Date", "Leave requests cannot be submitted for weekends.");
                        return;
                    }

                    Date startSqlDate = java.sql.Date.valueOf(startDate);
                    Date endSqlDate = java.sql.Date.valueOf(endDate);

                    boolean success = DataStore.saveLeaveRequest(manager.getId(), leaveTypeCombo.getValue(),
                            startSqlDate, endSqlDate, reasonArea.getText());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select start and end dates.");
                }
            }
        });
    }

    private void showScheduleMeetingDialog() {
        showScheduleMeeting(); // Navigate to the schedule meeting page
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
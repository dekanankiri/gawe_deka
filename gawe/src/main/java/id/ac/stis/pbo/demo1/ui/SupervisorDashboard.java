package id.ac.stis.pbo.demo1.ui;

import id.ac.stis.pbo.demo1.data.DataStore;
import id.ac.stis.pbo.demo1.models.Employee;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;

/**
 * Enhanced Supervisor Dashboard with monthly evaluation and fixes
 */
public class SupervisorDashboard extends Application {
    private final Employee supervisor;
    private StackPane contentArea;

    public SupervisorDashboard(Employee supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Create main layout components
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
        stage.setTitle("GAWE - Supervisor Dashboard - " + supervisor.getNama());
        stage.show();
    }

    private HBox createHeader(Stage stage) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        Label titleLabel = new Label("GAWE - Supervisor Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Welcome, " + supervisor.getNama() + " (" + supervisor.getDivisi() + ")");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        userLabel.setTextFill(Color.web("#7f8c8d"));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 25;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);
        logoutBtn.setOnAction(e -> stage.close());

        header.getChildren().addAll(titleLabel, spacer, userLabel, logoutBtn);
        return header;
    }

    private VBox createNavigation() {
        VBox navigation = new VBox(5);
        navigation.setPadding(new Insets(20));
        navigation.setPrefWidth(250);
        navigation.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 0 15 15 0;");

        Label navTitle = new Label("Navigation");
        navTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        navTitle.setTextFill(Color.web("#2c3e50"));

        Button[] navButtons = {
                createNavButton("📊 Dashboard", this::showDashboardContent),
                createNavButton("⏰ My Attendance", this::showMyAttendance),
                createNavButton("📅 My Meetings", this::showMyMeetings),
                createNavButton("🏖️ My Leave Requests", this::showMyLeaveRequests),
                createNavButton("👥 Team Management", this::showTeamManagementContent),
                createNavButton("⭐ Monthly Evaluation", this::showMonthlyEvaluationContent),
                createNavButton("📄 Upload Report", this::showUploadReportContent),
                createNavButton("📈 Performance Analytics", this::showPerformanceAnalyticsContent)
        };

        navigation.getChildren().add(navTitle);
        navigation.getChildren().add(new Separator());
        navigation.getChildren().addAll(navButtons);

        return navigation;
    }

    private Button createNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #2c3e50;
            -fx-padding: 12 15;
            -fx-background-radius: 8;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);

        button.setOnMouseEntered(e -> button.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 12 15;
            -fx-background-radius: 8;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """));

        button.setOnMouseExited(e -> button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #2c3e50;
            -fx-padding: 12 15;
            -fx-background-radius: 8;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """));

        button.setOnAction(e -> action.run());
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
        content.setAlignment(Pos.TOP_CENTER);

        // Dashboard title
        Label title = new Label("Supervisor Dashboard - " + supervisor.getDivisi() + " Division");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        // Quick actions for attendance
        HBox quickActions = createQuickActions();

        // Statistics cards
        HBox statsCards = createStatsCards();

        // Recent activities
        VBox recentActivities = createRecentActivitiesSection();

        content.getChildren().addAll(title, quickActions, statsCards, recentActivities);
        contentArea.getChildren().add(content);
    }

    private HBox createQuickActions() {
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER);

        // Check attendance status for today
        boolean alreadyClockedIn = hasAttendanceToday();
        boolean alreadyCompletedAttendance = hasCompletedAttendanceToday();

        Button clockInBtn = new Button("⏰ Clock In");
        clockInBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 15 25;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
        """);
        clockInBtn.setOnAction(e -> clockIn());
        clockInBtn.setDisable(alreadyClockedIn);

        Button clockOutBtn = new Button("🏃 Clock Out");
        clockOutBtn.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-padding: 15 25;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
        """);
        clockOutBtn.setOnAction(e -> clockOut());
        clockOutBtn.setDisable(!alreadyClockedIn || alreadyCompletedAttendance);

        Button requestLeaveBtn = new Button("🏖️ Request Leave");
        requestLeaveBtn.setStyle("""
            -fx-background-color: #f39c12;
            -fx-text-fill: white;
            -fx-padding: 15 25;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
        """);
        requestLeaveBtn.setOnAction(e -> showLeaveRequestDialog());

        actionsBox.getChildren().addAll(clockInBtn, clockOutBtn, requestLeaveBtn);
        return actionsBox;
    }

    private boolean hasAttendanceToday() {
        try {
            List<id.ac.stis.pbo.demo1.models.Attendance> todayAttendance = DataStore.getTodayAttendance(supervisor.getId());
            return !todayAttendance.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasCompletedAttendanceToday() {
        try {
            List<id.ac.stis.pbo.demo1.models.Attendance> todayAttendance = DataStore.getTodayAttendance(supervisor.getId());
            return !todayAttendance.isEmpty() &&
                    todayAttendance.get(0).getJamKeluar() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void clockIn() {
        if (hasAttendanceToday()) {
            showAlert(Alert.AlertType.WARNING, "Already Clocked In", "You have already clocked in today.");
            return;
        }

        LocalTime now = LocalTime.now();
        String timeStr = String.format("%02d:%02d", now.getHour(), now.getMinute());

        boolean success = DataStore.saveAttendance(supervisor.getId(), new Date(), timeStr, null, "hadir");
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock In", "Successfully clocked in at " + timeStr);
            showDashboardContent(); // Refresh to update buttons
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to clock in.");
        }
    }

    private void clockOut() {
        if (hasCompletedAttendanceToday()) {
            showAlert(Alert.AlertType.WARNING, "Already Clocked Out", "You have already clocked out today.");
            return;
        }

        LocalTime now = LocalTime.now();
        String timeStr = String.format("%02d:%02d", now.getHour(), now.getMinute());

        boolean success = DataStore.updateAttendanceClockOut(supervisor.getId(), timeStr);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock Out", "Successfully clocked out at " + timeStr);
            showDashboardContent(); // Refresh to update buttons
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to clock out.");
        }
    }

    private HBox createStatsCards() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);

        // Get team members count
        List<Employee> teamMembers = DataStore.getEmployeesByDivision(supervisor.getDivisi()).stream()
                .filter(emp -> emp.getRole().equals("pegawai"))
                .toList();

        // Team size card
        VBox teamSizeCard = createStatsCard("Team Size", String.valueOf(teamMembers.size()), "👥", "#3498db");

        // Average KPI card
        double avgKpi = teamMembers.stream()
                .mapToDouble(Employee::getKpiScore)
                .average()
                .orElse(0.0);
        VBox avgKpiCard = createStatsCard("Avg KPI", String.format("%.1f%%", avgKpi), "📊", "#2ecc71");

        // At risk employees
        long atRiskCount = teamMembers.stream()
                .filter(Employee::isLayoffRisk)
                .count();
        VBox atRiskCard = createStatsCard("At Risk", String.valueOf(atRiskCount), "⚠️", "#e74c3c");

        // Reports uploaded card
        VBox reportsCard = createStatsCard("My Leave Days", String.valueOf(supervisor.getSisaCuti()), "🏖️", "#9b59b6");

        statsContainer.getChildren().addAll(teamSizeCard, avgKpiCard, atRiskCard, reportsCard);
        return statsContainer;
    }

    private VBox createStatsCard(String title, String value, String icon, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(200, 120);
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
            -fx-border-color: %s;
            -fx-border-width: 0 0 4 0;
        """, color));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createRecentActivitiesSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20));
        section.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Label sectionTitle = new Label("Recent Activities");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#2c3e50"));

        ListView<String> activitiesList = new ListView<>();
        activitiesList.setPrefHeight(200);
        activitiesList.setStyle("-fx-background-radius: 10;");

        ObservableList<String> activities = FXCollections.observableArrayList(
                "📊 Dashboard accessed - just now",
                "📄 Ready to upload monthly report for " + supervisor.getDivisi(),
                "⭐ Team evaluations pending",
                "👥 Managing " + supervisor.getDivisi() + " team"
        );
        activitiesList.setItems(activities);

        section.getChildren().addAll(sectionTitle, activitiesList);
        return section;
    }

    // Personal Features
    private void showMyAttendance() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("My Attendance");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        TableView<id.ac.stis.pbo.demo1.models.Attendance> attendanceTable = createMyAttendanceTable();

        content.getChildren().addAll(title, attendanceTable);
        contentArea.getChildren().add(content);
    }

    private TableView<id.ac.stis.pbo.demo1.models.Attendance> createMyAttendanceTable() {
        TableView<id.ac.stis.pbo.demo1.models.Attendance> table = new TableView<>();
        table.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getTanggal())));

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> clockInCol = new TableColumn<>("Clock In");
        clockInCol.setCellValueFactory(new PropertyValueFactory<>("jamMasuk"));

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> clockOutCol = new TableColumn<>("Clock Out");
        clockOutCol.setCellValueFactory(new PropertyValueFactory<>("jamKeluar"));

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(dateCol, clockInCol, clockOutCol, statusCol);

        List<id.ac.stis.pbo.demo1.models.Attendance> myAttendance = DataStore.getAttendanceByEmployee(supervisor.getId());
        table.setItems(FXCollections.observableArrayList(myAttendance));
        table.setPrefHeight(400);

        return table;
    }

    private void showMyMeetings() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("My Meetings");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        TableView<id.ac.stis.pbo.demo1.models.Meeting> meetingsTable = createMyMeetingsTable();

        content.getChildren().addAll(title, meetingsTable);
        contentArea.getChildren().add(content);
    }

    private TableView<id.ac.stis.pbo.demo1.models.Meeting> createMyMeetingsTable() {
        TableView<id.ac.stis.pbo.demo1.models.Meeting> table = new TableView<>();
        table.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        TableColumn<id.ac.stis.pbo.demo1.models.Meeting, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<id.ac.stis.pbo.demo1.models.Meeting, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getTanggal())));

        TableColumn<id.ac.stis.pbo.demo1.models.Meeting, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getWaktuMulai() + " - " + cellData.getValue().getWaktuSelesai()));

        TableColumn<id.ac.stis.pbo.demo1.models.Meeting, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));

        table.getColumns().addAll(titleCol, dateCol, timeCol, locationCol);

        List<id.ac.stis.pbo.demo1.models.Meeting> myMeetings = DataStore.getMeetingsByEmployee(supervisor.getId());
        table.setItems(FXCollections.observableArrayList(myMeetings));
        table.setPrefHeight(400);

        return table;
    }

    private void showMyLeaveRequests() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("My Leave Requests");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Button newRequestBtn = new Button("➕ New Leave Request");
        newRequestBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);
        newRequestBtn.setOnAction(e -> showLeaveRequestDialog());

        TableView<id.ac.stis.pbo.demo1.models.LeaveRequest> leaveTable = createMyLeaveRequestsTable();

        content.getChildren().addAll(title, newRequestBtn, leaveTable);
        contentArea.getChildren().add(content);
    }

    private TableView<id.ac.stis.pbo.demo1.models.LeaveRequest> createMyLeaveRequestsTable() {
        TableView<id.ac.stis.pbo.demo1.models.LeaveRequest> table = new TableView<>();
        table.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getStartDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getEndDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("totalDays"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(typeCol, startDateCol, endDateCol, daysCol, statusCol);

        List<id.ac.stis.pbo.demo1.models.LeaveRequest> myLeaveRequests = DataStore.getLeaveRequestsByEmployee(supervisor.getId());
        table.setItems(FXCollections.observableArrayList(myLeaveRequests));
        table.setPrefHeight(400);

        return table;
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

                    boolean success = DataStore.saveLeaveRequest(supervisor.getId(), leaveTypeCombo.getValue(),
                            startSqlDate, endSqlDate, reasonArea.getText());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully!");
                        showMyLeaveRequests(); // Refresh
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select start and end dates.");
                }
            }
        });
    }

    private void showTeamManagementContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Team Management - " + supervisor.getDivisi() + " Division");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // Team members table
        TableView<Employee> teamTable = createTeamTable();

        content.getChildren().addAll(title, teamTable);
        contentArea.getChildren().add(content);
    }

    private TableView<Employee> createTeamTable() {
        TableView<Employee> table = new TableView<>();
        table.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        // Create columns
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        nameCol.setPrefWidth(200);

        TableColumn<Employee, String> positionCol = new TableColumn<>("Position");
        positionCol.setCellValueFactory(new PropertyValueFactory<>("jabatan"));
        positionCol.setPrefWidth(180);

        TableColumn<Employee, String> kpiCol = new TableColumn<>("KPI Score");
        kpiCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getKpiScore())));
        kpiCol.setPrefWidth(100);

        TableColumn<Employee, String> ratingCol = new TableColumn<>("Supervisor Rating");
        ratingCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getSupervisorRating())));
        ratingCol.setPrefWidth(150);

        TableColumn<Employee, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue();
            String status = emp.isLayoffRisk() ? "⚠️ At Risk" : "✅ Good Standing";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(nameCol, positionCol, kpiCol, ratingCol, statusCol);

        // Load team members (employees in the same division)
        List<Employee> teamMembers = DataStore.getAllEmployees().stream()
                .filter(emp -> emp.getDivisi().equals(supervisor.getDivisi()) &&
                        emp.getRole().equals("pegawai"))
                .toList();

        table.setItems(FXCollections.observableArrayList(teamMembers));
        table.setPrefHeight(400);

        return table;
    }

    private void showMonthlyEvaluationContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Monthly Employee Evaluation");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // Monthly evaluation form
        VBox evaluationForm = createMonthlyEvaluationForm();

        content.getChildren().addAll(title, evaluationForm);
        contentArea.getChildren().add(content);
    }

    private VBox createMonthlyEvaluationForm() {
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(700);
        form.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Label formTitle = new Label("Monthly Performance Evaluation");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#2c3e50"));

        // Employee and month selection
        GridPane selectionGrid = new GridPane();
        selectionGrid.setHgap(20);
        selectionGrid.setVgap(15);
        selectionGrid.setAlignment(Pos.CENTER);

        ComboBox<Employee> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.setPrefWidth(300);

        List<Employee> teamMembers = DataStore.getAllEmployees().stream()
                .filter(emp -> emp.getDivisi().equals(supervisor.getDivisi()) &&
                        emp.getRole().equals("pegawai"))
                .toList();
        employeeCombo.setItems(FXCollections.observableArrayList(teamMembers));

        // Month and Year selection
        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        monthCombo.setValue("January");
        monthCombo.setPrefWidth(150);

        ComboBox<Integer> yearCombo = new ComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            yearCombo.getItems().add(year);
        }
        yearCombo.setValue(LocalDate.now().getYear());
        yearCombo.setPrefWidth(100);

        selectionGrid.add(new Label("Employee:"), 0, 0);
        selectionGrid.add(employeeCombo, 1, 0);
        selectionGrid.add(new Label("Month:"), 0, 1);
        selectionGrid.add(monthCombo, 1, 1);
        selectionGrid.add(new Label("Year:"), 0, 2);
        selectionGrid.add(yearCombo, 1, 2);

        // Evaluation criteria
        GridPane criteriaGrid = new GridPane();
        criteriaGrid.setHgap(20);
        criteriaGrid.setVgap(15);
        criteriaGrid.setAlignment(Pos.CENTER);

        Label punctualityLabel = new Label("Punctuality Score (0-100):");
        Slider punctualitySlider = new Slider(0, 100, 75);
        punctualitySlider.setShowTickLabels(true);
        punctualitySlider.setShowTickMarks(true);
        punctualitySlider.setMajorTickUnit(25);
        Label punctualityValue = new Label("75");
        punctualitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                punctualityValue.setText(String.format("%.0f", newVal.doubleValue())));

        Label attendanceLabel = new Label("Attendance Score (0-100):");
        Slider attendanceSlider = new Slider(0, 100, 80);
        attendanceSlider.setShowTickLabels(true);
        attendanceSlider.setShowTickMarks(true);
        attendanceSlider.setMajorTickUnit(25);
        Label attendanceValue = new Label("80");
        attendanceSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                attendanceValue.setText(String.format("%.0f", newVal.doubleValue())));

        Label productivityLabel = new Label("Productivity Score (0-100):");
        Slider productivitySlider = new Slider(0, 100, 75);
        productivitySlider.setShowTickLabels(true);
        productivitySlider.setShowTickMarks(true);
        productivitySlider.setMajorTickUnit(25);
        Label productivityValue = new Label("75");
        productivitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                productivityValue.setText(String.format("%.0f", newVal.doubleValue())));

        Label overallLabel = new Label("Overall Rating (0-100):");
        Slider overallSlider = new Slider(0, 100, 75);
        overallSlider.setShowTickLabels(true);
        overallSlider.setShowTickMarks(true);
        overallSlider.setMajorTickUnit(25);
        Label overallValue = new Label("75");
        overallSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                overallValue.setText(String.format("%.0f", newVal.doubleValue())));

        criteriaGrid.add(punctualityLabel, 0, 0);
        criteriaGrid.add(punctualitySlider, 1, 0);
        criteriaGrid.add(punctualityValue, 2, 0);
        criteriaGrid.add(attendanceLabel, 0, 1);
        criteriaGrid.add(attendanceSlider, 1, 1);
        criteriaGrid.add(attendanceValue, 2, 1);
        criteriaGrid.add(productivityLabel, 0, 2);
        criteriaGrid.add(productivitySlider, 1, 2);
        criteriaGrid.add(productivityValue, 2, 2);
        criteriaGrid.add(overallLabel, 0, 3);
        criteriaGrid.add(overallSlider, 1, 3);
        criteriaGrid.add(overallValue, 2, 3);

        // Comments
        Label commentsLabel = new Label("Monthly Comments:");
        TextArea commentsArea = new TextArea();
        commentsArea.setPrefRowCount(4);
        commentsArea.setPromptText("Enter monthly evaluation comments and feedback...");

        // Submit button
        Button submitBtn = new Button("Submit Monthly Evaluation");
        submitBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 12 30;
            -fx-background-radius: 25;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);

        submitBtn.setOnAction(e -> {
            Employee selectedEmployee = employeeCombo.getValue();
            String selectedMonth = monthCombo.getValue();
            Integer selectedYear = yearCombo.getValue();

            if (selectedEmployee != null && selectedMonth != null && selectedYear != null) {
                try {
                    // Check if evaluation already exists for this month
                    boolean alreadyExists = DataStore.hasMonthlyEvaluation(
                            selectedEmployee.getId(),
                            monthCombo.getSelectionModel().getSelectedIndex() + 1,
                            selectedYear
                    );

                    if (alreadyExists) {
                        showAlert(Alert.AlertType.WARNING, "Evaluation Exists",
                                "Monthly evaluation for " + selectedEmployee.getNama() + " in " +
                                        selectedMonth + " " + selectedYear + " already exists!");
                        return;
                    }

                    boolean success = DataStore.saveMonthlyEmployeeEvaluation(
                            selectedEmployee.getId(),
                            supervisor.getId(),
                            monthCombo.getSelectionModel().getSelectedIndex() + 1,
                            selectedYear,
                            punctualitySlider.getValue(),
                            attendanceSlider.getValue(),
                            productivitySlider.getValue(),
                            overallSlider.getValue(),
                            commentsArea.getText()
                    );

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Monthly evaluation for " + selectedEmployee.getNama() +
                                        " (" + selectedMonth + " " + selectedYear + ") submitted successfully!");
                        // Reset form
                        employeeCombo.setValue(null);
                        punctualitySlider.setValue(75);
                        attendanceSlider.setValue(80);
                        productivitySlider.setValue(75);
                        overallSlider.setValue(75);
                        commentsArea.clear();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to submit monthly evaluation. Please try again.");
                    }
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "An error occurred: " + ex.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning",
                        "Please select an employee, month, and year.");
            }
        });

        form.getChildren().addAll(formTitle, selectionGrid, criteriaGrid,
                commentsLabel, commentsArea, submitBtn);
        return form;
    }

    private void showUploadReportContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Upload Monthly Report");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // Upload form
        VBox uploadForm = createUploadForm();

        content.getChildren().addAll(title, uploadForm);
        contentArea.getChildren().add(content);
    }

    private VBox createUploadForm() {
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(500);
        form.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Label formTitle = new Label("Monthly Division Report");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#2c3e50"));

        // Month and year selection
        HBox dateSelection = new HBox(10);
        dateSelection.setAlignment(Pos.CENTER);

        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        monthCombo.setValue("January");

        ComboBox<Integer> yearCombo = new ComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            yearCombo.getItems().add(year);
        }
        yearCombo.setValue(LocalDate.now().getYear());

        dateSelection.getChildren().addAll(new Label("Month:"), monthCombo,
                new Label("Year:"), yearCombo);

        // File selection
        HBox fileSelection = new HBox(10);
        fileSelection.setAlignment(Pos.CENTER);

        TextField filePathField = new TextField();
        filePathField.setPromptText("Select PDF file...");
        filePathField.setPrefWidth(300);
        filePathField.setEditable(false);

        Button browseBtn = new Button("Browse");
        browseBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);

        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select PDF Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        fileSelection.getChildren().addAll(filePathField, browseBtn);

        // Upload button
        Button uploadBtn = new Button("Upload Report");
        uploadBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 12 30;
            -fx-background-radius: 25;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);

        uploadBtn.setOnAction(e -> {
            if (!filePathField.getText().isEmpty()) {
                int monthIndex = monthCombo.getSelectionModel().getSelectedIndex() + 1;
                int year = yearCombo.getValue();

                boolean success = DataStore.saveReport(
                        supervisor.getId(),
                        supervisor.getDivisi(),
                        monthIndex,
                        year,
                        filePathField.getText()
                );

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Report uploaded successfully!");
                    filePathField.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to upload report. Please try again.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning",
                        "Please select a PDF file to upload.");
            }
        });

        form.getChildren().addAll(formTitle, dateSelection, fileSelection, uploadBtn);
        return form;
    }

    private void showPerformanceAnalyticsContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Performance Analytics - " + supervisor.getDivisi());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // Analytics dashboard
        VBox analyticsSection = createAnalyticsSection();

        content.getChildren().addAll(title, analyticsSection);
        contentArea.getChildren().add(content);
    }

    private VBox createAnalyticsSection() {
        VBox section = new VBox(20);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20));
        section.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Label sectionTitle = new Label("Division Performance Overview");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#2c3e50"));

        // Performance metrics
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(30);
        metricsGrid.setVgap(20);
        metricsGrid.setAlignment(Pos.CENTER);

        // Calculate metrics from team data
        List<Employee> teamMembers = DataStore.getAllEmployees().stream()
                .filter(emp -> emp.getDivisi().equals(supervisor.getDivisi()) &&
                        emp.getRole().equals("pegawai"))
                .toList();

        double avgKPI = teamMembers.stream()
                .mapToDouble(Employee::getKpiScore)
                .average()
                .orElse(0.0);

        double avgRating = teamMembers.stream()
                .mapToDouble(Employee::getSupervisorRating)
                .average()
                .orElse(0.0);

        long atRiskCount = teamMembers.stream()
                .filter(Employee::isLayoffRisk)
                .count();

        // Create metric cards
        VBox avgKpiCard = createMetricCard("Average KPI", String.format("%.1f%%", avgKPI), "#3498db");
        VBox avgRatingCard = createMetricCard("Average Rating", String.format("%.1f", avgRating), "#2ecc71");
        VBox atRiskCard = createMetricCard("At Risk Employees", String.valueOf(atRiskCount), "#e74c3c");
        VBox teamSizeCard = createMetricCard("Team Size", String.valueOf(teamMembers.size()), "#9b59b6");

        metricsGrid.add(avgKpiCard, 0, 0);
        metricsGrid.add(avgRatingCard, 1, 0);
        metricsGrid.add(atRiskCard, 0, 1);
        metricsGrid.add(teamSizeCard, 1, 1);

        section.getChildren().addAll(sectionTitle, metricsGrid);
        return section;
    }

    private VBox createMetricCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(180, 100);
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);
        """, color));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // Sample supervisor for testing
        Employee supervisor = new Employee("SUP001", "Alice Supervisor", "password123",
                "supervisor", "HR", "HR Supervisor", new java.util.Date());

        SupervisorDashboard dashboard = new SupervisorDashboard(supervisor);
        Application.launch(SupervisorDashboard.class, args);
    }
}
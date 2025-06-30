package id.ac.stis.pbo.demo1.ui;

import id.ac.stis.pbo.demo1.data.DataStoreFactory;
import id.ac.stis.pbo.demo1.data.MySQLDataStore;
import id.ac.stis.pbo.demo1.data.MySQLDataStore.MonthlyEvaluation;
import id.ac.stis.pbo.demo1.models.Employee;
import id.ac.stis.pbo.demo1.models.LeaveRequest;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
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
import java.util.stream.Collectors;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Enhanced Supervisor Dashboard with comprehensive features, meeting creation, and proper logout
 */
public class SupervisorDashboard extends Application {
    private final Employee supervisor;
    private final MySQLDataStore dataStore;
    private StackPane contentArea;
    private DecimalFormat df = new DecimalFormat("#.##");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public SupervisorDashboard(Employee supervisor) {
        this.supervisor = supervisor;
        this.dataStore = DataStoreFactory.getMySQLDataStore();
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

        Scene scene = new Scene(root, 1600, 900);
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
        logoutBtn.setOnAction(e -> {
            stage.close();
            new id.ac.stis.pbo.demo1.HelloApplication().start(new Stage());
        });

        header.getChildren().addAll(titleLabel, spacer, userLabel, logoutBtn);
        return header;
    }

    private VBox createNavigation() {
        VBox navigation = new VBox(5);
        navigation.setPadding(new Insets(20));
        navigation.setPrefWidth(280);
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
                createNavButton("📈 Performance Analytics", this::showPerformanceAnalyticsContent),
                createNavButton("💰 Salary Management", this::showSalaryManagementContent),
                createNavButton("📋 All History", this::showAllHistoryContent)
        };

        navigation.getChildren().add(navTitle);
        navigation.getChildren().add(new Separator());
        navigation.getChildren().addAll(navButtons);

        return navigation;
    }

    private Button createNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefWidth(240);
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
            List<id.ac.stis.pbo.demo1.models.Attendance> todayAttendance = dataStore.getTodayAttendance(supervisor.getId());
            return !todayAttendance.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasCompletedAttendanceToday() {
        try {
            List<id.ac.stis.pbo.demo1.models.Attendance> todayAttendance = dataStore.getTodayAttendance(supervisor.getId());
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

        boolean success = dataStore.saveAttendance(supervisor.getId(), new Date(), timeStr, null, "hadir");
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

        boolean success = dataStore.updateAttendanceClockOut(supervisor.getId(), timeStr);
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
        List<Employee> teamMembers = dataStore.getEmployeesByDivision(supervisor.getDivisi()).stream()
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

        // Leave days card
        VBox leaveCard = createStatsCard("My Leave Days", String.valueOf(supervisor.getSisaCuti()), "🏖️", "#9b59b6");

        statsContainer.getChildren().addAll(teamSizeCard, avgKpiCard, atRiskCard, leaveCard);
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
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getTanggal())));

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> clockInCol = new TableColumn<>("Clock In");
        clockInCol.setCellValueFactory(new PropertyValueFactory<>("jamMasuk"));

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> clockOutCol = new TableColumn<>("Clock Out");
        clockOutCol.setCellValueFactory(new PropertyValueFactory<>("jamKeluar"));

        TableColumn<id.ac.stis.pbo.demo1.models.Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(dateCol, clockInCol, clockOutCol, statusCol);

        List<id.ac.stis.pbo.demo1.models.Attendance> myAttendance = dataStore.getAttendanceByEmployee(supervisor.getId());
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

        Button newMeetingBtn = new Button("➕ Schedule New Meeting");
        newMeetingBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);
        newMeetingBtn.setOnAction(e -> showNewMeetingDialog());

        TableView<id.ac.stis.pbo.demo1.models.Meeting> meetingsTable = createMyMeetingsTable();

        content.getChildren().addAll(title, newMeetingBtn, meetingsTable);
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
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getTanggal())));

        TableColumn<id.ac.stis.pbo.demo1.models.Meeting, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getWaktuMulai() + " - " + cellData.getValue().getWaktuSelesai()));

        TableColumn<id.ac.stis.pbo.demo1.models.Meeting, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));

        table.getColumns().addAll(titleCol, dateCol, timeCol, locationCol);

        List<id.ac.stis.pbo.demo1.models.Meeting> myMeetings = dataStore.getMeetingsByEmployee(supervisor.getId());
        table.setItems(FXCollections.observableArrayList(myMeetings));
        table.setPrefHeight(400);

        return table;
    }

    private void showNewMeetingDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Schedule New Meeting");
        dialog.setHeaderText("Create a new meeting for your team");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Meeting title...");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Meeting description...");
        descriptionArea.setPrefRowCount(3);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(1));

        TextField startTimeField = new TextField();
        startTimeField.setPromptText("Start time (HH:MM)");

        TextField endTimeField = new TextField();
        endTimeField.setPromptText("End time (HH:MM)");

        TextField locationField = new TextField();
        locationField.setPromptText("Meeting location...");

        // Team member selection
        ListView<Employee> teamMembersList = new ListView<>();
        teamMembersList.setPrefHeight(150);
        teamMembersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        List<Employee> teamMembers = dataStore.getEmployeesByDivision(supervisor.getDivisi());
        teamMembersList.setItems(FXCollections.observableArrayList(teamMembers));

        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea,
                new Label("Date:"), datePicker,
                new Label("Start Time:"), startTimeField,
                new Label("End Time:"), endTimeField,
                new Label("Location:"), locationField,
                new Label("Team Members (select multiple):"), teamMembersList
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (!titleField.getText().isEmpty() && datePicker.getValue() != null) {
                    Date meetingDate = java.sql.Date.valueOf(datePicker.getValue());
                    
                    List<String> selectedParticipants = teamMembersList.getSelectionModel().getSelectedItems()
                            .stream()
                            .map(Employee::getId)
                            .collect(Collectors.toList());

                    // Add supervisor as organizer
                    selectedParticipants.add(supervisor.getId());

                    boolean success = dataStore.saveMeeting(
                            titleField.getText(),
                            descriptionArea.getText(),
                            meetingDate,
                            startTimeField.getText(),
                            endTimeField.getText(),
                            locationField.getText(),
                            supervisor.getId(),
                            selectedParticipants
                    );

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Meeting scheduled successfully!");
                        showMyMeetings();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to schedule meeting.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please fill in required fields.");
                }
            }
        });
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
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getStartDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getEndDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("totalDays"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> notesCol = new TableColumn<>("Approval Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("approverNotes"));

        table.getColumns().addAll(typeCol, startDateCol, endDateCol, daysCol, statusCol, notesCol);

        List<id.ac.stis.pbo.demo1.models.LeaveRequest> myLeaveRequests = dataStore.getLeaveRequestsByEmployee(supervisor.getId());
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

                    boolean success = dataStore.saveLeaveRequest(supervisor.getId(), leaveTypeCombo.getValue(),
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
        List<Employee> teamMembers = dataStore.getAllEmployees().stream()
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

        TabPane tabPane = new TabPane();
        tabPane.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Tab evaluationTab = new Tab("New Evaluation", createMonthlyEvaluationForm());
        Tab historyTab = new Tab("Evaluation History", createMonthlyEvaluationHistoryTable());

        tabPane.getTabs().addAll(evaluationTab, historyTab);

        content.getChildren().addAll(title, tabPane);
        contentArea.getChildren().add(content);
    }

    private VBox createMonthlyEvaluationForm() {
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(700);

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

        List<Employee> teamMembers = dataStore.getAllEmployees().stream()
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
                    boolean alreadyExists = dataStore.hasMonthlyEvaluation(
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

                    boolean success = dataStore.saveMonthlyEmployeeEvaluation(
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

    private TableView<MySQLDataStore.MonthlyEvaluation> createMonthlyEvaluationHistoryTable() {
        TableView<MySQLDataStore.MonthlyEvaluation> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<MySQLDataStore.MonthlyEvaluation, String> employeeCol = new TableColumn<>("Employee");
        employeeCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        TableColumn<MySQLDataStore.MonthlyEvaluation, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData -> {
            String[] months = {"", "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            return new javafx.beans.property.SimpleStringProperty(months[cellData.getValue().getMonth()]);
        });

        TableColumn<MySQLDataStore.MonthlyEvaluation, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<MySQLDataStore.MonthlyEvaluation, String> overallCol = new TableColumn<>("Overall Rating");
        overallCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getOverallRating()) + "%"));

        TableColumn<MySQLDataStore.MonthlyEvaluation, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getEvaluationDate())));

        table.getColumns().addAll(employeeCol, monthCol, yearCol, overallCol, dateCol);

        // Get monthly evaluations by this supervisor
        List<MySQLDataStore.MonthlyEvaluation> monthlyEvaluations = dataStore.getMonthlyEvaluationsBySupervisor(supervisor.getId());
        table.setItems(FXCollections.observableArrayList(monthlyEvaluations));

        return table;
    }

    private void showUploadReportContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Upload Monthly Report");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        TabPane tabPane = new TabPane();
        tabPane.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Tab uploadTab = new Tab("Upload Report", createUploadForm());
        Tab historyTab = new Tab("Report History", createMyReportHistoryTable());

        tabPane.getTabs().addAll(uploadTab, historyTab);

        content.getChildren().addAll(title, tabPane);
        contentArea.getChildren().add(content);
    }

    private VBox createUploadForm() {
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(500);

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

                boolean success = dataStore.saveReport(
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

    private TableView<id.ac.stis.pbo.demo1.models.Report> createMyReportHistoryTable() {
        TableView<id.ac.stis.pbo.demo1.models.Report> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<id.ac.stis.pbo.demo1.models.Report, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<id.ac.stis.pbo.demo1.models.Report, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<id.ac.stis.pbo.demo1.models.Report, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<id.ac.stis.pbo.demo1.models.Report, String> uploadDateCol = new TableColumn<>("Upload Date");
        uploadDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getUploadDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.Report, String> notesCol = new TableColumn<>("Manager Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("managerNotes"));

        table.getColumns().addAll(monthCol, yearCol, statusCol, uploadDateCol, notesCol);

        List<id.ac.stis.pbo.demo1.models.Report> myReports = dataStore.getReportsByDivision(supervisor.getDivisi())
                .stream()
                .filter(report -> report.getSupervisorId().equals(supervisor.getId()))
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(myReports));

        return table;
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

    private void showSalaryManagementContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Salary Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        TabPane tabPane = new TabPane();
        tabPane.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Tab mySalaryTab = new Tab("My Salary", createMySalaryView());
        Tab teamSalariesTab = new Tab("Team Salaries", createTeamSalariesTable());

        tabPane.getTabs().addAll(mySalaryTab, teamSalariesTab);

        content.getChildren().addAll(title, tabPane);
        contentArea.getChildren().add(content);
    }

    private VBox createMySalaryView() {
        VBox salaryView = new VBox(20);
        salaryView.setPadding(new Insets(20));

        // Current salary breakdown
        VBox salaryBreakdown = createSalaryBreakdown();

        // Salary history table
        TableView<id.ac.stis.pbo.demo1.models.SalaryHistory> salaryTable = createMySalaryHistoryTable();

        salaryView.getChildren().addAll(salaryBreakdown, salaryTable);
        return salaryView;
    }

    private VBox createSalaryBreakdown() {
        VBox breakdownBox = new VBox(15);
        breakdownBox.setPadding(new Insets(20));
        breakdownBox.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-background-radius: 10;
            -fx-border-color: #dee2e6;
            -fx-border-radius: 10;
        """);

        Label breakdownTitle = new Label("Current Monthly Salary Breakdown");
        breakdownTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane salaryGrid = new GridPane();
        salaryGrid.setHgap(20);
        salaryGrid.setVgap(15);

        double baseSalary = supervisor.getGajiPokok();
        double kpiBonus = 0;
        double supervisorBonus = 0;
        double penalty = 0;

        // Calculate KPI bonus
        if (supervisor.getKpiScore() >= 90) {
            kpiBonus = baseSalary * 0.20;
        } else if (supervisor.getKpiScore() >= 80) {
            kpiBonus = baseSalary * 0.15;
        } else if (supervisor.getKpiScore() >= 70) {
            kpiBonus = baseSalary * 0.10;
        } else if (supervisor.getKpiScore() >= 60) {
            kpiBonus = baseSalary * 0.05;
        }

        // Calculate supervisor bonus
        if (supervisor.getSupervisorRating() >= 90) {
            supervisorBonus = baseSalary * 0.15;
        } else if (supervisor.getSupervisorRating() >= 80) {
            supervisorBonus = baseSalary * 0.10;
        } else if (supervisor.getSupervisorRating() >= 70) {
            supervisorBonus = baseSalary * 0.05;
        }

        // Calculate penalty
        if (supervisor.getKpiScore() < 60 || supervisor.getSupervisorRating() < 60) {
            penalty = baseSalary * 0.10;
        }

        double totalSalary = supervisor.calculateGajiBulanan();

        salaryGrid.add(new Label("Base Salary:"), 0, 0);
        salaryGrid.add(new Label("Rp " + String.format("%,.0f", baseSalary)), 1, 0);

        salaryGrid.add(new Label("KPI Bonus:"), 0, 1);
        salaryGrid.add(new Label("Rp " + String.format("%,.0f", kpiBonus)), 1, 1);

        salaryGrid.add(new Label("Supervisor Bonus:"), 0, 2);
        salaryGrid.add(new Label("Rp " + String.format("%,.0f", supervisorBonus)), 1, 2);

        if (penalty > 0) {
            salaryGrid.add(new Label("Performance Penalty:"), 0, 3);
            Label penaltyLabel = new Label("-Rp " + String.format("%,.0f", penalty));
            penaltyLabel.setTextFill(Color.RED);
            salaryGrid.add(penaltyLabel, 1, 3);
        }

        salaryGrid.add(new Separator(), 0, 4);
        salaryGrid.add(new Separator(), 1, 4);

        salaryGrid.add(new Label("Total Monthly Salary:"), 0, 5);
        Label totalLabel = new Label("Rp " + String.format("%,.0f", totalSalary));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.web("#27ae60"));
        salaryGrid.add(totalLabel, 1, 5);

        breakdownBox.getChildren().addAll(breakdownTitle, salaryGrid);
        return breakdownBox;
    }

    private TableView<id.ac.stis.pbo.demo1.models.SalaryHistory> createMySalaryHistoryTable() {
        TableView<id.ac.stis.pbo.demo1.models.SalaryHistory> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> baseCol = new TableColumn<>("Base Salary");
        baseCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getBaseSalary())));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> totalCol = new TableColumn<>("Total Salary");
        totalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getTotalSalary())));

        table.getColumns().addAll(monthCol, yearCol, baseCol, totalCol);

        List<id.ac.stis.pbo.demo1.models.SalaryHistory> mySalaryHistory = DataStore.getSalaryHistoryByEmployee(supervisor.getId());
        table.setItems(FXCollections.observableArrayList(mySalaryHistory));

        return table;
    }

    private TableView<id.ac.stis.pbo.demo1.models.SalaryHistory> createTeamSalariesTable() {
        TableView<id.ac.stis.pbo.demo1.models.SalaryHistory> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> employeeCol = new TableColumn<>("Employee");
        employeeCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> baseCol = new TableColumn<>("Base Salary");
        baseCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getBaseSalary())));

        TableColumn<id.ac.stis.pbo.demo1.models.SalaryHistory, String> totalCol = new TableColumn<>("Total Salary");
        totalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getTotalSalary())));

        table.getColumns().addAll(employeeCol, monthCol, yearCol, baseCol, totalCol);

        // Get salary history for team members only
        List<Employee> teamMembers = dataStore.getEmployeesByDivision(supervisor.getDivisi());
        List<String> teamMemberIds = teamMembers.stream().map(Employee::getId).collect(Collectors.toList());

        List<id.ac.stis.pbo.demo1.models.SalaryHistory> teamSalaryHistory = dataStore.getAllSalaryHistory()
                .stream()
                .filter(salary -> teamMemberIds.contains(salary.getEmployeeId()))
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(teamSalaryHistory));

        return table;
    }

    private void showAllHistoryContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("All History & Submissions");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        TabPane tabPane = new TabPane();
        tabPane.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);

        Tab leaveHistoryTab = new Tab("Leave Requests", createAllLeaveRequestsTable());
        Tab reportHistoryTab = new Tab("My Reports", createMyReportHistoryTable());
        Tab evaluationHistoryTab = new Tab("Monthly Evaluations", createMonthlyEvaluationHistoryTable());

        tabPane.getTabs().addAll(leaveHistoryTab, reportHistoryTab, evaluationHistoryTab);

        content.getChildren().addAll(title, tabPane);
        contentArea.getChildren().add(content);
    }

    private TableView<id.ac.stis.pbo.demo1.models.LeaveRequest> createAllLeaveRequestsTable() {
        TableView<id.ac.stis.pbo.demo1.models.LeaveRequest> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> employeeCol = new TableColumn<>("Employee");
        employeeCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getStartDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getEndDate())));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("totalDays"));

        TableColumn<id.ac.stis.pbo.demo1.models.LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(employeeCol, typeCol, startDateCol, endDateCol, daysCol, statusCol);

        // Get leave requests for team members and supervisor
        List<Employee> teamMembers = dataStore.getEmployeesByDivision(supervisor.getDivisi());
        List<String> teamMemberIds = teamMembers.stream().map(Employee::getId).collect(Collectors.toList());

        List<id.ac.stis.pbo.demo1.models.LeaveRequest> teamLeaveRequests = dataStore.getAllLeaveRequests()
                .stream()
                .filter(leave -> teamMemberIds.contains(leave.getEmployeeId()))
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(teamLeaveRequests));

        return table;
    }

    private void showLeaveApprovalDialog(LeaveRequest request, boolean isApproval) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isApproval ? "Approve Leave Request" : "Reject Leave Request");
        dialog.setHeaderText("Leave request from " + request.getEmployeeId());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Employee requestingEmployee = dataStore.getEmployeeById(request.getEmployeeId());
        String employeeName = requestingEmployee != null ? requestingEmployee.getNama() : "Unknown";

        Label infoLabel = new Label(String.format(
                "Employee: %s\nType: %s\nDates: %s to %s\nDays: %d\nReason: %s",
                employeeName,
                request.getLeaveType(),
                sdf.format(request.getStartDate()),
                sdf.format(request.getEndDate()),
                request.getTotalDays(),
                request.getReason()
        ));

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter approval/rejection notes...");
        notesArea.setPrefRowCount(3);

        content.getChildren().addAll(
                new Label("Request Details:"), infoLabel,
                new Label("Notes:"), notesArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean success;
                if (isApproval) {
                    success = dataStore.approveLeaveRequest(request.getId(), supervisor.getId(), notesArea.getText());
                } else {
                    success = dataStore.rejectLeaveRequest(request.getId(), supervisor.getId(), notesArea.getText());
                }

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Leave request " + (isApproval ? "approved" : "rejected") + " successfully!");
                    showLeaveApprovalsContent(); // Refresh the view
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to " + (isApproval ? "approve" : "reject") + " leave request.");
                }
            }
        });
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
package id.ac.stis.pbo.demo1.ui;

import id.ac.stis.pbo.demo1.data.MySQLDataStore;
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
import java.util.Calendar;

/**
 * Enhanced Employee Dashboard with MySQL integration and role interactions
 */
public class EmployeeDashboard extends Application {
    private final Employee employee;
    private StackPane contentArea;
    private DecimalFormat df = new DecimalFormat("#.##");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final MySQLDataStore dataStore;

    public EmployeeDashboard(Employee employee) {
        this.employee = employee;
        this.dataStore = DataStoreFactory.getMySQLDataStore();
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
        stage.setTitle("GAWE - Employee Dashboard");
        stage.show();
    }

    private HBox createHeader(Stage primaryStage) {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #16a085;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("GAWE - Employee Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Welcome, " + employee.getNama() + " (" + employee.getDivisi() + " - " + employee.getJabatan() + ")");
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

        Button dashboardBtn = createNavButton("📊 Dashboard");
        dashboardBtn.setOnAction(e -> showDashboardContent());

        Button attendanceBtn = createNavButton("⏰ Attendance");
        attendanceBtn.setOnAction(e -> showAttendance());

        Button meetingsBtn = createNavButton("📅 My Meetings");
        meetingsBtn.setOnAction(e -> showMeetings());

        Button leaveBtn = createNavButton("🏖️ Leave Requests");
        leaveBtn.setOnAction(e -> showLeaveRequests());

        Button performanceBtn = createNavButton("📈 My Performance");
        performanceBtn.setOnAction(e -> showPerformance());

        Button salaryBtn = createNavButton("💰 Salary History");
        salaryBtn.setOnAction(e -> showSalaryHistory());

        navigation.getChildren().addAll(dashboardBtn, attendanceBtn, meetingsBtn, leaveBtn, performanceBtn, salaryBtn);
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

        Label titleLabel = new Label("Employee Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Personal info cards
        HBox infoRow = createPersonalInfoCards();

        // Quick actions
        VBox quickActions = createQuickActions();

        // Today's meetings preview - INTEGRATED DATA
        VBox todaysMeetings = createTodaysMeetingsPreview();

        // Pending leave requests status
        VBox leaveStatus = createLeaveRequestStatus();

        content.getChildren().addAll(titleLabel, infoRow, quickActions, todaysMeetings, leaveStatus);
        contentArea.getChildren().add(content);
    }

    private HBox createPersonalInfoCards() {
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER);

        VBox performanceCard = createInfoCard("Performance Score",
                df.format(employee.getSupervisorRating()) + "%",
                "#3498db");
        VBox kpiCard = createInfoCard("Division KPI",
                df.format(employee.getKpiScore()) + "%",
                "#9b59b6");
        VBox salaryCard = createInfoCard("Monthly Salary",
                "Rp " + String.format("%,.0f", employee.calculateGajiBulanan()),
                "#27ae60");
        VBox leaveCard = createInfoCard("Remaining Leave",
                employee.getSisaCuti() + " days",
                "#f39c12");

        infoRow.getChildren().addAll(performanceCard, kpiCard, salaryCard, leaveCard);
        return infoRow;
    }

    private VBox createInfoCard(String title, String value, String color) {
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
        card.setPrefWidth(200);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
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

        Button viewPerformanceBtn = new Button("📊 View Performance");
        viewPerformanceBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 15 25;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
        """);
        viewPerformanceBtn.setOnAction(e -> showPerformance());

        buttonsRow.getChildren().addAll(clockInBtn, clockOutBtn, requestLeaveBtn, viewPerformanceBtn);

        actionsBox.getChildren().addAll(titleLabel, buttonsRow);
        return actionsBox;
    }

    private boolean hasAttendanceToday() {
        List<Attendance> todayAttendance = dataStore.getTodayAttendance(employee.getId());
        return !todayAttendance.isEmpty();
    }

    private boolean hasCompletedAttendanceToday() {
        List<Attendance> todayAttendance = dataStore.getTodayAttendance(employee.getId());
        return !todayAttendance.isEmpty() &&
                todayAttendance.get(0).getJamKeluar() != null;
    }

    private VBox createTodaysMeetingsPreview() {
        VBox meetingsBox = new VBox(15);
        meetingsBox.setPadding(new Insets(20));
        meetingsBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        Label titleLabel = new Label("Today's Meetings");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        ListView<String> meetingsList = new ListView<>();
        meetingsList.setPrefHeight(150);

        // INTEGRATED: Get meetings where this employee is a participant
        List<Meeting> todaysMeetings = dataStore.getMeetingsByEmployee(employee.getId());
        Calendar today = Calendar.getInstance();

        ObservableList<String> meetingItems = FXCollections.observableArrayList();
        for (Meeting meeting : todaysMeetings) {
            Calendar meetingCal = Calendar.getInstance();
            meetingCal.setTime(meeting.getTanggal());

            if (meetingCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    meetingCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                
                // Get organizer name
                Employee organizer = dataStore.getEmployeeById(meeting.getOrganizerId());
                String organizerName = organizer != null ? organizer.getNama() : "Unknown";
                
                meetingItems.add("📅 " + meeting.getTitle() + " at " + meeting.getWaktuMulai() +
                        " (" + meeting.getLokasi() + ") - Organized by " + organizerName);
            }
        }

        if (meetingItems.isEmpty()) {
            meetingItems.add("No meetings scheduled for today");
        }

        meetingsList.setItems(meetingItems);

        meetingsBox.getChildren().addAll(titleLabel, meetingsList);
        return meetingsBox;
    }

    private VBox createLeaveRequestStatus() {
        VBox statusBox = new VBox(15);
        statusBox.setPadding(new Insets(20));
        statusBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        Label titleLabel = new Label("Leave Request Status");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Get pending leave requests for this employee
        List<LeaveRequest> myLeaveRequests = dataStore.getLeaveRequestsByEmployee(employee.getId());
        long pendingCount = myLeaveRequests.stream().filter(lr -> "pending".equals(lr.getStatus())).count();
        long approvedCount = myLeaveRequests.stream().filter(lr -> "approved".equals(lr.getStatus())).count();
        long rejectedCount = myLeaveRequests.stream().filter(lr -> "rejected".equals(lr.getStatus())).count();

        GridPane statusGrid = new GridPane();
        statusGrid.setHgap(20);
        statusGrid.setVgap(10);

        statusGrid.add(new Label("Pending:"), 0, 0);
        statusGrid.add(new Label(String.valueOf(pendingCount)), 1, 0);
        statusGrid.add(new Label("Approved:"), 0, 1);
        statusGrid.add(new Label(String.valueOf(approvedCount)), 1, 1);
        statusGrid.add(new Label("Rejected:"), 0, 2);
        statusGrid.add(new Label(String.valueOf(rejectedCount)), 1, 2);

        statusBox.getChildren().addAll(titleLabel, statusGrid);
        return statusBox;
    }

    private void clockIn() {
        // Check if already clocked in today
        if (hasAttendanceToday()) {
            showAlert(Alert.AlertType.WARNING, "Already Clocked In", "You have already clocked in today.");
            return;
        }

        LocalTime now = LocalTime.now();
        String timeStr = String.format("%02d:%02d", now.getHour(), now.getMinute());

        boolean success = dataStore.saveAttendance(employee.getId(), new Date(), timeStr, null, "hadir");
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

        boolean success = dataStore.updateAttendanceClockOut(employee.getId(), timeStr);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock Out", "Successfully clocked out at " + timeStr);
            showDashboardContent(); // Refresh to update buttons
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to clock out.");
        }
    }

    private void showMeetings() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Meetings");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Meetings table with integrated data
        TableView<Meeting> meetingsTable = createMeetingsTable();

        content.getChildren().addAll(titleLabel, meetingsTable);
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

        TableColumn<Meeting, String> organizerCol = new TableColumn<>("Organizer");
        organizerCol.setCellValueFactory(cellData -> {
            Employee organizer = dataStore.getEmployeeById(cellData.getValue().getOrganizerId());
            return new javafx.beans.property.SimpleStringProperty(organizer != null ? organizer.getNama() : "Unknown");
        });
        organizerCol.setPrefWidth(150);

        TableColumn<Meeting, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(titleCol, dateCol, timeCol, locationCol, organizerCol, statusCol);

        // INTEGRATED: Get meetings where this employee is a participant
        List<Meeting> myMeetings = dataStore.getMeetingsByEmployee(employee.getId());
        ObservableList<Meeting> meetingData = FXCollections.observableArrayList(myMeetings);
        table.setItems(meetingData);

        return table;
    }

    private void showLeaveRequests() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Leave Requests");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Quick request button
        Button newRequestBtn = new Button("➕ New Leave Request");
        newRequestBtn.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
        """);
        newRequestBtn.setOnAction(e -> showLeaveRequestDialog());

        // Leave requests table with integrated data
        TableView<LeaveRequest> leaveTable = createLeaveRequestsTable();

        content.getChildren().addAll(titleLabel, newRequestBtn, leaveTable);
        contentArea.getChildren().add(content);
    }

    private TableView<LeaveRequest> createLeaveRequestsTable() {
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

        TableColumn<LeaveRequest, String> approverCol = new TableColumn<>("Approver");
        approverCol.setCellValueFactory(cellData -> {
            String approverId = cellData.getValue().getApproverId();
            if (approverId != null) {
                Employee approver = dataStore.getEmployeeById(approverId);
                return new javafx.beans.property.SimpleStringProperty(approver != null ? approver.getNama() : "Unknown");
            }
            return new javafx.beans.property.SimpleStringProperty("Pending");
        });

        TableColumn<LeaveRequest, String> notesCol = new TableColumn<>("Approval Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("approverNotes"));

        table.getColumns().addAll(typeCol, startDateCol, endDateCol, daysCol, statusCol, approverCol, notesCol);

        // INTEGRATED: Get leave requests for this employee
        List<LeaveRequest> myLeaveRequests = dataStore.getLeaveRequestsByEmployee(employee.getId());
        ObservableList<LeaveRequest> leaveData = FXCollections.observableArrayList(myLeaveRequests);
        table.setItems(leaveData);

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

        // Show who will approve this request
        String supervisorId = dataStore.getSupervisorByDivision(employee.getDivisi());
        Employee supervisor = dataStore.getEmployeeById(supervisorId);
        Label approverInfo = new Label("This request will be sent to: " + 
                (supervisor != null ? supervisor.getNama() + " (" + supervisor.getJabatan() + ")" : "Your supervisor"));
        approverInfo.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        content.getChildren().addAll(
                new Label("Leave Type:"), leaveTypeCombo,
                new Label("Start Date (No weekends):"), startDatePicker,
                new Label("End Date (No weekends):"), endDatePicker,
                new Label("Reason:"), reasonArea,
                approverInfo
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

                    boolean success = dataStore.saveLeaveRequest(employee.getId(), leaveTypeCombo.getValue(),
                            startSqlDate, endSqlDate, reasonArea.getText());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                                "Leave request submitted successfully!\nIt will be reviewed by " + 
                                (supervisor != null ? supervisor.getNama() : "your supervisor") + ".");
                        showLeaveRequests(); // Refresh
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select start and end dates.");
                }
            }
        });
    }

    private void showAttendance() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Attendance");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Attendance table
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

        List<Attendance> myAttendance = dataStore.getAttendanceByEmployee(employee.getId());
        ObservableList<Attendance> attendanceData = FXCollections.observableArrayList(myAttendance);
        table.setItems(attendanceData);

        return table;
    }

    private void showPerformance() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("My Performance");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Performance overview
        VBox performanceOverview = createPerformanceOverview();

        // Evaluations history
        TableView<EmployeeEvaluation> evaluationsTable = createEvaluationsTable();

        content.getChildren().addAll(titleLabel, performanceOverview, evaluationsTable);
        contentArea.getChildren().add(content);
    }

    private VBox createPerformanceOverview() {
        VBox overviewBox = new VBox(15);
        overviewBox.setPadding(new Insets(20));
        overviewBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

        Label overviewTitle = new Label("Performance Overview");
        overviewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane performanceGrid = new GridPane();
        performanceGrid.setHgap(20);
        performanceGrid.setVgap(15);

        performanceGrid.add(new Label("Division KPI Score:"), 0, 0);
        performanceGrid.add(new Label(df.format(employee.getKpiScore()) + "%"), 1, 0);

        performanceGrid.add(new Label("Supervisor Rating:"), 0, 1);
        performanceGrid.add(new Label(df.format(employee.getSupervisorRating()) + "%"), 1, 1);

        performanceGrid.add(new Label("Base Salary:"), 0, 2);
        performanceGrid.add(new Label("Rp " + String.format("%,.0f", employee.getGajiPokok())), 1, 2);

        performanceGrid.add(new Label("Current Monthly Salary:"), 0, 3);
        performanceGrid.add(new Label("Rp " + String.format("%,.0f", employee.calculateGajiBulanan())), 1, 3);

        performanceGrid.add(new Label("Additional Leave Days:"), 0, 4);
        performanceGrid.add(new Label(employee.calculateAdditionalLeave() + " days"), 1, 4);

        performanceGrid.add(new Label("Employment Status:"), 0, 5);
        Label statusLabel = new Label(employee.isLayoffRisk() ? "⚠️ At Risk" : "✅ Secure");
        statusLabel.setTextFill(employee.isLayoffRisk() ? Color.RED : Color.GREEN);
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        performanceGrid.add(statusLabel, 1, 5);

        overviewBox.getChildren().addAll(overviewTitle, performanceGrid);
        return overviewBox;
    }

    private TableView<EmployeeEvaluation> createEvaluationsTable() {
        TableView<EmployeeEvaluation> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<EmployeeEvaluation, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getEvaluationDate())));

        TableColumn<EmployeeEvaluation, String> supervisorCol = new TableColumn<>("Supervisor");
        supervisorCol.setCellValueFactory(cellData -> {
            Employee supervisor = dataStore.getEmployeeById(cellData.getValue().getSupervisorId());
            return new javafx.beans.property.SimpleStringProperty(supervisor != null ? supervisor.getNama() : "Unknown");
        });

        TableColumn<EmployeeEvaluation, String> punctualityCol = new TableColumn<>("Punctuality");
        punctualityCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getPunctualityScore()) + "%"));

        TableColumn<EmployeeEvaluation, String> attendanceCol = new TableColumn<>("Attendance");
        attendanceCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getAttendanceScore()) + "%"));

        TableColumn<EmployeeEvaluation, String> overallCol = new TableColumn<>("Overall Rating");
        overallCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(df.format(cellData.getValue().getOverallRating()) + "%"));

        TableColumn<EmployeeEvaluation, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));

        table.getColumns().addAll(dateCol, supervisorCol, punctualityCol, attendanceCol, overallCol, commentsCol);

        List<EmployeeEvaluation> myEvaluations = dataStore.getEvaluationsByEmployee(employee.getId());
        ObservableList<EmployeeEvaluation> evaluationData = FXCollections.observableArrayList(myEvaluations);
        table.setItems(evaluationData);

        return table;
    }

    private void showSalaryHistory() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Salary History");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Current salary breakdown
        VBox salaryBreakdown = createSalaryBreakdown();

        // Salary history table
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

        double baseSalary = employee.getGajiPokok();
        double kpiBonus = 0;
        double supervisorBonus = 0;
        double penalty = 0;

        // Calculate KPI bonus
        if (employee.getKpiScore() >= 90) {
            kpiBonus = baseSalary * 0.20;
        } else if (employee.getKpiScore() >= 80) {
            kpiBonus = baseSalary * 0.15;
        } else if (employee.getKpiScore() >= 70) {
            kpiBonus = baseSalary * 0.10;
        } else if (employee.getKpiScore() >= 60) {
            kpiBonus = baseSalary * 0.05;
        }

        // Calculate supervisor bonus
        if (employee.getSupervisorRating() >= 90) {
            supervisorBonus = baseSalary * 0.15;
        } else if (employee.getSupervisorRating() >= 80) {
            supervisorBonus = baseSalary * 0.10;
        } else if (employee.getSupervisorRating() >= 70) {
            supervisorBonus = baseSalary * 0.05;
        }

        // Calculate penalty
        if (employee.getKpiScore() < 60 || employee.getSupervisorRating() < 60) {
            penalty = baseSalary * 0.10;
        }

        double totalSalary = employee.calculateGajiBulanan();

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

    private TableView<SalaryHistory> createSalaryHistoryTable() {
        TableView<SalaryHistory> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<SalaryHistory, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonthName()));

        TableColumn<SalaryHistory, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("tahun"));

        TableColumn<SalaryHistory, String> baseCol = new TableColumn<>("Base Salary");
        baseCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getBaseSalary())));

        TableColumn<SalaryHistory, String> kpiBonusCol = new TableColumn<>("KPI Bonus");
        kpiBonusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getKpiBonus())));

        TableColumn<SalaryHistory, String> supervisorBonusCol = new TableColumn<>("Supervisor Bonus");
        supervisorBonusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getSupervisorBonus())));

        TableColumn<SalaryHistory, String> totalCol = new TableColumn<>("Total Salary");
        totalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Rp " + String.format("%,.0f", cellData.getValue().getTotalSalary())));

        table.getColumns().addAll(monthCol, yearCol, baseCol, kpiBonusCol, supervisorBonusCol, totalCol);

        List<SalaryHistory> mySalaryHistory = dataStore.getSalaryHistoryByEmployee(employee.getId());
        ObservableList<SalaryHistory> salaryData = FXCollections.observableArrayList(mySalaryHistory);
        table.setItems(salaryData);

        return table;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
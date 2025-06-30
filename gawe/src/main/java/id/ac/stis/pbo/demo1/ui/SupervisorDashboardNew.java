/**
 * @deprecated This class has been replaced by SupervisorDashboard.
 * This file is kept only for reference and will be removed.
 * Please use SupervisorDashboard for all supervisor functionality.
 */

package id.ac.stis.pbo.demo1.ui;

@Deprecated
public final class SupervisorDashboardNew {
    private SupervisorDashboardNew() {
        throw new UnsupportedOperationException("This class is deprecated. Use SupervisorDashboard instead.");
    }
}
import javafx.collections.FXCollections;
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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated This class is deprecated and will be removed in future versions.
 * Please use {@link SupervisorDashboard} instead which provides full supervisor functionality.
 * This limited version only supported leave approvals and has been replaced by the full-featured dashboard.
 */
@Deprecated
public class SupervisorDashboardNew extends Application {
    private final Employee supervisor;
    private final MySQLDataStore dataStore;
    private StackPane contentArea;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public SupervisorDashboardNew(Employee supervisor) {
        this.supervisor = supervisor;
        this.dataStore = DataStoreFactory.getMySQLDataStore();
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50 0%, #34495e 100%);");

        // Create main layout components
        HBox header = createHeader(stage);
        VBox navigation = createNavigation();
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));

        root.setTop(header);
        root.setLeft(navigation);
        root.setCenter(contentArea);

        showLeaveApprovalsContent();

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("GAWE - Supervisor Dashboard - " + supervisor.getNama());
        stage.show();
    }

    private HBox createHeader(Stage stage) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95);");

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
        navigation.setPrefWidth(250);
        navigation.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        Button leaveApprovalsBtn = new Button("🏖️ Leave Approvals");
        leaveApprovalsBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #2c3e50;
            -fx-padding: 12 15;
            -fx-background-radius: 8;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);
        leaveApprovalsBtn.setOnAction(e -> showLeaveApprovalsContent());

        navigation.getChildren().add(leaveApprovalsBtn);
        return navigation;
    }

    private void showLeaveApprovalsContent() {
        contentArea.getChildren().clear();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Leave Request Approvals");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        TableView<LeaveRequest> leaveTable = new TableView<>();
        leaveTable.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
        """);
        leaveTable.setPrefHeight(400);

        TableColumn<LeaveRequest, String> employeeCol = new TableColumn<>("Employee");
        employeeCol.setCellValueFactory(cellData -> {
            Employee emp = dataStore.getEmployeeById(cellData.getValue().getEmployeeId());
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getNama() : "Unknown");
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

        TableColumn<LeaveRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<LeaveRequest, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button approveBtn = new Button("✓");
            private final Button rejectBtn = new Button("✗");

            {
                approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5;");

                approveBtn.setOnAction(e -> showApprovalDialog(getTableView().getItems().get(getIndex()), true));
                rejectBtn.setOnAction(e -> showApprovalDialog(getTableView().getItems().get(getIndex()), false));

                actionBox.getChildren().addAll(approveBtn, rejectBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LeaveRequest request = getTableView().getItems().get(getIndex());
                    setGraphic(request.getStatus().equals("pending") ? actionBox : null);
                }
            }
        });

        leaveTable.getColumns().addAll(employeeCol, typeCol, startDateCol, endDateCol, daysCol, reasonCol, actionCol);

        // Get pending leave requests for this supervisor's team
        List<LeaveRequest> pendingRequests = dataStore.getPendingLeaveRequests().stream()
                .filter(request -> {
                    Employee requestingEmployee = dataStore.getEmployeeById(request.getEmployeeId());
                    return requestingEmployee != null && 
                           requestingEmployee.getDivisi().equals(supervisor.getDivisi()) &&
                           requestingEmployee.getRole().equals("pegawai");
                })
                .collect(Collectors.toList());

        leaveTable.setItems(FXCollections.observableArrayList(pendingRequests));

        content.getChildren().addAll(title, leaveTable);
        contentArea.getChildren().add(content);
    }

    private void showApprovalDialog(LeaveRequest request, boolean isApproval) {
        LeaveRequestApprovalDialog dialog = new LeaveRequestApprovalDialog(request, supervisor, dataStore, isApproval);
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean success = dialog.processResult();
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
}

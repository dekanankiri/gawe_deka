package id.ac.stis.pbo.demo1;

import id.ac.stis.pbo.demo1.data.MySQLDataStore;
import id.ac.stis.pbo.demo1.models.Employee;
import id.ac.stis.pbo.demo1.server.GaweServer;
import id.ac.stis.pbo.demo1.ui.SupervisorDashboard;
import id.ac.stis.pbo.demo1.ui.ManagerDashboard;
import id.ac.stis.pbo.demo1.ui.EmployeeDashboard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced HelloApplication with MySQL database integration
 */
public class HelloApplication extends Application {
    private GaweServer server;
    private static MySQLDataStore dataStore; // Make dataStore static to persist across instances
    
    public static MySQLDataStore getDataStore() {
        return dataStore;
    }

    @Override
    public void init() {
        System.out.println("Initializing application...");
        
        // Initialize MySQL DataStore if not already initialized
        if (dataStore == null) {
            try {
                System.out.println("Creating new MySQL DataStore instance...");
                dataStore = new MySQLDataStore();
                logger.info("MySQL DataStore initialized successfully");
            } catch (Exception e) {
                String errorMsg = "Failed to initialize MySQL DataStore: " + e.getMessage();
                logger.severe(errorMsg);
                e.printStackTrace();
                // Show error dialog and exit
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Connection Error");
                    alert.setHeaderText("Failed to connect to MySQL database");
                    alert.setContentText("Please ensure MySQL is running and the database configuration is correct.\n\nError: " + e.getMessage());
                    alert.showAndWait();
                    Platform.exit();
                });
                return;
            }
        } else {
            System.out.println("Using existing MySQL DataStore instance");
        }

        // Start server in background thread
        CompletableFuture.runAsync(() -> {
            try {
                if (server == null) {
                    System.out.println("Starting server...");
                    server = new GaweServer();
                    server.start();
                    System.out.println("Server started successfully");
                }
            } catch (Exception e) {
                String errorMsg = "Failed to start server: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
            }
        });
        
        System.out.println("Application initialization completed");
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HelloApplication.class.getName());

    @Override
    public void start(Stage stage) {
        // Initialize MySQL DataStore if not already initialized
        if (dataStore == null) {
            try {
                System.out.println("Initializing MySQL DataStore...");
                dataStore = new MySQLDataStore();
                System.out.println("MySQL DataStore initialized successfully");
            } catch (Exception e) {
                System.err.println("Failed to initialize MySQL DataStore: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to connect to database. Please ensure MySQL is running.\n\nError: " + e.getMessage());
                Platform.exit();
                return;
            }
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Create login form
        VBox loginForm = createLoginForm(stage);
        root.setCenter(loginForm);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("GAWE - Employee Management System (MySQL)");
        stage.setOnCloseRequest(e -> {
            if (server != null) {
                try {
                    server.stop();
                } catch (Exception ex) {
                    System.err.println("Error stopping server: " + ex.getMessage());
                }
            }
            if (dataStore != null) {
                dataStore.close();
            }
            Platform.exit();
        });
        stage.show();
    }

    private VBox createLoginForm(Stage primaryStage) {
        VBox loginContainer = new VBox(20);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setPadding(new Insets(40));
        loginContainer.setMaxWidth(400);
        loginContainer.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);
        """);

        // Logo and title
        Label titleLabel = new Label("GAWE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label subtitleLabel = new Label("Employee Management System (MySQL)");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));

        // Database status indicator
        Label dbStatusLabel = new Label("✅ Connected to MySQL Database");
        dbStatusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        dbStatusLabel.setTextFill(Color.web("#27ae60"));

        // Login form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Employee ID:");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        TextField userField = new TextField();
        userField.setPromptText("Enter your employee ID");
        userField.setPrefWidth(250);
        userField.setStyle("""
            -fx-padding: 12;
            -fx-background-radius: 8;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 8;
        """);

        Label passLabel = new Label("Password:");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setPrefWidth(250);
        passField.setStyle("""
            -fx-padding: 12;
            -fx-background-radius: 8;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 8;
        """);

        formGrid.add(userLabel, 0, 0);
        formGrid.add(userField, 1, 0);
        formGrid.add(passLabel, 0, 1);
        formGrid.add(passField, 1, 1);

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(250);
        loginBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 12;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);

        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("""
            -fx-background-color: #2980b9;
            -fx-text-fill: white;
            -fx-padding: 12;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """));

        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 12;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """));

        loginBtn.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), primaryStage));

        // Enter key support
        passField.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), primaryStage));

        // Demo credentials info
        Label demoLabel = new Label("Demo Credentials:");
        demoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        demoLabel.setTextFill(Color.web("#2c3e50"));

        VBox demoInfo = new VBox(5);
        demoInfo.setAlignment(Pos.CENTER);

        Label[] demoCredentials = {
                new Label("Manager: MNG001 / password123"),
                new Label("Supervisor: SUP001 / password123"),
                new Label("Employee: EMP001 / password123")
        };

        for (Label label : demoCredentials) {
            label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
            label.setTextFill(Color.web("#7f8c8d"));
        }

        demoInfo.getChildren().addAll(demoCredentials);

        loginContainer.getChildren().addAll(titleLabel, subtitleLabel, dbStatusLabel, formGrid, loginBtn, demoLabel, demoInfo);
        return loginContainer;
    }

    private void handleLogin(String employeeId, String password, Stage primaryStage) {
        if (employeeId.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Login Failed", "Please enter both Employee ID and Password.");
            return;
        }

        try {
            // Authenticate user using MySQL DataStore
            Employee employee = dataStore.authenticateUser(employeeId, password);

            if (employee != null) {
                // Close login window
                primaryStage.close();

                // Open appropriate dashboard based on role
                openDashboard(employee);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Employee ID or Password.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to authenticate user: " + e.getMessage());
            logger.severe("Authentication error: " + e.getMessage());
        }
    }

    private void openDashboard(Employee employee) {
        try {
            Stage dashboardStage = new Stage();

            switch (employee.getRole().toLowerCase()) {
                case "manajer":
                    // Open Manager Dashboard
                    ManagerDashboard managerDashboard = new ManagerDashboard(employee);
                    managerDashboard.start(dashboardStage);
                    break;

                case "supervisor":
                    // Open Full-Featured Supervisor Dashboard
                    SupervisorDashboard supervisorDashboard = new SupervisorDashboard(employee);
                    supervisorDashboard.start(dashboardStage);
                    break;

                case "pegawai":
                    // Open Employee Dashboard
                    EmployeeDashboard employeeDashboard = new EmployeeDashboard(employee, dataStore);
                    employeeDashboard.start(dashboardStage);
                    break;

                default:
                    showAlert(Alert.AlertType.ERROR, "Error", "Unknown user role: " + employee.getRole());
                    return;
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }
        if (dataStore != null) {
            dataStore.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
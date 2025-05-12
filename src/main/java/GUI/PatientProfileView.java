package GUI;

import Service_Interfaces.*;
import Class_model.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
//import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PatientProfileView extends VBox {

    private MainApp mainApp;
    private int patientId;
    private String patientName;

    public PatientProfileView(MainApp mainApp, int patientId, String patientName) {
        this.mainApp = mainApp;
        this.patientId = patientId;
        this.patientName = patientName;
        this.setSpacing(20);
        this.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Patient Profile");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextFill(Color.DARKBLUE);
        titleLabel.setAlignment(Pos.CENTER);

        Button placeOrderButton = new Button("Place Order\n(Click to place a new order)");
        placeOrderButton.setGraphic(new ImageView(new Image("file:order_icon.png"))); // Replace with your icon file path
        placeOrderButton.setContentDisplay(ContentDisplay.TOP); // Place text below the icon
        placeOrderButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        placeOrderButton.setPrefSize(200, 100);
        placeOrderButton.setAlignment(Pos.CENTER);
        placeOrderButton.setOnAction(event -> mainApp.loadPage("Order" , patientName));

        // Center the Place Order Button
        HBox placeOrderBox = new HBox(placeOrderButton);
        placeOrderBox.setAlignment(Pos.CENTER);
        placeOrderBox.setPadding(new Insets(20));

        // Patient Details Section
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setAlignment(Pos.CENTER);

        //current patient.
        Patient currentpatient = Patient_Service.getInstance().GetPatient(patientName);


        Label nameLabel = new Label("Name:");
        Label nameValue = new Label(patientName);
        Label ageLabel = new Label("Age:");
        Label ageValue = new Label(String.valueOf(currentpatient.getAge()));
        Label addressLabel = new Label("Address:");
        Label addressValue = new Label(currentpatient.getAddress());

        detailsGrid.add(new Label("Name:"), 0, 0);
        detailsGrid.add(nameValue, 1, 0);
        detailsGrid.add(new Label("Age:"), 0, 1);
        detailsGrid.add(ageValue, 1, 1);
        detailsGrid.add(new Label("Address:"), 0, 2);
        detailsGrid.add(addressValue, 1, 2);

        // Edit Profile Section
        HBox editProfileBox = new HBox(10);
        editProfileBox.setAlignment(Pos.CENTER);

        Button editButton = new Button("Edit Profile");
        editButton.setOnAction(event -> {
            // Show a dialog to edit profile
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(mainApp.getPrimaryStage());
            dialog.setTitle("Edit Profile");

            TextField nameField = new TextField(nameValue.getText());
            TextField ageField = new TextField(ageValue.getText());
            TextField addressField = new TextField(addressValue.getText());

            dialog.getDialogPane().setContent(new VBox(new Label("Name:"), nameField, new Label("Age:"), ageField, new Label("Address:"), addressField));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    // Update patient attributes
                    updatePatient(patientName, currentpatient.getPassword(), "name", nameField.getText());
                    updatePatient(patientName, currentpatient.getPassword(), "age", Integer.parseInt(ageField.getText()));
                    updatePatient(patientName, currentpatient.getPassword(), "address", addressField.getText());
                    nameValue.setText(nameField.getText());
                    ageValue.setText(ageField.getText());
                    addressValue.setText(addressField.getText());
                }
                return dialogButton;
            });
            dialog.showAndWait();
        });

        Button deleteButton = new Button("Delete Account");
        deleteButton.setOnAction(event -> {
            // Confirm deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?", ButtonType.YES, ButtonType.NO);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    Patient_Service.getInstance().RemovePatient(patientName);
                    mainApp.loadPage("Entry" , null);
                }
            });
        });

        editProfileBox.getChildren().addAll(editButton, deleteButton);

        // Balance Section
        HBox balanceBox = new HBox(10);
        balanceBox.setAlignment(Pos.CENTER);

        Button balanceButton = new Button("View Balance");
        balanceButton.setOnAction(event -> {
            // Show balance
            double balance = Patient_Service.getInstance().GetPatientBalance(patientName);
            showAlert("Balance", "Your balance is: $" + balance);
        });

        balanceBox.getChildren().add(balanceButton);

        // Orders Section
        Label ordersLabel = new Label("Successfully Paid Orders");
        ordersLabel.setFont(new Font("Arial", 16));
        ordersLabel.setTextFill(Color.DARKRED);

        ListView<Order> paidOrdersListView = new ListView<>();
        paidOrdersListView.setPrefHeight(150);

        Button viewPaidOrdersButton = new Button("View Paid Orders");
        viewPaidOrdersButton.setOnAction(event -> {
            List<Order> paidOrders = fetchPaidOrders(patientName);
            if(paidOrders == null || paidOrders.isEmpty()) {
                showAlert("Error", "No Paid orders found.");
                paidOrders = new ArrayList<>();
            }
            ObservableList<Order> paidOrdersObservableList = FXCollections.observableArrayList(paidOrders);
            paidOrdersListView.setItems(paidOrdersObservableList);
        });

        // Order Status Section
        Label orderStatusLabel = new Label("Order Status");
        orderStatusLabel.setFont(new Font("Arial", 16));
        orderStatusLabel.setTextFill(Color.DARKRED);

        ListView<String> orderStatusListView = new ListView<>();
        orderStatusListView.setPrefHeight(150);

        Button viewOrderStatusButton = new Button("View Order Status");
        viewOrderStatusButton.setOnAction(event -> {
            List<Order> paidOrders = Order_Service.getInstance().GetByCustomer(patientName);
            if(paidOrders == null|| paidOrders.isEmpty()) {
                showAlert("Error", "No orders found.");
                paidOrders = new ArrayList<>();
            }
            ObservableList<String> orderStatusObservableList = FXCollections.observableArrayList(
                    paidOrders.stream().filter(order -> !order.getStatus().equals("Paid")).map(order -> "Order ID: " + order.getOrderId() + " - Status: " + order.getStatus()).toList()
            );
            orderStatusListView.setItems(orderStatusObservableList);
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> mainApp.loadPage("Entry" , null));

        // payment button
        HBox paymentButtonBox = new HBox(10);
        paymentButtonBox.setAlignment(Pos.CENTER);
        paymentButtonBox.setPadding(new Insets(20));

        Button paymentButton = new Button("Proceed to Payment");
        paymentButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        paymentButton.setOnAction(event -> mainApp.loadPage("Payment", patientName));

        paymentButtonBox.getChildren().add(paymentButton);

        // Organize components into the main VBox
        this.getChildren().addAll(titleLabel, placeOrderBox, detailsGrid, editProfileBox, balanceBox, ordersLabel, paidOrdersListView, viewPaidOrdersButton, orderStatusLabel, orderStatusListView, viewOrderStatusButton, paymentButtonBox, backButton);

    }

    private void updatePatient(String patientName, String password, String query, Object value) {
        // Implement update logic using PatientService
        if(Patient_Service.getInstance().UpdatePatient(patientName, password, query, value) == -1){
            showAlert("Error", "Failed to update patient.");
        }
    }

    private List<Order> fetchPaidOrders(String patientName) {
        // Implement fetching paid orders using OrderService
        if(Order_Service.getInstance().GetByCustomer(patientName) == null || patientName == null) {
            showAlert("Error", "No orders found.");
            return new ArrayList<>();
        }
        return Order_Service.getInstance().GetByCustomer(patientName).stream()
                .filter(order -> "Paid".equals(order.getStatus()))
                .toList();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();
        });
    }
}


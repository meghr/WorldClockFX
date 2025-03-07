package com.worldclock;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WorldClockApp extends Application {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
    
    private final Map<String, ZoneId> timeZones = new LinkedHashMap<>();
    private final List<ClockPanel> clockPanels = new ArrayList<>();
    private final Timer timer = new Timer(true);
    
    @Override
    public void start(Stage primaryStage) {
        initializeTimeZones();
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create HBox to hold clocks and conversion panel side by side
        HBox mainContainer = new HBox(15);
        mainContainer.setAlignment(Pos.CENTER);
        
        VBox clocksContainer = new VBox(10);
        clocksContainer.setPadding(new Insets(10));
        clocksContainer.setAlignment(Pos.CENTER);
        
        // Create 4 clock panels
        for (int i = 0; i < 4; i++) {
            ClockPanel clockPanel = new ClockPanel(i, timeZones);
            clockPanels.add(clockPanel);
            clocksContainer.getChildren().add(clockPanel);
        }
        
        // Time conversion panel
        VBox conversionPanel = createConversionPanel();
        
        // Add both containers to the HBox
        mainContainer.getChildren().addAll(clocksContainer, conversionPanel);
        
        // Set the HBox as the center of the root
        root.setCenter(mainContainer);
        
        // Start the timer to update clocks
        startClockUpdates();
        
        Scene scene = new Scene(root, 1000, 700);  // Increased width to accommodate side-by-side layout
        primaryStage.setTitle("World Clock Widget");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> {
            timer.cancel();
            Platform.exit();
        });
    }
    
    private void initializeTimeZones() {
        timeZones.put("New York (EST/EDT)", ZoneId.of("America/New_York"));
        timeZones.put("London (GMT/BST)", ZoneId.of("Europe/London"));
        timeZones.put("Tokyo (JST)", ZoneId.of("Asia/Tokyo"));
        timeZones.put("Sydney (AEST/AEDT)", ZoneId.of("Australia/Sydney"));
        timeZones.put("Los Angeles (PST/PDT)", ZoneId.of("America/Los_Angeles"));
        timeZones.put("Paris (CET/CEST)", ZoneId.of("Europe/Paris"));
        timeZones.put("Dubai (GST)", ZoneId.of("Asia/Dubai"));
        timeZones.put("Singapore (SGT)", ZoneId.of("Asia/Singapore"));
        timeZones.put("Mumbai (IST)", ZoneId.of("Asia/Kolkata"));
        timeZones.put("Berlin (CET/CEST)", ZoneId.of("Europe/Berlin"));
        timeZones.put("Beijing (CST)", ZoneId.of("Asia/Shanghai"));
        timeZones.put("São Paulo (BRT/BRST)", ZoneId.of("America/Sao_Paulo"));
        
        // Adding more European cities
        timeZones.put("Prague (CET/CEST)", ZoneId.of("Europe/Prague"));
        timeZones.put("Vienna (CET/CEST)", ZoneId.of("Europe/Vienna"));
        timeZones.put("Warsaw (CET/CEST)", ZoneId.of("Europe/Warsaw"));
        timeZones.put("Budapest (CET/CEST)", ZoneId.of("Europe/Budapest"));
        timeZones.put("Rome (CET/CEST)", ZoneId.of("Europe/Rome"));
        timeZones.put("Amsterdam (CET/CEST)", ZoneId.of("Europe/Amsterdam"));
        timeZones.put("Madrid (CET/CEST)", ZoneId.of("Europe/Madrid"));
        timeZones.put("Stockholm (CET/CEST)", ZoneId.of("Europe/Stockholm"));
        timeZones.put("Athens (EET/EEST)", ZoneId.of("Europe/Athens"));
        timeZones.put("Helsinki (EET/EEST)", ZoneId.of("Europe/Helsinki"));
        timeZones.put("Lisbon (WET/WEST)", ZoneId.of("Europe/Lisbon"));
        timeZones.put("Dublin (GMT/IST)", ZoneId.of("Europe/Dublin"));
    }
    
    private void startClockUpdates() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    for (ClockPanel panel : clockPanels) {
                        panel.updateTime();
                    }
                });
            }
        };
        
        // Update every second
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    
    private VBox createConversionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        Label titleLabel = new Label("Time Conversion");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> sourceZoneCombo = new ComboBox<>(FXCollections.observableArrayList(timeZones.keySet()));
        sourceZoneCombo.setPrefWidth(200);
        sourceZoneCombo.getSelectionModel().selectFirst();
        
        TextField hourField = new TextField("12");
        hourField.setPrefWidth(50);
        hourField.setPromptText("HH");
        
        TextField minuteField = new TextField("00");
        minuteField.setPrefWidth(50);
        minuteField.setPromptText("MM");
        
        Label colonLabel = new Label(":");
        
        Button convertButton = new Button("Convert");
        convertButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        inputRow.getChildren().addAll(
                new Label("Time in:"), sourceZoneCombo, 
                hourField, colonLabel, minuteField, 
                convertButton);
        
        GridPane resultGrid = new GridPane();
        resultGrid.setHgap(10);
        resultGrid.setVgap(5);
        resultGrid.setPadding(new Insets(10));
        
        // Add results section to the panel
        panel.getChildren().addAll(titleLabel, inputRow, resultGrid);
        
        convertButton.setOnAction(e -> {
            try {
                int hour = Integer.parseInt(hourField.getText().trim());
                int minute = Integer.parseInt(minuteField.getText().trim());
                
                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    showAlert("Invalid time. Hours must be 0-23, minutes must be 0-59.");
                    return;
                }
                
                String selectedZone = sourceZoneCombo.getValue();
                ZoneId sourceZoneId = timeZones.get(selectedZone);
                
                // Create LocalTime from input
                LocalTime localTime = LocalTime.of(hour, minute);
                
                // Get today's date in the source time zone
                ZonedDateTime now = ZonedDateTime.now(sourceZoneId);
                LocalDate today = now.toLocalDate();
                
                // Combine date and time
                LocalDateTime dateTime = LocalDateTime.of(today, localTime);
                ZonedDateTime sourceDateTime = ZonedDateTime.of(dateTime, sourceZoneId);
                
                // Clear previous results
                resultGrid.getChildren().clear();
                
                // Add headers
                resultGrid.add(new Label("Location"), 0, 0);
                resultGrid.add(new Label("Date"), 1, 0);
                resultGrid.add(new Label("Time"), 2, 0);
                
                // Add results for each time zone
                int row = 1;
                for (Map.Entry<String, ZoneId> entry : timeZones.entrySet()) {
                    ZonedDateTime targetDateTime = sourceDateTime.withZoneSameInstant(entry.getValue());
                    
                    Label locationLabel = new Label(entry.getKey());
                    Label dateLabel = new Label(targetDateTime.format(DATE_FORMATTER));
                    Label timeLabel = new Label(targetDateTime.format(TIME_FORMATTER));
                    
                    if (entry.getKey().equals(selectedZone)) {
                        locationLabel.setStyle("-fx-font-weight: bold;");
                        dateLabel.setStyle("-fx-font-weight: bold;");
                        timeLabel.setStyle("-fx-font-weight: bold;");
                    }
                    
                    resultGrid.add(locationLabel, 0, row);
                    resultGrid.add(dateLabel, 1, row);
                    resultGrid.add(timeLabel, 2, row);
                    
                    row++;
                }
                
            } catch (NumberFormatException ex) {
                showAlert("Please enter valid numbers for hours and minutes.");
            }
        });
        
        // Set minimum height for the conversion panel
        panel.setMinHeight(250);
        return panel;
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        timer.cancel();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Inner class for clock panels
    private static class ClockPanel extends VBox {
        private final Label timeLabel;
        private final Label dateLabel;
        private final Label timezoneLabel;
        private ZoneId currentZoneId;
        
        public ClockPanel(int index, Map<String, ZoneId> availableZones) {
            setSpacing(5);
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER);
            setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");
            setPrefHeight(120);
            
            ComboBox<String> zoneSelector = new ComboBox<>(FXCollections.observableArrayList(availableZones.keySet()));
            zoneSelector.getSelectionModel().select(index % availableZones.size());
            currentZoneId = availableZones.get(zoneSelector.getValue());
            
            timeLabel = new Label();
            timeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            
            dateLabel = new Label();
            
            timezoneLabel = new Label();
            timezoneLabel.setStyle("-fx-font-style: italic;");
            
            zoneSelector.setOnAction(e -> {
                String selected = zoneSelector.getValue();
                currentZoneId = availableZones.get(selected);
                updateTime();
            });
            
            getChildren().addAll(zoneSelector, timeLabel, dateLabel, timezoneLabel);
            updateTime();
        }
        
        public void updateTime() {
            ZonedDateTime now = ZonedDateTime.now(currentZoneId);
            timeLabel.setText(now.format(TIME_FORMATTER));
            dateLabel.setText(now.format(DATE_FORMATTER));
            timezoneLabel.setText(currentZoneId.getId() + " (UTC" + 
                    getOffsetString(now.getOffset()) + ")");
        }
        
        private String getOffsetString(ZoneOffset offset) {
            int totalSeconds = offset.getTotalSeconds();
            int hours = totalSeconds / 3600;
            int minutes = Math.abs((totalSeconds % 3600) / 60);
            
            return String.format("%+d:%02d", hours, minutes);
        }
    }
}
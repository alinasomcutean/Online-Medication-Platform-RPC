package ro.tuc.ds2020.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import ro.tuc.ds2020.dto.MedicationPillDispenserDto;
import ro.tuc.ds2020.dto.MedicationPlanPillDispenserDto;
import ro.tuc.ds2020.services.rpc.PillDispenser;
import ro.tuc.ds2020.services.rpc.PillDispenserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@FxmlView("pillDispenser.fxml")
@Controller
@Component
public class PillDispenserController {

    @Autowired
    private PillDispenserService pillDispenser;

    @FXML private Label time;
    @FXML private Label date;
    @FXML private Label medTaken;

    @FXML private Button backButton;
    @FXML private Button nextButton;

    @FXML private TableView<MedicationPlanPillDispenserDto> medicationPlansList;
    @FXML private TableColumn<MedicationPlanPillDispenserDto, String> intakeInterval;
    @FXML private TableColumn<MedicationPlanPillDispenserDto, String> startDate;
    @FXML private TableColumn<MedicationPlanPillDispenserDto, String> endDate;

    @FXML private TableView<MedicationPillDispenserDto> medicationList;
    @FXML private TableColumn<MedicationPillDispenserDto, String> medicationName;
    @FXML private TableColumn<MedicationPillDispenserDto, String> medicationDosage;
    @FXML private TableColumn<MedicationPillDispenserDto, String> sideEffects;
    @FXML private TableColumn<Button, Button> action;

    private MedicationPlanPillDispenserDto selectedMedPlan;

    //variables to monitor how many times each button is pressed in order to change the hour
    private int backTimes = 0;
    private int nextTimes = 0;

    //check if a row was selected
    private int rowSelected = 0;
    private boolean enable = false;

    private String patientId = "";
    private List<String> medicationsTaken = new ArrayList<>();
    private List<String> allMedications = new ArrayList<>();

    //variable to check if time for taking medications expired
    private boolean timeExpired;

    ///keep the day globally
    private int year;
    private int month;
    private int day;

    //know if a new day started or not
    private boolean newDay = true;

    public void initialize() {
        //get current date
        LocalDate currentDate = LocalDate.now();
        date.setText(currentDate.toString());
        patientId = "d64bb3ff-5fcf-4cb0-8936-6e10b4797015";

        year = currentDate.getYear();
        month = currentDate.getMonthValue();
        day = currentDate.getDayOfMonth();

        //get current time
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            int hour = validateHour(currentTime.getHour() - backTimes + nextTimes);
            checkDownloadTime(currentTime, patientId);

            if((hour == 7 || hour == 0)) {
                timeExpired = false;
            } else {
                if(hour >= 1 && hour < 7) {
                    timeExpired = true;
                }
            }

            time.setText(hour + ":" + currentTime.getMinute() + ":" + currentTime.getSecond());

            backButton.setOnAction(event -> {
                backTimes++;
                time.setText(hour + ":" + currentTime.getMinute() + ":" + currentTime.getSecond());
            });

            nextButton.setOnAction(event -> {
                nextTimes++;
                time.setText(hour + ":" + currentTime.getMinute() + ":" + currentTime.getSecond());
            });

            if(rowSelected != 0 && selectedMedPlan != null) {
                checkTimeToShowMedications(currentTime, selectedMedPlan.getIntakeIntervals(), patientId, selectedMedPlan.getId());
            }

            medicationPlansList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedMedPlan = medicationPlansList.getSelectionModel().getSelectedItem();
                if (rowSelected == 0) {
                    checkTimeToShowMedications(currentTime, selectedMedPlan.getIntakeIntervals(), patientId, selectedMedPlan.getId());
                    rowSelected++;
                }

            });

            checkDate(currentTime);
        }), new KeyFrame(Duration.seconds(1))
        );

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private int validateHour(int hour) {
        if(hour > 0) {
            while(hour > 23) {
                hour -= 24;
            }
        } else {
            while(hour < 0) {
                hour +=24;
            }
        }

        return hour;
    }

    private void validateDate() {
        day++;

        if(month < 8){
            if(month % 2 == 1) {
                if(day > 31) {
                    month++;
                    day = 1;
                } else {
                    if(month == 2) {
                        if(year % 4 == 0) {
                            if(day > 29) {
                                month++;
                                day = 1;
                            }
                        } else {
                            if(day > 28) {
                                month++;
                                day = 1;
                            }
                        }
                    } else {
                        if(day > 30) {
                            month++;
                            day = 1;
                        }
                    }
                }
            }
        } else {
            if(month % 2 == 1) {
                if(day > 30) {
                    month++;
                    day = 1;
                }
            } else {
                if(month == 12) {
                    if(day > 31) {
                        year++;
                        month = 1;
                        day = 1;
                    }
                } else {
                    month++;
                    day = 1;
                }
            }
        }

    }

    private void checkDate(LocalTime currentTime) {
        LocalTime startDay = LocalTime.of(0, 0, 0);
        LocalTime endDay = LocalTime.of(1, 0, 0);
        LocalTime localTime = LocalTime.of(validateHour(currentTime.getHour() - backTimes + nextTimes), currentTime.getMinute(), currentTime.getSecond());

        if(localTime.compareTo(startDay) >= 0 && localTime.compareTo(endDay) < 0 && newDay) {
            validateDate();
            date.setText(LocalDate.of(year, month, day).toString());
            newDay = false;
        }

        if(localTime.compareTo(endDay) >= 0) {
            newDay = true;
        }
    }

    private void checkDownloadTime(LocalTime currentTime, String patientId) {
        LocalTime localTime = LocalTime.of(validateHour(currentTime.getHour() - backTimes + nextTimes), currentTime.getMinute(), currentTime.getSecond());
        LocalTime downloadTimeStart = LocalTime.of(1, 0, 0);
        LocalTime endDay = LocalTime.of(0,0,0);

        if(localTime.compareTo(downloadTimeStart) >= 0) {
            downloadMedicationPlan(patientId);
        }

        if(localTime.compareTo(endDay) >= 0 && localTime.compareTo(downloadTimeStart) < 0) {
            medicationPlansList.getItems().clear();
            medicationList.getItems().clear();
        }
    }

    private void downloadMedicationPlan(String patientId) {
        List<MedicationPlanPillDispenserDto> medicationPlanList = pillDispenser.getMedicationPlansForPatient(patientId);

        LocalDate currentDate = LocalDate.now();
        LocalDate d = LocalDate.of(2020, 12, 26);

        //Filter the medication plans after date (keep only the active medication plans)
        for (MedicationPlanPillDispenserDto m : medicationPlanList) {
            if (m.getPeriodStart().compareTo(currentDate) < 0 && m.getPeriodEnd().compareTo(currentDate) > 0) {
                if(!medicationPlansList.getItems().contains(m)) {
                    medicationPlansList.getItems().add(m);
                }
            }
        }

        //Display all the active medication plans
        intakeInterval.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getIntakeIntervals().toString()));
        startDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPeriodStart().toString()));
        endDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPeriodEnd().toString()));
    }

    private Callback<TableColumn<Button, Button>, TableCell<Button, Button>> buttonDisable() {
        Callback<TableColumn<Button, Button>, TableCell<Button, Button>> cellFactory
                = new Callback<>() {
            @Override
            public TableCell<Button, Button> call(TableColumn<Button, Button> medicationPillDispenserDtoButtonTableColumn) {
                final TableCell<Button, Button> cell = new TableCell<>() {
                    final Button button = new Button("Take medication");

                    @Override
                    public void updateItem(Button item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty) {
                            setDisable(true);
                            setGraphic(button);
                        }
                    }
                };
                return cell;
            }
        };

        return cellFactory;
    }

    private Callback<TableColumn<Button, Button>, TableCell<Button, Button>> buttonEnable() {
        Callback<TableColumn<Button, Button>, TableCell<Button, Button>> cellFactory
                = new Callback<>() {
            @Override
            public TableCell<Button, Button> call(TableColumn<Button, Button> medicationPillDispenserDtoButtonTableColumn) {
                final TableCell<Button, Button> cell = new TableCell<>() {
                    final Button button = new Button("Take medication");

                    @Override
                    public void updateItem(Button item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty) {
                            setDisable(false);
                            setGraphic(button);
                        } else {
                            setDisable(false);
                            setGraphic(null);
                        }
                        {
                            button.setOnAction(event -> {
                                button.setDisable(true);
                                setGraphic(button);

                                //Get the name of the taken medication
                                TableRow row = this.getTableRow();
                                String medicationName = medicationList.getItems().get(row.getIndex()).getName();

                                //display a message that medication was taken
                                medTaken.setText("Medication " + medicationName + " was taken.");

                                medicationList.getItems().remove(row.getIndex());
                                pillDispenser.medicationTaken(patientId, medicationName);
                                medicationsTaken.add(medicationName);
                            });
                        }
                    }
                };
                return cell;
            }
        };

        return cellFactory;
    }

    private void displayMedications(String patientId, String medicationPlanId) {
        allMedications = new ArrayList<>();
        //get medications for a medication plan from the server
        List<MedicationPillDispenserDto> medicationPillDispenserDtos = pillDispenser.getMedicationsForMedicationPlan(patientId, medicationPlanId);
        medicationPillDispenserDtos.forEach(m -> medicationList.getItems().add(m));
        medicationPillDispenserDtos.forEach(m -> allMedications.add(m.getName()));

        //Display all the medications for a medication plan
        medicationName.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getName()));
        medicationDosage.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDosage()));
        sideEffects.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSideEffects().toString()));

        action.setCellFactory(buttonDisable());
    }

    private void checkTimeToShowMedications(LocalTime currentTime, int intakeInterval, String patientId, String medicationPlanId) {
        LocalTime localTime = LocalTime.of(validateHour(currentTime.getHour() - backTimes + nextTimes), currentTime.getMinute(), currentTime.getSecond());
        int hours = localTime.getHour() % intakeInterval;

        if((hours == 7 || hours == 0) && localTime.getHour() >= 7 && !enable) {
            displayMedications(patientId, medicationPlanId);
            medicationsTaken = new ArrayList<>();
            action.setCellFactory(buttonEnable());
            enable = true;
            timeExpired = false;
        } else {
            if(hours >= 1 && hours < 7) {
                medicationList.getItems().clear();
                if(medicationsTaken.size() == allMedications.size()) {
                    pillDispenser.allMedicationTaken("All the medication were taken");
                } else {
                    if(enable) {
                        for (String medicationName : allMedications) {
                            if (!medicationsTaken.contains(medicationName)) {
                                String msg = "Medication " + medicationName + " was not taken.";
                                medTaken.setText(msg);
                                pillDispenser.allMedicationTaken(msg);
                            }
                        }
                    }
                }
                enable = false;
                timeExpired = true;
            }
        }
    }

}

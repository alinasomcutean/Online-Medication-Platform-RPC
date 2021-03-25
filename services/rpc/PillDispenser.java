package ro.tuc.ds2020.services.rpc;

import org.springframework.stereotype.Component;
import ro.tuc.ds2020.dto.MedicationPillDispenserDto;
import ro.tuc.ds2020.dto.MedicationPlanPillDispenserDto;

import java.rmi.Remote;
import java.util.List;

public interface PillDispenser {

    List<MedicationPlanPillDispenserDto> getMedicationPlansForPatient(String patientId);

    List<MedicationPillDispenserDto> getMedicationsForMedicationPlan(String patientId, String medicationPlanId);

    void medicationTaken(String patientId, String medicationName);

    void allMedicationTaken(String message);

}

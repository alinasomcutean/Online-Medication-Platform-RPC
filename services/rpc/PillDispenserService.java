package ro.tuc.ds2020.services.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ro.tuc.ds2020.dto.MedicationPillDispenserDto;
import ro.tuc.ds2020.dto.MedicationPlanPillDispenserDto;

import javax.transaction.Transactional;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

@Component
public class PillDispenserService {

    private PillDispenser pillDispenser;
    public PillDispenserService(PillDispenser pillDispenser) {
        this.pillDispenser = pillDispenser;
    }

    public List<MedicationPlanPillDispenserDto> getMedicationPlansForPatient(String patientId) {
        return pillDispenser.getMedicationPlansForPatient(patientId);
    }

    public List<MedicationPillDispenserDto> getMedicationsForMedicationPlan(String patientId, String medicationPlanId) {
        return pillDispenser.getMedicationsForMedicationPlan(patientId, medicationPlanId);
    }

    public void medicationTaken(String patientId, String medicationName) {
        pillDispenser.medicationTaken(patientId, medicationName);
    }

    public void allMedicationTaken(String message) {
        pillDispenser.allMedicationTaken(message);
    }
}

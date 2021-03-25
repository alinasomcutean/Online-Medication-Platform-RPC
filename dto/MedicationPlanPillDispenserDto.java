package ro.tuc.ds2020.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicationPlanPillDispenserDto implements Serializable {
    String id;
    Integer intakeIntervals;
    LocalDate periodStart;
    LocalDate periodEnd;
}

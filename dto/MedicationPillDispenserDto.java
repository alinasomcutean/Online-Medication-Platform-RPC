package ro.tuc.ds2020.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class MedicationPillDispenserDto implements Serializable {

    String id;
    String name;
    String dosage;
    List<String> sideEffects = new ArrayList<>();
}

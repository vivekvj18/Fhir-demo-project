package com.example.fhir_demo.mapper;

import com.example.fhir_demo.HospitalB.model.HospitalBPatient;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FHIRToHospitalBPatientMapper {

    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("d'th' MMM yyyy");

    public static HospitalBPatient mapToHospitalB(Patient fhirPatient) {

        HospitalBPatient patientB = new HospitalBPatient();

        // Map ID
        patientB.setUhid(fhirPatient.getId());

        // Map Name
        if (!fhirPatient.getName().isEmpty()) {
            patientB.setFullName(fhirPatient.getName().get(0).getText());
        }

        // Map DOB (FHIR → Hospital-B format)
        if (fhirPatient.getBirthDate() != null) {
            LocalDate dob = fhirPatient.getBirthDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            patientB.setDateOfBirth(dob.format(OUTPUT_FORMAT));
        }

        // Map Gender
        if (fhirPatient.getGender() != null) {
            patientB.setGender(
                    fhirPatient.getGender().getDisplay()
            );
        }

        return patientB;
    }
}

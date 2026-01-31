package com.example.fhir_demo.mapper;

import com.example.fhir_demo.HospitalA.model.HospitalAPatient;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HospitalAToFHIRPatientMapper {

    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Patient mapToFHIRPatient(HospitalAPatient hospitalPatient) {

        Patient patient = new Patient();

        // Set ID
        patient.setId(hospitalPatient.getPatientId());

        // Set Name
        HumanName name = new HumanName();
        name.setText(hospitalPatient.getName());
        patient.addName(name);

        // Convert DOB
        LocalDate dob = LocalDate.parse(
                hospitalPatient.getDob(), INPUT_FORMAT
        );
        patient.setBirthDate(java.sql.Date.valueOf(dob));

        // Convert Gender
        if ("M".equalsIgnoreCase(hospitalPatient.getGender())) {
            patient.setGender(AdministrativeGender.MALE);

        } else if ("F".equalsIgnoreCase(hospitalPatient.getGender())) {
            patient.setGender(AdministrativeGender.FEMALE);

        }

        return patient;
    }
}

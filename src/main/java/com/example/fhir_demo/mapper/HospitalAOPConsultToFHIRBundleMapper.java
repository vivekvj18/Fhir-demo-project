package com.example.fhir_demo.mapper;

import com.example.fhir_demo.HospitalA.dto.HospitalAOPConsultRecordDTO;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class HospitalAOPConsultToFHIRBundleMapper {

    private static final DateTimeFormatter INPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Bundle mapToBundle(HospitalAOPConsultRecordDTO dto) {

        // ---------------- Patient ----------------
        Patient patient = new Patient();
        patient.setId(dto.getPatientId());

        // ✅ Dynamic name mapping (NO hardcode)
        if (dto.getPatientName() != null && !dto.getPatientName().isBlank()) {
            HumanName name = new HumanName();
            name.setText(dto.getPatientName());
            patient.addName(name);
        }

        // ---------------- Encounter ----------------
        Encounter encounter = new Encounter();
        encounter.setStatus(Encounter.EncounterStatus.FINISHED);
        encounter.setClass_(
                new Coding(
                        "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                        "AMB",
                        "Ambulatory"
                )
        );

        LocalDate visitDate =
                LocalDate.parse(dto.getVisitDate(), INPUT_DATE_FORMAT);

        encounter.setPeriod(
                new Period().setStart(
                        Date.from(
                                visitDate.atStartOfDay(
                                        ZoneId.systemDefault()
                                ).toInstant()
                        )
                )
        );

        encounter.addParticipant()
                .getIndividual()
                .setDisplay(dto.getDoctor());

        // ---------------- Observation: Temperature ----------------
        Observation tempObs = new Observation();
        tempObs.setStatus(Observation.ObservationStatus.FINAL);
        tempObs.getCode().setText("Body Temperature");
        tempObs.setValue(
                new Quantity()
                        .setValue(dto.getTemperature())
                        .setUnit("°F")
        );

        // ---------------- Observation: BP ----------------
        Observation bpObs = new Observation();
        bpObs.setStatus(Observation.ObservationStatus.FINAL);
        bpObs.getCode().setText("Blood Pressure");
        bpObs.setValue(new StringType(dto.getBloodPressure()));

        // ---------------- Observation: Symptoms ----------------
        Observation symptomsObs = new Observation();
        symptomsObs.setStatus(Observation.ObservationStatus.FINAL);
        symptomsObs.getCode().setText("Symptoms");
        symptomsObs.setValue(new StringType(dto.getSymptoms()));

        // ---------------- Bundle ----------------
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        bundle.addEntry().setResource(patient);
        bundle.addEntry().setResource(encounter);
        bundle.addEntry().setResource(tempObs);
        bundle.addEntry().setResource(bpObs);
        bundle.addEntry().setResource(symptomsObs);

        return bundle;
    }
}
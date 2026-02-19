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

        encounter.setSubject(
                new Reference("Patient/" + dto.getPatientId())
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

        tempObs.getCode().addCoding(
                new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("8310-5")
                        .setDisplay("Body temperature")
        );

        tempObs.setSubject(
                new Reference("Patient/" + dto.getPatientId())
        );

        tempObs.setValue(
                new Quantity()
                        .setValue(dto.getTemperature())
                        .setUnit("F")
        );

        // ---------------- Observation: BP ----------------
        Observation bpObs = new Observation();
        bpObs.setStatus(Observation.ObservationStatus.FINAL);

        bpObs.getCode().addCoding(
                new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("85354-9")
                        .setDisplay("Blood pressure panel")
        );

        bpObs.setSubject(
                new Reference("Patient/" + dto.getPatientId())
        );

        bpObs.setValue(new StringType(dto.getBloodPressure()));

        // ---------------- Condition (Symptom → SNOMED) ----------------
        Condition condition = new Condition();

        condition.setClinicalStatus(
                new CodeableConcept().addCoding(
                        new Coding()
                                .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                                .setCode("active")
                )
        );

        condition.getCode().addCoding(
                new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("386661006")   // Fever
                        .setDisplay(dto.getSymptoms())
        );

// Link Condition to Patient
        condition.setSubject(
                new Reference("Patient/" + dto.getPatientId())
        );

        // ---------------- Bundle ----------------
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        bundle.addEntry().setResource(patient);
        bundle.addEntry().setResource(encounter);
        bundle.addEntry().setResource(tempObs);
        bundle.addEntry().setResource(bpObs);
        bundle.addEntry().setResource(condition);

        return bundle;
    }
}
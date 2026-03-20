package com.example.fhir_demo.mapper;
import java.util.UUID;

import com.example.fhir_demo.HospitalA.dto.HospitalAOPConsultRecordDTO;
import org.hl7.fhir.r4.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

public class HospitalAOPConsultToFHIRBundleMapper {

    private static final DateTimeFormatter INPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Bundle mapToBundle(HospitalAOPConsultRecordDTO dto) {

        // ---------------- Patient ----------------
        Patient patient = new Patient();
        patient.setId(dto.getPatientId());

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
        encounter.setSubject(new Reference("Patient/" + dto.getPatientId()));

        // ✅ FIX 2: Parse the actual visitDate from DTO (was silently using new Date())
        LocalDate visitDate = LocalDate.parse(dto.getVisitDate(), INPUT_DATE_FORMAT);
        Date visitDateAsDate = Date.from(
                visitDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
        encounter.setPeriod(new Period().setStart(visitDateAsDate));

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
        tempObs.setSubject(new Reference("Patient/" + dto.getPatientId()));
        tempObs.setValue(
                new Quantity()
                        .setValue(dto.getTemperature())
                        .setUnit("degF")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("[degF]")
        );

        // ✅ FIX 1: Blood Pressure as two Observation.component entries (FHIR R4 compliant)
        // A real FHIR server rejects BP stored as a plain string.
        Observation bpObs = new Observation();
        bpObs.setStatus(Observation.ObservationStatus.FINAL);

        // Panel-level code: Blood pressure panel (LOINC 85354-9)
        bpObs.getCode().addCoding(
                new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("85354-9")
                        .setDisplay("Blood pressure panel with all children optional")
        );
        bpObs.setSubject(new Reference("Patient/" + dto.getPatientId()));

        // Parse "120/80" → systolic=120, diastolic=80
        BigDecimal systolic = BigDecimal.ZERO;
        BigDecimal diastolic = BigDecimal.ZERO;
        if (dto.getBloodPressure() != null && dto.getBloodPressure().contains("/")) {
            String[] parts = dto.getBloodPressure().split("/");
            systolic  = new BigDecimal(parts[0].trim());
            diastolic = new BigDecimal(parts[1].trim());
        }

        // Component 1 — Systolic (LOINC 8480-6)
        Observation.ObservationComponentComponent systolicComp =
                new Observation.ObservationComponentComponent();
        systolicComp.getCode().addCoding(
                new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("8480-6")
                        .setDisplay("Systolic blood pressure")
        );
        systolicComp.setValue(
                new Quantity()
                        .setValue(systolic)
                        .setUnit("mmHg")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("mm[Hg]")
        );
        bpObs.addComponent(systolicComp);

        // Component 2 — Diastolic (LOINC 8462-4)
        Observation.ObservationComponentComponent diastolicComp =
                new Observation.ObservationComponentComponent();
        diastolicComp.getCode().addCoding(
                new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("8462-4")
                        .setDisplay("Diastolic blood pressure")
        );
        diastolicComp.setValue(
                new Quantity()
                        .setValue(diastolic)
                        .setUnit("mmHg")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("mm[Hg]")
        );
        bpObs.addComponent(diastolicComp);

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
                        .setCode("386661006")
                        .setDisplay(dto.getSymptoms())
        );
        condition.setSubject(new Reference("Patient/" + dto.getPatientId()));

        // ---------------- DocumentReference (PDF) ----------------
        DocumentReference docRef = new DocumentReference();
        docRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

        CodeableConcept type = new CodeableConcept();
        type.addCoding()
                .setSystem("http://loinc.org")
                .setCode("60591-5")
                .setDisplay("Prescription Document");
        docRef.setType(type);

        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        attachment.setData(
                Base64.getDecoder().decode(dto.getPrescriptionPdfBase64())
        );
        docRef.addContent().setAttachment(attachment);

        // ---------------- Bundle ----------------
        String patientUuid  = UUID.randomUUID().toString();
        String encounterUuid = UUID.randomUUID().toString();
        String tempObsUuid  = UUID.randomUUID().toString();
        String bpObsUuid    = UUID.randomUUID().toString();
        String conditionUuid = UUID.randomUUID().toString();
        String docRefUuid   = UUID.randomUUID().toString();

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        bundle.addEntry()
                .setFullUrl("urn:uuid:" + patientUuid)
                .setResource(patient);

        bundle.addEntry()
                .setFullUrl("urn:uuid:" + encounterUuid)
                .setResource(encounter);

        bundle.addEntry()
                .setFullUrl("urn:uuid:" + tempObsUuid)
                .setResource(tempObs);

        bundle.addEntry()
                .setFullUrl("urn:uuid:" + bpObsUuid)
                .setResource(bpObs);

        bundle.addEntry()
                .setFullUrl("urn:uuid:" + conditionUuid)
                .setResource(condition);

        bundle.addEntry()
                .setFullUrl("urn:uuid:" + docRefUuid)
                .setResource(docRef);

        return bundle;
    }
}
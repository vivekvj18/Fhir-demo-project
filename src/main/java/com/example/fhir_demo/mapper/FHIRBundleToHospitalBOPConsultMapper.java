package com.example.fhir_demo.mapper;

import com.example.fhir_demo.HospitalB.dto.HospitalBOPConsultRecordDTO;
import org.hl7.fhir.r4.model.*;

import java.util.Base64;

public class FHIRBundleToHospitalBOPConsultMapper {

    public static HospitalBOPConsultRecordDTO map(Bundle bundle) {

        HospitalBOPConsultRecordDTO dto = new HospitalBOPConsultRecordDTO();
        HospitalBOPConsultRecordDTO.Vitals vitals = new HospitalBOPConsultRecordDTO.Vitals();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

            // -------- Patient --------
            if (entry.getResource() instanceof Patient patient) {
                dto.setUhid(patient.getId());
                if (patient.hasName() && patient.getNameFirstRep().hasText()) {
                    dto.setPatientName(patient.getNameFirstRep().getText());
                }
            }

            // -------- Encounter --------
            else if (entry.getResource() instanceof Encounter encounter) {
                if (encounter.hasPeriod() && encounter.getPeriod().hasStart()) {
                    dto.setConsultDate(encounter.getPeriod().getStart().toString());
                }
                if (encounter.hasParticipant()
                        && encounter.getParticipantFirstRep().hasIndividual()) {
                    dto.setDoctor(
                            encounter.getParticipantFirstRep().getIndividual().getDisplay()
                    );
                }
            }

            // -------- Observations --------
            else if (entry.getResource() instanceof Observation obs) {
                if (!obs.hasCode() || !obs.getCode().hasCoding()) continue;

                String loincCode = obs.getCode().getCodingFirstRep().getCode();

                // Body Temperature (LOINC 8310-5)
                if ("8310-5".equals(loincCode) && obs.hasValueQuantity()) {
                    vitals.setTemp(
                            obs.getValueQuantity().getValue().toPlainString()
                    );
                }

                // ✅ FIX 1: Blood Pressure Panel (LOINC 85354-9) — read from components
                // The old code tried to read a StringType value which no longer exists.
                else if ("85354-9".equals(loincCode) && obs.hasComponent()) {
                    String systolic  = null;
                    String diastolic = null;

                    for (Observation.ObservationComponentComponent comp : obs.getComponent()) {
                        if (!comp.hasCode() || !comp.getCode().hasCoding()) continue;
                        String compCode = comp.getCode().getCodingFirstRep().getCode();

                        if ("8480-6".equals(compCode) && comp.hasValueQuantity()) {
                            systolic = comp.getValueQuantity().getValue().toPlainString();
                        } else if ("8462-4".equals(compCode) && comp.hasValueQuantity()) {
                            diastolic = comp.getValueQuantity().getValue().toPlainString();
                        }
                    }

                    // Reassemble as "120/80" for Hospital-B's internal format
                    if (systolic != null && diastolic != null) {
                        vitals.setBp(systolic + "/" + diastolic);
                    }
                }
            }

            // -------- Condition --------
            else if (entry.getResource() instanceof Condition condition) {
                if (condition.hasCode() && condition.getCode().hasCoding()) {
                    String snomedCode = condition.getCode().getCodingFirstRep().getCode();
                    if ("386661006".equals(snomedCode)) {
                        dto.setClinicalNotes("Fever");
                    }
                }
            }

            // -------- DocumentReference --------
            else if (entry.getResource() instanceof DocumentReference docRef) {
                byte[] pdfData = docRef.getContentFirstRep()
                        .getAttachment()
                        .getData();
                dto.setPrescriptionPdfBase64(Base64.getEncoder().encodeToString(pdfData));
            }
        }

        dto.setVitals(vitals);
        return dto;
    }
}
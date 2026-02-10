package com.example.fhir_demo.mapper;

import com.example.fhir_demo.HospitalB.dto.HospitalBOPConsultRecordDTO;
import org.hl7.fhir.r4.model.*;

public class FHIRBundleToHospitalBOPConsultMapper {

    public static HospitalBOPConsultRecordDTO map(Bundle bundle) {

        HospitalBOPConsultRecordDTO dto =
                new HospitalBOPConsultRecordDTO();
        HospitalBOPConsultRecordDTO.Vitals vitals =
                new HospitalBOPConsultRecordDTO.Vitals();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

            // -------- Patient --------
            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();

                dto.setUhid(patient.getId());

                if (patient.hasName()
                        && patient.getNameFirstRep().hasText()) {
                    dto.setPatientName(
                            patient.getNameFirstRep().getText()
                    );
                }
            }

            // -------- Encounter --------
            else if (entry.getResource() instanceof Encounter) {
                Encounter encounter = (Encounter) entry.getResource();

                if (encounter.hasPeriod()
                        && encounter.getPeriod().hasStart()) {
                    dto.setConsultDate(
                            encounter.getPeriod()
                                    .getStart()
                                    .toString()
                    );
                }

                if (encounter.hasParticipant()
                        && encounter.getParticipantFirstRep()
                        .hasIndividual()) {
                    dto.setDoctor(
                            encounter.getParticipantFirstRep()
                                    .getIndividual()
                                    .getDisplay()
                    );
                }
            }

            // -------- Observations --------
            else if (entry.getResource() instanceof Observation) {
                Observation obs = (Observation) entry.getResource();
                String code = obs.getCode().getText();

                if ("Blood Pressure".equalsIgnoreCase(code)) {
                    vitals.setBp(
                            obs.getValueStringType().getValue()
                    );
                }
                else if ("Body Temperature"
                        .equalsIgnoreCase(code)
                        && obs.hasValueQuantity()) {
                    vitals.setTemp(
                            obs.getValueQuantity()
                                    .getValue()
                                    .toString()
                    );
                }
                else if ("Symptoms".equalsIgnoreCase(code)) {
                    dto.setClinicalNotes(
                            obs.getValueStringType().getValue()
                    );
                }
            }
        }

        dto.setVitals(vitals);
        return dto;
    }
}
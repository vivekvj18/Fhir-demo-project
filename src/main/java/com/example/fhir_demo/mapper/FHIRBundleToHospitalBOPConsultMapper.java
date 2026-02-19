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

                if (obs.hasCode() && obs.getCode().hasCoding()) {

                    String loincCode = obs.getCode()
                            .getCodingFirstRep()
                            .getCode();

                    // Body Temperature (LOINC 8310-5)
                    if ("8310-5".equals(loincCode)
                            && obs.hasValueQuantity()) {

                        vitals.setTemp(
                                obs.getValueQuantity()
                                        .getValue()
                                        .toString()
                        );
                    }

                    // Blood Pressure Panel (LOINC 85354-9)
                    else if ("85354-9".equals(loincCode)
                            && obs.hasValueStringType()) {

                        vitals.setBp(
                                obs.getValueStringType().getValue()
                        );
                    }
                }
            }

            else if (entry.getResource() instanceof Condition) {

                Condition condition = (Condition) entry.getResource();

                if (condition.hasCode()
                        && condition.getCode().hasCoding()) {

                    String snomedCode = condition.getCode()
                            .getCodingFirstRep()
                            .getCode();

                    // Fever (SNOMED 386661006)
                    if ("386661006".equals(snomedCode)) {
                        dto.setClinicalNotes("Fever");
                    }
                }
            }
        }

        dto.setVitals(vitals);
        return dto;
    }
}
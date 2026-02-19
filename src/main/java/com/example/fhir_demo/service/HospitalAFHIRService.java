package com.example.fhir_demo.service;

import ca.uhn.fhir.context.FhirContext;
import com.example.fhir_demo.HospitalA.dto.HospitalAOPConsultRecordDTO;
import com.example.fhir_demo.HospitalA.model.HospitalAPatient;
import com.example.fhir_demo.mapper.HospitalAOPConsultToFHIRBundleMapper;
import com.example.fhir_demo.mapper.HospitalAToFHIRPatientMapper;
import com.example.fhir_demo.util.FHIRValidatorUtil;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class HospitalAFHIRService {

    private final FhirContext fhirContext = FhirContext.forR4();

    // ===============================
    // 🔹 EXISTING PATIENT LOGIC
    // ===============================
    public String convertAndValidate(HospitalAPatient hospitalPatient) {

        // 1️⃣ Convert Hospital-A → FHIR Patient
        Patient fhirPatient =
                HospitalAToFHIRPatientMapper.mapToFHIRPatient(hospitalPatient);

        // 2️⃣ Validate FHIR Patient
        String validationResult =
                FHIRValidatorUtil.validateResource(fhirPatient);

        if (!validationResult.contains("Successful")) {
            return validationResult;
        }

        // 3️⃣ Return JSON
        return fhirContext
                .newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(fhirPatient);
    }

    // ===============================
    // 🔥 NEW OP-CONSULT LOGIC
    // ===============================
    public String convertOPConsultAndValidate(HospitalAOPConsultRecordDTO dto) {

        // 1️⃣ Convert DTO → FHIR Bundle
        Bundle bundle =
                HospitalAOPConsultToFHIRBundleMapper.mapToBundle(dto);

        // 2️⃣ Validate Bundle (HL7 Validation)
        String validationResult =
                FHIRValidatorUtil.validateResource(bundle);

        if (!validationResult.contains("Successful")) {
            return validationResult;
        }

        // 3️⃣ Return FHIR Bundle JSON
        return fhirContext
                .newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(bundle);
    }
}
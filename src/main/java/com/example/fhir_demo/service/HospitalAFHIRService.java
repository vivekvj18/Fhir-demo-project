package com.example.fhir_demo.service;

import ca.uhn.fhir.context.FhirContext;
import com.example.fhir_demo.HospitalA.model.HospitalAPatient;
import com.example.fhir_demo.mapper.HospitalAToFHIRPatientMapper;
import com.example.fhir_demo.util.FHIRValidatorUtil;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class HospitalAFHIRService {

    private final FhirContext fhirContext = FhirContext.forR4();

    public String convertAndValidate(HospitalAPatient hospitalPatient) {

        // Convert Hospital-A → FHIR
        Patient fhirPatient =
                HospitalAToFHIRPatientMapper.mapToFHIRPatient(hospitalPatient);

        // Validate FHIR
        FHIRValidatorUtil.validatePatient(fhirPatient);

        // Convert FHIR object → JSON string
        return fhirContext
                .newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(fhirPatient);
    }
}

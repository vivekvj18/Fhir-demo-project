package com.example.fhir_demo.HospitalB;

import ca.uhn.fhir.context.FhirContext;
import com.example.fhir_demo.HospitalB.dto.HospitalBOPConsultRecordDTO;
import com.example.fhir_demo.HospitalB.model.HospitalBPatient;
import com.example.fhir_demo.mapper.FHIRBundleToHospitalBOPConsultMapper;
import com.example.fhir_demo.service.HospitalBFHIRService;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospitalB")
public class HospitalBController {

    private final HospitalBFHIRService service;
    private final FhirContext fhirContext = FhirContext.forR4();

    public HospitalBController(HospitalBFHIRService service) {
        this.service = service;
    }

    @PostMapping("/patient")
    public HospitalBPatient receivePatientFromFHIR(@RequestBody String fhirJson) {

        // Convert JSON → FHIR Patient
        Patient patient = (Patient) fhirContext
                .newJsonParser()
                .parseResource(fhirJson);

        // Convert FHIR → Hospital-B format
        return service.convertFromFHIR(patient);
    }

    @PostMapping("/op-consult")
    public HospitalBOPConsultRecordDTO receiveOPConsultFromFHIR(
            @RequestBody String fhirJson) {

        Bundle bundle = FhirContext.forR4()
                .newJsonParser()
                .parseResource(Bundle.class, fhirJson);

        return FHIRBundleToHospitalBOPConsultMapper.map(bundle);
    }
}

package com.example.fhir_demo.HospitalA;

import com.example.fhir_demo.HospitalA.dto.HospitalAOPConsultRecordDTO;
import com.example.fhir_demo.HospitalA.model.HospitalAPatient;
import com.example.fhir_demo.service.HospitalAFHIRService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospitalA")
public class HospitalAController {

    private final HospitalAFHIRService service;

    public HospitalAController(HospitalAFHIRService service) {
        this.service = service;
    }

    // ===============================
    // 🔹 Patient → FHIR
    // ===============================
    @PostMapping("/patient/fhir")
    public String convertToFHIR(@RequestBody HospitalAPatient patient) {
        return service.convertAndValidate(patient);
    }

    // ===============================
    // 🔥 OP Consult → FHIR + Validate
    // ===============================
    @PostMapping("/op-consult")
    public String receiveOPConsult(
            @RequestBody HospitalAOPConsultRecordDTO consultRecord) {

        return service.convertOPConsultAndValidate(consultRecord);
    }
}
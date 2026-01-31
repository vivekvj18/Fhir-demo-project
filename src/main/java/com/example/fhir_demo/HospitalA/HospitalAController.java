package com.example.fhir_demo.HospitalA;

import com.example.fhir_demo.HospitalA.model.HospitalAPatient;
import com.example.fhir_demo.service.HospitalAFHIRService;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospitalA")
public class HospitalAController {

    private final HospitalAFHIRService service;

    public HospitalAController(HospitalAFHIRService service) {
        this.service = service;
    }

    @PostMapping("/patient/fhir")
    public String convertToFHIR(@RequestBody HospitalAPatient patient) {
        return service.convertAndValidate(patient);
    }

}

package com.example.fhir_demo.HospitalA;

import com.example.fhir_demo.HospitalA.dto.HospitalAOPConsultRecordDTO;
import com.example.fhir_demo.mapper.HospitalAOPConsultToFHIRBundleMapper;
import com.example.fhir_demo.util.FHIRJsonUtil;
import com.example.fhir_demo.util.FHIRValidatorUtil;
import org.hl7.fhir.r4.model.Bundle;
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

    @PostMapping("/op-consult")
    public String receiveOPConsult(
            @RequestBody HospitalAOPConsultRecordDTO consultRecord) {

        // 1️⃣ Convert to FHIR Bundle
        Bundle bundle =
                HospitalAOPConsultToFHIRBundleMapper.mapToBundle(consultRecord);

        // 2️⃣ Validate Bundle
        FHIRValidatorUtil.validateResource(bundle);

        // 3️⃣ Return clean FHIR JSON
        return FHIRJsonUtil.toJson(bundle);
    }

}

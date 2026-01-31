package com.example.fhir_demo.service;

import com.example.fhir_demo.HospitalB.model.HospitalBPatient;
import com.example.fhir_demo.mapper.FHIRToHospitalBPatientMapper;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class HospitalBFHIRService {

    public HospitalBPatient convertFromFHIR(Patient fhirPatient) {
        return FHIRToHospitalBPatientMapper.mapToHospitalB(fhirPatient);
    }
}

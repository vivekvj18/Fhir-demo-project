package com.example.fhir_demo.consent;

import lombok.Data;

@Data
public class ConsentRequest {

    /** Must match the patientId used in HospitalAOPConsultRecordDTO */
    private String patientId;

    /** Target hospital identifier, e.g. "HospitalB" */
    private String targetHospital;

    /** Patient's decision — GRANTED or DENIED */
    private ConsentStatus decision;
}
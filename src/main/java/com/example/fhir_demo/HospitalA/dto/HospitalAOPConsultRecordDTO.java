package com.example.fhir_demo.HospitalA.dto;

import lombok.Data;

@Data
public class HospitalAOPConsultRecordDTO {

    private String patientId;       // Hospital-A patient ID
    private String patientName;     // Patient Name
    private String doctor;          // Consulting doctor
    private String visitDate;       // dd/MM/yyyy (Hospital-A format)
    private String symptoms;        // Clinical notes
    private Double temperature;     // Body temperature
    private String bloodPressure;
    private String prescriptionPdfBase64;

}
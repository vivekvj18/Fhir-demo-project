package com.example.fhir_demo.HospitalB.dto;

import lombok.Data;

@Data
public class HospitalBOPConsultRecordDTO {

    private String uhid;          // Hospital-B patient id
    private String patientName;
    private String consultDate;
    private String doctor;
    private String clinicalNotes;

    private Vitals vitals;

    @Data
    public static class Vitals {
        private String bp;
        private String temp;
    }
}
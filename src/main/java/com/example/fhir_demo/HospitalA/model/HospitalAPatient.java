package com.example.fhir_demo.HospitalA.model;

public class HospitalAPatient {

    private String patientId;   // Local ID (not FHIR)
    private String name;        // Flat name
    private String dob;         // dd/MM/yyyy (NON-FHIR)
    private String gender;      // M / F (NON-FHIR)

    public HospitalAPatient() {}

    public HospitalAPatient(String patientId, String name, String dob, String gender) {
        this.patientId = patientId;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

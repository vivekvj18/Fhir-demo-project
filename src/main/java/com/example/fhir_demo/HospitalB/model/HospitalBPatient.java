package com.example.fhir_demo.HospitalB.model;

public class HospitalBPatient {

    private String uhid;          // Hospital-B local ID
    private String fullName;      // Different field name
    private String dateOfBirth;   // "19th Aug 2003"
    private String gender;        // Male / Female

    public HospitalBPatient() {}

    public HospitalBPatient(String uhid, String fullName,
                            String dateOfBirth, String gender) {
        this.uhid = uhid;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}


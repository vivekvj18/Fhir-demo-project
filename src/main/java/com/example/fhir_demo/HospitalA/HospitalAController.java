package com.example.fhir_demo.HospitalA;

import com.example.fhir_demo.HospitalA.dto.HospitalAOPConsultRecordDTO;
import com.example.fhir_demo.HospitalA.model.HospitalAPatient;
import com.example.fhir_demo.consent.ConsentService;
import com.example.fhir_demo.consent.ConsentStatus;
import com.example.fhir_demo.service.HospitalAFHIRService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospitalA")
public class HospitalAController {

    private final HospitalAFHIRService service;
    private final ConsentService consentService;

    public HospitalAController(HospitalAFHIRService service,
                               ConsentService consentService) {
        this.service = service;
        this.consentService = consentService;
    }

    // =========================================================
    // 🔹 Patient → FHIR  (unchanged)
    // =========================================================
    @PostMapping("/patient/fhir")
    public String convertToFHIR(@RequestBody HospitalAPatient patient) {
        return service.convertAndValidate(patient);
    }

    // =========================================================
    // 🔥 OP Consult → FHIR + Validate  (unchanged, no gate)
    // =========================================================
    @PostMapping("/op-consult")
    public String receiveOPConsult(
            @RequestBody HospitalAOPConsultRecordDTO consultRecord) {
        return service.convertOPConsultAndValidate(consultRecord);
    }

    // =========================================================
    // 🔐 Transfer with Consent Gate
    //
    // POST /hospitalA/transfer
    //
    // Flow:
    //   1. Check consent for patientId → HospitalB
    //   2. GRANTED  → build FHIR Bundle, return 200
    //   3. DENIED   → return 403 BLOCKED
    //   4. PENDING  → return 403 (no decision registered yet)
    // =========================================================
    @PostMapping("/transfer")
    public ResponseEntity<String> transferWithConsentGate(
            @RequestBody HospitalAOPConsultRecordDTO consultRecord,
            @RequestParam(defaultValue = "HospitalB") String targetHospital) {

        String patientId = consultRecord.getPatientId();

        // ── Step 1: Check consent ──────────────────────────────────────
        ConsentStatus status = consentService.checkConsent(patientId, targetHospital);

        // ── Step 2: Gate ───────────────────────────────────────────────
        if (status == ConsentStatus.DENIED) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("🚫 Transfer BLOCKED — Patient " + patientId
                            + " has DENIED consent for data transfer to "
                            + targetHospital + ".");
        }

        if (status == ConsentStatus.PENDING) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("⏳ Transfer BLOCKED — No consent decision registered yet"
                            + " for Patient " + patientId
                            + " → " + targetHospital
                            + ". Ask the patient to register consent at"
                            + " POST /consent/register first.");
        }

        // ── Step 3: GRANTED — build and return FHIR Bundle ────────────
        String fhirBundle = service.convertOPConsultAndValidate(consultRecord);

        return ResponseEntity.ok(
                "✅ Consent GRANTED — Transfer approved for Patient "
                        + patientId + " → " + targetHospital + "\n\n"
                        + fhirBundle
        );
    }
}
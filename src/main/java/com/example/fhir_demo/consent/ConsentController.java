package com.example.fhir_demo.consent;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consent")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    /**
     * Patient registers (or updates) their consent decision.
     *
     * POST /consent/register
     * {
     *   "patientId":      "P001",
     *   "targetHospital": "HospitalB",
     *   "decision":       "GRANTED"   ← or "DENIED"
     * }
     *
     * Returns the FHIR Consent resource JSON that was stored.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerConsent(
            @RequestBody ConsentRequest request) {

        if (request.getDecision() == null) {
            return ResponseEntity.badRequest()
                    .body("decision must be GRANTED or DENIED");
        }

        if (request.getDecision() == ConsentStatus.PENDING) {
            return ResponseEntity.badRequest()
                    .body("PENDING is not a valid decision. Use GRANTED or DENIED.");
        }

        String fhirConsentJson = consentService.registerConsent(request);
        return ResponseEntity.ok(fhirConsentJson);
    }

    /**
     * Check current consent status for a patient → target hospital.
     *
     * GET /consent/status/P001?targetHospital=HospitalB
     */
    @GetMapping("/status/{patientId}")
    public ResponseEntity<String> getConsentStatus(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "HospitalB") String targetHospital) {

        ConsentStatus status = consentService.checkConsent(patientId, targetHospital);
        return ResponseEntity.ok(
                "{ \"patientId\": \"" + patientId
                        + "\", \"targetHospital\": \"" + targetHospital
                        + "\", \"status\": \"" + status + "\" }"
        );
    }

    /**
     * Return the full FHIR Consent resource JSON for inspection / audit.
     *
     * GET /consent/resource/P001?targetHospital=HospitalB
     */
    @GetMapping("/resource/{patientId}")
    public ResponseEntity<String> getConsentResource(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "HospitalB") String targetHospital) {

        return ResponseEntity.ok(
                consentService.getConsentResourceJson(patientId, targetHospital)
        );
    }
}
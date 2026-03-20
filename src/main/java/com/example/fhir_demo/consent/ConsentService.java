package com.example.fhir_demo.consent;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages FHIR Consent resources for patient data-transfer decisions.
 *
 * Key format: "patientId::targetHospital"
 * e.g.        "P001::HospitalB"
 *
 * Each registered decision is stored as a proper FHIR R4 Consent resource
 * so it can be inspected, serialised, or later persisted to a FHIR server.
 */
@Service
public class ConsentService {

    private static final FhirContext FHIR_CTX = FhirContext.forR4();

    // In-memory store: key → FHIR Consent resource
    private final Map<String, Consent> consentStore = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------ //
    //  Register / update a consent decision                               //
    // ------------------------------------------------------------------ //

    public String registerConsent(ConsentRequest request) {

        Consent consent = buildFhirConsent(request);
        String key = toKey(request.getPatientId(), request.getTargetHospital());
        consentStore.put(key, consent);

        return FHIR_CTX.newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(consent);
    }

    // ------------------------------------------------------------------ //
    //  Check consent status                                               //
    // ------------------------------------------------------------------ //

    /**
     * Returns GRANTED / DENIED / PENDING.
     * PENDING means no record exists yet — no consent was ever registered.
     */
    public ConsentStatus checkConsent(String patientId, String targetHospital) {
        String key = toKey(patientId, targetHospital);

        if (!consentStore.containsKey(key)) {
            return ConsentStatus.PENDING;   // No decision registered yet
        }

        Consent consent = consentStore.get(key);

        // Rejected consent state → DENIED
        if (consent.getStatus() == Consent.ConsentState.REJECTED) {
            return ConsentStatus.DENIED;
        }

        // Active consent + PERMIT provision → GRANTED
        if (consent.getStatus() == Consent.ConsentState.ACTIVE
                && consent.hasProvision()
                && consent.getProvision().getType()
                == Consent.ConsentProvisionType.PERMIT) {
            return ConsentStatus.GRANTED;
        }

        return ConsentStatus.DENIED;
    }

    // ------------------------------------------------------------------ //
    //  Return the raw FHIR Consent JSON (useful for audit / debugging)   //
    // ------------------------------------------------------------------ //

    public String getConsentResourceJson(String patientId, String targetHospital) {
        String key = toKey(patientId, targetHospital);
        Consent consent = consentStore.get(key);

        if (consent == null) {
            return "{ \"message\": \"No consent record found for patient "
                    + patientId + " → " + targetHospital + "\" }";
        }

        return FHIR_CTX.newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(consent);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                    //
    // ------------------------------------------------------------------ //

    private Consent buildFhirConsent(ConsentRequest request) {

        Consent consent = new Consent();

        // ── Status ──────────────────────────────────────────────────────
        // ACTIVE  = the consent is in force (could be permit OR deny)
        // REJECTED = patient explicitly withdrew / denied
        if (request.getDecision() == ConsentStatus.DENIED) {
            consent.setStatus(Consent.ConsentState.REJECTED);
        } else {
            consent.setStatus(Consent.ConsentState.ACTIVE);
        }

        // ── Scope: patient-privacy ───────────────────────────────────────
        consent.setScope(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/consentscope")
                        .setCode("patient-privacy")
                        .setDisplay("Privacy Consent")
        ));

        // ── Category: Patient Consent (LOINC 59284-0) ───────────────────
        consent.addCategory(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("59284-0")
                        .setDisplay("Patient Consent")
        ));

        // ── Patient reference ────────────────────────────────────────────
        consent.setPatient(new Reference("Patient/" + request.getPatientId()));

        // ── Date of decision ─────────────────────────────────────────────
        consent.setDateTime(new Date());

        // ── Organisation receiving the data (Hospital B) ─────────────────
        consent.addOrganization(
                new Reference().setDisplay(request.getTargetHospital())
        );

        // ── Provision: PERMIT or DENY ────────────────────────────────────
        Consent.provisionComponent provision = new Consent.provisionComponent();

        if (request.getDecision() == ConsentStatus.GRANTED) {
            provision.setType(Consent.ConsentProvisionType.PERMIT);
        } else {
            provision.setType(Consent.ConsentProvisionType.DENY);
        }

        consent.setProvision(provision);

        return consent;
    }

    private String toKey(String patientId, String targetHospital) {
        return patientId + "::" + targetHospital;
    }
}
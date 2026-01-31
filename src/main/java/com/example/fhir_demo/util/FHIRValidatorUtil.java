package com.example.fhir_demo.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleValidationMessage;
import org.hl7.fhir.r4.model.Patient;

public class FHIRValidatorUtil {

    private static final FhirContext fhirContext = FhirContext.forR4();

    public static void validatePatient(Patient patient) {

        FhirValidator validator = fhirContext.newValidator();
        ValidationResult result = validator.validateWithResult(patient);

        if (result.isSuccessful()) {
            System.out.println("FHIR Patient is VALID ✅");
        } else {
            System.out.println("FHIR Patient is INVALID ❌");
            for (SingleValidationMessage message : result.getMessages()) {
                System.out.println(
                        message.getSeverity() + " : " + message.getMessage()
                );
            }
        }
    }
}


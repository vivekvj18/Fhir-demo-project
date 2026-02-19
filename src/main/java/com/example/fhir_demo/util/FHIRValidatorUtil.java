package com.example.fhir_demo.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.Resource;

public class FHIRValidatorUtil {

    private static final FhirContext ctx = FhirContext.forR4();
    private static final FhirValidator validator = ctx.newValidator();

    public static String validateResource(Resource resource) {

        ValidationResult result =
                validator.validateWithResult(resource);

        if (result.isSuccessful()) {
            return "FHIR Validation Successful ✅";
        }

        StringBuilder errors = new StringBuilder();

        result.getMessages().forEach(msg ->
                errors.append(msg.getSeverity())
                        .append(" - ")
                        .append(msg.getMessage())
                        .append("\n")
        );

        return errors.toString();
    }
}
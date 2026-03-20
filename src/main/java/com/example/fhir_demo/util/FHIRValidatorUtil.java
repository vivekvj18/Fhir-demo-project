package com.example.fhir_demo.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;

public class FHIRValidatorUtil {

    private static final FhirContext CTX = FhirContext.forR4();

    // ✅ FIX 3: Build a proper validation support chain and register FhirInstanceValidator.
    // The old code created a bare FhirValidator with no modules, so it only checked
    // whether the JSON was parseable — not whether it conformed to R4 rules.
    private static final FhirValidator VALIDATOR;

    static {
        // 1. Core R4 structure definitions (StructureDefinition, ValueSet, CodeSystem)
        DefaultProfileValidationSupport coreSupport =
                new DefaultProfileValidationSupport(CTX);

        // 2. In-memory terminology server — resolves ValueSet bindings locally
        InMemoryTerminologyServerValidationSupport inMemoryTerminology =
                new InMemoryTerminologyServerValidationSupport(CTX);

        // 3. Built-in code systems (LOINC answers, HL7 v2/v3 tables, etc.)
        CommonCodeSystemsTerminologyService commonCodeSystems =
                new CommonCodeSystemsTerminologyService(CTX);

        // 4. Chain them in order of priority
        ValidationSupportChain supportChain = new ValidationSupportChain(
                coreSupport,
                inMemoryTerminology,
                commonCodeSystems
        );

        // 5. Create the instance validator module and configure it
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(supportChain);

        // Warn (not error) on unknown extensions so vendor extensions don't block you
        instanceValidator.setNoExtensibleWarnings(true);

        // Tolerate unknown profiles — useful while India ABDM profiles are not loaded
        instanceValidator.setAnyExtensionsAllowed(true);

        // 6. Register the module with HAPI's validator
        FhirValidator validator = CTX.newValidator();
        validator.registerValidatorModule(instanceValidator);

        VALIDATOR = validator;
    }

    public static String validateResource(Resource resource) {
        ValidationResult result = VALIDATOR.validateWithResult(resource);

        if (result.isSuccessful()) {
            return "FHIR Validation Successful ✅";
        }

        StringBuilder errors = new StringBuilder();
        result.getMessages().forEach(msg ->
                errors.append("[")
                        .append(msg.getSeverity())
                        .append("] ")
                        .append(msg.getLocationString())
                        .append(" — ")
                        .append(msg.getMessage())
                        .append("\n")
        );
        return errors.toString();
    }
}
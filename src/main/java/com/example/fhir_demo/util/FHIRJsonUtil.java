package com.example.fhir_demo.util;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Resource;

public class FHIRJsonUtil {

    private static final FhirContext fhirContext = FhirContext.forR4();

    public static String toJson(Resource resource) {
        return fhirContext
                .newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(resource);
    }
}

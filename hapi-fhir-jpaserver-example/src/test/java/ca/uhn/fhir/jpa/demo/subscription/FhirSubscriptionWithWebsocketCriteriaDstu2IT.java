/*
 *  Copyright 2016 Cognitive Medical Systems, Inc (http://www.cognitivemedicine.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.uhn.fhir.jpa.demo.subscription;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.server.EncodingEnum;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.hl7.fhir.instance.model.api.IBaseCoding;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

import java.net.URI;

/**
 * Adds a FHIR subscription by creating a websocket that includes the subscription criteria.  The server will create
 * a subscription automatically and return the subscription id
 * <p>
 * 1. Execute the 'createPatient' test
 * 2. Update the patient id in the 'attachWebSocket' and the 'sendObservation' tests
 * 3. Execute the 'attachWebSocket' test
 * 4. Execute the 'sendObservation' test
 * 5. Look in the 'attachWebSocket' terminal execution and wait for your JSON/XML response
 */
public class FhirSubscriptionWithWebsocketCriteriaDstu2IT {

    public final static String PORT = "8080";
    public final static String WEBSOCKET_PATH = "/websocket/dstu2";

    /**
     * Attach a websocket to the FHIR server based on a criteria
     *
     * @throws Exception
     */
    @Test
    public void attachWebSocket() throws Exception {
        String criteria = "Observation?code=SNOMED-CT|82313006&_format=xml";

        SocketImplementation socket = new SocketImplementation(criteria, EncodingEnum.JSON);
        WebSocketClient client = new WebSocketClient();

        try {
            client.start();
            URI echoUri = new URI("ws://localhost:" + PORT + WEBSOCKET_PATH);
            client.connect(socket, echoUri);

            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            Thread.sleep(60000);
            socket.keepAlive();
        }
    }

    /**
     * Create a patient in the FHIR server
     *
     * @throws Exception
     */
    @Test
    public void createPatient() throws Exception {
        IGenericClient client = FhirServiceUtil.getFhirDstu2Client();

        Patient patient = FhirDstu2Util.getPatient();
        MethodOutcome methodOutcome = client.create().resource(patient).execute();
        String id = methodOutcome.getId().getIdPart();
        patient.setId(id);

        System.out.println("Patient id generated by server is: " + id);
    }

    /**
     * Create an observation in the FHIR server
     */
    @Test
    public void createObservation() {
        //String patientId = "1";

        IGenericClient client = FhirServiceUtil.getFhirDstu2Client();

        //IQuery iquery = client.search().forResource(Patient.class);
        //iquery.where(new StringClientParam("_id").matches().value(patientId));

        //Bundle bundle = (Bundle)iquery.returnBundle(Bundle.class).execute();
        //Patient patientRef = (Patient)bundle.getEntry().get(0).getResource();

        Observation observation = new Observation();
        //observation.getSubject().setReference(patientRef.getId());
        observation.setStatus(ObservationStatusEnum.FINAL);
        CodeableConceptDt codeableConcept = new CodeableConceptDt();
        observation.setCode(codeableConcept);
        CodingDt coding = codeableConcept.addCoding();
        coding.setCode("82313006");
        coding.setSystem("SNOMED-CT");

        MethodOutcome methodOutcome2 = client.create().resource(observation).execute();
        String observationId = methodOutcome2.getId().getIdPart();
        observation.setId(observationId);

        System.out.println("Observation id generated by server is: " + observationId);
    }
}

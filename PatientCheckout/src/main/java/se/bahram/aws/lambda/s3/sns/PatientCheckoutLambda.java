package se.bahram.aws.lambda.s3.sns;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PatientCheckoutLambda {

    private static final String PATIENT_CHECKOUT_TOPIC = System.getenv("PATIENT_CHECKOUT_TOPIC");
    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();

    public void handler(S3Event s3Event) {
        s3Event.getRecords().forEach(s3EventNotificationRecord -> {
            S3ObjectInputStream s3ObjectContent = s3.getObject(
                            s3EventNotificationRecord.getS3().getBucket().getName(),
                            s3EventNotificationRecord.getS3().getObject().getKey()
                    )
                    .getObjectContent();
            // read data
            try {
                List<PatientCheckoutEvent> patientCheckoutEvents =
                        Arrays.asList(
                                objectMapper.readValue(s3ObjectContent, PatientCheckoutEvent[].class)
                        );
                System.out.println(patientCheckoutEvents);
                s3ObjectContent.close();
                publishMessageToSNS(patientCheckoutEvents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void publishMessageToSNS(List<PatientCheckoutEvent> patientCheckoutEvents) {
        patientCheckoutEvents.forEach(patientCheckoutEvent -> {
            try {
                sns.publish(
                        PATIENT_CHECKOUT_TOPIC,
                        objectMapper.writeValueAsString(patientCheckoutEvent)
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }
}

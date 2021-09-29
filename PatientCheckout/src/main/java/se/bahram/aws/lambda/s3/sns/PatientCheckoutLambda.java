package se.bahram.aws.lambda.s3.sns;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PatientCheckoutLambda {

    AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    ObjectMapper objectMapper = new ObjectMapper();

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

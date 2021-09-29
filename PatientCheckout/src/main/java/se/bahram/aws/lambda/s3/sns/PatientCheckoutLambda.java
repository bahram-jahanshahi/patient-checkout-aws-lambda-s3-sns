package se.bahram.aws.lambda.s3.sns;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class PatientCheckoutLambda {

    private static final String PATIENT_CHECKOUT_TOPIC = System.getenv("PATIENT_CHECKOUT_TOPIC");
    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();

    public void handler(S3Event s3Event, Context context) {
        // LambdaLogger logger = context.getLogger();
        Logger logger = LoggerFactory.getLogger(PatientCheckoutLambda.class);
        s3Event.getRecords().forEach(s3EventNotificationRecord -> {
            S3ObjectInputStream s3ObjectContent = s3.getObject(
                            s3EventNotificationRecord.getS3().getBucket().getName(),
                            s3EventNotificationRecord.getS3().getObject().getKey()
                    )
                    .getObjectContent();
            // read data
            try {
                logger.info("Reading data from S3.");
                List<PatientCheckoutEvent> patientCheckoutEvents =
                        Arrays.asList(
                                objectMapper.readValue(s3ObjectContent, PatientCheckoutEvent[].class)
                        );
                logger.info(patientCheckoutEvents.toString());
                s3ObjectContent.close();
                logger.info("Message being published to SNS.");
                publishMessageToSNS(patientCheckoutEvents);
            } catch (IOException e) {
                /*StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                logger.log(stringWriter.toString());*/
                logger.error("Error is:", e);
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

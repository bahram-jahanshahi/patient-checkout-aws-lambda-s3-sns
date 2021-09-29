package se.bahram.aws.lambda.s3.sns.error_handling;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    public void handler(SNSEvent snsEvent) {
        Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
        snsEvent.getRecords().forEach(snsRecord -> {
            logger.info("Dead Letter Queue Event: " + snsRecord.toString());
        });
    }
}

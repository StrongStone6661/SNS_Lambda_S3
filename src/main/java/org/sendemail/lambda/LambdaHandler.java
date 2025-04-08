package org.sendemail.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.regions.Region;

public class LambdaHandler implements RequestHandler<S3Event, String> {

    /**
     * Environment variable containing the SNS topic ARN
     * **/
    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        //creating sns client
        SnsClient snsClient = SnsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        try {
            //Get the s3 bucket and object key from the event
            String bucketName = s3Event.getRecords().get(0).getS3().getBucket().getName();
            String objectKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

            //Create the message
            String message = String.format("New file uploaded to bucket: %s, object key: %s", bucketName, objectKey);

            //Publish the message to the SNS topic
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(SNS_TOPIC_ARN)
                    .message(message)
                    .subject("S3 Upload Notification")
                    .build();
            snsClient.publish(publishRequest);
            context.getLogger().log("Publish SNS message" + message);

            return "SNS message published successfully";
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }finally {
            snsClient.close();
        }
    }
}

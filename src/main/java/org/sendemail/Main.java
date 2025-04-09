package org.sendemail;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.regions.Region;

public class Main implements RequestHandler<S3Event, String> {

    // Environment variable for SNS topic
    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        // Create SNS client
        SnsClient snsClient = SnsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();


        try {
            // Get the S3 bucket and object key from the event
            String bucketName = s3Event.getRecords().get(0).getS3().getBucket().getName();
            String objectKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

            // Create the message
            String message = String.format("New file uploaded to S3!\nBucket: %s\nFile: %s", bucketName, objectKey);

            // Publish to SNS topic
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(SNS_TOPIC_ARN)
                    .message(message)
                    .subject("S3 Upload Notification")
                    .build();

            snsClient.publish(publishRequest);
            context.getLogger().log("Published SNS message: " + message);

            return "Success";
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            snsClient.close();
        }
    }
}
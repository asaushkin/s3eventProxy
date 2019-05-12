package me.asaushkin;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LambdaS3ProxyConsumer implements RequestHandler<KinesisEvent, List<String>> {
    private static final Logger logger = LogManager.getLogger(LambdaS3ProxyConsumer.class);

    @Override
    public List<String> handleRequest(KinesisEvent input, Context context) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        logger.info("Processing Kinesis input: {}", input);

        List<String> results = new ArrayList<>();

        for (KinesisEvent.KinesisEventRecord r : input.getRecords()) {
            try {
                S3Event s3Event = mapper.readerFor(S3Event.class)
                        .readValue(r.getKinesis().getData().array());
                results.add(handle(s3Event));
            } catch (IOException e) {
                logger.throwing(e);
                results.add("FAIL");
            }
        }
        return results;
    }

    private String handle(S3Event s3event) {
        logger.info("Processing S3 event: {}", s3event);
        return "OK";
    }
}

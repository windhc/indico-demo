package com.windhc;

import com.indico.IndicoClient;
import com.indico.JSON;
import com.indico.mutation.DocumentExtraction;
import com.indico.query.Job;
import com.indico.storage.Blob;
import com.indico.storage.RetrieveBlob;
import com.indico.type.JobStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HC
 */
public class Ocr {
    private static final Logger logger = LoggerFactory.getLogger(Ocr.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("ocr");

        IndicoClientProvider indicoClientProvider = new IndicoClientProvider();
        IndicoClient client = indicoClientProvider.getIndicoClient();

        List<String> files = new ArrayList<>();
        files.add("src/main/resources/input/test.pdf");

        JSONObject json = new JSONObject();
        json.put("preset_config", "simple");

        DocumentExtraction extraction = client.documentExtraction();
        extraction.files(files).jsonConfig(json);
        List<Job> jobs = extraction.execute();
        Job job = jobs.get(0);
        while (job.status() == JobStatus.PENDING) {
            logger.info("wait...");
            Thread.sleep(1000);
            jobs = extraction.execute();
            job = jobs.get(0);
        }
        JSONObject obj = job.result();
        String url = obj.getString("url");
        RetrieveBlob retrieveBlob = client.retrieveBlob();
        Blob blob = retrieveBlob.url(url).execute();
        //call close on blob to dispose when done with object.
        blob.close();
        System.out.println(blob.asString());
    }
}

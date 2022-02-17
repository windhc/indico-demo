package com.windhc;

import cn.hutool.core.io.FileUtil;
import com.indico.IndicoClient;
import com.indico.entity.Submission;
import com.indico.mutation.UpdateSubmission;
import com.indico.mutation.WorkflowSubmission;
import com.indico.query.GetSubmission;
import com.indico.storage.Blob;
import com.indico.storage.RetrieveBlob;
import com.indico.type.SubmissionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HC
 */
public class ObjectDetection {
    private static final Logger logger = LoggerFactory.getLogger(ObjectDetection.class);

    private final IndicoClientProvider indicoClientProvider;

    public ObjectDetection(IndicoClientProvider indicoClientProvider) {
        this.indicoClientProvider = indicoClientProvider;
    }

    public void execute() throws IOException, InterruptedException {
        IndicoClient client = indicoClientProvider.getIndicoClient();
        List<File> files = Optional.ofNullable(new File("src/main/resources/input").listFiles())
                .map(Arrays::stream)
                .orElseThrow(() -> new RuntimeException(
                        "There should be some binary PDF files at Path src/main/resources/pdf for the test."))
                .collect(Collectors.toList());
        List<Integer> submissions = submission(client, Constant.workflowId, files);
        for (Integer id : submissions) {
            retrieveResult(client, id);
        }
    }

    private List<Integer> submission(IndicoClient client, int workflowId, List<File> files) {
        Map<String, byte[]> maps = new HashMap<>();
        for (File f : files) {
            maps.put(f.getName(), FileUtil.readBytes(f));
        }
        WorkflowSubmission workflowSubmission = client.workflowSubmission();
        if (workflowSubmission == null) {
            throw new RuntimeException("Indico workflowSubmission is null");
        }
        return workflowSubmission.byteStreams(maps).workflowId(workflowId).execute();
    }

    private void retrieveResult(IndicoClient client, Integer id) throws IOException, InterruptedException {
        GetSubmission getSubmission = client.getSubmission();
        Submission submission = getSubmission.submissionId(id).query();
        while (submission.status != SubmissionStatus.COMPLETE &&
                submission.status != SubmissionStatus.FAILED) {
            logger.info("submission Status: {}", submission.status);
            logger.info("wait...");
            Thread.sleep(1000);
            submission = getSubmission.submissionId(id).query();
        }
        logger.info("submission Status: {}", submission.status);
        // retrieve blob
        String url = "https://beta-indico.rpa.compass.com/" + submission.resultFile;
        RetrieveBlob retrieveBlob = client.retrieveBlob();
        logger.info(url);
        retrieveBlob.url(url);
        Blob blob = retrieveBlob.execute();
        String blobString = blob.asString();
        logger.info("submission id {} blob result {}", submission.id, blobString);
        outputResult(submission.resultFile.substring(submission.resultFile.lastIndexOf("/")),
                blobString);
        blob.close();
        // update submission retrieved
        UpdateSubmission updateSubmission = client.updateSubmission();
        updateSubmission.submissionId(submission.id);
        updateSubmission.retrieved(true);
        updateSubmission.execute();
    }

    private void outputResult(String outputName, String blobString) throws IOException {
        File target = new File("src/main/resources/output/" + outputName);
        if (target.exists()) {
            if (!target.delete()) {
                logger.error("Failed to delete legacy file" + outputName);
            } else {
                logger.info("Success to delete legacy file " + outputName);
            }
        }
        if (!target.createNewFile()) {
            logger.error("Failed to create file" + outputName);
        }
        FileOutputStream fos = new FileOutputStream(target, false);
        fos.write(blobString.getBytes());
        fos.close();
    }
}

package com.windhc;

import java.io.IOException;

/**
 * @author HC
 */
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
//        Gson gson = new Gson();
//        Properties properties = new Properties();
//        InputStream configFile = Application.class.getClassLoader().getResourceAsStream("config.properties");
//        properties.load(configFile);
//
//        String host = properties.getProperty("host");
//        String indicoToken = properties.getProperty("indicoToken");
//        System.out.println("host :" + host);
//        IndicoConfig config = new IndicoConfig.Builder()
//                .host(host)
//                .apiToken(indicoToken)
//                .build();
//
//        try (IndicoClient client = new IndicoKtorClient(config)) {
//            ListSubmissions listSubmissions = client.listSubmissions();
//            List<Submission> submissions = listSubmissions.query();
//            System.out.println("submissions size: " + submissions.size());
//            for (Submission submission : submissions) {
//                System.out.println(gson.toJson(submission));
//            }
//        }

        IndicoClientProvider indicoClientProvider = new IndicoClientProvider();
        ObjectDetection objectDetection = new ObjectDetection(indicoClientProvider);
        objectDetection.execute();
    }
}

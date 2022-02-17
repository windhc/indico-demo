package com.windhc;

import com.indico.IndicoClient;
import com.indico.IndicoConfig;
import com.indico.IndicoKtorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HC
 */
public class IndicoClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(IndicoClientProvider.class);

    public IndicoClient getIndicoClient() {
        logger.info("API Token:" + Constant.token);
        IndicoConfig config = new IndicoConfig.Builder()
                .host(Constant.host)
                .apiToken(Constant.token)
                .build();
        return new IndicoKtorClient(config);
    }
}

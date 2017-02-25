package uk.co.sentinelweb.microserver.example;

import android.app.Application;

import uk.co.sentinelweb.microserver.server.WebServerConfig;
import uk.co.sentinelweb.microserver.server.cp.PingCommandProcessor;

/**
 * Created by robert on 24/02/2017.
 */

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final PingCommandProcessor pcp = new PingCommandProcessor();
        final WebServerConfig config = new WebServerConfig.Builder().addProcessor("/ping",pcp).build();
        pcp.setConfig(config);
        uk.co.sentinelweb.microservice.MicroService.setWebServerConfig(config);
    }
}

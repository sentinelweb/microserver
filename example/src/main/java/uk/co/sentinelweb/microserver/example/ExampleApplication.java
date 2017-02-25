package uk.co.sentinelweb.microserver.example;

import android.app.Application;

import uk.co.sentinelweb.microserver.server.WebServerConfig;
import uk.co.sentinelweb.microserver.server.cp.PingCommandProcessor;
import uk.co.sentinelweb.microservice.cp.AssetCommandProcessor;
import uk.co.sentinelweb.microservice.cp.DrawableCommandProcessor;

import static uk.co.sentinelweb.microservice.MicroService.setWebServerConfig;

/**
 * Created by robert on 24/02/2017.
 */

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final WebServerConfig config = new WebServerConfig.Builder()
                .addProcessor(new PingCommandProcessor("/ping"))
                .addProcessor(new AssetCommandProcessor("/a/", this))
                .addProcessor(new DrawableCommandProcessor("/d/", this))
                .addForward("/favicon.ico", "/d/mipmap/ic_launcher")
                .addRedirect("/google", "http://www.google.com")
                .build();
        setWebServerConfig(config);
    }
}

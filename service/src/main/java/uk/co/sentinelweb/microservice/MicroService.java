package uk.co.sentinelweb.microservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import uk.co.sentinelweb.microserver.server.WebServer;
import uk.co.sentinelweb.microserver.server.WebServerConfig;

public class MicroService extends Service {
    private WebServer _webServer;
    private static WebServerConfig _webServerConfig;

    public static void setWebServerConfig(final WebServerConfig webServerConfig) {
        MicroService._webServerConfig = webServerConfig;
    }

    public MicroService() {
    }

    @Override
    public IBinder onBind(final Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (C.ACTION_START.equals(intent.getAction())) {
            startServer();
        } else if (C.ACTION_STOP.equals(intent.getAction())) {
            stopServer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startServer() {
        // todo wrap rx off main thread, wait for start
        _webServer = new WebServer(_webServerConfig);
        _webServer.start();
    }

    private void stopServer() {
        // todo wrap rx off main thread, wait for stop
        if (_webServer != null) {
            _webServer.close();
        }

    }

    public boolean isRunning() {
        return _webServer != null && _webServer.isAlive() && _webServer.isServerRunning();
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }
}

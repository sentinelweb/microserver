package uk.co.sentinelweb.microservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import uk.co.sentinelweb.microserver.server.WebServer;
import uk.co.sentinelweb.microserver.server.WebServerConfig;
import uk.co.sentinelweb.microserver.server.util.ThreadUtil;

public class MicroService extends Service {
    public static final String TAG = MicroService.class.getSimpleName();
    private WebServer _webServer;
    private static WebServerConfig _webServerConfig;
    private WifiManager.WifiLock _wifiLock;

    public enum Status {STARTED, STOPPED}

    public static final ReplaySubject<Status> statusPublishSubject = ReplaySubject.createWithSize(1);
    static {
        statusPublishSubject.onNext(Status.STOPPED);
        statusPublishSubject.doOnSubscribe(() -> Log.d(TAG, "ReplaySubject Subscribe"));
    }

    public static Intent getStartIntent(final Context c) {
        final Intent intent = new Intent(c, MicroService.class);
        intent.setAction(C.ACTION_START);
        return intent;
    }
//    public static Intent getBindIntent(final Context c) {
//        final Intent intent = new Intent(c, MicroService.class);
//        return intent;
//    }

    public static Intent getStopIntent(final Context c) {
        final Intent intent = new Intent(c, MicroService.class);
        intent.setAction(C.ACTION_STOP);
        return intent;
    }

    public static void setWebServerConfig(final WebServerConfig webServerConfig) {
        MicroService._webServerConfig = webServerConfig;
    }

    public MicroService() {
    }

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "Service bind");
        return new MsBinder();

    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroy");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(TAG, "Service startcommand");
        if (intent!=null) {
            Log.d(TAG, "Service startcommand acton:"+intent.getAction());
            if (C.ACTION_START.equals(intent.getAction())) {
                startServer();
            } else if (C.ACTION_STOP.equals(intent.getAction())) {
                stopServer();
            }
        }

        return START_STICKY;
    }

    private void startServer() {
        if (!isRunning()) {
            getStartObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.computation())
                    .subscribe((webServer) -> {
                                _webServer = webServer;
                                statusPublishSubject.onNext(Status.STARTED);
                            },
                            (e) -> Log.d(TAG, "Error starting server", e));
        }
    }

    @NonNull
    private Single<WebServer> getStartObservable() {
        return Single.create((subscriber) -> {
            final WebServer webServer = new WebServer(_webServerConfig);
            webServer.start();
            _wifiLock = ConnectivityUtil.obtainLock(this);
            while (!webServer.isServerRunning()) {
                ThreadUtil.sleep(20);
            }
            subscriber.onSuccess(webServer);
        });
    }

    private void stopServer() {
        if (isRunning()) {
            getStopObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.computation())
                    .subscribe((voided) -> {
                                _webServer = null;
                                statusPublishSubject.onNext(Status.STOPPED);
                                stopSelf();
                            },
                            (e) -> Log.d(TAG, "Error stopping server", e));
        }
    }

    @NonNull
    private Single<Void> getStopObservable() {
        return Single.create((subscriber) -> {
            if (_webServer != null) {
                _webServer.close();
            }
            if (_wifiLock != null) {
                ConnectivityUtil.releaseWifiLock(_wifiLock);
                _wifiLock = null;
            }
            while (_webServer.isServerRunning()) {
                ThreadUtil.sleep(20);
            }
            subscriber.onSuccess(null);
        });
    }

    public boolean isRunning() {
        return _webServer != null && (_webServer.isAlive() || _webServer.isServerRunning());
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    public Observable<Status> getStatusObserver() {
        return statusPublishSubject;
    }

    public class MsBinder extends Binder {
        public MicroService getService() {
            return MicroService.this;
        }
    }
}

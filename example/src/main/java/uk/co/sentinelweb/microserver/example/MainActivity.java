package uk.co.sentinelweb.microserver.example;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.sentinelweb.microservice.MicroService;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.start_button)
    Button startButton;

    @Bind(R.id.ping_button)
    Button pingButton;

    @Bind(R.id.ping_result)
    EditText pingOutputText;

    MicroService.Status _status;
    private Subscription _subscription;
    //private MSConnection _conn;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //_conn = new MSConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        _subscription = MicroService.statusPublishSubject.subscribe(_observer);
        //bindService(MicroService.getBindIntent(this), _conn, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbindService(_conn);
        Log.d(TAG, "onStop");
        if (_subscription != null && !_subscription.isUnsubscribed()) {
            _subscription.unsubscribe();
            _subscription = null;
        }
    }

    @OnClick(R.id.start_button)
    public void toggleServer() {
        if (_status == null || _status == MicroService.Status.STOPPED) {
            startService(MicroService.getStartIntent(this));
        } else {
            startService(MicroService.getStopIntent(this));
        }
    }

    @OnClick(R.id.ping_button)
    public void pingServer() {
        Single.create(
                new Single.OnSubscribe<String>() {
                    @Override
                    public void call(final SingleSubscriber<? super String> singleSubscriber) {
                        try {
                            singleSubscriber.onSuccess(doPingServer());
                        } catch (final IOException e) {
                            singleSubscriber.onError(e);
                        }
                    }
                }


        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.d(TAG, "Error pinging", e);
                    }

                    @Override
                    public void onNext(final String s) {
                        pingOutputText.setText(s);
                    }
                });
    }


    private static String doPingServer() throws IOException {
        final URL url = new URL("http://localhost:4443/ping");
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();

        if (con.getResponseCode() != 200) {
            throw new RuntimeException("HTTP error code : " + con.getResponseCode());
        }

        final BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        return br.readLine();
    }

    private final Observer<MicroService.Status> _observer = new Observer<MicroService.Status>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(final Throwable e) {
            Log.d(TAG, "Error binding to service", e);
        }

        @Override
        public void onNext(final MicroService.Status status) {
            MainActivity.this._status = status;
            if (status == MicroService.Status.STARTED) {
                startButton.setText("Stop");
                pingButton.setEnabled(true);
            } else {
                startButton.setText("Start");
                pingButton.setEnabled(false);
            }
        }
    };


    private class MSConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            _subscription = ((MicroService.MsBinder) service).getService().getStatusObserver()
                    .subscribe(_observer);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {

        }
    }

}

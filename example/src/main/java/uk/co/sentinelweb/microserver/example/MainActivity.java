package uk.co.sentinelweb.microserver.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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
    public static final String PING_URL = "http://localhost:4443/ping";
    public static final String PAGE_URL = "http://localhost:4443/a/index.html";

    @Bind(R.id.start_button)
    Button startButton;

    @Bind(R.id.ping_button)
    Button pingButton;

    @Bind(R.id.webpage_button)
    Button webpageButton;

    @Bind(R.id.ping_result)
    TextView pingOutputText;

    MicroService.Status _status;
    private Subscription _subscription;
    private Subscription _pingSubscription;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        _subscription=MicroService.statusPublishSubject.subscribe(_statusObserver);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        unsub(_subscription);
        unsub(_pingSubscription);
        super.onPause();

    }

    private void unsub(final Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
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

    @OnClick(R.id.webpage_button)
    public void launchWebpage() {
        startActivity(WebActivity.Companion.getIntent(this, PAGE_URL));
    }

    @OnClick(R.id.ping_button)
    public void pingServer() {
        final Single<String> pingSingle = Single.create(
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
        );
        _pingSubscription = pingSingle
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(pingObserver);
    }

    private static String doPingServer() throws IOException {
        final URL url = new URL(PING_URL);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();

        if (con.getResponseCode() != 200) {
            throw new RuntimeException("HTTP error code : " + con.getResponseCode());
        }

        final BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        return br.readLine();
    }

    private final Observer<MicroService.Status> _statusObserver = new Observer<MicroService.Status>() {
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
                webpageButton.setEnabled(true);
            } else {
                startButton.setText("Start");
                pingButton.setEnabled(false);
                webpageButton.setEnabled(false);
            }
        }
    };

    private final Observer<String> pingObserver = new Observer<String>() {
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
    };

}

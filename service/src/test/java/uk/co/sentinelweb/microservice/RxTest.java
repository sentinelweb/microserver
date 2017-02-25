package uk.co.sentinelweb.microservice;

import org.junit.Test;

import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import static org.junit.Assert.assertEquals;

/**
 */
public class RxTest {

    @Test
    public void singleTest() {
        Single.create((sub) -> {
            sub.onSuccess(1);
        }).subscribe(
                (val) -> System.out.println(val),
                (e) -> System.err.println(e.getMessage()));
    }

    @Test
    public void replayTest() {
        final ConnectableObservable<Integer> replayer = Observable.<Integer>just(1,2,3).publish();
        replayer.subscribe((val)->System.out.println("got"+val));
        replayer.subscribe((val)->System.out.println("and"+val));
        replayer.connect();// emits now
    }

    @Test
    public void replayTest1() {
        final Observable<Integer> replayer = Observable.<Integer>just(1,2,3).replay().refCount();
        replayer.subscribe((val)->System.out.println("got"+val));
        replayer.subscribe((val)->System.out.println("and"+val));
        //replayer.connect();
    }

    @Test
    public void replayTest2() {
        final PublishSubject<Integer> intPublishSubject = PublishSubject.create();
        final Observable<Integer> replayer = intPublishSubject.replay().refCount();
        final Subscription subscribe1 = replayer.subscribe((val) -> System.out.println("got" + val));
        final Subscription subscribe2 = replayer.subscribe((val) -> System.out.println("and" + val));
        intPublishSubject.onNext(1);
        subscribe1.unsubscribe();
        intPublishSubject.onNext(2);
        final Subscription subscribe3 = replayer.subscribe((val) -> System.out.println("tot" + val));
        subscribe2.unsubscribe();
        intPublishSubject.onNext(3);
        subscribe3.unsubscribe();
        intPublishSubject.onNext(4);
        //replayer.connect();
    }

    @Test
    public void cacheTest() {
        final PublishSubject<Integer> intPublishSubject = PublishSubject.create();
        final Observable<Integer> cacher = intPublishSubject.cache();
        final Subscription subscribe = cacher.subscribe((val) -> System.out.println("got" + val));
        intPublishSubject.onNext(1);
        intPublishSubject.onNext(2);
        cacher.takeUntil(integer -> integer==3).subscribe((val) -> System.out.println("lot" + val));
        intPublishSubject.onNext(3);
        intPublishSubject.onNext(4);
        subscribe.unsubscribe();
        intPublishSubject.onNext(5);

    }

    @Test
    public void replaySubjectTest() {
        final ReplaySubject<Integer> replaySubject = ReplaySubject.create(1);
        final Subscription subscribe = replaySubject.subscribe((val) -> System.out.println("got" + val));
        replaySubject.onNext(1);
        replaySubject.onNext(2);
        replaySubject.onNext(3);
        replaySubject.buffer(3).subscribe((val) -> System.out.println("lot" + val));
        replaySubject.onNext(4);
        subscribe.unsubscribe();
        replaySubject.onNext(5);
        replaySubject.onNext(6);
    }

    @Test
    public void replaySubjectSizeTest() {
        final ReplaySubject<Integer> replaySubject = ReplaySubject.createWithSize(1);
        final Subscription subscribe = replaySubject.subscribe((val) -> System.out.println("got" + val));
        replaySubject.onNext(1);
        replaySubject.onNext(2);
        replaySubject.onNext(3);
        replaySubject.subscribe((val) -> System.out.println("lot" + val));
        replaySubject.onNext(4);
        subscribe.unsubscribe();
        replaySubject.onNext(5);
        replaySubject.onNext(6);
    }
}
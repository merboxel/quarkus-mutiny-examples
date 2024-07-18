package merboxel.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class MutinyObservingEventsTest {

    private final static String UNI_DEFAULT_TEST_VALUE = "test";
    private final static Uni<String> UNI_DEFAULT_MUTINY = Uni.createFrom().item(UNI_DEFAULT_TEST_VALUE);
    private final static List<String> MULTI_DEFAULT_TEST_VALUE = List.of("test1", "test2", "test3", "test4");
    private final static Multi<String> MULTI_DEFAULT_MUTINY = Multi.createFrom().iterable(MULTI_DEFAULT_TEST_VALUE);

    @Test
    void uni_observing_onSuccess() {
        UniAssertSubscriber<String> subscriber = UNI_DEFAULT_MUTINY
                .onSubscription().invoke(() -> System.out.println("'uni_observing_onItem' onSubscription"))
                .onItem().invoke((i) -> System.out.println("'uni_observing_onItem' onItem " + i))
                .onFailure().invoke((e) -> System.out.println("'uni_observing_onItem' onFailure " + e))
                .onCancellation().invoke(() -> System.out.println("'uni_observing_onItem' onCancellation"))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertCompleted();
    }

    @Test
    void uni_observing_onFailure() {
        UniAssertSubscriber<Integer> subscriber = Uni.createFrom().<Integer>failure(new RuntimeException("emmitter failure"))
                .onSubscription().invoke(() -> System.out.println("'uni_observing_onItem' onSubscription"))
                .onItem().invoke((i) -> System.out.println("'uni_observing_onItem' onItem " + i))
                .onFailure().invoke((e) -> System.out.println("'uni_observing_onItem' onFailure " + e))
                .onCancellation().invoke(() -> System.out.println("'uni_observing_onItem' onCancellation"))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertFailed();
    }

    @Test
    void uni_observing_onCancellation() {
        UniAssertSubscriber<String> subscriber = UNI_DEFAULT_MUTINY
                .onSubscription().invoke(() -> System.out.println("'uni_observing_onItem' onSubscription"))
                .onItem().invoke((i) -> System.out.println("'uni_observing_onItem' onItem " + i))
                .onFailure().invoke((e) -> System.out.println("'uni_observing_onItem' onFailure " + e))
                .onCancellation().invoke(() -> System.out.println("'uni_observing_onItem' onCancellation"))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.cancel();
    }

    @Test
    void multi_observing_completion() {
        AssertSubscriber<String> subscriber = MULTI_DEFAULT_MUTINY
                .onSubscription().invoke(() -> System.out.println("'multi_observing_completion' onSubscription"))
                .onItem().invoke((i) -> System.out.println("'multi_observing_completion' onItem " + i))
                .onCompletion().invoke(() -> System.out.println("'multi_observing_completion' onCompleted"))
                .onFailure().invoke((e) -> System.out.println("'multi_observing_completion' onFailure " + e.getMessage()))
                .onCancellation().invoke(() -> System.out.println("'multi_observing_completion' onCancellation "))
                .onRequest().invoke((r) -> System.out.println("'multi_observing_completion' onRequest " + r))
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).onCompletion();
    }

    @Test
    void multi_observing_failure() {
        AssertSubscriber<Integer> subscriber = Multi.createFrom().<Integer>emitter((em) -> {

                    em.emit(1);
                    em.emit(2);
                    em.emit(3);
                    em.fail(new RuntimeException("emmitter failure"));
                }).onSubscription().invoke(() -> System.out.println("'multi_observing_failure' onSubscription"))
                .onItem().invoke((i) -> System.out.println("'multi_observing_failure' onItem " + i))
                .onCompletion().invoke(() -> System.out.println("'multi_observing_failure' onCompleted"))
                .onFailure().invoke((e) -> System.out.println("'multi_observing_failure' onFailure " + e.getMessage()))
                .onCancellation().invoke(() -> System.out.println("'multi_observing_failure' onCancellation "))
                .onRequest().invoke((r) -> System.out.println("'multi_observing_failure' onRequest " + r))
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(3).awaitFailure();
    }

    @Test
    void multi_observing_failure_recover() {
        AssertSubscriber<Integer> subscriber = Multi.createFrom().<Integer>emitter((em) -> {
                    em.emit(1);
                    em.emit(2);
                    em.emit(3);
                    em.fail(new Exception("emmitter failure"));
                }).onItem().invoke((i) -> System.out.println("'multi_observing_failure' onItem " + i))
                .onFailure().invoke((e) -> System.out.println("'multi_observing_failure' onFailure " + e.getMessage()))
                .onFailure().recoverWithMulti(
                        Multi.createFrom().<Integer>emitter((em) -> {
                            em.emit(4);
                            em.emit(5);
                            em.emit(6);
                            em.complete();
                        }).onItem().invoke((i) -> System.out.println("'multi_observing_failure_inner' onItem " + i))
                ).subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(6).assertCompleted();
    }

    @Test
    void multi_observing_cancellation() {
        AssertSubscriber<Integer> subscriber = Multi.createFrom().<Integer>emitter((em) -> {
                    em.emit(1);
                    em.emit(2);
                    em.emit(3);
                    em.fail(new Exception("emmitter failure"));
                }).onSubscription().invoke(() -> System.out.println("'multi_observing_cancellation' onSubscription"))
                .onItem().invoke((i) -> System.out.println("'multi_observing_cancellation' onItem " + i))
                .onCompletion().invoke(() -> System.out.println("'multi_observing_cancellation' onCompleted"))
                .onFailure().invoke((e) -> System.out.println("'multi_observing_cancellation' onFailure " + e.getMessage()))
                .onCancellation().invoke(() -> System.out.println("'multi_observing_cancellation' onCancellation "))
                .onRequest().invoke((r) -> System.out.println("'multi_observing_cancellation' onRequest " + r))
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(2).cancel();
    }
}

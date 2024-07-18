package merboxel.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@QuarkusTest
public class MutinyReactive2ImperativeTest {

    private final static String UNI_DEFAULT_TEST_VALUE = "test";
    private final static Uni<String> UNI_DEFAULT_MUTINY = Uni.createFrom().item(UNI_DEFAULT_TEST_VALUE);
    private final static List<String> MULTI_DEFAULT_TEST_VALUE = List.of("test1", "test2", "test3", "test4");
    private final static Multi<String> MULTI_DEFAULT_MUTINY = Multi.createFrom().iterable(MULTI_DEFAULT_TEST_VALUE);

    @Test
    void uni_reactive_to_imperative_await_indefinitely() {
        UniAssertSubscriber<String> subscriber = UNI_DEFAULT_MUTINY
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitItem()
                .assertItem(UNI_DEFAULT_TEST_VALUE);
    }

    @Test
    void uni_reactive_to_imperative_await_duration_1s() {
        UniAssertSubscriber<String> subscriber = UNI_DEFAULT_MUTINY
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitItem(Duration.ofSeconds(1))
                .assertItem(UNI_DEFAULT_TEST_VALUE);
    }

    @Test
    void multi_reactive_to_imperative_subscribe() {
        AssertSubscriber<String> subscriber = MULTI_DEFAULT_MUTINY
                .subscribe()
                .withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).awaitCompletion()
                .assertItems(MULTI_DEFAULT_TEST_VALUE.toArray(String[]::new));
    }

    @Test
    void multi_reactive_to_imperative_subscribe_duration_1s() {
        AssertSubscriber<String> subscriber = MULTI_DEFAULT_MUTINY
                .subscribe()
                .withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).awaitCompletion(Duration.ofSeconds(1))
                .assertItems(MULTI_DEFAULT_TEST_VALUE.toArray(String[]::new));
    }

    @Test
    void multi_create_endless_get_first_100() {

        final long sizeToFetch = 100L;

        AssertSubscriber<Long> subscriber = Multi.createFrom().items(
                Stream.generate(() -> {
                    AtomicLong counter = new AtomicLong();
                    return counter.getAndIncrement();
                })
        ).subscribe().withSubscriber(AssertSubscriber.create(sizeToFetch));

        Long[] expected = Stream.generate(() -> {
            AtomicLong counter = new AtomicLong();
            return counter.getAndIncrement();
        }).limit(sizeToFetch).toArray(Long[]::new);

        subscriber.assertItems(expected);
    }
}

package merboxel.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class MutinyTransformSyncTest {

    private final static String UNI_DEFAULT_TEST_VALUE = "test";
    private final static Uni<String> UNI_DEFAULT_MUTINY = Uni.createFrom().item(UNI_DEFAULT_TEST_VALUE);
    private final static List<String> MULTI_DEFAULT_TEST_VALUE = List.of("test1", "test2", "test3", "test4");
    private final static Multi<String> MULTI_DEFAULT_MUTINY = Multi.createFrom().iterable(MULTI_DEFAULT_TEST_VALUE);

    @Test
    public void uni_transform_uppercase() {
        UniAssertSubscriber<String> subscriber = UNI_DEFAULT_MUTINY.onItem().transform(String::toUpperCase)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertItem(UNI_DEFAULT_TEST_VALUE.toUpperCase()).assertCompleted();
    }

    @Test
    public void uni_transform_random() {
        final String actual = "random";

        UniAssertSubscriber<String> subscriber = UNI_DEFAULT_MUTINY.onItem().transform((str) -> actual)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertItem(actual).assertCompleted();
    }

    @Test
    public void uni_transform_method() {
        UniAssertSubscriber<Integer> subscriber = UNI_DEFAULT_MUTINY.onItem().transform(this::transformStrToInt)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertItem(1).assertCompleted();
    }

    @Test
    public void multi_transform_uppercase() {
        AssertSubscriber<String> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transform(String::toUpperCase)
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).assertItems(
                MULTI_DEFAULT_TEST_VALUE.stream()
                        .map(String::toUpperCase)
                        .toArray(String[]::new)
        ).assertCompleted();
    }

    @Test
    public void multi_transform_random() {
        final String actual = "random";

        AssertSubscriber<String> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transform((str) -> actual)
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).assertItems(
                new String[]{actual, actual, actual, actual}
        ).assertCompleted();
    }

    @Test
    public void multi_transform_method() {
        AssertSubscriber<Integer> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transform(this::transformStrToInt)
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).assertItems(
                new Integer[]{2,3,4,5}
        ).assertCompleted();
    }

    public int transformStrToInt(String str) {
        return switch (str) {
            case "test" ->  1;
            case "test1" -> 2;
            case "test2" -> 3;
            case "test3" -> 4;
            case "test4" -> 5;
            default -> throw new RuntimeException();
        };
    }
}

package merboxel.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class MutinyCreationsTest {

    private final static String UNI_DEFAULT_TEST_VALUE = "test";
    private final static Uni<String> UNI_DEFAULT_MUTINY = Uni.createFrom().item(UNI_DEFAULT_TEST_VALUE);
    private final static List<String> MULTI_DEFAULT_TEST_VALUE = List.of("test1", "test2", "test3", "test4");
    private final static Multi<String> MULTI_DEFAULT_MUTINY = Multi.createFrom().iterable(MULTI_DEFAULT_TEST_VALUE);

    @Test
    void uni_create_via_item() {
        Uni.createFrom().item(1);
    }

    @Test
    void uni_create_via_item_null_wrong() {
        assertThrows(IllegalArgumentException.class, () -> Uni.createFrom().item(null));
    }

    @Test
    void uni_create_via_item_null() {
        Uni.createFrom().item((Object) null);
    }

    @Test
    void uni_create_via_voidItem() {
        Uni.createFrom().voidItem();
    }

    @Test
    void uni_create_void_vs_null() {
        assertEquals(
                Uni.createFrom().item((Object) null)
                        .await().atMost(Duration.ofSeconds(1)),
                Uni.createFrom().voidItem()
                        .await().atMost(Duration.ofSeconds(1))
        );
    }

    @Test
    void uni_create_via_emitter() {
        Uni.createFrom().emitter((em) -> {
            em.complete(1);
        });
    }

    @Test
    void uni_create_via_failure() {
        Uni<?> failure = Uni.createFrom().failure(new Exception());
        assertThrows(Exception.class, () -> failure.await().atMost(Duration.ofSeconds(1)));
    }

    @Test
    void multi_create_endless_via_stream() {
        Multi.createFrom().items(
                Stream.generate(() -> {
                    AtomicLong counter = new AtomicLong();
                    return counter.getAndIncrement();
                })
        );
    }

    @Test
    void multi_create_endless_via_ticks() {
        Multi.createFrom().ticks().every(Duration.ofSeconds(1));
    }

    @Test
    void multi_create_endless_via_items() {
        Multi.createFrom().items(1, 2, 3, 4, 5, 6);
    }

    @Test
    void multi_create_endless_via_iterable_list() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6);
        Multi.createFrom().iterable(list);
    }

    @Test
    void multi_create_endless_via_emitter() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6);
        Multi.createFrom().<Integer>emitter((em) -> {
            em.emit(1);
            em.emit(2);
            em.emit(3);
            em.emit(4);
            em.emit(5);
            em.emit(6);
            em.complete();
        });
    }

    @Test
    void multi_create_endless_via_range() {
        Multi.createFrom().range(1, 6);
    }
}

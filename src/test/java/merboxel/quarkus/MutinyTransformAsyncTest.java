package merboxel.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.smallrye.mutiny.helpers.spies.Spy.onItem;

@QuarkusTest
public class MutinyTransformAsyncTest {

    private final static Integer UNI_DEFAULT_TEST_VALUE = 1;
    private final static Uni<Integer> UNI_DEFAULT_MUTINY = Uni.createFrom().item(UNI_DEFAULT_TEST_VALUE);
    private final static List<Integer> MULTI_DEFAULT_TEST_VALUE = List.of(1,2,3,4);
    private final static Multi<Integer> MULTI_DEFAULT_MUTINY = Multi.createFrom().iterable(MULTI_DEFAULT_TEST_VALUE);

    @Test
    public void uni_transformToUni() {
        UniAssertSubscriber<Integer> subscriber = UNI_DEFAULT_MUTINY.onItem()
                .transformToUni((i) -> Uni.createFrom().item(i + 1))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertItem(UNI_DEFAULT_TEST_VALUE + 1).assertCompleted();
    }

    @Test
    public void uni_transformToMulti() {
        AssertSubscriber<Integer> subscriber = Uni.createFrom().item(MULTI_DEFAULT_TEST_VALUE).onItem()
                .transformToMulti((list) -> Multi.createFrom().iterable(list))
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).assertItems(
                new Integer[]{1,2,3,4}
        ).assertCompleted();
    }

    @Test
    public void multi_transformToUni_merge() {
        AssertSubscriber<Integer> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transformToUni((i) -> Uni.createFrom().item(i)).merge()
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).assertItems(
                new Integer[]{1,2,3,4}
        ).assertCompleted();
    }

    @Test
    public void multi_transformToUni_concat() {
        AssertSubscriber<Integer> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transformToUni((i) -> Uni.createFrom().item(i)).concatenate()
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()).assertItems(
                new Integer[]{1,2,3,4}
        ).assertCompleted();
    }

    @Test
    public void multi_transformToMulti_merge() {
        AssertSubscriber<Integer> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transformToMulti((i) -> Multi.createFrom().items(i,i+4)).merge()
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()*2).assertItems(
                new Integer[]{1,2,3,4,5,6,7,8}
        ).assertCompleted();
    }

    @Test
    public void multi_transformToMulti_concat() {
        AssertSubscriber<Integer> subscriber = MULTI_DEFAULT_MUTINY.onItem()
                .transformToMulti((i) -> Multi.createFrom().items(i,i+4)).collectFailures().concatenate()
                .subscribe().withSubscriber(AssertSubscriber.create());

        subscriber.awaitNextItems(MULTI_DEFAULT_TEST_VALUE.size()*2).assertItems(
                new Integer[]{1,5,2,6,3,7,4,8}
        ).assertCompleted();
    }

    @Test
    @Disabled
    public void multi_pgpool() {

        MULTI_DEFAULT_MUTINY.onItem().transformToMulti((i) ->

            PgPool.client().preparedQuery("""
                SELECT * FROM table WHERE column = $1
            """).execute(Tuple.of(i)).onItem().transformToMulti(RowSet::toMulti)
            .onItem().transformToMulti((row) ->

                PgPool.client().preparedQuery("""
                    SELECT * FROM table2 WHERE column = $1
                """).execute(Tuple.of(row.getInteger("id"))).onItem().transformToMulti(RowSet::toMulti)
            ).merge()
        ).merge().onItem().ignoreAsUni().replaceWithVoid();
    }

    @Test
    @Disabled
    public void multi_pgpool_executeBatch() {

        MULTI_DEFAULT_MUTINY.onItem().transformToMulti((i) ->

            PgPool.client().preparedQuery("""
                SELECT * FROM table WHERE column = $1
            """).execute(Tuple.of(i)).onItem().transformToMulti(RowSet::toMulti)
            .onItem().transform((row) -> Tuple.of(row.getInteger("id")))
            .collect().asList()
            .onItem().transformToMulti((rowList) ->

                PgPool.client().preparedQuery("""
                    SELECT * FROM table2 WHERE column = $1
                """).executeBatch(rowList).onItem().transformToMulti(RowSet::toMulti)
            )
        ).merge().onItem().ignoreAsUni().replaceWithVoid();
    }
}

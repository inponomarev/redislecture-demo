package edu.phystech;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.fppt.jedismock.RedisServer;
import org.junit.jupiter.api.*;
import org.assertj.core.api.Assertions;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public class AppTest {
    static RedisServer server;

    Jedis jedis;
    RedissonClient client;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = RedisServer
                .newRedisServer()
                .start();
    }

    @BeforeEach
    void setUp() {
        jedis = new Jedis(server.getHost(), server.getBindPort());
        Config config = new Config();
        config.useSingleServer().setAddress(
                String.format("redis://%s:%d",
                        server.getHost(), server.getBindPort()));
        client = Redisson.create(config);
    }


    @AfterEach
    void tearDown() {
        jedis.close();
        client.shutdown();
    }

    @AfterAll
    static void afterAll() throws IOException {
        server.stop();
    }

    @Test
    void simpleSet() {
        jedis.set("foo", "bar");
        Assertions.assertThat(jedis.get("foo")).isEqualTo("bar");
    }

    AtomicLong al = new AtomicLong();

    @Test
    void atomicInteger() {

        RAtomicLong atomicLong = client.getAtomicLong("atomicLong");
        Assertions.assertThat(atomicLong.incrementAndGet()).isEqualTo(1);
        Assertions.assertThat(atomicLong.incrementAndGet()).isEqualTo(2);
        Assertions.assertThat(atomicLong.incrementAndGet()).isEqualTo(3);
    }

    @Test
    void typeSafeObjectHandler() {
        Person p = testPerson();
        RBucket<Person> bucket = client.getBucket("person");
        bucket.set(p);
        Assertions.assertThat(bucket.get()).isEqualTo(testPerson());
    }


    @Test
    void typeSafeMap() {
        RMap<String, Person> map = client.getMap("personMap");
        map.put("key", testPerson());
        Assertions.assertThat(map.get("key")).isEqualTo(testPerson());
    }

    private static Person testPerson() {
        return new Person("John", "Doe", LocalDate.of(1975, 4, 11));
    }

}

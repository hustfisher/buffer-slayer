package io.github.tramchamploo.bufferslayer;

import com.google.common.annotations.VisibleForTesting;
import io.github.tramchamploo.bufferslayer.RedisCommand.Append;
import io.github.tramchamploo.bufferslayer.RedisCommand.Blpop;
import io.github.tramchamploo.bufferslayer.internal.MessageFuture;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster.Reset;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Pool;
import redis.clients.util.SafeEncoder;
import redis.clients.util.Slowlog;

/**
 * {@link Jedis} that execute commands in pipeline
 */
public class BatchJedis {

  private final Jedis delegate;
  private final Reporter<RedisCommand, ?> reporter;

  @VisibleForTesting
  BatchJedis(Jedis delegate, Reporter reporter) {
    this.delegate = delegate;
    this.reporter = reporter;
  }

  public BatchJedis(Jedis delegate) {
    this.delegate = delegate;
    this.reporter = AsyncReporter.builder(new JedisSender(delegate)).build();
  }

  public String set(String key, String value) {
    return delegate.set(key, value);
  }

  public String set(String key, String value, String nxxx, String expx, long time) {
    return delegate.set(key, value, nxxx, expx, time);
  }

  public String get(String key) {
    return delegate.get(key);
  }

  public Long exists(String... keys) {
    return delegate.exists(keys);
  }

  public Boolean exists(String key) {
    return delegate.exists(key);
  }

  public Long del(String... keys) {
    return delegate.del(keys);
  }

  public Long del(String key) {
    return delegate.del(key);
  }

  public String type(String key) {
    return delegate.type(key);
  }

  public Set<String> keys(String pattern) {
    return delegate.keys(pattern);
  }

  public String randomKey() {
    return delegate.randomKey();
  }

  public String rename(String oldkey, String newkey) {
    return delegate.rename(oldkey, newkey);
  }

  public Long renamenx(String oldkey, String newkey) {
    return delegate.renamenx(oldkey, newkey);
  }

  public Long expire(String key, int seconds) {
    return delegate.expire(key, seconds);
  }

  public Long expireAt(String key, long unixTime) {
    return delegate.expireAt(key, unixTime);
  }

  public Long ttl(String key) {
    return delegate.ttl(key);
  }

  public Long move(String key, int dbIndex) {
    return delegate.move(key, dbIndex);
  }

  public String getSet(String key, String value) {
    return delegate.getSet(key, value);
  }

  public List<String> mget(String... keys) {
    return delegate.mget(keys);
  }

  public Long setnx(String key, String value) {
    return delegate.setnx(key, value);
  }

  public String setex(String key, int seconds, String value) {
    return delegate.setex(key, seconds, value);
  }

  public String mset(String... keysvalues) {
    return delegate.mset(keysvalues);
  }

  public Long msetnx(String... keysvalues) {
    return delegate.msetnx(keysvalues);
  }

  public Long decrBy(String key, long integer) {
    return delegate.decrBy(key, integer);
  }

  public Long decr(String key) {
    return delegate.decr(key);
  }

  public Long incrBy(String key, long integer) {
    return delegate.incrBy(key, integer);
  }

  public Double incrByFloat(String key, double value) {
    return delegate.incrByFloat(key, value);
  }

  public Long incr(String key) {
    return delegate.incr(key);
  }

  @SuppressWarnings("unchecked")
  public MessageFuture<Long> append(String key, String value) {
    return (MessageFuture<Long>) reporter.report(
        new Append(SafeEncoder.encode(key), SafeEncoder.encode(value)));
  }

  public String substr(String key, int start, int end) {
    return delegate.substr(key, start, end);
  }

  public Long hset(String key, String field, String value) {
    return delegate.hset(key, field, value);
  }

  public String hget(String key, String field) {
    return delegate.hget(key, field);
  }

  public Long hsetnx(String key, String field, String value) {
    return delegate.hsetnx(key, field, value);
  }

  public String hmset(String key, Map<String, String> hash) {
    return delegate.hmset(key, hash);
  }

  public List<String> hmget(String key, String... fields) {
    return delegate.hmget(key, fields);
  }

  public Long hincrBy(String key, String field, long value) {
    return delegate.hincrBy(key, field, value);
  }

  public Double hincrByFloat(String key, String field, double value) {
    return delegate.hincrByFloat(key, field, value);
  }

  public Boolean hexists(String key, String field) {
    return delegate.hexists(key, field);
  }

  public Long hdel(String key, String... fields) {
    return delegate.hdel(key, fields);
  }

  public Long hlen(String key) {
    return delegate.hlen(key);
  }

  public Set<String> hkeys(String key) {
    return delegate.hkeys(key);
  }

  public List<String> hvals(String key) {
    return delegate.hvals(key);
  }

  public Map<String, String> hgetAll(String key) {
    return delegate.hgetAll(key);
  }

  public Long rpush(String key, String... strings) {
    return delegate.rpush(key, strings);
  }

  public Long lpush(String key, String... strings) {
    return delegate.lpush(key, strings);
  }

  public Long llen(String key) {
    return delegate.llen(key);
  }

  public List<String> lrange(String key, long start, long end) {
    return delegate.lrange(key, start, end);
  }

  public String ltrim(String key, long start, long end) {
    return delegate.ltrim(key, start, end);
  }

  public String lindex(String key, long index) {
    return delegate.lindex(key, index);
  }

  public String lset(String key, long index, String value) {
    return delegate.lset(key, index, value);
  }

  public Long lrem(String key, long count, String value) {
    return delegate.lrem(key, count, value);
  }

  public String lpop(String key) {
    return delegate.lpop(key);
  }

  public String rpop(String key) {
    return delegate.rpop(key);
  }

  public String rpoplpush(String srckey, String dstkey) {
    return delegate.rpoplpush(srckey, dstkey);
  }

  public Long sadd(String key, String... members) {
    return delegate.sadd(key, members);
  }

  public Set<String> smembers(String key) {
    return delegate.smembers(key);
  }

  public Long srem(String key, String... members) {
    return delegate.srem(key, members);
  }

  public String spop(String key) {
    return delegate.spop(key);
  }

  public Set<String> spop(String key, long count) {
    return delegate.spop(key, count);
  }

  public Long smove(String srckey, String dstkey, String member) {
    return delegate.smove(srckey, dstkey, member);
  }

  public Long scard(String key) {
    return delegate.scard(key);
  }

  public Boolean sismember(String key, String member) {
    return delegate.sismember(key, member);
  }

  public Set<String> sinter(String... keys) {
    return delegate.sinter(keys);
  }

  public Long sinterstore(String dstkey, String... keys) {
    return delegate.sinterstore(dstkey, keys);
  }

  public Set<String> sunion(String... keys) {
    return delegate.sunion(keys);
  }

  public Long sunionstore(String dstkey, String... keys) {
    return delegate.sunionstore(dstkey, keys);
  }

  public Set<String> sdiff(String... keys) {
    return delegate.sdiff(keys);
  }

  public Long sdiffstore(String dstkey, String... keys) {
    return delegate.sdiffstore(dstkey, keys);
  }

  public String srandmember(String key) {
    return delegate.srandmember(key);
  }

  public List<String> srandmember(String key, int count) {
    return delegate.srandmember(key, count);
  }

  public Long zadd(String key, double score, String member) {
    return delegate.zadd(key, score, member);
  }

  public Long zadd(String key, double score, String member,
      ZAddParams params) {
    return delegate.zadd(key, score, member, params);
  }

  public Long zadd(String key, Map<String, Double> scoreMembers) {
    return delegate.zadd(key, scoreMembers);
  }

  public Long zadd(String key, Map<String, Double> scoreMembers,
      ZAddParams params) {
    return delegate.zadd(key, scoreMembers, params);
  }

  public Set<String> zrange(String key, long start, long end) {
    return delegate.zrange(key, start, end);
  }

  public Long zrem(String key, String... members) {
    return delegate.zrem(key, members);
  }

  public Double zincrby(String key, double score, String member) {
    return delegate.zincrby(key, score, member);
  }

  public Double zincrby(String key, double score, String member,
      ZIncrByParams params) {
    return delegate.zincrby(key, score, member, params);
  }

  public Long zrank(String key, String member) {
    return delegate.zrank(key, member);
  }

  public Long zrevrank(String key, String member) {
    return delegate.zrevrank(key, member);
  }

  public Set<String> zrevrange(String key, long start, long end) {
    return delegate.zrevrange(key, start, end);
  }

  public Set<Tuple> zrangeWithScores(String key, long start, long end) {
    return delegate.zrangeWithScores(key, start, end);
  }

  public Set<Tuple> zrevrangeWithScores(String key, long start,
      long end) {
    return delegate.zrevrangeWithScores(key, start, end);
  }

  public Long zcard(String key) {
    return delegate.zcard(key);
  }

  public Double zscore(String key, String member) {
    return delegate.zscore(key, member);
  }

  public String watch(String... keys) {
    return delegate.watch(keys);
  }

  public List<String> sort(String key) {
    return delegate.sort(key);
  }

  public List<String> sort(String key, SortingParams sortingParameters) {
    return delegate.sort(key, sortingParameters);
  }

  @SuppressWarnings("unchecked")
  public MessageFuture<List<String>> blpop(int timeout, String... keys) {
    return (MessageFuture<List<String>>) reporter.report(
        new Blpop(timeout, SafeEncoder.encodeMany(keys)));
  }

  @SuppressWarnings("unchecked")
  public MessageFuture<List<String>> blpop(String... args) {
    return (MessageFuture<List<String>>) reporter.report(
        new Blpop(0, SafeEncoder.encodeMany(args)));
  }

  public List<String> brpop(String... args) {
    return delegate.brpop(args);
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  public MessageFuture<List<String>> blpop(String arg) {
    return (MessageFuture<List<String>>) reporter.report(
        new Blpop(0, SafeEncoder.encode(arg)));
  }

  @Deprecated
  public List<String> brpop(String arg) {
    return delegate.brpop(arg);
  }

  public Long sort(String key, SortingParams sortingParameters,
      String dstkey) {
    return delegate.sort(key, sortingParameters, dstkey);
  }

  public Long sort(String key, String dstkey) {
    return delegate.sort(key, dstkey);
  }

  public List<String> brpop(int timeout, String... keys) {
    return delegate.brpop(timeout, keys);
  }

  public Long zcount(String key, double min, double max) {
    return delegate.zcount(key, min, max);
  }

  public Long zcount(String key, String min, String max) {
    return delegate.zcount(key, min, max);
  }

  public Set<String> zrangeByScore(String key, double min, double max) {
    return delegate.zrangeByScore(key, min, max);
  }

  public Set<String> zrangeByScore(String key, String min, String max) {
    return delegate.zrangeByScore(key, min, max);
  }

  public Set<String> zrangeByScore(String key, double min, double max, int offset,
      int count) {
    return delegate.zrangeByScore(key, min, max, offset, count);
  }

  public Set<String> zrangeByScore(String key, String min, String max, int offset,
      int count) {
    return delegate.zrangeByScore(key, min, max, offset, count);
  }

  public Set<Tuple> zrangeByScoreWithScores(String key, double min,
      double max) {
    return delegate.zrangeByScoreWithScores(key, min, max);
  }

  public Set<Tuple> zrangeByScoreWithScores(String key,
      String min, String max) {
    return delegate.zrangeByScoreWithScores(key, min, max);
  }

  public Set<Tuple> zrangeByScoreWithScores(String key, double min,
      double max, int offset, int count) {
    return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
  }

  public Set<Tuple> zrangeByScoreWithScores(String key,
      String min, String max, int offset, int count) {
    return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
  }

  public Set<String> zrevrangeByScore(String key, double max, double min) {
    return delegate.zrevrangeByScore(key, max, min);
  }

  public Set<String> zrevrangeByScore(String key, String max, String min) {
    return delegate.zrevrangeByScore(key, max, min);
  }

  public Set<String> zrevrangeByScore(String key, double max, double min, int offset,
      int count) {
    return delegate.zrevrangeByScore(key, max, min, offset, count);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
      double min) {
    return delegate.zrevrangeByScoreWithScores(key, max, min);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
      double min, int offset, int count) {
    return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(String key,
      String max, String min, int offset, int count) {
    return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
  }

  public Set<String> zrevrangeByScore(String key, String max, String min, int offset,
      int count) {
    return delegate.zrevrangeByScore(key, max, min, offset, count);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(String key,
      String max, String min) {
    return delegate.zrevrangeByScoreWithScores(key, max, min);
  }

  public Long zremrangeByRank(String key, long start, long end) {
    return delegate.zremrangeByRank(key, start, end);
  }

  public Long zremrangeByScore(String key, double start, double end) {
    return delegate.zremrangeByScore(key, start, end);
  }

  public Long zremrangeByScore(String key, String start, String end) {
    return delegate.zremrangeByScore(key, start, end);
  }

  public Long zunionstore(String dstkey, String... sets) {
    return delegate.zunionstore(dstkey, sets);
  }

  public Long zunionstore(String dstkey, ZParams params, String... sets) {
    return delegate.zunionstore(dstkey, params, sets);
  }

  public Long zinterstore(String dstkey, String... sets) {
    return delegate.zinterstore(dstkey, sets);
  }

  public Long zinterstore(String dstkey, ZParams params, String... sets) {
    return delegate.zinterstore(dstkey, params, sets);
  }

  public Long zlexcount(String key, String min, String max) {
    return delegate.zlexcount(key, min, max);
  }

  public Set<String> zrangeByLex(String key, String min, String max) {
    return delegate.zrangeByLex(key, min, max);
  }

  public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
    return delegate.zrangeByLex(key, min, max, offset, count);
  }

  public Set<String> zrevrangeByLex(String key, String max, String min) {
    return delegate.zrevrangeByLex(key, max, min);
  }

  public Set<String> zrevrangeByLex(String key, String max, String min, int offset,
      int count) {
    return delegate.zrevrangeByLex(key, max, min, offset, count);
  }

  public Long zremrangeByLex(String key, String min, String max) {
    return delegate.zremrangeByLex(key, min, max);
  }

  public Long strlen(String key) {
    return delegate.strlen(key);
  }

  public Long lpushx(String key, String... string) {
    return delegate.lpushx(key, string);
  }

  public Long persist(String key) {
    return delegate.persist(key);
  }

  public Long rpushx(String key, String... string) {
    return delegate.rpushx(key, string);
  }

  public String echo(String string) {
    return delegate.echo(string);
  }

  public Long linsert(String key, LIST_POSITION where,
      String pivot, String value) {
    return delegate.linsert(key, where, pivot, value);
  }

  public String brpoplpush(String source, String destination, int timeout) {
    return delegate.brpoplpush(source, destination, timeout);
  }

  public Boolean setbit(String key, long offset, boolean value) {
    return delegate.setbit(key, offset, value);
  }

  public Boolean setbit(String key, long offset, String value) {
    return delegate.setbit(key, offset, value);
  }

  public Boolean getbit(String key, long offset) {
    return delegate.getbit(key, offset);
  }

  public Long setrange(String key, long offset, String value) {
    return delegate.setrange(key, offset, value);
  }

  public String getrange(String key, long startOffset, long endOffset) {
    return delegate.getrange(key, startOffset, endOffset);
  }

  public Long bitpos(String key, boolean value) {
    return delegate.bitpos(key, value);
  }

  public Long bitpos(String key, boolean value, BitPosParams params) {
    return delegate.bitpos(key, value, params);
  }

  public List<String> configGet(String pattern) {
    return delegate.configGet(pattern);
  }

  public String configSet(String parameter, String value) {
    return delegate.configSet(parameter, value);
  }

  public Object eval(String script, int keyCount, String... params) {
    return delegate.eval(script, keyCount, params);
  }

  public void subscribe(JedisPubSub jedisPubSub, String... channels) {
    delegate.subscribe(jedisPubSub, channels);
  }

  public Long publish(String channel, String message) {
    return delegate.publish(channel, message);
  }

  public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
    delegate.psubscribe(jedisPubSub, patterns);
  }

  public Object eval(String script, List<String> keys,
      List<String> args) {
    return delegate.eval(script, keys, args);
  }

  public Object eval(String script) {
    return delegate.eval(script);
  }

  public Object evalsha(String script) {
    return delegate.evalsha(script);
  }

  public Object evalsha(String sha1, List<String> keys,
      List<String> args) {
    return delegate.evalsha(sha1, keys, args);
  }

  public Object evalsha(String sha1, int keyCount, String... params) {
    return delegate.evalsha(sha1, keyCount, params);
  }

  public Boolean scriptExists(String sha1) {
    return delegate.scriptExists(sha1);
  }

  public List<Boolean> scriptExists(String... sha1) {
    return delegate.scriptExists(sha1);
  }

  public String scriptLoad(String script) {
    return delegate.scriptLoad(script);
  }

  public List<Slowlog> slowlogGet() {
    return delegate.slowlogGet();
  }

  public List<Slowlog> slowlogGet(long entries) {
    return delegate.slowlogGet(entries);
  }

  public Long objectRefcount(String string) {
    return delegate.objectRefcount(string);
  }

  public String objectEncoding(String string) {
    return delegate.objectEncoding(string);
  }

  public Long objectIdletime(String string) {
    return delegate.objectIdletime(string);
  }

  public Long bitcount(String key) {
    return delegate.bitcount(key);
  }

  public Long bitcount(String key, long start, long end) {
    return delegate.bitcount(key, start, end);
  }

  public Long bitop(BitOP op, String destKey, String... srcKeys) {
    return delegate.bitop(op, destKey, srcKeys);
  }

  public List<Map<String, String>> sentinelMasters() {
    return delegate.sentinelMasters();
  }

  public List<String> sentinelGetMasterAddrByName(String masterName) {
    return delegate.sentinelGetMasterAddrByName(masterName);
  }

  public Long sentinelReset(String pattern) {
    return delegate.sentinelReset(pattern);
  }

  public List<Map<String, String>> sentinelSlaves(String masterName) {
    return delegate.sentinelSlaves(masterName);
  }

  public String sentinelFailover(String masterName) {
    return delegate.sentinelFailover(masterName);
  }

  public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
    return delegate.sentinelMonitor(masterName, ip, port, quorum);
  }

  public String sentinelRemove(String masterName) {
    return delegate.sentinelRemove(masterName);
  }

  public String sentinelSet(String masterName,
      Map<String, String> parameterMap) {
    return delegate.sentinelSet(masterName, parameterMap);
  }

  public byte[] dump(String key) {
    return delegate.dump(key);
  }

  public String restore(String key, int ttl, byte[] serializedValue) {
    return delegate.restore(key, ttl, serializedValue);
  }

  @Deprecated
  public Long pexpire(String key, int milliseconds) {
    return delegate.pexpire(key, milliseconds);
  }

  public Long pexpire(String key, long milliseconds) {
    return delegate.pexpire(key, milliseconds);
  }

  public Long pexpireAt(String key, long millisecondsTimestamp) {
    return delegate.pexpireAt(key, millisecondsTimestamp);
  }

  public Long pttl(String key) {
    return delegate.pttl(key);
  }

  @Deprecated
  public String psetex(String key, int milliseconds, String value) {
    return delegate.psetex(key, milliseconds, value);
  }

  public String psetex(String key, long milliseconds, String value) {
    return delegate.psetex(key, milliseconds, value);
  }

  public String set(String key, String value, String nxxx) {
    return delegate.set(key, value, nxxx);
  }

  public String set(String key, String value, String nxxx, String expx, int time) {
    return delegate.set(key, value, nxxx, expx, time);
  }

  public String clientKill(String client) {
    return delegate.clientKill(client);
  }

  public String clientSetname(String name) {
    return delegate.clientSetname(name);
  }

  public String migrate(String host, int port, String key, int destinationDb, int timeout) {
    return delegate.migrate(host, port, key, destinationDb, timeout);
  }

  @Deprecated
  public ScanResult<String> scan(int cursor) {
    return delegate.scan(cursor);
  }

  @Deprecated
  public ScanResult<String> scan(int cursor,
      ScanParams params) {
    return delegate.scan(cursor, params);
  }

  @Deprecated
  public ScanResult<Entry<String, String>> hscan(
      String key, int cursor) {
    return delegate.hscan(key, cursor);
  }

  @Deprecated
  public ScanResult<Entry<String, String>> hscan(
      String key, int cursor, ScanParams params) {
    return delegate.hscan(key, cursor, params);
  }

  @Deprecated
  public ScanResult<String> sscan(String key, int cursor) {
    return delegate.sscan(key, cursor);
  }

  @Deprecated
  public ScanResult<String> sscan(String key, int cursor,
      ScanParams params) {
    return delegate.sscan(key, cursor, params);
  }

  @Deprecated
  public ScanResult<Tuple> zscan(String key, int cursor) {
    return delegate.zscan(key, cursor);
  }

  @Deprecated
  public ScanResult<Tuple> zscan(String key, int cursor,
      ScanParams params) {
    return delegate.zscan(key, cursor, params);
  }

  public ScanResult<String> scan(String cursor) {
    return delegate.scan(cursor);
  }

  public ScanResult<String> scan(String cursor,
      ScanParams params) {
    return delegate.scan(cursor, params);
  }

  public ScanResult<Entry<String, String>> hscan(
      String key, String cursor) {
    return delegate.hscan(key, cursor);
  }

  public ScanResult<Entry<String, String>> hscan(
      String key, String cursor, ScanParams params) {
    return delegate.hscan(key, cursor, params);
  }

  public ScanResult<String> sscan(String key, String cursor) {
    return delegate.sscan(key, cursor);
  }

  public ScanResult<String> sscan(String key, String cursor,
      ScanParams params) {
    return delegate.sscan(key, cursor, params);
  }

  public ScanResult<Tuple> zscan(String key,
      String cursor) {
    return delegate.zscan(key, cursor);
  }

  public ScanResult<Tuple> zscan(String key,
      String cursor, ScanParams params) {
    return delegate.zscan(key, cursor, params);
  }

  public String clusterNodes() {
    return delegate.clusterNodes();
  }

  public String readonly() {
    return delegate.readonly();
  }

  public String clusterMeet(String ip, int port) {
    return delegate.clusterMeet(ip, port);
  }

  public String clusterReset(Reset resetType) {
    return delegate.clusterReset(resetType);
  }

  public String clusterAddSlots(int... slots) {
    return delegate.clusterAddSlots(slots);
  }

  public String clusterDelSlots(int... slots) {
    return delegate.clusterDelSlots(slots);
  }

  public String clusterInfo() {
    return delegate.clusterInfo();
  }

  public List<String> clusterGetKeysInSlot(int slot, int count) {
    return delegate.clusterGetKeysInSlot(slot, count);
  }

  public String clusterSetSlotNode(int slot, String nodeId) {
    return delegate.clusterSetSlotNode(slot, nodeId);
  }

  public String clusterSetSlotMigrating(int slot, String nodeId) {
    return delegate.clusterSetSlotMigrating(slot, nodeId);
  }

  public String clusterSetSlotImporting(int slot, String nodeId) {
    return delegate.clusterSetSlotImporting(slot, nodeId);
  }

  public String clusterSetSlotStable(int slot) {
    return delegate.clusterSetSlotStable(slot);
  }

  public String clusterForget(String nodeId) {
    return delegate.clusterForget(nodeId);
  }

  public String clusterFlushSlots() {
    return delegate.clusterFlushSlots();
  }

  public Long clusterKeySlot(String key) {
    return delegate.clusterKeySlot(key);
  }

  public Long clusterCountKeysInSlot(int slot) {
    return delegate.clusterCountKeysInSlot(slot);
  }

  public String clusterSaveConfig() {
    return delegate.clusterSaveConfig();
  }

  public String clusterReplicate(String nodeId) {
    return delegate.clusterReplicate(nodeId);
  }

  public List<String> clusterSlaves(String nodeId) {
    return delegate.clusterSlaves(nodeId);
  }

  public String clusterFailover() {
    return delegate.clusterFailover();
  }

  public List<Object> clusterSlots() {
    return delegate.clusterSlots();
  }

  public String asking() {
    return delegate.asking();
  }

  public List<String> pubsubChannels(String pattern) {
    return delegate.pubsubChannels(pattern);
  }

  public Long pubsubNumPat() {
    return delegate.pubsubNumPat();
  }

  public Map<String, String> pubsubNumSub(String... channels) {
    return delegate.pubsubNumSub(channels);
  }

  public void close() {
    delegate.close();
  }

  public void setDataSource(Pool<Jedis> jedisPool) {
    delegate.setDataSource(jedisPool);
  }

  public Long pfadd(String key, String... elements) {
    return delegate.pfadd(key, elements);
  }

  public long pfcount(String key) {
    return delegate.pfcount(key);
  }

  public long pfcount(String... keys) {
    return delegate.pfcount(keys);
  }

  public String pfmerge(String destkey, String... sourcekeys) {
    return delegate.pfmerge(destkey, sourcekeys);
  }

  @SuppressWarnings("unchecked")
  public MessageFuture<List<String>> blpop(int timeout, String key) {
    return (MessageFuture<List<String>>) reporter.report(
        new Blpop(timeout, SafeEncoder.encode(key)));
  }

  public List<String> brpop(int timeout, String key) {
    return delegate.brpop(timeout, key);
  }

  public Long geoadd(String key, double longitude, double latitude, String member) {
    return delegate.geoadd(key, longitude, latitude, member);
  }

  public Long geoadd(String key,
      Map<String, GeoCoordinate> memberCoordinateMap) {
    return delegate.geoadd(key, memberCoordinateMap);
  }

  public Double geodist(String key, String member1, String member2) {
    return delegate.geodist(key, member1, member2);
  }

  public Double geodist(String key, String member1, String member2, GeoUnit unit) {
    return delegate.geodist(key, member1, member2, unit);
  }

  public List<String> geohash(String key, String... members) {
    return delegate.geohash(key, members);
  }

  public List<GeoCoordinate> geopos(String key,
      String... members) {
    return delegate.geopos(key, members);
  }

  public List<GeoRadiusResponse> georadius(String key, double longitude,
      double latitude, double radius, GeoUnit unit) {
    return delegate.georadius(key, longitude, latitude, radius, unit);
  }

  public List<GeoRadiusResponse> georadius(String key, double longitude,
      double latitude, double radius, GeoUnit unit,
      GeoRadiusParam param) {
    return delegate.georadius(key, longitude, latitude, radius, unit, param);
  }

  public List<GeoRadiusResponse> georadiusByMember(String key,
      String member, double radius, GeoUnit unit) {
    return delegate.georadiusByMember(key, member, radius, unit);
  }

  public List<GeoRadiusResponse> georadiusByMember(String key,
      String member, double radius, GeoUnit unit,
      GeoRadiusParam param) {
    return delegate.georadiusByMember(key, member, radius, unit, param);
  }

  public List<Long> bitfield(String key, String... arguments) {
    return delegate.bitfield(key, arguments);
  }

  public String ping() {
    return delegate.ping();
  }

  public String set(byte[] key, byte[] value) {
    return delegate.set(key, value);
  }

  public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
    return delegate.set(key, value, nxxx, expx, time);
  }

  public byte[] get(byte[] key) {
    return delegate.get(key);
  }

  public String quit() {
    return delegate.quit();
  }

  public Long exists(byte[]... keys) {
    return delegate.exists(keys);
  }

  public Boolean exists(byte[] key) {
    return delegate.exists(key);
  }

  public Long del(byte[]... keys) {
    return delegate.del(keys);
  }

  public Long del(byte[] key) {
    return delegate.del(key);
  }

  public String type(byte[] key) {
    return delegate.type(key);
  }

  public String flushDB() {
    return delegate.flushDB();
  }

  public Set<byte[]> keys(byte[] pattern) {
    return delegate.keys(pattern);
  }

  public byte[] randomBinaryKey() {
    return delegate.randomBinaryKey();
  }

  public String rename(byte[] oldkey, byte[] newkey) {
    return delegate.rename(oldkey, newkey);
  }

  public Long renamenx(byte[] oldkey, byte[] newkey) {
    return delegate.renamenx(oldkey, newkey);
  }

  public Long dbSize() {
    return delegate.dbSize();
  }

  public Long expire(byte[] key, int seconds) {
    return delegate.expire(key, seconds);
  }

  public Long expireAt(byte[] key, long unixTime) {
    return delegate.expireAt(key, unixTime);
  }

  public Long ttl(byte[] key) {
    return delegate.ttl(key);
  }

  public String select(int index) {
    return delegate.select(index);
  }

  public Long move(byte[] key, int dbIndex) {
    return delegate.move(key, dbIndex);
  }

  public String flushAll() {
    return delegate.flushAll();
  }

  public byte[] getSet(byte[] key, byte[] value) {
    return delegate.getSet(key, value);
  }

  public List<byte[]> mget(byte[]... keys) {
    return delegate.mget(keys);
  }

  public Long setnx(byte[] key, byte[] value) {
    return delegate.setnx(key, value);
  }

  public String setex(byte[] key, int seconds, byte[] value) {
    return delegate.setex(key, seconds, value);
  }

  public String mset(byte[]... keysvalues) {
    return delegate.mset(keysvalues);
  }

  public Long msetnx(byte[]... keysvalues) {
    return delegate.msetnx(keysvalues);
  }

  public Long decrBy(byte[] key, long integer) {
    return delegate.decrBy(key, integer);
  }

  public Long decr(byte[] key) {
    return delegate.decr(key);
  }

  public Long incrBy(byte[] key, long integer) {
    return delegate.incrBy(key, integer);
  }

  public Double incrByFloat(byte[] key, double integer) {
    return delegate.incrByFloat(key, integer);
  }

  public Long incr(byte[] key) {
    return delegate.incr(key);
  }

  @SuppressWarnings("unchecked")
  public MessageFuture<Long> append(byte[] key, byte[] value) {
    return (MessageFuture<Long>) reporter.report(new Append(key, value));
  }

  public byte[] substr(byte[] key, int start, int end) {
    return delegate.substr(key, start, end);
  }

  public Long hset(byte[] key, byte[] field, byte[] value) {
    return delegate.hset(key, field, value);
  }

  public byte[] hget(byte[] key, byte[] field) {
    return delegate.hget(key, field);
  }

  public Long hsetnx(byte[] key, byte[] field, byte[] value) {
    return delegate.hsetnx(key, field, value);
  }

  public String hmset(byte[] key, Map<byte[], byte[]> hash) {
    return delegate.hmset(key, hash);
  }

  public List<byte[]> hmget(byte[] key, byte[]... fields) {
    return delegate.hmget(key, fields);
  }

  public Long hincrBy(byte[] key, byte[] field, long value) {
    return delegate.hincrBy(key, field, value);
  }

  public Double hincrByFloat(byte[] key, byte[] field, double value) {
    return delegate.hincrByFloat(key, field, value);
  }

  public Boolean hexists(byte[] key, byte[] field) {
    return delegate.hexists(key, field);
  }

  public Long hdel(byte[] key, byte[]... fields) {
    return delegate.hdel(key, fields);
  }

  public Long hlen(byte[] key) {
    return delegate.hlen(key);
  }

  public Set<byte[]> hkeys(byte[] key) {
    return delegate.hkeys(key);
  }

  public List<byte[]> hvals(byte[] key) {
    return delegate.hvals(key);
  }

  public Map<byte[], byte[]> hgetAll(byte[] key) {
    return delegate.hgetAll(key);
  }

  public Long rpush(byte[] key, byte[]... strings) {
    return delegate.rpush(key, strings);
  }

  public Long lpush(byte[] key, byte[]... strings) {
    return delegate.lpush(key, strings);
  }

  public Long llen(byte[] key) {
    return delegate.llen(key);
  }

  public List<byte[]> lrange(byte[] key, long start, long end) {
    return delegate.lrange(key, start, end);
  }

  public String ltrim(byte[] key, long start, long end) {
    return delegate.ltrim(key, start, end);
  }

  public byte[] lindex(byte[] key, long index) {
    return delegate.lindex(key, index);
  }

  public String lset(byte[] key, long index, byte[] value) {
    return delegate.lset(key, index, value);
  }

  public Long lrem(byte[] key, long count, byte[] value) {
    return delegate.lrem(key, count, value);
  }

  public byte[] lpop(byte[] key) {
    return delegate.lpop(key);
  }

  public byte[] rpop(byte[] key) {
    return delegate.rpop(key);
  }

  public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
    return delegate.rpoplpush(srckey, dstkey);
  }

  public Long sadd(byte[] key, byte[]... members) {
    return delegate.sadd(key, members);
  }

  public Set<byte[]> smembers(byte[] key) {
    return delegate.smembers(key);
  }

  public Long srem(byte[] key, byte[]... member) {
    return delegate.srem(key, member);
  }

  public byte[] spop(byte[] key) {
    return delegate.spop(key);
  }

  public Set<byte[]> spop(byte[] key, long count) {
    return delegate.spop(key, count);
  }

  public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
    return delegate.smove(srckey, dstkey, member);
  }

  public Long scard(byte[] key) {
    return delegate.scard(key);
  }

  public Boolean sismember(byte[] key, byte[] member) {
    return delegate.sismember(key, member);
  }

  public Set<byte[]> sinter(byte[]... keys) {
    return delegate.sinter(keys);
  }

  public Long sinterstore(byte[] dstkey, byte[]... keys) {
    return delegate.sinterstore(dstkey, keys);
  }

  public Set<byte[]> sunion(byte[]... keys) {
    return delegate.sunion(keys);
  }

  public Long sunionstore(byte[] dstkey, byte[]... keys) {
    return delegate.sunionstore(dstkey, keys);
  }

  public Set<byte[]> sdiff(byte[]... keys) {
    return delegate.sdiff(keys);
  }

  public Long sdiffstore(byte[] dstkey, byte[]... keys) {
    return delegate.sdiffstore(dstkey, keys);
  }

  public byte[] srandmember(byte[] key) {
    return delegate.srandmember(key);
  }

  public List<byte[]> srandmember(byte[] key, int count) {
    return delegate.srandmember(key, count);
  }

  public Long zadd(byte[] key, double score, byte[] member) {
    return delegate.zadd(key, score, member);
  }

  public Long zadd(byte[] key, double score, byte[] member,
      ZAddParams params) {
    return delegate.zadd(key, score, member, params);
  }

  public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
    return delegate.zadd(key, scoreMembers);
  }

  public Long zadd(byte[] key, Map<byte[], Double> scoreMembers,
      ZAddParams params) {
    return delegate.zadd(key, scoreMembers, params);
  }

  public Set<byte[]> zrange(byte[] key, long start, long end) {
    return delegate.zrange(key, start, end);
  }

  public Long zrem(byte[] key, byte[]... members) {
    return delegate.zrem(key, members);
  }

  public Double zincrby(byte[] key, double score, byte[] member) {
    return delegate.zincrby(key, score, member);
  }

  public Double zincrby(byte[] key, double score, byte[] member,
      ZIncrByParams params) {
    return delegate.zincrby(key, score, member, params);
  }

  public Long zrank(byte[] key, byte[] member) {
    return delegate.zrank(key, member);
  }

  public Long zrevrank(byte[] key, byte[] member) {
    return delegate.zrevrank(key, member);
  }

  public Set<byte[]> zrevrange(byte[] key, long start, long end) {
    return delegate.zrevrange(key, start, end);
  }

  public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
    return delegate.zrangeWithScores(key, start, end);
  }

  public Set<Tuple> zrevrangeWithScores(byte[] key, long start,
      long end) {
    return delegate.zrevrangeWithScores(key, start, end);
  }

  public Long zcard(byte[] key) {
    return delegate.zcard(key);
  }

  public Double zscore(byte[] key, byte[] member) {
    return delegate.zscore(key, member);
  }

  public Transaction multi() {
    return delegate.multi();
  }

  @Deprecated
  public List<Object> multi(TransactionBlock jedisTransaction) {
    return delegate.multi(jedisTransaction);
  }

  public void connect() {
    delegate.connect();
  }

  public void disconnect() {
    delegate.disconnect();
  }

  public void resetState() {
    delegate.resetState();
  }

  public String watch(byte[]... keys) {
    return delegate.watch(keys);
  }

  public String unwatch() {
    return delegate.unwatch();
  }

  public List<byte[]> sort(byte[] key) {
    return delegate.sort(key);
  }

  public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
    return delegate.sort(key, sortingParameters);
  }

  public MessageFuture<List<byte[]>> blpop(int timeout, byte[]... keys) {
    return ResponseUtil.transformResponse(
        reporter.report(new Blpop(timeout, keys)), BuilderFactory.BYTE_ARRAY_LIST);
  }

  public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
    return delegate.sort(key, sortingParameters, dstkey);
  }

  public Long sort(byte[] key, byte[] dstkey) {
    return delegate.sort(key, dstkey);
  }

  public List<byte[]> brpop(int timeout, byte[]... keys) {
    return delegate.brpop(timeout, keys);
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  public MessageFuture<List<byte[]>> blpop(byte[] arg) {
    return (MessageFuture<List<byte[]>>) reporter.report(new Blpop(0, arg));
  }

  @Deprecated
  public List<byte[]> brpop(byte[] arg) {
    return delegate.brpop(arg);
  }

  @SuppressWarnings("unchecked")
  public MessageFuture<List<byte[]>> blpop(byte[]... args) {
    return (MessageFuture<List<byte[]>>) reporter.report(new Blpop(0, args));
  }

  public List<byte[]> brpop(byte[]... args) {
    return delegate.brpop(args);
  }

  public String auth(String password) {
    return delegate.auth(password);
  }

  @Deprecated
  public List<Object> pipelined(PipelineBlock jedisPipeline) {
    return delegate.pipelined(jedisPipeline);
  }

  public Pipeline pipelined() {
    return delegate.pipelined();
  }

  public Long zcount(byte[] key, double min, double max) {
    return delegate.zcount(key, min, max);
  }

  public Long zcount(byte[] key, byte[] min, byte[] max) {
    return delegate.zcount(key, min, max);
  }

  public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
    return delegate.zrangeByScore(key, min, max);
  }

  public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
    return delegate.zrangeByScore(key, min, max);
  }

  public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset,
      int count) {
    return delegate.zrangeByScore(key, min, max, offset, count);
  }

  public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset,
      int count) {
    return delegate.zrangeByScore(key, min, max, offset, count);
  }

  public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
      double max) {
    return delegate.zrangeByScoreWithScores(key, min, max);
  }

  public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min,
      byte[] max) {
    return delegate.zrangeByScoreWithScores(key, min, max);
  }

  public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
      double max, int offset, int count) {
    return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
  }

  public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min,
      byte[] max, int offset, int count) {
    return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
  }

  public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
    return delegate.zrevrangeByScore(key, max, min);
  }

  public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
    return delegate.zrevrangeByScore(key, max, min);
  }

  public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset,
      int count) {
    return delegate.zrevrangeByScore(key, max, min, offset, count);
  }

  public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset,
      int count) {
    return delegate.zrevrangeByScore(key, max, min, offset, count);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
      double min) {
    return delegate.zrevrangeByScoreWithScores(key, max, min);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
      double min, int offset, int count) {
    return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
      byte[] min) {
    return delegate.zrevrangeByScoreWithScores(key, max, min);
  }

  public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
      byte[] min, int offset, int count) {
    return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
  }

  public Long zremrangeByRank(byte[] key, long start, long end) {
    return delegate.zremrangeByRank(key, start, end);
  }

  public Long zremrangeByScore(byte[] key, double start, double end) {
    return delegate.zremrangeByScore(key, start, end);
  }

  public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
    return delegate.zremrangeByScore(key, start, end);
  }

  public Long zunionstore(byte[] dstkey, byte[]... sets) {
    return delegate.zunionstore(dstkey, sets);
  }

  public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
    return delegate.zunionstore(dstkey, params, sets);
  }

  public Long zinterstore(byte[] dstkey, byte[]... sets) {
    return delegate.zinterstore(dstkey, sets);
  }

  public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
    return delegate.zinterstore(dstkey, params, sets);
  }

  public Long zlexcount(byte[] key, byte[] min, byte[] max) {
    return delegate.zlexcount(key, min, max);
  }

  public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
    return delegate.zrangeByLex(key, min, max);
  }

  public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
    return delegate.zrangeByLex(key, min, max, offset, count);
  }

  public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
    return delegate.zrevrangeByLex(key, max, min);
  }

  public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset,
      int count) {
    return delegate.zrevrangeByLex(key, max, min, offset, count);
  }

  public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
    return delegate.zremrangeByLex(key, min, max);
  }

  public String save() {
    return delegate.save();
  }

  public String bgsave() {
    return delegate.bgsave();
  }

  public String bgrewriteaof() {
    return delegate.bgrewriteaof();
  }

  public Long lastsave() {
    return delegate.lastsave();
  }

  public String shutdown() {
    return delegate.shutdown();
  }

  public String info() {
    return delegate.info();
  }

  public String info(String section) {
    return delegate.info(section);
  }

  public void monitor(JedisMonitor jedisMonitor) {
    delegate.monitor(jedisMonitor);
  }

  public String slaveof(String host, int port) {
    return delegate.slaveof(host, port);
  }

  public String slaveofNoOne() {
    return delegate.slaveofNoOne();
  }

  public List<byte[]> configGet(byte[] pattern) {
    return delegate.configGet(pattern);
  }

  public String configResetStat() {
    return delegate.configResetStat();
  }

  public byte[] configSet(byte[] parameter, byte[] value) {
    return delegate.configSet(parameter, value);
  }

  public boolean isConnected() {
    return delegate.isConnected();
  }

  public Long strlen(byte[] key) {
    return delegate.strlen(key);
  }

  public void sync() {
    delegate.sync();
  }

  public Long lpushx(byte[] key, byte[]... string) {
    return delegate.lpushx(key, string);
  }

  public Long persist(byte[] key) {
    return delegate.persist(key);
  }

  public Long rpushx(byte[] key, byte[]... string) {
    return delegate.rpushx(key, string);
  }

  public byte[] echo(byte[] string) {
    return delegate.echo(string);
  }

  public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
      byte[] value) {
    return delegate.linsert(key, where, pivot, value);
  }

  public String debug(DebugParams params) {
    return delegate.debug(params);
  }

  public Client getClient() {
    return delegate.getClient();
  }

  public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
    return delegate.brpoplpush(source, destination, timeout);
  }

  public Boolean setbit(byte[] key, long offset, boolean value) {
    return delegate.setbit(key, offset, value);
  }

  public Boolean setbit(byte[] key, long offset, byte[] value) {
    return delegate.setbit(key, offset, value);
  }

  public Boolean getbit(byte[] key, long offset) {
    return delegate.getbit(key, offset);
  }

  public Long bitpos(byte[] key, boolean value) {
    return delegate.bitpos(key, value);
  }

  public Long bitpos(byte[] key, boolean value, BitPosParams params) {
    return delegate.bitpos(key, value, params);
  }

  public Long setrange(byte[] key, long offset, byte[] value) {
    return delegate.setrange(key, offset, value);
  }

  public byte[] getrange(byte[] key, long startOffset, long endOffset) {
    return delegate.getrange(key, startOffset, endOffset);
  }

  public Long publish(byte[] channel, byte[] message) {
    return delegate.publish(channel, message);
  }

  public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
    delegate.subscribe(jedisPubSub, channels);
  }

  public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
    delegate.psubscribe(jedisPubSub, patterns);
  }

  public Long getDB() {
    return delegate.getDB();
  }

  public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
    return delegate.eval(script, keys, args);
  }

  public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
    return delegate.eval(script, keyCount, params);
  }

  public Object eval(byte[] script, int keyCount, byte[]... params) {
    return delegate.eval(script, keyCount, params);
  }

  public Object eval(byte[] script) {
    return delegate.eval(script);
  }

  public Object evalsha(byte[] sha1) {
    return delegate.evalsha(sha1);
  }

  public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
    return delegate.evalsha(sha1, keys, args);
  }

  public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
    return delegate.evalsha(sha1, keyCount, params);
  }

  public String scriptFlush() {
    return delegate.scriptFlush();
  }

  public Long scriptExists(byte[] sha1) {
    return delegate.scriptExists(sha1);
  }

  public List<Long> scriptExists(byte[]... sha1) {
    return delegate.scriptExists(sha1);
  }

  public byte[] scriptLoad(byte[] script) {
    return delegate.scriptLoad(script);
  }

  public String scriptKill() {
    return delegate.scriptKill();
  }

  public String slowlogReset() {
    return delegate.slowlogReset();
  }

  public Long slowlogLen() {
    return delegate.slowlogLen();
  }

  public List<byte[]> slowlogGetBinary() {
    return delegate.slowlogGetBinary();
  }

  public List<byte[]> slowlogGetBinary(long entries) {
    return delegate.slowlogGetBinary(entries);
  }

  public Long objectRefcount(byte[] key) {
    return delegate.objectRefcount(key);
  }

  public byte[] objectEncoding(byte[] key) {
    return delegate.objectEncoding(key);
  }

  public Long objectIdletime(byte[] key) {
    return delegate.objectIdletime(key);
  }

  public Long bitcount(byte[] key) {
    return delegate.bitcount(key);
  }

  public Long bitcount(byte[] key, long start, long end) {
    return delegate.bitcount(key, start, end);
  }

  public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
    return delegate.bitop(op, destKey, srcKeys);
  }

  public byte[] dump(byte[] key) {
    return delegate.dump(key);
  }

  public String restore(byte[] key, int ttl, byte[] serializedValue) {
    return delegate.restore(key, ttl, serializedValue);
  }

  @Deprecated
  public Long pexpire(byte[] key, int milliseconds) {
    return delegate.pexpire(key, milliseconds);
  }

  public Long pexpire(byte[] key, long milliseconds) {
    return delegate.pexpire(key, milliseconds);
  }

  public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
    return delegate.pexpireAt(key, millisecondsTimestamp);
  }

  public Long pttl(byte[] key) {
    return delegate.pttl(key);
  }

  @Deprecated
  public String psetex(byte[] key, int milliseconds, byte[] value) {
    return delegate.psetex(key, milliseconds, value);
  }

  public String psetex(byte[] key, long milliseconds, byte[] value) {
    return delegate.psetex(key, milliseconds, value);
  }

  public String set(byte[] key, byte[] value, byte[] nxxx) {
    return delegate.set(key, value, nxxx);
  }

  public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, int time) {
    return delegate.set(key, value, nxxx, expx, time);
  }

  public String clientKill(byte[] client) {
    return delegate.clientKill(client);
  }

  public String clientGetname() {
    return delegate.clientGetname();
  }

  public String clientList() {
    return delegate.clientList();
  }

  public String clientSetname(byte[] name) {
    return delegate.clientSetname(name);
  }

  public List<String> time() {
    return delegate.time();
  }

  public String migrate(byte[] host, int port, byte[] key, int destinationDb, int timeout) {
    return delegate.migrate(host, port, key, destinationDb, timeout);
  }

  public Long waitReplicas(int replicas, long timeout) {
    return delegate.waitReplicas(replicas, timeout);
  }

  public Long pfadd(byte[] key, byte[]... elements) {
    return delegate.pfadd(key, elements);
  }

  public long pfcount(byte[] key) {
    return delegate.pfcount(key);
  }

  public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
    return delegate.pfmerge(destkey, sourcekeys);
  }

  public Long pfcount(byte[]... keys) {
    return delegate.pfcount(keys);
  }

  public ScanResult<byte[]> scan(byte[] cursor) {
    return delegate.scan(cursor);
  }

  public ScanResult<byte[]> scan(byte[] cursor,
      ScanParams params) {
    return delegate.scan(cursor, params);
  }

  public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key,
      byte[] cursor) {
    return delegate.hscan(key, cursor);
  }

  public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key,
      byte[] cursor, ScanParams params) {
    return delegate.hscan(key, cursor, params);
  }

  public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
    return delegate.sscan(key, cursor);
  }

  public ScanResult<byte[]> sscan(byte[] key, byte[] cursor,
      ScanParams params) {
    return delegate.sscan(key, cursor, params);
  }

  public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
    return delegate.zscan(key, cursor);
  }

  public ScanResult<Tuple> zscan(byte[] key, byte[] cursor,
      ScanParams params) {
    return delegate.zscan(key, cursor, params);
  }

  public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
    return delegate.geoadd(key, longitude, latitude, member);
  }

  public Long geoadd(byte[] key,
      Map<byte[], GeoCoordinate> memberCoordinateMap) {
    return delegate.geoadd(key, memberCoordinateMap);
  }

  public Double geodist(byte[] key, byte[] member1, byte[] member2) {
    return delegate.geodist(key, member1, member2);
  }

  public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
    return delegate.geodist(key, member1, member2, unit);
  }

  public List<byte[]> geohash(byte[] key, byte[]... members) {
    return delegate.geohash(key, members);
  }

  public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
    return delegate.geopos(key, members);
  }

  public List<GeoRadiusResponse> georadius(byte[] key, double longitude,
      double latitude, double radius, GeoUnit unit) {
    return delegate.georadius(key, longitude, latitude, radius, unit);
  }

  public List<GeoRadiusResponse> georadius(byte[] key, double longitude,
      double latitude, double radius, GeoUnit unit,
      GeoRadiusParam param) {
    return delegate.georadius(key, longitude, latitude, radius, unit, param);
  }

  public List<GeoRadiusResponse> georadiusByMember(byte[] key,
      byte[] member, double radius, GeoUnit unit) {
    return delegate.georadiusByMember(key, member, radius, unit);
  }

  public List<GeoRadiusResponse> georadiusByMember(byte[] key,
      byte[] member, double radius, GeoUnit unit,
      GeoRadiusParam param) {
    return delegate.georadiusByMember(key, member, radius, unit, param);
  }

  public List<byte[]> bitfield(byte[] key, byte[]... arguments) {
    return delegate.bitfield(key, arguments);
  }
}

package io.github.tramchamploo.bufferslayer;

import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Send buffered commands in delegate's pipeline
 */
final class JedisSender implements Sender<RedisCommand, Object> {

  private final Jedis delegate;

  JedisSender(Jedis delegate) {
    this.delegate = delegate;
  }

  @Override
  public CheckResult check() {
    try {
      String ping = delegate.ping();
      if ("PONG".equalsIgnoreCase(ping)) {
        return CheckResult.OK;
      }
      return CheckResult.failed(new RuntimeException("PING doesn't get PONG."));
    } catch (Exception e) {
      return CheckResult.failed(e);
    }
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public List<Object> send(List<RedisCommand> messages) {
    Pipeline pipeline = delegate.pipelined();

    for (RedisCommand command: messages) {
      command.apply(pipeline);
    }
    return pipeline.syncAndReturnAll();
  }
}

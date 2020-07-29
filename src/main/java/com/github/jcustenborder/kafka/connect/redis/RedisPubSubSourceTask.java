/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.redis;

import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubListener;
import io.lettuce.core.pubsub.RedisPubSubListener;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RedisPubSubSourceTask extends AbstractRedisPubSubSourceTask<RedisPubSubSourceConnectorConfig>
    implements RedisPubSubListener<byte[], byte[]>, RedisClusterPubSubListener<byte[], byte[]> {
  private static final Logger log = LoggerFactory.getLogger(AbstractRedisCacheSinkTask.class);

  @Override
  public void start(Map<String, String> settings) {
    super.start(settings);
    this.session.connection().addListener(this);
    log.info("start() - Subscribing to {}", this.config.channels);
    byte[][] subscriptions = this.config.channels.stream()
        .map(s -> s.getBytes(this.config.charset))
        .toArray(byte[][]::new);
    this.session.asyncCommands().subscribe(subscriptions);
  }

  @Override
  protected RedisPubSubSourceConnectorConfig config(Map<String, String> settings) {
    return new RedisPubSubSourceConnectorConfig(settings);
  }

  void addRecord(byte[] channel, byte[] message) {
    String topic = new String(channel, this.config.charset);
    log.trace("addRecord() - topic = '{}'", topic);

    this.records.add(
        new SourceRecord(
            null,
            null,
            topic,
            null,
            null,
            null,
            Schema.BYTES_SCHEMA,
            message
        )
    );
  }

  @Override
  public void message(byte[] channel, byte[] message) {
    addRecord(channel, message);
  }

  @Override
  public void message(byte[] pattern, byte[] channel, byte[] message) {
    addRecord(channel, message);
  }

  @Override
  public void subscribed(byte[] channel, long count) {

  }

  @Override
  public void psubscribed(byte[] pattern, long count) {

  }

  @Override
  public void unsubscribed(byte[] channel, long count) {

  }

  @Override
  public void punsubscribed(byte[] pattern, long count) {

  }

  @Override
  public void message(RedisClusterNode node, byte[] channel, byte[] message) {
    addRecord(channel, message);
  }

  @Override
  public void message(RedisClusterNode node, byte[] pattern, byte[] channel, byte[] message) {
    addRecord(channel, message);
  }

  @Override
  public void subscribed(RedisClusterNode node, byte[] channel, long count) {

  }

  @Override
  public void psubscribed(RedisClusterNode node, byte[] channel, long count) {

  }

  @Override
  public void unsubscribed(RedisClusterNode node, byte[] channel, long count) {

  }

  @Override
  public void punsubscribed(RedisClusterNode node, byte[] channel, long count) {

  }
}

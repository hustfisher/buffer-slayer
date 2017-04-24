package io.github.tramchamploo.bufferslayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import redis.clients.util.SafeEncoder;

/**
 * Factory for {@link Builder} that builds result
 */
final class BuilderFactory {

  @SuppressWarnings("unchecked")
  static final Builder<List<byte[]>> BYTE_ARRAY_LIST = new Builder<List<byte[]>>() {
    @Override
    List<byte[]> build(Object data) {
      if (null == data) {
        return Collections.emptyList();
      }
      List<String> l = (List<String>) data;
      final ArrayList<byte[]> result = new ArrayList<>(l.size());
      for (final String s: l) {
        if (s == null) {
          result.add(null);
        } else {
          result.add(SafeEncoder.encode(s));
        }
      }
      return result;
    }
  };
}

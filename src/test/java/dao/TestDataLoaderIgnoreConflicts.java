package dao;

import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.Response;
import gov.nasa.pds.registry.common.RestClient;
import gov.nasa.pds.registry.common.es.dao.DataLoader;
import gov.nasa.pds.registry.common.es.dao.dd.LddVersions;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for DataLoader.ignoreConflicts flag and related behaviour.
 *
 * <p>These tests call the real DataLoader.processErrors() via reflection so that production
 * conflict-handling logic is actually exercised, not duplicated.
 */
public class TestDataLoaderIgnoreConflicts {

  // Minimal Response.Bulk.Item stub
  private static Response.Bulk.Item item(
      String operation, int status, boolean error, String id) {
    return new Response.Bulk.Item() {
      public boolean error() {
        return error;
      }

      public String id() {
        return id;
      }

      public String index() {
        return "test";
      }

      public String operation() {
        return operation;
      }

      public String reason() {
        return "conflict";
      }

      public String result() {
        return null;
      }

      public int status() {
        return status;
      }
    };
  }

  // Minimal Response.Bulk stub
  private static Response.Bulk bulkResponse(boolean errors, List<Response.Bulk.Item> items) {
    return new Response.Bulk() {
      public boolean errors() {
        return errors;
      }

      public List<Response.Bulk.Item> items() {
        return items;
      }

      public void logErrors() { /* no-op stub */ }

      public long took() {
        return 0;
      }
    };
  }

  /**
   * Constructs a real DataLoader with a no-op ConnectionFactory stub and sets ignoreConflicts via
   * the public setter. Exposes processErrors() via reflection.
   */
  private static int invokeProcessErrors(
      boolean ignoreConflicts,
      Response.Bulk resp,
      Set<String> errorLidvids,
      LinkedHashMap<String, String> todo,
      int retry)
      throws Exception {
    ConnectionFactory stub = new StubConnectionFactory();
    DataLoader loader = new DataLoader(stub);
    loader.setIgnoreConflicts(ignoreConflicts);

    Method m =
        DataLoader.class.getDeclaredMethod(
            "processErrors", Response.Bulk.class, Set.class, LinkedHashMap.class, int.class);
    m.setAccessible(true);
    return (int) m.invoke(loader, resp, errorLidvids, todo, retry);
  }

  @Test
  void lddVersions_defaultLastDate_isPublicConstant() {
    assertNotNull(LddVersions.DEFAULT_LAST_DATE);
    assertEquals(
        LddVersions.DEFAULT_LAST_DATE,
        new LddVersions().lastDate,
        "DEFAULT_LAST_DATE must equal the initial lastDate of a new LddVersions");
    assertEquals(
        Instant.parse(LddVersions.DEFAULT_DATE),
        LddVersions.DEFAULT_LAST_DATE,
        "DEFAULT_LAST_DATE and DEFAULT_DATE must represent the same instant");
  }

  @Test
  void processErrors_409_ignoreConflicts_false_countsAsError() throws Exception {
    Response.Bulk.Item conflict409 = item("create", 409, true, "urn:test::1.0");
    Response.Bulk resp = bulkResponse(true, Arrays.asList(conflict409));
    LinkedHashMap<String, String> todo = new LinkedHashMap<>();
    todo.put("{\"create\":{\"_id\":\"urn:test::1.0\"}}", "{}");

    int errors = invokeProcessErrors(false, resp, null, todo, 0);

    assertEquals(1, errors, "409 with ignoreConflicts=false must count as 1 error");
    assertTrue(todo.isEmpty(), "409 item must be removed from todo regardless of ignoreConflicts");
  }

  @Test
  void processErrors_409_ignoreConflicts_true_notCountedAsError() throws Exception {
    Response.Bulk.Item conflict409 = item("create", 409, true, "urn:test::1.0");
    Response.Bulk resp = bulkResponse(true, Arrays.asList(conflict409));
    LinkedHashMap<String, String> todo = new LinkedHashMap<>();
    todo.put("{\"create\":{\"_id\":\"urn:test::1.0\"}}", "{}");

    int errors = invokeProcessErrors(true, resp, null, todo, 0);

    assertEquals(0, errors, "409 with ignoreConflicts=true must not count as an error");
    assertTrue(todo.isEmpty(), "409 item must still be removed from todo");
  }

  @Test
  void processErrors_nonConflictError_alwaysCountsRegardlessOfFlag() throws Exception {
    for (boolean flag : new boolean[] {false, true}) {
      Response.Bulk.Item serverError = item("index", 500, true, "urn:test::1.0");
      Response.Bulk resp = bulkResponse(true, Arrays.asList(serverError));
      LinkedHashMap<String, String> todo = new LinkedHashMap<>();
      todo.put("{\"index\":{\"_id\":\"urn:test::1.0\"}}", "{}");

      int errors = invokeProcessErrors(flag, resp, null, todo, 0);

      assertEquals(
          1,
          errors,
          "Non-409 error must always count regardless of ignoreConflicts=" + flag);
    }
  }

  @Test
  void processErrors_successItem_zeroErrors() throws Exception {
    Response.Bulk.Item success = item("index", 200, false, "urn:test::1.0");
    Response.Bulk resp = bulkResponse(false, Arrays.asList(success));
    LinkedHashMap<String, String> todo = new LinkedHashMap<>();
    todo.put("{\"index\":{\"_id\":\"urn:test::1.0\"}}", "{}");

    int errors = invokeProcessErrors(false, resp, null, todo, 0);

    assertEquals(0, errors);
    assertTrue(todo.isEmpty());
  }

  /**
   * Minimal ConnectionFactory stub. Only getIndexName() is called by processErrors() (via the
   * debug log path when ignoreConflicts=true). All other methods are unused in these unit tests.
   */
  private static class StubConnectionFactory implements ConnectionFactory {

    public ConnectionFactory clone() {
      return this;
    }

    public RestClient createRestClient() {
      throw new UnsupportedOperationException();
    }

    public CredentialsProvider getCredentials() {
      return null;
    }

    public org.apache.hc.client5.http.auth.CredentialsProvider getCredentials5() {
      return null;
    }

    public HttpHost getHost() {
      return null;
    }

    public org.apache.hc.core5.http.HttpHost getHost5() {
      return null;
    }

    public String getHostName() {
      return "stub-host";
    }

    public String getIndexName() {
      return "stub-index";
    }

    public boolean isTrustingSelfSigned() {
      return false;
    }

    public void reconnect() throws IOException, InterruptedException { /* no-op */ }

    public ConnectionFactory setIndexName(String idxName) {
      return this;
    }
  }
}

package gov.nasa.pds.registry.common.connection.es;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The data dictionary can hold well over 1000 fields of a given type, so the "list fields" request
 * must not be limited to the old hardcoded page size of 1000.
 */
class TestJsonHelperListFields {

  @Test
  void buildListFieldsRequest_requestsFullResultWindow() {
    JsonObject request = JsonParser.parseString(JsonHelper.buildListFieldsRequest("date")).getAsJsonObject();

    assertEquals(JsonHelper.MAX_LIST_FIELDS_SIZE, request.get("size").getAsInt());
    assertTrue(JsonHelper.MAX_LIST_FIELDS_SIZE > 1000);
  }
}

package gov.nasa.pds.registry.common.connection.es;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The SDK1 "list fields" request is paged the same way as the AWS one: sorted by es_field_name,
 * with search_after carrying the last name of the previous page.
 */
class TestJsonHelperListFields {

  private static JsonObject parse(String json) {
    return JsonParser.parseString(json).getAsJsonObject();
  }

  @Test
  void firstPage_isSortedByFieldNameWithoutSearchAfter() {
    JsonObject request = parse(JsonHelper.buildListFieldsRequest("date", 500, null));

    assertEquals(500, request.get("size").getAsInt());
    assertEquals("asc",
        request.getAsJsonArray("sort").get(0).getAsJsonObject().get("es_field_name").getAsString());
    assertFalse(request.has("search_after"));
  }

  @Test
  void nextPage_searchesAfterTheLastName() {
    JsonObject request = parse(JsonHelper.buildListFieldsRequest("date", 500, "pds:Time/pds:stop"));

    assertEquals("pds:Time/pds:stop", request.getAsJsonArray("search_after").get(0).getAsString());
  }

  @Test
  void withoutPaging_keepsTheOriginalPageSize() {
    JsonObject request = parse(JsonHelper.buildListFieldsRequest("date"));

    assertEquals(JsonHelper.LIST_FIELDS_PAGE_SIZE, request.get("size").getAsInt());
  }
}

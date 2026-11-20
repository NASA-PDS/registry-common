package gov.nasa.pds.registry.common.connection.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.core.SearchRequest;

/**
 * The data dictionary can hold more fields of a type than fit in one page, so the "list fields"
 * request is sorted by es_field_name and paged with search_after instead of asking for a bigger
 * page than the index's max_result_window may allow.
 */
class TestSearchImplListFields {

  @Test
  void buildListFields_firstPage_isSortedByFieldNameWithoutSearchAfter() {
    SearchImpl search = new SearchImpl();
    search.buildListFields("date", 500, null).setIndex("registry-dd");

    SearchRequest request = search.craftsman.build();

    assertEquals(500, request.size());
    assertEquals("es_field_name", request.sort().get(0).field().field());
    assertTrue(request.searchAfter() == null || request.searchAfter().isEmpty());
  }

  @Test
  void buildListFields_nextPage_searchesAfterTheLastName() {
    SearchImpl search = new SearchImpl();
    search.buildListFields("date", 500, "pds:Time_Coordinates/pds:stop_date_time").setIndex("registry-dd");

    SearchRequest request = search.craftsman.build();

    assertEquals(1, request.searchAfter().size());
    assertEquals("pds:Time_Coordinates/pds:stop_date_time", request.searchAfter().get(0).stringValue());
  }

  @Test
  void buildListFields_withoutPaging_keepsTheOriginalPageSize() {
    SearchImpl search = new SearchImpl();
    search.buildListFields("date").setIndex("registry-dd");

    assertEquals(SearchImpl.LIST_FIELDS_PAGE_SIZE, search.craftsman.build().size());
  }
}

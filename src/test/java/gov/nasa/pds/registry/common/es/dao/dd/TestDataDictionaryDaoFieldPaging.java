package gov.nasa.pds.registry.common.es.dao.dd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import gov.nasa.pds.registry.common.Request;
import gov.nasa.pds.registry.common.Response;
import gov.nasa.pds.registry.common.RestClient;

/**
 * getFieldNamesByEsType must return every field of the type, however many pages that takes, so no
 * value of a field past the first page reaches OpenSearch unnormalized.
 */
class TestDataDictionaryDaoFieldPaging {

  /** A data dictionary of sorted field names answered page by page, recording each search_after. */
  private static class FakeDictionary {
    final List<String> names;
    final List<String> cursors = new ArrayList<>();

    FakeDictionary(int count) {
      TreeSet<String> sorted = new TreeSet<>();
      for (int i = 0; i < count; i++) sorted.add(String.format("pds:Class/pds:date_%05d", i));
      this.names = new ArrayList<>(sorted);
    }

    RestClient client() {
      return (RestClient) Proxy.newProxyInstance(RestClient.class.getClassLoader(),
          new Class<?>[] {RestClient.class}, (self, m, args) -> {
            switch (m.getName()) {
              case "createSearchRequest": return search();
              case "performRequest": return ((PageRequest) args[0]).response();
              default: throw new UnsupportedOperationException(m.getName());
            }
          });
    }

    private interface PageRequest { Response.Search response(); }

    private Request.Search search() {
      Object[] state = new Object[2]; // size, searchAfter
      return (Request.Search) Proxy.newProxyInstance(Request.Search.class.getClassLoader(),
          new Class<?>[] {Request.Search.class, PageRequest.class}, (self, m, args) -> {
            switch (m.getName()) {
              case "buildListFields":
                state[0] = args[1];
                state[1] = args[2];
                return self;
              case "setIndex": return self;
              case "response": return page((Integer) state[0], (String) state[1]);
              default: throw new UnsupportedOperationException(m.getName());
            }
          });
    }

    private Response.Search page(int size, String searchAfter) {
      cursors.add(searchAfter);
      Set<String> page = new LinkedHashSet<>();
      for (String name : names) {
        if (searchAfter != null && name.compareTo(searchAfter) <= 0) continue;
        if (page.size() == size) break;
        page.add(name);
      }
      return (Response.Search) Proxy.newProxyInstance(Response.Search.class.getClassLoader(),
          new Class<?>[] {Response.Search.class}, (self, m, args) -> {
            if (m.getName().equals("fields")) return page;
            throw new UnsupportedOperationException(m.getName());
          });
    }
  }

  @Test
  void everyFieldIsReturned_whenTheDictionarySpansSeveralPages() throws Exception {
    int count = DataDictionaryDao.LIST_FIELDS_PAGE_SIZE * 2 + 37;
    FakeDictionary dd = new FakeDictionary(count);

    Set<String> names = new DataDictionaryDao(dd.client(), "registry").getFieldNamesByEsType("date");

    assertEquals(count, names.size());
    assertEquals(new LinkedHashSet<>(dd.names), names);
    assertEquals(Arrays.asList(null, dd.names.get(999), dd.names.get(1999)), dd.cursors);
  }

  @Test
  void anExactMultipleOfThePageSize_endsWithOneEmptyPage() throws Exception {
    FakeDictionary dd = new FakeDictionary(DataDictionaryDao.LIST_FIELDS_PAGE_SIZE);

    Set<String> names = new DataDictionaryDao(dd.client(), "registry").getFieldNamesByEsType("date");

    assertEquals(DataDictionaryDao.LIST_FIELDS_PAGE_SIZE, names.size());
    assertEquals(2, dd.cursors.size());
  }

  @Test
  void aSinglePage_needsOneRequest() throws Exception {
    FakeDictionary dd = new FakeDictionary(12);

    assertEquals(12, new DataDictionaryDao(dd.client(), "registry").getFieldNamesByEsType("boolean").size());
    assertEquals(1, dd.cursors.size());
  }
}

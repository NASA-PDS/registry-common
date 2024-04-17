package gov.nasa.pds.registry.common.connection.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import gov.nasa.pds.registry.common.Response;
import gov.nasa.pds.registry.common.es.dao.dd.LddInfo;
import gov.nasa.pds.registry.common.es.dao.dd.LddVersions;

@SuppressWarnings("unchecked") // JSON heterogenous structures requires raw casting
class SearchRespWrap implements Response.Search {
  final private SearchResponse<Object> parent;
  SearchRespWrap(SearchResponse<Object> parent) {
    this.parent = parent;
  }
  @Override
  public Map<String, Set<String>> altIds() throws UnsupportedOperationException, IOException {
    HashMap<String, Set<String>> results = new HashMap<String, Set<String>>();
    if (true) throw new NotImplementedException("Need to fill this out when have a return value");
    return results;
  }
  @Override
  public Set<String> fields() throws UnsupportedOperationException, IOException {
    Set<String> results = new HashSet<String>();
    for (Hit<Object> hit : this.parent.hits().hits()) {
      for (String value : ((Map<String,String>)hit.source()).values()) {
        results.add(value);
      }
    }
    return results;
  }
  @Override
  public List<String> latestLidvids() {
    ArrayList<String> lidvids = new ArrayList<String>();
    if (true) throw new NotImplementedException("Need to fill this out when have a return value");
    return lidvids;
  }
  @Override
  public LddVersions lddInfo() throws UnsupportedOperationException, IOException {
    LddVersions result = new LddVersions();
    for (Hit<Object> hit : this.parent.hits().hits()) {
      Map<String,String> source = (Map<String,String>)hit.source();
      if (source.containsKey("attr_name") && source.containsKey("date")) {
        result.addSchemaFile(source.get("attr_name"));
        result.updateDate(source.get("date"));
      } else {
        throw new UnsupportedOperationException("Either date or attr_name or both are missing from hit.");
      }
    }
    return result;
  }
  @Override
  public List<LddInfo> ldds() throws UnsupportedOperationException, IOException {
    ArrayList<LddInfo> results = new ArrayList<LddInfo>();
    if (true) throw new NotImplementedException("Need to fill this out when have a return value");
    return results;
  }
  @Override
  public Set<String> nonExistingIds(Collection<String> from_ids)
      throws UnsupportedOperationException, IOException {
    HashSet<String> results = new HashSet<String>(from_ids);
    for (Hit<Object> hit : this.parent.hits().hits()) {
      if (true) throw new NotImplementedException("Need to fill this out when have a return value");
    }
    return results;
  }
}
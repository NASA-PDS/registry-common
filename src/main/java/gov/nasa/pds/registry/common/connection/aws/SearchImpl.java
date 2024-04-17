package gov.nasa.pds.registry.common.connection.aws;

import java.util.ArrayList;
import java.util.Collection;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.IdsQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.Query.Builder;
import org.opensearch.client.opensearch._types.query_dsl.TermsQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermsQueryField;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import gov.nasa.pds.registry.common.Request.Search;

class SearchImpl implements Search {
  final SearchRequest.Builder craftsman = new SearchRequest.Builder();
  private void buildIds (Collection<String> lids, boolean alt) {
    SourceConfig.Builder journeyman = new SourceConfig.Builder();
    if (alt) {
      journeyman.filter(new SourceFilter.Builder().includes("alternate_ids").build());
    }
    this.craftsman.query(new Query.Builder().ids(new IdsQuery.Builder().values(new ArrayList<String>(lids)).build()).build());
    this.craftsman.size(lids.size());
    this.craftsman.source(journeyman.fetch(alt).build());
  }
  private Query.Builder matchQuery (String fieldname, String fieldvalue) {
    return (Builder)new Query.Builder().match(new MatchQuery.Builder().field(fieldname).query(new FieldValue.Builder().stringValue(fieldvalue).build()).build());
  }
  @Override
  public Search buildAlternativeIds(Collection<String> lids) {
    this.buildIds(lids, true);
    return this;
  }
  @Override
  public Search buildLatestLidVids(Collection<String> lids) {
    ArrayList<FieldValue> terms = new ArrayList<FieldValue>(lids.size());
    for (String lid : lids) {
      terms.add(new FieldValue.Builder().stringValue(lid).build());
    }
    // FIXME: need to work out aggregates
    // this.craftsman.aggregations("latest", );
    this.craftsman.query(new Query.Builder().terms(new TermsQuery.Builder().field("lid").terms(new TermsQueryField.Builder().value(terms).build()).build()).build());
    this.craftsman.size(0);
    this.craftsman.source(new SourceConfig.Builder().fetch(false).build());
    return this;
  }
  @Override
  public Search buildListFields(String dataType) {
    this.craftsman.query(new Query.Builder().bool(new BoolQuery.Builder().must(this.matchQuery("es_data_type", dataType).build()).build()).build());
    this.craftsman.size(1000); // have no idea why hardcoded but it is (.es.JsonHelper:217
    this.craftsman.source(new SourceConfig.Builder().filter(new SourceFilter.Builder().includes("es_field_name").build()).build());
    return this;
  }
  @Override
  public Search buildListLdds(String namespace) {
    BoolQuery.Builder journeyman = new BoolQuery.Builder()
        .must(this.matchQuery("class_ns", "registry").build(),
            this.matchQuery("class_name", "LDD_Info").build(),
            this.matchQuery("attr_ns", namespace).build());
    this.craftsman.query(new Query.Builder().bool(journeyman.build()).build());
    this.craftsman.size(1000); // have no idea why hardcoded but it is (.es.JsonHelper:265
    this.craftsman.source(new SourceConfig.Builder().filter(new SourceFilter.Builder().includes("date", "attr_name").build()).build());
    return this;
  }
  @Override
  public Search buildTheseIds(Collection<String> lids) {
    this.buildIds(lids, false);
    return this;
  }
  @Override
  public Search setIndex(String name) {
    this.craftsman.index(name);
    return this;
  }
  @Override
  public Search setPretty(boolean pretty) {
    // ignored because Java v2 returns a document not JSON
    return this;
  }
}
package gov.nasa.pds.registry.common.es.dao.dd;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import gov.nasa.pds.registry.common.Request;
import gov.nasa.pds.registry.common.ResponseException;
import gov.nasa.pds.registry.common.RestClient;
import gov.nasa.pds.registry.common.util.Tuple;


  /**
 * Data dictionary DAO (Data Access Object). This class provides methods to read and update data
 * dictionary.
 * 
 * @author karpenko
 */
public class DataDictionaryDao {
  private RestClient client;
  private String indexName;


  /**
   * Constructor
   * 
   * @param client Elasticsearch client
   * @param indexName Elasticsearch index name
   */
  public DataDictionaryDao(RestClient client, String indexName) {
    this.client = client;
    this.indexName = indexName;
  }



  /**
   * Get LDD date from data dictionary index in Elasticsearch.
   * 
   * @param namespace LDD namespace, e.g., "pds", "geom", etc.
   * @return ISO instant class representing LDD date.
   * @throws IOException
   * @throws ResponseException
   * @throws UnsupportedOperationException
   * @throws Exception an exception
   */
  public LddVersions getLddInfo(String namespace)
      throws UnsupportedOperationException, IOException {
    Request.Search req =
        client.createSearchRequest().buildListLdds(namespace).setIndex(indexName + "-dd");
    return client.performRequest(req).lddInfo();
  }

    /**
   * Get LDD date from data dictionary index in Elasticsearch. Force skip the OpenSearch cache.
   * 
   * @param namespace LDD namespace, e.g., "pds", "geom", etc.
   * @return ISO instant class representing LDD date.
   * @throws IOException
   * @throws ResponseException
   * @throws UnsupportedOperationException
   * @throws Exception an exception
   */
  public LddVersions getLddInfoNoCache(String namespace)
      throws UnsupportedOperationException, IOException {
    Request.Search req =
        client.createSearchRequest().buildListLddsNoCache(namespace).setIndex(indexName + "-dd");
    return client.performRequest(req).lddInfo();
  }
  


  /**
   * List registered LDDs
   * 
   * @param namespace if this parameter is null list all LDDs
   * @return a list of LDDs
   * @throws IOException
   * @throws ResponseException
   * @throws UnsupportedOperationException
   * @throws Exception an exception
   */
  public List<LddInfo> listLdds(String namespace)
      throws UnsupportedOperationException, IOException {
    Request.Search req =
        client.createSearchRequest().buildListLdds(namespace).setIndex(this.indexName + "-dd");
    return client.performRequest(req).ldds();
  }

  /**
   * Get field names by Elasticsearch type, such as "boolean" or "date".
   * 
   * @return a set of field names
   * @throws IOException
   * @throws ResponseException
   * @throws UnsupportedOperationException
   * @throws Exception an exception
   */
  public Set<String> getFieldNamesByEsType(String esType)
      throws UnsupportedOperationException, IOException {
    Request.Search req =
        client.createSearchRequest().buildListFields(esType).setIndex(this.indexName + "-dd");
    return client.performRequest(req).fields();
  }


  /**
   * Query Elasticsearch data dictionary to get data types for a list of field ids.
   *
   * @param ids A list of field IDs, e.g., "pds:Array_3D/pds:axes".
   * @return Data types information object
   * @throws DataTypeNotFoundException
   * @throws IOException
   */
  public List<Tuple> getDataTypes(Collection<String> ids)
      throws IOException, DataTypeNotFoundException {
    return getDataTypes(ids, false);
  }

  /**
   * Query Elasticsearch data dictionary to get data types for a list of field ids.
   *
   * @param ids A list of field IDs, e.g., "pds:Array_3D/pds:axes".
   * @param forceRefresh If true, force a shard refresh before the mget. Use only in targeted wait
   *        loops after bulk loading an LDD — do not use in normal query paths.
   * @return Data types information object
   * @throws DataTypeNotFoundException
   * @throws IOException
   */
  public List<Tuple> getDataTypes(Collection<String> ids, boolean forceRefresh)
      throws IOException, DataTypeNotFoundException {
    if (ids == null || ids.isEmpty())
      return null;
    Request.MGet mgetReq = client.createMGetRequest();
    if (forceRefresh) mgetReq.setRefresh(true);
    Request.Get req = mgetReq.setIds(ids).includeField("es_data_type")
        .setIndex(this.indexName + "-dd");
    return this.client.performRequest(req).dataTypes();
  }

}

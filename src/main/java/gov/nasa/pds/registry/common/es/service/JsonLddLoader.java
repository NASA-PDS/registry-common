package gov.nasa.pds.registry.common.es.service;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.dd.LddEsJsonWriter;
import gov.nasa.pds.registry.common.dd.LddUtils;
import gov.nasa.pds.registry.common.dd.Pds2EsDataTypeMap;
import gov.nasa.pds.registry.common.dd.parser.AttributeDictionaryParser;
import gov.nasa.pds.registry.common.dd.parser.ClassAttrAssociationParser;
import gov.nasa.pds.registry.common.dd.parser.DDAttribute;
import gov.nasa.pds.registry.common.es.dao.DataLoader;
import gov.nasa.pds.registry.common.es.dao.dd.DataDictionaryDao;
import gov.nasa.pds.registry.common.es.dao.dd.DataTypeNotFoundException;
import gov.nasa.pds.registry.common.es.dao.dd.LddVersions;


/**
 * Loads PDS LDD JSON file into Elasticsearch data dictionary index
 * 
 * @author karpenko
 */
public class JsonLddLoader {
  private Logger log;

  private Pds2EsDataTypeMap dtMap;
  private DataLoader loader;

  private DataDictionaryDao dao;

  /**
   * Constructor
   * 
   * @param dao Data dictionary data access object
   * @param conFact instance of class gov.nasa.pds.registry.common.ConnectionFactory
   * @throws Exception an exception
   */
  public JsonLddLoader(DataDictionaryDao dao, ConnectionFactory conFact) throws Exception {
    log = LogManager.getLogger(this.getClass());
    dtMap = new Pds2EsDataTypeMap();

    loader = new DataLoader(conFact.clone().setIndexName(conFact.getIndexName() + "-dd"));
    this.dao = dao;
  }


  /**
   * Load PDS to Elasticsearch data type map
   * 
   * @param file configuration file
   * @throws Exception an exception
   */
  public void loadPds2EsDataTypeMap(File file) throws Exception {
    dtMap.load(file);
  }


  /**
   * Load PDS LDD JSON file into Elasticsearch data dictionary index
   * 
   * @param lddFile PDS LDD JSON file
   * @param namespace Namespace filter. Only load classes having this namespace.
   * @throws Exception an exception
   */
  public void load(File lddFile, String namespace) throws Exception {
    String lddFileName = lddFile.getName();
    load(lddFile, lddFileName, namespace);
  }


  /**
   * Load PDS LDD JSON file into Elasticsearch data dictionary index
   * 
   * @param lddFile PDS LDD JSON file
   * @param lddFileName file name to store in Elasticsearch (could be different from lddFile).
   *        lddFile could point to a temporary file loaded from the Internet.
   * @param namespace Namespace filter. Only load classes having this namespace.
   * @throws Exception an exception
   */
  public void load(File lddFile, String lddFileName, String namespace) throws Exception {
    // If a namespace is not provided get it from the LDD.
    // If there are more than one namespace, an exception will be thrown.
    if (namespace == null || namespace.isBlank()) {
      namespace = LddUtils.getLddNamespace(lddFile);
    }

    // get date of the current LDD for this namespace in the registry. If there is no LDD for this namespace, use epoch date.
    // TODO add the test of overwrite yes/no, remove it from where it is not >?


    // Get information about LDDs already loaded into the registry (for this namespace)
    LddVersions info = dao.getLddInfo(namespace);
    if (info.files.contains(lddFileName)) {
      log.info("This LDD already loaded.");
      return;
    }

    // Create and load temporary data file into Elasticsearch
    loadOnly(lddFile, lddFileName, namespace, info.lastDate);
  }


  /**
   * Load PDS LDD JSON file into Elasticsearch data dictionary index. Do not validate parameters.
   * This is a low level method called by other classes / methods.
   * 
   * @param lddFile PDS LDD JSON file
   * @param lddFileName file name to store in Elasticsearch (could be different from lddFile).
   *        lddFile could point to a temporary file loaded from the Internet.
   * @param namespace Namespace filter. Only load classes having this namespace.
   * @param lastDate last date of an LDD for given namespace already loaded into registry
   * @throws Exception an exception
   */
  public void loadOnly(File lddFile, String lddFileName, String namespace, Instant lastDate)
      throws Exception {
    // Create and load temporary data file into Elasticsearch
    File tempEsDataFile = File.createTempFile("es-", ".json");
    log.debug("Creating temporary ES data file " + tempEsDataFile.getAbsolutePath());

    try {
      String firstFieldId = createEsDataFile(lddFile, lddFileName, namespace, tempEsDataFile, lastDate);
      loader.loadFile(tempEsDataFile);

      // Wait until the LDD_Info sentinel is visible (search with requestCache=false).
      // This confirms the bulk load completed on the server side.
      LddVersions info = dao.getLddInfoNoCache(namespace);
      int nAttempts = 0;
      int maxAttempts = 30;
      while (info.isEmpty() && nAttempts < maxAttempts) {
        Thread.sleep(1000);
        nAttempts++;
        if (nAttempts % 60 == 0) {
          log.info("Waiting for the new LDD {} to be indexed... ({} seconds elapsed)", namespace, nAttempts);
        } else {
          log.debug("Waiting for the new LDD {} to be indexed... ({} seconds elapsed)", namespace, nAttempts);
        }
        info = dao.getLddInfoNoCache(namespace);
      }

      if (info.isEmpty()) {
        log.warn("The new LDD {} is not indexed after {} seconds. It may be indexed later, but there may be a delay in loading other LDDs for this namespace.", namespace, maxAttempts);
      } else {
        log.debug("The new LDD {} is indexed with date {}. It may take some time for it to be visible via mget.", namespace, info.lastDate);
     
      // On AWS OpenSearch Serverless (AOSS), mget and search use different visibility paths.
      // Wait until a specific field document is also visible via mget with refresh=true,
      // so that getDataTypes() calls immediately following this load will succeed.
      if (firstFieldId != null) {
        boolean fieldVisible = false;
        while (!fieldVisible) {
          try {
            dao.getDataTypesWithRefresh(Collections.singletonList(firstFieldId));
            fieldVisible = true;
          } catch (DataTypeNotFoundException e) {
            Thread.sleep(1000);
            log.debug("Waiting for field {} of namespace {} to be visible via mget...", firstFieldId, namespace);
          }
        }
      }
      log.debug("Visibility of namespace " + namespace + " has been fully validated.");
    }
    } finally {
      // Delete temporary file
      tempEsDataFile.delete();
    }
  }


  private static class CaaCallback implements ClassAttrAssociationParser.Callback {
    private final LddEsJsonWriter writer;
    private final String namespace;
    private final Map<String, DDAttribute> ddAttrCache;
    private String firstFieldId;

    public CaaCallback(LddEsJsonWriter writer, String namespace, Map<String, DDAttribute> ddAttrCache) {
      this.writer = writer;
      this.namespace = namespace;
      this.ddAttrCache = ddAttrCache;
    }

    @Override
    public void onAssociation(String classNs, String className, String attrId) throws Exception {
      writer.writeFieldDefinition(classNs, className, attrId);
      if (firstFieldId == null && namespace.equals(classNs)) {
        DDAttribute attr = ddAttrCache.get(attrId);
        if (attr != null) {
          firstFieldId = classNs + ":" + className + "/" + attr.attrNs + ":" + attr.attrName;
        }
      }
    }

    public String getFirstFieldId() {
      return firstFieldId;
    }
  }


  /**
   * Create Elasticsearch data file to be loaded into data dictionary index.
   * 
   * @param lddFile PDS LDD JSON file
   * @param namespace Namespace filter. Only load classes having this namespace.
   * @param tempEsFile Write to this Elasticsearch file
   * @throws Exception an exception
   */
  private String createEsDataFile(File lddFile, String lddFileName, String namespace, File tempEsFile,
      Instant lastDate) throws Exception {
    // Parse and cache LDD attributes
    Map<String, DDAttribute> ddAttrCache = new TreeMap<>();
    AttributeDictionaryParser.Callback acb = (attr) -> {
      ddAttrCache.put(attr.id, attr);
    };
    AttributeDictionaryParser attrParser = new AttributeDictionaryParser(lddFile, acb);
    attrParser.parse();

    // If this LDD date is after the last stored in Elasticsearch, overwrite old records
    boolean overwrite = overwriteLdd(lastDate, attrParser.getLddDate());

    // Create a writer to save LDD data in Elasticsearch JSON data file
    LddEsJsonWriter writer = null;
    try {
      writer = new LddEsJsonWriter(tempEsFile, dtMap, ddAttrCache, overwrite);
      writer.setNamespaceFilter(namespace);

      // Parse class attribute associations and write to ES data file
      CaaCallback ccb = new CaaCallback(writer, namespace, ddAttrCache);
      ClassAttrAssociationParser caaParser = new ClassAttrAssociationParser(lddFile, ccb);
      caaParser.parse();

      // Write data dictionary version and date
      writer.writeLddInfo(namespace, lddFileName, attrParser.getImVersion(),
          attrParser.getLddVersion(), attrParser.getLddDate());

      return ccb.getFirstFieldId();
    } finally {
      writer.close();
    }
  }


  private boolean overwriteLdd(Instant lastDate, String strLddDate) {
    try {
      Instant lddDate = LddUtils.lddDateToIsoInstant(strLddDate);
      return lddDate.isAfter(lastDate);
    } catch (Exception ex) {
      log.warn("Could not parse LDD date " + strLddDate);
      return false;
    }
  }

}

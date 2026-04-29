package gov.nasa.pds.registry.common.es.service;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.registry.common.dd.LddException;
import gov.nasa.pds.registry.common.dd.LddUtils;
import gov.nasa.pds.registry.common.util.ExceptionUtils;
import gov.nasa.pds.registry.common.util.file.FileDownloader;

import gov.nasa.pds.registry.common.util.Tuple;

import gov.nasa.pds.registry.common.es.dao.dd.DataDictionaryDao;
import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.es.dao.schema.SchemaDao;
import gov.nasa.pds.registry.common.es.dao.dd.LddVersions;


/**
 * Update Elasticsearch schema and LDDs
 * 
 * @author karpenko
 */
public class SchemaUpdater {
  private Logger log;
  private FileDownloader fileDownloader;
  private JsonLddLoader lddLoader;

  private DataDictionaryDao ddDao;
  private SchemaDao schemaDao;

  final private String index;

  /**
   * Constructor
   * 
   * @param conFact instance of class ConnectionFactory
   * @param ddDao instance of DataDictionaryDao
   * @param schemaDao instance of SchemaDao
   * 
   * @throws Exception
   */
  public SchemaUpdater(ConnectionFactory conFact, DataDictionaryDao ddDao, SchemaDao schemaDao)
      throws Exception {
    log = LogManager.getLogger(this.getClass());

    this.ddDao = ddDao;
    this.schemaDao = schemaDao;

    fileDownloader = new FileDownloader(true);

    lddLoader = new JsonLddLoader(ddDao, conFact);
    lddLoader.loadPds2EsDataTypeMap(LddUtils.getPds2EsDataTypeCfgFile("HARVEST_HOME"));
    this.index = conFact.getIndexName();
  }


  /**
   * Update Elasticsearch schema
   * 
   * @param fields fields to add
   * @param xsds XSDs of fields to add
   * @throws Exception an exception
   */
  public void updateSchema(Set<String> fields, Map<String, String> xsds) throws Exception {
    // Update LDDs
    if (xsds != null && !xsds.isEmpty()) {
      log.info("Updating LDDs.");

      for (Map.Entry<String, String> xsd : xsds.entrySet()) {
        String uri = xsd.getKey();
        String prefix = xsd.getValue();

        try {
          updateLdd(uri, prefix);
        } catch (Exception ex) {
          log.error("Could not update LDD for namespace '" + prefix + "' at URI " + uri
              + ": " + ex.getMessage() + ". Harvesting will continue with available field definitions.");
        }
      }
    }

    // Update schema
    if (fields != null && !fields.isEmpty()) {
      List<Tuple> newFields = ddDao.getDataTypes(fields);
      if (newFields != null) {
        schemaDao.updateSchema(newFields);
        log.debug("Updated " + newFields.size() + " fields in OpenSearch mapping for index "
            + this.index);
      }
    }
  }


  private void updateLdd(String uri, String prefix) throws LddException {
    if (uri == null || uri.isEmpty())
      return;
    if (prefix == null || prefix.isEmpty())
      return;

    log.info("Updating '" + prefix + "' LDD. Schema location: " + uri);

    // Get JSON schema URL from XSD URL
    String jsonUrl = getJsonUrl(uri);

    // Get schema file name
    int idx = jsonUrl.lastIndexOf('/');
    if (idx < 0) {
      throw new IllegalArgumentException("Invalid schema URI: " + uri);
    }
    String schemaFileName = jsonUrl.substring(idx + 1);

    // Get stored LDDs info
    LddVersions lddInfo;
    try {
      lddInfo = ddDao.getLddInfo(prefix);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (IOException ex) {
      throw new LddException("Failed to query registry for existing LDD info for namespace '"
          + prefix + "': " + ExceptionUtils.getMessage(ex), ex);
    }

    // LDD already loaded
    if (lddInfo.files.contains(schemaFileName)) {
      return;
    }

    // Download LDD
    File lddFile;
    try {
      lddFile = File.createTempFile("LDD-", ".JSON");
      // Restrict permissions to owner only (mitigate publicly writable temp dir risk)
      lddFile.setReadable(false, false);
      lddFile.setReadable(true, true);
      lddFile.setWritable(false, false);
      lddFile.setWritable(true, true);
    } catch (IOException ex) {
      throw new LddException("Failed to create temp file for LDD download for namespace '"
          + prefix + "': " + ExceptionUtils.getMessage(ex), ex);
    }

    try {
      if (fileDownloader.download(jsonUrl, lddFile)) {
        lddLoader.load(lddFile, schemaFileName, prefix);
      }
    } catch (RuntimeException ex) {
      throw ex;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while downloading or loading LDD for namespace '" + prefix + "' from " + jsonUrl);
      if (lddInfo.isEmpty()) {
        log.warn("No previously loaded LDD found for namespace '" + prefix
            + "'. Fields from this namespace will use 'keyword' data type.");
      } else {
        log.warn("Will use previously loaded field definitions for namespace '" + prefix
            + "' from " + lddInfo.files);
      }
      return;
    } catch (Exception ex) {
      log.error("Failed to download or load LDD for namespace '" + prefix + "' from " + jsonUrl
          + ": " + ExceptionUtils.getMessage(ex));
      if (lddInfo.isEmpty()) {
        log.warn("No previously loaded LDD found for namespace '" + prefix
            + "'. Fields from this namespace will use 'keyword' data type.");
      } else {
        log.warn("Will use previously loaded field definitions for namespace '" + prefix
            + "' from " + lddInfo.files);
      }
    } finally {
      lddFile.delete();
    }
  }


  private String getJsonUrl(String uri) {
    if (uri.endsWith(".xsd")) {
      return uri.substring(0, uri.length() - 3) + "JSON";
    } else {
      throw new IllegalArgumentException("Invalid schema URI - does not end with '.xsd': " + uri);
    }
  }

}

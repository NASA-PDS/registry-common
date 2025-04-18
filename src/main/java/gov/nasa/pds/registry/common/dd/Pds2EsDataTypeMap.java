package gov.nasa.pds.registry.common.dd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.nasa.pds.registry.common.es.dao.dd.DataTypeNotFoundException;
import gov.nasa.pds.registry.common.util.CloseUtils;


/**
 * Mappings between PDS LDD data types such as 'ASCII_LID' 
 * and Elasticsearch data types such as 'keyword'.
 * 
 * <p>Mappings are loaded from a configuration file similar to Java properties file.
 * There is one mapping per line:
 * <p>&lt;PDS LDD data type&gt;=&lt;Elasticsearch data type&gt;
 * 
 * <p>Default configuration file is in 
 * &lt;PROJECT_ROOT&gt;/src/main/resources/elastic/data-dic-types.cfg
 * 
 * @author karpenko
 */
public class Pds2EsDataTypeMap
{
    private Logger log;
    private Map<String, String> map;
    
    /**
     * Constructor
     */
    public Pds2EsDataTypeMap()
    {
        log = LogManager.getLogger(this.getClass());
        map = new HashMap<>();
    }

    
    /**
     * Get Elasticsearch data type for a PDS LDD data type
     * @param pdsType PDS LDD data type
     * @return Elasticsearch data type
     */
    public String getEsDataType(String pdsType) throws DataTypeNotFoundException
    {
        String esType = map.get(pdsType);
        if(esType != null) return esType;
        
        esType = guessEsDataType(pdsType);
        log.warn("No PDS to Elasticsearch data type mapping for '" + pdsType 
                + "'. Will use '" + esType + "'");

        map.put(pdsType, esType);
        return esType;
    }
    
    
    /**
     * Try to determine Elasticsearch data type from a PDS data type
     * @param pdsType PDS data type, e.g., "UTF8_Text_Preserved"
     * @return Elasticsearch data type, e.g., "text".
     */
    private String guessEsDataType(String pdsType) throws DataTypeNotFoundException
    {
        pdsType = pdsType.toLowerCase();
        if(pdsType.contains("_real")) return "double";
        if(pdsType.contains("_integer")) return "integer";
        if(pdsType.contains("_string")) return "keyword";
        if(pdsType.contains("_text")) return "text";
        if(pdsType.contains("_date")) return "date";
        if(pdsType.contains("_boolean")) return "boolean";        
        
        throw new DataTypeNotFoundException("Could not guess type and not more defaulting to keyword (harvest#204)");
    }
    
    
    /**
     * Load data type mappings from a configuration file
     * @param file Configuration file with PDS LDD to Elasticsearch data type mappings.
     * <p>Mappings are loaded from a configuration file similar to Java properties file.
     * There is one mapping per line:
     * <p>&lt;PDS LDD data type&gt;=&lt;Elasticsearch data type&gt;</p>
     * @throws Exception an exception
     */
    public void load(File file) throws Exception
    {
        if(file == null) return;
        
        log.info("Loading PDS to ES data type mapping from " + file.getAbsolutePath());
        
        BufferedReader rd = null;
        
        try
        {
            rd = new BufferedReader(new FileReader(file));
        }
        catch(Exception ex)
        {
          if (rd != null) rd.close();
          throw new Exception("Could not open data type configuration file '" + file.getAbsolutePath());
        }
        
        try
        {
            String line;
            while((line = rd.readLine()) != null)
            {
                line = line.trim();
                if(line.startsWith("#") || line.isEmpty()) continue;
                String[] tokens = line.split("=");
                if(tokens.length != 2) 
                {
                    throw new Exception("Invalid entry in data type configuration file " 
                            + file.getAbsolutePath() + ": " + line);
                }
                
                String key = tokens[0].trim();
                if(key.isEmpty()) 
                {
                    throw new Exception("Empty key in data type configuration file " 
                            + file.getAbsolutePath() + ": " + line);
                }
                
                String value = tokens[1].trim();
                if(key.isEmpty())
                {
                    throw new Exception("Empty value in data type configuration file " 
                            + file.getAbsolutePath() + ": " + line);
                }
                
                map.put(key, value);
            }
        }
        finally
        {
            CloseUtils.close(rd);
        }
    }
    
    
    /**
     * Print all mappings
     */
    public void debug()
    {
        map.forEach((key, val) -> { System.out.println(key + "  -->  " + val); } );
    }
}

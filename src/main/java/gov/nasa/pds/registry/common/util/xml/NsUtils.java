package gov.nasa.pds.registry.common.util.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Utility methods to extract namespace information from XMLs.
 * @author karpenko
 */
public class NsUtils
{
    /**
     * Extract URI to prefix and prefix to location mappings.
     * @param doc XML DOM model
     * @return mappings
     * @throws Exception an exception
     */
    public static XmlNamespaces getNamespaces(Document doc) throws Exception
    {
        Element root = doc.getDocumentElement();
        NamedNodeMap attrs = root.getAttributes();

        XmlNamespaces resp = new XmlNamespaces();
        resp.uri2prefix = new HashMap<>();
        resp.uri2prefix.put("http://pds.nasa.gov/pds4/pds/v1", "pds");
        
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node attr = attrs.item(i);
            String name = attr.getNodeName();
            if(name.startsWith("xmlns:"))
            {
                String prefix = name.substring(6);
                String uri = attr.getNodeValue();
                if(!uri.startsWith("http://www.w3.org/"))
                {
                    resp.uri2prefix.put(uri, prefix);
                }
            }
            else if(name.endsWith(":schemaLocation"))
            {
                resp.prefix2location = createPrefixToLocationMap(attr.getNodeValue(), resp.uri2prefix);
            }
        }

        return resp;
    }

    
    /**
     * Parse "xsi:schemaLocation" attribute and create prefix to location map.
     * @param str Value of "xsi:schemaLocation" attribute. 
     * (List of space delimited URI-location tuples)
     * @param uriToPrefixMap This map is used to map URIs from "xsi:schemaLocation" 
     * attribute to prefixes (such as "pds", "disp", "cassini", etc.) 
     * @return Prefix to location map
     * @throws Exception an exception
     */
    private static Map<String, String> createPrefixToLocationMap(String str, 
            Map<String, String> uriToPrefixMap) throws Exception
    {
        Map<String, String> prefixToLocationMap = new HashMap<>();
        if(str == null) return prefixToLocationMap;
        
        StringTokenizer tkz = new StringTokenizer(str);        

        while(tkz.hasMoreTokens())
        {
            String uri = tkz.nextToken();
            if(tkz.hasMoreTokens())
            {
                String location = tkz.nextToken();
                String prefix = uriToPrefixMap.get(uri);
                if(prefix == null)
                {
                    Logger log = LogManager.getLogger(NsUtils.class);
                    log.warn("Could not find 'xmlns:XXX' definition for a schema listed in 'xsi:schemaLocation': " + uri);
                }
                
                prefixToLocationMap.put(prefix, location);
            }
            else
            {
                Logger log = LogManager.getLogger(NsUtils.class);
                log.warn("Missing location for URI " + uri);
            }
        }
        
        return prefixToLocationMap;
    }
}

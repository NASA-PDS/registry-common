package gov.nasa.pds.registry.common.connection.es;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class GetAltIdsParser implements SearchResponseParser.Callback
{
    private Map<String, Set<String>> map;
    
    
    GetAltIdsParser()
    {
        map = new TreeMap<>();
    }

    
    public Map<String, Set<String>> getIdMap()
    {
        return map;
    }
    
    
    @Override
    public void onRecord(String id, Object rec)
    {
        if(rec instanceof Map<?, ?>)
        {
            Object obj = ((Map<?, ?>)rec).get("alternate_ids");
            if(obj == null) return;
            
            // Multiple values
            if(obj instanceof List<?>)
            {
                Set<String> altIds = new TreeSet<>();
                for(Object item: (List<?>)obj)
                {
                    altIds.add(item.toString());
                }
                
                map.put(id, altIds);
            }
            // Single value
            else if(obj instanceof String)
            {
                Set<String> altIds = new TreeSet<>();
                altIds.add((String)obj);
                map.put(id, altIds);
            }
        }
    }

}

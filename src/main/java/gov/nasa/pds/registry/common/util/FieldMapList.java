package gov.nasa.pds.registry.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Implementation of FieldMap interface which stores values in a list.
 * It preserves order and number of values in PDS label XML.
 * 
 * @author karpenko
 */
public class FieldMapList implements FieldMap
{
    private Map<String, List<String>> fields;
    
    
    /**
     * Constructor
     */
    public FieldMapList()
    {
        fields = new TreeMap<>();
    }
    
    
    /**
     * Map size / number of fields.
     */
    public int size()
    {
        return fields.size();
    }
    
    
    /**
     * Check if map is empty.
     */
    public boolean isEmpty()
    {
        return fields.size() == 0;
    }
    
    
    /**
     * Remove all the fields from the map
     */
    public void clear()
    {
        fields.clear();
    }

    
    private List<String> getOrCreateValues(String fieldName)
    {
        if(fieldName == null) throw new IllegalArgumentException("Field name is null");

        List<String> values = fields.get(fieldName);
        if(values == null) 
        {
            values = new ArrayList<>();
            fields.put(fieldName, values);
        }

        return values;
    }
    
    
    /**
     * Set field's value
     * @param fieldName field name
     * @param value field value
     */
    public void setValue(String fieldName, String value)
    {
        if(fieldName == null) throw new IllegalArgumentException("Field name is null");
        if(value == null) return;
        
        fields.put(fieldName, Arrays.asList(value));
    }

    
    /**
     * Set field's value
     * @param fieldName field name
     * @param values field values
     */
    public void setValues(String fieldName, List<String> values)
    {
        if(fieldName == null) throw new IllegalArgumentException("Field name is null");
        if(values == null) return;
        
        fields.put(fieldName, values);
    }

    
    /**
     * Add field's value
     */
    public void addValue(String fieldName, String value)
    {
        if(value == null) return;
        
        List<String> values = getOrCreateValues(fieldName);        
        values.add(value);
    }
    

    /**
     * Add multiple values
     */
    public void addValues(String fieldName, String[] values)
    {
        if(values == null || values.length == 0) return;
        
        List<String> set = getOrCreateValues(fieldName);        
        Collections.addAll(set, values);
    }


    /**
     * Get first value of a field.
     */
    public String getFirstValue(String fieldName)
    {
        Collection<String> values = getValues(fieldName);
        return (values == null || values.isEmpty()) ? null : values.iterator().next();
    }


    /**
     * Get all values of a field.
     */
    public Collection<String> getValues(String fieldName)
    {
        return fields.get(fieldName);
    }

    
    /**
     * Get names of all fields in this map.
     */
    public Set<String> getNames()
    {
        return fields.keySet();
    }
    
}

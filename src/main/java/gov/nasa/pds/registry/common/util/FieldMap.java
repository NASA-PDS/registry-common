package gov.nasa.pds.registry.common.util;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for a map of multi-valued fields (metadata) extracted from PDS labels. 
 *  
 * @author karpenko
 */
public interface FieldMap<T>
{
    public void addValue(String fieldName, T value);
    public void addValues(String fieldName, T[] values);

    public Collection<T> getValues(String fieldName);
    public T getFirstValue(String fieldName);
    public Set<String> getNames();
    
    public boolean isEmpty();
    public int size();
    public void clear();
}

package gov.nasa.pds.registry.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FieldMapListOfListOfObjects implements FieldMap<List<Map<String, String>>> {
  private Map<String, List<List<Map<String,String>>>> arrays = new TreeMap<String, List<List<Map<String,String>>>>();

  private List<List<Map<String,String>>> fetch (String fieldName) {
    if (!this.arrays.containsKey(fieldName)) this.arrays.put(fieldName, new ArrayList<List<Map<String,String>>>());
    return this.arrays.get(fieldName);
  }

  @Override
  public void addValue(String fieldName, List<Map<String,String>> value) {
    if (value != null) this.fetch(fieldName).add(value);
  }

  @Override
  public void addValues(String fieldName, List<Map<String,String>>[] values) {
    if (values != null) this.fetch(fieldName).addAll(Arrays.asList(values));
  }

  @Override
  public Collection<List<Map<String,String>>> getValues(String fieldName) {
    return this.fetch(fieldName);
  }

  @Override
  public List<Map<String,String>> getFirstValue(String fieldName) {
    return this.fetch(fieldName).get(0);
  }

  @Override
  public Set<String> getNames() {
    return this.arrays.keySet();
  }

  @Override
  public boolean isEmpty() {
    return this.arrays.isEmpty();
  }

  @Override
  public int size() {
    return this.arrays.size();
  }

  @Override
  public void clear() {
    this.arrays.clear();
  }
}
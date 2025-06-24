package gov.nasa.pds.registry.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompressionPattern {
  final private List<String> extensions;
  final private Pattern regex;
  final private String algorithm;
  public CompressionPattern(String regex, String algorithm, List<String> extensions) {
    this.algorithm = algorithm;
    this.regex = Pattern.compile(regex);
    this.extensions = new ArrayList<String>(extensions);
  }
  public String algorithm( ) { return this.algorithm; }
  public List<String> extensions() { return this.extensions; }
  public Matcher matcher(String filename) { return this.regex.matcher(filename); }
}

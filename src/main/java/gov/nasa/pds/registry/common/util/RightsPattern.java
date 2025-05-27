package gov.nasa.pds.registry.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RightsPattern {
  final private AccessRights right;
  final private Pattern regex;
  public RightsPattern(String regex, AccessRights right) {
    this.right = right;
    this.regex = Pattern.compile(regex);
  }
  public AccessRights accessRight() { return this.right; }
  public Matcher matcher(String filename) { return this.regex.matcher(filename); }
}

package gov.nasa.pds.registry.common.connection.aws;

import java.util.Arrays;
import java.util.List;
import org.opensearch.client.opensearch._types.mapping.BinaryProperty;
import org.opensearch.client.opensearch._types.mapping.BooleanProperty;
import org.opensearch.client.opensearch._types.mapping.DateProperty;
import org.opensearch.client.opensearch._types.mapping.DoubleNumberProperty;
import org.opensearch.client.opensearch._types.mapping.FloatNumberProperty;
import org.opensearch.client.opensearch._types.mapping.IntegerNumberProperty;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch._types.mapping.LongNumberProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;

final class HelperFunctions {
  static List<String> indices (String indexName) {
    return Arrays.asList(indexName.replace(",", ":").replace(";", ":").split(":"));
  }
  static Property.Builder setType (Property.Builder builder, String fieldType) {
    switch (fieldType) {
      case "binary":
        builder.binary(new BinaryProperty.Builder().build());
        break;
      case "boolean":
        builder.boolean_(new BooleanProperty.Builder().build());
        break;
      case "date":
        builder.date(new DateProperty.Builder().build());
        break;
      case "double":
        builder.double_(new DoubleNumberProperty.Builder().build());
        break;
      case "float":
        builder.float_(new FloatNumberProperty.Builder().build());
        break;
      case "integer":
        builder.integer(new IntegerNumberProperty.Builder().build());
        break;
      case "keyword":
        builder.keyword(new KeywordProperty.Builder().build());
        break;
      case "long":
        builder.long_(new LongNumberProperty.Builder().build());
        break;
      case "text":
        builder.text(new TextProperty.Builder().build());
        break;
      default:
        throw new RuntimeException("Cannot map type '" + fieldType + "' yet. Please review PropertyHelper.setType() code and fix.");
    }
    return builder;
  }
  public static void main(String args[]) {
    // since this class is package level, test code has to go here so do not forget to enable assertions when testing
    List<String> values;
    values = indices("apple");
    assert 1 == values.size() : "array is wrong size for 'apple'";
    assert ("apple").equals(values.get(0)) : "value of array is not 'apple'";
    values = indices("apple,cherry");
    assert 2 == values.size() : "array is wrong size for 'apple,cherry'";
    assert ("apple").equals(values.get(0)) : "value of array is not 'apple,cherry'";
    assert ("cherry").equals(values.get(1)) : "value of array is not 'apple,cherry'";    
    values = indices("apple;cherry");
    assert 2 == values.size() : "array is wrong size for 'apple;cherry'";
    assert ("apple").equals(values.get(0)) : "value of array is not 'apple;cherry'";
    assert ("cherry").equals(values.get(1)) : "value of array is not 'apple;cherry'";    
    values = indices("apple:cherry");
    assert 2 == values.size() : "array is wrong size for 'apple:cherry'";
    assert ("apple").equals(values.get(0)) : "value of array is not 'apple;cherry'";
    assert ("cherry").equals(values.get(1)) : "value of array is not 'apple;cherry'";
    values = indices("apple,cherry;kiwi:plum");
    assert 4 == values.size() : "array is wrong size for 'apple,cherry;kiwi:plum'";
    assert ("apple").equals(values.get(0)) : "value of array is not 'apple,cherry;kiwi:plum'";
    assert ("cherry").equals(values.get(1)) : "value of array is not 'apple,cherry;kiwi:plum'";
    assert ("kiwi").equals(values.get(2)) : "value of array is not 'apple,cherry;kiwi:plum'";
    assert ("plum").equals(values.get(3)) : "value of array is not 'apple,cherry;kiwi:plum'";
  }
}

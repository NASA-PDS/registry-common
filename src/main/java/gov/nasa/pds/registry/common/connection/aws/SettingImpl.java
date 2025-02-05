package gov.nasa.pds.registry.common.connection.aws;

import org.opensearch.client.opensearch.indices.GetIndicesSettingsRequest;
import gov.nasa.pds.registry.common.Request.Setting;

class SettingImpl implements Setting {
  final GetIndicesSettingsRequest.Builder craftsman = new GetIndicesSettingsRequest.Builder();
  @Override
  public Setting setIndex(String name) {
    this.craftsman.index(HelperFunctions.indices(name));
    return this;
  }
}

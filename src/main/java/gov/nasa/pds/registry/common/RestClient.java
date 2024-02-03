package gov.nasa.pds.registry.common;

import java.io.Closeable;
import java.io.IOException;

public interface RestClient extends Closeable {
  public Request createRequest(Request.Method method, String endpoint);
  public Response performRequest(Request request) throws IOException;
}
package dao;

import java.io.StringReader;
import gov.nasa.pds.registry.common.connection.es.BulkResponseParser;
import tt.TestLogConfigurator;

public class TestBulkResponseParser
{
    private static final String TWO_ERRORS_JSON 
        = "{\"took\":3,\"errors\":true,\"items\":["
        + "{\"update\":{\"_index\":\"registry\",\"_type\":\"_doc\","
        + "\"_id\":\"urn:nasa:pds:kaguya_grs_spectra:document:kgrs_calibrated_spectra::1.0\","
        + "\"status\":404,\"error\":{\"type\":\"document_missing_exception\","
        + "\"reason\":\"[_doc][urn:nasa:pds:kaguya_grs_spectra:document:kgrs_calibrated_spectra::1.0]: document missing\","
        + "\"index_uuid\":\"i-NO93-HTu-dw5tP9i_MqQ\",\"shard\":\"0\",\"index\":\"registry\"}}},"
        + "{\"update\":{\"_index\":\"registry\",\"_type\":\"_doc\","
        + "\"_id\":\"urn:nasa:pds:kaguya_grs_spectra:document:kgrs_ephemerides_doc::1.0\","
        + "\"status\":404,\"error\":{\"type\":\"document_missing_exception\","
        + "\"reason\":\"[_doc][urn:nasa:pds:kaguya_grs_spectra:document:kgrs_ephemerides_doc::1.0]: document missing\","
        + "\"index_uuid\":\"i-NO93-HTu-dw5tP9i_MqQ\",\"shard\":\"0\",\"index\":\"registry\"}}}]}";


    public static void main(String[] args) throws Exception
    {
        TestLogConfigurator.configureLogger();
        
        StringReader rd = new StringReader(TWO_ERRORS_JSON);
        
        BulkResponseParser parser = new BulkResponseParser();
        parser.parse(rd);
    }
}

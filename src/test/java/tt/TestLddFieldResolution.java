package tt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import gov.nasa.pds.registry.common.dd.LddEsJsonWriter;
import gov.nasa.pds.registry.common.dd.Pds2EsDataTypeMap;
import gov.nasa.pds.registry.common.dd.parser.AttributeDictionaryParser;
import gov.nasa.pds.registry.common.dd.parser.ClassAttrAssociationParser;
import gov.nasa.pds.registry.common.dd.parser.DDAttribute;
import gov.nasa.pds.registry.common.util.file.FileDownloader;


/**
 * Integration test: downloads a live LDD JSON from pds.nasa.gov, parses it, maps PDS types to
 * ES types, and asserts that specific fields are resolved with the expected ES data types.
 *
 * Each test case supplies its own XSD URL and expected field→type map, so adding coverage for a
 * new namespace is just a new entry in testCases().
 *
 * Skipped automatically when pds.nasa.gov is unreachable (e.g. offline CI runners).
 */
public class TestLddFieldResolution {

    // ---------------------------------------------------------------------------
    // Test cases
    // ---------------------------------------------------------------------------

    record LddTestCase(String label, String xsdUrl, Map<String, String> expectedFields) {
        @Override public String toString() { return label; }
    }

    static Stream<LddTestCase> testCases() {
        return Stream.of(
            new LddTestCase(
                "MRO 1M00_1400",
                "https://pds.nasa.gov/pds4/mission/mro/v1/PDS4_MRO_1M00_1400.xsd",
                Map.of(
                    "mro:CRISM_Temperatures/mro:fpe_temperature",           "double",
                    "mro:CRISM_Parameters/mro:sensor_id",                   "keyword",
                    "mro:CRISM_Band/mro:scaling_factor",                    "double",
                    "mro:MRO_Parameters/mro:orbit_number",                  "long",
                    "mro:MRO_Parameters/mro:spacecraft_clock_start_count",  "keyword",
                    "mro:CRISM_Band/mro:band_sequence_number",              "integer",
                    "mro:CRISM_Parameters/mro:observation_id",              "keyword",
                    "mro:CRISM_Band/mro:value_offset",                      "double",
                    "mro:CRISM_Parameters/mro:observation_number",          "keyword",
                    "mro:MRO_Parameters/mro:product_type",                  "keyword"
                )
            )
            // Add more LDD test cases here as new namespaces are validated, e.g.:
            // new LddTestCase("CART ...", "https://pds.nasa.gov/pds4/.../PDS4_CART_....xsd", Map.of(...))
        );
    }

    // ---------------------------------------------------------------------------
    // Test body
    // ---------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("testCases")
    void lddFieldsResolveToExpectedEsTypes(LddTestCase tc) throws Exception {
        assumeTrue(isReachable("https://pds.nasa.gov"), "pds.nasa.gov is not reachable — skipping network test");

        String jsonUrl = xsdToJsonUrl(tc.xsdUrl());
        String namespace = namespaceFromUrl(jsonUrl);

        // Download LDD JSON to a temp file
        File lddFile = downloadLdd(jsonUrl);
        try {
            Map<String, String> resolved = resolveFields(lddFile, namespace);

            for (Map.Entry<String, String> expected : tc.expectedFields().entrySet()) {
                String field = expected.getKey();
                String esType = resolved.get(field);
                assertNotNull(esType,
                    "Field '" + field + "' was not resolved (not found in LDD output for " + tc.xsdUrl() + ")");
                assertEquals(expected.getValue(), esType,
                    "Wrong ES type for field '" + field + "'");
            }
        } finally {
            lddFile.delete();
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private static String xsdToJsonUrl(String xsdUrl) {
        if (!xsdUrl.endsWith(".xsd")) throw new IllegalArgumentException("URL must end with .xsd: " + xsdUrl);
        return xsdUrl.substring(0, xsdUrl.length() - 3) + "JSON";
    }

    private static String namespaceFromUrl(String jsonUrl) {
        // e.g. "...PDS4_MRO_1M00_1400.JSON" → filename → split on _ → second token is the namespace
        String filename = jsonUrl.substring(jsonUrl.lastIndexOf('/') + 1);
        String[] parts = filename.split("_");
        // PDS4_<NS>_<imver>_<lddver>.JSON  →  parts[1]
        if (parts.length < 2) throw new IllegalArgumentException("Cannot derive namespace from: " + jsonUrl);
        return parts[1].toLowerCase();
    }

    private static File downloadLdd(String jsonUrl) throws Exception {
        Path tmp = Files.createTempFile("LDD-test-", ".JSON");
        File lddFile = tmp.toFile();
        FileDownloader downloader = new FileDownloader(true);
        boolean downloaded = downloader.download(jsonUrl, lddFile);
        if (!downloaded || lddFile.length() == 0) {
            lddFile.delete();
            throw new IllegalStateException("LDD download produced empty file: " + jsonUrl);
        }
        return lddFile;
    }

    /**
     * Runs the full LDD parse + PDS→ES type mapping pipeline and returns a map of
     * "classNs:ClassName/attrNs:attrName" → ES data type for every field in the LDD.
     * Does not touch OpenSearch.
     */
    private static Map<String, String> resolveFields(File lddFile, String namespace) throws Exception {
        // Load the PDS→ES type map from the bundled config (same file Harvest ships)
        Pds2EsDataTypeMap dtMap = new Pds2EsDataTypeMap();
        URL cfgUrl = TestLddFieldResolution.class.getClassLoader().getResource("elastic/data-dic-types.cfg");
        assertNotNull(cfgUrl, "data-dic-types.cfg not found on test classpath");
        dtMap.load(new File(cfgUrl.toURI()));

        // Parse attributeDictionary → id → DDAttribute cache
        Map<String, DDAttribute> attrCache = new TreeMap<>();
        AttributeDictionaryParser attrParser = new AttributeDictionaryParser(lddFile, attr -> attrCache.put(attr.id, attr));
        attrParser.parse();

        // Write to a temp NDJSON file via LddEsJsonWriter (same path as production code)
        File outFile = Files.createTempFile("ldd-es-", ".json").toFile();
        try {
            LddEsJsonWriter writer = new LddEsJsonWriter(outFile, dtMap, attrCache, true);
            writer.setNamespaceFilter(namespace);
            ClassAttrAssociationParser caaParser = new ClassAttrAssociationParser(lddFile,
                (classNs, className, attrId) -> writer.writeFieldDefinition(classNs, className, attrId));
            caaParser.parse();
            writer.close();

            return parseNdjsonFieldTypes(outFile);
        } finally {
            outFile.delete();
        }
    }

    /**
     * Reads the NDJSON bulk file produced by LddEsJsonWriter and returns a map of
     * es_field_name → es_data_type for every data record (skips the action lines).
     */
    private static Map<String, String> parseNdjsonFieldTypes(File ndjson) throws Exception {
        Map<String, String> result = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ndjson))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("{\"create\"") || line.startsWith("{\"index\"")) continue;

                // Data line: {"es_field_name":"...","es_data_type":"...",...}
                JsonReader jr = new JsonReader(new java.io.StringReader(line));
                jr.beginObject();
                String fieldName = null, esType = null;
                while (jr.hasNext() && jr.peek() != JsonToken.END_OBJECT) {
                    String key = jr.nextName();
                    if ("es_field_name".equals(key))      fieldName = jr.nextString();
                    else if ("es_data_type".equals(key))  esType    = jr.nextString();
                    else                                   jr.skipValue();
                }
                jr.endObject();
                jr.close();

                if (fieldName != null && esType != null) result.put(fieldName, esType);
            }
        }
        return result;
    }

    private static boolean isReachable(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            return code >= 200 && code < 400;
        } catch (Exception e) {
            return false;
        }
    }
}

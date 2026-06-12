package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import gov.nasa.pds.registry.common.dd.parser.ClassAttrAssociationParser;


/**
 * Unit tests for ClassAttrAssociationParser against trimmed LDD JSON fixtures.
 *
 * Covers two structural variants found across IM versions:
 *   - IM <= 1.24: association objects carry "identifier" (string) only; no "attributeId" key.
 *   - IM >= 1.25: association objects carry both "attributeId" (array of strings) and "identifier".
 *
 * Fixture files in src/test/resources/ldd/ are trimmed slices of real LDD JSON files — only the
 * classDictionary and attributeDictionary entries needed to exercise each variant are included.
 * They are checked in so the test has no network dependency.
 */
public class TestClassAttrAssociationParser {

    record Assoc(String classNs, String className, String attrId) {}

    // Both IM variants (1.22 and 1.25) should produce identical associations from the same classes.
    static final List<Assoc> CTLI_EXPECTED = List.of(
        new Assoc("ctli", "Type_List",  "0001_NASA_PDS_1.ctli.Type_List.ctli.type"),
        new Assoc("ctli", "Type_List",  "0001_NASA_PDS_1.ctli.Type_List.ctli.subtype"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.ASCII_AnyURI.pds.minimum_characters"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.formation_rule"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.ASCII_AnyURI.pds.maximum_characters"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.ASCII_AnyURI.pds.character_constraint"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.ASCII_AnyURI.pds.character_encoding"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.minimum_value"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.ASCII_AnyURI.pds.xml_schema_base_type"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.maximum_value"),
        new Assoc("pds",  "ASCII_AnyURI", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.pattern"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.minimum_characters"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.maximum_characters"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.minimum_value"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.maximum_value"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.Character_Data_Type.pds.character_encoding"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.ASCII_BibCode.pds.character_constraint"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.ASCII_BibCode.pds.formation_rule"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.ASCII_BibCode.pds.pattern"),
        new Assoc("pds",  "ASCII_BibCode", "0001_NASA_PDS_1.pds.ASCII_BibCode.pds.xml_schema_base_type")
    );

    record ParserTestCase(String label, String fixturePath, List<Assoc> expected) {
        @Override public String toString() { return label; }
    }

    static Stream<ParserTestCase> testCases() {
        return Stream.of(
            new ParserTestCase(
                "IM 1.22 (identifier only, no attributeId)",
                "ldd/ctli_1M00_im1220.JSON",
                CTLI_EXPECTED
            ),
            new ParserTestCase(
                "IM 1.25 (attributeId array + identifier)",
                "ldd/ctli_1P00_im1250.JSON",
                CTLI_EXPECTED
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testCases")
    void parsesAllAttributeAssociations(ParserTestCase tc) throws Exception {
        URL resource = getClass().getClassLoader().getResource(tc.fixturePath());
        assertTrue(resource != null, "Fixture not found on classpath: " + tc.fixturePath());
        File fixture = new File(resource.toURI());

        List<Assoc> actual = new ArrayList<>();
        ClassAttrAssociationParser parser = new ClassAttrAssociationParser(fixture,
            (classNs, className, attrId) -> actual.add(new Assoc(classNs, className, attrId)));
        parser.parse();

        assertEquals(tc.expected().size(), actual.size(),
            "Expected " + tc.expected().size() + " associations but got " + actual.size()
            + "\nActual: " + actual);

        for (int i = 0; i < tc.expected().size(); i++) {
            Assoc exp = tc.expected().get(i);
            Assoc act = actual.get(i);
            assertEquals(exp.classNs(), act.classNs(),   "association[" + i + "] classNs mismatch");
            assertEquals(exp.className(), act.className(), "association[" + i + "] className mismatch");
            assertEquals(exp.attrId(), act.attrId(),     "association[" + i + "] attrId mismatch");
        }
    }
}

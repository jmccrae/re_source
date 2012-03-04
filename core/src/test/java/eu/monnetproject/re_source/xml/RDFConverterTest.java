/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.re_source.xml;

import eu.monnetproject.re_source.rdf.URIRef;
import eu.monnetproject.re_source.rdf.turtle.TurtleWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.xml.sax.InputSource;

/**
 *
 * @author jmccrae
 */
public class RDFConverterTest {
    
    public RDFConverterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    private final String SAMPLE_DOC1 = "<?xml version=\"1.0\"?>\n"
            + "<Lexicon>\n"
            + "\t<Entries>\n"
            + "\t\t<Entry value=\"cat\"/>\n"
            + "\t\t<Entry value=\"dog\"/>\n"
            + "\t</Entries>\n"
            + "</Lexicon>";

    /**
     * Test of toRDF method, of class RDFConverter.
     */
    @Test
    public void testToRDF() throws Exception {
        System.out.println("toRDF");
        final InputSource inputSource = new InputSource(new StringReader(SAMPLE_DOC1));
        final URI uri = URI.create("http://example.com/servlet/doc1");
        RDFConverterImpl instance = new RDFConverterImpl(inputSource, uri, "http://example.com/servlet");
        URIRef result = instance.toRDF();
        assertFalse(result.getTriples().isEmpty());
        new TurtleWriter().write(result, new OutputStreamWriter(System.out));
    }
}

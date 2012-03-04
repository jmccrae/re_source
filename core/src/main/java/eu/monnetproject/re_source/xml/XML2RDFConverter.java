/**
 * ********************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.re_source.xml;

import eu.monnetproject.re_source.SourceParseException;
import eu.monnetproject.re_source.rdf.RDFFactory;
import static eu.monnetproject.re_source.rdf.RDFPrefixes.XSD;
import eu.monnetproject.re_source.rdf.Resource;
import eu.monnetproject.re_source.rdf.URIRef;
import eu.monnetproject.re_source.rdf.Value;
import eu.monnetproject.re_source.servlet.Re_SourceServlet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A converter from XML to RDF
 *
 * @author John McCrae
 */
public class XML2RDFConverter extends DefaultHandler {

    private final SAXParser saxParser;
    private final InputSource source;
    private final URIRef headResource;
    private final String servletPrefix;
    private final RDFFactory rdfFactory = new RDFFactory();
    // Stateful variables
    private Deque<Resource> resources = new LinkedList<Resource>();
    private Deque<Integer> indexes = new LinkedList<Integer>();
    private int nodeId = 1;
    private Set<String> usedIds = new HashSet<String>();
    private String language = null;

    /**
     * Create a new converter
     *
     * @param source The source XML document
     * @param uri The base URI of this document
     * @param servletPrefix The URI where this server is published
     * @throws ParserConfigurationException If the XML parse is not properly
     * configured
     * @throws SAXException If an error occurred in setting up the XML parser
     */
    public XML2RDFConverter(InputSource source, URI uri, String servletPrefix) throws ParserConfigurationException, SAXException {
        assert (uri.getFragment() == null);
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        this.saxParser = factory.newSAXParser();
        this.source = source;
        this.headResource = rdfFactory.newURIRef(uri);
        this.servletPrefix = servletPrefix;
    }

    private void addAttributes(Resource resource, Attributes attributes) {
        language = null;
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getQName(i).equals("xml:lang")) {
                language = attributes.getValue(i);
            } else if (attributes.getQName(i).equals("id")) {
                // ignore
            } else {
                resource.addTriple(rdfFactory.newURIRef(mkURI(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i))), mapLiteral(attributes.getValue(i)));
            }
        }
    }

    private URI mkURI(String uri, String localName, String qName) {
        if (uri == null || uri.equals("")) {
            return URI.create(servletPrefix + Re_SourceServlet.ontologyPath() + qName);
        } else {
            return URI.create(uri + localName);
        }
    }

    private Value mapLiteral(String literal) {
        if (literal.startsWith("http:") || literal.startsWith("https:") || literal.startsWith("ftp:")) {
            try {
                return rdfFactory.newURIRef(new URL(literal).toURI());
            } catch (MalformedURLException x) {
                return rdfFactory.newLiteral(literal);
            } catch (URISyntaxException x) {
                return rdfFactory.newLiteral(literal);
            }
        } else {
            return rdfFactory.newLiteral(literal);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String id = attributes.getValue("id") == null ? ("n" + nodeId++) : attributes.getValue("id");
        while(usedIds.contains(id)) {
            id = ("n" + nodeId++);
        }
        usedIds.add(id);
        final Resource resource = rdfFactory.newURIRef(URI.create(headResource.getURI().toString() + "#" + id));
        final URIRef property = rdfFactory.newURIRef(mkURI(uri, localName, qName));
        resources.peek().addTriple(property, resource);
        resources.push(resource);

        // We add an index property so that the data remains ordered as the original document
        resource.addTriple(rdfFactory.newURIRef(URI.create(Re_SourceServlet.contextPath() + "/property#index")), rdfFactory.newLiteral("" + indexes.peek(), URI.create(XSD + "integer")));
        indexes.push(indexes.pop() + 1);
        indexes.push(0);

        addAttributes(resource, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        resources.pop();
        indexes.pop();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        assert (!resources.isEmpty());
        final String string = new String(ch, start, length);
        if (string.matches("\\s*")) {
            return;
        }
        if (language != null) {
            resources.peek().addTriple(rdfFactory.newURIRef(URI.create(valueProperty())), rdfFactory.newLiteral(string, language));
        } else {
            resources.peek().addTriple(rdfFactory.newURIRef(URI.create(valueProperty())), rdfFactory.newLiteral(string));
        }
    }


    /**
     * Get the document as RDF
     *
     * @return The document as RDF
     * @throws SourceParseException If the source could not be parsed (for any
     * reason)
     */
    public URIRef toRDF() throws SourceParseException {
        try {
            resources.push(headResource);
            indexes.push(0);
            saxParser.parse(source, this);
            return headResource;
        } catch (IOException x) {
            throw new SourceParseException(x);
        } catch (SAXException x) {
            throw new SourceParseException(x);
        }
    }
    
    /**
     * The property that indicates a literal value
     * @return The property's URI as a String
     */
    public static String valueProperty() {
        return Re_SourceServlet.contextPath() + "/property#value";
    }
    
    /**
     * The property that indicates the index (order) of an annotation
     * @return The property's URI as a String
     */
    public static String indexProperty() {
        return Re_SourceServlet.contextPath() + "/property#index";
    }
}

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
package eu.monnetproject.re_source.rdf.xml;

import eu.monnetproject.re_source.rdf.BNode;
import eu.monnetproject.re_source.rdf.Literal;
import eu.monnetproject.re_source.rdf.PrefixTool;
import eu.monnetproject.re_source.rdf.RDFWriter;
import eu.monnetproject.re_source.rdf.Resource;
import eu.monnetproject.re_source.rdf.URIRef;
import eu.monnetproject.re_source.rdf.Value;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Writer for RDF/XML data
 *
 * @author John McCrae
 */
public class RDFXMLWriter implements RDFWriter {

    @Override
    public void write(Resource headResource, Writer out2) {
        final PrintWriter out = new PrintWriter(out2);
        final PrefixTool prefixTool = new PrefixTool();
        prefixTool.addRecursively(headResource);
        out.println("<?xml version=\"1.0\"?>");
        out.print("<rdf:RDF");
        for (String prefix : prefixTool.getPrefixes()) {
            out.print(" xmlns:" + prefix + "=\"" + prefixTool.full(prefix) + "\"");
        }
        out.println(">");

        writeResource(headResource, out, prefixTool, new HashSet<Resource>());

        out.println("</rdf:RDF>");
        
        out.flush();
    }

    private static String escapeXML(String str) {
        return str.replaceAll("\"", "&quot;").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;");
    }

    private void writeResource(Resource resource, PrintWriter out, PrefixTool prefixTool, Set<Resource> done) {
        // Prevent closed loops
        if (done.contains(resource)) {
            return;
        }
        done.add(resource);

        if (resource instanceof URIRef) {
            out.println("\t<rdf:Description rdf:about=\"" + ((URIRef) resource).getURI().toString() + "\">");
        } else {
            out.println("\t<rdf:Description rdf:nodeID=\"" + ((BNode) resource).getId() + "\">");
        }
        for (URIRef prop : resource.getTriples().keySet()) {
            for (Value value : resource.getTriples().get(prop)) {
                final String[] ss = prefixTool.split(prop.getURI());
                String tag;
                if (ss.length == 2) {
                    tag = ss[0] + ":" + ss[1];
                    out.print("\t\t<" + tag);
                } else {
                    assert (ss.length == 1);
                    // Issue here is that XML cannot name nodes with URIs so we must find a way to represent it as a QName
                    int n = ss[0].lastIndexOf('#');
                    if (n < 0 || !ss[0].substring(n).matches(PrefixTool.validPrefix)) {
                        n = ss[0].lastIndexOf('/');
                        if (n < 0 || !ss[0].substring(n).matches(PrefixTool.validPrefix)) {
                            n = ss[0].length() - 1;
                            if (n < 0 || !ss[0].substring(n).matches(PrefixTool.validPrefix)) {
                                // I believe this is unreachable but cannot verify at the moment
                                throw new RuntimeException("Bad URI " + ss[0]);
                            }
                        }
                    }
                    tag = "ns:" + ss[0].substring(n);
                    out.print("\t\t<ns:" + ss[0].substring(n) + " xmlns:ns=\"" + ss[0].substring(0, n));
                }
                if (value instanceof URIRef) {
                    out.println(" rdf:resource=\"" + ((URIRef) value).toString() + "\"/>");
                } else if (value instanceof BNode) {
                    out.println(" rdf:nodeID=\"" + ((BNode) value).getId() + "\"/>");
                } else {
                    final Literal literal = (Literal) value;
                    if (literal.getLanguage() != null) {
                        out.println(" xml:lang=\"" + literal.getLanguage() + "\">" + escapeXML(literal.getValue()) + "</" + tag + ">");
                    } else if (literal.getDatatype() != null) {
                        out.println(" rdf:datatype=\"" + literal.getDatatype().toString() + "\">" + escapeXML(literal.getValue()) + "</" + tag + ">");
                    } else {
                        out.println(">" + escapeXML(literal.getValue()) + "</" + tag + ">");
                    }
                }
            }
        }
        out.println("\t</rdf:Description>");
        
        for (Set<Value> values : resource.getTriples().values()) {
            for (Value value : values) {
                if (value instanceof Resource) {
                    writeResource((Resource) value, out,prefixTool,done);
                }
            }
        }
    }
}

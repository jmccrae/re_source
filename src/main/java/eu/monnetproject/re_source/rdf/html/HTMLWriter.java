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
package eu.monnetproject.re_source.rdf.html;

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
 * Write as simple style-able HTML with RDFa
 *
 * @author John McCrae
 */
public class HTMLWriter implements RDFWriter {

    private final String localPrefix;

    public HTMLWriter() {
        this.localPrefix = "";
    }

    public HTMLWriter(String localPrefix) {
        this.localPrefix = localPrefix;
    }

    @Override
    public void write(Resource headResource, Writer out2) {
        final PrintWriter out = new PrintWriter(out2);
        final PrefixTool prefixTool = new PrefixTool();
        prefixTool.addRecursively(headResource);
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        out.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println();
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\"");
        for (String prefix : prefixTool.getPrefixes()) {
            out.print(" xmlns:" + prefix + "=\"" + prefixTool.full(prefix) + "\"");
        }
        out.println(" version=\"XHTML+RDFa 1.0\">");
        out.println("<head>");
        out.println("<title>" + escapeXML(headResource.toString()) + "</title>");
        out.println("</head>");
        if (headResource instanceof URIRef) {
            out.println("<body about=\"" + ((URIRef) headResource).getURI().toString() + "\">");
        } else {
            out.println("<body>");
        }

        writeResource(headResource, out, prefixTool, new HashSet<Resource>());

        out.println("</body>");
        out.println("</html>");

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
            out.println("\t<div about=\"" + ((URIRef) resource).getURI().toString() + "\" class=\"uriref\">");
        } else {
            out.println("\t<div id=\"" + ((BNode) resource).getId() + "\" class=\"bnode\">");
        }
        for (URIRef prop : resource.getTriples().keySet()) {
            for (Value value : resource.getTriples().get(prop)) {

                final String[] ss = prefixTool.split(prop.getURI());
                String rel;
                if (ss.length == 2) {
                    rel = ss[0] + ":" + ss[1];
                } else {
                    assert (ss.length == 1);
                    rel = ss[0];
                }
                if (value instanceof URIRef) {
                    final String uriStr = ((URIRef) value).getURI().toString();
                    if (!uriStr.startsWith(localPrefix)) {
                        out.println("<a href=\"" + uriStr + "\" rel=\"" + rel + "\"/>");
                        continue;
                    }
                }
                out.print("\t\t<span property=\"" + rel+"\"");
                if (value instanceof Resource) {
                    out.println(" class=\"property\">");
                    writeResource((Resource) value, out, prefixTool, done);
                    out.println("\t\t</span>");
                } else {
                    final Literal literal = (Literal) value;
                    if (literal.getLanguage() != null) {
                        out.println(" xml:lang=\"" + literal.getLanguage() + "\" class=\"langliteral\">" + escapeXML(literal.getValue()) + "</span>");
                    } else if (literal.getDatatype() != null) {
                        out.println(" rdf:datatype=\"" + literal.getDatatype().toString() + "\" class=\"typedliteral\">" + escapeXML(literal.getValue()) + "</span>");
                    } else {
                        out.println(" class=\"literal\">" + escapeXML(literal.getValue()) + "</span>");
                    }
                }
            }
        }
        out.println("\t</div>");
    }
}

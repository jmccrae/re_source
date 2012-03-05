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
 ********************************************************************************
 */
package eu.monnetproject.re_source.rdf;

import static eu.monnetproject.re_source.rdf.RDFPrefixes.*;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Create pretty and unique prefixes
 *
 * @author John McCrae
 */
public class PrefixTool {

    private static final String validPrefixStartChar = "[A-Za-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD";
    private static final String validPrefixChar = validPrefixStartChar + "\\-0-9\u00B7\u0300-\u036F\u203F-\u2040]";
    public static final String validPrefix = validPrefixStartChar + "]" + validPrefixChar + "*";
    /**
     * To cover a minor technical issue with RDF/XML serialization, "ns" is not a valid namespace
     */
    public static final String RESERVED_NS = "ns";
    private final Map<String, String> prefix2full = new HashMap<String, String>();
    private final Map<String, String> full2prefix = new HashMap<String, String>();

    public PrefixTool() {
        prefix2full.put("rdf", RDF);
        prefix2full.put("rdfs", RDFS);
        prefix2full.put("owl", OWL);
        prefix2full.put("xsd", XSD);

        full2prefix.put(RDF, "rdf");
        full2prefix.put(RDFS, "rdfs");
        full2prefix.put(OWL, "owl");
        full2prefix.put(XSD, "xsd");
    }

    public void add(URIRef ref) {
        final String uriStr = ref.getURI().toString();
        int pt = uriStr.lastIndexOf('#');
        if (pt < 0) {
            pt = uriStr.lastIndexOf('/');
        }
        if (pt >= 0) {
            int pt2 = uriStr.lastIndexOf("/", pt);
            if (pt2 > 0 && pt - pt2 - 1 > 0) {
                final String prefix = uriStr.substring(pt2 + 1, pt);
                final String full = uriStr.substring(0, pt+1);

                if (!prefix2full.containsKey(prefix) && prefix.matches(validPrefix) && !prefix.equals(RESERVED_NS)) {
                    prefix2full.put(prefix, full);
                    full2prefix.put(full, prefix);
                }
            }
        }

    }

    public void addRecursively(Resource resource) {
        addRecursively(resource, new HashSet<Resource>());
    }

    private void addRecursively(Resource resource, Set<Resource> done) {
        if (done.contains(resource)) {
            return;
        }
        done.add(resource);
        if (resource instanceof URIRef) {
            add((URIRef) resource);
        }
        for (URIRef ref : resource.getTriples().keySet()) {
            add(ref);
        }
        for (Set<Value> values : resource.getTriples().values()) {
            for (Value value : values) {
                if (value instanceof Resource) {
                    addRecursively((Resource) value, done);
                }
            }
        }
    }

    public String[] split(URI uri) {
        final String uriStr = uri.toString();
        for (String full : full2prefix.keySet()) {
            if (uriStr.startsWith(full)) {
                final String name = uriStr.substring(full.length(), uriStr.length());
                if (name.matches(validPrefix)) {
                    return new String[]{full2prefix.get(full), name};
                }
            }
        }
        return new String[]{uriStr};
    }

    public String toString(Value resource) {
        if (!(resource instanceof URIRef)) {
            return resource.toString();
        } else {
            final String[] s = split(((URIRef) resource).getURI());
            if (s.length == 1) {
                return resource.toString();
            } else {
                return s[0] + ":" + s[1];
            }
        }
    }

    public Set<String> getPrefixes() {
        return prefix2full.keySet();
    }

    public String full(String prefix) {
        return prefix2full.get(prefix);
    }
}

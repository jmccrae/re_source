/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.re_source.rdf;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for making RDF elements. Ensures that these elements are unique
 * 
 * @author John McCrae
 */
public class RDFFactory {
    
    private final Map<URI,URIRef> uriRefs = new HashMap<URI, URIRef>();
    private final Map<String,BNode> bNodes = new HashMap<String, BNode>();
    
    public RDFFactory() {
        
    }
    
    /**
     * Create a blank node
     * @param id  BNode id
     */
    public BNode newBNode(String id) {
        BNode bn = bNodes.get(id);
        if(bn == null) {
            bNodes.put(id, bn = new BNode(id));
        }
        return bn;
    }
    
    /**
     * Create an untyped literal (avoid if possible)
     * @param value The value of the literal
     */
    public Literal newLiteral(String value) {
        return new Literal(value);
    }
    
    /**
     * Create a language typed literal 
     * @param value The value of the literal
     * @param language The language as an IETF code (e.g., "en", "eng" or "en-GB")
     */
    public Literal newLiteral(String value, String language) {
        return new Literal(value, language);
    }
    
    /**
     * Create a datatyped literal (generally not used for natural language literals)
     * @param value The value of the literal
     * @param datatype The data type (e.g., according to XSD URI)
     */
    public Literal newLiteral(String value, URI dataType) {
        return new Literal(value, dataType);
    }
    
    /**
     * Create a URI ref
     * @param uri The URI
     */
    public URIRef newURIRef(URI uri) {
        URIRef uriRef = uriRefs.get(uri);
        if(uriRef == null) {
            uriRefs.put(uri,uriRef = new URIRef(uri));
        }
        return uriRef;
    }
}

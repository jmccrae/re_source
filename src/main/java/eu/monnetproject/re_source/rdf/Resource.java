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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A resource in a RDF graph. The resource also contains all triples that this 
 * node is a subject of. This is used instead of a separate SPOC index as for a 
 * linked data resource we wish only to return the description of the resource, 
 * i.e, the set of triples from this node.
 * 
 * @author John McCrae
 */
public class Resource extends Value {
    protected final Map<URIRef,Set<Value>> triples = new HashMap<URIRef, Set<Value>>();
    
    // Do not create subclasses other than the one here
    Resource() {
    }
    
    /**
     * Add a triple
     * @param property The property
     * @param value The object
     * @return true if the properties of this resource changed
     */
    public boolean addTriple(URIRef property, Value value) {
        if(triples.containsKey(property)) {
            return triples.get(property).add(value);
        } else {
            final HashSet<Value> set = new HashSet<Value>();
            set.add(value);
            triples.put(property,set);
            return true;
        }
    }
    
    /**
     * Remove a triple
     * @param property The property
     * @param value The object
     * @return true if the properties of this resource changed
     */
    public boolean removeTriple(URIRef property, Value value) {
        if(triples.containsKey(property)) {
            if(triples.get(property).size() == 1) {
                if(triples.get(property).iterator().next().equals(value)) {
                    triples.remove(property);
                    return true;
                } else {
                    return false;
                }
            } else {
                return triples.get(property).remove(value);
            }
        } else {
            return false;
        }
    }

    /**
     * Get the triples whose subject is this resource
     * @return An immutable map from properties to values
     */
    public Map<URIRef, Set<Value>> getTriples() {
        return Collections.unmodifiableMap(triples);
    }
}

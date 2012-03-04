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
package eu.monnetproject.re_source.rdf.turtle;

import eu.monnetproject.re_source.rdf.PrefixTool;
import eu.monnetproject.re_source.rdf.RDFWriter;
import eu.monnetproject.re_source.rdf.Resource;
import eu.monnetproject.re_source.rdf.URIRef;
import eu.monnetproject.re_source.rdf.Value;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
public class TurtleWriter implements RDFWriter {

    @Override
    public void write(URIRef headResource, Writer out2) {
        final PrintWriter out = new PrintWriter(out2);
        final PrefixTool prefixTool = new PrefixTool();
        prefixTool.addRecursively(headResource);
        for(String prefix : prefixTool.getPrefixes()) {
            out.println("@prefix " + prefix + ": <" + prefixTool.full(prefix)+ "> .");
        }
        out.println("");
        
        writeResource(headResource,out,prefixTool,new HashSet<Resource>());
        
        out.flush();
    }
        
    private void writeResource(Resource resource, PrintWriter out, PrefixTool prefixTool, Set<Resource> done) {
        // Prevent closed loops
        if(done.contains(resource))
            return;
        done.add(resource);
        
        if(resource.getTriples().isEmpty())
            return;
        out.print(prefixTool.toString(resource) + " ");
        final Iterator<URIRef> propIter = resource.getTriples().keySet().iterator();
        while(propIter.hasNext()) {
            final URIRef prop = propIter.next();
            out.print(prefixTool.toString(prop) + " ");
            final Iterator<Value> valueIter = resource.getTriples().get(prop).iterator();
            while(valueIter.hasNext()) {
                final Value value = valueIter.next();
                out.print(prefixTool.toString(value));
                if(valueIter.hasNext()) {
                    out.print(" ,\n\t\t");
                } else if(propIter.hasNext()) {
                    out.print(" ;\n\t");
                } else {
                    out.print(" .\n");
                }
            }
        }
        out.println();
        
        for(Set<Value> values : resource.getTriples().values()) {
            for(Value value : values) {
                if(value instanceof Resource) {
                    writeResource((Resource)value,out,prefixTool,done);
                }
            }
        }
    }
}

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

import eu.monnetproject.re_source.rdf.html.HTMLWriter;
import eu.monnetproject.re_source.rdf.turtle.TurtleWriter;
import eu.monnetproject.re_source.rdf.xml.RDFXMLWriter;

/**
 * The standard RDF Writer Builder (supports RDF/XML, Turtle and XHTML+RDFa)
 * 
 * @author John McCrae
 */
public class DefaultRDFWriterBuilder implements RDFWriterBuilder {

    @Override
    public RDFWriter getWriter(String mimeType, String localURL) {
        if(mimeType.equals("application/rdf+xml")) {
            return new RDFXMLWriter();
        } else if(mimeType.equals("text/turtle")) {
            return new TurtleWriter();
        } else if(mimeType.equals("application/xhtml+xml") || mimeType.equals("text/html")) {
            return new HTMLWriter(localURL);
        } else {
            return null;
        }
    }

    @Override
    public RDFWriter getWriter(String localURL) {
        return new RDFXMLWriter();
    }

    @Override
    public String defaultMIMEType() {
        return "application/rdf+xml";
    }

    
}

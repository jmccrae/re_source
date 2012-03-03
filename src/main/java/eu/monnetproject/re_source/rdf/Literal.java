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

/**
 * A literal in the RDF graph
 * 
 * @author John McCrae
 */
public class Literal implements Value {
    private final String value, language;
    private final URI datatype;

    
    Literal(String value) {
        this.value = value;
        this.language = null;
        this.datatype = null;
    }
    
    
    Literal(String value, String language) {
        this.value = value;
        this.language = language;
        this.datatype = null;
    }

    
    Literal(String value, URI datatype) {
        this.value = value;
        this.language = null;
        this.datatype = datatype;
    }

    /**
     * Get the value of this literal
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the data type of this literal
     * @return The data type or null for no type
     */
    public URI getDatatype() {
        return datatype;
    }

    /**
     * Get the language of this literal
     * @return The language as IETF code or null for no language
     */
    public String getLanguage() {
        return language;
    }

    private static String escapeLiteral(String v) {
        return v.replaceAll("\n", "\\\\n").replaceAll("\"", "\\\\\"").replaceAll("\r","\\\\r").replaceAll("\t","\\\\t");
    }
    
    @Override
    public String toString() {
        if(language != null) {
            return "\"" + escapeLiteral(value) + "\"@" + language;
        } else if(datatype != null) {
            return "\"" + escapeLiteral(value) + "\"^^" + datatype;
        } else {
            return "\"" + escapeLiteral(value) + "\"";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Literal other = (Literal) obj;
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        if ((this.language == null) ? (other.language != null) : !this.language.equals(other.language)) {
            return false;
        }
        if (this.datatype != other.datatype && (this.datatype == null || !this.datatype.equals(other.datatype))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 83 * hash + (this.language != null ? this.language.hashCode() : 0);
        hash = 83 * hash + (this.datatype != null ? this.datatype.hashCode() : 0);
        return hash;
    }
}

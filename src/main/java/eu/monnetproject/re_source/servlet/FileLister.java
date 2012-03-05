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
package eu.monnetproject.re_source.servlet;

import java.io.PrintWriter;
import javax.servlet.ServletContext;

/**
 * Writes index pages as HTML+RDFa, RDF/XML or Turtle
 * 
 * @author John McCrae
 */
public class FileLister {

    private FileLister() {
        
    }
    
    public static void writeFileAsHTML(PrintWriter out, String rootPath, ServletContext context) {
        
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        out.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println();
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + Re_SourceServlet.contextPath() + Re_SourceServlet.getProperty("main.css", "/default.css") + "\" />");
        out.println("<head>");
        out.println("<title>" + Re_SourceServlet.servletTitle() + "</title>");
        out.println("</head>");
        out.println("<body about=\"\">");
        for (String path : context.getResourcePaths(rootPath)) {
            final String relativePath = path.substring(rootPath.length());
            out.println("<div class=\"resourcelink\"><a href=\"" + relativePath + "\" rel=\""+Re_SourceServlet.contextPath()+Re_SourceServlet.ontologyPath()+"resource\">" + relativePath + "</a></div>");
        }
        out.println("</body>");
        out.println("</html>");
    }
    
    public static void writeFileAsXMLRDF(PrintWriter out, String rootPath, ServletContext context, String reqPath) {
        out.println("<?xml version=\"1.0\"?>");
        out.println("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:onto=\""+Re_SourceServlet.contextPath()+Re_SourceServlet.ontologyPath()+"\">");
        out.println("\t<rdf:Description rdf:about=\""+reqPath+"\">");
        for (String path : context.getResourcePaths(rootPath)) {
            final String relativePath = path.substring(rootPath.length());
            out.println("\t\t<onto:resource rdf:resource=\"" + reqPath + relativePath + "\"/>");
        }
        out.println("\t</rdf:Description>");
        out.println("</rdf:RDF>");
    }
    
    public static void writeFileAsTurtle(PrintWriter out, String rootPath, ServletContext context, String reqPath) {
        
        out.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        out.println("@prefix onto: <"+Re_SourceServlet.contextPath()+Re_SourceServlet.ontologyPath()+"> .");
        
        for (String path : context.getResourcePaths(rootPath)) {
            final String relativePath = path.substring(rootPath.length());
            out.println("<" + reqPath + "> onto:resource <" + reqPath + relativePath + "> .");
        }
    }
}

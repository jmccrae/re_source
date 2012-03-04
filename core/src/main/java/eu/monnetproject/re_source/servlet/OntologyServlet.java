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
package eu.monnetproject.re_source.servlet;

import eu.monnetproject.re_source.Converter;
import eu.monnetproject.re_source.rdf.Resource;
import eu.monnetproject.re_source.rdf.URIRef;
import eu.monnetproject.re_source.rdf.Value;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet produces a basic RDFS representation of a property
 *
 * @author John McCrae
 */
public class OntologyServlet extends HttpServlet {

    private Set<URIRef> props;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("") || pathInfo.equals("/")) {
            if (props == null) {
                buildProps(req);
            }
            resp.setContentType("application/rdf+xml");
            final PrintWriter out = resp.getWriter();
            out.println("<?xml version=\"1.0\"?>");
            out.println("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">");
            for (URIRef prop : props) {
                out.println("\t<rdf:Property rdf:about=\"" + prop.getURI().toString() + "\"/>");
            }
            out.println("</rdf:RDF>");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void buildProps(HttpServletRequest req) {
        props = new HashSet<URIRef>();
        final ServiceLoader<Converter> converters = ServiceLoader.load(Converter.class);
        final String servletPath = getServletPath(req);
        for (Converter converter : converters) {
            for (String path : getServletContext().getResourcePaths(Re_SourceServlet.DATA_PATH)) {
                System.err.println(path);
                final URI resourceURI = URI.create(servletPath + path);
                try {
                    final URIRef resource = converter.convert(getServletContext().getResource(path), resourceURI, servletPath);
                    if (resource != null) {
                        buildProps(resource, servletPath, new HashSet<Resource>());
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }
    }

    private void buildProps(Resource resource, String servletPath, Set<Resource> done) {
        if (done.contains(resource)) {
            return;
        }
        done.add(resource);
        for (URIRef prop : resource.getTriples().keySet()) {
            if (prop.getURI().toString().startsWith(servletPath)) {
                props.add(prop);
            }
            for (Value value : resource.getTriples().get(prop)) {
                if (value instanceof Resource) {
                    buildProps((Resource)value,servletPath,done);
                }
            }
        }
    }

    private String getServletPath(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + (req.getServerPort() != 80 ? ":" + req.getServerPort() : "")
                + req.getContextPath() + req.getServletPath();
    }
}

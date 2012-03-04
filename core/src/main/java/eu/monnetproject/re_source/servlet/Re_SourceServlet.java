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
import eu.monnetproject.re_source.SourceParseException;
import eu.monnetproject.re_source.rdf.RDFWriter;
import eu.monnetproject.re_source.rdf.RDFWriterBuilder;
import eu.monnetproject.re_source.rdf.URIRef;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author John McCrae
 */
public class Re_SourceServlet extends HttpServlet {
    public static final String DATA_PATH = "/WEB-INF/data";

    private final List<Converter> converters = new LinkedList<Converter>();
    private final List<RDFWriterBuilder> writers = new LinkedList<RDFWriterBuilder>();

    // Set of static variables set before the first call is handled
    private static ServletConfig servletConfig;
    private static String contextPath;
    private static String servletPath;
    private static String ontologyPath = "/ontology#";
    private static String servletTitle = "The re_source example servlet";
        
    /**
     * The path (from the context path) where the ontology servlet is deployed
     */
    public static String ontologyPath() {
        return ontologyPath;
    }

    /**
     * The title of pages produced by this servlet
     */
    public static String servletTitle() {
        return servletTitle;
    }
    
    /**
     * The URL where this application is deployed to
     */
    public static String contextPath() {
        return contextPath;
    }
    
    /**
     * The URL where this particular servlet is deployed
     */
    public static String servletPath() {
        return servletPath;
    }
        
    public static String getProperty(String prop, String defaultValue) {
        final String param = servletConfig.getInitParameter(prop);
        if(param == null) {
            return defaultValue;
        } else {
            return prop;
        }
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servletConfig = config;
        final ServiceLoader<Converter> convs = ServiceLoader.load(Converter.class);
        for (Converter converter : convs) {
            converters.add(converter);
        }
        final ServiceLoader<RDFWriterBuilder> rwbs = ServiceLoader.load(RDFWriterBuilder.class);
        for (RDFWriterBuilder rwb : rwbs) {
            writers.add(rwb);
        }
        if(config.getInitParameter("ontology.path") != null) {
            ontologyPath = config.getInitParameter("ontology.path");
        }
        if(config.getInitParameter("servlet.title") != null) {
            servletTitle = config.getInitParameter("servlet.title");
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        if(pathInfo == null || pathInfo.equals("") || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Re_source works, but there is no welcome page ;)");
            return;
        }
        final URL resource = getServletContext().getResource(DATA_PATH + req.getPathInfo());
        if (resource == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            contextPath = getContextPath(req);
            servletPath = getServletPath(req);
            final URI resourceURI = URI.create(servletPath + pathInfo);
            final List<String> accepts = getAccepts(req);
            for (Converter converter : converters) {
                try {
                    final URIRef rdf = converter.convert(resource, resourceURI, servletPath);
                    if (rdf != null) {
                        for (RDFWriterBuilder writerBuilder : writers) {
                            boolean acceptAll = false;
                            for (String mimeType : accepts) {
                                if (mimeType.equals("*/*")) {
                                    acceptAll = true;
                                } else {
                                    final RDFWriter writer = writerBuilder.getWriter(mimeType, servletPath);
                                    if (writer != null) {
                                        resp.setContentType(mimeType);
                                        resp.setStatus(HttpServletResponse.SC_OK);
                                        writer.write(rdf, resp.getWriter());
                                        return;
                                    }
                                }
                            }
                            if (accepts.isEmpty() || acceptAll) {
                                final RDFWriter writer = writerBuilder.getWriter(servletPath);
                                if (writer != null) {
                                    resp.setContentType(writerBuilder.defaultMIMEType());
                                    resp.setStatus(HttpServletResponse.SC_OK);
                                    writer.write(rdf, resp.getWriter());
                                    return;
                                }
                            }
                        }
                    }
                    // We can make it RDF but the client requested a MIME type
                    // that we cannot process
                    resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                    return;
                } catch (SourceParseException x) {
                    throw new ServletException(x);
                }
            }
            // Could not convert, just copy the resource
            resp.setStatus(HttpServletResponse.SC_OK);
            copy(resource.openStream(),resp.getOutputStream());
        }
    }

    private List<String> getAccepts(HttpServletRequest req) {
        final String acceptStr = req.getHeader("Accept");
        if (acceptStr == null) {
            return Collections.singletonList("*/*");
        } else {
            final String[] acceptFields = acceptStr.split(",");
            final ArrayList<String> accepts = new ArrayList<String>(acceptFields.length);
            for (String acceptField : acceptFields) {
                if (acceptField.indexOf(";") >= 0) {
                    accepts.add(acceptField.substring(0, acceptField.indexOf(";")));
                } else {
                    accepts.add(acceptField);
                }
            }
            return accepts;
        }
    }

    private String getContextPath(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + (req.getServerPort() != 80 ? ":" + req.getServerPort() : "")
                + req.getContextPath();
    }
    
    private String getServletPath(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + (req.getServerPort() != 80 ? ":" + req.getServerPort() : "")
                + req.getContextPath() + req.getServletPath();
    }
    
    private void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[32768];
        int s;
        while((s = is.read(buf)) != -1) {
            os.write(buf, 0, s);
        }
        os.flush();
    }
}

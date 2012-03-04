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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The main servlet that handles all requests for resources.
 *
 * @author John McCrae
 */
public class Re_SourceServlet extends HttpServlet {

    /**
     * The location of data resources
     */
    public static final String DATA_PATH = "/WEB-INF/data";
    /**
     * The location of extra headers files
     */
    public static final String EXTRA_HEADERS_FILE = "/META-INF/extraheaders";
    private final List<Converter> converters = new LinkedList<Converter>();
    private final List<RDFWriterBuilder> writers = new LinkedList<RDFWriterBuilder>();
    private final Map<String, String> extraHeaders = new HashMap<String, String>();
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
        if (param == null) {
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
        if (config.getInitParameter("ontology.path") != null) {
            ontologyPath = config.getInitParameter("ontology.path");
        }
        if (config.getInitParameter("servlet.title") != null) {
            servletTitle = config.getInitParameter("servlet.title");
        }
        try {
            final Enumeration<URL> extraHeaderResources = this.getClass().getClassLoader().getResources(EXTRA_HEADERS_FILE);
            while (extraHeaderResources.hasMoreElements()) {
                final Properties props = new Properties();
                props.load(extraHeaderResources.nextElement().openStream());
                for (String p : props.stringPropertyNames()) {
                    extraHeaders.put(p, props.getProperty(p));
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("") || pathInfo.equals("/")) {
            welcomePage(resp);
        } else if (pathInfo.endsWith("/")) {
            listFilesPage(resp, DATA_PATH + pathInfo);
        } else {
            final URL resource = getServletContext().getResource(DATA_PATH + req.getPathInfo());
            if (resource == null) {
                notFound(resp);
            } else {
                if (!resource(req, pathInfo, resource, resp)) {
                    // Could not convert, just copy the resource
                    resp.setStatus(HttpServletResponse.SC_OK);
                    copy(resource.openStream(), resp.getOutputStream());
                }
            }
        }
    }

    private boolean resource(HttpServletRequest req, final String pathInfo, final URL resource, HttpServletResponse resp) throws ServletException, IOException {
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
                                    return true;
                                }
                            }
                        }
                        if (accepts.isEmpty() || acceptAll) {
                            final RDFWriter writer = writerBuilder.getWriter(servletPath);
                            if (writer != null) {
                                resp.setContentType(writerBuilder.defaultMIMEType());
                                resp.setStatus(HttpServletResponse.SC_OK);
                                writer.write(rdf, resp.getWriter());
                                return true;
                            }
                        }
                    }
                }
                // We can make it RDF but the client requested a MIME type
                // that we cannot process
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                return true;
            } catch (SourceParseException x) {
                throw new ServletException(x);
            }
        }
        return false;
    }

    private void notFound(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void welcomePage(HttpServletResponse resp) throws IOException {
        listFilesPage(resp, DATA_PATH);
    }

    private void listFilesPage(HttpServletResponse resp, String rootPath) throws IOException {
        resp.setContentType("application/xhtml+xml");
        addExtraHeaders(resp);
        final PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        out.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println();
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + contextPath() + getProperty("main.css", "/default.css") + "\" />");
        out.println("<head>");
        out.println("<title>" + servletTitle() + "</title>");
        out.println("</head>");
        out.println("<body>");
        for (String path : getServletContext().getResourcePaths(rootPath)) {
            final String relativePath = path.substring(rootPath.length() + 1); // +1 to clip leading /
            out.println("<div class=\"resourcelink\"><a href=\"" + relativePath + "\">" + relativePath + "</a></div>");
        }
        out.println("</body>");
        out.println("</html>");
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
        while ((s = is.read(buf)) != -1) {
            os.write(buf, 0, s);
        }
        os.flush();
    }

    private void addExtraHeaders(HttpServletResponse resp) {
        for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
            resp.setHeader(entry.getKey(), entry.getValue());
        }
    }
}

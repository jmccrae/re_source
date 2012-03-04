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
package eu.monnetproject.re_source.util;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility functions for working with servlets
 *
 * @author John McCrae
 */
public final class ServletUtils {

    // Do not instantiate!
    private ServletUtils() {
    }
    private static final Map<String, Integer> defaultPorts = new HashMap<String, Integer>();

    static {
        defaultPorts.put("http", 80);
        defaultPorts.put("https", 443);
    }

    /**
     * Get the root path at which this application (WAR file) was deployed to
     *
     * @param req The request object from the client
     * @return The URL as a String
     */
    public static String getContextPath(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() 
                + (req.getServerPort() != defaultPorts.get(req.getScheme()).intValue() ? ":" + req.getServerPort() : "")
                + req.getContextPath();
    }

    /**
     * Get the servlet path to which the servlet is deployed
     *
     * @param req The request object from the client
     * @return The URL as a string
     */
    public static String getServletPath(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName()
                + (req.getServerPort() != defaultPorts.get(req.getScheme()).intValue() ? ":" + req.getServerPort() : "")
                + req.getContextPath() + req.getServletPath();
    }
}

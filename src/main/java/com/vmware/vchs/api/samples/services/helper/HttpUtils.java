/*
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.vchs.api.samples.services.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordsType;
/**
 * This class provides the common http functionality using the Apache HttpClient library.
 */
public class HttpUtils {
    /**
     * Executes an http request using the passed in request parameter.
     * 
     * @param request
     *            the HttpRequestBase subclass to make a request with
     * @return the response of the request
     */
    public static HttpResponse httpInvoke(HttpRequestBase request) {
        HttpResponse httpResponse = null;
        HttpClient httpClient = null;

        try {
            // Create a fresh HttpClient.. some samples will make calls to two (or more)
            // urls in a single run, sharing a static non-multithreaded instance causes
            // exceptions. This prevents that by ensuring each call to httpInvoke gets
            // its own instance.
            httpClient = createTrustingHttpClient();
            httpResponse = httpClient.execute(request);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return httpResponse;
    }

    /**
     * This method returns a secure HttpClient instance.
     * 
     * @return HttpClient a new secure instance of HttpClient
     */
    static HttpClient createSecureHttpClient() {
        return null;
    }

    /**
     * This method returns an HttpClient instance wrapped to trust all HTTPS certificates.
     * 
     * @return HttpClient a new instance of HttpClient
     */
    static HttpClient createTrustingHttpClient() {
        HttpClient base = new DefaultHttpClient();

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");

            // WARNING: This creates a TrustManager that trusts all certificates and should not be
            // used in production code.
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                        String authType) {
                }
            }
            };

            ctx.init(null, trustAllCerts, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));

            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * This helper method will create the JAXB context for the provided Class<T> and
     * marshal the provided JAXBElement<T> into a StringEntity.
     * 
     * @param clazz
     * @param jaxb
     * @return
     */
    public static <T> StringEntity marshal(Class<T> clazz, JAXBElement<T> jaxb) {
        JAXBContext jaxbContexts = null;
        OutputStream os = null;

        try {
            jaxbContexts = JAXBContext.newInstance(clazz);
        } catch (JAXBException ex) {
            throw new RuntimeException("Problem creating JAXB Context: ", ex);
        }

        try {
            javax.xml.bind.Marshaller marshaller = jaxbContexts.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            os = new ByteArrayOutputStream();
            // Marshal the object via JAXB to XML
            marshaller.marshal(jaxb, os);
        } catch (JAXBException e) {
            throw new RuntimeException("Problem marshalling instantiation VDC template", e);
        }

        try {
            return new StringEntity(os.toString());
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("Problem marshalling instantiation VDC template", e1);
        }
    }

    /**
     * This method will unmarshal the passed in entity using the passed in class type. It will check
     * the content-type to determine if the response is json or xml and use the appropriate
     * deserializer.
     * 
     * @param entity
     *            the entity to unmarshal
     * @param clazz
     *            the class type to base the unmarshal from
     * @return unmarshal an instance of the provided class type
     */
    public static <T> T unmarshal(HttpEntity entity, Class<T> clazz) {
        InputStream is = null;

        try {
            String s = EntityUtils.toString(entity);

            // Check if the response content-type contains the string json.. if so use GSON to
            // convert from json to the provided Class<T> type
            if (entity.getContentType().toString().toLowerCase().contains("json")) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                        new XMLGregorianClassConverter.Serializer());
                gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                        new XMLGregorianClassConverter.Deserializer());

                Gson g = gsonBuilder.create();
                return g.fromJson(s, clazz);
            }

            is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            return JAXB.unmarshal(is, clazz);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method can be used to query the vCloud Query API. The baseVcdUrl represents the portion
     * of the url up to the /api at the end. The /query is appended. Query parameters allow any of
     * the Query API to be called, and the response is the QueryResultRecordsType which the calling
     * method can then use to parse the response. The version is the version of the vCloud Query API
     * to call, and the token is the vCloud API token provided by the login step via the
     * x-vcloud-authorization response header.
     * 
     * @param baseVcdUrl
     *            the base vCloud API url up to the /api on the end
     * @param queryParameters
     *            any vCloud Query API Parameters
     * @param version
     *            the vCloud Query API version to call against
     * @param token
     *            the vCloud API token retrieved after a successful login
     * @return
     */
    public static QueryResultRecordsType getQueryResults(String baseVcdUrl, String queryParameters,
            String version, String token) {
        URL url = null;
        QueryResultRecordsType results = null;

        try {
            // Construct the URL from the baseVcdUrl to utilize the vCloud Query API to find a
            // matching template
            url = new URL(baseVcdUrl + "/query?" + queryParameters);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + baseVcdUrl);
        }

        HttpGet httpGet = new HttpGet(url.toString());
        httpGet.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                + version);

        httpGet.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

        HttpResponse response = HttpUtils.httpInvoke(httpGet);

        // make sure the status is 200 OK
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            results = HttpUtils.unmarshal(response.getEntity(), QueryResultRecordsType.class);
        }

        return results;
    }

    /**
     * Gets the string content from the passed in InputStream
     * 
     * @param is
     *            response stream from GET/POST method call
     * @return String content of the passed in InputStream
     */
    public static String getContent(InputStream is) {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        throw new RuntimeException(e);
                    }
                }
            }

            return sb.toString();
        }

        return "";
    }

    public static class XMLGregorianCalendarDeserializer implements
            JsonDeserializer<XMLGregorianCalendar> {
        public XMLGregorianCalendar deserialize(JsonElement je, Type type,
                JsonDeserializationContext jdc) throws JsonParseException {
            JsonObject jo = je.getAsJsonObject();
            GregorianCalendar c = new GregorianCalendar();
            if (null == jo) {
                System.out.println("DESERIALIZING JO IS NULL");
            } else {
                System.out.println("TOS created date is " + jo.getAsString());
            }
            try {
                XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            } catch (DatatypeConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.out);
            }
            // Location l = new Location(jo.getAsJsonPrimitive("mProvider").getAsString());
            // l.setAccuracy(jo.getAsJsonPrimitive("mAccuracy").getAsFloat());
            // etc, getting and setting all the data
            return null;
        }
    }

    public static class XMLGregorianClassConverter {
        public static class Serializer implements JsonSerializer {
            public Serializer() {
                super();
            }

            public JsonElement serialize(Object t, Type type,
                    JsonSerializationContext jsonSerializationContext) {
                XMLGregorianCalendar xgcal = (XMLGregorianCalendar) t;
                return new JsonPrimitive(xgcal.toXMLFormat());
            }
        }

        public static class Deserializer implements JsonDeserializer {
            public Object deserialize(JsonElement jsonElement, Type type,
                    JsonDeserializationContext jsonDeserializationContext) {
                try {
                    return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                            jsonElement.getAsString());
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }

    /**
     * This private helper method will construct the Accept or Content-Type headers from 1, 2 or
     * 3 parts depending on if the parts passed in are null or not. The mediaType passed in should
     * be one of application/xml, application/json, or any variance that would typically be
     * acceptable. The classType is optional, so if it's not needed, passing in null will avoid
     * it being added to the final string. Version as well is optional and like classType, if it's
     * null is not added.
     * 
     * @param mediaType
     *            the media type for the Accpet or Content-Type header that the result will apply to
     * @param classType
     *            the class= type, if needed (optional... pass in null to avoid it being used)
     * @param version
     *            the version= value, if needed (optional... pass in null to avoid it being used)
     * @return a concatenated string of the 1, 2 or 3 parts depending on what was provided
     */
    private static String buildMediaType(String mediaType, String classType, String version) {
        StringBuilder sb = new StringBuilder(mediaType);

        if (null != classType && classType.length() > 0) {
            sb.append(";class=");
            sb.append(classType);
        }

        if (null != version && version.length() > 0) {
            sb.append(";version=");
            sb.append(version);
        }

        return sb.toString();
    }

    /**
     * This method will make a GET request using the provided parameters. The URL is assumed to be
     * a complete URL, including any query parameters. The token is the OAUTH2 token provided by
     * the IAM login process. The accept and contentType parameters are used to set the initial
     * Http header media types for Accept and Content-Type respectively. The classType parameter
     * is optional and if specified will add a class=... to the Accept and/or Content-Type headers.
     * The version is optional and will add a version=... to the Accept and/or Content-Type headers.
     * 
     * @param url
     *            the full URL path to the API resource
     * @param token
     *            the OAUTH2 token
     * @param accept
     *            value to be part of the Accept header (e.g. application/json)
     * @param contentType
     *            value to be part of the Content-Type header (e.g. application/xml)
     * @param classType
     *            value to be part of the Accept of Content-Type header (e.g.
     *            class=com.vmware.vchs.iam.api.schema.v2.classes.user.User)
     * @param version
     *            the version of the API to request if provided
     * @return an instance of HttpResponse
     */
    public static HttpResponse httpGet(String url, String token, String accept, String contentType,
            String classType, String version) {
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        if (null != accept) {
            get.setHeader(HttpHeaders.ACCEPT, buildMediaType(accept, classType, version));
        }
        if (null != contentType) {
            get.setHeader(HttpHeaders.CONTENT_TYPE, buildMediaType(contentType, classType, version));
        }

        return httpInvoke(get);
    }

    /**
     * This method will make a GET request using the provided parameters. The URL is assumed to be
     * a complete URL, including any query parameters. The token is the OAUTH2 token provided by
     * the IAM login process. The accept and contentType parameters are used to set the initial
     * Http header media types for Accept and Content-Type respectively. The classType parameter
     * is optional and if specified will add a class=... to the Accept and/or Content-Type headers.
     * The version is optional and will add a version=... to the Accept and/or Content-Type headers.
     * 
     * @param url
     *            the full URL path to the API resource
     * @param token
     *            the OAUTH2 token
     * @param accept
     *            value to be part of the Accept header (e.g. application/json)
     * @param contentType
     *            value to be part of the Content-Type header (e.g. application/xml)
     * @param classType
     *            value to be part of the Accept of Content-Type header (e.g.
     *            class=com.vmware.vchs.iam.api.schema.v2.classes.user.User)
     * @param version
     *            the version of the API to request if provided
     * @return an instance of HttpResponse
     */
    public static HttpResponse httpPost(String url, String token, String accept,
            String contentType, String classType, String version, String requestBody) {
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        HttpEntity entity = null;

        try {
            entity = new StringEntity(requestBody);
            post.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        post.setEntity(entity);

        if (null != accept) {
            post.setHeader(HttpHeaders.ACCEPT, buildMediaType(accept, classType, version));
        }
        if (null != contentType) {
            post.setHeader(HttpHeaders.CONTENT_TYPE,
                    buildMediaType(contentType, classType, version));
        }

        return httpInvoke(post);
    }

    /**
     * This method will make a GET request using the provided parameters. The URL is assumed to be
     * a complete URL, including any query parameters. The token is the OAUTH2 token provided by
     * the IAM login process. The accept and contentType parameters are used to set the initial
     * Http header media types for Accept and Content-Type respectively. The classType parameter
     * is optional and if specified will add a class=... to the Accept and/or Content-Type headers.
     * The version is optional and will add a version=... to the Accept and/or Content-Type headers.
     * 
     * @param url
     *            the full URL path to the API resource
     * @param token
     *            the OAUTH2 token
     * @param accept
     *            value to be part of the Accept header (e.g. application/json)
     * @param contentType
     *            value to be part of the Content-Type header (e.g. application/xml)
     * @param classType
     *            value to be part of the Accept of Content-Type header (e.g.
     *            class=com.vmware.vchs.iam.api.schema.v2.classes.user.User)
     * @param version
     *            the version of the API to request if provided
     * @return an instance of HttpResponse
     */
    public static HttpResponse httpPut(String url, String token, String accept, String contentType,
            String classType, String version, String requestBody) {
        HttpPut put = new HttpPut(url);
        put.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        HttpEntity entity = null;

        try {
            entity = new StringEntity(requestBody);
            put.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        put.setEntity(entity);

        if (null != accept) {
            put.setHeader(HttpHeaders.ACCEPT, buildMediaType(accept, classType, version));
        }
        if (null != contentType) {
            put.setHeader(HttpHeaders.CONTENT_TYPE, buildMediaType(contentType, classType, version));
        }

        return httpInvoke(put);
    }

    /**
     * This method will make a GET request using the provided parameters. The URL is assumed to be
     * a complete URL, including any query parameters. The token is the OAUTH2 token provided by
     * the IAM login process. The accept and contentType parameters are used to set the initial
     * Http header media types for Accept and Content-Type respectively. The classType parameter
     * is optional and if specified will add a class=... to the Accept and/or Content-Type headers.
     * The version is optional and will add a version=... to the Accept and/or Content-Type headers.
     * 
     * @param url
     *            the full URL path to the API resource
     * @param token
     *            the OAUTH2 token
     * @param accept
     *            value to be part of the Accept header (e.g. application/json)
     * @param contentType
     *            value to be part of the Content-Type header (e.g. application/xml)
     * @param classType
     *            value to be part of the Accept of Content-Type header (e.g.
     *            class=com.vmware.vchs.iam.api.schema.v2.classes.user.User)
     * @param version
     *            the version of the API to request if provided
     * @return an instance of HttpResponse
     */
    public static HttpResponse httpDelete(String url, String token, String accept,
            String contentType, String classType, String version) {
        HttpDelete delete = new HttpDelete(url);
        delete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        if (null != accept) {
            delete.setHeader(HttpHeaders.ACCEPT, buildMediaType(accept, classType, version));
        }
        if (null != contentType) {
            delete.setHeader(HttpHeaders.CONTENT_TYPE,
                    buildMediaType(contentType, classType, version));
        }

        return httpInvoke(delete);
    }
}
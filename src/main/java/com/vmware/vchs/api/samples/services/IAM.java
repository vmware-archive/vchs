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
package com.vmware.vchs.api.samples.services;

import java.io.IOException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.iam.v2.User;
import com.vmware.vchs.iam.v2.Users;

/**
 * This helper class implements the API calls to the IAM service. It provides methods for making
 * REST API calls to log in to IAM as well as user management API calls.
 */
public class IAM {
    // IAM resource path for login API call
    private static final String LOGIN_URL_RESOURCE = "/api/iam/login";

    /**
     * This method will attempt to create a user session, effectively logging in if the username
     * and password are valid account credentials and the account TOS has been accepted (via
     * the UI currently).
     * 
     * @param hostname
     *            the url of the API to make requests to
     * @param username
     *            the username of the account to log in with
     * @param password
     *            the password of the account to log in with
     * @param version
     *            the version of the API to call
     * @return the value of the response header vchs-authorization if login is successful, null
     *         otherwise
     */
    public static final String login(String hostname, String username, String password,
            String version) {
        HttpPost post = new HttpPost(hostname + LOGIN_URL_RESOURCE);
        post.setHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic "
                        + Base64.encodeBase64URLSafeString(new String(username + ":" + password)
                                .getBytes()));
        post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(post);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    return response.getFirstHeader(SampleConstants.VCHS_AUTHORIZATION_HEADER)
                            .getValue();
                }
            }
        }

        return null;
    }

    /**
     * Retrieves an instance of User for the provided userId
     * 
     * @param url
     *            the url to make API requests to
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param userId
     *            the id of the user to retrieve
     * @param version
     *            the version of the API to call
     * @return an instance of User if found, null otherwise
     */
    public static final User getUser(String url, String token, String userId, String version) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_IAM_USERS);
        sb.append("/");
        sb.append(userId);

        HttpResponse response = HttpUtils.httpGet(sb.toString(), token,
                SampleConstants.APPLICATION_XML, null, SampleConstants.CLASS_IAM_USER, version);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        return HttpUtils.unmarshal(response.getEntity(), User.class);
                    } catch (ParseException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method will assume the provied URL is to a IAM PayGo service. The Accept header will be
     * set to application/json;version=5.7
     * 
     * @param url
     *            the url of the API to make requests to
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return an instance of Users if accessible, null otherwise
     */
    public static final Users getUsers(String url, String token, String version) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_IAM_USERS);

        HttpResponse response = HttpUtils.httpGet(sb.toString(), token,
                SampleConstants.APPLICATION_XML, null, SampleConstants.CLASS_IAM_USERS, version);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        return HttpUtils.unmarshal(response.getEntity(), Users.class);
                    } catch (ParseException e) {
                        throw new RuntimeException("Error unmarshalling user response"
                                + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method will create a new user from the provided User instance.
     * 
     * @param url
     *            the url of the API to make requests to
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param user
     *            the instance of User to create the new user from
     * @param version
     *            the version of the API to call
     * @return an instance of User if created successfully, null otherwise
     */
    public static final User createUser(String url, String token, User user, String version) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_IAM_USERS);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Serializer());
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Deserializer());
        Gson g = gsonBuilder.create();

        String userToSend = g.toJson(user);

        HttpResponse response = HttpUtils.httpPost(sb.toString(), token,
                SampleConstants.APPLICATION_JSON, SampleConstants.APPLICATION_JSON,
                SampleConstants.CLASS_IAM_USER, version, userToSend);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    try {
                        String s = EntityUtils.toString(response.getEntity());
                        User createdUser = g.fromJson(s, User.class);

                        return createdUser;
                    } catch (JsonSyntaxException e1) {
                        throw new RuntimeException("JSON Syntax Exception: " + e1.getMessage());
                    } catch (ParseException e1) {
                        throw new RuntimeException("Response Parsing Exception: " + e1.getMessage());
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method updates the passed in user by using the PUT method to the
     * /api/iam/Users/{user-id} url. The response should be a 204 if the update was successful.
     * 
     * @param url
     *            the hostname url to send the update request to
     * @param token
     *            the OAUTH token to authenticate the request with
     * @param user
     *            the user instance to send as the entity to update with
     * @return true if the update was successful false otherwise
     */
    public static boolean updateUser(String url, String token, User user, String version) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_IAM_USERS);
        sb.append("/");
        sb.append(user.getId());

        // Configure the GSON object
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Serializer());
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Deserializer());
        Gson g = gsonBuilder.create();

        // Convert the object to JSON
        String userToSend = g.toJson(user);

        // Send the PUT request
        HttpResponse response = HttpUtils.httpPut(sb.toString(), token,
                SampleConstants.APPLICATION_JSON, SampleConstants.APPLICATION_JSON,
                SampleConstants.CLASS_IAM_USER, version, userToSend);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                return response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED;
            }
        }

        return false;
    }

    /**
     * This method will send a DELETE request to the provided url to delete the user referenced by
     * the provided userId.
     * 
     * @param url
     *            the hostname url to send the update request to
     * @param token
     *            the OAUTH token to authenticate the request with
     * @param userId
     *            the id of the user to delete
     * @return the http status code
     */
    public static boolean deleteUser(String url, String token, String userId, String version) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("/");
        sb.append(userId);

        HttpResponse response = HttpUtils.httpDelete(sb.toString(), token,
                SampleConstants.APPLICATION_JSON, null, null, version);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                // Status code for successful delete should be NO CONTENT (204).
                return response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT;
            }
        }

        return false;
    }
}
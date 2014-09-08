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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.sc.instance.v1.InstanceListType;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vchs.sc.service.v1.PlanListType;
import com.vmware.vchs.sc.service.v1.PlanType;

/**
 * This helper class implements API calls to the service controller. It provides methods to get the
 * list of plans and instances for the provided authorization token, as well as creating a new
 * instance or deleting an existing instance.
 */
public class ServiceController {
    /**
     * Returns a collection of plans
     * 
     * @param hostname
     *            the url of the API to make requests to
     * @param version
     *            the version of the API to call
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @return a collection of PlanType instances if successful, null otherwise
     */
    public static Collection<PlanType> getPlans(String hostname, String version, String token) {

        HttpGet get = new HttpGet(hostname + SampleConstants.API_SERVICECONTROLLER_PLANS);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_XML_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance. Because the Accept type is XML, unmarshal
                // response type
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    PlanListType plans = HttpUtils.unmarshal(response.getEntity(),
                            PlanListType.class);
                    if (null != plans) {
                        return plans.getPlans();
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method will retrieve all the discoverable instances accessible for the logged in user as
     * determined by the provided authToken
     * 
     * @param hostname
     *            the host URL to make API calls to
     * @param version
     *            the version of the API to call
     * @param token
     *            the authentication token to make API calls with
     * @return a collection of instances if found, null otherwise
     */
    public static Collection<InstanceType> getInstances(String hostname, String version,
            String token) {
        HttpGet get = new HttpGet(hostname + "/api/sc/instances");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_XML_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance. Because the Accept type is XML, unmarshal
                // response type
                Error error = HttpUtils.unmarshal(response.getEntity(), Error.class);
                // Do something with Error, possibly using Error.getCode() value to
                // determine the specific reason for the error.
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    InstanceListType instances = HttpUtils.unmarshal(response.getEntity(),
                            InstanceListType.class);
                    if (null != instances) {
                        return instances.getInstances();
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method will return the list of instances matching the provided serviceType string. It
     * will get all plans and find the plans that match the serviceName attribute to the provided
     * serviceType string, then look through all instances for matching instances to the plans,
     * only returning those instances.
     * 
     * @param hostname
     *            the host URL to make API calls to
     * @param version
     *            the version of the API to call
     * @param token
     *            the OAUTH token to use with API calls
     * @param serviceType
     *            the Plan serviceName attribute string to use to match plans with
     * @return a collection of instances, or null if nothing is found
     */
    public static Collection<InstanceType> getInstancesForServiceType(String hostname,
            String version, String token, String serviceType) {
        // Retrieve all plans for the authenticated user
        Collection<PlanType> plans = getPlans(hostname, version, token);

        // Retrieve all instances for the authenticated user
        Collection<InstanceType> instances = getInstances(hostname, version, token);

        if (null != plans && null != instances && plans.size() > 0 && instances.size() > 0) {
            Collection<InstanceType> matchedInstances = new ArrayList<InstanceType>();

            for (PlanType plan : plans) {
                if (plan.getServiceName().equalsIgnoreCase(serviceType)) {
                    // Now get the service instance, if available, that was created from this plan
                    for (InstanceType instance : instances) {
                        if (instance.getPlanId().equalsIgnoreCase(plan.getId())) {
                            matchedInstances.add(instance);
                        }
                    }
                }
            }

            return matchedInstances;
        }

        return null;
    }

    /**
     * Creates an instance of the service provided by the instanceId
     * 
     * @param hostname
     *            the host URL to make API calls to
     * @param version
     *            the version of the API to call
     * @param token
     *            the authentication token to make API calls with
     * @param planId
     *            the id of the plan to base this instance from
     * @param serviceGroupId
     *            the service group id of the org to create this instance in
     * @return true if successfully created, false otherwise.
     */
    public static boolean createInstance(String hostname, String version, String token,
            String planId, String serviceGroupId) {
        HttpPost post = new HttpPost(hostname + "/api/sc/instances");
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version
                + ";class=com.vmware.vchs.sc.restapi.model.instancetype");
        post.setHeader(HttpHeaders.CONTENT_TYPE, SampleConstants.APPLICATION_JSON_VERSION + version
                + ";class=com.vmware.vchs.sc.restapi.model.instancespecparamstype");

        InstanceType it = new InstanceType();
        it.setName("NewVDC");
        it.setDescription("A description of new service");
        it.setPlanId(planId);
        it.setServiceGroupId(serviceGroupId);

        Gson g = new Gson();

        String instanceToCreate = g.toJson(it);

        HttpEntity entity;
        try {
            entity = new StringEntity(instanceToCreate);
            post.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        HttpResponse response = HttpUtils.httpInvoke(post);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                String s = null;

                try {
                    s = EntityUtils.toString(response.getEntity());
                    Error error = g.fromJson(s, Error.class);
                    // Do something with Error, possibly using Error.getCode() value to
                    // determine the specific reason for the error.
                } catch (ParseException e) {
                    throw new RuntimeException("Error parsing error response: " + e.getMessage());
                } catch (IOException e) {
                    throw new RuntimeException("Error with response: " + e.getMessage());
                }
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Deletes a service provided by the instance id
     * 
     * @param hostname
     *            the host URL to make API calls to
     * @param version
     *            the version of the API to call
     * @param token
     *            the authenticated token to make API calls with
     * @param instanceId
     *            the id of the instance to delete
     * @return true if deleted, false if not
     */
    public static boolean deleteInstance(String hostname, String version, String token,
            String instanceId) {
        HttpDelete delete = new HttpDelete(hostname + "/api/sc/instances/" + instanceId);
        delete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        delete.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(delete);

        if (null != response) {
            // If the response status is 400 - 599
            if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // This is here to show when an error occurs, the response should always be
                // an Error instance
                String s = null;
                Gson g = new Gson();

                try {
                    s = EntityUtils.toString(response.getEntity());
                    Error error = g.fromJson(s, Error.class);
                    // Do something with Error, possibly using Error.getCode() value to
                    // determine the specific reason for the error.
                } catch (ParseException e) {
                    throw new RuntimeException("Error parsing error response: " + e.getMessage());
                } catch (IOException e) {
                    throw new RuntimeException("Error with response: " + e.getMessage());
                }
            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return true;
                }
            }
        }

        return false;
    }
}
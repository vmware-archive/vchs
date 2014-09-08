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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.vms.billabledata.v5.BilledCostsType;
import com.vmware.vchs.vms.billabledata.v5.BilledUsageType;
import com.vmware.vchs.vms.billabledata.v5.ServiceGroupType;
import com.vmware.vchs.vms.billabledata.v5.ServiceGroupsType;
/**
 * This helper class implements API calls to the metering and billing APIs. This particular class
 * focuses on the billing API calls.
 */
public class Billing {
    /**
     * List all service-groups for a given company. It will include company details and service
     * group list. It uses auth token to determine the company for which service-groups will be
     * returned
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @return instance of ServiceGroupsType or null
     */
    public static ServiceGroupsType listServiceGroups(String url, String authToken, String version) {
        HttpGet get = new HttpGet(url + "/api/billing/service-groups");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.serviceGroups;version=" + version);

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
                    return HttpUtils.unmarshal(response.getEntity(), ServiceGroupsType.class);
                }
            }
        }

        return null;
    }

    /**
     * Retrieve data associated with the specified service group. It will include details like
     * service group id, service group display name, billing currency, billing attributes and
     * anniversary dates
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceGroupId
     *            the service group id
     * @return instance of ServiceGroupType or null
     */
    public static ServiceGroupType getServiceGroupDetails(String url, String authToken,
            String version, String serviceGroupId) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_BILLING_SERVICE_GROUP);
        sb.append("/");
        sb.append(serviceGroupId);

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_JSON, null, SampleConstants.CLASS_BILLING_SERVICEGROUP,
                version);

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
                }            } else {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return HttpUtils.unmarshal(response.getEntity(), ServiceGroupType.class);
                }
            }
        }

        return null;
    }

    /**
     * List cost items associated with the specified service group for a given billing month; Cost
     * items are shown only for the month for which bill is generated. It will support the following
     * cost items - Support Cost and Service Credit If query params are not specified, then it will
     * default to 'month={last billed month} and year={last billed year}'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            the version of the API to invoke
     * @param serviceGroupId
     *            service group id
     * @return an instance of BilledCostsType or null
     */
    public static BilledCostsType getBilledCosts(String url, String authToken, String version,
            String serviceGroupId) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_BILLING_SERVICE_GROUP);
        sb.append("/");
        sb.append(serviceGroupId);
        sb.append("/billed-costs");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null, SampleConstants.CLASS_BILLING_BILLED_COSTS,
                version);

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
                    return HttpUtils.unmarshal(response.getEntity(), BilledCostsType.class);
                }
            }
        }

        return null;
    }

    /**
     * Gets billed usage for the specified L1 for a given billing month; Usage is shown only for
     * months for which bill is generated. It will include details like bill duration, entity
     * details, metric name, usage, unit, rate, currency and cost If query params are not specified,
     * then it will default to 'month={last billed month} and year={last billed year}'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @param l1id
     *            the L1 id
     * @return an instance of BilledUsageType or null
     */
    public static BilledUsageType getL1BilledUsage(String url, String authToken, String version,
            String serviceInstanceId, String l1id) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_BILLING_SERVICE_INSTANCE);
        sb.append("/");
        sb.append(serviceInstanceId);
        sb.append("/l1/");
        sb.append(l1id);
        sb.append("/billed-usage");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null, SampleConstants.CLASS_BILLING_BILLED_USAGE,
                version);

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
                    return HttpUtils.unmarshal(response.getEntity(), BilledUsageType.class);
                }
            }
        }

        return null;
    }

    /**
     * Gets billed usage for the specified L2 for a given billing month; Usage is shown only for
     * months for which bill is generated. It will include details like bill duration, entity
     * details, metric name, usage, unit, rate, currency and cost If query params are not specified,
     * then it will default to 'month={last billed month}, year={last billed year} and level=self'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @param l2
     *            the L2 id
     * @return an instance of BilledUsageType or null
     */
    public static BilledUsageType getL2BilledUsage(String url, String authToken, String version,
            String serviceInstanceId, String l2id) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_BILLING_SERVICE_INSTANCE);
        sb.append("/");
        sb.append(serviceInstanceId);
        sb.append("/l2");
        sb.append(l2id);
        sb.append("/billed-usage");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null, SampleConstants.CLASS_BILLING_BILLED_USAGE,
                version);

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
                    return HttpUtils.unmarshal(response.getEntity(), BilledUsageType.class);
                }
            }
        }

        return null;
    }

    /**
     * Gets billed usage for the specified service instance for a given billing month; Usage is
     * shown only for months for which bill is generated. It will include details like bill
     * duration, entity details, metric name, usage, unit, rate, currency and cost If query params
     * are not specified, then it will default to 'month={last billed month}, year={last billed
     * year} and level=self'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @return an instance of BilledUsageType or null
     */
    public static BilledUsageType getBilledUsageForServiceInstance(String url, String authToken,
            String version, String serviceInstanceId) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_BILLING_SERVICE_INSTANCE);
        sb.append("/");
        sb.append(serviceInstanceId);
        sb.append("/billed-usage");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null, SampleConstants.CLASS_BILLING_BILLED_USAGE,
                version);

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
                    return HttpUtils.unmarshal(response.getEntity(), BilledUsageType.class);
                }
            }
        }

        return null;
    }
}
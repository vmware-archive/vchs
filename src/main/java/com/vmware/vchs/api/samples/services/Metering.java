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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.vms.billabledata.v1.BillableCostsType;
import com.vmware.vchs.vms.billabledata.v1.BillableUsageType;
/**
 * This helper class implements API calls to the metering and billing APIs. This particular class
 * focuses on the metering API calls.
 */
public class Metering {
    /**
     * Gets billable/current usage for the specified L1; Usage is shown only for the duration for
     * which bill is not yet generated. It will include details like entity details, metric name,
     * usage, unit, rate, currency and cost If query params are not specified, then it will default
     * to 'duration=BillToDate' Query params start and end are mutually exclusive with duration
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance ID
     * @param l1Id
     *            the L1 id
     * @return an instance of BillableUsageType or null
     */
    public static BillableUsageType getL1BillableUsage(String url, String authToken,
            String version, String serviceInstanceId, String l1id) {

        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_METERING_SERVICE_INSTANCE);
        sb.append("/");
        sb.append(serviceInstanceId);
        sb.append("/l1");
        sb.append(l1id);
        sb.append("/billable-usage");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null,
                SampleConstants.CLASS_METERING_BILLABLE_USAGE, version);

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
                    BillableUsageType bill = HttpUtils.unmarshal(response.getEntity(),
                            BillableUsageType.class);
                    return bill;
                }
            }
        }

        if (null != response) {
        }

        return null;
    }

    /**
     * Gets billable/current usage for the specified L2; Usage is shown only for durations for which
     * bill is not yet generated. It will include details like entity details, metric name, usage,
     * unit, rate, currency and cost If query params are not specified, then it will default to
     * 'duration=BillToDate' Query params start and end are mutually exclusive with duration
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance ID
     * @param l2Id
     *            the L2 id
     * @return instance of BillableUsageType or null
     */
    public static BillableUsageType getL2BillableUsage(String url, String authToken,
            String version, String serviceInstanceId, String l2id) {

        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_METERING_SERVICE_INSTANCE);
        sb.append("/");
        sb.append(serviceInstanceId);
        sb.append("/l2");
        sb.append(l2id);
        sb.append("/billable-usage");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null,
                SampleConstants.CLASS_METERING_BILLABLE_USAGE, version);

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
                    BillableUsageType bill = HttpUtils.unmarshal(response.getEntity(),
                            BillableUsageType.class);
                    return bill;
                }
            }
        }

        return null;
    }

    /**
     * Gets billable/current usage for the specified service instance; Usage is shown only for
     * durations for which bill is not yet generated. It will include details like entity details,
     * metric name, usage, unit, rate, currency and cost If query params are not specified, then it
     * will default to 'duration=BillToDate' Query params start and end are mutually exclusive with
     * duration
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @return instance of BillableUsageType or null
     */
    public static BillableUsageType getBillableUsage(String url, String authToken, String version,
            String serviceInstanceId) {

        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_METERING_SERVICE_INSTANCE);
        sb.append("/");
        sb.append(serviceInstanceId);
        sb.append("/billable-usage");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null,
                SampleConstants.CLASS_METERING_BILLABLE_USAGE, version);

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
                    return HttpUtils.unmarshal(response.getEntity(), BillableUsageType.class);
                }
            }
        }

        return null;
    }

    /**
     * Represent billable/current value of cost items associated with the specified service group;
     * Only those cost items are listed which are available after last bill cut/generation date. It
     * will support the following cost items - Support Cost and Service Credit
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceGroupId
     *            the service group id
     * @return instance of BillableCostsType or null
     */
    public static BillableCostsType getBillableCosts(String url, String authToken, String version,
            String serviceGroupId) {

        StringBuilder sb = new StringBuilder(url);
        sb.append(SampleConstants.API_METERING_SERVICE_GROUP);
        sb.append("/");
        sb.append(serviceGroupId);
        sb.append("/billable-costs");

        HttpResponse response = HttpUtils.httpGet(sb.toString(), authToken,
                SampleConstants.APPLICATION_XML, null,
                SampleConstants.CLASS_METERING_BILLABLE_COSTS, version);

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
                    BillableCostsType bill = HttpUtils.unmarshal(response.getEntity(),
                            BillableCostsType.class);
                    return bill;
                }
            }
        }

        return null;
    }
}
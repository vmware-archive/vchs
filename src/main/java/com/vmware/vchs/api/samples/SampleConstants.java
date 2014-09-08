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
package com.vmware.vchs.api.samples;
/**
 * This class provides common constants that will be used in samples.
 */
public final class SampleConstants {
    /*
     * Prevent this class from being instantiated
     */
    private SampleConstants() {
    }

    /*
     * Http header for vCHS API
     */
    public static final String APPLICATION_XML_VERSION = "application/xml;version=";

    /*
     * Http header for VCD API
     */
    public static final String APPLICATION_PLUS_XML_VERSION = "application/*+xml;version=";

    /*
     * Http header for JSON media type
     */
    public static final String APPLICATION_JSON = "application/json";

    /*
     * Http header for XML media type
     */
    public static final String APPLICATION_XML = "application/xml";

    /*
     * VCD Authorization header string
     */
    public static final String VCD_AUTHORIZATION_HEADER = "x-vcloud-authorization";

    /*
     * Content-Type header for VCD session
     */
    public static final String APPLICATION_XML_VCD_SESSION = "application/xml;class=vnd.vmware.vchs.vcloudsession";

    /*
     * Default vCHS Public API entry point
     */
    public static final String DEFAULT_HOSTNAME = "vchs.vmware.com";

    /*
     * Default vCHS IAM Public API entry point for login and user management
     */
    public static final String DEFAULT_IAM_HOSTNAME = "iam.vchs.vmware.com";

    /*
     * Default vCHS Public API Version to make calls to
     */
    public static final String DEFAULT_VCHS_VERSION = "5.7";

    /*
     * Default VCD API Version to make calls to
     */
    public static final String DEFAULT_VCD_VERSION = "5.7";

    /*
     * VCloud Public API Versions url
     */
    public static final String VERSION_URL = "/api/versions";

    /*
     * The string value representing a vCloud Org
     */
    public static final String ORG = "application/vnd.vmware.vcloud.org+xml";

    /*
     * The string value representing a vCloud Edge Gateway
     */
    public static final String CONTENT_TYPE_EDGE_GATEWAY = "application/vnd.vmware.admin.edgeGatewayServiceConfiguration+xml";

    /*
     * The string value representing the vCHS Authorization request header
     */
    public static final String VCHS_AUTHORIZATION_HEADER = "vchs-authorization";

    /*
     * The string value representing the application/json media type and the version of the API to
     * use
     */
    public static final String APPLICATION_JSON_VERSION = "application/json;version=";

    /*
     * The string value representing the OnDemand compute service type
     */
    public static final String COMPUTE_SERVICE_TYPE = "com.vmware.vchs.compute";

    /*
     * The string value representing ServiceController Plans resource
     */
    public static final String API_SERVICECONTROLLER_PLANS = "/api/sc/plans";

    /*
     * The string value representing ServiceController Instances resource
     */
    public static final String API_SERVICECONTROLLER_INSTANCES = "/api/sc/instances";

    /*
     * The string value representing IAM Login resource
     */
    public static final String API_IAM_LOGIN = "/api/iam/login";

    /*
     * The string value representing IAM Users resource
     */
    public static final String API_IAM_USERS = "/api/iam/Users";

    /*
     * The string value representing IAM Users 'self' resource; the self is User
     * requesting the call
     */
    public static final String API_IAM_USERS_SELF = "/api/iam/Users?self=1";

    /*
     * The string value representing the Billing ServiceGroup resource
     */
    public static final String API_BILLING_SERVICE_GROUP = "/api/billing/service-group";

    /*
     * The string value representing the Billing ServiceInstance resource
     */
    public static final String API_BILLING_SERVICE_INSTANCE = "/api/billing/service-instance";

    /*
     * The string value representing the Metering ServiceGroup resource
     */
    public static final String API_METERING_SERVICE_GROUP = "/api/metering/service-group";

    /*
     * The string value representing the Metering ServiceInstance resource
     */
    public static final String API_METERING_SERVICE_INSTANCE = "/api/metering/service-instance";

    /*
     * The string value representing the Http Header class types for Billing and Metering
     */
    public static final String CLASS_BILLING_SERVICEGROUP = "vnd.vmware.vchs.billing.serviceGroup";
    public static final String CLASS_BILLING_BILLED_COSTS = "vnd.vmware.vchs.billing.billedCosts";
    public static final String CLASS_BILLING_BILLED_USAGE = "vnd.vmware.vchs.billing.billedUsage";
    public static final String CLASS_METERING_SERVICEGROUP = "vnd.vmware.vchs.metering.serviceGroup";
    public static final String CLASS_METERING_BILLED_COSTS = "vnd.vmware.vchs.metering.billedCosts";
    public static final String CLASS_METERING_BILLED_USAGE = "vnd.vmware.vchs.metering.billedUsage";
    public static final String CLASS_METERING_BILLABLE_USAGE = "vnd.vmware.vchs.metering.billableUsage";
    public static final String CLASS_METERING_BILLABLE_COSTS = "vnd.vmware.vchs.metering.billableCosts";
    
    /*
     * The string value representing the Http Header class types for IAM
     */
    public static final String CLASS_IAM_USER = "com.vmware.vchs.iam.api.schema.v2.classes.user.User";
    public static final String CLASS_IAM_USERS = "com.vmware.vchs.iam.api.schema.v2.classes.user.Users";
}
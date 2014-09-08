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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vcloud.api.rest.schema_v1_5.DeployVAppParamsType;
import com.vmware.vcloud.api.rest.schema_v1_5.InstantiateVAppTemplateParamsType;
import com.vmware.vcloud.api.rest.schema_v1_5.InstantiateVdcTemplateParamsType;
import com.vmware.vcloud.api.rest.schema_v1_5.InstantiationParamsType;
import com.vmware.vcloud.api.rest.schema_v1_5.LinkType;
import com.vmware.vcloud.api.rest.schema_v1_5.NetworkConnectionSectionType;
import com.vmware.vcloud.api.rest.schema_v1_5.ObjectFactory;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordsType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultVAppTemplateRecordType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.ResourceEntitiesType;
import com.vmware.vcloud.api.rest.schema_v1_5.ResourceReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;
import com.vmware.vcloud.api.rest.schema_v1_5.TasksInProgressType;
import com.vmware.vcloud.api.rest.schema_v1_5.VAppTemplateType;
import com.vmware.vcloud.api.rest.schema_v1_5.VAppType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcTemplateListType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcTemplateType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcType;
import com.vmware.vcloud.api.rest.schema_v1_5.VmType;

/**
 * This class implements API calls to the vCloud Compute API.
 */
public class Compute {
    /**
     * This method will log in to compute using the provided URL (which should be the /api/sessions
     * path).
     * 
     * @param url the url to the VCD API to make requests to
     * @param username the username to log in with
     * @param password the password to log in with
     * @param orgName the org name to use as part of the login process
     * @param version the version of the API to call
     * @return the value of the header x-vcloud-authorization if login success
     */
    public static final String login(String url, String username, String password, String orgName,
            String version) {
        // Default base URL to log in to using the provided URL
        HttpPost post = new HttpPost(url);

        // Encode the username and password provided via the command line options.username and
        // options.password appending the provided orgName to the username before encoding. Compute
        // services require username@orgName:password syntax to log in with.
        String auth = "Basic "
                + Base64.encodeBase64URLSafeString(new String(username + "@" + orgName + ":"
                        + password).getBytes());

        post.setHeader(HttpHeaders.AUTHORIZATION, auth);
        post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION + version
                + ";charset=utf-8");

        HttpResponse response = HttpUtils.httpInvoke(post);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Return the response header x-vcloud-authorization which contains the session
                // token for the now logged in user.
                return response.getFirstHeader(SampleConstants.VCD_AUTHORIZATION_HEADER).getValue();
            }
        }

        return null;
    }

    /**
     * This method will use the provided input parameters to create a VM.
     * 
     * @param vdc
     *            the VdcType instance to create the VM in to
     * @param template
     *            the vApp template to create the VM from
     * @param vAppName
     *            the name of the created VM
     * @param version
     *            the version of the API to use
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @return an instance of VAppType if successful, null otherwise
     */
    public static final VAppType createVmFromTemplate(VdcType vdc, VAppTemplateType template,
            String vAppName, String version, String token) {
        ReferenceType vappReference = new ReferenceType();
        vappReference.setHref(template.getHref());

        // Create an InstantiateVAppTemplateParamsType object and initialize it
        InstantiateVAppTemplateParamsType instvApp = new InstantiateVAppTemplateParamsType();

        // Set the name of vApp using the options.vappname (command line option --targetvappname)
        instvApp.setName(vAppName);

        // Deploy the VM
        instvApp.setDeploy(Boolean.TRUE);

        // Power on this VM
        instvApp.setPowerOn(Boolean.TRUE);

        // vApp reference to be used
        instvApp.setSource(vappReference);
        instvApp.setDescription("VM Create from template");
        instvApp.setAllEULAsAccepted(Boolean.TRUE);

        InstantiationParamsType instParams = new InstantiationParamsType();

        instvApp.setInstantiationParams(instParams);

        // Get the HREF link to send POST to to instantiate the vapp
        List<LinkType> links = vdc.getLink();
        String instantiateHref = null;
        for (LinkType link : links) {
            if (link.getType().contains("instantiateVAppTemplate")) {
                instantiateHref = link.getHref();
                break;
            }
        }

        // Create HttpPost request to perform InstantiatevApp action
        HttpPost post = new HttpPost(instantiateHref);
        post.setHeader(HttpHeaders.CONTENT_TYPE, SampleConstants.APPLICATION_PLUS_XML_VERSION
                + version);
        post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION + version
                + ";charset=utf-8");
        post.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

        // Create the XSD generated ObjectFactory factory class
        ObjectFactory obj = new ObjectFactory();

        // Create the InstantiateVdcTemplateParamsType from the ObjectFactory
        JAXBElement<InstantiateVAppTemplateParamsType> instvAppTemplate = obj
                .createInstantiateVAppTemplateParams(instvApp);

        // Get the StringEntity marshaled instance
        StringEntity se = HttpUtils.marshal(InstantiateVAppTemplateParamsType.class,
                instvAppTemplate);

        // Set the Content-Type header for the VM vApp template parameters
        se.setContentType("application/vnd.vmware.vcloud.instantiateVAppTemplateParams+xml");
        post.setEntity(se);

        // Invoke the HttoPost to initiate the VM creation process
        HttpResponse response = HttpUtils.httpInvoke(post);

        // Make sure response status is 201 Created
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            return HttpUtils.unmarshal(response.getEntity(), VAppType.class);
        }

        return null;
    }

    /**
     * This method will return a collection of VdcType objects for the passed in OrgListType.
     * 
     * @param org
     *            the list of Orgs to get VDCs from
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return a collection of VdcType instances if available, null otherwise.
     */
    public static final Collection<VdcType> getVDCsForOrgs(OrgListType org, String token,
            String version) {
        if (null != org) {
            List<VdcType> vdcs = new ArrayList<VdcType>();
            List<LinkType> links = org.getLink();
            for (LinkType link : links) {
                if (link.getRel().equalsIgnoreCase("down")
                        && link.getType().equalsIgnoreCase("application/vnd.vmware.vcloud.vdc+xml")) {
                    HttpGet get = new HttpGet(link.getHref());
                    get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                            + version + ";charset=utf-8");
                    get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);
                    HttpResponse response = HttpUtils.httpInvoke(get);

                    if (null != response
                            && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        VdcType vdc = HttpUtils.unmarshal(response.getEntity(), VdcType.class);
                        if (null != vdc) {
                            vdcs.add(vdc);
                        }
                    }
                }
            }

            return vdcs;
        }

        return null;
    }

    /**
     * @param vApp
     *            the vApp to search for a matching Vm
     * @return the VmType instance
     */
    /**
     * Searches through the passed in vApp children element for a VmType Vm that matches the name of
     * the command line options.vappName. If found it is returned, otherwise a RuntimeException is
     * thrown.
     * 
     * @param vApp
     *            the VAppType to get the VM from
     * @param vmName
     *            the name of the VM to match
     * @param version
     *            the version of the API to call
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @return an instance of VmType if found, null otherwise
     */
    public static final VmType getVmFromVApp(VAppType vApp, String vmName, String version,
            String token) {
        // Get the status of initialization operation and IP details
        if (null != vApp.getChildren()) {
            List<VmType> vms = vApp.getChildren().getVm();

            for (VmType vm : vms) {
                List<LinkType> links = vm.getLink();

                for (LinkType link : links) {
                    // If there is a rel="up", we use that to get the ID and match it to the
                    // passed in vApp id
                    if (link.getRel().equalsIgnoreCase("up")) {
                        // make GET request to get the up vApp to compare it to the
                        // passed in vApp
                        HttpGet get = new HttpGet(vApp.getHref());
                        get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);
                        get.setHeader(HttpHeaders.ACCEPT,
                                SampleConstants.APPLICATION_PLUS_XML_VERSION + version
                                        + ";charset=utf-8");
                        HttpResponse response = HttpUtils.httpInvoke(get);
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            VAppType upVApp = HttpUtils.unmarshal(response.getEntity(),
                                    VAppType.class);
                            if (upVApp.getName().equalsIgnoreCase(vmName)) {
                                return vm;
                            }
                        }
                    }
                }

                if (vm.getName().equalsIgnoreCase(vmName)) {
                    return vm;
                }
            }
        }

        return null;
    }

    /**
     * This method will return a collection of VmType instances for the passed in Vdc. It will use
     * another method found in this class, getVAppsForVdc, to first get the vapps for the vdc. In
     * most cases, there is one Vm to each VApp. Just in case any of the VApp's returned for the VDC
     * contain more than one VM, this method will loop through each VApp collection of VM children
     * and add them all to the final collection that is returned.
     * 
     * @param vdc
     *            the VdcType instance to get all Vms for.
     * @param token
     *            the vCloud API Auth token
     * @param version
     *            the version of the API to invoke
     * @return a collection of VmType instances, or null
     */
    public static final Collection<VmType> getVmsForVdc(VdcType vdc, String token, String version) {
        if (null != vdc) {
            Collection<VAppType> vapps = getVAppsForVdc(vdc, token, version);
            if (null != vapps) {
                Collection<VmType> vms = new ArrayList<VmType>();
                for (VAppType vapp : vapps) {
                    if (null != vapp.getChildren()) {
                        List<VmType> vs = vapp.getChildren().getVm();
                        for (VmType v : vs) {
                            vms.add(v);
                        }
                    }
                }

                return vms;
            }
        }

        return null;
    }

    /**
     * This method will return a collection of vApps for the provided VDC if found.
     * 
     * @param vdc
     *            the VdcType to get the VApp collection from
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return a collection of VappType instances if found, null otherwise
     */
    public static final Collection<VAppType> getVAppsForVdc(VdcType vdc, String token,
            String version) {
        if (null != vdc && null != vdc.getResourceEntities()) {
            ResourceEntitiesType resourceEntities = vdc.getResourceEntities();
            List<ResourceReferenceType> resources = resourceEntities.getResourceEntity();
            Collection<VAppType> vapps = new ArrayList<VAppType>();
            for (ResourceReferenceType resource : resources) {
                if (resource.getType().equalsIgnoreCase("application/vnd.vmware.vcloud.vApp+xml")) {
                    HttpGet get = new HttpGet(resource.getHref());
                    get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);
                    get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                            + version + ";charset=utf-8");
                    HttpResponse response = HttpUtils.httpInvoke(get);

                    if (null != response
                            && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        vapps.add(HttpUtils.unmarshal(response.getEntity(), VAppType.class));
                    }
                }
            }

            return vapps;
        }

        return null;
    }

    /**
     * This method will retrieve the specified Org's details
     * 
     * @param url
     *            the url to make API requests to
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return an instance of OrgListType if found, null otherwise
     */
    public static final OrgListType getOrgDetails(String url, String token, String version) {
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION + version
                + ";charset=utf-8");
        get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return HttpUtils.unmarshal(response.getEntity(), OrgListType.class);
        }

        return null;
    }

    /**
     * This method will use the vCloud Query API to search for a single template with a matching
     * name. If found it is returned, otherwise null is returned.
     * 
     * @param url
     *            the URL to make the API request to
     * @param vdc
     *            the VdcType to get the template from
     * @param templateName
     *            the name of the template to find
     * @param token
     *            OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return an instance of VAppTemplateType if found, null otherwise
     */
    public static final VAppTemplateType getTemplateForVdc(String url, VdcType vdc,
            String templateName, String token, String version) {
        QueryResultRecordsType queryResults = HttpUtils.getQueryResults(url,
                "type=vAppTemplate&filter=name==" + templateName, version, token);

        List<JAXBElement<? extends QueryResultRecordType>> rslt = queryResults.getRecord();

        VAppTemplateType vat = null;

        // We should have only one record with the name matching templateName
        if (rslt.size() == 1) {
            QueryResultVAppTemplateRecordType qrrt = (QueryResultVAppTemplateRecordType) rslt
                    .get(0).getValue();
            String templateHref = qrrt.getHref();

            // invoke the GET request to the template href to get the VAppTemplateType
            HttpGet get = new HttpGet(templateHref);
            get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                    + version + ";charset=utf-8");
            get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

            HttpResponse response = HttpUtils.httpInvoke(get);

            // make surethe status is 200 OK
            if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // unmarshal the response entity into a VAppTemplateType
                vat = HttpUtils.unmarshal(response.getEntity(), VAppTemplateType.class);
            }
        }

        return vat;
    }

    /**
     * @param vdcTemplate
     *            the VdcTemplateType object to instantiate the new VDC from
     * @param token
     *            the vCloud Rest API user authentication token
     * @param version
     *            the version of the vCloud Rest API to make requests against
     * @return the HttpStatus code result of the request.
     */
    /**
     * This method will use the vCloud Rest API to create a new VDC from the provided VDC Template.
     * 
     * @param vdcTemplateRef
     *            a ReferencType instance that refers to the VDC template
     * @param url
     *            the url to make API requests to
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @param vdcName
     *            the name of the VDC to create
     * @param description
     *            a description to provide for the created VDC
     * @return an instance of TaskType if successful which can be queried for status, or null
     *         otherwise
     */
    public static final TaskType createVdcFromVdcTemplate(ReferenceType vdcTemplateRef, String url,
            String token, String version, String vdcName, String description) {
        if (null != vdcTemplateRef && null != token && null != version) {
            HttpPost post = new HttpPost(url);
            post.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);
            post.setHeader(HttpHeaders.CONTENT_TYPE,
                    "application/vnd.vmware.vcloud.instantiateVdcTemplateParams+xml");
            post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                    + version + ";charset=utf-8");
            InstantiateVdcTemplateParamsType newTemplate = new InstantiateVdcTemplateParamsType();
            newTemplate.setDescription(description);
            newTemplate.setName(vdcName);
            newTemplate.setSource(vdcTemplateRef);

            // Create the XSD generated ObjectFactory factory class
            ObjectFactory obj = new ObjectFactory();

            // Create the InstantiateVdcTemplateParamsType from the ObjectFactory
            JAXBElement<InstantiateVdcTemplateParamsType> t = obj
                    .createInstantiateVdcTemplateParams(newTemplate);

            // Get the StringEntity marshaled instance
            StringEntity se = HttpUtils.marshal(InstantiateVdcTemplateParamsType.class, t);

            // Set it as the POST body
            post.setEntity(se);

            // Make the call
            HttpResponse postResponse = HttpUtils.httpInvoke(post);

            // Make sure the response HTTP status code is 202 Accepted
            if (null != postResponse
                    && postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
                // Accepted, so return the TaskType instance
                return HttpUtils.unmarshal(postResponse.getEntity(), TaskType.class);
            }
        }

        return null;
    }

    /**
     * This method will issue requests to the vCloud Rest API to retrieve any accessible VDC
     * Templates for the user authenticated by the provided token. The return of this method is a
     * collection of ReferenceType objects, however, the only ReferenceType objects added to the
     * returned collection are those that have the type =
     * "application/vnd.vmware.admin.vdcTemplate+xml". The reason the ReferenceType is returned and
     * not the VdcTemplateType, is because when instantiating a new VDC template, the source of the
     * VdcTemplate is required, which is the ref.getHref() value. To avoid further lookup calls when
     * needing to create a VDC from a template, by returning the ReferenceType, access to the
     * VdcTemplateType source is made available.
     * 
     * @param url
     *            the url to make API requests to
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return a collection of ReferenceType objects if found, null otherwise
     */
    public static final Collection<ReferenceType> getVdcTemplates(String url, String token,
            String version) {

        HttpGet get = new HttpGet(getBaseUrl(url) + "/vdcTemplates");
        get.setHeader(HttpHeaders.ACCEPT, "application/vnd.vmware.admin.vdcTemplates+xml;version="
                + version + ";charset=utf-8");
        get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            VdcTemplateListType listTemplates = HttpUtils.unmarshal(response.getEntity(),
                    VdcTemplateListType.class);

            if (null != listTemplates) {
                List<ReferenceType> refs = listTemplates.getVdcTemplate();

                if (null != refs && refs.size() > 0) {

                    List<ReferenceType> templates = new ArrayList<ReferenceType>();
                    for (ReferenceType ref : refs) {
                        if (ref.getType().equalsIgnoreCase(
                                "application/vnd.vmware.admin.vdcTemplate+xml")) {
                            templates.add(ref);
                        }
                    }

                    return templates;
                }
            }
        }

        return null;
    }

    /**
     * This method will retrieve all the VDC Templates (via the getVdcTemplates() method found in
     * this class) and then iterate the results looking for a matching VDC template with the name
     * passed in the templateName parameter. If a match is found, a GET request is done on the
     * matching HREF to get the VdcTemplateType instance details and is returned.
     * 
     * @param url
     *            the url of the API to make requests to
     * @param templateName
     *            the name of the template to find
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return a ReferenceType instance if found, null otherwise
     */
    public static final ReferenceType findVdcTemplateByName(String url, String templateName,
            String token, String version) {

        Collection<ReferenceType> refs = getVdcTemplates(url, token, version);

        if (null != refs) {
            for (ReferenceType ref : refs) {
                if (ref.getName().equalsIgnoreCase(templateName)) {
                    return ref;
                }
            }
        }

        return null;
    }

    /**
     * This helper method will retrieve the VdcTemplateType from the provided ReferenceType.
     * 
     * @param ref
     *            the ReferenceType instance that contains the VdcTemplateType
     * @param token
     *            the OAUTH2 authentication token from IAM
     * @param version
     *            the version of the API to call
     * @return an instance of VdcTemplateType, or null otherwise
     */
    public static final VdcTemplateType getVdcTemplateFromRef(ReferenceType ref, String token,
            String version) {
        HttpGet get = new HttpGet(ref.getHref());
        get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);
        get.setHeader(HttpHeaders.ACCEPT, "application/vnd.vmware.admin.vdcTemplate+xml;version="
                + version + ";charset=utf-8");
        HttpResponse response = HttpUtils.httpInvoke(get);
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return HttpUtils.unmarshal(response.getEntity(), VdcTemplateType.class);
        }

        return null;
    }

    /**
     * This method uses the vCloud Query API to retrieve vCloud VDC Templates. It will return a
     * collection of VAppTemplateType instances for each template retrieved.
     * 
     * @param vdc
     *            the VDC to find and return all templates from
     */
    public static final Collection<VAppTemplateType> getTemplatesForVdc(String computeUrl,
            VdcType vdc, String version, String token) {
        // Query the vCloud Query API to search for a vAppTemplate matching the
        // options.templateName (command line option --templatename)
        QueryResultRecordsType queryResults = HttpUtils.getQueryResults(getBaseUrl(computeUrl),
                "type=vAppTemplate", version, token);

        List<JAXBElement<? extends QueryResultRecordType>> rslt = queryResults.getRecord();

        // We should have only one record with the name matching templateName
        if (null != rslt && rslt.size() > 0) {
            Collection<VAppTemplateType> templates = new ArrayList<VAppTemplateType>();

            for (JAXBElement<? extends QueryResultRecordType> record : rslt) {
                if (record.getDeclaredType().equals(QueryResultVAppTemplateRecordType.class)) {
                    QueryResultVAppTemplateRecordType template = (QueryResultVAppTemplateRecordType) record
                            .getValue();

                    // invoke the GET request to the template href to get the VAppTemplateType
                    HttpGet get = new HttpGet(template.getHref());
                    get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                            + version + ";charset=utf-8");
                    get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);
                    HttpResponse response = HttpUtils.httpInvoke(get);

                    // make sure the status is 200 OK
                    if (null != response
                            && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        // unmarshal the response entity into a VAppTemplateType
                        VAppTemplateType vat = HttpUtils.unmarshal(response.getEntity(),
                                VAppTemplateType.class);
                        templates.add(vat);
                    }
                }
            }

            return templates;
        }

        return null;
    }

    /**
     * This method will attempt to deploy (or undeploy) the provided vApp. The Boolean provided
     * parameter, deploy, if set to true attempts to deploy the vApp, otherwise false will attempt
     * to undeploy the vApp.
     * 
     * @param vApp
     * @param deploy
     * @param version
     * @param token
     * @return
     */
    public static final TaskType deployUndeployVM(VAppType vApp, Boolean deploy, String version,
            String token) {
        String deployHref = null;

        // Search the list of links for the vApp rel="deploy" to get the correct Href
        for (LinkType link : vApp.getLink()) {
            if (link.getRel().equalsIgnoreCase("deploy")) {
                deployHref = link.getHref();
                break;
            }
        }

        // Only proceed if we found a valid deploy Href
        if (null != deployHref) {
            DeployVAppParamsType deployParams = new DeployVAppParamsType();
            deployParams.setPowerOn(deploy);

            // Create the XSD generated ObjectFactory factory class
            ObjectFactory obj = new ObjectFactory();

            // Create the DeployVAppParamsType from the ObjectFactory
            JAXBElement<DeployVAppParamsType> deployParamsType = obj
                    .createDeployVAppParams(deployParams);

            // Get the StringEntity marshaled instance
            StringEntity se = HttpUtils.marshal(DeployVAppParamsType.class, deployParamsType);

            HttpPost deployPost = new HttpPost(deployHref);
            deployPost.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                    + version + ";charset=utf-8");
            deployPost.setHeader(HttpHeaders.AUTHORIZATION, token);

            // Set the Content-Type header for the VM vApp template parameters
            se.setContentType("application/vnd.vmware.vcloud.deployVAppParams+xml");

            // Set it as the POST body
            deployPost.setEntity(se);

            // Invoke the HttoPost to initiate the VM creation process
            HttpResponse response = HttpUtils.httpInvoke(deployPost);

            // Make sure response status is 201 Created
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
                TaskType taskType = HttpUtils.unmarshal(response.getEntity(), TaskType.class);
                return taskType;
            }
        }

        return null;
    }

    /**
     * This method will retrieve the internal ip value for the passed in Vm using the Vms
     * NetworkConfigSection to obtain the ip.
     * 
     * @param vm
     *            the VmType to obtain the internal ip from
     * @return the internal ip if found, otherwise "none" is returned
     */
    public static String getIpForVm(VmType vm, String version, String token) {
        HttpGet get = new HttpGet(vm.getHref());
        get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION + version
                + ";charset=utf-8");
        get.setHeader(HttpHeaders.AUTHORIZATION, token);

        // Request the NetworkConnection information for the VM to extract IP from it.
        // HttpResponse response = HttpUtils.httpInvoke(vcd.get(vm.getHref() + VM_NETWORK_URL,
        // options));

        HttpResponse response = HttpUtils.httpInvoke(get);

        // Make sure response is ok
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            NetworkConnectionSectionType nwsc = HttpUtils.unmarshal(response.getEntity(),
                    NetworkConnectionSectionType.class);
            if (null != nwsc) {
                String ip = nwsc.getNetworkConnection().get(0).getIpAddress();
                return ip;
            }
        }

        return null;
    }

    /**
     * Continually makes a GET request to the passed in Taks's Href with a 10 second delay between
     * each request to avoid sending too many requests to the API too fast.
     * 
     * @param task
     *            the TaskType instnace to query and wait for completion
     */
    public static void waitForTaskCompletion(TaskType task, String version, String token,
            int retryCount) {
        int retry = 0;

        TaskType statusTask = task;

        while ((!statusTask.getStatus().equalsIgnoreCase("success") && !statusTask.getStatus()
                .equalsIgnoreCase("error")) && retry++ < retryCount) {
            HttpGet get = new HttpGet(statusTask.getHref());
            get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                    + version + ";charset=utf-8");
            get.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

            HttpResponse response = HttpUtils.httpInvoke(get);
            statusTask = HttpUtils.unmarshal(response.getEntity(), TaskType.class);

            if (null != statusTask && statusTask.getStatus().equalsIgnoreCase("running")) {
                System.out.print(".");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Loops through the list of tasks provided by the tasksInProgressType parameter, calling the
     * waitForTaskCompletion method with each individual task.
     * 
     * @param tasksInProgressType
     *            a collection of tasks to wait for
     */
    public static void waitForTasks(TasksInProgressType tasksInProgressType, String version,
            String token) {
        List<TaskType> tasks = tasksInProgressType.getTask();
        for (TaskType task : tasks) {
            if (null != task) {
                waitForTaskCompletion(task, version, token, 10);
            }
        }
    }

    /**
     * This is a private helper method called by other methods in this class to find the base URL of
     * a compute instance from whatever URL is provided in the calling method. This is needed
     * because the compute instance apiUrl returned often refers to the compute Org url, instead of
     * just the base url.
     * 
     * @param url
     *            the compute instance apiUrl to try to find the compute instance base URL from
     * @return the compute instance base API URL
     */
    private static final String getBaseUrl(String url) {
        String baseUrl = url;

        int indx = url.indexOf("/compute/api");
        if (indx >= 0) {
            baseUrl = url.substring(0, indx + 12);
        }

        return baseUrl;
    }
}
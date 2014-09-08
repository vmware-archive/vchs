/*
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.    You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.vchs.api.samples.ondemand.endtoend;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmware.vchs.api.samples.SampleConstants;
/**
 * This class maintains all the command line options for the various samples.
 */
public class SampleCommandLineOptions {
    static final String OPTION_USERNAME = "username";
    static final String OPTION_PASSWORD = "password";
    static final String OPTION_HOSTNAME = "hostname";
    static final String OPTION_VERSION = "version";
    static final String OPTION_VDCNAME = "vdcname";
    static final String OPTION_VMNAME = "vmname";
    static final String OPTION_VDCID = "vdcid";
    static final String OPTION_VMID = "vmid";
    static final String OPTION_SGID = "sgid";
    static final String OPTION_SID = "sid";
    static final String OPTION_L1 = "l1";
    static final String OPTION_L2 = "l2";
    static final String OPTION_VM_TEMPLATE_NAME = "vmtemplatename";
    static final String OPTION_VDC_TEMPLATE_NAME = "vdctemplatename";
    static final String OPTION_REGION = "region";
    static final String OPTION_NETWORK_NAME = "networkname";

    // Command line arguments
    Option[] options = new Option[] {
            new Option(OPTION_USERNAME, true, "The username to log in with."),
            new Option(OPTION_PASSWORD, true, "The password for username to log in with."),
            new Option(OPTION_HOSTNAME, true,
                    "The vCHS OnDemand Server URL to send API calls to if "
                            + SampleConstants.DEFAULT_HOSTNAME + " is not to be used."),
            new Option(OPTION_VERSION, true,
                    "The version of the OnDemand API to run this sample against if the default of "
                            + SampleConstants.DEFAULT_VCHS_VERSION + " is not to be used."),
            new Option(OPTION_VDCNAME, true, "The VDC name to use when searching for a VDC match."),
            new Option(OPTION_VMNAME, true, "The VM name to use when searching for a VM match."),
            new Option(OPTION_VDCID, true, "The VDC ID to use when searching for a VDC match."),
            new Option(OPTION_VMID, true, "The VM ID to use when searching for a VM match."),
            new Option(OPTION_SGID, true, "The service group ID."),
            new Option(OPTION_SID, true, "The service instance ID."),
            new Option(OPTION_L1, true, "The L1 service ID."),
            new Option(OPTION_L2, true, "The L2 service ID."),
            new Option(OPTION_VM_TEMPLATE_NAME, true,
                    "The name of the template to use to create a VM from."),
            new Option(OPTION_VDC_TEMPLATE_NAME, true,
                    "The name of the template to use to create a VDC from."),
            new Option(OPTION_REGION, true, "The region the service may be found in."),
            new Option(OPTION_NETWORK_NAME, true,
                    "The name of the network to apply to a VM when creating or reconfiguring a VM."),
    };

    /*
     * The host (url) that will be used to make vCHS Public API Rest calls to
     */
    public String hostname = SampleConstants.DEFAULT_HOSTNAME;

    /*
     * The version of the vCHS Public API to make Rest calls against
     */
    public String version = SampleConstants.DEFAULT_VCHS_VERSION;

    /*
     * The vCHS Public API username encoded with the password for authentication purposes
     */
    public String username;

    /*
     * The vCHS Public API password encoded with the username for authentication purposes
     */
    public String password;

    public String vdcname;
    public String vmname;
    public String vdcid;
    public String vmid;
    public String sgid;
    public String sid;
    public String l1;
    public String l2;
    public String vmtemplatename;
    public String vdctemplatename;
    public String region;
    public String networkname;

    /**
     * This method returns the Apache Commons Cli Options instance that represents the common
     * options all vCHS Rest API Samples may need. Samples can provide their own subclass of this
     * class and override this method, call super on this method to get the Options object back,
     * then add their options to the returned Options from this method.
     * 
     * @return
     */
    protected Options getOptions() {
        Options opts = new Options();

        for (Option opt : options) {
            opts.addOption(opt);
        }

        return opts;
    }

    /**
     * This method will process the passed in command line args (typically from a main() method) and
     * process those args with the passed in Options instance. Subclasses of this class can call
     * super.parseOptions() and use the CommandLine instance returned to match any of the subclass
     * specific options processed by the CommandLineParser object in this method.
     * 
     * @param args
     *            the command line String[] args to process
     * @param options
     *            the Apache Command Line Options instance to parse the args against
     * @return an instance of the Apache cli CommandLine for subclasses to use to match up specific
     *         sublcass samples options with
     */
    public CommandLine parseOptions(String[] args) {
        CommandLineParser parser = new PosixParser();
        HelpFormatter help = new HelpFormatter();
        CommandLine cl = null;

        // process command line args
        try {
            cl = parser.parse(getOptions(), args);

            if (cl.hasOption(OPTION_USERNAME)) {
                username = cl.getOptionValue(OPTION_USERNAME);
            }

            if (cl.hasOption(OPTION_PASSWORD)) {
                password = cl.getOptionValue(OPTION_PASSWORD);
            }

            if (cl.hasOption(OPTION_HOSTNAME)) {
                hostname = cl.getOptionValue(OPTION_HOSTNAME);

                // remove trailing / if it exists
                if (hostname.endsWith("/")) {
                    hostname = hostname.substring(0, hostname.length() - 1);
                }
            }

            if (cl.hasOption(OPTION_VERSION)) {
                version = cl.getOptionValue(OPTION_VERSION);
            }

            if (cl.hasOption(OPTION_VDCNAME)) {
                vdcname = cl.getOptionValue(OPTION_VDCNAME);
            }

            if (cl.hasOption(OPTION_VMNAME)) {
                vmname = cl.getOptionValue(OPTION_VMNAME);
            }

            if (cl.hasOption(OPTION_VDCID)) {
                vdcid = cl.getOptionValue(OPTION_VDCID);
            }

            if (cl.hasOption(OPTION_VMID)) {
                vmid = cl.getOptionValue(OPTION_VMID);
            }

            if (cl.hasOption(OPTION_SGID)) {
                sgid = cl.getOptionValue(OPTION_SGID);
            }

            if (cl.hasOption(OPTION_SID)) {
                sid = cl.getOptionValue(OPTION_SID);
            }

            if (cl.hasOption(OPTION_L1)) {
                l1 = cl.getOptionValue(OPTION_L1);
            }

            if (cl.hasOption(OPTION_L2)) {
                l2 = cl.getOptionValue(OPTION_L2);
            }

            if (cl.hasOption(OPTION_VDC_TEMPLATE_NAME)) {
                vdctemplatename = cl.getOptionValue(OPTION_VDC_TEMPLATE_NAME);
            }

            if (cl.hasOption(OPTION_VM_TEMPLATE_NAME)) {
                vmtemplatename = cl.getOptionValue(OPTION_VM_TEMPLATE_NAME);
            }

            if (cl.hasOption(OPTION_REGION)) {
                region = cl.getOptionValue(OPTION_REGION);
            }

            if (cl.hasOption(OPTION_NETWORK_NAME)) {
                networkname = cl.getOptionValue(OPTION_NETWORK_NAME);
            }
        } catch (org.apache.commons.cli.ParseException e) {
            help.printHelp("vCHS Sample command line syntax", getOptions());
            System.exit(1);
        }

        return cl;
    }
}
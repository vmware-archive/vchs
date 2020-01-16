# VMware has ended active development of this project, this repository will no longer be updated.

# VMware OnDemand REST API Java Samples

<h3>Requirements</h3>
<ul>
  <li>Java 7 or later</li>
  <li>Maven 3</li>
</ul>

<h3>Clone repository</h3>

<pre>
git clone https://github.com/vmware/vchs.git
</pre>

<h3>Change directory</h3>

<pre>
cd vchs
</pre>

<h3>Install required libraries</h3>

<pre>
mvn install:install-file -Dfile=./lib/vchs-rest-apis-1.0.0.jar -DgroupId=com.vmware.vchs -DartifactId=vchs-rest-apis -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=./lib/rest-api-schemas-1.0.0.jar -DgroupId=com.vmware.vcloud -DartifactId=rest-api-schemas -Dversion=1.0.0 -Dpackaging=jar
</pre>

<h3>Compile</h3>

<pre>
mvn compile
</pre>

<h3>Run examples</h3>

<pre>
mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.endtoend.ListPlansAndInstances -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7"
</pre>

The list below will detail each sample you can run and what it does. You can then look at the source code to see how it uses the API for your own purposes.

<h2>Helper samples</h2>

  <h3>ListVdcAndVmIds</h3>
    This helper class can be executed to list all the VDCs and VMs of your organization. It's purpose is to list the name and IDs of each VDC and VM.
    The IDs can be utilized in various API calls, including but not limited to the Metering and Billing APIs where the L1 and L2 IDs represent VM and VDC IDs respectively.
    It also can be used after creating a new VDC, for example, to verify the VDC was indeed created.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.details.ListVdcAndVmIds -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7"</pre>

  <h3>ListVdcTemplates</h3>
    This helper class can be executed to list all the VDCs and VDC Templates of your organization. This is primarily to help
    identify VDC Templates you can use to create additional VDCs with.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.details.ListVdcTemplates -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7"</pre>

  <h3>ListVmTemplates</h3>
    This helper class can be executed to list all the VM templates available for your organization. The results of this call
    can then be used in the CreateVmFromTemplate end to end sample to create a new VM from one of the listed templates.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.details.ListVmTemplates -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7"</pre>

<h2>OnDemand End to End Samples</h2>

  <h3>CreateVdcFromTemplate</h3>
    This end to end sample will create an additional VDC for your organization from a VDC Template you
    specify as part of the command line.<br/><br/> 
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.endtoend.CreateVdcFromTemplate -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7 --region &lt;region identifier&gt; --vdctemplatename &lt;name of VDC template&gt;"</pre>

  <h3>CreateVmFromTemplate</h3>
    This end to end sample will create a new VM within a VDC of your organization using a VM template
    you specify. Using the ListVmTemplates helper class will provide the list of templates you can
    use to create a VM from.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.endtoend.CreateVmFromTemplate -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7 --region &lt;region identifier&gt; --vdcname &lt;name of the VDC to create VM in&gt; --vmname &lt;display name of VM&gt; --vmtemplatename &lt;the name of the VM template to create the VM from&gt;"</pre>

  <h3>ListPlansAndServices</h3>
    This is a simple end to end sample, which will log in to IAM, then list all available service
    plans, then list all available service instances. Each list will contain helpful details such
    as the plan ID, serviceName property and so on.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.endtoend.ListPlansAndInstances -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7"</pre>

  <h3>UserManagement</h3>
    This end to end sample will use the IAM user management APIs to display a list of all users
    in your organization that are accessible by the provided credentials, create a new user, update
    the user's Family Name value, then delete the new user.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.endtoend.UserManagement -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7"</pre>

  <h3>MeteringAndBilling</h3>
    This end to end sample will display some billing and metering details for the specified VM and
    VDC IDs provided. Using the ListVdcAndVmIds helper will list the VDC and VM ids that can be used
    in this sample. Also, to get the service instance id (sid) execute the ListPlansAndInstances sample.<br/><br/>
    <pre>mvn exec:java -Dexec.mainClass=com.vmware.vchs.api.samples.ondemand.endtoend.MeteringAndBilling -Dexec.args="--username &lt;your account username here&gt; --password &lt;your account password here&gt; --hostname https://vchs.vmware.com --version 5.7 --l2 &lt;the id of L2 (e.g. VDC ID for compute) to retrieve metering data for&gt; --l1 &lt;the id of L1 (e.g. VM ID for compute service) to retrieve metering data for&gt; --serviceGroupId &lt;the service group id to retrieve metering data for&gt; --serviceInstanceId &lt;the service instance id to retrieve metering data for &gt;"</pre>
    

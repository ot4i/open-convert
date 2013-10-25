#WESB-IIB Conversion tool

##Download EGit v1.3
In the Integration Toolkit, select **Help > Install New Software**, and enter the URL http://download.eclipse.org/egit/updates-1.3 in the **Work with** field. Select **Eclipse Git Team Provider**, click **Next**, and select only **Eclipse EGit** from the list of options. Note, the Mylyn option must not be selected.

##Clone the Git repositories and import the projects - detailed instructions
In the Integration Toolkit, import the Git repositories and set up IIB:

* In the Git Repository Exploring perspective, click **Clone a Git repository**.
* In the Git Repositories view, click the **Clone a Git Repository** icon on the toolbar at the top right of the window.
* Clone the Open Technologies For Integration repository by setting the git repository URI to git://OpenTechnologiesForIntegration.github.com/?.git
* Import the project com.ibm.etools.mft.conversion.esb into your workspace (in the Git Repositories view, under org.openintegration.connector.mqtt?, right-click Working Directory and select **Import Projects**). 

##Copyright and license
Copyright 2013 IBM Corp. under the [Eclipse Public license](http://www.eclipse.org/legal/epl-v10.html).

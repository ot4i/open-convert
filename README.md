#WESB-IIB Conversion tool
You can use the WESB-IIB conversion tool to convert existing WebSphere ESB source projects to IBM Integration Bus (IIB) source projects. This repository is an open source development project for developers who require WESB-IIB conversion capabilities to share, re-use and contribute to the WESB-IIB conversion tool.

If you would like to contribute, please email chenz@ca.ibm.com.

##Dependencies
Install [IBM Integration Bus Developer Edition](http://www.ibm.com/software/products/us/en/integration-bus/).

To avoid having to manually import the projects into the Integration Toolkit, install the EGit client. A specific version of the client is needed, see [additional instructions](INSTRUCTIONS.md).

##Setup
1. Clone the Git repositories and import the projects (see [additional instructions](INSTRUCTIONS.md) if you need more detailed instructions):
  * Clone and import this repository (URI: git://OpenTechnologiesForIntegration.github.com/?.git).
 
2. Launch or Debug an Eclipse Application via Run | Run (or Debug) Configurations, and choose Eclipse Application.

##Platforms
Tested on Windows 7 Pro N x64 with [IIB Developer Edition](http://www.ibm.com/software/products/us/en/integration-bus/).

##Contributing
We welcome your feedback on this new development. Download the sample, install, and have a play. If you would like to contribute, please use the contact details on the [organisation homepage](https://github.com/OpenTechnologiesForIntegration).

There are 2 types of contribution:
  * Contribute to mediation primitive converter or binding converter. You can add your extension to project com.ibm.etools.mft.conversion.esb or you can create your own plug-in project and add the extension there.
  * Other contributions include, but are not limited to, assembly diagram topology conversion, data object / path conversion and so on. You can contribute to project com.ibm.etools.mft.conversion.esb.


##Authors
The WESB-IIB conversion tool is a creation of the IBM Integration Bus open integration team.


##Copyright and license
Copyright 2013 IBM Corp. under the [Eclipse Public license](http://www.eclipse.org/legal/epl-v10.html).



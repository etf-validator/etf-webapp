# Overview

This manual is intended for testers seeking information on the execution of tests.


The ETF web interface enables the user to start, control, monitor tests and of course, to inspect the test results. These task areas are reflected in the ETF menu structure.

![screenshot1](https://cloud.githubusercontent.com/assets/13570741/24769663/92baa548-1b07-11e7-9861-d856bd67f39d.png)

In the **Start test** section all available (installed) Executable Test Suites are listed. Within this section, an Executable Test Suite can be selected and run against a Test Object. A Test Object can be a single or multiple artifacts like GML files or a whole system like a Web Feature Service.

The **Status** section shows all tests that are currently executed on the system and enables the user to open a monitor view for single test runs.

The **Test reports** section list the test results of completed test runs ordered by their start date.

The **Help** link refers to this page.

## Starting tests
The landing view shows the available _Executable Test Suites.

![screenshot2](https://cloud.githubusercontent.com/assets/13570741/24769794/0ab17ed2-1b08-11e7-98af-7dfd4f7224ef.png)

Additional information about a Test Suite can be shown by clicking on the plus button.

![screenshot3](https://cloud.githubusercontent.com/assets/13570741/24769921/76a97770-1b08-11e7-8052-035f7e2ea9bd.png)

These information include:

* a description of the Test Suite
* may include a link to the Abstract Test Suite from which the Executable Test Suite has been derived (Source)
* may include Test Suite dependencies which are automatically executed with the Test Suite in a Test Run (Pre-requisite conformance classes)
* may include the name of associated Tags that are used to group the Test Suites in the view
* the name of applicable Test Object Types (explained in the next section)
* general information like the version, author and last editor, creation and change dates.  


To start a Test Run, a Test Suite must be selected with a click on the **use** flip switch on the right-hand side.

![screenshot4](https://cloud.githubusercontent.com/assets/13570741/24769985/b0ee7872-1b08-11e7-9d5e-bdd374596be6.png)

A **Start** button appears once at least one Test Suite is selected.

A Test Suite is applicable to certain Test Object Types, that are listed in the description. Multiple Test Suites can be selected for one Test Run, but must be applicable to the same Test Object Type. Once one Test Suite is selected, the flip switch of all Test Suites, that have different Test Object Types, are disabled.

![screenshot5](https://cloud.githubusercontent.com/assets/13570741/24770148/44831192-1b09-11e7-9727-072fe116381d.png)

A Test Suite may depend on other Test Suites. The dependencies are also shown in the description of the Test Suites. These dependencies are also automatically executed during the test run.

A click on the *Start* button will open a new view that asks the user about the target to be tested.

![screenshot6](https://cloud.githubusercontent.com/assets/13570741/24771135/bd70a15c-1b0c-11e7-88ec-24dbcc1e7d45.png)

The _Label_ field is mandatory but automatically  preset with the current time and names of the selected Test Suites. The Label will be shown in the _Test reports_ overview and can be changed in order to help finding the report again after a test run.

The style of the view may depend on the selected Test Suites.

<u>File-based Tests</u>

The following elements are shown when Test Suites have been selected that test one or multiple test data files.

If *File upload* is selected as *Data source* one or multiple local files can be selected and uploaded to the Validator. The Validator only accepts files with XML and GML file ending and ZIP files containing these two file types. All other files like schema files can not be used and are silently ignored by the Validator!

![screenshot7](https://cloud.githubusercontent.com/assets/13570741/24774770/759dc7de-1b1a-11e7-98af-c9deff4054d4.png)

The maximum uploadable file size is displayed when the mouse is over the question mark with the blue background.

If the data are available on the web they can be tested by providing one single URL. After *Remote file (URL)* has been selected as *Data source*, an URL to either one single XML, GML or a ZIP file can be entered.

![screenshot8](https://cloud.githubusercontent.com/assets/13570741/24774946/34a566c8-1b1b-11e7-85ca-fe04628e6897.png)

If the URL requires authentication, username and password can be provided by clicking on _Credentials_.

![screenshot9](https://cloud.githubusercontent.com/assets/13570741/24775066/af8c75de-1b1b-11e7-96ae-b5989e9702e7.png)

<u>Service Tests</u>

The following elements are shown when Test Suites have been selected that test one service.

The URL of a service must be entered beginning with http:// or https:// .

![screenshot10](https://cloud.githubusercontent.com/assets/13570741/24775449/405f7268-1b1d-11e7-8ed9-28b364b4e339.png)

If the service requires authentication, username and password can be provided by clicking on _Credentials_.


The _Test Suites_ button shows some basic information about the selected Test Suites and -if applicable- about the direct dependencies.

![screenshot11](https://cloud.githubusercontent.com/assets/13570741/24775122/dbe5741e-1b1b-11e7-858d-ac453b36f97e.png)


If the Test accepts parameters, they are shown in the Test Suite Parameters section. Optional parameters can be displayed by clicking on the _Optional Parameters_ button. A description of the parameters is displayed when the mouse is over the question mark. In most cases the preset default values can be used.

![screenshot12](https://cloud.githubusercontent.com/assets/13570741/24775199/180a8d94-1b1c-11e7-85d1-a591df928738.png)

Finally the test can be started by clicking on the **Start** button. The view then changes  automatically to the _Monitor View_.

## Monitor test runs

After a Test Run has been started the Monitor View is shown.

![screenshot13](https://cloud.githubusercontent.com/assets/13570741/24776001/6e32c3dc-1b1f-11e7-95e7-d37ec54f2d74.png)

The blue bar indicates the progress.

![screenshot14](https://cloud.githubusercontent.com/assets/13570741/24776030/8044c64c-1b1f-11e7-96ee-bf7c3c7a38fa.png)

The console area shows information and result messages. The Test Run can be canceled with a click on the *Cancel* button.

The view can be left for instance with the X Button in the upper left corner. The Test Run execution continues on the server.

To reattach to a Monitor View select in the menu bar the _Status_ view. The Status view shows all running tests. A click on the Test Run opens the Monitor View of that Test Run again.

![screenshot15](https://cloud.githubusercontent.com/assets/13570741/24776115/d22463b4-1b1f-11e7-9ad0-262ecbb5aecc.png)

When a Test Run finshes and the Monitor View is opened, the Test Report is displayed automatically.

## Inspect test reports

The top of a Test Report shows general information including the overall test result *Status*, the start time, the duration and a statistical table, which summarizes the status of all tests on several levels.

![screenshot16](https://cloud.githubusercontent.com/assets/13570741/24777508/fa19217e-1b25-11e7-8bda-cfede748804b.png)

The Test Reports are interactive. The *Show* switch can be used to filter *Only failed* or *Only manual* tests.
The *Level of detail* switch is used to show additional information in the reports.

![screenshot17](https://cloud.githubusercontent.com/assets/13570741/24777824/9b754a4c-1b27-11e7-9a0f-7dad61f6e251.png)

The test results are summarized hierarchically in a report. At the top level there are the Test Suites.

By clicking on one test suite a description and all lower level tests in that test suite are shown. Failures in a test suite can be immediately recognized by the red color. The number of failed tests is shown in the right corner.

![Screenshot17_1](https://cloud.githubusercontent.com/assets/13570741/24778385/8f6f73dc-1b2a-11e7-8e86-f32a05de0517.png)

The green color indicates a passed test. A passed test which requires further manual test steps that could not be automated are colored orange. The orange color may also indicate a test that has been skipped as it depends on another test that have been failed. The exact status can be found below the description.

The number of levels depends on the tested Test Object. If service tests have been executed the hierarchy is as follows:

* Executable Test Suites
* Test Modules (bundles Test Cases)
* Test Cases (bundles Test Steps)
* Test Steps (interactions with the service, bundles Test Assertions)
* Test Assertions (atomar tests)

In a file-based test, Test Modules and Test Steps do not exist and are not shown in the report.

Each test provides a description on how aspects are tested and lists the requirements. The test may possess a link to an abstract test suite, from which the test has been derived (Source).

![Screenshot17_2](https://cloud.githubusercontent.com/assets/13570741/24778468/0ad773e4-1b2b-11e7-8368-a7b1735853be.png)

Assertions stand for atomar test queries on the lowest level. Failed, red colored assertions display rrror messages in the *Messages* section.

![Screenshot18](https://cloud.githubusercontent.com/assets/13570741/24777997/7e6fae96-1b28-11e7-80b1-cc3e89dbaa15.png)

Helpful information may also be found on the next higher level, like for instance the response from a service  on the Test Step level (note the _Open saved response_ link in the report).

![Screenshot19](https://cloud.githubusercontent.com/assets/13570741/24778272/ff0df296-1b29-11e7-8eb3-4dcfea24f195.png)


## Test Reports

The Test Reports view shows all reports that have been generated from Test Runs.

![screenshot_X](https://cloud.githubusercontent.com/assets/13570741/24776748/781f80d0-1b22-11e7-8386-107febbb9c3e.png)

By clicking on the plus button information, about the Start time, the test result status, the name of the Test Object and the used Test Suites are shown.

A Test Report can be opened again by clicking on **Open report** or can be downloaded as HTML file by clicking on the **Download** button.

The log file of the test run can be inspected with the **Open log** button. By clicking on **Delete report** button, the report will be deleted permanently.

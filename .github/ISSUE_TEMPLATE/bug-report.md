---
name: Bug report
about: Create an issue report to help us improve ETF

---

!!!
!!! PLEASE READ THE TEXT BELOW !!!
!!!

### Correct repository?

This repository is intended for issue reports specific to the ETF validator software.

If you have an issue with an Executable Test Suite, then please use the
corresponding repository, for instance the [INSPIRE repository](https://github.com/inspire-eu-validation/community/blob/master/README.md)
if you have an issue with an INSPIRE test.

If you are using an instance of the validator from a service provider and your problem is solely that the web interface is not loading or the service is unavailable, then contact the provider directly. This include the following HTTP error codes:
- 403 - Forbidden
- 404 - Not Found
- 413 - Request Entity Too Large
- 502 - Bad Gateway
- 503 - Service Unavailable
- 504 - Gateway Timeout
- 507 - Insufficient Storage
- 509 - Bandwidth Limit Exceeded


Our User, Administrator and Developer
manuals are available here: http://docs.etf-validator.net

### Prerequisites for a bug report

* [ ] Set a clear and descriptive title
* [ ] Can you reproduce the problem
* [ ] Did you [use the Github issue search](https://github.com/issues?utf8=✓&q=is%3Aissue+user%3Aetf-validator) to check whether your bug has already been reported?
* [ ] Upload excerpts of the etf.log, catalina.out/log and localhost-<date>.log files. Important parts are:
the initialization log entries beginning with the ETF ASCII logo and ending with "TestRunController initialized!" plus the last exception messages.
* [ ] If the problem is related to the Web user interface: include screenshots
* [ ] If the problem is related to specific test data, provide the URL of your
service / the file you have uploaded or referenced.
If your service is protected or you have confidential data that you cannot
upload here, you can request an email address to which you can send URLs / files.
Please note that issues cannot be fixed for services/datasets that we cannot
access.
* [ ] Don't forget to remove sensitive information from uploaded files!!!

### Description

[Description of the bug]

### Operating systems and browser

- If the issue occurs in the user interface: add information about the Client operating system, the Browser name and version
- Otherwise add the etf.log and tomcat log files. If you can't: add information about the operating system on which the ETF web application is running and information about the web application server

### Steps to Reproduce

[Describe the exact steps which can be used to reproduce the problem]

1. [First Step]
2. [Second Step]
3. [and so on...]

**Expected behavior:** [Which behavior you expected to see instead and why.]

**Actual behavior:** [Describe the behavior you observed after following the steps]

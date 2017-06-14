#/bin/bash

# Prerequisite: gem install asciidoctor && gem install pygments.rb

asciidoctor \
index.adoc \
User_manuals/Simplified_workflows.adoc \
Admin_manuals/index.adoc \
Developer_manuals/WEB-API.adoc \
Developer_manuals/Building.adoc \
Developer_manuals/Developing_Executable_Test_Suites.adoc

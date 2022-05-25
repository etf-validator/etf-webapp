import module namespace etfxdb = "http://interactive_instruments.de/etf/etfxdb";

declare default element namespace "http://www.interactive-instruments.de/etf/2.0";
declare namespace etf = "http://www.interactive-instruments.de/etf/2.0";
declare namespace xs = 'http://www.w3.org/2001/XMLSchema';

declare variable $function external;

declare variable $qids external := "";

declare variable $offset external := 0;
declare variable $limit external := 0;
declare variable $levelOfDetail external := 'SIMPLE';
declare variable $fields external := '*';

declare function local:get-testruntemplates($offset as xs:integer, $limit as xs:integer, $fields as xs:string) {
        <DsResultSet
            xmlns="http://www.interactive-instruments.de/etf/2.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
            xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <testRunTemplates>
                {etfxdb:get-all(db:open('b')/etf:TestRunTemplate, $offset, $limit, $fields)}
            </testRunTemplates>
        </DsResultSet>
};

declare function local:get-testruntemplate($ids as xs:string*, $fields as xs:string) {
    let $testRunTemplateDb := db:open('b')/etf:TestRunTemplate
    let $testObjectsDb := db:open('o')/etf:TestObject
    let $executableTestSuiteDb := db:open('b')/etf:ExecutableTestSuite
    let $translationTemplateBundleDb := db:open('b')/etf:TranslationTemplateBundle

    let $testRunTemplate := $testRunTemplateDb[@id = $ids]
    let $executableTestSuites := etfxdb:get-executableTestSuites($executableTestSuiteDb, $levelOfDetail, $testRunTemplate/etf:executableTestSuites)
    let $testObjects := etfxdb:get-testObjects($testObjectsDb, $levelOfDetail, $testRunTemplate/etf:testObjects)

    return
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            {if ($fields = '*') then
                (
                    element executableTestSuites {
                        $executableTestSuites
                    },
                    element testObjects {
                        $testObjects
                    },
                    element translationTemplateBundles {
                        etfxdb:get-translationTemplateBundles($translationTemplateBundleDb, $levelOfDetail, $executableTestSuites)
                    },
                    element testRunTemplates {
                        $testRunTemplate
                    }
                )
            else
                element testRunTemplates {
                    etfxdb:filter-fields($testRunTemplate, $fields)}
                }
        </DsResultSet>
};

if ($function = 'byId')
then
    local:get-testruntemplate($qids, $fields)
else
    local:get-testruntemplates($offset, $limit, $fields)

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

declare function local:get-testTaskResults($offset as xs:integer, $limit as xs:integer) {
    <DsResultSet
    xmlns="http://www.interactive-instruments.de/etf/2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
    xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
        <testTaskResults>
            {etfxdb:get-all(db:open('etf-ds')/etf:TestTaskResult, $levelOfDetail, $offset, $limit, $fields)}
        </testTaskResults>
    </DsResultSet>
};


declare function local:get-testTaskResult($ids as xs:string*) {
    let $testRunDbNames := db:list()[starts-with(., "r-")]
    let $testObjectsDb := (db:open('o'), $testRunDbNames ! db:open(.))/etf:TestObject

    let $testObjectTypesDb := db:open('b')/etf:TestObjectType
    let $executableTestSuiteDb := db:open('b')/etf:ExecutableTestSuite
    let $translationTemplateBundleDb := db:open('b')/etf:TranslationTemplateBundle
    let $testTaskResultsDb := $testRunDbNames ! db:open(.)/etf:TestTaskResult

    let $testTaskResult := $testTaskResultsDb[@id = $ids]
    let $executableTestSuite := etfxdb:get-executableTestSuites($executableTestSuiteDb, $levelOfDetail, $testTaskResult)
    let $testObjects := etfxdb:get-testObjects($testObjectsDb, $levelOfDetail, $testTaskResult)

    return
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <executableTestSuites>
                {$executableTestSuite}
            </executableTestSuites>
            <testObjects>
                {$testObjects}
            </testObjects>
            <testObjectTypes>
                {etfxdb:get-testObjectTypes($testObjectTypesDb, $levelOfDetail, ($executableTestSuite/etf:supportedTestObjectTypes, $executableTestSuite/etf:consumableResultObjectTypes, $testObjects/etf:testObjectType))}
            </testObjectTypes>
            <translationTemplateBundles>
                {etfxdb:get-translationTemplateBundles($translationTemplateBundleDb, $levelOfDetail, $executableTestSuite)}
            </translationTemplateBundles>
            <testTaskResults>
                {$testTaskResult}
            </testTaskResults>
        </DsResultSet>
};

if ($function = 'byId')
then
    local:get-testTaskResult($qids)
else
    local:get-testTaskResults($offset, $limit)

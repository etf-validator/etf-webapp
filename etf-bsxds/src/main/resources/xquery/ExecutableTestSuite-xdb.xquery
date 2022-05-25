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

declare function local:get-executableTestSuites($offset as xs:integer, $limit as xs:integer, $fields as xs:string*) {
      <DsResultSet
      xmlns="http://www.interactive-instruments.de/etf/2.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
      xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
          <executableTestSuites>
              {etfxdb:get-all(db:open('b')/etf:ExecutableTestSuite, $levelOfDetail, $offset, $limit, $fields)}
          </executableTestSuites>
      </DsResultSet>
};


declare function local:get-executableTestSuite($ids as xs:string*) {
    let $executableTestSuiteDb := db:open('b')/etf:ExecutableTestSuite
    let $testObjectTypesDb := db:open('b')/etf:TestObjectType
    let $translationTemplateBundleDb := db:open('b')/etf:TranslationTemplateBundle

    let $executableTestSuite := $executableTestSuiteDb[@id = $ids]

    return
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <executableTestSuites>
                {$executableTestSuite}{etfxdb:get-replacedByRec($executableTestSuiteDb, $levelOfDetail, $executableTestSuite)}
            </executableTestSuites>
            <testObjectTypes>
                {etfxdb:get-testObjectTypes($testObjectTypesDb, $levelOfDetail, $executableTestSuite/etf:supportedTestObjectTypes)}
                {etfxdb:get-testObjectTypes($testObjectTypesDb, $levelOfDetail, $executableTestSuite/etf:consumableResultObjectTypes)}
            </testObjectTypes>
            <translationTemplateBundles>
                {etfxdb:get-translationTemplateBundles($translationTemplateBundleDb, $levelOfDetail, $executableTestSuite)}
            </translationTemplateBundles>
        </DsResultSet>
};

if ($function = 'byId')
then
    local:get-executableTestSuite($qids)
else
    local:get-executableTestSuites($offset, $limit, $fields)

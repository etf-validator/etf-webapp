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

declare function local:get-testobjects($offset as xs:integer, $limit as xs:integer) {
    <DsResultSet
    xmlns="http://www.interactive-instruments.de/etf/2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
    xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
        <testObjects>
            {etfxdb:get-all(db:open('o')/etf:TestObject, $levelOfDetail, $offset, $limit, $fields)}
        </testObjects>
    </DsResultSet>
};


declare function local:get-testobject($ids as xs:string*) {
    let $testRunDbNames := db:list()[starts-with(., "r-")]
    let $testObjectsDb := (db:open('o'), $testRunDbNames ! db:open(.))/etf:TestObject
    let $testObject := for $id in $ids return ($testObjectsDb[@id = $id])[1]
    let $testObjectTypesDb := db:open('b')/etf:TestObjectType

    return
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <testObjects>
                {$testObject}{etfxdb:get-replacedByRec($testObjectsDb, $levelOfDetail, $testObject)}
            </testObjects>
            <testObjectTypes>
                {etfxdb:get-testObjectTypes($testObjectTypesDb, $levelOfDetail, $testObject/etf:testObjectTypes[1])}
            </testObjectTypes>
        </DsResultSet>
};

if ($function = 'byId')
then
    local:get-testobject($qids)
else
    local:get-testobjects($offset, $limit)

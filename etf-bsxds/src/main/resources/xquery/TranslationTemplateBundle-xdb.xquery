import module namespace etfxdb = "http://interactive_instruments.de/etf/etfxdb";

declare default element namespace "http://www.interactive-instruments.de/etf/2.0";
declare namespace etf = "http://www.interactive-instruments.de/etf/2.0";
declare namespace xs = 'http://www.w3.org/2001/XMLSchema';

declare variable $function external;

declare variable $qids external := "";

declare variable $offset external := 0;
declare variable $limit external := 0;

declare function local:get-translationtemplatebundles($offset as xs:integer, $limit as xs:integer) {
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <translationTemplateBundles>
                {etfxdb:get-all(db:open('b')/etf:TranslationTemplateBundle, $offset, $limit, '*')}
            </translationTemplateBundles>
        </DsResultSet>
};

declare function local:get-translationtemplatebundle($ids as xs:string*) {
    let $translationTemplateBundles := db:open('b')/etf:TranslationTemplateBundle[@id = $ids]
    return
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <translationTemplateBundles>
                {$translationTemplateBundles}
            </translationTemplateBundles>
        </DsResultSet>
};

if ($function = 'byId')
then
    local:get-translationtemplatebundle($qids)
else
    local:get-translationtemplatebundles($offset, $limit)

(:~
 : ----------------------------------------------------------------
 : ETF XQuery data storage Function Library
 : ----------------------------------------------------------------
 :)
module namespace  etfxdb = "http://interactive_instruments.de/etf/etfxdb" ;

declare default element namespace "http://www.interactive-instruments.de/etf/2.0";
declare namespace etf = "http://www.interactive-instruments.de/etf/2.0";


(:~
 : ----------------------------------------------------------------
 : get-replacedByRec
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-replacedByRec($dbs as node()*, $levelOfDetail as xs:string, $item as node()*) {
    if ($levelOfDetail = 'HISTORY')
    then
        etfxdb:get-replacedByRec($dbs, $item)
    else
        ()
};

declare function etfxdb:get-replacedByRec($dbs as node()*, $item as node()*) {
    let $replacedBy := $dbs[@id = $item/etf:replacedBy[1]/@ref]
    return
        if (empty($replacedBy))
        then
            ()
        else
            ($replacedBy, etfxdb:get-replacedByRec($dbs, $replacedBy))
};

(:~
 : ----------------------------------------------------------------
 : get-all
 :
 : 'disabled' items are filtered
 : ----------------------------------------------------------------
 : TODO implement abstract filter as higher order function http://docs.basex.org/wiki/Higher-Order_Functions
 :)
declare function etfxdb:get-all($items as node()*, $levelOfDetail as xs:string, $offset as xs:integer, $limit as xs:integer, $fields as xs:string) {
    (
        for $item in $items
        where
            ($levelOfDetail = 'HISTORY' or not(exists($item/etf:replacedBy[1]))) and not($item/etf:disabled = 'true')
        order by $item/etf:label/text() ascending
        return
            etfxdb:filter-fields($item, $fields)
     )[position() > $offset and position() <= $offset + $limit]
};

declare function etfxdb:get-all($items as node()*, $offset as xs:integer, $limit as xs:integer, $fields as xs:string) {
    (
        for $item in $items
        where
            not($item/etf:disabled = 'true')
        order by $item/etf:label/text() ascending
         return
            etfxdb:filter-fields($item, $fields)
    )[position() > $offset and position() <= $offset + $limit]
};

declare function etfxdb:filter-fields($item as node(), $fields as xs:string) {
        if ($fields = '*') then
            $item
        else
            element {
                node-name($item)
            }{
                $item/@*,
                $item/*[contains($fields, local-name())]
            }
};

(:~
 : ----------------------------------------------------------------
 : get-parent
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-parentRec($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    let $parent := $dbs[@id = $items/etf:parent[1]/@ref]
    return
    (: todo :)
        if (empty($parent))
        then
            ()
        else
            ($parent, etfxdb:get-parentRec($dbs, $parent))
};

declare function etfxdb:get-parentRec($dbs as node()*, $items as node()*) {
    let $parent := $dbs[@id = $items/etf:parent[1]/@ref]
    return
    (: todo :)
        if (empty($parent))
        then
            ()
        else
            ($parent, etfxdb:get-parentRec($dbs, $parent))
};

(:~
 : ----------------------------------------------------------------
 : get-testItemTypes
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testItemTypes($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        $dbs[@id = $items/etf:type[1]/@ref]
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-testObjects
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testObjects($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        $dbs[@id = $items/etf:testObject[1]/@ref]
    else
        ()
};


(:~
 : ----------------------------------------------------------------
 : get-testObjectTypes
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testObjectTypes($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        let $testObjectTypes := $dbs[@id = $items/etf:testObjectType[1]/@ref]
        return
            if (empty($testObjectTypes))
            then
                ()
            else
                for $t in ($testObjectTypes, etfxdb:get-testObjectTypesRec($dbs, $testObjectTypes))
                group by $g := $t/@id
                return $t[1]
    else
        ()
};

declare function etfxdb:get-testObjectTypesRec($dbs as node()*, $testObjectTypes as node()*) {
    let $testObjectTypes := $dbs[@id = $testObjectTypes/etf:subTypes[1]/etf:testObjectType/@ref and @id != $testObjectTypes/@id]
    return
        if (empty($testObjectTypes))
        then
            ()
        else
            ($testObjectTypes, etfxdb:get-testObjectTypesRec($dbs, $testObjectTypes))
};

(:~
 : ----------------------------------------------------------------
 : get-executableTestSuites
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-executableTestSuites($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        $dbs[@id = $items/etf:resultedFrom[1]/@ref or @id = $items/etf:executableTestSuite[1]/@ref]
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-testTaskResults
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-testTaskResults($dbs as node()*, $levelOfDetail as xs:string, $items as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        $dbs[@id = $items/etf:testTaskResult[1]/@ref]
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-tags
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-tags($dbs as node()*, $levelOfDetail as xs:string, $item as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        let $tags := $dbs[@id = $item/etf:tags[1]/etf:tag/@ref]
        return
            if (empty($tags))
            then
                ()
            else
                ($tags, etfxdb:get-replacedByRec($dbs, $tags))
    else
        ()
};

(:~
 : ----------------------------------------------------------------
 : get-translationTemplateBundels
 : ----------------------------------------------------------------
 :)
declare function etfxdb:get-translationTemplateBundles($dbs as node()*, $levelOfDetail as xs:string, $item as node()*) {
    if ($levelOfDetail = 'DETAILED_WITHOUT_HISTORY')
    then
        let $translationTemplateBundles := $dbs[@id = $item/etf:translationTemplateBundle[1]/@ref]
        return
            if (empty($translationTemplateBundles))
            then
                ()
            else
                ($translationTemplateBundles, etfxdb:get-parentRec($dbs, $translationTemplateBundles))
    else
        ()
};


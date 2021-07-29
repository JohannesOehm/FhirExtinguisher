import {VmColumn, VmSubColumn} from "../ui-types";
import {
    Column,
    ExplodeLong,
    ExplodeWide,
    Join,
    ListProcessingMode,
    parseColumns,
    stringifyColumns
} from "columns-parser";


export function columnsToString(columns: VmColumn[]): string {
    return stringifyColumns(columns.map((it: VmColumn) => convertFromVmColumn(it)));
}

export function columnsFromString(value: string): VmColumn[] {
    return parseColumns(value).map((it: Column) => convertToVmColumn(it));
}

export function convertToVmColumn(column: Column): VmColumn {
    let subColumns: VmSubColumn[];
    let type = column.type.toString();
    if (column.type instanceof ExplodeLong || column.type instanceof ExplodeWide) {
        subColumns = column.type.subcolumns.map((it: Column) => ({name: it.name, expression: it.expression}));
        type = "explodeLong";
    }
    if (column.type instanceof ExplodeWide) {
        subColumns.unshift({name: "$disc", expression: column.type.discriminator});
        type = "explodeWide";
    }
    return {
        name: column.name,
        expression: column.expression,
        type: type,
        subColumns: subColumns
    }
}

export function convertFromVmColumn(vmColumn: VmColumn): Column {
    let type: ListProcessingMode;
    if (vmColumn.type.toString().startsWith("join")) {
        let sep = /join\(\s*"([^"]*)"\s*\)/.exec(vmColumn.type)[1]
        type = new Join(sep);
    } else if (vmColumn.type.startsWith("explodeLong")) {
        type = new ExplodeLong(vmColumn.subColumns.map((it: VmSubColumn) => new Column(it.name, it.expression, null)))
    } else if (vmColumn.type.startsWith("explodeWide")) {
        let disc = vmColumn.subColumns.filter((it: VmSubColumn) => it.name === "$disc")[0].expression;
        type = new ExplodeWide(disc, vmColumn.subColumns.filter((it: VmSubColumn) => it.name !== "$disc").map(it => new Column(it.name, it.expression, null)));
    }
    return new Column(vmColumn.name, vmColumn.expression, type)
}

export function getUrlParams(search: string): Map<string, string> {
    const hashes = search.slice(search.indexOf('?') + 1).split('&');
    const params: Map<string, string> = new Map();
    for (let hash of hashes) {
        let idx = hash.indexOf('=');
        const key = hash.substring(0, idx);
        const val = hash.substring(idx + 1);
        params.set(key, val);
    }
    return params;
}


type ParsedUrl = { limit: number, url: string, columns: Column[] };

export function parseLink(link: string): ParsedUrl {
    let urlToParse: string;
    if (link.indexOf("fhir/") != -1) {
        urlToParse = link.substring(link.indexOf("fhir/") + "fhir/".length);
    } else {
        urlToParse = link;
    }

    let urlParams = getUrlParams(urlToParse);

    let limit = parseInt(urlParams.get("__limit"));

    // let columns = new ColumnsParser().parseColumns(decodeURIComponent(urlParams.get("__columns")));
    let columns = parseColumns(decodeURIComponent(urlParams.get("__columns")));

    let url = urlToParse.split("?")[0];
    let query = [...urlParams.entries()] //TODO: Improve this somehow
        .filter(it => it[0] !== "__columns" && it[0] !== "__limit")
        .map(it => it[0] + "=" + it[1])
        .join("&");
    return {url: `${url}?${query}`, columns, limit}
}

export function getResourceName(fhirsearch: string): string[] | null {
    let resourceName: string;
    if (fhirsearch.indexOf("/") !== -1) {
        resourceName = fhirsearch.substring(0, fhirsearch.indexOf("/"));
    } else if (fhirsearch.indexOf("?") !== -1) {
        resourceName = fhirsearch.substring(0, fhirsearch.indexOf("?"));
    }

    if (!resourceName) {
        let idxOfQ = fhirsearch.indexOf("?");
        if (idxOfQ !== -1) {
            let urlSearchParams = new URLSearchParams(fhirsearch.substring(idxOfQ + 1));
            if (urlSearchParams.has("_type")) {
                return urlSearchParams.get("_type").split(",");
            }
        }
        return [fhirsearch];
    } else {
        return [resourceName];
    }

}
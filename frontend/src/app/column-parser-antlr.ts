import {ANTLRInputStream, CommonTokenStream} from 'antlr4ts';
import {ColumnsContext, ColumnsTokens} from "../antlr/ColumnsTokens";
import {Column, SubColumn} from "./index";
import {ColumnsLexer} from "../antlr/ColumnsLexer";


export class ColumnsParser {
    parseColumns(string: string): Column[] {
        let inputStream = new ANTLRInputStream(string);
        let lexer = new ColumnsLexer(inputStream);
        let tokenStream = new CommonTokenStream(lexer);
        let parser = new ColumnsTokens(tokenStream);
        let columns = this.parseColumn(parser.columns());

        return columns;
    }

    private parseColumn(columnsContext: ColumnsContext): Column[] {
        let results: Column[] = [];
        for (let columnContext of columnsContext.column()) {
            let columnName = columnContext.columnName().text.replace(/\\:/g, ":").replace(/\\@/g, "@")
            let type;
            let subcolumns: SubColumn[] = undefined;
            if (columnContext.columnType() != null) {
                type = columnContext.columnType().typeName().text.replace(/\\:/g, ":")
                if (type === "explodeWide" || type === "explodeLong") {
                    let columnsStr = ColumnsParser.split(columnContext.columnType().typeParam().text.replace("\\)", ")"))
                    subcolumns = [];
                    for (let columnStr in columnsStr) {
                        let [name, expression] = ColumnsParser.split(columnStr, ":")
                        subcolumns.push({
                            name: this.unescape(name),
                            expression: this.unescape(expression)
                        });
                    }

                }
            } else {
                type = 'join(" ")'
            }
            let expression = columnContext.fhirpathExpression().text.replace(",", "\\,")
            results.push({name: columnName, type: type, expression: expression, subColumns: subcolumns})
        }
        return results;
    }


    private unescape(name: string): string {
        return name.replace(/\\,/g, ",").replace(/\\:/g, ":");
    }

    private static split(toSplit: string, delimiter: string = ',', escape: string = '\\'): string[] {
        let result: string[] = []
        let lastStart = 0
        let escapeChar = false
        for (let i = 0; i < toSplit.length; i++) {
            let c = toSplit[i];
            if (c == escape) {
                escapeChar = true;
            } else {
                if (!escapeChar && c == delimiter) {
                    result.push(toSplit.substring(lastStart, i).replace(escape + delimiter, delimiter));
                    lastStart = i + 1;
                }
                if (i == toSplit.length - 1) {
                    result.push(toSplit.substring(lastStart).replace(escape + delimiter, delimiter));
                }
                escapeChar = false;
            }
        }
        return result;
    }

}




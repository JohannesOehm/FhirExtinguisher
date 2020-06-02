import {
    ANTLRInputStream,
    CommonTokenStream,
    DiagnosticErrorListener, ParserRuleContext,
    ProxyParserErrorListener, Token
} from 'antlr4ts';
import {ColumnsContext, ColumnsTokens} from "../antlr/ColumnsTokens";
import {Column} from "./index";
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
            let columnName = columnContext.columnName().text.replace("\\:", ":").replace("\\@", "@")
            let type;
            if (columnContext.columnType() != null) {
                type = columnContext.columnType().text.replace(":", "\\:")
            } else {
                type = 'join(" ")'
            }
            let expression = columnContext.fhirpathExpression().text.replace(",", "\\,")
            results.push({name: columnName, type: type, expression: expression})
        }
        return results;
    }

}




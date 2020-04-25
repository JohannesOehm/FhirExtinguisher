import {
    ANTLRInputStream,
    CommonTokenStream,
    DiagnosticErrorListener, ParserRuleContext,
    ProxyParserErrorListener, RecognitionException,
    Recognizer, RuleContext,
    Token
} from 'antlr4ts';
import {QueryLexer} from "../antlr/QueryLexer";
import {languages} from "monaco-editor";
import IState = languages.IState;
import ILineTokens = languages.ILineTokens;
import IToken = languages.IToken;
import TokensProvider = languages.TokensProvider;
import EOF = Token.EOF;
import {ParserErrorListener} from "antlr4ts/ParserErrorListener";
import {AbstractParseTreeVisitor, ErrorNode, ParseTree, RuleNode, TerminalNode} from "antlr4ts/tree";
import {QueryTokens} from "../antlr/QueryTokens";


export class UrlState implements IState {
    clone(): IState {
        return new UrlState();
    }

    equals(other: IState): boolean {
        return true;
    }
}

class UrlLineTokens implements ILineTokens {
    endState: IState;
    tokens: IToken[];

    constructor(tokens: IToken[]) {
        this.endState = new UrlState();
        this.tokens = tokens;
    }
}

export class UrlTokensProvider implements TokensProvider {
    getInitialState(): IState {
        return new UrlState();
    }

    tokenize(line: string, state: IState): ILineTokens {
        // So far we ignore the state, which is not great for performance reasons
        let foo = tokensForLine(line);
        console.log(foo);
        return foo;

        // let inputStream = new ANTLRInputStream(line);
        // let lexer = new QueryLexer(inputStream);
        // let tokenStream = new CommonTokenStream(lexer);
        // let parser = new QueryTokens(tokenStream);
        // let tokens = this.recurseParseTree(parser.url(), " ");
        // console.log("advanced", tokens);

        // return new UrlLineTokens(tokens);
    }

    // recurseParseTree(item: ParseTree, prefix: string, lastRule: number = 0): IToken[] {
    //     if (item instanceof RuleNode) {
    //         var nodes = [];
    //         let ruleIndex = (<RuleNode>item).ruleContext.ruleIndex;
    //         for (let i = 0; i < item.childCount; i++) {
    //             let ruleIdx2 = ruleIndex === QueryLexer.RULE_string ? lastRule : ruleIndex;
    //             nodes.push(...this.recurseParseTree(item.getChild(i), " " + prefix, ruleIdx2));
    //         }
    //         return nodes;
    //     } else {
    //         let token = (<TerminalNode>item).symbol;
    //         let designation;
    //         if (token.type === QueryLexer.STRING) {
    //             designation = QueryLexer.ruleNames[lastRule];
    //         } else {
    //             designation = QueryLexer.VOCABULARY.getSymbolicName(token.type);
    //         }
    //         return [new UrlToken(designation, token.charPositionInLine)];
    //
    //     }
    //
    // }
}


class UrlToken implements IToken {
    scopes: string;
    startIndex: number;

    constructor(ruleName: String, startIndex: number) {
        this.scopes = ruleName.toLowerCase() + ".url";
        this.startIndex = startIndex;
    }
}


export function tokensForLine(input: string): ILineTokens {
    class ErrorCollectorListener implements ParserErrorListener {
        errors: number[] = [];

        syntaxError<T>(recognizer: Recognizer<T, any>, offendingSymbol: T | undefined, line: number, charPositionInLine: number, msg: string, e: RecognitionException | undefined): void {
            this.errors.push(charPositionInLine);
        }

    }

    let inputStream = new ANTLRInputStream(input);
    let lexer = new QueryLexer(inputStream);
    lexer.removeErrorListeners();
    let errorListener = new ErrorCollectorListener();
    lexer.addErrorListener(errorListener);
    let myTokens: IToken[] = [];
    let token;
    while ((token = lexer.nextToken()) != null) {
        // We exclude EOF
        if (token.type == EOF) {
            break;
        } else {
            let tokenTypeName = lexer.vocabulary.getSymbolicName(token.type);
            let myToken = new UrlToken(tokenTypeName, token.charPositionInLine);
            myTokens.push(myToken);
        }
    }

    // Add all errors
    for (let e of errorListener.errors) {
        myTokens.push(new UrlToken("error.url", e));
    }
    myTokens.sort((a, b) => (a.startIndex > b.startIndex) ? 1 : -1)
    return new UrlLineTokens(myTokens);
}


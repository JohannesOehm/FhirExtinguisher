// Generated from src/antlr/ColumnsTokens.g4 by ANTLR 4.7.3-SNAPSHOT


import {ATN} from "antlr4ts/atn/ATN";
import {ATNDeserializer} from "antlr4ts/atn/ATNDeserializer";
import {FailedPredicateException} from "antlr4ts/FailedPredicateException";
import {NotNull} from "antlr4ts/Decorators";
import {NoViableAltException} from "antlr4ts/NoViableAltException";
import {Override} from "antlr4ts/Decorators";
import {Parser} from "antlr4ts/Parser";
import {ParserRuleContext} from "antlr4ts/ParserRuleContext";
import {ParserATNSimulator} from "antlr4ts/atn/ParserATNSimulator";
import {ParseTreeListener} from "antlr4ts/tree/ParseTreeListener";
import {ParseTreeVisitor} from "antlr4ts/tree/ParseTreeVisitor";
import {RecognitionException} from "antlr4ts/RecognitionException";
import {RuleContext} from "antlr4ts/RuleContext";
//import { RuleVersion } from "antlr4ts/RuleVersion";
import {TerminalNode} from "antlr4ts/tree/TerminalNode";
import {Token} from "antlr4ts/Token";
import {TokenStream} from "antlr4ts/TokenStream";
import {Vocabulary} from "antlr4ts/Vocabulary";
import {VocabularyImpl} from "antlr4ts/VocabularyImpl";

import * as Utils from "antlr4ts/misc/Utils";

import {ColumnsTokensListener} from "./ColumnsTokensListener";
import {ColumnsTokensVisitor} from "./ColumnsTokensVisitor";


export class ColumnsTokens extends Parser {
    public static readonly TYPE_SEPARATOR = 1;
    public static readonly EXPRESSION_SEPARATOR = 2;
    public static readonly COLUMN_STRING = 3;
    public static readonly PAREN_OPEN = 4;
    public static readonly TYPE_STRING = 5;
    public static readonly FHIRPATH_STRING = 6;
    public static readonly COLUMN_SEPARATOR = 7;
    public static readonly PAREN_STRING = 8;
    public static readonly PAREN_CLOSE = 9;
    public static readonly WS = 10;
    public static readonly RULE_columns = 0;
    public static readonly RULE_column = 1;
    public static readonly RULE_columnType = 2;
    public static readonly RULE_columnName = 3;
    public static readonly RULE_typeName = 4;
    public static readonly RULE_typeParam = 5;
    public static readonly RULE_fhirpathExpression = 6;
    // tslint:disable:no-trailing-whitespace
    public static readonly ruleNames: string[] = [
        "columns", "column", "columnType", "columnName", "typeName", "typeParam",
        "fhirpathExpression",
    ];

    private static readonly _LITERAL_NAMES: Array<string | undefined> = [
        undefined, "'@'", "':'", undefined, "'('", undefined, undefined, "','",
        undefined, "')'",
    ];
    private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
        undefined, "TYPE_SEPARATOR", "EXPRESSION_SEPARATOR", "COLUMN_STRING",
        "PAREN_OPEN", "TYPE_STRING", "FHIRPATH_STRING", "COLUMN_SEPARATOR", "PAREN_STRING",
        "PAREN_CLOSE", "WS",
    ];
    public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(ColumnsTokens._LITERAL_NAMES, ColumnsTokens._SYMBOLIC_NAMES, []);

    // @Override
    // @NotNull
    public get vocabulary(): Vocabulary {
        return ColumnsTokens.VOCABULARY;
    }

    // tslint:enable:no-trailing-whitespace

    // @Override
    public get grammarFileName(): string {
        return "ColumnsTokens.g4";
    }

    // @Override
    public get ruleNames(): string[] {
        return ColumnsTokens.ruleNames;
    }

    // @Override
    public get serializedATN(): string {
        return ColumnsTokens._serializedATN;
    }

    constructor(input: TokenStream) {
        super(input);
        this._interp = new ParserATNSimulator(ColumnsTokens._ATN, this);
    }

    // @RuleVersion(0)
    public columns(): ColumnsContext {
        let _localctx: ColumnsContext = new ColumnsContext(this._ctx, this.state);
        this.enterRule(_localctx, 0, ColumnsTokens.RULE_columns);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 14;
                this.column();
                this.state = 19;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                while (_la === ColumnsTokens.COLUMN_SEPARATOR) {
                    {
                        {
                            this.state = 15;
                            this.match(ColumnsTokens.COLUMN_SEPARATOR);
                            this.state = 16;
                            this.column();
                        }
                    }
                    this.state = 21;
                    this._errHandler.sync(this);
                    _la = this._input.LA(1);
                }
            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    // @RuleVersion(0)
    public column(): ColumnContext {
        let _localctx: ColumnContext = new ColumnContext(this._ctx, this.state);
        this.enterRule(_localctx, 2, ColumnsTokens.RULE_column);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 22;
                this.columnName();
                this.state = 25;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === ColumnsTokens.TYPE_SEPARATOR) {
                    {
                        this.state = 23;
                        this.match(ColumnsTokens.TYPE_SEPARATOR);
                        this.state = 24;
                        this.columnType();
                    }
                }

                this.state = 27;
                this.match(ColumnsTokens.EXPRESSION_SEPARATOR);
                this.state = 29;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === ColumnsTokens.FHIRPATH_STRING) {
                    {
                        this.state = 28;
                        this.fhirpathExpression();
                    }
                }

            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    // @RuleVersion(0)
    public columnType(): ColumnTypeContext {
        let _localctx: ColumnTypeContext = new ColumnTypeContext(this._ctx, this.state);
        this.enterRule(_localctx, 4, ColumnsTokens.RULE_columnType);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 31;
                this.typeName();
                this.state = 37;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === ColumnsTokens.PAREN_OPEN) {
                    {
                        this.state = 32;
                        this.match(ColumnsTokens.PAREN_OPEN);
                        this.state = 34;
                        this._errHandler.sync(this);
                        _la = this._input.LA(1);
                        if (_la === ColumnsTokens.PAREN_STRING) {
                            {
                                this.state = 33;
                                this.typeParam();
                            }
                        }

                        this.state = 36;
                        this.match(ColumnsTokens.PAREN_CLOSE);
                    }
                }

            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    // @RuleVersion(0)
    public columnName(): ColumnNameContext {
        let _localctx: ColumnNameContext = new ColumnNameContext(this._ctx, this.state);
        this.enterRule(_localctx, 6, ColumnsTokens.RULE_columnName);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 39;
                this.match(ColumnsTokens.COLUMN_STRING);
            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    // @RuleVersion(0)
    public typeName(): TypeNameContext {
        let _localctx: TypeNameContext = new TypeNameContext(this._ctx, this.state);
        this.enterRule(_localctx, 8, ColumnsTokens.RULE_typeName);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 41;
                this.match(ColumnsTokens.TYPE_STRING);
            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    // @RuleVersion(0)
    public typeParam(): TypeParamContext {
        let _localctx: TypeParamContext = new TypeParamContext(this._ctx, this.state);
        this.enterRule(_localctx, 10, ColumnsTokens.RULE_typeParam);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 43;
                this.match(ColumnsTokens.PAREN_STRING);
            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    // @RuleVersion(0)
    public fhirpathExpression(): FhirpathExpressionContext {
        let _localctx: FhirpathExpressionContext = new FhirpathExpressionContext(this._ctx, this.state);
        this.enterRule(_localctx, 12, ColumnsTokens.RULE_fhirpathExpression);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 45;
                this.match(ColumnsTokens.FHIRPATH_STRING);
            }
        } catch (re) {
            if (re instanceof RecognitionException) {
                _localctx.exception = re;
                this._errHandler.reportError(this, re);
                this._errHandler.recover(this, re);
            } else {
                throw re;
            }
        } finally {
            this.exitRule();
        }
        return _localctx;
    }

    public static readonly _serializedATN: string =
        "\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x03\f2\x04\x02\t" +
        "\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04\x06\t\x06\x04\x07\t" +
        "\x07\x04\b\t\b\x03\x02\x03\x02\x03\x02\x07\x02\x14\n\x02\f\x02\x0E\x02" +
        "\x17\v\x02\x03\x03\x03\x03\x03\x03\x05\x03\x1C\n\x03\x03\x03\x03\x03\x05" +
        "\x03 \n\x03\x03\x04\x03\x04\x03\x04\x05\x04%\n\x04\x03\x04\x05\x04(\n" +
        "\x04\x03\x05\x03\x05\x03\x06\x03\x06\x03\x07\x03\x07\x03\b\x03\b\x03\b" +
        "\x02\x02\x02\t\x02\x02\x04\x02\x06\x02\b\x02\n\x02\f\x02\x0E\x02\x02\x02" +
        "\x02/\x02\x10\x03\x02\x02\x02\x04\x18\x03\x02\x02\x02\x06!\x03\x02\x02" +
        "\x02\b)\x03\x02\x02\x02\n+\x03\x02\x02\x02\f-\x03\x02\x02\x02\x0E/\x03" +
        "\x02\x02\x02\x10\x15\x05\x04\x03\x02\x11\x12\x07\t\x02\x02\x12\x14\x05" +
        "\x04\x03\x02\x13\x11\x03\x02\x02\x02\x14\x17\x03\x02\x02\x02\x15\x13\x03" +
        "\x02\x02\x02\x15\x16\x03\x02\x02\x02\x16\x03\x03\x02\x02\x02\x17\x15\x03" +
        "\x02\x02\x02\x18\x1B\x05\b\x05\x02\x19\x1A\x07\x03\x02\x02\x1A\x1C\x05" +
        "\x06\x04\x02\x1B\x19\x03\x02\x02\x02\x1B\x1C\x03\x02\x02\x02\x1C\x1D\x03" +
        "\x02\x02\x02\x1D\x1F\x07\x04\x02\x02\x1E \x05\x0E\b\x02\x1F\x1E\x03\x02" +
        "\x02\x02\x1F \x03\x02\x02\x02 \x05\x03\x02\x02\x02!\'\x05\n\x06\x02\"" +
        "$\x07\x06\x02\x02#%\x05\f\x07\x02$#\x03\x02\x02\x02$%\x03\x02\x02\x02" +
        "%&\x03\x02\x02\x02&(\x07\v\x02\x02\'\"\x03\x02\x02\x02\'(\x03\x02\x02" +
        "\x02(\x07\x03\x02\x02\x02)*\x07\x05\x02\x02*\t\x03\x02\x02\x02+,\x07\x07" +
        "\x02\x02,\v\x03\x02\x02\x02-.\x07\n\x02\x02.\r\x03\x02\x02\x02/0\x07\b" +
        "\x02\x020\x0F\x03\x02\x02\x02\x07\x15\x1B\x1F$\'";
    public static __ATN: ATN;
    public static get _ATN(): ATN {
        if (!ColumnsTokens.__ATN) {
            ColumnsTokens.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(ColumnsTokens._serializedATN));
        }

        return ColumnsTokens.__ATN;
    }

}

export class ColumnsContext extends ParserRuleContext {
    public column(): ColumnContext[];
    public column(i: number): ColumnContext;
    public column(i?: number): ColumnContext | ColumnContext[] {
        if (i === undefined) {
            return this.getRuleContexts(ColumnContext);
        } else {
            return this.getRuleContext(i, ColumnContext);
        }
    }

    public COLUMN_SEPARATOR(): TerminalNode[];
    public COLUMN_SEPARATOR(i: number): TerminalNode;
    public COLUMN_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(ColumnsTokens.COLUMN_SEPARATOR);
        } else {
            return this.getToken(ColumnsTokens.COLUMN_SEPARATOR, i);
        }
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_columns;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterColumns) {
            listener.enterColumns(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitColumns) {
            listener.exitColumns(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitColumns) {
            return visitor.visitColumns(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class ColumnContext extends ParserRuleContext {
    public columnName(): ColumnNameContext {
        return this.getRuleContext(0, ColumnNameContext);
    }

    public EXPRESSION_SEPARATOR(): TerminalNode {
        return this.getToken(ColumnsTokens.EXPRESSION_SEPARATOR, 0);
    }

    public TYPE_SEPARATOR(): TerminalNode | undefined {
        return this.tryGetToken(ColumnsTokens.TYPE_SEPARATOR, 0);
    }

    public columnType(): ColumnTypeContext | undefined {
        return this.tryGetRuleContext(0, ColumnTypeContext);
    }

    public fhirpathExpression(): FhirpathExpressionContext | undefined {
        return this.tryGetRuleContext(0, FhirpathExpressionContext);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_column;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterColumn) {
            listener.enterColumn(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitColumn) {
            listener.exitColumn(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitColumn) {
            return visitor.visitColumn(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class ColumnTypeContext extends ParserRuleContext {
    public typeName(): TypeNameContext {
        return this.getRuleContext(0, TypeNameContext);
    }

    public PAREN_OPEN(): TerminalNode | undefined {
        return this.tryGetToken(ColumnsTokens.PAREN_OPEN, 0);
    }

    public PAREN_CLOSE(): TerminalNode | undefined {
        return this.tryGetToken(ColumnsTokens.PAREN_CLOSE, 0);
    }

    public typeParam(): TypeParamContext | undefined {
        return this.tryGetRuleContext(0, TypeParamContext);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_columnType;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterColumnType) {
            listener.enterColumnType(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitColumnType) {
            listener.exitColumnType(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitColumnType) {
            return visitor.visitColumnType(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class ColumnNameContext extends ParserRuleContext {
    public COLUMN_STRING(): TerminalNode {
        return this.getToken(ColumnsTokens.COLUMN_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_columnName;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterColumnName) {
            listener.enterColumnName(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitColumnName) {
            listener.exitColumnName(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitColumnName) {
            return visitor.visitColumnName(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class TypeNameContext extends ParserRuleContext {
    public TYPE_STRING(): TerminalNode {
        return this.getToken(ColumnsTokens.TYPE_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_typeName;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterTypeName) {
            listener.enterTypeName(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitTypeName) {
            listener.exitTypeName(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitTypeName) {
            return visitor.visitTypeName(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class TypeParamContext extends ParserRuleContext {
    public PAREN_STRING(): TerminalNode {
        return this.getToken(ColumnsTokens.PAREN_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_typeParam;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterTypeParam) {
            listener.enterTypeParam(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitTypeParam) {
            listener.exitTypeParam(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitTypeParam) {
            return visitor.visitTypeParam(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class FhirpathExpressionContext extends ParserRuleContext {
    public FHIRPATH_STRING(): TerminalNode {
        return this.getToken(ColumnsTokens.FHIRPATH_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return ColumnsTokens.RULE_fhirpathExpression;
    }

    // @Override
    public enterRule(listener: ColumnsTokensListener): void {
        if (listener.enterFhirpathExpression) {
            listener.enterFhirpathExpression(this);
        }
    }

    // @Override
    public exitRule(listener: ColumnsTokensListener): void {
        if (listener.exitFhirpathExpression) {
            listener.exitFhirpathExpression(this);
        }
    }

    // @Override
    public accept<Result>(visitor: ColumnsTokensVisitor<Result>): Result {
        if (visitor.visitFhirpathExpression) {
            return visitor.visitFhirpathExpression(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}



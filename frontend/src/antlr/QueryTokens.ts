// Generated from src/antlr/QueryTokens.g4 by ANTLR 4.7.3-SNAPSHOT


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

import {QueryTokensListener} from "./QueryTokensListener";
import {QueryTokensVisitor} from "./QueryTokensVisitor";


export class QueryTokens extends Parser {
    public static readonly PATH_STRING = 1;
    public static readonly PATH_SEPARATOR = 2;
    public static readonly QUERY_PATH_SEPARATOR = 3;
    public static readonly WS = 4;
    public static readonly QUERY_STRING = 5;
    public static readonly MODIFIER_SEPARATOR = 6;
    public static readonly SEARCHPARAMETER_SEPARATOR = 7;
    public static readonly KEYVAL_SEPARATOR = 8;
    public static readonly VALUE_SEPARATOR = 9;
    public static readonly COMPOSITE_SEPARATOR = 10;
    public static readonly BAR_SEPARATOR = 11;
    public static readonly QUERY_VALUE_STRING = 12;
    public static readonly RULE_url = 0;
    public static readonly RULE_path = 1;
    public static readonly RULE_query = 2;
    public static readonly RULE_search = 3;
    public static readonly RULE_searchparameter = 4;
    public static readonly RULE_values = 5;
    public static readonly RULE_value = 6;
    public static readonly RULE_keystring = 7;
    public static readonly RULE_path_string = 8;
    public static readonly RULE_modifier = 9;
    // tslint:disable:no-trailing-whitespace
    public static readonly ruleNames: string[] = [
        "url", "path", "query", "search", "searchparameter", "values", "value",
        "keystring", "path_string", "modifier",
    ];

    private static readonly _LITERAL_NAMES: Array<string | undefined> = [
        undefined, undefined, "'/'", "'?'", undefined, undefined, "':'", undefined,
        "'='", "','", "'$'", "'|'",
    ];
    private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
        undefined, "PATH_STRING", "PATH_SEPARATOR", "QUERY_PATH_SEPARATOR", "WS",
        "QUERY_STRING", "MODIFIER_SEPARATOR", "SEARCHPARAMETER_SEPARATOR", "KEYVAL_SEPARATOR",
        "VALUE_SEPARATOR", "COMPOSITE_SEPARATOR", "BAR_SEPARATOR", "QUERY_VALUE_STRING",
    ];
    public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(QueryTokens._LITERAL_NAMES, QueryTokens._SYMBOLIC_NAMES, []);

    // @Override
    // @NotNull
    public get vocabulary(): Vocabulary {
        return QueryTokens.VOCABULARY;
    }

    // tslint:enable:no-trailing-whitespace

    // @Override
    public get grammarFileName(): string {
        return "QueryTokens.g4";
    }

    // @Override
    public get ruleNames(): string[] {
        return QueryTokens.ruleNames;
    }

    // @Override
    public get serializedATN(): string {
        return QueryTokens._serializedATN;
    }

    constructor(input: TokenStream) {
        super(input);
        this._interp = new ParserATNSimulator(QueryTokens._ATN, this);
    }

    // @RuleVersion(0)
    public url(): UrlContext {
        let _localctx: UrlContext = new UrlContext(this._ctx, this.state);
        this.enterRule(_localctx, 0, QueryTokens.RULE_url);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 21;
                this._errHandler.sync(this);
                switch (this.interpreter.adaptivePredict(this._input, 0, this._ctx)) {
                    case 1: {
                        this.state = 20;
                        this.path();
                    }
                        break;
                }
                this.state = 24;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.QUERY_PATH_SEPARATOR) {
                    {
                        this.state = 23;
                        this.query();
                    }
                }

                this.state = 27;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.WS) {
                    {
                        this.state = 26;
                        this.match(QueryTokens.WS);
                    }
                }

                this.state = 29;
                this.match(QueryTokens.EOF);
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
    public path(): PathContext {
        let _localctx: PathContext = new PathContext(this._ctx, this.state);
        this.enterRule(_localctx, 2, QueryTokens.RULE_path);
        let _la: number;
        try {
            let _alt: number;
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 32;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.PATH_STRING) {
                    {
                        this.state = 31;
                        this.path_string();
                    }
                }

                this.state = 38;
                this._errHandler.sync(this);
                _alt = this.interpreter.adaptivePredict(this._input, 4, this._ctx);
                while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
                    if (_alt === 1) {
                        {
                            {
                                this.state = 34;
                                this.match(QueryTokens.PATH_SEPARATOR);
                                this.state = 35;
                                this.path_string();
                            }
                        }
                    }
                    this.state = 40;
                    this._errHandler.sync(this);
                    _alt = this.interpreter.adaptivePredict(this._input, 4, this._ctx);
                }
                this.state = 42;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.PATH_SEPARATOR) {
                    {
                        this.state = 41;
                        this.match(QueryTokens.PATH_SEPARATOR);
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
    public query(): QueryContext {
        let _localctx: QueryContext = new QueryContext(this._ctx, this.state);
        this.enterRule(_localctx, 4, QueryTokens.RULE_query);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 44;
                this.match(QueryTokens.QUERY_PATH_SEPARATOR);
                this.state = 46;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.QUERY_STRING) {
                    {
                        this.state = 45;
                        this.search();
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
    public search(): SearchContext {
        let _localctx: SearchContext = new SearchContext(this._ctx, this.state);
        this.enterRule(_localctx, 6, QueryTokens.RULE_search);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 48;
                this.searchparameter();
                this.state = 53;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                while (_la === QueryTokens.SEARCHPARAMETER_SEPARATOR) {
                    {
                        {
                            this.state = 49;
                            this.match(QueryTokens.SEARCHPARAMETER_SEPARATOR);
                            this.state = 50;
                            this.searchparameter();
                        }
                    }
                    this.state = 55;
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
    public searchparameter(): SearchparameterContext {
        let _localctx: SearchparameterContext = new SearchparameterContext(this._ctx, this.state);
        this.enterRule(_localctx, 8, QueryTokens.RULE_searchparameter);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 56;
                this.keystring();
                this.state = 61;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                while (_la === QueryTokens.MODIFIER_SEPARATOR) {
                    {
                        {
                            this.state = 57;
                            this.match(QueryTokens.MODIFIER_SEPARATOR);
                            this.state = 58;
                            this.modifier();
                        }
                    }
                    this.state = 63;
                    this._errHandler.sync(this);
                    _la = this._input.LA(1);
                }
                this.state = 66;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.KEYVAL_SEPARATOR) {
                    {
                        this.state = 64;
                        this.match(QueryTokens.KEYVAL_SEPARATOR);
                        this.state = 65;
                        this.values();
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
    public values(): ValuesContext {
        let _localctx: ValuesContext = new ValuesContext(this._ctx, this.state);
        this.enterRule(_localctx, 10, QueryTokens.RULE_values);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 69;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                if (_la === QueryTokens.QUERY_VALUE_STRING) {
                    {
                        this.state = 68;
                        this.value();
                    }
                }

                this.state = 75;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                while (_la === QueryTokens.VALUE_SEPARATOR) {
                    {
                        {
                            this.state = 71;
                            this.match(QueryTokens.VALUE_SEPARATOR);
                            this.state = 72;
                            this.value();
                        }
                    }
                    this.state = 77;
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
    public value(): ValueContext {
        let _localctx: ValueContext = new ValueContext(this._ctx, this.state);
        this.enterRule(_localctx, 12, QueryTokens.RULE_value);
        let _la: number;
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 78;
                this.match(QueryTokens.QUERY_VALUE_STRING);
                this.state = 93;
                this._errHandler.sync(this);
                switch (this.interpreter.adaptivePredict(this._input, 14, this._ctx)) {
                    case 1: {
                        this.state = 83;
                        this._errHandler.sync(this);
                        _la = this._input.LA(1);
                        while (_la === QueryTokens.BAR_SEPARATOR) {
                            {
                                {
                                    this.state = 79;
                                    this.match(QueryTokens.BAR_SEPARATOR);
                                    this.state = 80;
                                    this.match(QueryTokens.QUERY_VALUE_STRING);
                                }
                            }
                            this.state = 85;
                            this._errHandler.sync(this);
                            _la = this._input.LA(1);
                        }
                    }
                        break;

                    case 2: {
                        this.state = 90;
                        this._errHandler.sync(this);
                        _la = this._input.LA(1);
                        while (_la === QueryTokens.COMPOSITE_SEPARATOR) {
                            {
                                {
                                    this.state = 86;
                                    this.match(QueryTokens.COMPOSITE_SEPARATOR);
                                    this.state = 87;
                                    this.match(QueryTokens.QUERY_VALUE_STRING);
                                }
                            }
                            this.state = 92;
                            this._errHandler.sync(this);
                            _la = this._input.LA(1);
                        }
                    }
                        break;
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
    public keystring(): KeystringContext {
        let _localctx: KeystringContext = new KeystringContext(this._ctx, this.state);
        this.enterRule(_localctx, 14, QueryTokens.RULE_keystring);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 95;
                this.match(QueryTokens.QUERY_STRING);
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
    public path_string(): Path_stringContext {
        let _localctx: Path_stringContext = new Path_stringContext(this._ctx, this.state);
        this.enterRule(_localctx, 16, QueryTokens.RULE_path_string);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 97;
                this.match(QueryTokens.PATH_STRING);
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
    public modifier(): ModifierContext {
        let _localctx: ModifierContext = new ModifierContext(this._ctx, this.state);
        this.enterRule(_localctx, 18, QueryTokens.RULE_modifier);
        try {
            this.enterOuterAlt(_localctx, 1);
            {
                this.state = 99;
                this.match(QueryTokens.QUERY_STRING);
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
        "\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x03\x0Eh\x04\x02" +
        "\t\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04\x06\t\x06\x04\x07" +
        "\t\x07\x04\b\t\b\x04\t\t\t\x04\n\t\n\x04\v\t\v\x03\x02\x05\x02\x18\n\x02" +
        "\x03\x02\x05\x02\x1B\n\x02\x03\x02\x05\x02\x1E\n\x02\x03\x02\x03\x02\x03" +
        "\x03\x05\x03#\n\x03\x03\x03\x03\x03\x07\x03\'\n\x03\f\x03\x0E\x03*\v\x03" +
        "\x03\x03\x05\x03-\n\x03\x03\x04\x03\x04\x05\x041\n\x04\x03\x05\x03\x05" +
        "\x03\x05\x07\x056\n\x05\f\x05\x0E\x059\v\x05\x03\x06\x03\x06\x03\x06\x07" +
        "\x06>\n\x06\f\x06\x0E\x06A\v\x06\x03\x06\x03\x06\x05\x06E\n\x06\x03\x07" +
        "\x05\x07H\n\x07\x03\x07\x03\x07\x07\x07L\n\x07\f\x07\x0E\x07O\v\x07\x03" +
        "\b\x03\b\x03\b\x07\bT\n\b\f\b\x0E\bW\v\b\x03\b\x03\b\x07\b[\n\b\f\b\x0E" +
        "\b^\v\b\x05\b`\n\b\x03\t\x03\t\x03\n\x03\n\x03\v\x03\v\x03\v\x02\x02\x02" +
        "\f\x02\x02\x04\x02\x06\x02\b\x02\n\x02\f\x02\x0E\x02\x10\x02\x12\x02\x14" +
        "\x02\x02\x02\x02l\x02\x17\x03\x02\x02\x02\x04\"\x03\x02\x02\x02\x06.\x03" +
        "\x02\x02\x02\b2\x03\x02\x02\x02\n:\x03\x02\x02\x02\fG\x03\x02\x02\x02" +
        "\x0EP\x03\x02\x02\x02\x10a\x03\x02\x02\x02\x12c\x03\x02\x02\x02\x14e\x03" +
        "\x02\x02\x02\x16\x18\x05\x04\x03\x02\x17\x16\x03\x02\x02\x02\x17\x18\x03" +
        "\x02\x02\x02\x18\x1A\x03\x02\x02\x02\x19\x1B\x05\x06\x04\x02\x1A\x19\x03" +
        "\x02\x02\x02\x1A\x1B\x03\x02\x02\x02\x1B\x1D\x03\x02\x02\x02\x1C\x1E\x07" +
        "\x06\x02\x02\x1D\x1C\x03\x02\x02\x02\x1D\x1E\x03\x02\x02\x02\x1E\x1F\x03" +
        "\x02\x02\x02\x1F \x07\x02\x02\x03 \x03\x03\x02\x02\x02!#\x05\x12\n\x02" +
        "\"!\x03\x02\x02\x02\"#\x03\x02\x02\x02#(\x03\x02\x02\x02$%\x07\x04\x02" +
        "\x02%\'\x05\x12\n\x02&$\x03\x02\x02\x02\'*\x03\x02\x02\x02(&\x03\x02\x02" +
        "\x02()\x03\x02\x02\x02),\x03\x02\x02\x02*(\x03\x02\x02\x02+-\x07\x04\x02" +
        "\x02,+\x03\x02\x02\x02,-\x03\x02\x02\x02-\x05\x03\x02\x02\x02.0\x07\x05" +
        "\x02\x02/1\x05\b\x05\x020/\x03\x02\x02\x0201\x03\x02\x02\x021\x07\x03" +
        "\x02\x02\x0227\x05\n\x06\x0234\x07\t\x02\x0246\x05\n\x06\x0253\x03\x02" +
        "\x02\x0269\x03\x02\x02\x0275\x03\x02\x02\x0278\x03\x02\x02\x028\t\x03" +
        "\x02\x02\x0297\x03\x02\x02\x02:?\x05\x10\t\x02;<\x07\b\x02\x02<>\x05\x14" +
        "\v\x02=;\x03\x02\x02\x02>A\x03\x02\x02\x02?=\x03\x02\x02\x02?@\x03\x02" +
        "\x02\x02@D\x03\x02\x02\x02A?\x03\x02\x02\x02BC\x07\n\x02\x02CE\x05\f\x07" +
        "\x02DB\x03\x02\x02\x02DE\x03\x02\x02\x02E\v\x03\x02\x02\x02FH\x05\x0E" +
        "\b\x02GF\x03\x02\x02\x02GH\x03\x02\x02\x02HM\x03\x02\x02\x02IJ\x07\v\x02" +
        "\x02JL\x05\x0E\b\x02KI\x03\x02\x02\x02LO\x03\x02\x02\x02MK\x03\x02\x02" +
        "\x02MN\x03\x02\x02\x02N\r\x03\x02\x02\x02OM\x03\x02\x02\x02P_\x07\x0E" +
        "\x02\x02QR\x07\r\x02\x02RT\x07\x0E\x02\x02SQ\x03\x02\x02\x02TW\x03\x02" +
        "\x02\x02US\x03\x02\x02\x02UV\x03\x02\x02\x02V`\x03\x02\x02\x02WU\x03\x02" +
        "\x02\x02XY\x07\f\x02\x02Y[\x07\x0E\x02\x02ZX\x03\x02\x02\x02[^\x03\x02" +
        "\x02\x02\\Z\x03\x02\x02\x02\\]\x03\x02\x02\x02]`\x03\x02\x02\x02^\\\x03" +
        "\x02\x02\x02_U\x03\x02\x02\x02_\\\x03\x02\x02\x02`\x0F\x03\x02\x02\x02" +
        "ab\x07\x07\x02\x02b\x11\x03\x02\x02\x02cd\x07\x03\x02\x02d\x13\x03\x02" +
        "\x02\x02ef\x07\x07\x02\x02f\x15\x03\x02\x02\x02\x11\x17\x1A\x1D\"(,07" +
        "?DGMU\\_";
    public static __ATN: ATN;
    public static get _ATN(): ATN {
        if (!QueryTokens.__ATN) {
            QueryTokens.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(QueryTokens._serializedATN));
        }

        return QueryTokens.__ATN;
    }

}

export class UrlContext extends ParserRuleContext {
    public EOF(): TerminalNode {
        return this.getToken(QueryTokens.EOF, 0);
    }

    public path(): PathContext | undefined {
        return this.tryGetRuleContext(0, PathContext);
    }

    public query(): QueryContext | undefined {
        return this.tryGetRuleContext(0, QueryContext);
    }

    public WS(): TerminalNode | undefined {
        return this.tryGetToken(QueryTokens.WS, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_url;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterUrl) {
            listener.enterUrl(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitUrl) {
            listener.exitUrl(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitUrl) {
            return visitor.visitUrl(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class PathContext extends ParserRuleContext {
    public path_string(): Path_stringContext[];
    public path_string(i: number): Path_stringContext;
    public path_string(i?: number): Path_stringContext | Path_stringContext[] {
        if (i === undefined) {
            return this.getRuleContexts(Path_stringContext);
        } else {
            return this.getRuleContext(i, Path_stringContext);
        }
    }

    public PATH_SEPARATOR(): TerminalNode[];
    public PATH_SEPARATOR(i: number): TerminalNode;
    public PATH_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.PATH_SEPARATOR);
        } else {
            return this.getToken(QueryTokens.PATH_SEPARATOR, i);
        }
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_path;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterPath) {
            listener.enterPath(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitPath) {
            listener.exitPath(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitPath) {
            return visitor.visitPath(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class QueryContext extends ParserRuleContext {
    public QUERY_PATH_SEPARATOR(): TerminalNode {
        return this.getToken(QueryTokens.QUERY_PATH_SEPARATOR, 0);
    }

    public search(): SearchContext | undefined {
        return this.tryGetRuleContext(0, SearchContext);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_query;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterQuery) {
            listener.enterQuery(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitQuery) {
            listener.exitQuery(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitQuery) {
            return visitor.visitQuery(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class SearchContext extends ParserRuleContext {
    public searchparameter(): SearchparameterContext[];
    public searchparameter(i: number): SearchparameterContext;
    public searchparameter(i?: number): SearchparameterContext | SearchparameterContext[] {
        if (i === undefined) {
            return this.getRuleContexts(SearchparameterContext);
        } else {
            return this.getRuleContext(i, SearchparameterContext);
        }
    }

    public SEARCHPARAMETER_SEPARATOR(): TerminalNode[];
    public SEARCHPARAMETER_SEPARATOR(i: number): TerminalNode;
    public SEARCHPARAMETER_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.SEARCHPARAMETER_SEPARATOR);
        } else {
            return this.getToken(QueryTokens.SEARCHPARAMETER_SEPARATOR, i);
        }
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_search;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterSearch) {
            listener.enterSearch(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitSearch) {
            listener.exitSearch(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitSearch) {
            return visitor.visitSearch(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class SearchparameterContext extends ParserRuleContext {
    public keystring(): KeystringContext {
        return this.getRuleContext(0, KeystringContext);
    }

    public MODIFIER_SEPARATOR(): TerminalNode[];
    public MODIFIER_SEPARATOR(i: number): TerminalNode;
    public MODIFIER_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.MODIFIER_SEPARATOR);
        } else {
            return this.getToken(QueryTokens.MODIFIER_SEPARATOR, i);
        }
    }

    public modifier(): ModifierContext[];
    public modifier(i: number): ModifierContext;
    public modifier(i?: number): ModifierContext | ModifierContext[] {
        if (i === undefined) {
            return this.getRuleContexts(ModifierContext);
        } else {
            return this.getRuleContext(i, ModifierContext);
        }
    }

    public KEYVAL_SEPARATOR(): TerminalNode | undefined {
        return this.tryGetToken(QueryTokens.KEYVAL_SEPARATOR, 0);
    }

    public values(): ValuesContext | undefined {
        return this.tryGetRuleContext(0, ValuesContext);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_searchparameter;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterSearchparameter) {
            listener.enterSearchparameter(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitSearchparameter) {
            listener.exitSearchparameter(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitSearchparameter) {
            return visitor.visitSearchparameter(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class ValuesContext extends ParserRuleContext {
    public value(): ValueContext[];
    public value(i: number): ValueContext;
    public value(i?: number): ValueContext | ValueContext[] {
        if (i === undefined) {
            return this.getRuleContexts(ValueContext);
        } else {
            return this.getRuleContext(i, ValueContext);
        }
    }

    public VALUE_SEPARATOR(): TerminalNode[];
    public VALUE_SEPARATOR(i: number): TerminalNode;
    public VALUE_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.VALUE_SEPARATOR);
        } else {
            return this.getToken(QueryTokens.VALUE_SEPARATOR, i);
        }
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_values;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterValues) {
            listener.enterValues(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitValues) {
            listener.exitValues(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitValues) {
            return visitor.visitValues(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class ValueContext extends ParserRuleContext {
    public QUERY_VALUE_STRING(): TerminalNode[];
    public QUERY_VALUE_STRING(i: number): TerminalNode;
    public QUERY_VALUE_STRING(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.QUERY_VALUE_STRING);
        } else {
            return this.getToken(QueryTokens.QUERY_VALUE_STRING, i);
        }
    }

    public BAR_SEPARATOR(): TerminalNode[];
    public BAR_SEPARATOR(i: number): TerminalNode;
    public BAR_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.BAR_SEPARATOR);
        } else {
            return this.getToken(QueryTokens.BAR_SEPARATOR, i);
        }
    }

    public COMPOSITE_SEPARATOR(): TerminalNode[];
    public COMPOSITE_SEPARATOR(i: number): TerminalNode;
    public COMPOSITE_SEPARATOR(i?: number): TerminalNode | TerminalNode[] {
        if (i === undefined) {
            return this.getTokens(QueryTokens.COMPOSITE_SEPARATOR);
        } else {
            return this.getToken(QueryTokens.COMPOSITE_SEPARATOR, i);
        }
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_value;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterValue) {
            listener.enterValue(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitValue) {
            listener.exitValue(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitValue) {
            return visitor.visitValue(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class KeystringContext extends ParserRuleContext {
    public QUERY_STRING(): TerminalNode {
        return this.getToken(QueryTokens.QUERY_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_keystring;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterKeystring) {
            listener.enterKeystring(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitKeystring) {
            listener.exitKeystring(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitKeystring) {
            return visitor.visitKeystring(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class Path_stringContext extends ParserRuleContext {
    public PATH_STRING(): TerminalNode {
        return this.getToken(QueryTokens.PATH_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_path_string;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterPath_string) {
            listener.enterPath_string(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitPath_string) {
            listener.exitPath_string(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitPath_string) {
            return visitor.visitPath_string(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}


export class ModifierContext extends ParserRuleContext {
    public QUERY_STRING(): TerminalNode {
        return this.getToken(QueryTokens.QUERY_STRING, 0);
    }

    constructor(parent: ParserRuleContext | undefined, invokingState: number) {
        super(parent, invokingState);
    }

    // @Override
    public get ruleIndex(): number {
        return QueryTokens.RULE_modifier;
    }

    // @Override
    public enterRule(listener: QueryTokensListener): void {
        if (listener.enterModifier) {
            listener.enterModifier(this);
        }
    }

    // @Override
    public exitRule(listener: QueryTokensListener): void {
        if (listener.exitModifier) {
            listener.exitModifier(this);
        }
    }

    // @Override
    public accept<Result>(visitor: QueryTokensVisitor<Result>): Result {
        if (visitor.visitModifier) {
            return visitor.visitModifier(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}



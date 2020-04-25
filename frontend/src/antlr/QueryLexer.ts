// Generated from src/antlr/QueryLexer.g4 by ANTLR 4.7.3-SNAPSHOT


import {ATN} from "antlr4ts/atn/ATN";
import {ATNDeserializer} from "antlr4ts/atn/ATNDeserializer";
import {CharStream} from "antlr4ts/CharStream";
import {Lexer} from "antlr4ts/Lexer";
import {LexerATNSimulator} from "antlr4ts/atn/LexerATNSimulator";
import {NotNull} from "antlr4ts/Decorators";
import {Override} from "antlr4ts/Decorators";
import {RuleContext} from "antlr4ts/RuleContext";
import {Vocabulary} from "antlr4ts/Vocabulary";
import {VocabularyImpl} from "antlr4ts/VocabularyImpl";

import * as Utils from "antlr4ts/misc/Utils";


export class QueryLexer extends Lexer {
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
    public static readonly QUERY_KEY = 1;
    public static readonly QUERY_VALUE = 2;

    // tslint:disable:no-trailing-whitespace
    public static readonly channelNames: string[] = [
        "DEFAULT_TOKEN_CHANNEL", "HIDDEN",
    ];

    // tslint:disable:no-trailing-whitespace
    public static readonly modeNames: string[] = [
        "DEFAULT_MODE", "QUERY_KEY", "QUERY_VALUE",
    ];

    public static readonly ruleNames: string[] = [
        "PATH_STRING", "PATH_SEPARATOR", "QUERY_PATH_SEPARATOR", "WS", "QUERY_STRING",
        "MODIFIER_SEPARATOR", "SEARCHPARAMETER_SEPARATOR", "KEYVAL_SEPARATOR",
        "VALUE_SEPARATOR", "COMPOSITE_SEPARATOR", "BAR_SEPARATOR", "SEARCHPARAMETER_SEPARATOR2",
        "QUERY_VALUE_STRING",
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
    public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(QueryLexer._LITERAL_NAMES, QueryLexer._SYMBOLIC_NAMES, []);

    // @Override
    // @NotNull
    public get vocabulary(): Vocabulary {
        return QueryLexer.VOCABULARY;
    }

    // tslint:enable:no-trailing-whitespace


    constructor(input: CharStream) {
        super(input);
        this._interp = new LexerATNSimulator(QueryLexer._ATN, this);
    }

    // @Override
    public get grammarFileName(): string {
        return "QueryLexer.g4";
    }

    // @Override
    public get ruleNames(): string[] {
        return QueryLexer.ruleNames;
    }

    // @Override
    public get serializedATN(): string {
        return QueryLexer._serializedATN;
    }

    // @Override
    public get channelNames(): string[] {
        return QueryLexer.channelNames;
    }

    // @Override
    public get modeNames(): string[] {
        return QueryLexer.modeNames;
    }

    public static readonly _serializedATN: string =
        "\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x02\x0ER\b\x01\b" +
        "\x01\b\x01\x04\x02\t\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04" +
        "\x06\t\x06\x04\x07\t\x07\x04\b\t\b\x04\t\t\t\x04\n\t\n\x04\v\t\v\x04\f" +
        "\t\f\x04\r\t\r\x04\x0E\t\x0E\x03\x02\x06\x02!\n\x02\r\x02\x0E\x02\"\x03" +
        "\x03\x03\x03\x03\x04\x03\x04\x03\x04\x03\x04\x03\x05\x06\x05,\n\x05\r" +
        "\x05\x0E\x05-\x03\x06\x06\x061\n\x06\r\x06\x0E\x062\x03\x07\x03\x07\x03" +
        "\b\x03\b\x03\t\x03\t\x03\t\x03\t\x03\n\x03\n\x03\v\x03\v\x03\f\x03\f\x03" +
        "\r\x03\r\x03\r\x03\r\x03\r\x03\x0E\x03\x0E\x03\x0E\x03\x0E\x03\x0E\x03" +
        "\x0E\x03\x0E\x06\x0EO\n\x0E\r\x0E\x0E\x0EP\x02\x02\x02\x0F\x05\x02\x03" +
        "\x07\x02\x04\t\x02\x05\v\x02\x06\r\x02\x07\x0F\x02\b\x11\x02\t\x13\x02" +
        "\n\x15\x02\v\x17\x02\f\x19\x02\r\x1B\x02\x02\x1D\x02\x0E\x05\x02\x03\x04" +
        "\x06\x04\x0211AA\x04\x02\f\f\x0F\x0F\x05\x02((<<??\x06\x02&&((..~~\x02" +
        "V\x02\x05\x03\x02\x02\x02\x02\x07\x03\x02\x02\x02\x02\t\x03\x02\x02\x02" +
        "\x02\v\x03\x02\x02\x02\x03\r\x03\x02\x02\x02\x03\x0F\x03\x02\x02\x02\x03" +
        "\x11\x03\x02\x02\x02\x03\x13\x03\x02\x02\x02\x04\x15\x03\x02\x02\x02\x04" +
        "\x17\x03\x02\x02\x02\x04\x19\x03\x02\x02\x02\x04\x1B\x03\x02\x02\x02\x04" +
        "\x1D\x03\x02\x02\x02\x05 \x03\x02\x02\x02\x07$\x03\x02\x02\x02\t&\x03" +
        "\x02\x02\x02\v+\x03\x02\x02\x02\r0\x03\x02\x02\x02\x0F4\x03\x02\x02\x02" +
        "\x116\x03\x02\x02\x02\x138\x03\x02\x02\x02\x15<\x03\x02\x02\x02\x17>\x03" +
        "\x02\x02\x02\x19@\x03\x02\x02\x02\x1BB\x03\x02\x02\x02\x1DN\x03\x02\x02" +
        "\x02\x1F!\n\x02\x02\x02 \x1F\x03\x02\x02\x02!\"\x03\x02\x02\x02\" \x03" +
        "\x02\x02\x02\"#\x03\x02\x02\x02#\x06\x03\x02\x02\x02$%\x071\x02\x02%\b" +
        "\x03\x02\x02\x02&\'\x07A\x02\x02\'(\x03\x02\x02\x02()\b\x04\x02\x02)\n" +
        "\x03\x02\x02\x02*,\t\x03\x02\x02+*\x03\x02\x02\x02,-\x03\x02\x02\x02-" +
        "+\x03\x02\x02\x02-.\x03\x02\x02\x02.\f\x03\x02\x02\x02/1\n\x04\x02\x02" +
        "0/\x03\x02\x02\x0212\x03\x02\x02\x0220\x03\x02\x02\x0223\x03\x02\x02\x02" +
        "3\x0E\x03\x02\x02\x0245\x07<\x02\x025\x10\x03\x02\x02\x0267\x07(\x02\x02" +
        "7\x12\x03\x02\x02\x0289\x07?\x02\x029:\x03\x02\x02\x02:;\b\t\x03\x02;" +
        "\x14\x03\x02\x02\x02<=\x07.\x02\x02=\x16\x03\x02\x02\x02>?\x07&\x02\x02" +
        "?\x18\x03\x02\x02\x02@A\x07~\x02\x02A\x1A\x03\x02\x02\x02BC\x07(\x02\x02" +
        "CD\x03\x02\x02\x02DE\b\r\x04\x02EF\b\r\x05\x02F\x1C\x03\x02\x02\x02GO" +
        "\n\x05\x02\x02HI\x07^\x02\x02IO\x07.\x02\x02JK\x07^\x02\x02KO\x07&\x02" +
        "\x02LM\x07^\x02\x02MO\x07~\x02\x02NG\x03\x02\x02\x02NH\x03\x02\x02\x02" +
        "NJ\x03\x02\x02\x02NL\x03\x02\x02\x02OP\x03\x02\x02\x02PN\x03\x02\x02\x02" +
        "PQ\x03\x02\x02\x02Q\x1E\x03\x02\x02\x02\n\x02\x03\x04\"-2NP\x06\x07\x03" +
        "\x02\x07\x04\x02\x06\x02\x02\t\t\x02";
    public static __ATN: ATN;
    public static get _ATN(): ATN {
        if (!QueryLexer.__ATN) {
            QueryLexer.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(QueryLexer._serializedATN));
        }

        return QueryLexer.__ATN;
    }

}


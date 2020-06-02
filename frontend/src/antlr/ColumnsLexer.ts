// Generated from src/antlr/ColumnsLexer.g4 by ANTLR 4.7.3-SNAPSHOT


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


export class ColumnsLexer extends Lexer {
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
    public static readonly TYPE = 1;
    public static readonly EXPRESSION = 2;
    public static readonly PAREN = 3;

    // tslint:disable:no-trailing-whitespace
    public static readonly channelNames: string[] = [
        "DEFAULT_TOKEN_CHANNEL", "HIDDEN",
    ];

    // tslint:disable:no-trailing-whitespace
    public static readonly modeNames: string[] = [
        "DEFAULT_MODE", "TYPE", "EXPRESSION", "PAREN",
    ];

    public static readonly ruleNames: string[] = [
        "TYPE_SEPARATOR", "EXPRESSION_SEPARATOR", "COLUMN_STRING", "PAREN_OPEN",
        "EXPRESSION_SEPARATOR2", "TYPE_STRING", "FHIRPATH_STRING", "COLUMN_SEPARATOR",
        "PAREN_STRING", "PAREN_CLOSE", "WS",
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
    public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(ColumnsLexer._LITERAL_NAMES, ColumnsLexer._SYMBOLIC_NAMES, []);

    // @Override
    // @NotNull
    public get vocabulary(): Vocabulary {
        return ColumnsLexer.VOCABULARY;
    }

    // tslint:enable:no-trailing-whitespace


    constructor(input: CharStream) {
        super(input);
        this._interp = new LexerATNSimulator(ColumnsLexer._ATN, this);
    }

    // @Override
    public get grammarFileName(): string {
        return "ColumnsLexer.g4";
    }

    // @Override
    public get ruleNames(): string[] {
        return ColumnsLexer.ruleNames;
    }

    // @Override
    public get serializedATN(): string {
        return ColumnsLexer._serializedATN;
    }

    // @Override
    public get channelNames(): string[] {
        return ColumnsLexer.channelNames;
    }

    // @Override
    public get modeNames(): string[] {
        return ColumnsLexer.modeNames;
    }

    public static readonly _serializedATN: string =
        "\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x02\fW\b\x01\b\x01" +
        "\b\x01\b\x01\x04\x02\t\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04" +
        "\x06\t\x06\x04\x07\t\x07\x04\b\t\b\x04\t\t\t\x04\n\t\n\x04\v\t\v\x04\f" +
        "\t\f\x03\x02\x03\x02\x03\x02\x03\x02\x03\x03\x03\x03\x03\x03\x03\x03\x03" +
        "\x04\x03\x04\x03\x04\x03\x04\x03\x04\x06\x04*\n\x04\r\x04\x0E\x04+\x03" +
        "\x05\x03\x05\x03\x05\x03\x05\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x03" +
        "\x06\x03\x07\x06\x079\n\x07\r\x07\x0E\x07:\x03\b\x03\b\x03\b\x06\b@\n" +
        "\b\r\b\x0E\bA\x03\t\x03\t\x03\t\x03\t\x03\n\x03\n\x03\n\x06\nK\n\n\r\n" +
        "\x0E\nL\x03\v\x03\v\x03\v\x03\v\x03\f\x06\fT\n\f\r\f\x0E\fU\x02\x02\x02" +
        "\r\x06\x02\x03\b\x02\x04\n\x02\x05\f\x02\x06\x0E\x02\x02\x10\x02\x07\x12" +
        "\x02\b\x14\x02\t\x16\x02\n\x18\x02\v\x1A\x02\f\x06\x02\x03\x04\x05\x07" +
        "\x04\x02<<BB\x04\x02**<<\x03\x02..\x03\x02++\x04\x02\f\f\x0F\x0F\x02\\" +
        "\x02\x06\x03\x02\x02\x02\x02\b\x03\x02\x02\x02\x02\n\x03\x02\x02\x02\x03" +
        "\f\x03\x02\x02\x02\x03\x0E\x03\x02\x02\x02\x03\x10\x03\x02\x02\x02\x04" +
        "\x12\x03\x02\x02\x02\x04\x14\x03\x02\x02\x02\x05\x16\x03\x02\x02\x02\x05" +
        "\x18\x03\x02\x02\x02\x05\x1A\x03\x02\x02\x02\x06\x1C\x03\x02\x02\x02\b" +
        " \x03\x02\x02\x02\n)\x03\x02\x02\x02\f-\x03\x02\x02\x02\x0E1\x03\x02\x02" +
        "\x02\x108\x03\x02\x02\x02\x12?\x03\x02\x02\x02\x14C\x03\x02\x02\x02\x16" +
        "J\x03\x02\x02\x02\x18N\x03\x02\x02\x02\x1AS\x03\x02\x02\x02\x1C\x1D\x07" +
        "B\x02\x02\x1D\x1E\x03\x02\x02\x02\x1E\x1F\b\x02\x02\x02\x1F\x07\x03\x02" +
        "\x02\x02 !\x07<\x02\x02!\"\x03\x02\x02\x02\"#\b\x03\x03\x02#\t\x03\x02" +
        "\x02\x02$*\n\x02\x02\x02%&\x07^\x02\x02&*\x07B\x02\x02\'(\x07^\x02\x02" +
        "(*\x07<\x02\x02)$\x03\x02\x02\x02)%\x03\x02\x02\x02)\'\x03\x02\x02\x02" +
        "*+\x03\x02\x02\x02+)\x03\x02\x02\x02+,\x03\x02\x02\x02,\v\x03\x02\x02" +
        "\x02-.\x07*\x02\x02./\x03\x02\x02\x02/0\b\x05\x04\x020\r\x03\x02\x02\x02" +
        "12\x07<\x02\x0223\x03\x02\x02\x0234\b\x06\x05\x0245\b\x06\x06\x0256\b" +
        "\x06\x03\x026\x0F\x03\x02\x02\x0279\n\x03\x02\x0287\x03\x02\x02\x029:" +
        "\x03\x02\x02\x02:8\x03\x02\x02\x02:;\x03\x02\x02\x02;\x11\x03\x02\x02" +
        "\x02<@\n\x04\x02\x02=>\x07^\x02\x02>@\x07.\x02\x02?<\x03\x02\x02\x02?" +
        "=\x03\x02\x02\x02@A\x03\x02\x02\x02A?\x03\x02\x02\x02AB\x03\x02\x02\x02" +
        "B\x13\x03\x02\x02\x02CD\x07.\x02\x02DE\x03\x02\x02\x02EF\b\t\x05\x02F" +
        "\x15\x03\x02\x02\x02GK\n\x05\x02\x02HI\x07^\x02\x02IK\x07+\x02\x02JG\x03" +
        "\x02\x02\x02JH\x03\x02\x02\x02KL\x03\x02\x02\x02LJ\x03\x02\x02\x02LM\x03" +
        "\x02\x02\x02M\x17\x03\x02\x02\x02NO\x07+\x02\x02OP\x03\x02\x02\x02PQ\b" +
        "\v\x05\x02Q\x19\x03\x02\x02\x02RT\t\x06\x02\x02SR\x03\x02\x02\x02TU\x03" +
        "\x02\x02\x02US\x03\x02\x02\x02UV\x03\x02\x02\x02V\x1B\x03\x02\x02\x02" +
        "\x0E\x02\x03\x04\x05)+:?AJLU\x07\x07\x03\x02\x07\x04\x02\x07\x05\x02\x06" +
        "\x02\x02\t\x04\x02";
    public static __ATN: ATN;
    public static get _ATN(): ATN {
        if (!ColumnsLexer.__ATN) {
            ColumnsLexer.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(ColumnsLexer._serializedATN));
        }

        return ColumnsLexer.__ATN;
    }

}


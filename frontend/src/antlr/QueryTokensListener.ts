// Generated from src/antlr/QueryTokens.g4 by ANTLR 4.7.3-SNAPSHOT


import {ParseTreeListener} from "antlr4ts/tree/ParseTreeListener";

import {UrlContext} from "./QueryTokens";
import {PathContext} from "./QueryTokens";
import {QueryContext} from "./QueryTokens";
import {SearchContext} from "./QueryTokens";
import {SearchparameterContext} from "./QueryTokens";
import {ValuesContext} from "./QueryTokens";
import {ValueContext} from "./QueryTokens";
import {KeystringContext} from "./QueryTokens";
import {Path_stringContext} from "./QueryTokens";
import {ModifierContext} from "./QueryTokens";


/**
 * This interface defines a complete listener for a parse tree produced by
 * `QueryTokens`.
 */
export interface QueryTokensListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by `QueryTokens.url`.
     * @param ctx the parse tree
     */
    enterUrl?: (ctx: UrlContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.url`.
     * @param ctx the parse tree
     */
    exitUrl?: (ctx: UrlContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.path`.
     * @param ctx the parse tree
     */
    enterPath?: (ctx: PathContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.path`.
     * @param ctx the parse tree
     */
    exitPath?: (ctx: PathContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.query`.
     * @param ctx the parse tree
     */
    enterQuery?: (ctx: QueryContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.query`.
     * @param ctx the parse tree
     */
    exitQuery?: (ctx: QueryContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.search`.
     * @param ctx the parse tree
     */
    enterSearch?: (ctx: SearchContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.search`.
     * @param ctx the parse tree
     */
    exitSearch?: (ctx: SearchContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.searchparameter`.
     * @param ctx the parse tree
     */
    enterSearchparameter?: (ctx: SearchparameterContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.searchparameter`.
     * @param ctx the parse tree
     */
    exitSearchparameter?: (ctx: SearchparameterContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.values`.
     * @param ctx the parse tree
     */
    enterValues?: (ctx: ValuesContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.values`.
     * @param ctx the parse tree
     */
    exitValues?: (ctx: ValuesContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.value`.
     * @param ctx the parse tree
     */
    enterValue?: (ctx: ValueContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.value`.
     * @param ctx the parse tree
     */
    exitValue?: (ctx: ValueContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.keystring`.
     * @param ctx the parse tree
     */
    enterKeystring?: (ctx: KeystringContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.keystring`.
     * @param ctx the parse tree
     */
    exitKeystring?: (ctx: KeystringContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.path_string`.
     * @param ctx the parse tree
     */
    enterPath_string?: (ctx: Path_stringContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.path_string`.
     * @param ctx the parse tree
     */
    exitPath_string?: (ctx: Path_stringContext) => void;

    /**
     * Enter a parse tree produced by `QueryTokens.modifier`.
     * @param ctx the parse tree
     */
    enterModifier?: (ctx: ModifierContext) => void;
    /**
     * Exit a parse tree produced by `QueryTokens.modifier`.
     * @param ctx the parse tree
     */
    exitModifier?: (ctx: ModifierContext) => void;
}


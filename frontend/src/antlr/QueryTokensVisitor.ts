// Generated from src/antlr/QueryTokens.g4 by ANTLR 4.7.3-SNAPSHOT


import {ParseTreeVisitor} from "antlr4ts/tree/ParseTreeVisitor";

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
 * This interface defines a complete generic visitor for a parse tree produced
 * by `QueryTokens`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface QueryTokensVisitor<Result> extends ParseTreeVisitor<Result> {
    /**
     * Visit a parse tree produced by `QueryTokens.url`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitUrl?: (ctx: UrlContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.path`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitPath?: (ctx: PathContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.query`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitQuery?: (ctx: QueryContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.search`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitSearch?: (ctx: SearchContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.searchparameter`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitSearchparameter?: (ctx: SearchparameterContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.values`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitValues?: (ctx: ValuesContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.value`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitValue?: (ctx: ValueContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.keystring`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitKeystring?: (ctx: KeystringContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.path_string`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitPath_string?: (ctx: Path_stringContext) => Result;

    /**
     * Visit a parse tree produced by `QueryTokens.modifier`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitModifier?: (ctx: ModifierContext) => Result;
}


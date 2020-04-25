// Generated from src/antlr/ColumnsTokens.g4 by ANTLR 4.7.3-SNAPSHOT


import {ParseTreeVisitor} from "antlr4ts/tree/ParseTreeVisitor";

import {ColumnsContext} from "./ColumnsTokens";
import {ColumnContext} from "./ColumnsTokens";
import {ColumnTypeContext} from "./ColumnsTokens";
import {ColumnNameContext} from "./ColumnsTokens";
import {TypeNameContext} from "./ColumnsTokens";
import {TypeParamContext} from "./ColumnsTokens";
import {FhirpathExpressionContext} from "./ColumnsTokens";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `ColumnsTokens`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface ColumnsTokensVisitor<Result> extends ParseTreeVisitor<Result> {
    /**
     * Visit a parse tree produced by `ColumnsTokens.columns`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitColumns?: (ctx: ColumnsContext) => Result;

    /**
     * Visit a parse tree produced by `ColumnsTokens.column`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitColumn?: (ctx: ColumnContext) => Result;

    /**
     * Visit a parse tree produced by `ColumnsTokens.columnType`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitColumnType?: (ctx: ColumnTypeContext) => Result;

    /**
     * Visit a parse tree produced by `ColumnsTokens.columnName`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitColumnName?: (ctx: ColumnNameContext) => Result;

    /**
     * Visit a parse tree produced by `ColumnsTokens.typeName`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitTypeName?: (ctx: TypeNameContext) => Result;

    /**
     * Visit a parse tree produced by `ColumnsTokens.typeParam`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitTypeParam?: (ctx: TypeParamContext) => Result;

    /**
     * Visit a parse tree produced by `ColumnsTokens.fhirpathExpression`.
     * @param ctx the parse tree
     * @return the visitor result
     */
    visitFhirpathExpression?: (ctx: FhirpathExpressionContext) => Result;
}


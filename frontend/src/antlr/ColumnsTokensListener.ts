// Generated from src/antlr/ColumnsTokens.g4 by ANTLR 4.7.3-SNAPSHOT


import {ParseTreeListener} from "antlr4ts/tree/ParseTreeListener";

import {ColumnsContext} from "./ColumnsTokens";
import {ColumnContext} from "./ColumnsTokens";
import {ColumnTypeContext} from "./ColumnsTokens";
import {ColumnNameContext} from "./ColumnsTokens";
import {TypeNameContext} from "./ColumnsTokens";
import {TypeParamContext} from "./ColumnsTokens";
import {FhirpathExpressionContext} from "./ColumnsTokens";


/**
 * This interface defines a complete listener for a parse tree produced by
 * `ColumnsTokens`.
 */
export interface ColumnsTokensListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by `ColumnsTokens.columns`.
     * @param ctx the parse tree
     */
    enterColumns?: (ctx: ColumnsContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.columns`.
     * @param ctx the parse tree
     */
    exitColumns?: (ctx: ColumnsContext) => void;

    /**
     * Enter a parse tree produced by `ColumnsTokens.column`.
     * @param ctx the parse tree
     */
    enterColumn?: (ctx: ColumnContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.column`.
     * @param ctx the parse tree
     */
    exitColumn?: (ctx: ColumnContext) => void;

    /**
     * Enter a parse tree produced by `ColumnsTokens.columnType`.
     * @param ctx the parse tree
     */
    enterColumnType?: (ctx: ColumnTypeContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.columnType`.
     * @param ctx the parse tree
     */
    exitColumnType?: (ctx: ColumnTypeContext) => void;

    /**
     * Enter a parse tree produced by `ColumnsTokens.columnName`.
     * @param ctx the parse tree
     */
    enterColumnName?: (ctx: ColumnNameContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.columnName`.
     * @param ctx the parse tree
     */
    exitColumnName?: (ctx: ColumnNameContext) => void;

    /**
     * Enter a parse tree produced by `ColumnsTokens.typeName`.
     * @param ctx the parse tree
     */
    enterTypeName?: (ctx: TypeNameContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.typeName`.
     * @param ctx the parse tree
     */
    exitTypeName?: (ctx: TypeNameContext) => void;

    /**
     * Enter a parse tree produced by `ColumnsTokens.typeParam`.
     * @param ctx the parse tree
     */
    enterTypeParam?: (ctx: TypeParamContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.typeParam`.
     * @param ctx the parse tree
     */
    exitTypeParam?: (ctx: TypeParamContext) => void;

    /**
     * Enter a parse tree produced by `ColumnsTokens.fhirpathExpression`.
     * @param ctx the parse tree
     */
    enterFhirpathExpression?: (ctx: FhirpathExpressionContext) => void;
    /**
     * Exit a parse tree produced by `ColumnsTokens.fhirpathExpression`.
     * @param ctx the parse tree
     */
    exitFhirpathExpression?: (ctx: FhirpathExpressionContext) => void;
}


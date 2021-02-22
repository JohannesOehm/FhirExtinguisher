/**
 * parseTokens into AST, which also features parent pointers on the objects
 */

function parseTokens(tokens, current) {
    let token = tokens[current];
    if (token.type === "inlinecomment") {
        return parseInlineComment(tokens, current);
    } else if (token.type === "multilinecomment") {
        return parseMultilineComment(tokens, current);
    } else if (token.type === "whitespace") {
        return parseWhitespace(tokens, current);
    } else if (token.type === "string") {
        return parseString(tokens, current);
    } else if (token.type === "literal") {
        return parseLiteral(tokens, current);
    } else if (token.type === "number") {
        return parseNumber(tokens, current);
    } else if (token.type === "punctuation") {
        if (token.raw === "{") {
            return parseObject(tokens, current);
        } else if (token.raw === "[") {
            return parseArray(tokens, current);
        } else if (token.raw === ":" || token.raw === ",") {
            return parseToken(tokens, current);
        }
    }

}

function parseInlineComment(tokens, current) {
    return [current + 1, {type: "InlineComment", raw: tokens[current].raw, position: tokens[current].position}];
}

function parseMutltilineComment(tokens, current) {
    return [current + 1, {type: "MultilineComment", raw: tokens[current].raw, position: tokens[current].position}];
}

function parseString(tokens, current) {
    return [current + 1, {type: "StringLiteral", value: tokens[current].value, position: tokens[current].position}];
}

function parseNumber(tokens, current) {
    return [current + 1, {type: "NumberLiteral", value: tokens[current].value, position: tokens[current].position}];
}

function parseLiteral(tokens, current) {
    return [current + 1, {type: "LiteralLiteral", value: tokens[current].value, position: tokens[current].position}];
}

function parseWhitespace(tokens, current) {
    return [current + 1, {type: "Whitespace", value: tokens[current].value, position: tokens[current].position}];
}

function parseToken(tokens, current) {
    return [current + 1, {type: "Punctuation", value: tokens[current].raw, position: tokens[current].position}]
}

function parseObject(tokens, current) {
    let children = [];
    let curr = current + 1;
    let result = {type: "Object", children: children};
    while (tokens[curr].raw !== "}") {
        let foo = parseTokens(tokens, curr);
        curr = foo[0];
        foo[1].parent = result;
        children.push(foo[1]);
    }
    return [curr + 1, result]; //TODO: Add position
}

function parseArray(tokens, current) {
    let children = [];
    let curr = current + 1;
    let result = {type: "Object", children: children};
    while (tokens[curr].raw !== "]") {
        let foo = parseTokens(tokens, curr);
        curr = foo[0];
        foo[1].parent = result;
        children.push(foo[1]);
    }
    return [curr + 1, result]; //TODO: Add position
}
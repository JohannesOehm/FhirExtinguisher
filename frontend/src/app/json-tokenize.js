//Stolen from https://github.com/queckezz/json-tokenize
//Modifications to run with Internet Explorer and not crash when having incomplete last token


var tokenTypes = [
    {
        regexp: /^\/\/.*/,
        create: function (value, position) {
            return {
                type: 'inlinecomment',
                position: position,
                raw: value,
                value: value
            }
        }
    },
    {
        regexp: /^\/\*.*\/\*/,
        create: function (value, position) {
            return {
                type: 'multilinecomment',
                position: position,
                raw: value,
                value: value
            }
        }
    },
    {
        regexp: /^\s+/,
        create: function (value, position) {
            return {
                type: 'whitespace',
                position: position,
                raw: value,
                value: value
            }
        }
    },
    {
        regexp: /^"(?:[^"\\]|\\.)*"/,
        create: function (value, position) {
            return {
                type: 'string',
                position: position,
                raw: value,
                value: value
                    .slice(1, -1)
                    .replace(/\\"/g, '"')
            };
        }
    },

    {
        regexp: /^(true|false|null)/,
        create: function (value, position) {
            return {
                type: 'literal',
                position: position,
                raw: value,
                value: value === 'null'
                    ? null
                    : value === 'true'
            };
        }
    },

    {
        regexp: /^(-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?)/,
        create: function (value, position) {
            return {
                type: 'number',
                position: position,
                raw: value,
                value: +value
            };
        }
    },

    {
        regexp: /^({|}|\[|]|:|,)/,
        create: function (value, position) {
            return {
                type: 'punctuation',
                position: position,
                raw: value,
                value: value
            };
        }
    }
];

function tokenize(json, tokens, position) {
    tokens = tokens || [];
    position = position || {lineno: 1, column: 1};

    var char = json[0];

    if (!char) {
        return tokens
    }

    var tmp = tokenTypes.reduce(function (acc, type) {
        if (acc) return acc;
        var str = match(type.regexp);
        if (!str) return acc;

        return [
            type.create,
            str
        ]
    }, null);
    if (tmp == null) {
        return tokens;
    }

    var createToken = tmp[0];
    var str = tmp[1];


    var token = createToken(str, str.length === 1 ? position : {start: position, end: updateColumn(str.length - 1)});

    var lines = str.match(/^\n+/g);

    if (lines) {
        return tokenize(advance(lines), tokens, {lineno: position.lineno + lines.length, column: 1});
    }

    return next(token);

    function updateColumn(column) {
        return {
            lineno: position.lineno,
            column: position.column + column
        }
    }

    function next(token, nextPosition) {
        return tokenize(
            advance(token.raw),
            tokens.concat([token]),
        nextPosition || updateColumn(token.raw.length)
            )
    }

    function match(re) {
        var m = re.exec(json);
        if (!m) return;
        var str = m[0];
        return str
    }

    function advance(str) {
        return json.slice(str.length)
    }
}

module.exports.tokenize = tokenize
module.exports.getPositionOfReferences = function (tokens, paths) {
    var stack = [];
    var highlights = [];

    var nextStringWillBeKey = true; //Object key/value distinction
    for (var i in tokens) {
        //TODO: Handle arrays?!
        var token = tokens[i];
        if (token.type === "punctuation" && token.raw === "{") {
            nextStringWillBeKey = true;
        } else if (token.type === "punctuation" && token.raw === "}") {
            stack.pop();
            nextStringWillBeKey = true;
        } else if (token.type === "string") {
            if (nextStringWillBeKey) {
                nextStringWillBeKey = false;
                stack.push(token.value);
            } else { //string we encountered is value
                // if (paths.includes(stack.join("."))) {
                if (stack[stack.length - 1] === "reference") {
                    highlights.push(token.position);
                }
                nextStringWillBeKey = true;
                stack.pop();
            }
        } else if (token.type === "literal" || token.type === "number") { //only as values allowed
            nextStringWillBeKey = true;
            stack.pop();
        }
    }

    return highlights;
}

module.exports.getPositionOfKeys = function (tokens) {
    var highlights = [];

    var nextStringWillBeKey = true; //Object key/value distinction
    for (var i in tokens) {
        //TODO: Handle arrays?!
        var token = tokens[i];
        if (token.type === "punctuation" && token.raw === "{") {
            nextStringWillBeKey = true;
        } else if (token.type === "punctuation" && token.raw === "}") {
            nextStringWillBeKey = true;
        } else if (token.type === "string") {
            if (nextStringWillBeKey) {
                nextStringWillBeKey = false;
                if (token.value !== "resourceType") {
                    highlights.push(token.position);
                }
            } else { //string we encountered is value
                nextStringWillBeKey = true;
            }
        } else if (token.type === "literal" || token.type === "number") { //only as values allowed
            nextStringWillBeKey = true;
        }
    }

    return highlights;
}

module.exports.getPathInObject = function (tokens) {
    var stack = [];

    var nextStringWillBeKey = true; //Object key/value distinction
    for (var i in tokens) {
        //TODO: Handle arrays?!
        var token = tokens[i];
        if (token.type === "punctuation" && token.raw === "{") {
            nextStringWillBeKey = true;
        } else if (token.type === "punctuation" && token.raw === "}") {
            stack.pop();
            nextStringWillBeKey = true;
        } else if (token.type === "punctuation" && token.raw === "[") {
            stack.push("[");
            nextStringWillBeKey = false;
        } else if (token.type === "punctuation" && token.raw === "]") {
            stack.pop();
            nextStringWillBeKey = true;
        } else if (token.type === "string") {
            if (nextStringWillBeKey) {
                nextStringWillBeKey = false;
                stack.push(token.value);
            } else { //string we encountered is value
                nextStringWillBeKey = true;
                stack.pop();
            }
        } else if (token.type === "literal" || token.type === "number") { //only as values allowed
            nextStringWillBeKey = true;
            stack.pop();
        }
    }

    return [stack.filter(it => it !== "["), !nextStringWillBeKey];
}
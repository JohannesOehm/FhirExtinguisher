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
        if (token.type === "punctuation" && (token.raw === "{" || token.raw === "}")) {
            nextStringWillBeKey = true;
        } else if (token.type === "string") {
            if (nextStringWillBeKey) {
                nextStringWillBeKey = false;
                if (token.value !== "resourceType") {
                    highlights.push(token);
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

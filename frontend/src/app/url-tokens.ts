import {languages} from "monaco-editor";
import IState = languages.IState;
import TokensProvider = languages.TokensProvider;

class UrlState implements IState {
    constructor(public value: string) {
    }

    clone(): languages.IState {
        return new UrlState(this.value);
    }

    equals(other: languages.IState): boolean {
        return (<UrlState>other).value === this.value;
    }

}

class UrlTokensProvider implements TokensProvider {
    private path = new UrlState("path");
    private queryKey = new UrlState("queryKey");
    private queryValue = new UrlState("queryValue");

    getInitialState(): languages.IState {
        return this.path;
    }

    tokenize(line: string, state: languages.IState): languages.ILineTokens {


        return {
            endState: this.queryValue,
            tokens: []
        };
    }

}
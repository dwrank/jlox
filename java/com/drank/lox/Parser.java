package com.drank.lox;

import java.util.List;

import com.drank.lox.TokenType;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    // expression -> comma ;
    private Expr expression() {
        return comma();
    }

    // comma -> conditional ("," conditional)* ;
    private Expr comma() {
        Expr expr = conditional();

        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = conditional();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // conditional -> equality ("?" expression ":" expression)?
    private Expr conditional() {
        Expr expr = equality();

        if (match(TokenType.QUESTION)) {
            Expr thenBranch = expression();

            consume(TokenType.COLON, "Expect ':' after expression.");

            // can not be an expression here or else you can not have a comma
            // after the conditional
            // 1 ? 2 : 3, 4  => (, (conditional 1.0 2.0 3.0) 4.0)
            // this is wrong => (, (conditional 1.0 2.0 (, 3.0 4.0)))
            Expr elseBranch = conditional();

            expr = new Expr.Conditional(expr, thenBranch, elseBranch);
        }

        return expr;
    }

    // equality -> comparison ( ("!=" | "==") comparison )* ;
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison -> term ( (">" | ">=" | "<" | "<=") term )* ;
    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                     TokenType.LESSER, TokenType.LESSER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // term -> factor ( ("-" | "+") factor )* ;
    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor -> unary ( ("/" | "*") unary )* ;
    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary -> ("!" | "-") unary
    //        -> | primary ;
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil" ;
    //         -> | "(" expression ")" ;
    //         Error productions ...
    //         -> ("!=", "==") equality ;
    //         -> ("<", "<=", ">", ">=") comparison ;
    //         -> ("+") term ;
    //         -> ("/", "*") factor ;
    private Expr primary() {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.TRUE)) { return new Expr.Literal(true); }
        if (match(TokenType.FALSE)) { return new Expr.Literal(false); }
        if (match(TokenType.NIL)) { return new Expr.Literal(null); }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // Error productions
        if (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            error(previous(), "Missing left-hand operand.");
            equality();
            return null;
        }

        if (match(TokenType.LESSER, TokenType.LESSER_EQUAL,
                  TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            error(previous(), "Missing left-hand operand.");
            comparison();
            return null;
        }

        if (match(TokenType.PLUS)) {
            error(previous(), "Missing left-hand operand.");
            term();
            return null;
        }

        if (match(TokenType.SLASH, TokenType.STAR)) {
            error(previous(), "Missing left-hand operand.");
            factor();
            return null;
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) { return false; }
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) { current++; }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) { return advance(); }

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) { return; }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}

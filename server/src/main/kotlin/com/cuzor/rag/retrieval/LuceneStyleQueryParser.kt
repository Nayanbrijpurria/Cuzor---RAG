package com.cuzor.rag.retrieval

/**
 * Parses a small Lucene-style Boolean query syntax.
 *
 * Supported:
 *
 *     word
 *     word AND word
 *     word OR word
 *     (word OR word) AND word
 *     (word OR word) AND (word OR word)
 *
 * This is NOT Apache Lucene.
 * It only provides Lucene-style Boolean query syntax
 * for our Cuzor RAG.
 */
object LuceneStyleQueryParser {

    sealed interface QueryNode {

        data class Term(
            val value: String
        ) : QueryNode

        data class And(
            val left: QueryNode,
            val right: QueryNode
        ) : QueryNode

        data class Or(
            val left: QueryNode,
            val right: QueryNode
        ) : QueryNode
    }

    private enum class TokenType {
        TERM,
        AND,
        OR,
        LEFT_PAREN,
        RIGHT_PAREN
    }

    private data class Token(
        val type: TokenType,
        val value: String
    )

    /**
     * Parse a query into a Boolean expression tree.
     */
    fun parse(query: String): QueryNode {

        val tokens = tokenize(query)

        if (tokens.isEmpty()) {
            throw IllegalArgumentException(
                "Search query cannot be empty."
            )
        }

        val parser = Parser(tokens)

        val result = parser.parseExpression()

        if (!parser.isAtEnd()) {
            throw IllegalArgumentException(
                "Unexpected token in search query."
            )
        }

        return result
    }

    /**
     * Determines whether the query contains explicit
     * Lucene-style Boolean syntax.
     */
    fun containsBooleanSyntax(query: String): Boolean {

        return Regex(
            """\b(AND|OR)\b|[()]""",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(query)
    }

    /**
     * Extract all terms from a parsed expression.
     *
     * Example:
     *
     * (hissing OR sound) AND (low OR backup)
     *
     * becomes:
     *
     * [hissing, sound, low, backup]
     */
    fun extractTerms(node: QueryNode): List<String> {

        return when (node) {

            is QueryNode.Term -> {
                listOf(node.value)
            }

            is QueryNode.And -> {
                extractTerms(node.left) +
                        extractTerms(node.right)
            }

            is QueryNode.Or -> {
                extractTerms(node.left) +
                        extractTerms(node.right)
            }
        }
    }

    fun matches(
        node: QueryNode,
        text: String
    ): Boolean {
        return when (node) {

            is QueryNode.Term -> {
                text.contains(
                    node.value,
                    ignoreCase = true
                )
            }

            is QueryNode.And -> {
                matches(node.left, text) &&
                        matches(node.right, text)
            }

            is QueryNode.Or -> {
                matches(node.left, text) ||
                        matches(node.right, text)
            }
        }
    }

    private fun tokenize(query: String): List<Token> {

        val tokens = mutableListOf<Token>()

        var index = 0

        while (index < query.length) {

            val character = query[index]

            when {

                character.isWhitespace() -> {
                    index++
                }

                character == '(' -> {

                    tokens.add(
                        Token(
                            TokenType.LEFT_PAREN,
                            "("
                        )
                    )

                    index++
                }

                character == ')' -> {

                    tokens.add(
                        Token(
                            TokenType.RIGHT_PAREN,
                            ")"
                        )
                    )

                    index++
                }

                else -> {

                    val start = index

                    while (
                        index < query.length &&
                        !query[index].isWhitespace() &&
                        query[index] != '(' &&
                        query[index] != ')'
                    ) {
                        index++
                    }

                    val word =
                        query.substring(
                            start,
                            index
                        )

                    when {

                        word.equals(
                            "AND",
                            ignoreCase = true
                        ) -> {

                            tokens.add(
                                Token(
                                    TokenType.AND,
                                    word
                                )
                            )
                        }

                        word.equals(
                            "OR",
                            ignoreCase = true
                        ) -> {

                            tokens.add(
                                Token(
                                    TokenType.OR,
                                    word
                                )
                            )
                        }

                        else -> {

                            tokens.add(
                                Token(
                                    TokenType.TERM,
                                    word
                                )
                            )
                        }
                    }
                }
            }
        }

        return tokens
    }

    private class Parser(
        private val tokens: List<Token>
    ) {

        private var position = 0

        fun isAtEnd(): Boolean {

            return position >= tokens.size
        }

        /**
         * Lowest precedence:
         *
         * OR
         */
        fun parseExpression(): QueryNode {

            var left =
                parseAndExpression()

            while (
                !isAtEnd() &&
                current().type == TokenType.OR
            ) {

                advance()

                val right =
                    parseAndExpression()

                left =
                    QueryNode.Or(
                        left,
                        right
                    )
            }

            return left
        }

        /**
         * Higher precedence:
         *
         * AND
         */
        private fun parseAndExpression(): QueryNode {

            var left =
                parsePrimary()

            while (
                !isAtEnd() &&
                current().type == TokenType.AND
            ) {

                advance()

                val right =
                    parsePrimary()

                left =
                    QueryNode.And(
                        left,
                        right
                    )
            }

            return left
        }

        /**
         * Highest precedence:
         *
         * Terms and parentheses.
         */
        private fun parsePrimary(): QueryNode {

            if (isAtEnd()) {

                throw IllegalArgumentException(
                    "Unexpected end of search query."
                )
            }

            val token =
                current()

            return when (token.type) {

                TokenType.TERM -> {

                    advance()

                    QueryNode.Term(
                        token.value
                    )
                }

                TokenType.LEFT_PAREN -> {

                    advance()

                    val expression =
                        parseExpression()

                    if (
                        isAtEnd() ||
                        current().type !=
                        TokenType.RIGHT_PAREN
                    ) {

                        throw IllegalArgumentException(
                            "Missing closing parenthesis."
                        )
                    }

                    advance()

                    expression
                }

                else -> {

                    throw IllegalArgumentException(
                        "Expected a search term."
                    )
                }
            }
        }

        private fun current(): Token {

            return tokens[position]
        }

        private fun advance() {

            position++
        }
    }
}
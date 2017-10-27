package com.spcore.spmobileapi.helpers

class Strings {

    class Words(vararg words: String) : ArrayList<String>() {
        init {
            this.addAll(words)
        }

        constructor(words: List<String>) : this(*words.toTypedArray())

        /**
         * Render this Words instance to a string with a given CapitalizationStyle and
         * word delimiter
         */
        fun render(capitalizationStyle: CapitalizationStyle = CapitalizationStyle.Sentence_case,
                   delimiter: String = " "): String {

            if (size == 0)
                return ""

            when (capitalizationStyle) {

                CapitalizationStyle.CAPITALIZED ->
                    return this.toString(delimiter).toUpperCase()

                CapitalizationStyle.Title_Case -> {
                    this.mutate{it.toOnlyFirstCharCapitalized()}
                }

                CapitalizationStyle.Sentence_case -> {
                    this[0] = this[0].toOnlyFirstCharCapitalized()
                    this.mutate(1) {it.toLowerCase()}
                }

                CapitalizationStyle.lower_case ->
                    return this.toString(delimiter).toLowerCase()
            }

            return this.toString(delimiter)
        }

        override fun toString(): String = toString(" ")
        fun toString(delimiter: String) = this.reduce { a, x -> "$a$delimiter$x"}

        enum class CapitalizationStyle {
            Sentence_case,
            Title_Case,
            lower_case,
            CAPITALIZED
        }
    }

    enum class WordDelimiterType {
        UNDERSCORE_DELIMITED,   // Multiple simultaneous underscores treated as one delimiter
        CAMELCASE_DELIMITED,     // Can be UpperCamel or lowerCamel
        WHITESPACE_DELIMITED  // Any type and quantity of whitespace treated as one delimiter
    }
}

fun String.toWords(delimiterType: Strings.WordDelimiterType): Strings.Words {
    return when (delimiterType) {
        Strings.WordDelimiterType.UNDERSCORE_DELIMITED ->
            this.split(Regex("""_+""")).toWords()

        Strings.WordDelimiterType.CAMELCASE_DELIMITED ->
            this.replace("(?<=[A-Z])(?=[A-Z][a-z])|" +
                    "(?<=[^A-Z])(?=[A-Z])|" +
                    "(?<=[A-Za-z])(?=[^A-Za-z])",
                    " ")
                    .split(" ").toWords()

        Strings.WordDelimiterType.WHITESPACE_DELIMITED ->
            this.split(Regex("""\s+""")).toWords()
    }
}

fun List<String>.toWords() = Strings.Words(this)

/**
 * Capitalize the first character and makes the rest lowercase
 */
fun String.toOnlyFirstCharCapitalized() = this.toLowerCase().capitalize()
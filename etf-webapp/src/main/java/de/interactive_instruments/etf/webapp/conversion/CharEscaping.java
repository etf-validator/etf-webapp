/**
 * Copyright 2010-2022 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.webapp.conversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.interactive_instruments.container.Pair;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class CharEscaping {

    private static Pair<Pattern, String>[] REPLACEMENTS = new Pair[]{
            new Pair<>(Pattern.compile("&uuml;", Pattern.LITERAL), Matcher.quoteReplacement("ü")),
            new Pair<>(Pattern.compile("&Uuml;", Pattern.LITERAL), Matcher.quoteReplacement("Ü")),
            new Pair<>(Pattern.compile("&auml;", Pattern.LITERAL), Matcher.quoteReplacement("ä")),
            new Pair<>(Pattern.compile("&Auml;", Pattern.LITERAL), Matcher.quoteReplacement("Ä")),
            new Pair<>(Pattern.compile("&ouml;", Pattern.LITERAL), Matcher.quoteReplacement("ö")),
            new Pair<>(Pattern.compile("&Ouml;", Pattern.LITERAL), Matcher.quoteReplacement("Ö")),
            new Pair<>(Pattern.compile("&sect;", Pattern.LITERAL), Matcher.quoteReplacement("§")),
            new Pair<>(Pattern.compile("&acirc;", Pattern.LITERAL), Matcher.quoteReplacement("â")),
            new Pair<>(Pattern.compile("&aring;", Pattern.LITERAL), Matcher.quoteReplacement("å")),
            new Pair<>(Pattern.compile("&acute;", Pattern.LITERAL), Matcher.quoteReplacement("´")),
            new Pair<>(Pattern.compile("&grave;", Pattern.LITERAL), Matcher.quoteReplacement("`")),
            new Pair<>(Pattern.compile("&aacute;", Pattern.LITERAL), Matcher.quoteReplacement("á")),
            new Pair<>(Pattern.compile("&agrave;", Pattern.LITERAL), Matcher.quoteReplacement("à")),
            new Pair<>(Pattern.compile("&eacute;", Pattern.LITERAL), Matcher.quoteReplacement("é")),
            new Pair<>(Pattern.compile("&egrave;", Pattern.LITERAL), Matcher.quoteReplacement("è")),
            new Pair<>(Pattern.compile("&Ccedil;", Pattern.LITERAL), Matcher.quoteReplacement("Ç")),
            new Pair<>(Pattern.compile("&ccedil;", Pattern.LITERAL), Matcher.quoteReplacement("ç")),
            new Pair<>(Pattern.compile("&szlig;", Pattern.LITERAL), Matcher.quoteReplacement("ß")),
            new Pair<>(Pattern.compile("&#39;", Pattern.LITERAL), Matcher.quoteReplacement("'")),
            new Pair<>(Pattern.compile("&quot;", Pattern.LITERAL), Matcher.quoteReplacement("'")),
            new Pair<>(Pattern.compile("&#34;", Pattern.LITERAL), Matcher.quoteReplacement("'")),
            new Pair<>(Pattern.compile("&apos;", Pattern.LITERAL), Matcher.quoteReplacement("'")),
            new Pair<>(Pattern.compile("&deg;", Pattern.LITERAL), Matcher.quoteReplacement("°"))
    };

    private CharEscaping() {}

    public static String unescapeSpecialChars(String str) {
        for (final Pair<Pattern, String> replacement : REPLACEMENTS) {
            str = replacement.getLeft().matcher(str).replaceAll(replacement.getRight());
        }
        return str.trim();
    }
}

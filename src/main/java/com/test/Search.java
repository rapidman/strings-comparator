package com.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author timur
 * @since 13.04.2023
 */
public class Search {

  private static final Map<String, List<String>> DICTIONARY = new HashMap<>();
  private static final Set<String> STOP_WORDS = new HashSet<>();

  static {
    DICTIONARY.computeIfAbsent("пер.", s -> new ArrayList<>()).add("переулок");
    DICTIONARY.computeIfAbsent("пер.", s -> new ArrayList<>()).add("пер-ок");
    DICTIONARY.computeIfAbsent("пр.", s -> new ArrayList<>()).add("проезд");
    DICTIONARY.computeIfAbsent("пр.", s -> new ArrayList<>()).add("пр-д");
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Expected two params");
      printUsage();
    }
    String input = args[0];
    String compareWith = args[1];
    double ratio = getStringDiffRatio(input, compareWith);
    System.out.println(ratio);
  }

  public static double getStringDiffRatio(String input, String compareWith) {
    List<StringToken> inputLexems = new LexemsBuilder(input)
        .withDictionaryLexems(new DictionaryLexemsProvider(DICTIONARY))
        .withStopWords(STOP_WORDS)
        .build();

    List<StringToken> compareWithLexems = new LexemsBuilder(compareWith)
        .withDictionaryLexems(new DictionaryLexemsProvider(DICTIONARY))
        .withStopWords(STOP_WORDS)
        .build();

    return getStringDiffRatio(inputLexems, compareWithLexems);
  }

  private static void printUsage() {
    System.out.println("Usage example: java com.test.Search 'Малый пер.' 'Малый пр.'");
  }

  private static double getStringDiffRatio(List<StringToken> inputLexems,
      List<StringToken> compareWithLexems) {
    long inputLexemsCount = inputLexems.stream()
        .filter(stringToken -> !stringToken.isDictionaryLexem()).count();
    long foundLexemsCount = 0;
    for (StringToken inputLexem : inputLexems) {
      if (compareWithLexems.contains(inputLexem)) {
        foundLexemsCount++;
      }
    }
    if (foundLexemsCount > inputLexemsCount) {
      // all inputs matched + dictionary lexems, decrease count to be 100% matches
      foundLexemsCount = inputLexemsCount;
    }
    return (double) foundLexemsCount / (double) inputLexemsCount;
  }


  private static class LexemsBuilder {

    private final Set<String> stopWords = new HashSet<>();
    private final String phrase;
    private DictionaryLexemsProvider dictionaryLexemsProvider;

    private LexemsBuilder(String phrase) {
      this.phrase = phrase;
    }

    LexemsBuilder withDictionaryLexems(DictionaryLexemsProvider dictionaryLexemsProvider) {
      this.dictionaryLexemsProvider = dictionaryLexemsProvider;
      return this;
    }

    LexemsBuilder withStopWords(Collection<String> stopWords) {
      this.stopWords.addAll(stopWords);
      return this;
    }

    List<StringToken> build() {
      List<String> initialInputTokens = Arrays.stream(phrase.trim().split("\\s+"))
          .map(s -> s.trim())
          .collect(Collectors.toList());
      List<String> allInputTokens = initialInputTokens.stream()
          .map(token -> dictionaryLexemsProvider.getLexems(token))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
      allInputTokens.removeAll(stopWords);
      List<StringToken> result = new ArrayList<>(allInputTokens.size());
      for (int i = 0; i < allInputTokens.size(); i++) {
        result.add(new StringToken(allInputTokens.get(i), i,
            !initialInputTokens.contains(allInputTokens.get(i))));
      }

      return result;
    }

  }

  private static class DictionaryLexemsProvider {

    private final Map<String, List<String>> dictionary;

    public DictionaryLexemsProvider(Map<String, List<String>> dictionary) {
      this.dictionary = dictionary;
    }

    List<String> getLexems(String key) {
      List<String> result = new ArrayList<>(Collections.singletonList(key));
      List<String> foundLexems = dictionary.get(key);
      if (foundLexems != null) {
        result.addAll(foundLexems);
      }
      return result;
    }

  }

  private static class StringToken {

    private final boolean dictionaryLexem;
    private final String token;
    private final int tokenOrder;

    private StringToken(String token, int tokenOrder, boolean dictionaryLexem) {
      this.dictionaryLexem = dictionaryLexem;
      Objects.requireNonNull(token, "token must be not null");
      this.token = token;
      this.tokenOrder = tokenOrder;
    }

    public boolean isDictionaryLexem() {
      return dictionaryLexem;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || StringToken.class != o.getClass()) {
        return false;
      }

      StringToken that = (StringToken) o;
      return tokenOrder == that.tokenOrder && dictionaryLexem == that.dictionaryLexem
          && token.equals(
          that.token);
    }

    public int hashCode() {
      return Objects.hash(token, tokenOrder, dictionaryLexem);
    }
  }
}

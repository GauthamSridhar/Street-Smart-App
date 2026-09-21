package com.shopapp.ShopService.service;

/** H2 test equivalent of PostgreSQL's bounded, unit-cost Levenshtein function. */
public final class EditDistance {
  private EditDistance() {}

  public static int distance(String first, String second, int maximum) {
    int[] a = first.codePoints().toArray(), b = second.codePoints().toArray();
    if (Math.abs(a.length - b.length) > maximum) return maximum + 1;
    int[] prior = new int[b.length + 1];
    for (int j = 0; j <= b.length; j++) prior[j] = j;
    for (int i = 1; i <= a.length; i++) {
      int[] row = new int[b.length + 1];
      row[0] = i;
      for (int j = 1; j <= b.length; j++)
        row[j] =
            Math.min(
                Math.min(row[j - 1] + 1, prior[j] + 1),
                prior[j - 1] + (a[i - 1] == b[j - 1] ? 0 : 1));
      prior = row;
    }
    return Math.min(maximum + 1, prior[b.length]);
  }
}

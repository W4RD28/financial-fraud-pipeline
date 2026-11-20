package com.fraud.common.dto.api;

public record PageMeta(
    int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {

  public static PageMeta ofZeroIndexed(
      int zeroBasedPage,
      int size,
      long totalElements,
      int totalPages,
      boolean hasNext,
      boolean hasPrevious) {
    return new PageMeta(zeroBasedPage + 1, size, totalElements, totalPages, hasNext, hasPrevious);
  }
}

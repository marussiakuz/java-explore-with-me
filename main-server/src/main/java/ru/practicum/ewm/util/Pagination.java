package ru.practicum.ewm.util;

import lombok.Data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class Pagination implements Pageable {
    private final int from;
    private final int size;
    private final Sort sort;

    private Pagination(int from, int size, Sort sort) {
        this.from = from;
        this.size = size;
        this.sort = sort;
    }

    private Pagination(int from, int size) {
        this.from = from;
        this.size = size;
        sort = Sort.unsorted();
    }

    @Override
    public int getPageNumber() {
        return from + 1;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return from;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new Pagination(from + size, size, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        if ((from - size) > 0) return new Pagination(from - size, size, sort);
        return new Pagination(0, size, sort);
    }

    @Override
    public Pageable first() {
        return new Pagination(0, size, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new Pagination(from + size * pageNumber, size, sort);
    }

    @Override
    public boolean hasPrevious() {
        return from != 0;
    }

    public static Pageable of(int from, int size) {
        return new Pagination(from, size);
    }

    public static Pageable of(int from, int size, Sort sort) {
        return new Pagination(from, size, sort);
    }
}

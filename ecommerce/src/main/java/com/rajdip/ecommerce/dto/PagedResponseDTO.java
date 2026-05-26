package com.rajdip.ecommerce.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination response wrapper.
 *
 * Wraps Spring's Page<T> into a clean API response that includes:
 *  - content       : the data list for the current page
 *  - page          : current page number (0-indexed)
 *  - size          : page size (items per page)
 *  - totalElements : grand total of matching records
 *  - totalPages    : total number of pages
 *  - first / last  : boundary flags
 *  - sortBy        : the field used for sorting
 *  - sortDir       : asc or desc
 */
public class PagedResponseDTO<T> {

    private List<T> content;

    private int  page;
    private int  size;
    private long totalElements;
    private int  totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    private String sortBy;
    private String sortDir;

    // ── Constructor from Spring Page ──────────────────────────────────────────

    public PagedResponseDTO(Page<T> springPage, String sortBy, String sortDir) {
        this.content       = springPage.getContent();
        this.page          = springPage.getNumber();
        this.size          = springPage.getSize();
        this.totalElements = springPage.getTotalElements();
        this.totalPages    = springPage.getTotalPages();
        this.first         = springPage.isFirst();
        this.last          = springPage.isLast();
        this.empty         = springPage.isEmpty();
        this.sortBy        = sortBy;
        this.sortDir       = sortDir;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<T> getContent()       { return content; }
    public int     getPage()          { return page; }
    public int     getSize()          { return size; }
    public long    getTotalElements() { return totalElements; }
    public int     getTotalPages()    { return totalPages; }
    public boolean isFirst()          { return first; }
    public boolean isLast()           { return last; }
    public boolean isEmpty()          { return empty; }
    public String  getSortBy()        { return sortBy; }
    public String  getSortDir()       { return sortDir; }
}

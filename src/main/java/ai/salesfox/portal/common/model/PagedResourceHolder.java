package ai.salesfox.portal.common.model;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

public class PagedResourceHolder<T> {
    @Getter
    private final Page<T> firstPage;
    private final Function<Pageable, Page<T>> retrieveNextPageFunction;

    public PagedResourceHolder(Page<T> firstPage, Function<Pageable, Page<T>> retrieveNextPageFunction) {
        this.firstPage = firstPage;
        this.retrieveNextPageFunction = retrieveNextPageFunction;
    }

    public final Page<T> retrieveNextPage(Page<T> currentPage) {
        if (currentPage.hasNext()) {
            return retrieveNextPageFunction.apply(currentPage.nextPageable());
        }
        return Page.empty();
    }

}

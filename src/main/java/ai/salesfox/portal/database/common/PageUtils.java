package ai.salesfox.portal.database.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PageUtils {
    public static final int DEFAULT_INTERNAL_PAGE_SIZE = 1000;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public static <T> List<T> retrieveAll(Function<PageRequest, Page<T>> requestPage) {
        return retrieveAll(requestPage, DEFAULT_INTERNAL_PAGE_SIZE);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public static <T> List<T> retrieveAll(Function<PageRequest, Page<T>> requestPage, int pageSize) {
        List<T> allEntries = new ArrayList<>(pageSize);

        int pageNumber = 0;
        Page<T> currentPage;
        do {
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
            currentPage = requestPage.apply(pageRequest);
            allEntries.addAll(currentPage.getContent());
            pageNumber++;
        } while (currentPage.hasNext());
        return allEntries;
    }

}

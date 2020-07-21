package com.getboostr.portal.database.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PageUtilsTest {
    @Test
    public void retrieveAllTest() {
        int pageSize = 10;
        int totalElements = 105;
        LinkedHashMap<Integer, String> testData = createTestData(totalElements);
        Function<PageRequest, Page<String>> requestStringFunction = pageRequest -> getPageOfTestData(testData, pageRequest);

        List<String> retrievedData = PageUtils.retrieveAll(requestStringFunction, pageSize);
        assertEquals(testData.size(), retrievedData.size());

        List<String> testDataValues = new ArrayList<>(testData.values());
        for (int i = 0; i < totalElements; i++) {
            assertEquals(testDataValues.get(i), retrievedData.get(i));
        }
    }

    private LinkedHashMap<Integer, String> createTestData(int size) {
        LinkedHashMap<Integer, String> uniqueStrings = new LinkedHashMap<>(size * 2, 0.75f);
        for (int i = 0; i < size; i++) {
            uniqueStrings.put(i, "element_" + i);
        }
        return uniqueStrings;
    }

    private Page<String> getPageOfTestData(LinkedHashMap<Integer, String> testData, PageRequest pageRequest) {
        int requestedElementIndex = pageRequest.getPageNumber() * pageRequest.getPageSize();
        int upperBoundIndex = requestedElementIndex + pageRequest.getPageSize();

        List<String> pageOfStrings = new ArrayList<>(pageRequest.getPageSize());
        for (; requestedElementIndex < upperBoundIndex; requestedElementIndex++) {
            String requestedElement = testData.get(requestedElementIndex);
            if (requestedElement != null) {
                pageOfStrings.add(requestedElement);
            } else {
                break;
            }
        }
        return new PageImpl<>(pageOfStrings, pageRequest, testData.size());
    }

}

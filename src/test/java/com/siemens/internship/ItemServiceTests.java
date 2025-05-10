package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemServiceTests {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem1;
    private Item testItem2;

    @BeforeEach
    void setUp() {

        itemRepository.deleteAll();

        testItem1 = new Item();
        testItem1.setName("Test Item 1");
        testItem1.setDescription("Test Description 1");
        testItem1.setEmail("test1@example.com");

        testItem2 = new Item();
        testItem2.setName("Test Item 2");
        testItem2.setDescription("Test Description 2");
        testItem2.setEmail("test2@example.com");

        testItem1 = itemRepository.save(testItem1);
        testItem2 = itemRepository.save(testItem2);
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
    }

    @Test
    void testFindAllItems() {
        List<Item> items = itemService.findAll();

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(item -> item.getName().equals("Test Item 1")));
        assertTrue(items.stream().anyMatch(item -> item.getName().equals("Test Item 2")));
    }

    @Test
    void testFindItemById() {
        Optional<Item> foundItem = itemService.findById(testItem1.getId());

        assertTrue(foundItem.isPresent());
        assertEquals("Test Item 1", foundItem.get().getName());
    }

    @Test
    void testItemNotFound() {
        Optional<Item> foundItem = itemService.findById(999L);

        assertFalse(foundItem.isPresent());
    }

    @Test
    void testDeleteItemById() {
        long initialCount = itemRepository.count();

        itemService.deleteById(testItem1.getId());

        assertEquals(initialCount - 1, itemRepository.count());
        assertFalse(itemRepository.findById(testItem1.getId()).isPresent());
    }

    @Test
    void testProcessItemsAsync() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get(5, TimeUnit.SECONDS);

        assertEquals(2, processedItems.size());

        for (Item item : processedItems) {
            assertEquals("PROCESSED", item.getStatus());
        }

        List<Item> itemsInDb = itemRepository.findAll();
        for (Item item : itemsInDb) {
            assertEquals("PROCESSED", item.getStatus());
        }
    }
}

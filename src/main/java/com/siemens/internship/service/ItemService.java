package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    /**
     * added an emailValidator object
     */
    private final EmailValidator emailValidator;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * changed the variable type from int to AtomicInteger, and ArrayList to
     * CopyOnWriteArrayList for thread safety
     */
    private final List<Item> processedItems = new CopyOnWriteArrayList<>();
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public ItemService(EmailValidator emailValidator) {
        this.emailValidator = emailValidator;
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Verify if the email is valid in service layer, with an auxiliary class from validator package
     */
    public Item save(Item item) {
        if(!emailValidator.validateEmail(item.getEmail())){
            throw new IllegalArgumentException("Invalid Email format: " + item.getEmail());
        }
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */


    /**
     * modified the returning type to CompletableFuture<List<Item>>
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(100);

                        itemRepository.findById(id).ifPresent(item -> {
                            item.setStatus("PROCESSED");
                            itemRepository.save(item);

                            processedItems.add(item);
                            processedCount.incrementAndGet();
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(e);
                    }
                }, executor))
                .toList();

        // with allOf we wait for every task to end
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems);
    }

}


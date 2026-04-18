package com.medtracker.controller;

import com.medtracker.model.ShoppingList;
import com.medtracker.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shopping-list")
public class ShoppingListController {

    @Autowired
    private ShoppingListService shoppingListService;

    @GetMapping
    public List<ShoppingList> getAll() {
        return shoppingListService.getAll();
    }
}
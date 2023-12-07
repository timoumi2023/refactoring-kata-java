package com.sipios.refactoring.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.sipios.refactoring.model.Body;
import com.sipios.refactoring.model.Item;

@RestController
@RequestMapping("/shopping")
public class ShoppingController {

    private Logger logger = LoggerFactory.getLogger(ShoppingController.class);
    private static final String STANDARD_CUSTOMER = "STANDARD_CUSTOMER";
    private static final String PREMIUM_CUSTOMER = "PREMIUM_CUSTOMER";
    private static final String PLATINUM_CUSTOMER = "PLATINUM_CUSTOMER";

    @PostMapping
    public String getPrice(@RequestBody Body body) {
        double price = calculateTotalPrice(body);
        validatePrice(body.getType(), price);
        return String.valueOf(price);
    }

    //calculer les totaux des prix aprés discount
    private double calculateTotalPrice(Body body) {
        double discount = calculateDiscount(body.getType());
        double totalPrice = 0;

        if (body.getItems() != null) {
            for (Item item : body.getItems()) {
                totalPrice += calculateItemPrice(item, discount);
            }
        }

        return totalPrice;
    }

    //determiner le % de discount
    private double calculateDiscount(String customerType) {
        switch (customerType) {
            case STANDARD_CUSTOMER:
                return 1.0;
            case PREMIUM_CUSTOMER:
                return 0.9;
            case PLATINUM_CUSTOMER:
                return 0.5;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    // calculer les prix de chaque item en fonction de discount
    private double calculateItemPrice(Item item, double discount) {
        switch (item.getType()) {
            case "TSHIRT":
                return 30 * item.getNb() * discount;
            case "DRESS":
                return 50 * item.getNb() * (isSummerDiscount() ? 0.8 : 1) * discount;
            case "JACKET":
                return 100 * item.getNb() * (isSummerDiscount() ? 0.9 : 1) * discount;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    //determiner la saison de discount
    private boolean isSummerDiscount() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        return (dayOfMonth > 5 && dayOfMonth < 15) && (month == 0 || month == 5);
    }

    private void validatePrice(String customerType, double price) {
        double maxPrice = getMaxPrice(customerType);
        if (price > maxPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price (" + price + ") is too high for " + customerType + " customer");
        }
    }

    //determiner le prix selon la categorie de client
    private double getMaxPrice(String customerType) {
        switch (customerType) {
            case STANDARD_CUSTOMER:
                return 200;
            case PREMIUM_CUSTOMER:
                return 800;
            case PLATINUM_CUSTOMER:
                return 2000;
            default:
                return 200;
        }
    }
}


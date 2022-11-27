package com.bittoo.cart.resource;

import com.bittoo.cart.model.CartItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartResourceTest {


    CartResource cartResource = new CartResource();

    @Test
    public void testMerge() {
        CartItem c1 = cartItem("id1", 5,"item1");
        CartItem c2 = cartItem("id2", 3,"item1");
        CartItem c3 = cartItem("id3", 3,"item2");
        List<CartItem> toMerge = Arrays.asList(c1, c2, c3);
        List<CartItem> merged = cartResource.mergeItems(toMerge);
        assertEquals(8, merged.get(0).getQuantity());
    }

    private CartItem cartItem(String cartItemId, int quantity, String itemId) {
        CartItem c = new CartItem();
        c.setId(cartItemId);
        c.setQuantity(quantity);
        c.setItemId(itemId);
        return c;
    }
}

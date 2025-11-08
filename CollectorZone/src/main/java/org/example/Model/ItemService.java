package org.example.Model;

import java.util.*;


public class ItemService {

    private static final Map<String, CollectibleItem> itemDatabase = new HashMap<>();

    static {
        CollectibleItem item1 = new CollectibleItem();
        item1.setId(UUID.randomUUID().toString());
        item1.setName("Gorra autografiada por Peso Pluma");
        item1.setDescription("Una gorra autografiada por el famoso Peso Pluma");
        item1.setPrice(621.3);

        CollectibleItem item2 = new CollectibleItem();
        item2.setId(UUID.randomUUID().toString());
        item2.setName("Casco autografiado por Rosalía");
        item2.setDescription("Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!");
        item2.setPrice(734.57);

        CollectibleItem item3 = new CollectibleItem();
        item3.setId(UUID.randomUUID().toString());
        item3.setName("Chamarra de Bad Bunny");
        item3.setDescription("Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista");
        item3.setPrice(521.89);

        CollectibleItem item4 = new CollectibleItem();
        item4.setId(UUID.randomUUID().toString());
        item4.setName("Guitarra de Fernando Delgadillo");
        item4.setDescription("Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo");
        item4.setPrice(823.12);

        CollectibleItem item5 = new CollectibleItem();
        item5.setId(UUID.randomUUID().toString());
        item5.setName("Jersey firmado por Snoop Dogg");
        item5.setDescription("Un jersey autografiado por el legendario rapero Snoop Dogg");
        item5.setPrice(355.67);

        CollectibleItem item6 = new CollectibleItem();
        item6.setId(UUID.randomUUID().toString());
        item6.setName("Gorra firmado por Snoop Dogg");
        item6.setDescription("Un jersey autografiado por el legendario rapero Snoop Dogg");
        item6.setPrice(480.4);

        List.of(item1, item2, item3, item4, item5, item6)
                .forEach(i -> itemDatabase.put(i.getId().toString(), i));
    }

    public Collection<CollectibleItem> getAllItems() {
        return itemDatabase.values();
    }

    public CollectibleItem getItemById(String id) {
        CollectibleItem item = itemDatabase.get(id);
        if (item == null) {

            throw new ApiException(404, "Item not found");
        }
        return item;
    }

    public CollectibleItem createItem(String id, CollectibleItem item) {
        if (itemDatabase.containsKey(id)) {

            throw new ApiException(409, "Item with this ID already exists");
        }

        item.setId(id);
        itemDatabase.put(id, item);
        return item;
    }

    public CollectibleItem updateItem(String id, CollectibleItem item) {
        if (!itemDatabase.containsKey(id)) {

            throw new ApiException(404, "Item not found, cannot update");
        }
        item.setId(id);
        itemDatabase.put(id, item);
        return item;
    }

    public void deleteItem(String id) {
        if (itemDatabase.remove(id) == null) {

            throw new ApiException(404, "Item not found");
        }

    }

    public void itemExists(String id) {
        if (!itemDatabase.containsKey(id)) {

            throw new ApiException(404, "Item not found");
        }

    }
}
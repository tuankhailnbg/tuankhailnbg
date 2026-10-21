/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.card;

import nro.models.player.Player;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class CollectionBook {

    @Getter
    @Setter
    private List<Card> cards;
    private Player player;

    public CollectionBook(Player player) {
        this.player = player;
    }

    public void init() {
        List<CardTemplate> cardTemplates = CardManager.getInstance().getCardTemplates();
        if (cards.size() < cardTemplates.size()) {
            for (CardTemplate cardT : cardTemplates) {
                Card card = find(cardT.getId());
                if (card == null) {
                    Card cardNew = new Card();
                    cardNew.setId(cardT.getId());
                    cards.add(cardNew);
                }
            }
        }
        for (Card card : cards) {
            card.setTemplate();
        }
    }

    public void add(Card card) {
        cards.add(card);
    }

    public void remove(Card card) {
        cards.remove(card);
    }

    public Card find(int id) {
        for (Card card : cards) {
            if (card.getId() == id) {
                return card;
            }
        }
        return null;
    }

    public Card findWithItemID(int id) {
        for (Card card : cards) {
            if (card.getCardTemplate().getItemID() == id) {
                return card;
            }
        }
        return null;
    }

    public Card findWithMobID(int id) {
        for (Card card : cards) {
            if (card.getCardTemplate().getMobID() == id) {
                return card;
            }
        }
        return null;
    }
}

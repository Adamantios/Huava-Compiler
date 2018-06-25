package org.hua.symbol;

import java.util.Collection;

/**
 * Symbol table
 *
 * @param <E> The type of objects that can be stored in the symbol table.
 */
public interface SymTable<E> {

    /**
     * Lookup a symbol in the symbol table.
     *
     * @param s The name of the symbol
     * @return The entry for the symbol or null if not found.
     */
    E lookup(String s);

    /**
     * Lookup a symbol in the symbol table.
     *
     * @param s The name of the symbol
     * @return The entry for the symbol or null if not found.
     */
    E lookupOnlyInTop(String s);

    /**
     * Add a new symbol table entry.
     *
     * @param s The name of the new entry
     * @param symbol The actual entry
     */
    void put(String s, E symbol);

    /**
     * Get all the symbols available in this symbol table.
     *
     * @return A collection of symbols.
     */
    Collection<E> getSymbols();

    /**
     * Get all the symbols available in this symbol table.
     *
     * @return A collection of symbols.
     */
    Collection<E> getSymbolsOnlyInTop();

    /**
     * Clear all symbol entries.
     */
    void clearOnlyInTop();

}
